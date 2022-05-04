package ch.skyfy.fk.logic;

import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.data.Cube;
import ch.skyfy.fk.events.*;
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectInputFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@SuppressWarnings("FieldCanBeLocal")
public class FKGame {

    private final MinecraftServer server;

    public Timeline timeline;

    private final NotStartedEvents notStartedEvents;
    private final PauseEvents pauseEvents;

    private final FKGameEvents fkGameEvents;

    private final Map<String, Vec3d> playerPositionWhenPaused;

    public FKGame(MinecraftServer server) {
        this.server = server;
        this.timeline = new Timeline(server);
        notStartedEvents = new NotStartedEvents();
        pauseEvents = new PauseEvents();
        fkGameEvents = new FKGameEvents();
        this.playerPositionWhenPaused = new HashMap<>();

        setWorldSpawn();
        registerEvents();
    }

    @SuppressWarnings("ConstantConditions")
    public void start() {
        server.getOverworld().setTimeOfDay(0);
        timeline.startTimer();

        // Send a message to all fk player to tell them where their respective base is
        for (ServerPlayerEntity fkPlayer : GameUtils.getAllConnectedFKPlayers(server.getPlayerManager().getPlayerList())) {
            var baseLoc = GameUtils.getBaseCoordinateByPlayer(fkPlayer.getName().asString());
            var message = new LiteralText("Your base is at this coords: X: " + baseLoc.getX() + " Y: " + baseLoc.getY() + " Z: " + baseLoc.getZ())
                    .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE));
            fkPlayer.sendMessage(message, false);
        }
    }

    public void pause() {
        // Records the position of the players. Will prevent them from moving when the game is paused.
        for (var fkPlayer : GameUtils.getAllConnectedFKPlayers(server.getPlayerManager().getPlayerList()))
            playerPositionWhenPaused.putIfAbsent(fkPlayer.getUuidAsString(), new Vec3d(fkPlayer.getX(), fkPlayer.getY(), fkPlayer.getZ()));
    }

    public void resume() {
        // If the timeline wasn't started (in the case of a server restart with gamestate at PAUSE OR RUNNING)
        if (!timeline.getIsStartedRef().get()) {
            timeline.startTimer();
        }

        playerPositionWhenPaused.clear();
    }

    private void updateTeam(MinecraftServer server, ServerPlayerEntity player) {
        var playerName = player.getName().asString();

        var serverScoreboard = server.getScoreboard();

        var fkTeam = GameUtils.getFKTeamOfPlayerByName(playerName);
        if (fkTeam == null) return;

        var team = serverScoreboard.getTeam(fkTeam.getName());
        if (team == null) { // Create a new team
            team = serverScoreboard.addTeam(fkTeam.getName());
            team.setColor(Formatting.byName(fkTeam.getColor()));
        }

        var playerTeam = serverScoreboard.getPlayerTeam(playerName);
        if (playerTeam == null) { // Player has no team
            serverScoreboard.addPlayerToTeam(playerName, team);
        }

        serverScoreboard.updateScoreboardTeamAndPlayers(team);
        serverScoreboard.updateScoreboardTeam(team);

    }

    private void setWorldSpawn(){
        var spawnLocation = Configs.FK.config.worldSpawn;
        server.getOverworld().setSpawnPos(new BlockPos(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ()), 1.0f);
    }

    private void registerEvents() {
        // Event use when the game state is "running"
        PlayerBlockBreakEvents.BEFORE.register(fkGameEvents::cancelPlayerFromBreakingBlocks);
        UseBlockCallback.EVENT.register(fkGameEvents::cancelPlayerFromPlacingBlocks);
        BucketFillCallback.EVENT.register(fkGameEvents::cancelPlayerFromFillingABucket);
        BucketEmptyCallback.EVENT.register(fkGameEvents::cancelPlayerFromEmptyingABucket);
        UseBlockCallback.EVENT.register(fkGameEvents::cancelPlayerFromFiringATNT);
        AttackEntityCallback.EVENT.register(fkGameEvents::cancelPlayerPvP);
        PlayerEnterPortalCallback.EVENT.register(fkGameEvents::cancelPlayerFromEnteringInPortal);
        PlayerMoveCallback.EVENT.register(fkGameEvents::cancelPlayersFromMoving);
        PlayerDamageCallback.EVENT.register(fkGameEvents::onPlayerDamage);
        PlayerHungerCallback.EVENT.register(fkGameEvents::onPlayerHungerUpdate);

        // Event use when the game state is "pause"
        PlayerJoinCallback.EVENT.register(pauseEvents::playerJoin);
        EntityMoveCallback.EVENT.register(pauseEvents::stopEntitiesFromMoving);
        TimeOfDayUpdatedCallback.EVENT.register(pauseEvents::cancelTimeOfDayToBeingUpdated);

        // Event use when the game state is NOT_STARTED
        PlayerJoinCallback.EVENT.register(notStartedEvents::teleportPlayerToWaitingRoom);
    }

    public void addPlayerPosIf_PAUSED(ServerPlayerEntity player){
        if(GameUtils.isGameStatePAUSE()){
            playerPositionWhenPaused.putIfAbsent(player.getUuidAsString(), new Vec3d(player.getX(), player.getY(), player.getZ()));
        }
    }

    /**
     * This class contains events that will be used when the game state is "RUNNING
     */
    @SuppressWarnings("ConstantConditions")
    public class FKGameEvents {

        @SuppressWarnings({"RedundantIfStatement"})
        private boolean cancelPlayerFromBreakingBlocks(World world, PlayerEntity player, BlockPos pos, BlockState state, /* Nullable */ BlockEntity blockEntity) {
            if (player.hasPermissionLevel(4)) return true;

            var breakPlace = (GameUtils.WhereIsThePlayer<Boolean>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {
                var block = world.getBlockState(pos).getBlock();

                // If the player is inside an enemy base
                if (isPlayerInAnEnemyBase) {
                    if (block != Blocks.TNT) { // We cancel the block that had to be broken except if it is TNT
                        return false;
                    }
                    return true;
                }

                if (isPlayerInHisOwnBase) {
                    // TODO Nothing to do for now
                    return true;
                }

                if (isPlayerCloseToHisOwnBase) {
                    // TODO Nothing to do for now
                    return true;
                }

                // In the rules of this FK, players can break blocks outside their base. Except near an enemy base
                if (isPlayerCloseToAnEnemyBase) {
                    if (block != Blocks.TNT) { // We cancel the block that had to be broken except if it is TNT
                        return false;
                    }
                    return true;
                }

                System.out.println("Player is in the wild");

                return true;
            };

            return GameUtils.whereIsThePlayer(player, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), breakPlace);

        }

        private ActionResult cancelPlayerFromPlacingBlocks(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            if (player.hasPermissionLevel(4)) return ActionResult.PASS;

            var placeBlock = (GameUtils.WhereIsThePlayer<ActionResult>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

                var placedItemStack = player.getStackInHand(player.getActiveHand());

                // If the player is inside an enemy base
                if (isPlayerInAnEnemyBase) {
                    if (!placedItemStack.isOf(Items.TNT))
                        return ActionResult.FAIL;
                    if (!GameUtils.areAssaultEnabled(timeline.timelineData.getDay()))
                        return ActionResult.FAIL;
                    return ActionResult.PASS;
                }

                if (isPlayerInHisOwnBase) {
                    return ActionResult.PASS;
                }

                if (isPlayerCloseToHisOwnBase) {
                    return ActionResult.FAIL;
                }

                // A player can place blocks outside his base, except if it is near another base (except TNT)
                if (isPlayerCloseToAnEnemyBase) {
                    if (!placedItemStack.isOf(Items.TNT))
                        return ActionResult.FAIL;
                    if (!GameUtils.areAssaultEnabled(timeline.timelineData.getDay()))
                        return ActionResult.FAIL;
                    return ActionResult.PASS;
                }

                // Player is in the wild

                return ActionResult.PASS;
            };

            var blockPos = hitResult.getBlockPos();
            return GameUtils.whereIsThePlayer(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), placeBlock);
        }

        private TypedActionResult<ItemStack> cancelPlayerFromFillingABucket(World world, PlayerEntity player, Hand hand, Fluid fillFluid, BucketItem bucketItem, BlockHitResult blockHitResult) {
            if (player.hasPermissionLevel(4)) return TypedActionResult.pass(player.getStackInHand(hand));

            var fillBucketImpl = (GameUtils.WhereIsThePlayer<TypedActionResult<ItemStack>>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

                var placedItemStack = player.getStackInHand(player.getActiveHand());

                // If the player is inside an enemy base
                if (isPlayerInAnEnemyBase) {
                    // Player cannot fill a bucket with water or lava inside an enemy base
                    if (fillFluid instanceof LavaFluid || fillFluid instanceof WaterFluid) {
                        return TypedActionResult.fail(placedItemStack);
                    }
                    return TypedActionResult.pass(placedItemStack);
                }

                if (isPlayerInHisOwnBase) {
                    return TypedActionResult.pass(placedItemStack);
                }

                if (isPlayerCloseToHisOwnBase) {
                    return TypedActionResult.pass(placedItemStack);
                }

                // A player can fill bucket outside his base, except if it is near another base
                if (isPlayerCloseToAnEnemyBase) {
                    if (fillFluid instanceof LavaFluid || fillFluid instanceof WaterFluid) {
                        return TypedActionResult.fail(placedItemStack);
                    }
                    return TypedActionResult.pass(placedItemStack);
                }

                // Player is in the wild

                return TypedActionResult.pass(placedItemStack);
            };

            var blockPos = blockHitResult.getBlockPos();
            return GameUtils.whereIsThePlayer(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), fillBucketImpl);
        }

        private TypedActionResult<ItemStack> cancelPlayerFromEmptyingABucket(World world, PlayerEntity player, Hand hand, Fluid emptyFluid, BucketItem bucketItem, BlockHitResult blockHitResult) {
            if (player.hasPermissionLevel(4)) return TypedActionResult.pass(player.getStackInHand(hand));

            var emptyBucketImpl = (GameUtils.WhereIsThePlayer<TypedActionResult<ItemStack>>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

                var placedItemStack = player.getStackInHand(player.getActiveHand());

                // If the player is inside an enemy base
                if (isPlayerInAnEnemyBase) {
                    // Player cannot empty a bucket with water or lava inside an enemy base
                    if (emptyFluid instanceof LavaFluid || emptyFluid instanceof WaterFluid) {
                        return TypedActionResult.fail(placedItemStack);
                    }
                    return TypedActionResult.pass(placedItemStack);
                }

                if (isPlayerInHisOwnBase) {
                    return TypedActionResult.pass(placedItemStack);
                }

                if (isPlayerCloseToHisOwnBase) {
                    return TypedActionResult.fail(placedItemStack);
                }

                // A player can empty bucket outside his base, except if it is near another base
                if (isPlayerCloseToAnEnemyBase) {
                    if (emptyFluid instanceof LavaFluid || emptyFluid instanceof WaterFluid) {
                        return TypedActionResult.fail(placedItemStack);
                    }
                    return TypedActionResult.pass(placedItemStack);
                }

                // Player is in the wild

                return TypedActionResult.pass(placedItemStack);
            };

            var blockPos = blockHitResult.getBlockPos();
            return GameUtils.whereIsThePlayer(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), emptyBucketImpl);
        }

        private ActionResult cancelPlayerFromFiringATNT(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            if (player.hasPermissionLevel(4)) return ActionResult.PASS;

            var emptyBucketImpl = (GameUtils.WhereIsThePlayer<ActionResult>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

                var stackInHand = player.getStackInHand(hand);

                var didPlayerTryToFireATNT = false;

                if (stackInHand.isOf(Items.FLINT_AND_STEEL)) {
                    var block = world.getBlockState(hitResult.getBlockPos()).getBlock();
                    if (block instanceof TntBlock)
                        didPlayerTryToFireATNT = true;
                }

                // If the player is inside an enemy base
                if (isPlayerInAnEnemyBase) {
                    if (didPlayerTryToFireATNT) {
                        if (!GameUtils.areAssaultEnabled(timeline.timelineData.getDay())) {
                            return ActionResult.FAIL;
                        }
                    }
                    return ActionResult.PASS;
                }

                if (isPlayerInHisOwnBase) {
                    return ActionResult.PASS;
                }

                if (isPlayerCloseToHisOwnBase) {
                    return ActionResult.PASS;
                }

                // A player can empty bucket outside his base, except if it is near another base
                if (isPlayerCloseToAnEnemyBase) {
                    if (didPlayerTryToFireATNT) {
                        if (!GameUtils.areAssaultEnabled(timeline.timelineData.getDay())) {
                            return ActionResult.FAIL;
                        }
                    }
                    return ActionResult.PASS;
                }

                // Player is in the wild

                return ActionResult.PASS;
            };

            var blockPos = hitResult.getBlockPos();
            return GameUtils.whereIsThePlayer(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), emptyBucketImpl);
        }

        private ActionResult cancelPlayerPvP(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
            if (player.hasPermissionLevel(4)) return ActionResult.PASS;

            if (!GameUtils.isGameStateRUNNING()) return ActionResult.PASS;

            if (entity instanceof PlayerEntity)
                if (!GameUtils.isPvPEnabled(timeline.timelineData.getDay()))
                    return ActionResult.FAIL;
            return ActionResult.PASS;
        }

        private ActionResult cancelPlayerFromEnteringInPortal(ServerPlayerEntity player, Identifier dimensionId) {
            if (player.hasPermissionLevel(4)) return ActionResult.PASS;

            if (dimensionId == DimensionType.THE_NETHER_ID) {
                if (!GameUtils.isNetherEnabled(timeline.timelineData.getDay()))
                    return ActionResult.FAIL;
            } else if (dimensionId == DimensionType.THE_END_ID) {
                if (!GameUtils.isEndEnabled(timeline.timelineData.getDay()))
                    return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        }

        private ActionResult cancelPlayersFromMoving(PlayerMoveCallback.MoveData moveData, ServerPlayerEntity player) {
            if (player.hasPermissionLevel(4)) return ActionResult.PASS; // OP Player can move anymore

            // Cancel player from going outside the waitingRoom
            if (GameUtils.isGameStateNOT_STARTED()) {
                var waitingRoom = Configs.FK.config.waitingRoom;
                if (Utils.cancelPlayerFromLeavingACube(waitingRoom.getCube(), player, Optional.of(waitingRoom.getSpawnLocation())))
                    return ActionResult.FAIL;

                return ActionResult.PASS;
            }

            // Cancel player from moving.
            if (GameUtils.isGameStatePAUSE()) {
                for (var entry : playerPositionWhenPaused.entrySet()) {
                    var fkPlayer = server.getPlayerManager().getPlayer(UUID.fromString(entry.getKey()));
                    if (fkPlayer != null) { // If fkPlayer is null, this is because it is not connected
                        var pos = entry.getValue();
                        var cube = new Cube((short) 1, 3, 3, pos.x, pos.y, pos.z); // The area where the player can move
                        if (Utils.cancelPlayerFromLeavingACube(cube, player, Optional.empty()))
                            return ActionResult.FAIL;
                    }
                }
            }

            // Cancel the player from going too far into the map
            if(Utils.cancelPlayerFromLeavingACube(Configs.WORLD_CONFIG.config.worldInfo.getWorldDimension(), player, Optional.empty())){
                player.sendMessage(new LiteralText("You reach the border limit !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        }

        private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
            if (player.hasPermissionLevel(4)) return ActionResult.PASS;

            if (GameUtils.isGameStatePAUSE() || GameUtils.isGameStateNOT_STARTED())
                return ActionResult.FAIL;
            return ActionResult.PASS;
        }

        private ActionResult onPlayerHungerUpdate(PlayerEntity player){
            if (player.hasPermissionLevel(4)) return ActionResult.PASS;

            if( GameUtils.isGameStateNOT_STARTED() || GameUtils.isGameStatePAUSE()){
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        }
    }

    class PauseEvents {

        private void playerJoin(ServerPlayerEntity player, MinecraftServer server){
            if(GameUtils.isGameStatePAUSE())
                playerPositionWhenPaused.putIfAbsent(player.getUuidAsString(), new Vec3d(player.getX(), player.getY(), player.getZ()));
        }

        private ActionResult stopEntitiesFromMoving(Entity entity, MovementType movementType, Vec3d movement) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;
            return ActionResult.FAIL;
        }

        private ActionResult cancelTimeOfDayToBeingUpdated(long time) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;
            return ActionResult.FAIL;
        }

    }

    @SuppressWarnings("ConstantConditions")
    class NotStartedEvents {
        private void teleportPlayerToWaitingRoom(ServerPlayerEntity player, MinecraftServer server) {
            if (player.hasPermissionLevel(4)) return;

            if (!GameUtils.isGameStateNOT_STARTED()) return;

            var spawnLoc = Configs.FK.config.waitingRoom.getSpawnLocation();

            StreamSupport.stream(server.getWorlds().spliterator(), false)
                    .filter(serverWorld -> serverWorld.getDimension().getEffects().toString().equals(spawnLoc.getDimensionName()))
                    .findFirst()
                    .ifPresent(serverWorld -> {
                        updateTeam(server, player);
                        ScoreboardManager.getInstance().updateSidebar(player, 0, 0, 0);

                        player.teleport(serverWorld, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), spawnLoc.getYaw(), spawnLoc.getPitch());
                    });
        }
    }

}

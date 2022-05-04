package ch.skyfy.fk.logic;

import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.config.Configs;
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

import java.util.Optional;
import java.util.stream.StreamSupport;

@SuppressWarnings("FieldCanBeLocal")
public class FKGame {

    private final MinecraftServer server;

    public Timeline timeline;

    private final PauseEvents pauseEvents;

    private final FKGameEvents fkGameEvents;

    public FKGame(MinecraftServer server) {
        this.server = server;
        this.timeline = new Timeline(server);
        pauseEvents = new PauseEvents();
        fkGameEvents = new FKGameEvents();

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

    }

    public void resume() {
        // If the timeline wasn't started (in the case of a server restart with gamestate at PAUSE OR RUNNING)
        if (!timeline.getIsStartedRef().get())
            timeline.startTimer();
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
        var spawnLocation = Configs.FK_CONFIG.config.getWorldSpawn();
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
        PlayerJoinCallback.EVENT.register(fkGameEvents::teleportPlayerToWaitingRoom);

        // Event use when the game state is "pause"
        EntityMoveCallback.EVENT.register(pauseEvents::stopEntitiesFromMoving);
        TimeOfDayUpdatedCallback.EVENT.register(pauseEvents::cancelTimeOfDayToBeingUpdated);

        // Event use when the game state is NOT_STARTED
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
                var waitingRoom = Configs.FK_CONFIG.config.getWaitingRoom();
                if (Utils.cancelPlayerFromLeavingACube(waitingRoom.getCube(), player, Optional.of(waitingRoom.getSpawnLocation())))
                    return ActionResult.FAIL;

                return ActionResult.PASS;
            }

            // Cancel player from moving.
            if (GameUtils.isGameStatePAUSE())
                return ActionResult.FAIL;

            // Cancel the player from going too far into the map
            if(Utils.cancelPlayerFromLeavingACube(Configs.WORLD_CONFIG.config.getWorldInfo().getWorldDimension(), player, Optional.empty())){
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

        private void teleportPlayerToWaitingRoom(ServerPlayerEntity player, MinecraftServer server) {
            if (player.hasPermissionLevel(4)) return;

            if (!GameUtils.isGameStateNOT_STARTED()) return;

            var spawnLoc = Configs.FK_CONFIG.config.getWaitingRoom().getSpawnLocation();

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

    static class PauseEvents {

        private ActionResult stopEntitiesFromMoving(Entity entity, MovementType movementType, Vec3d movement) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;
            return ActionResult.FAIL;
        }

        private ActionResult cancelTimeOfDayToBeingUpdated(long time) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;
            return ActionResult.FAIL;
        }

    }

}

package ch.skyfy.fk.logic;

import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.data.FKTeam;
import ch.skyfy.fk.config.data.Cube;
import ch.skyfy.fk.events.*;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FKGame {

    private final MinecraftServer server;

    public Timeline timeline;

    private final PauseEvents pauseEvents;

    private final FKGameEvents FKGameEvents;

    private final Map<String, Vec3d> playerPositionWhenPaused;

    public FKGame(MinecraftServer server) {
        this.server = server;
        this.timeline = new Timeline(server);
        pauseEvents = new PauseEvents();
        FKGameEvents = new FKGameEvents();
        this.playerPositionWhenPaused = new HashMap<>();

        registerEvents();
    }

    @SuppressWarnings("ConstantConditions")
    public void start() {
        server.getOverworld().setTimeOfDay(0);
        timeline.startTimer();

        // Send a message to all fk player to tell them where their respective base is
        for (ServerPlayerEntity fkPlayer : GameUtils.getFkPlayers(server)) {
            var baseLoc = GameUtils.getBaseCoordinateByPlayer(fkPlayer.getName().asString());
            var message = new LiteralText("Your base is at this coords: X: " + baseLoc.getX() + " Y: " + baseLoc.getY() + " Z: " + baseLoc.getZ())
                    .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE));
            fkPlayer.sendMessage(message, false);
        }
    }

    public void pause() {
        // Records the position of the players. Will prevent them from moving when the game is paused.
        for (var fkPlayer : GameUtils.getFkPlayers(server))
            playerPositionWhenPaused.putIfAbsent(fkPlayer.getUuidAsString(), new Vec3d(fkPlayer.getX(), fkPlayer.getY(), fkPlayer.getZ()));
    }

    public void resume(ServerPlayerEntity player) {

        if(GameUtils.areMissingPlayers(server.getPlayerManager().getPlayerList())){
            GameUtils.sendMissingPlayersMessage(player, server.getPlayerManager().getPlayerList());
        }

        // If the timeline wasn't started (in the case of a server restart with gamestate at PAUSE OR RUNNING)
        if(!timeline.getIsRunningRef().get()){
            timeline.startTimer();
        }

        playerPositionWhenPaused.clear();
    }

    private void registerEvents() {

        // Event use when the game state is "running"
        PlayerBlockBreakEvents.BEFORE.register(FKGameEvents::cancelPlayerFromBreakingBlocks);
        UseBlockCallback.EVENT.register(FKGameEvents::cancelPlayerFromPlacingBlocks);
        BucketFillEvent.EVENT.register(FKGameEvents::cancelPlayerFromFillingABucket);
        BucketEmptyEvent.EVENT.register(FKGameEvents::cancelPlayerFromEmptyingABucket);
        UseBlockCallback.EVENT.register(FKGameEvents::cancelPlayerFromFiringATNT);
        AttackEntityCallback.EVENT.register(FKGameEvents::cancelPlayerPvP);
        PlayerEnterPortalCallback.EVENT.register(FKGameEvents::cancelPlayerFromEnteringInPortal);


        // Event use when the game state is "pause"
        PlayerMoveCallback.EVENT.register(pauseEvents::stopThePlayersFromMoving);
        EntityMoveCallback.EVENT.register(pauseEvents::stopEntitiesFromMoving);
        PlayerDamageCallback.EVENT.register(pauseEvents::onPlayerDamage);
        TimeOfDayUpdatedCallback.EVENT.register(pauseEvents::cancelTimeOfDayToBeingUpdated);
    }

    /**
     * This class contains events that will be used when the game state is "RUNNING
     */
    @SuppressWarnings("ConstantConditions")
    class FKGameEvents {

        @FunctionalInterface
        private interface BreakPlaceFillEmptyImpl<T> {
            T impl(boolean isPlayerInHisOwnBase, boolean isPlayerInAnEnemyBase, boolean isPlayerCloseToHisOwnBase, boolean isPlayerCloseToAnEnemyBase);

        }

        @SuppressWarnings({"RedundantIfStatement"})
        private boolean cancelPlayerFromBreakingBlocks(World world, PlayerEntity player, BlockPos pos, BlockState state, /* Nullable */ BlockEntity blockEntity) {
            if (!GameUtils.isGameStateRUNNING()) return true;

            var breakPlace = (BreakPlaceFillEmptyImpl<Boolean>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {
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

            return iDontKnowTheNameOfThisMethod(player, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), breakPlace);

        }

        private ActionResult cancelPlayerFromPlacingBlocks(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            if (!GameUtils.isGameStateRUNNING()) return ActionResult.PASS;

            var placeBlock = (BreakPlaceFillEmptyImpl<ActionResult>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

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
                    // TODO Nothing to do for now
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

                System.out.println("Player is in the wild");

                return ActionResult.PASS;
            };

            var blockPos = hitResult.getBlockPos();
            return iDontKnowTheNameOfThisMethod(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), placeBlock);
        }

        private TypedActionResult<ItemStack> cancelPlayerFromFillingABucket(World world, PlayerEntity player, Hand hand, Fluid fillFluid, BucketItem bucketItem, BlockHitResult blockHitResult) {
            if (!GameUtils.isGameStateRUNNING()) return TypedActionResult.pass(player.getStackInHand(hand));

            var fillBucketImpl = (BreakPlaceFillEmptyImpl<TypedActionResult<ItemStack>>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

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
                    // TODO Nothing to do for now
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

                System.out.println("Player is in the wild");

                return TypedActionResult.pass(placedItemStack);
            };

            var blockPos = blockHitResult.getBlockPos();
            return FKGameEvents.this.iDontKnowTheNameOfThisMethod(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), fillBucketImpl);
        }

        private TypedActionResult<ItemStack> cancelPlayerFromEmptyingABucket(World world, PlayerEntity player, Hand hand, Fluid emptyFluid, BucketItem bucketItem, BlockHitResult blockHitResult) {
            if (!GameUtils.isGameStateRUNNING()) return TypedActionResult.pass(player.getStackInHand(hand));

            var emptyBucketImpl = (BreakPlaceFillEmptyImpl<TypedActionResult<ItemStack>>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

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

                System.out.println("Player is in the wild");

                return TypedActionResult.pass(placedItemStack);
            };

            var blockPos = blockHitResult.getBlockPos();
            return FKGameEvents.this.iDontKnowTheNameOfThisMethod(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), emptyBucketImpl);
        }

        private ActionResult cancelPlayerFromFiringATNT(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            if (!GameUtils.isGameStateRUNNING()) return ActionResult.PASS;

            var emptyBucketImpl = (BreakPlaceFillEmptyImpl<ActionResult>) (isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {

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
                    // TODO Nothing to do for now
                    return ActionResult.PASS;
                }

                if (isPlayerCloseToHisOwnBase) {
                    // TODO Nothing to do for now
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

                System.out.println("Player is in the wild");

                return ActionResult.PASS;
            };

            var blockPos = hitResult.getBlockPos();
            return FKGameEvents.this.iDontKnowTheNameOfThisMethod(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), emptyBucketImpl);
        }

        private ActionResult cancelPlayerPvP(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
            if (!GameUtils.isGameStateRUNNING()) return ActionResult.PASS;

            if (entity instanceof PlayerEntity)
                if (!GameUtils.isPvPEnabled(timeline.timelineData.getDay()))
                    return ActionResult.FAIL;
            return ActionResult.PASS;
        }

        private ActionResult cancelPlayerFromEnteringInPortal(ServerPlayerEntity player, Identifier dimensionId) {

            if (dimensionId == DimensionType.THE_NETHER_ID) {
                if (!GameUtils.isNetherEnabled(timeline.timelineData.getDay()))
                    return ActionResult.FAIL;
            } else if (dimensionId == DimensionType.THE_END_ID) {
                if (!GameUtils.isEndEnabled(timeline.timelineData.getDay()))
                    return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        }

        private <T> T iDontKnowTheNameOfThisMethod(PlayerEntity player, Vec3d blockPos, BreakPlaceFillEmptyImpl<T> breakPlace) {

            var isPlayerInHisOwnBase = false;

            var isPlayerInAnEnemyBase = false;

            // Is the player close to his own base, but not inside
            var isPlayerCloseToHisOwnBase = false;

            // Is the player close to an enemy base, but not inside
            var isPlayerCloseToAnEnemyBase = false;

            for (FKTeam team : Configs.BASES_CONFIG.config.teams) {
                var baseSquare = team.getBase().getSquare();

                // Is this base the base of the player who break the block ?
                var isBaseOfPlayer = team.getPlayers().stream().anyMatch(fkPlayerName -> player.getName().asString().equals(fkPlayerName));

                var isPlayerCloseToABase = false;

                var proximitySquare = new Cube((short) (baseSquare.getSize() + 5), baseSquare.getNumberOfBlocksDown() + 5, baseSquare.getNumberOfBlocksUp() + 5, baseSquare.getX(), baseSquare.getY(), baseSquare.getZ());
                if (Utils.isPlayerInsideCube(proximitySquare, blockPos)) {
                    isPlayerCloseToABase = true;
                }

                // If player is inside a base
                if (Utils.isPlayerInsideCube(baseSquare, blockPos)) {

                    // And this base is not his own
                    if (!isBaseOfPlayer) {
                        isPlayerInAnEnemyBase = true;
                    } else {
                        isPlayerInHisOwnBase = true;
                    }

                } else {

                    // If the player is close to a base, but not inside
                    if (isPlayerCloseToABase) {
                        if (!isPlayerInHisOwnBase) {
                            if (isBaseOfPlayer) isPlayerCloseToHisOwnBase = true;
                            else isPlayerCloseToAnEnemyBase = true;
                        } else if (!isPlayerInAnEnemyBase) {
                            if (!isBaseOfPlayer) isPlayerCloseToAnEnemyBase = true;
                            else isPlayerCloseToHisOwnBase = true;
                        }
                    }

                }

            }

            return breakPlace.impl(isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase);
        }

    }

    class PauseEvents {

        private ActionResult stopThePlayersFromMoving(PlayerMoveCallback.MoveData moveData, ServerPlayerEntity player) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;

            if(player.hasPermissionLevel(4))return ActionResult.PASS; // OP Player can move anymore

            for (var entry : playerPositionWhenPaused.entrySet()) {
                var fkPlayer = server.getPlayerManager().getPlayer(UUID.fromString(entry.getKey()));
                if (fkPlayer != null) { // If fkPlayer is null, this is because it is not connected
                    var pos = entry.getValue();
                    var square = new Cube((short) 1,3,3, pos.x, pos.y, pos.z); // The area where the player can move

                    if (Utils.didPlayerTryToLeaveAnArea(square, player))
                        return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        }

        private ActionResult stopEntitiesFromMoving(Entity entity, MovementType movementType, Vec3d movement) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;



            return ActionResult.FAIL;
        }

        private ActionResult onPlayerDamage(DamageSource source, float amount) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;
            return ActionResult.FAIL;
        }

        private ActionResult cancelTimeOfDayToBeingUpdated(long time) {
            if (!GameUtils.isGameStatePAUSE()) return ActionResult.PASS;
            return ActionResult.FAIL;
        }

    }

}

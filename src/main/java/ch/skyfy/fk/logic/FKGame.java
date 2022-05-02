package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.data.FKTeam;
import ch.skyfy.fk.config.data.Square;
import ch.skyfy.fk.events.EntityMoveCallback;
import ch.skyfy.fk.events.PlayerDamageCallback;
import ch.skyfy.fk.events.PlayerMoveCallback;
import ch.skyfy.fk.events.TimeOfDayUpdatedCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.skyfy.fk.FK.GAME_STATE;

public class FKGame {

    private final MinecraftServer server;

    public Timeline timeline;

    private final PauseEvents pauseEvents;

    private final RunningEvents runningEvents;

    private final Map<String, Vec3d> playerPositionWhenPaused;

    public FKGame(MinecraftServer server) {
        this.server = server;
        this.timeline = new Timeline(server);
        pauseEvents = new PauseEvents();
        runningEvents = new RunningEvents();
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

    public void resume() {
        playerPositionWhenPaused.clear();
    }

    private void registerEvents() {

        PlayerBlockBreakEvents.BEFORE.register(runningEvents::cancelPlayerFromBreakingBlocks);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if(player.getStackInHand(player.getActiveHand()).isOf(Items.DIRT)){
                System.out.println("player.getStackInHand(player.getActiveHand()).isOf(Items.DIRT)");
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });


        // Events triggered when the game is paused
        PlayerMoveCallback.EVENT.register(pauseEvents::stopThePlayersFromMoving);
        EntityMoveCallback.EVENT.register(pauseEvents::stopEntitiesFromMoving);
        PlayerDamageCallback.EVENT.register(pauseEvents::onPlayerDamage);
        TimeOfDayUpdatedCallback.EVENT.register(pauseEvents::cancelTimeOfDayToBeingUpdated);
    }

    @SuppressWarnings("ConstantConditions")
    static class RunningEvents {

        @FunctionalInterface
        private interface BreakPlaceImpl {
            boolean manageBreakPlaceEvent(boolean isPlayerInHisOwnBase, boolean isPlayerInAnEnemyBase, boolean isPlayerCloseToHisOwnBase, boolean isPlayerCloseToAnEnemyBase);

        }

        @SuppressWarnings({"RedundantIfStatement"})
        private boolean cancelPlayerFromBreakingBlocks(World world, PlayerEntity player, BlockPos pos, BlockState state, /* Nullable */ BlockEntity blockEntity) {

//            var isPlayerInHisOwnBase = false;
//
//            var isPlayerInAnEnemyBase = false;
//
//            // Is the player close to his own base, but not inside
//            var isPlayerCloseToHisOwnBase = false;
//
//            // Is the player close to an enemy base, but not inside
//            var isPlayerCloseToAnEnemyBase = false;
//
//            for (FKTeam team : Configs.BASES_CONFIG.config.teams) {
//                var baseSquare = team.getBase().getSquare();
//
//                // Is this base the base of the player who break the block ?
//                var isBaseOfPlayer = team.getPlayers().stream().anyMatch(fkPlayerName -> player.getName().asString().equals(fkPlayerName));
//
//                var isPlayerCloseToABase = false;
//
//                var proximitySquare = new Square((short) (baseSquare.getSize() + 5), baseSquare.getX(), baseSquare.getY(), baseSquare.getZ());
//                if (Utils.isPlayerInsideArea(proximitySquare, new Vec3d(player.getX(), player.getY(), player.getZ()))) {
//                    isPlayerCloseToABase = true;
//                }
//
//                // If player is inside a base
//                if (Utils.isPlayerInsideArea(baseSquare, new Vec3d(player.getX(), player.getY(), player.getZ()))) {
//
//                    // And this base is not his own
//                    if (!isBaseOfPlayer) {
//                        isPlayerInAnEnemyBase = true;
//                    } else {
//                        isPlayerInHisOwnBase = true;
//                    }
//
//                } else {
//
//                    // If the player is close to a base, but not inside
//                    if (isPlayerCloseToABase) {
//                        if (!isPlayerInHisOwnBase) isPlayerCloseToHisOwnBase = true;
//                        if (!isPlayerInAnEnemyBase) isPlayerCloseToAnEnemyBase = true;
//                    }
//
//                }
//
//
//            }

            var breakPlace = (BreakPlaceImpl)(isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {
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
                }

                System.out.println("Player is in the wild");

                return true;
            };

            return cancelPlayerFromBreakingOrPlacingBlocks(player,new Vec3d(pos.getX(), pos.getY(), pos.getZ()), breakPlace);

        }

        private boolean cancelPlayerFromPlacingBlocks(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

            var breakPlace = (BreakPlaceImpl)(isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase) -> {


                return true;
            };

            var blockPos = hitResult.getBlockPos();
            cancelPlayerFromBreakingOrPlacingBlocks(player, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()),breakPlace);

            return true;
        }

        private boolean cancelPlayerFromBreakingOrPlacingBlocks(PlayerEntity player, Vec3d blockPos, BreakPlaceImpl breakPlace){

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

                var proximitySquare = new Square((short) (baseSquare.getSize() + 5), baseSquare.getX(), baseSquare.getY(), baseSquare.getZ());
                if (Utils.isPlayerInsideArea(proximitySquare, blockPos)) {
                    isPlayerCloseToABase = true;
                }

                // If player is inside a base
                if (Utils.isPlayerInsideArea(baseSquare, blockPos)) {

                    // And this base is not his own
                    if (!isBaseOfPlayer) {
                        isPlayerInAnEnemyBase = true;
                    } else {
                        isPlayerInHisOwnBase = true;
                    }

                } else {

                    // If the player is close to a base, but not inside
                    if (isPlayerCloseToABase) {
                        if (!isPlayerInHisOwnBase){
                            if(isBaseOfPlayer)isPlayerCloseToHisOwnBase = true;
                            else isPlayerCloseToAnEnemyBase = true;
                        }else if(!isPlayerInAnEnemyBase){
                            if(!isBaseOfPlayer)isPlayerCloseToAnEnemyBase = true;
                            else isPlayerCloseToHisOwnBase = true;
                        }
                    }

                }

            }

            return breakPlace.manageBreakPlaceEvent(isPlayerInHisOwnBase, isPlayerInAnEnemyBase, isPlayerCloseToHisOwnBase, isPlayerCloseToAnEnemyBase);
        }

    }

    class PauseEvents {

        private ActionResult stopThePlayersFromMoving(PlayerMoveCallback.MoveData moveData, ServerPlayerEntity player) {
            if (FK.GAME_STATE == FK.GameState.PAUSED) {
                for (var entry : playerPositionWhenPaused.entrySet()) {
                    var fkPlayer = server.getPlayerManager().getPlayer(UUID.fromString(entry.getKey()));
                    if (fkPlayer != null) { // If fkPlayer is null, this is because it is not connected
                        var pos = entry.getValue();
                        var square = new Square((short) 1, pos.x, pos.y, pos.z); // The area where the player can move

                        if (Utils.didPlayerTryToLeaveAnArea(square, player))
                            return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        }

        private ActionResult stopEntitiesFromMoving(Entity entity, MovementType movementType, Vec3d movement) {
            if (FK.GAME_STATE == FK.GameState.PAUSED)
                return ActionResult.FAIL;
            return ActionResult.PASS;
        }

        private ActionResult onPlayerDamage(DamageSource source, float amount) {
            if (GAME_STATE == FK.GameState.PAUSED) return ActionResult.FAIL;
            return ActionResult.PASS;
        }

        private ActionResult cancelTimeOfDayToBeingUpdated(long time) {
            if (FK.GAME_STATE == FK.GameState.PAUSED) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        }

    }

}

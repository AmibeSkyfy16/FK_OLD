package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.events.PlayerDamageCallback;
import ch.skyfy.fk.events.PlayerMoveCallback;
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

import java.util.stream.StreamSupport;

import static ch.skyfy.fk.FK.GAME_STATE;

@SuppressWarnings("ConstantConditions")
public class PreFKGame {

    public void registerAll() {
        PlayerJoinCallback.EVENT.register(this::teleportPlayerToWaitingRoom);
        PlayerDamageCallback.EVENT.register(this::onPlayerDamage);
        PlayerMoveCallback.EVENT.register(this::onPlayerMove);
    }

    @SuppressWarnings("ConstantConditions")
    private void teleportPlayerToWaitingRoom(ServerPlayerEntity player, MinecraftServer server) {
        if (GAME_STATE != FK.GameState.NOT_STARTED) return;
        var spawnLoc = Configs.FK_CONFIG.config.waitingRoom.getSpawnLocation();
        StreamSupport.stream(server.getWorlds().spliterator(), false)
                .filter(serverWorld -> serverWorld.getDimension().getEffects().toString().equals(spawnLoc.getDimensionName()))
                .findFirst()
                .ifPresent(serverWorld -> {
                    player.teleport(serverWorld, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), spawnLoc.getYaw(), spawnLoc.getPitch());
                    System.out.println("[SUCCESS] player teleported to the waiting room");
                });
    }

    private ActionResult onPlayerDamage(DamageSource source, float amount){
        if (GAME_STATE != FK.GameState.NOT_STARTED) return ActionResult.SUCCESS;
        return ActionResult.FAIL;
    }

    /**
     * Prevents the player from leaving the waiting room
     */
    private ActionResult onPlayerMove(PlayerMoveCallback.MoveData moveData, ServerPlayerEntity player) {

        if (GAME_STATE != FK.GameState.NOT_STARTED)return ActionResult.SUCCESS;

        var waitingRoom = Configs.FK_CONFIG.config.waitingRoom;
        var square = waitingRoom.getSquare();

        Vec3d vec = null;

        if (player.getX() >= square.getX() + square.getSize()) {
            vec = new Vec3d(player.getX() - 1, player.getY(), player.getZ());
        }else if(player.getX() <= square.getX() - square.getSize()){
            vec = new Vec3d(player.getX() + 1, player.getY(), player.getZ());
        }else if (player.getZ() >= square.getZ() + square.getSize()) {
            vec = new Vec3d(player.getZ() - 1, player.getY(), player.getZ() - 1);
        }else if(player.getZ() <= square.getZ() - square.getSize()){
            vec = new Vec3d(player.getX(), player.getY(), player.getZ() + 1);
        }

        if(vec != null){
            player.teleport(vec.x, vec.y, vec.z);
            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

}

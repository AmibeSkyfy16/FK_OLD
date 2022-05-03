package ch.skyfy.fk.logic;

import ch.skyfy.fk.config.data.Cube;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Utils {


    /**
     * If the player tries to leave his authorized area, he is teleported just behind
     */
    public static boolean didPlayerTryToLeaveAnArea(Cube cube, ServerPlayerEntity player) {
        Vec3d vec = null;

        if (player.getX() >= cube.getX() + cube.getSize()) {
            vec = new Vec3d(player.getX() - 1, player.getY(), player.getZ());
        } else if (player.getX() <= cube.getX() - cube.getSize()) {
            vec = new Vec3d(player.getX() + 1, player.getY(), player.getZ());
        } else if (player.getZ() >= cube.getZ() + cube.getSize()) {
            vec = new Vec3d(player.getZ() - 1, player.getY(), player.getZ() - 1);
        } else if (player.getZ() <= cube.getZ() - cube.getSize()) {
            vec = new Vec3d(player.getX(), player.getY(), player.getZ() + 1);
        }

        if (vec != null)
            player.teleport(vec.x, vec.y, vec.z);

        return vec != null;
    }

    public static boolean isPlayerInsideArea(Cube cube, Vec3d pos) {
        if ((pos.getX() <= cube.getX() + cube.getSize()) && (pos.getX() >= cube.getX() - cube.getSize())) {
            return (pos.getZ() <= cube.getZ() + cube.getSize()) && (pos.getZ() >= cube.getZ() - cube.getSize());
        }
        return false;
    }

    public static boolean isPlayerInsideCube(Cube cube, Vec3d pos) {
        if ((pos.getX() <= cube.getX() + cube.getSize()) && (pos.getX() >= cube.getX() - cube.getSize())) {
            if ((pos.getZ() <= cube.getZ() + cube.getSize()) && (pos.getZ() >= cube.getZ() - cube.getSize())) {
                return (pos.getY() <= cube.getY() + cube.getNumberOfBlocksUp()) && (pos.getY() >= cube.getY() - cube.getNumberOfBlocksDown());
            }
        }
        return false;
    }

}

package ch.skyfy.fk.logic;

import ch.skyfy.fk.config.data.Square;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class Utils {


    /**
     * If the player tries to leave his authorized area, he is teleported just behind
     */
    public static boolean didPlayerTryToLeaveAnArea(Square square, ServerPlayerEntity player){
        Vec3d vec = null;

        if (player.getX() >= square.getX() + square.getSize()) {
            vec = new Vec3d(player.getX() - 1, player.getY(), player.getZ());
        } else if (player.getX() <= square.getX() - square.getSize()) {
            vec = new Vec3d(player.getX() + 1, player.getY(), player.getZ());
        } else if (player.getZ() >= square.getZ() + square.getSize()) {
            vec = new Vec3d(player.getZ() - 1, player.getY(), player.getZ() - 1);
        } else if (player.getZ() <= square.getZ() - square.getSize()) {
            vec = new Vec3d(player.getX(), player.getY(), player.getZ() + 1);
        }

        if(vec != null)
            player.teleport(vec.x, vec.y, vec.z);

        return vec != null;
    }

    public static boolean isPlayerInsideArea(Square square, Vec3d pos){
        if((pos.getX() <= square.getX() + square.getSize()) && (pos.getX() >= square.getX() - square.getSize())){
            return (pos.getZ() <= square.getZ() + square.getSize()) && (pos.getZ() >= square.getZ() - square.getSize());
        }
        return false;
    }

}

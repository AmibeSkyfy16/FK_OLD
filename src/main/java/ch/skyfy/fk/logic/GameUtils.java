package ch.skyfy.fk.logic;

import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.data.Base;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GameUtils {

    /**
     * If among the connected players, one is missing, the game is not started
     */
    public static List<String> ArePlayersReady(List<ServerPlayerEntity> onlinePlayers){
        var missingPlayers  = new ArrayList<String>();
        for (Base bases : Configs.BASES_CONFIG.config.bases) {
            for (String fkPlayerName : bases.getTeam()) {
                if(onlinePlayers.stream().noneMatch(serverPlayerEntity -> serverPlayerEntity.getName().asString().equals(fkPlayerName))){
                    missingPlayers.add(fkPlayerName);
                }
            }
        }
        return missingPlayers;
    }

    public static List<ServerPlayerEntity> getFkPlayers(MinecraftServer server){
        var onlinePlayers = server.getPlayerManager().getPlayerList();
        var fkPlayers  = new ArrayList<ServerPlayerEntity>();
        for (Base base : Configs.BASES_CONFIG.config.bases) {
            for (String fkPlayerName : base.getTeam()) {
                for (ServerPlayerEntity onlinePlayer : onlinePlayers) {
                    if(onlinePlayer.getName().asString().equals(fkPlayerName)){
                        fkPlayers.add(onlinePlayer);
                    }
                }
            }
        }
        return fkPlayers;
    }

    @Nullable
    public static BlockPos getBaseCoordinateByPlayer(String name){
        for (Base base : Configs.BASES_CONFIG.config.bases) {
            if(base.getTeam().stream().anyMatch(name::equals)){
                return new BlockPos(base.getSquare().getX(), base.getSquare().getY(), base.getSquare().getZ());
            }
        }
        return null;
    }

}

package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.data.FKTeam;
import ch.skyfy.fk.logic.data.AllData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class GameUtils {

    /**
     * If among the connected players, one is missing, the game is not started
     */
    public static List<String> ArePlayersReady(List<ServerPlayerEntity> onlinePlayers) {
        var missingPlayers = new ArrayList<String>();
        for (FKTeam fkTeam : Configs.BASES_CONFIG.config.teams) {
            for (String fkPlayerName : fkTeam.getPlayers()) {
                if (onlinePlayers.stream().noneMatch(serverPlayerEntity -> serverPlayerEntity.getName().asString().equals(fkPlayerName))) {
                    missingPlayers.add(fkPlayerName);
                }
            }
        }
        return missingPlayers;
    }

    public static boolean isFKPlayer(List<ServerPlayerEntity> onlinePlayers, String playerName) {
        // Find the player to check
        if (onlinePlayers.stream().anyMatch(serverPlayerEntity -> serverPlayerEntity.getName().asString().equals(playerName)))
            for (FKTeam fkTeam : Configs.BASES_CONFIG.config.teams)
                if (fkTeam.getPlayers().stream().anyMatch(fkPlayerName -> fkPlayerName.equals(playerName)))
                    return true;
        return false;
    }

    public static List<ServerPlayerEntity> getFkPlayers(MinecraftServer server) {
        var onlinePlayers = server.getPlayerManager().getPlayerList();
        var fkPlayers = new ArrayList<ServerPlayerEntity>();
        for (FKTeam fkTeam : Configs.BASES_CONFIG.config.teams) {
            for (String fkPlayerName : fkTeam.getPlayers()) {
                for (ServerPlayerEntity onlinePlayer : onlinePlayers) {
                    if (onlinePlayer.getName().asString().equals(fkPlayerName)) {
                        fkPlayers.add(onlinePlayer);
                    }
                }
            }
        }
        return fkPlayers;
    }

    @Nullable
    public static BlockPos getBaseCoordinateByPlayer(String name) {
        for (FKTeam fkTeam : Configs.BASES_CONFIG.config.teams) {
            if (fkTeam.getPlayers().stream().anyMatch(name::equals)) {
                return new BlockPos(fkTeam.getBase().getSquare().getX(), fkTeam.getBase().getSquare().getY(), fkTeam.getBase().getSquare().getZ());
            }
        }
        return null;
    }

    @Nullable
    public static FKTeam getFKTeamOfPlayerByName(String name) {
        for (FKTeam fkTeam : Configs.BASES_CONFIG.config.teams) {
            if (fkTeam.getPlayers().stream().anyMatch(name::equals)) {
                return fkTeam;
            }
        }
        return null;
    }

    public static boolean isInTheSameTeam(String playerName, String anotherPlayerName){
        for (FKTeam fkTeam : Configs.BASES_CONFIG.config.teams) {
            if(fkTeam.getPlayers().stream().anyMatch(playerName::equals) && fkTeam.getPlayers().stream().anyMatch(anotherPlayerName::equals))
                return true;
        }
        return false;
    }

    public static boolean areMissingPlayers(List<ServerPlayerEntity> onlinePlayers){
        var missingPlayers = GameUtils.ArePlayersReady(onlinePlayers);
        return !missingPlayers.isEmpty();
    }

    public static void sendMissingPlayersMessage(ServerPlayerEntity player, List<ServerPlayerEntity> onlinePlayers){
        var missingPlayers = GameUtils.ArePlayersReady(onlinePlayers);
        if (!missingPlayers.isEmpty()) {
            var sb = new StringBuilder();
            missingPlayers.forEach(missingPlayer -> sb.append(missingPlayer).append("\n"));
            player.sendMessage(Text.of("the game cannot be started/resumed because the following players are missing\n" + sb), false);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isGameStateRUNNING(){
        return AllData.FK_GAME_DATA.config.getGameState() == FK.GameState.RUNNING;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isGameStatePAUSE(){
        return AllData.FK_GAME_DATA.config.getGameState() == FK.GameState.PAUSED;
    }

    public static boolean isGameStateNOT_STARTED(){
        return AllData.FK_GAME_DATA.config.getGameState() == FK.GameState.NOT_STARTED;
    }


    public static boolean areAssaultEnabled(int currentDay){
        return currentDay >= Configs.FK_CONFIG.config.dayOfAuthorizationOfTheAssaults;
    }

    public static boolean isNetherEnabled(int currentDay){
        return currentDay >= Configs.FK_CONFIG.config.dayOfAuthorizationOfTheEntryInTheNether;
    }

    public static boolean isEndEnabled(int currentDay){
        return currentDay >= Configs.FK_CONFIG.config.dayOfAuthorizationOfTheEntryInTheEnd;
    }

    public static boolean isPvPEnabled(int currentDay){
        return currentDay >= Configs.FK_CONFIG.config.dayOfAuthorizationOfThePvP;
    }

}

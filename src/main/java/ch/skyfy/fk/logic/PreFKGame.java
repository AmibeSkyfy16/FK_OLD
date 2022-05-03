package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.events.PlayerDamageCallback;
import ch.skyfy.fk.events.PlayerMoveCallback;
import ch.skyfy.fk.logic.data.AllData;
import com.mojang.brigadier.context.CommandContextBuilder;
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.stream.StreamSupport;


@SuppressWarnings({"ConstantConditions", "FieldCanBeLocal"})
public class PreFKGame {

    private final ch.skyfy.fk.commands.CommandManager commandManager;

    public PreFKGame() {
        this.commandManager = new ch.skyfy.fk.commands.CommandManager(this);

        if(GameUtils.isGameStateRUNNING()){
            System.out.println("game was running at restart, game is now paused");
            AllData.FK_GAME_DATA.config.setGameState(FK.GameState.PAUSED);
        }
    }

    public void registerAll() {

        commandManager.registerCommands();

        PlayerJoinCallback.EVENT.register((player, server) -> {
            if(server.getPlayerManager().getPlayerList().size() == 1){

                if(GameUtils.isGameStatePAUSE()) {
                    System.out.println("first player joined and game is paused");
                    var fkGame = new FKGame(server);
                    commandManager.getFkGameRef().set(fkGame);
                }

            }
        });

        PlayerJoinCallback.EVENT.register(this::teleportPlayerToWaitingRoom);
        PlayerDamageCallback.EVENT.register(this::onPlayerDamage);
        PlayerMoveCallback.EVENT.register(this::onPlayerMove);
    }

    @SuppressWarnings("ConstantConditions")
    private void teleportPlayerToWaitingRoom(ServerPlayerEntity player, MinecraftServer server) {
        if(!GameUtils.isGameStateNOT_STARTED())return;

        var spawnLoc = Configs.FK_CONFIG.config.waitingRoom.getSpawnLocation();

        StreamSupport.stream(server.getWorlds().spliterator(), false)
                .filter(serverWorld -> serverWorld.getDimension().getEffects().toString().equals(spawnLoc.getDimensionName()))
                .findFirst()
                .ifPresent(serverWorld -> {
                    // Player like admin player, don't have to be teleported
                    if (!GameUtils.isFKPlayer(server.getPlayerManager().getPlayerList(), player.getName().asString()))
                        return;

                    updateTeam(server, player);
                    ScoreboardManager.getInstance().updateSidebar(player, 0, 0, 0);

                    player.teleport(serverWorld, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), spawnLoc.getYaw(), spawnLoc.getPitch());
                });
    }

    private void updateTeam(MinecraftServer server, ServerPlayerEntity player) {
        var playerName = player.getName().asString();

        var serverScoreboard = server.getScoreboard();

        var fkTeam = GameUtils.getFKTeamOfPlayerByName(playerName);

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

    private ActionResult onPlayerDamage(DamageSource source, float amount) {
        if(!GameUtils.isGameStateNOT_STARTED())return ActionResult.PASS;
        return ActionResult.FAIL;
    }

    /**
     * Prevents the player from leaving the waiting room
     */
    private ActionResult onPlayerMove(PlayerMoveCallback.MoveData moveData, ServerPlayerEntity player) {
        if(!GameUtils.isGameStateNOT_STARTED())return ActionResult.PASS;

        var waitingRoom = Configs.FK_CONFIG.config.waitingRoom;
        var square = waitingRoom.getSquare();

        if (Utils.didPlayerTryToLeaveAnArea(square, player))
            return ActionResult.FAIL;

        return ActionResult.PASS;
    }

}

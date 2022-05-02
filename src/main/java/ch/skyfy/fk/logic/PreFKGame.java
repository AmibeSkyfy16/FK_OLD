package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.commands.StartCmd;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.events.PlayerDamageCallback;
import ch.skyfy.fk.events.PlayerMoveCallback;
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.stream.StreamSupport;

import static ch.skyfy.fk.FK.GAME_STATE;

@SuppressWarnings("ConstantConditions")
public class PreFKGame {

    private final StartCmd startCmd;

    public PreFKGame() {
        this.startCmd = new StartCmd(this);
    }

    public void registerAll() {

        registerCommands();

        PlayerJoinCallback.EVENT.register(this::teleportPlayerToWaitingRoom);
        PlayerDamageCallback.EVENT.register(this::onPlayerDamage);
        PlayerMoveCallback.EVENT.register(this::onPlayerMove);
    }

    private void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("start").executes(startCmd));
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void teleportPlayerToWaitingRoom(ServerPlayerEntity player, MinecraftServer server) {
        if (GAME_STATE != FK.GameState.NOT_STARTED) return;
        var spawnLoc = Configs.FK_CONFIG.config.waitingRoom.getSpawnLocation();

        StreamSupport.stream(server.getWorlds().spliterator(), false)
                .filter(serverWorld -> serverWorld.getDimension().getEffects().toString().equals(spawnLoc.getDimensionName()))
                .findFirst()
                .ifPresent(serverWorld -> {
                    // Player like admin player, don't have to be teleported
                    if(!GameUtils.isFKPlayer(server.getPlayerManager().getPlayerList(), player.getName().asString())) return;

                    updateTeam(server, player);
                    ScoreboardManager.getInstance().updateSidebar(player, 0, 0, 0);

                    player.teleport(serverWorld, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), spawnLoc.getYaw(), spawnLoc.getPitch());
                });
    }

    private void updateTeam(MinecraftServer server, ServerPlayerEntity player){
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
        if (GAME_STATE != FK.GameState.NOT_STARTED) return ActionResult.SUCCESS;
        return ActionResult.FAIL;
    }

    /**
     * Prevents the player from leaving the waiting room
     */
    private ActionResult onPlayerMove(PlayerMoveCallback.MoveData moveData, ServerPlayerEntity player) {

        if (GAME_STATE != FK.GameState.NOT_STARTED) return ActionResult.SUCCESS;

        var waitingRoom = Configs.FK_CONFIG.config.waitingRoom;
        var square = waitingRoom.getSquare();

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

        if (vec != null) {
            player.teleport(vec.x, vec.y, vec.z);
            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

}

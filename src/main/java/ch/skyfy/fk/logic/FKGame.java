package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.tests.TestUtils;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FKGame {

    private final MinecraftServer server;

    public Timeline timeline;

    public FKGame(MinecraftServer server) {
        this.server = server;
        this.timeline = new Timeline(server);
    }


    @SuppressWarnings("ConstantConditions")
    public void start() {
        server.getOverworld().setTimeOfDay(0);

        timeline.startTimer();

        FK.GAME_STATE = FK.GameState.RUNNING;
        var fkPlayers = GameUtils.getFkPlayers(server);
        for (ServerPlayerEntity fkPlayer : fkPlayers) {
            var baseLoc = GameUtils.getBaseCoordinateByPlayer(fkPlayer.getName().asString());
            fkPlayer.sendMessage(Text.of("The game has started, your base is at this coords: X: " + baseLoc.getX() + " Y: " + baseLoc.getY() + " Z: " + baseLoc.getZ()), false);
        }

    }
}

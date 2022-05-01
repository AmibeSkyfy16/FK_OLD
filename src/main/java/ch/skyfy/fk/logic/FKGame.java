package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
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

        TestUtils.printVersion(com.google.gson.Gson.class);
    }


    @SuppressWarnings("ConstantConditions")
    public void start() {
        System.out.println("the game is started");

        server.getOverworld().setTimeOfDay(0);
        var timeOfDay = server.getOverworld().getTimeOfDay();
        System.out.println("time of day is: " + timeOfDay);

        timeline.startTimer();


        var player = server.getPlayerManager().getPlayer("Skyfy16");


//        Scoreboard scoreboard = player.getScoreboard(); // Get the player scoreboard
//        scoreboard.addObjective("FK", ScoreboardCriterion.DUMMY, Text.of("<< Fallen Kingdoms >>"), ScoreboardCriterion.RenderType.INTEGER); // adding objective
//        var objective = scoreboard.getObjective("FK");
//
//        ScoreboardPlayerScore scoreboardPlayerScore = new ScoreboardPlayerScore(scoreboard, objective, "time 1"); // create a score
//        scoreboardPlayerScore.setScore(111111);
//
//        ScoreboardPlayerScore scoreboardPlayerScore2 = new ScoreboardPlayerScore(scoreboard, objective, "time 2"); // create a score
//        scoreboardPlayerScore2.setScore(999999);
//
//        ScoreboardPlayerScore scoreboardPlayerScore3 = new ScoreboardPlayerScore(scoreboard, objective, "time 555"); // create a score
//        scoreboardPlayerScore2.setScore(0);
//
//        scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);
//
//        scoreboard.updateObjective(objective);
//        scoreboard.updateScore(scoreboardPlayerScore);
//        scoreboard.updateScore(scoreboardPlayerScore2);
//        scoreboard.updateScore(scoreboardPlayerScore3);
//
//        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, 2));
//        player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, "FK", "Skyfy16", 1212));

//        anotherScoreboard(player, "info1", Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, 0);
//        anotherScoreboard(player, "info2", Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, 0);
//        anotherScoreboard(player, "info3", Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, 0);

        FK.GAME_STATE = FK.GameState.RUNNING;
        var fkPlayers = GameUtils.getFkPlayers(server);
        for (ServerPlayerEntity fkPlayer : fkPlayers) {
            var baseLoc = GameUtils.getBaseCoordinateByPlayer(fkPlayer.getName().asString());
            fkPlayer.sendMessage(Text.of("The game has started, your base is at this coords: X: " + baseLoc.getX() + " Y: " + baseLoc.getY() + " Z: " + baseLoc.getZ()), false);
        }

    }

    private void anotherScoreboard(ServerPlayerEntity player, String name, int slot, int mode){
        Scoreboard scoreboard = player.getScoreboard(); // Get the player scoreboard
        scoreboard.addObjective(name, ScoreboardCriterion.DUMMY, Text.of("<< INFO >>"), ScoreboardCriterion.RenderType.INTEGER); // adding objective
        var objective = scoreboard.getObjective(name);

        ScoreboardPlayerScore scoreboardPlayerScore = new ScoreboardPlayerScore(scoreboard, objective, "time 1"); // create a score
        scoreboardPlayerScore.setScore(111111);

        ScoreboardPlayerScore scoreboardPlayerScore2 = new ScoreboardPlayerScore(scoreboard, objective, "time 2"); // create a score
        scoreboardPlayerScore2.setScore(999999);

        ScoreboardPlayerScore scoreboardPlayerScore3 = new ScoreboardPlayerScore(scoreboard, objective, "time 555"); // create a score
        scoreboardPlayerScore3.setScore(0);

        scoreboard.setObjectiveSlot(slot, objective);

        scoreboard.updateObjective(objective);
        scoreboard.updateScore(scoreboardPlayerScore);
        scoreboard.updateScore(scoreboardPlayerScore2);
        scoreboard.updateScore(scoreboardPlayerScore3);

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, mode));
    }


}

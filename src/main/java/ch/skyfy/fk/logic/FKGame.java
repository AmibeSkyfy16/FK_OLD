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
        this.timeline = new Timeline();

        TestUtils.printVersion(com.google.gson.Gson.class);
    }


    @SuppressWarnings("ConstantConditions")
    public void start() {
        System.out.println("the game is started");

        timeline.startTimer();

//        var serverScoreBoard = server.getScoreboard();
//        var scoreBoardCriterionOpt = ScoreboardCriterion.getOrCreateStatCriterion("time");
//        var scoreboardCriterion = scoreBoardCriterionOpt.get();
//        serverScoreBoard.addObjective("FK", scoreboardCriterion, Text.of("FK dn"), ScoreboardCriterion.RenderType.INTEGER);

        var player = server.getPlayerManager().getPlayer("Skyfy16");
//
//        Scoreboard playerScoreboard = player.getScoreboard();
//
//        playerScoreboard.addObjective("general", ScoreboardCriterion.DUMMY, Text.of("FK display"), ScoreboardCriterion.RenderType.INTEGER);
//        var objective2 = playerScoreboard.getObjective("general");
//
//
//        ScoreboardPlayerScore scoreboardPlayerScore1 = new ScoreboardPlayerScore(playerScoreboard, objective2, "Skyfy16");
//        scoreboardPlayerScore1.setScore(12212);
//
//        playerScoreboard.setObjectiveSlot(2, objective2); // 2 is sidebar


        // Another try

//        Scoreboard scoreboard = player.getScoreboard();
////        ScoreboardObjective objective = new ScoreboardObjective(scoreboard, "FK", ScoreboardCriterion.DUMMY, Text.of("FK display"), ScoreboardCriterion.RenderType.INTEGER);
////        ScoreboardPlayerScore scoreboardPlayerScore = new ScoreboardPlayerScore(scoreboard, objective, "Skyfy16");
////        scoreboardPlayerScore.setScore(179);
//
//        scoreboard.addObjective("FK2", ScoreboardCriterion.DUMMY, Text.of("FK display2"), ScoreboardCriterion.RenderType.INTEGER);
//        ScoreboardObjective objective = scoreboard.getObjective("FK2");
//
//        ScoreboardPlayerScore scoreboardPlayerScore = new ScoreboardPlayerScore(scoreboard, objective, "Skyfy16");
//        scoreboardPlayerScore.setScore(179);
//
//        scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);
//
//
//        server.getScoreboard().updateScore(scoreboardPlayerScore);
//        server.getScoreboard().updatePlayerScore("Skyfy16");
//        server.getScoreboard().updateObjective(objective);
//
//        player.getScoreboard().updateScore(scoreboardPlayerScore);
//        player.getScoreboard().updatePlayerScore("Skyfy16");
//        player.getScoreboard().updateObjective(objective);
//
//        player.getScoreboard().updateExistingObjective(player.getScoreboard().getObjective("FK2"));


        // Create a scoreboard another try

        Scoreboard scoreboard = player.getScoreboard(); // Get the player scoreboard
        scoreboard.addObjective("FK2", ScoreboardCriterion.DUMMY, Text.of("FK display2"), ScoreboardCriterion.RenderType.INTEGER); // adding objective
        var objective = scoreboard.getObjective("FK2");
        ScoreboardPlayerScore scoreboardPlayerScore = new ScoreboardPlayerScore(scoreboard, objective, "Skyfy16"); // create a score
        scoreboardPlayerScore.setScore(1212121);

        scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);

        scoreboard.updateObjective(objective);
        scoreboard.updateExistingObjective(objective);

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, 2));
        player.networkHandler.sendPacket(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, "FK2", "Skyfy16", 1212));


        FK.GAME_STATE = FK.GameState.RUNNING;
        var fkPlayers = GameUtils.getFkPlayers(server);
        for (ServerPlayerEntity fkPlayer : fkPlayers) {
            var baseLoc = GameUtils.getBaseCoordinateByPlayer(fkPlayer.getName().asString());
            fkPlayer.sendMessage(Text.of("The game has started, your base is at this coords: X: " + baseLoc.getX() + " Y: " + baseLoc.getY() + " Z: " + baseLoc.getZ()), false);
        }

    }

}

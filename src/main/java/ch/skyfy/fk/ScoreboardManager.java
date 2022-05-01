package ch.skyfy.fk;

import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ScoreboardManager {

    @SuppressWarnings("InstantiationOfUtilityClass")
    private static class LazyHolder {
        static final ScoreboardManager INSTANCE = new ScoreboardManager();
    }

    public static ScoreboardManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static final String FK_OBJECTIVE = "FK";

    private String lastDayStr = "";
    private String lastTime = "";

    private ScoreboardManager() {

    }

    public void updateScoreboard(ServerPlayerEntity player){
        var scoreboard = player.getScoreboard();
        if(!scoreboard.containsObjective(FK_OBJECTIVE)){
            scoreboard.addObjective(FK_OBJECTIVE, ScoreboardCriterion.DUMMY, Text.of("<< Fallen Kingdoms >>"), ScoreboardCriterion.RenderType.INTEGER);
            updateScoreboard(player,0, 0, 0, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE);
        }

    }

    public void updateScoreboard(ServerPlayerEntity player, int day, int minutes, int seconds, int mode){
        var scoreboard = player.getScoreboard();
        var objective = scoreboard.getObjective(FK_OBJECTIVE);

        var dayS = scoreboard.getPlayerScore(lastDayStr, objective);
        scoreboard.resetPlayerScore(lastDayStr, null);
        scoreboard.resetPlayerScore(lastTime, null);

        lastDayStr = "Day: " + day;
        lastTime = "Time: " + minutes + ":" + seconds;

        ScoreboardPlayerScore space = new ScoreboardPlayerScore(scoreboard, objective, " ");
        space.setScore(11);

        ScoreboardPlayerScore dayScore = new ScoreboardPlayerScore(scoreboard, objective, lastDayStr);
        dayScore.setScore(1);

        ScoreboardPlayerScore timeScore = new ScoreboardPlayerScore(scoreboard, objective, lastTime);
        timeScore.setScore(0);


        scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);

        scoreboard.updateObjective(objective);
        scoreboard.updateScore(dayScore);
        scoreboard.updateScore(timeScore);

        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, mode));
    }

}

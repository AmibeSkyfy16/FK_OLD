package ch.skyfy.fk;

import ch.skyfy.fk.sidebar.api.Sidebar;
import ch.skyfy.fk.sidebar.api.lines.SidebarLine;
import ch.skyfy.fk.sidebar.mixin.StyleAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ScoreboardManager {

    private static class LazyHolder {
        static final ScoreboardManager INSTANCE = new ScoreboardManager();
    }
    public static ScoreboardManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private final Sidebar generalSidebar;

    private ScoreboardManager() {
        generalSidebar = new Sidebar(Sidebar.Priority.MEDIUM);
        generalSidebar.setTitle(new LiteralText("<< Fallen Kingdoms >>").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)));
    }

    public void updateSidebar(ServerPlayerEntity player, int day, int minutes, int seconds){
        updateSidebar(day, minutes, seconds);

        if(generalSidebar.getPlayerHandlerSet().stream().noneMatch(serverPlayNetworkHandler -> serverPlayNetworkHandler.player.equals(player)))
            generalSidebar.addPlayer(player);

        generalSidebar.show();
    }

    private void updateSidebar(int day, int minutes, int seconds){
        generalSidebar.setLine(11, new LiteralText("Day: %d".formatted(day)).setStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        generalSidebar.setLine(10, new LiteralText("Time: %d:%s".formatted(minutes, seconds)).setStyle(Style.EMPTY.withColor(Formatting.BLUE)));

        generalSidebar.setLine(9, new LiteralText("PvP: Disabled" ).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        generalSidebar.setLine(8, new LiteralText("Nether: Disabled" ).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        generalSidebar.setLine(7, new LiteralText("End: Disabled" ).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        generalSidebar.setLine(6, new LiteralText("Assault: Disabled" ).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }

    public void test3(ServerPlayerEntity player){
        try {
            Sidebar sidebar = new Sidebar(Sidebar.Priority.LOW);
            boolean bool = Math.random() > 0.5;
            System.out.println(bool);
            sidebar.setTitle(new LiteralText("LOW").setStyle(Style.EMPTY.withColor(bool ? Formatting.GOLD : Formatting.AQUA)));

            sidebar.setLine(0, new LiteralText("Hello World! " + (int) (Math.random() * 1000)).setStyle(
                    StyleAccessor.invokeInit(
                            TextColor.fromRgb((int) (Math.random() * 0xFFFFFF)),
                            Math.random() > 0.5,
                            Math.random() > 0.5,
                            Math.random() > 0.5,
                            Math.random() > 0.5,
                            Math.random() > 0.5,
                            null,
                            null,
                            null,
                            Math.random() > 0.6 ? new Identifier("default") : Math.random() > 0.3 ? new Identifier("uniform") : new Identifier("alt")
                    )
            ));
            int speed = (int) (Math.random() * 20);
            sidebar.setUpdateRate(speed);

            sidebar.addLines(SidebarLine.create(2, new LiteralText("" + speed)));

            sidebar.addLines(SidebarLine.create(2, (p) -> {
                System.out.println(p.age);
                return new LiteralText("" + p.age);
            }));

            sidebar.addPlayer(player);
            sidebar.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    public void updateScoreboard(ServerPlayerEntity player){
//        var scoreboard = player.getScoreboard();
//        if(!scoreboard.containsObjective(FK_OBJECTIVE)){
//            scoreboard.addObjective(FK_OBJECTIVE, ScoreboardCriterion.DUMMY, Text.of("<< Fallen Kingdoms >>"), ScoreboardCriterion.RenderType.INTEGER);
//            updateScoreboard(player,0, 0, 0, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE);
//        }
//
//    }
//
//    public void updateScoreboard(ServerPlayerEntity player, int day, int minutes, int seconds, int mode){
//        var scoreboard = player.getScoreboard();
//        var objective = scoreboard.getObjective(FK_OBJECTIVE);
//
////        var dayS = scoreboard.getPlayerScore(lastDayStr, objective);
////        scoreboard.resetPlayerScore(lastDayStr, null);
////        scoreboard.resetPlayerScore(lastTime, null);
//
//        lastDayStr = "Day: " + day;
//        lastTime = "Time: " + minutes + ":" + seconds;
//
//        ScoreboardPlayerScore space = new ScoreboardPlayerScore(scoreboard, objective, " ");
//        space.setScore(11);
//
//        ScoreboardPlayerScore dayScore = new ScoreboardPlayerScore(scoreboard, objective, lastDayStr);
//        dayScore.setScore(1);
//
//        ScoreboardPlayerScore timeScore = new ScoreboardPlayerScore(scoreboard, objective, lastTime);
//        timeScore.setScore(0);
//
//
//        scoreboard.setObjectiveSlot(Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);
//
//        scoreboard.updateObjective(objective);
//        scoreboard.updateScore(dayScore);
//        scoreboard.updateScore(timeScore);
//
//        player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, mode));
//    }

}

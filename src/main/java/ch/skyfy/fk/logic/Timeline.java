package ch.skyfy.fk.logic;

import ch.skyfy.fk.ScoreboardManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timeline {

    private int day;

    private long minutes, seconds;

    private final MinecraftServer server;

    private long startTick = 0;

    public Timeline(MinecraftServer server) {
        this.server = server;
        day = 1;
    }

    public void startTimer() {
        startTick = server.getOverworld().getTime();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> new Thread(r) {{
            setDaemon(true);
        }});
        executor.scheduleAtFixedRate(() -> {
            var timeInTick = server.getOverworld().getTimeOfDay();

            minutes = timeInTick / 1200;
            seconds = timeInTick / 20 - minutes * 60;

            var roundMinutes = Math.round(minutes);
            var roundSeconds = Math.round(seconds);

//            String s = "" + roundMinutes + ":" + roundSeconds;
//            System.out.println("" + (timeInTick) + " tick(s) = " + s + "  second(s).");

            if (server.getOverworld().getTime() - startTick >= 24_000) {
                day++;
                startTick = server.getOverworld().getTime();
            }

            for (ServerPlayerEntity fkPlayer : GameUtils.getFkPlayers(server)) {
//                ScoreboardManager.getInstance().updateScoreboard(fkPlayer, day, roundMinutes, roundSeconds);
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

}

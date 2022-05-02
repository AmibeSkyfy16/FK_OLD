package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.ScoreboardManager;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Timeline {

    private final AtomicInteger dayRef, minutesRef, secondsRef;

    private final MinecraftServer server;

    {
        dayRef = new AtomicInteger(0);
        minutesRef = new AtomicInteger(0);
        secondsRef = new AtomicInteger(0);
    }

    public Timeline(MinecraftServer server) {
        this.server = server;
    }

    public void startTimer() {
        dayRef.getAndIncrement();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> new Thread(r) {{
            setDaemon(true);
        }});
        executor.scheduleAtFixedRate(() -> {
            var timeInTick = server.getOverworld().getTimeOfDay();

            minutesRef.set((int) (timeInTick / 1200d));
            secondsRef.set((int) (((timeInTick / 1200d) - minutesRef.get()) * 60));

            if(timeInTick >= 24000) {
                server.getOverworld().setTimeOfDay(0L);
                dayRef.getAndIncrement();
            }

            // Update player sidebar
            for (var fkPlayer : GameUtils.getFkPlayers(server))
                ScoreboardManager.getInstance().updateSidebar(fkPlayer, dayRef.get(), minutesRef.get(), secondsRef.get());

        }, 0, 500, TimeUnit.MILLISECONDS);

    }

}

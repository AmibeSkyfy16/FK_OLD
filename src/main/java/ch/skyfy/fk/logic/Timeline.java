package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.config.core.ConfigUtils;
import ch.skyfy.fk.logic.data.AllData;
import ch.skyfy.fk.logic.data.TimelineData;
import com.google.common.reflect.TypeToken;
import io.netty.util.concurrent.ScheduledFuture;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static ch.skyfy.fk.config.core.BetterConfig.CONFIG_DIRECTORY;

public class Timeline {

    private final AtomicBoolean isRunningRef = new AtomicBoolean(false);

    public final TimelineData timelineData;

    private final MinecraftServer server;


    {
        timelineData = AllData.FK_GAME_DATA.config.getTimelineData();
    }

    public Timeline(MinecraftServer server) {
        this.server = server;
    }

    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    public void startTimer() {
        isRunningRef.set(true);
        var count = new AtomicInteger(0);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> new Thread(r) {{
            setDaemon(true);
        }});
        executor.scheduleAtFixedRate(() -> {
            if(AllData.FK_GAME_DATA.config.getGameState() != FK.GameState.RUNNING) return;

            var timeInTick = server.getOverworld().getTimeOfDay();

            timelineData.setMinutes((int) (timeInTick / 1200d));
            timelineData.setSeconds((int) (((timeInTick / 1200d) - timelineData.getMinutes()) * 60));

            if(timeInTick >= 24000) {
                server.getOverworld().setTimeOfDay(0L);
                timelineData.setDay(timelineData.getDay() + 1);
            }

            // Update player sidebar
            for (var fkPlayer : GameUtils.getFkPlayers(server))
                ScoreboardManager.getInstance().updateSidebar(fkPlayer, timelineData.getDay(), timelineData.getMinutes(), timelineData.getSeconds());

            // every minute, we save FK GAME DATA
            if (count.get() >= 120) {
                count.set(0);
                var configData = AllData.FK_GAME_DATA;
                var type = TypeToken.of(AllData.FK_GAME_DATA.cClass).getType();
                var configFile = CONFIG_DIRECTORY.resolve(configData.relativeFilePath).toFile();
                try {
                    ConfigUtils.save(configFile, type, configData.config);
                } catch (IOException e) {
                    System.out.println("erreur de sauvegarde des données");
                    e.printStackTrace();
                }
            }

            count.getAndIncrement();

        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public AtomicBoolean getIsRunningRef() {
        return isRunningRef;
    }
}

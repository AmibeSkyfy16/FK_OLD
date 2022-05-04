package ch.skyfy.fk.logic;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.ScoreboardManager;
import ch.skyfy.fk.config.core.ConfigUtils;
import ch.skyfy.fk.events.TimeOfDayUpdatedCallback;
import ch.skyfy.fk.logic.data.AllData;
import ch.skyfy.fk.logic.data.TimelineData;
import com.google.common.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.skyfy.fk.config.core.BetterConfig.CONFIG_DIRECTORY;

public class Timeline {

    private final AtomicBoolean isStartedRef = new AtomicBoolean(false);

    public final TimelineData timelineData;

    private final MinecraftServer server;


    {
        timelineData = AllData.FK_GAME_DATA.config.getTimelineData();
    }

    public Timeline(MinecraftServer server) {
        this.server = server;

        TimeOfDayUpdatedCallback.EVENT.register(timeOfDay -> {
            if(GameUtils.isGameStateRUNNING()){
                if(isStartedRef.get()){
                    updateTime(timeOfDay);
                }
            }

            return ActionResult.PASS;
        });
    }

    public void startTimer() {
        isStartedRef.set(true);
    }

    private void updateTime(long timeOfDay){

        var previousMinutes = timelineData.getMinutes();

        timelineData.setMinutes((int) (timeOfDay / 1200d));
        timelineData.setSeconds((int) (((timeOfDay / 1200d) - timelineData.getMinutes()) * 60));

        if(timeOfDay >= 24000) {
            server.getOverworld().setTimeOfDay(0L);
            timelineData.setDay(timelineData.getDay() + 1);
        }

        if(previousMinutes != timelineData.getMinutes())
            saveData();

        // Update player sidebar
        for (var fkPlayer : GameUtils.getFkPlayers(server))
            ScoreboardManager.getInstance().updateSidebar(fkPlayer, timelineData.getDay(), timelineData.getMinutes(), timelineData.getSeconds());

    }

    @SuppressWarnings("UnstableApiUsage")
    private void saveData(){
        var configData = AllData.FK_GAME_DATA;
        var type = TypeToken.of(AllData.FK_GAME_DATA.cClass).getType();
        var configFile = CONFIG_DIRECTORY.resolve(configData.relativeFilePath).toFile();
        try {
            ConfigUtils.save(configFile, type, configData.config);
        } catch (IOException e) {
            FK.LOGGER.warn("An error occurred while trying to save game data");
            e.printStackTrace();
        }
    }

    public AtomicBoolean getIsStartedRef() {
        return isStartedRef;
    }
}

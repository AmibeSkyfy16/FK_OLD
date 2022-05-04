package ch.skyfy.fk.logic;

import ch.skyfy.fk.FKMod;
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

        // TODO TICK PROBLEM -> second timer

        var previousMinutes = timelineData.getMinutes();

        var remainingTime = timeOfDay % 24_000;

        timelineData.setMinutes((int) (remainingTime / 1200d));
        timelineData.setSeconds((int) (((remainingTime / 1200d) - timelineData.getMinutes()) * 60));

        if(remainingTime == 0)
            timelineData.setDay(timelineData.getDay() + 1);

        // TODO
//        if(previousMinutes != timelineData.getMinutes())
//            saveData();

        // Update player sidebar
        for (var fkPlayer : GameUtils.getAllConnectedFKPlayers(server.getPlayerManager().getPlayerList()))
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
            FKMod.LOGGER.warn("An error occurred while trying to save game data");
            e.printStackTrace();
        }
    }

    public AtomicBoolean getIsStartedRef() {
        return isStartedRef;
    }
}

package ch.skyfy.fk;


import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.core.BetterConfig;
import ch.skyfy.fk.config.core.ConfigUtils;
import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.PreFKGame;
import ch.skyfy.fk.logic.data.AllData;
import ch.skyfy.fk.logic.data.FKGameData;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FK implements DedicatedServerModInitializer {

    public enum GameState {
        NOT_STARTED,
        RUNNING,
        PAUSED
    }

    public static final String MOD_ID = "fk";

    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean DISABLED = false;

    private final PreFKGame preFKGame;

    public FK() throws Exception {
        if(BetterConfig.initialize(new Class[]{AllData.class}) || BetterConfig.initialize(new Class[]{Configs.class})){
            DISABLED = true;
            throw new Exception("GAME IS DISABLE DU TO ERROR IN JSON CONFIGS");
        }

        preFKGame = new PreFKGame();
    }

    @Override
    public void onInitializeServer() {
        if(DISABLED)return;
        preFKGame.registerAll();
    }

}

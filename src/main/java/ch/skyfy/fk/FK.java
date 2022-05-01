package ch.skyfy.fk;


import ch.skyfy.fk.commands.StartCmd;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.core.BetterConfig;
import ch.skyfy.fk.logic.PreFKGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FK implements ModInitializer {

    public enum GameState {
        NOT_STARTED,
        RUNNING,
        PAUSED
    }

    public static final String MOD_ID = "fk";

    public static final Logger LOGGER = LogManager.getLogger();

    public static GameState GAME_STATE = GameState.NOT_STARTED;

    private final PreFKGame preFKGame;

    public FK() {
        preFKGame = new PreFKGame();
    }

    @Override
    public void onInitialize() {
        if (BetterConfig.initialize(new Class[]{Configs.class})) return;

        preFKGame.registerAll();
        registerCommands();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated) StartCmd.register(dispatcher);
        });
    }

}

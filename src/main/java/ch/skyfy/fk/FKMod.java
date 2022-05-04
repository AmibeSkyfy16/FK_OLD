package ch.skyfy.fk;


import ch.skyfy.fk.commands.PauseCmd;
import ch.skyfy.fk.commands.ResumeCmd;
import ch.skyfy.fk.commands.StartCmd;
import ch.skyfy.fk.commands.WhereIAmCmd;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.core.BetterConfig;
import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.GameUtils;
import ch.skyfy.fk.logic.data.AllData;
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FKMod implements DedicatedServerModInitializer {

    public enum GameState {
        NOT_STARTED,
        RUNNING,
        PAUSED
    }

    public static final String MOD_ID = "fk";

    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean DISABLED = false;

    private boolean firstJoin = false;

    private final AtomicReference<Optional<FKGame>> optFKGameRef;

    private final StartCmd startCmd;
    private final PauseCmd pauseCmd;
    private final ResumeCmd resumeCmd;

    public FKMod() throws Exception {
        if(BetterConfig.initialize(new Class[]{AllData.class}) || BetterConfig.initialize(new Class[]{Configs.class})){
            DISABLED = true;
            throw new Exception("GAME IS DISABLE DU TO ERROR IN JSON CONFIGS");
        }

        optFKGameRef = new AtomicReference<>(Optional.empty());

        startCmd = new StartCmd(optFKGameRef);
        pauseCmd = new PauseCmd(optFKGameRef);
        resumeCmd = new ResumeCmd(optFKGameRef);
    }

    @Override
    public void onInitializeServer() {
        if(DISABLED)return;
        PlayerJoinCallback.EVENT.register(this::onFirstPlayerJoin);
        registerCommands();
    }

    private void onFirstPlayerJoin(ServerPlayerEntity player, MinecraftServer server){
        if(firstJoin)return;
        if(server.getPlayerManager().getPlayerList().size() == 1){
            System.out.println("Player " + player.getName().asString() + " is the first player on the server");
            firstJoin = true;

            final var fkGame = new FKGame(server);
            optFKGameRef.set(Optional.of(fkGame));

            if(GameUtils.isGameStateRUNNING())
                AllData.FK_GAME_DATA.config.setGameState(GameState.PAUSED);

            if(GameUtils.isGameStatePAUSE())
                fkGame.addPlayerPosIf_PAUSED(player);

        }
    }

    public void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKStart").executes(startCmd));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKPause").executes(pauseCmd));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKResume").executes(resumeCmd));

            dispatcher.register(net.minecraft.server.command.CommandManager.literal("WhereIAm").executes(new WhereIAmCmd()));
        });
    }
}

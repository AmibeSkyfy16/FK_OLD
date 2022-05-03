package ch.skyfy.fk;


import ch.skyfy.fk.commands.*;
import ch.skyfy.fk.config.Configs;
import ch.skyfy.fk.config.core.BetterConfig;
import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.GameUtils;
import ch.skyfy.fk.logic.data.AllData;
import com.mojang.brigadier.Command;
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FK implements DedicatedServerModInitializer {

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

    private final StartCmd2 startCmd2;
    private final PauseCmd2 pauseCmd2;
    private final ResumeCmd2 resumeCmd2;

    public FK() throws Exception {
        if(BetterConfig.initialize(new Class[]{AllData.class}) || BetterConfig.initialize(new Class[]{Configs.class})){
            DISABLED = true;
            throw new Exception("GAME IS DISABLE DU TO ERROR IN JSON CONFIGS");
        }
        optFKGameRef = new AtomicReference<Optional<FKGame>>(Optional.empty());

        startCmd2 = new StartCmd2(optFKGameRef);
        pauseCmd2 = new PauseCmd2(optFKGameRef);
        resumeCmd2 = new ResumeCmd2(optFKGameRef);
    }

    @Override
    public void onInitializeServer() {
        if(DISABLED)return;
        PlayerJoinCallback.EVENT.register(this::onFirstPlayerJoin);


        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKStart2").executes(startCmd2));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKPause2").executes(pauseCmd2));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKResume2").executes(resumeCmd2));
        });
    }


    private void onFirstPlayerJoin(ServerPlayerEntity player, MinecraftServer server){
        if(firstJoin)return;
        if(server.getPlayerManager().getPlayerList().size() == 1){
            System.out.println("Player " + player.getName().asString() + " is the first player on the server");
            firstJoin = true;

            final var fkGame = new FKGame(server);
            optFKGameRef.set(Optional.of(fkGame));

            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKStart").executes(fkGame.getStartCmd()));
                dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKPause").executes(fkGame.getPauseCmd()));
                dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKResume").executes(fkGame.getResumeCmd()));
            });

            if(GameUtils.isGameStateRUNNING()){
                AllData.FK_GAME_DATA.config.setGameState(GameState.PAUSED);
            }

            if(GameUtils.isGameStatePAUSE()){
                fkGame.resume(player);
            }

        }
    }
}

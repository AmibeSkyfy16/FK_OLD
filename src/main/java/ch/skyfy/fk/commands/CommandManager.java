package ch.skyfy.fk.commands;

import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.PreFKGame;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.util.concurrent.atomic.AtomicReference;


@SuppressWarnings("FieldCanBeLocal")
public class CommandManager {

    private final StartCmd startCmd;

    private final PauseCmd pauseCmd;

    private final ResumeCmd resumeCmd;

    private final AtomicReference<FKGame> fkGameRef;

    public CommandManager(PreFKGame preFKGame){
        fkGameRef = new AtomicReference<>(null);
        startCmd = new StartCmd(preFKGame, fkGameRef);
        pauseCmd = new PauseCmd(preFKGame, fkGameRef);
        resumeCmd = new ResumeCmd(preFKGame, fkGameRef);
    }

    public void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKStart").executes(startCmd));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKPause").executes(pauseCmd));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKResume").executes(resumeCmd));
        });
    }
}

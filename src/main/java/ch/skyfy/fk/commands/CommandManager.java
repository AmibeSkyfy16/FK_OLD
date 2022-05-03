package ch.skyfy.fk.commands;

import ch.skyfy.fk.logic.FKGame;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;


@SuppressWarnings("FieldCanBeLocal")
public class CommandManager {

    private final StartCmd startCmd;

    private final PauseCmd pauseCmd;

    private final ResumeCmd resumeCmd;

    public CommandManager(final FKGame fkGame){
        startCmd = new StartCmd(fkGame);
        pauseCmd = new PauseCmd(fkGame);
        resumeCmd = new ResumeCmd(fkGame);
    }

    public void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKStart").executes(startCmd));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKPause").executes(pauseCmd));
            dispatcher.register(net.minecraft.server.command.CommandManager.literal("FKResume").executes(resumeCmd));
        });
    }
}

package ch.skyfy.fk.commands;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.PreFKGame;
import ch.skyfy.fk.logic.data.AllData;
import ch.skyfy.fk.logic.data.FKGameData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.util.Util.NIL_UUID;

public class PauseCmd implements Command<ServerCommandSource> {

    private final PreFKGame preFKGame;

    private final AtomicReference<FKGame> fkGameRef;

    private final FKGameData fkGameData = AllData.FK_GAME_DATA.config;

    public PauseCmd(PreFKGame preFKGame, AtomicReference<FKGame> fkGameRef) {
        this.preFKGame = preFKGame;
        this.fkGameRef = fkGameRef;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        var source = context.getSource();
        var player = source.getPlayer();

        if (!player.hasPermissionLevel(4)) {
            player.sendMessage(Text.of("You dont have required privileges to use this command"), false);
            return 0;
        }

        switch (fkGameData.getGameState()){
            case NOT_STARTED -> player.sendMessage(new LiteralText("The game cannot be paused because it is not started !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case PAUSED -> player.sendMessage(new LiteralText("The game is already paused !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case RUNNING -> {

                source.getServer().getPlayerManager().broadcast(Text.of("The game has been paused"), MessageType.CHAT, NIL_UUID);

                // Normally the fkGame should not be null, but you never know
                if(fkGameRef.get() != null){
                    fkGameData.setGameState(FK.GameState.PAUSED);
                    fkGameRef.get().pause();
                }
            }
        }

        return 0;
    }

}

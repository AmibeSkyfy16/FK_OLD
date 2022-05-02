package ch.skyfy.fk.commands;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.PreFKGame;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.util.Util.NIL_UUID;

public class ResumeCmd implements Command<ServerCommandSource> {

    private final PreFKGame preFKGame;

    private final AtomicReference<FKGame> fkGameRef;

    public ResumeCmd(PreFKGame preFKGame, AtomicReference<FKGame> fkGameRef) {
        this.preFKGame = preFKGame;
        this.fkGameRef = fkGameRef;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        var source = context.getSource();
        var player = source.getPlayer();

        switch (FK.GAME_STATE){
            case NOT_STARTED -> player.sendMessage(new LiteralText("The game cannot be resumed because it is not started !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case RUNNING -> player.sendMessage(new LiteralText("The game cannot be resumed because it is running !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case PAUSED -> {

                source.getServer().getPlayerManager().broadcast(new LiteralText("The game has been resumed").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), MessageType.CHAT, NIL_UUID);

                // Normally the fkGame should not be null, but you never know
                if(fkGameRef.get() != null){
                    FK.GAME_STATE = FK.GameState.RUNNING;
                    fkGameRef.get().resume();
                }
            }
        }

        return 0;
    }
}

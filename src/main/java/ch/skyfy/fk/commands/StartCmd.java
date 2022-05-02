package ch.skyfy.fk.commands;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.GameUtils;
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

@SuppressWarnings({"FieldCanBeLocal", "ClassCanBeRecord"})
public class StartCmd implements Command<ServerCommandSource> {

    private final PreFKGame preFKGame;

    private final AtomicReference<FKGame> fkGameRef;

    public StartCmd(PreFKGame preFKGame, AtomicReference<FKGame> fkGameRef) {
        this.preFKGame = preFKGame;
        this.fkGameRef = fkGameRef;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        var source = context.getSource();
        var player = source.getPlayer();

        switch (FK.GAME_STATE){
            case PAUSED -> player.sendMessage(new LiteralText("The game cannot be started because it is paused !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case RUNNING -> player.sendMessage(new LiteralText("The game has already started !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case NOT_STARTED -> {

                if (!player.hasPermissionLevel(4)) {
                    player.sendMessage(Text.of("You dont have required privileges to start the game"), false);
                    return 0;
                }

                // TODO UNCOMMENT
//                var missingPlayers = GameUtils.ArePlayersReady(source.getServer().getPlayerManager().getPlayerList());
//                if (!missingPlayers.isEmpty()) {
//                    var sb = new StringBuilder();
//                    missingPlayers.forEach(missingPlayer -> sb.append(missingPlayer).append("\n"));
//                    player.sendMessage(Text.of("the game cannot be started because the following players are missing\n" + sb), false);
//                    return 0;
//                }

                source.getServer().getPlayerManager().broadcast(new LiteralText("The game begins !").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), MessageType.CHAT, NIL_UUID);

                FK.GAME_STATE = FK.GameState.RUNNING;

                fkGameRef.set(new FKGame(source.getServer()));
                fkGameRef.get().start();
            }
        }

        return 0;
    }

}

package ch.skyfy.fk.commands;

import ch.skyfy.fk.FK;
import ch.skyfy.fk.logic.FKGame;
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

import static net.minecraft.util.Util.NIL_UUID;

@SuppressWarnings({"FieldCanBeLocal", "ClassCanBeRecord"})
public class StartCmd implements Command<ServerCommandSource> {

    private final FKGame fkGame;

    private final FKGameData fkGameData = AllData.FK_GAME_DATA.config;

    public StartCmd(FKGame fkGame) {
        this.fkGame = fkGame;
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
            case PAUSED -> player.sendMessage(new LiteralText("The game cannot be started because it is paused !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case RUNNING -> player.sendMessage(new LiteralText("The game has already started !").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            case NOT_STARTED -> {

                // TODO UNCOMMENT
//                if(GameUtils.areMissingPlayers(source.getServer().getPlayerManager().getPlayerList())){
//                    GameUtils.sendMissingPlayersMessage(player, source.getServer().getPlayerManager().getPlayerList());
//                }

                source.getServer().getPlayerManager().broadcast(new LiteralText("The game begins !").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), MessageType.CHAT, NIL_UUID);

                fkGameData.setGameState(FK.GameState.RUNNING);

                fkGame.start();
            }
        }

        return 0;
    }

}

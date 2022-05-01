package ch.skyfy.fk.commands;

import ch.skyfy.fk.logic.FKGame;
import ch.skyfy.fk.logic.GameUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class StartCmd implements Command<ServerCommandSource> {


    public StartCmd() {

    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();
        var fkGame = new FKGame(source.getServer());

        if(!player.hasPermissionLevel(4)){
            player.sendMessage(Text.of("You dont have required privileges to start the game"), false);
            return 0;
        }

        // TODO UNCOMMENT
//        var missingPlayers = GameUtils.ArePlayersReady(source.getServer().getPlayerManager().getPlayerList());
//        if(!missingPlayers.isEmpty()){
//            var sb = new StringBuilder();
//            missingPlayers.forEach(missingPlayer -> sb.append(missingPlayer).append("\n"));
//            player.sendMessage(Text.of("the game cannot be started because the following players are missing\n" + sb), false);
//            return 0;
//        }

        fkGame.start();

        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("start").executes(new StartCmd()));
    }

}

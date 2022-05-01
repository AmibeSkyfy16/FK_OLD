package ch.skyfy.fk.logic;

import ch.skyfy.fk.tests.TestUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FKGame {

    private final MinecraftServer server;

    public Timeline timeline;


    public FKGame(MinecraftServer server) {
        this.server = server;
        this.timeline = new Timeline();

        TestUtils.printVersion(com.google.gson.Gson.class);
    }


    @SuppressWarnings("ConstantConditions")
    public void start() {
        System.out.println("the game is started");

        var fkPlayers = GameUtils.getFkPlayers(server);
        for (ServerPlayerEntity fkPlayer : fkPlayers) {
            var baseLoc = GameUtils.getBaseCoordinateByPlayer(fkPlayer.getName().asString());
            fkPlayer.sendMessage(Text.of("The game has started, your base is at this coords: X: " + baseLoc.getX() + " Y: " + baseLoc.getY() + " Z: " + baseLoc.getZ()), false);
        }

    }

}

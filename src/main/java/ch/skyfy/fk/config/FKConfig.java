package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.SpawnLocation;
import ch.skyfy.fk.config.data.Square;
import ch.skyfy.fk.config.data.WaitingRoom;

public class FKConfig {

    public WaitingRoom waitingRoom;

    public FKConfig() {
        this.waitingRoom = new WaitingRoom(
                new Square((short) 5, 0, -33, 0),
                new SpawnLocation("minecraft:overworld", 0, -33, 0, 69, 69)
        );
    }

}

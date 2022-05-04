package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.Cube;
import ch.skyfy.fk.config.data.SpawnLocation;
import ch.skyfy.fk.config.data.WaitingRoom;
import lombok.Getter;

public class FKConfig {
    @Getter
    private final int dayOfAuthorizationOfTheAssaults;
    @Getter
    private final int dayOfAuthorizationOfTheEntryInTheNether;
    @Getter
    private final int dayOfAuthorizationOfTheEntryInTheEnd;
    @Getter
    private final int dayOfAuthorizationOfThePvP;
    @Getter
    private final WaitingRoom waitingRoom;
    @Getter
    private final SpawnLocation worldSpawn;

    public FKConfig() {

        dayOfAuthorizationOfTheAssaults = 6;
        dayOfAuthorizationOfTheEntryInTheNether = 3;
        dayOfAuthorizationOfTheEntryInTheEnd = 3;
        dayOfAuthorizationOfThePvP = 2;

        this.waitingRoom = new WaitingRoom(
                new Cube((short) 5, 5,5,0, -33, 0),
                new SpawnLocation("minecraft:overworld", 0, -33, 0, 69, 69)
        );

        this.worldSpawn = new SpawnLocation("minecraft:overworld", 110, -33, 110, 69, 69);
    }

}

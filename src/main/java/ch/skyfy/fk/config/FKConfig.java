package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.SpawnLocation;
import ch.skyfy.fk.config.data.Cube;
import ch.skyfy.fk.config.data.WaitingRoom;

public class FKConfig {

    public int dayOfAuthorizationOfTheAssaults;

    public int dayOfAuthorizationOfTheEntryInTheNether;

    public int dayOfAuthorizationOfTheEntryInTheEnd;

    public int dayOfAuthorizationOfThePvP;

    public WaitingRoom waitingRoom;

    public SpawnLocation worldSpawn;

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

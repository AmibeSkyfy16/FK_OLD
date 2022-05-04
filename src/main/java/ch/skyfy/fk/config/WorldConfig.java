package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.Cube;
import ch.skyfy.fk.config.data.WorldInfo;

public class WorldConfig {

    public WorldInfo worldInfo;

    public WorldConfig() {
        worldInfo = new WorldInfo("minecraft:overworld", new Cube((short)200,500, 500, 0, -33, 0));
    }
}

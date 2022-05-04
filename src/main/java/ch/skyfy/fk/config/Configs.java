package ch.skyfy.fk.config;

import ch.skyfy.fk.config.core.ConfigData;
import ch.skyfy.fk.FKMod;

/**
 * This class is loaded by reflection
 * This class contains all loaded configurations for the mods
 */
public class Configs {
    public static final ConfigData<FKConfig> FK = new ConfigData<>("fkconfig.json", FKConfig.class);
    public static final ConfigData<TeamsConfig> TEAMS = new ConfigData<>("teams.json", TeamsConfig.class);

    static {
        FKMod.LOGGER.info(Configs.class.getName() + " has been loaded");
    }
}

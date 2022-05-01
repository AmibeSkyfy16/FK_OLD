package ch.skyfy.fk.config;

import ch.skyfy.fk.config.core.ConfigData;
import ch.skyfy.fk.FK;

/**
 * This class is loaded by reflection
 * This class contains all loaded configurations for the mods
 */
public class Configs {
    public static final ConfigData<FKConfig> FK_CONFIG = new ConfigData<>("fkconfig.json", FKConfig.class);
    public static final ConfigData<TeamsConfig> BASES_CONFIG = new ConfigData<>("bases.json", TeamsConfig.class);

    static {
        FK.LOGGER.info(Configs.class.getName() + " has been loaded");
    }
}

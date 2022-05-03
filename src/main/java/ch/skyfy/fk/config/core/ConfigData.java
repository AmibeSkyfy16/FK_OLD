package ch.skyfy.fk.config.core;

public class ConfigData<C> {
    public final C config;
    public final String relativeFilePath;
    public final Class<C> cClass;
    public ConfigData(String relativeFilePath, Class<C> cClass) {
        this.relativeFilePath = relativeFilePath;
        this.cClass = cClass;
        config = ConfigUtils.getOrCreateConfig(cClass, relativeFilePath);
    }
}

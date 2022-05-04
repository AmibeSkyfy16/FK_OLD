package ch.skyfy.fk.config.data;

public class WorldInfo {

    private String dimensionName;

    private Cube worldDimension;

    public WorldInfo() {}

    public WorldInfo(String dimensionName, Cube worldDimension) {
        this.dimensionName = dimensionName;
        this.worldDimension = worldDimension;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public Cube getWorldDimension() {
        return worldDimension;
    }

    public void setWorldDimension(Cube worldDimension) {
        this.worldDimension = worldDimension;
    }
}

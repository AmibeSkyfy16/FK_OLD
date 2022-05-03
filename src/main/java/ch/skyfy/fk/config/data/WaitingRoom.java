package ch.skyfy.fk.config.data;

public class WaitingRoom {

    private Cube cube;

    private SpawnLocation spawnLocation;

    public WaitingRoom() {}

    public WaitingRoom(Cube cube, SpawnLocation spawnLocation) {
        this.cube = cube;
        this.spawnLocation = spawnLocation;
    }

    public Cube getCube() {
        return cube;
    }

    public void setSquare(Cube cube) {
        this.cube = cube;
    }

    public SpawnLocation getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(SpawnLocation spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}

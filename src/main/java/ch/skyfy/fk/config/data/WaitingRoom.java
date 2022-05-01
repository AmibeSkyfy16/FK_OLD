package ch.skyfy.fk.config.data;

public class WaitingRoom {

    private Square square;

    private SpawnLocation spawnLocation;

    public WaitingRoom() {}

    public WaitingRoom(Square square, SpawnLocation spawnLocation) {
        this.square = square;
        this.spawnLocation = spawnLocation;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }

    public SpawnLocation getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(SpawnLocation spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}

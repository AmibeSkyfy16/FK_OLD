package ch.skyfy.fk.logic.data;

import ch.skyfy.fk.FKMod;

public class FKGameData {

    private FKMod.GameState gameState;

    private TimelineData timelineData;

    public FKGameData() {
        gameState = FKMod.GameState.NOT_STARTED;
        timelineData = new TimelineData(1, 0, 0);
    }

    public FKMod.GameState getGameState() {
        return gameState;
    }

    public void setGameState(FKMod.GameState gameState) {
        this.gameState = gameState;
    }

    public TimelineData getTimelineData() {
        return timelineData;
    }

    public void setTimelineData(TimelineData timelineData) {
        this.timelineData = timelineData;
    }
}

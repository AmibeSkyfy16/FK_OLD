package ch.skyfy.fk.logic.data;

import ch.skyfy.fk.FK;

public class FKGameData {

    private FK.GameState gameState;

    private TimelineData timelineData;

    public FKGameData() {
        gameState = FK.GameState.NOT_STARTED;
        timelineData = new TimelineData(1, 0, 0);
    }

    public FK.GameState getGameState() {
        return gameState;
    }

    public void setGameState(FK.GameState gameState) {
        this.gameState = gameState;
    }

    public TimelineData getTimelineData() {
        return timelineData;
    }

    public void setTimelineData(TimelineData timelineData) {
        this.timelineData = timelineData;
    }
}

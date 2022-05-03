package ch.skyfy.fk.logic.data;

import ch.skyfy.fk.config.core.ConfigData;

public class AllData {

    public static final ConfigData<FKGameData> FK_GAME_DATA = new ConfigData<>("data\\FKGameData.json", FKGameData.class);

}

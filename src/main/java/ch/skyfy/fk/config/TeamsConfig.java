package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.Base;
import ch.skyfy.fk.config.data.FKTeam;
import ch.skyfy.fk.config.data.Square;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TeamsConfig {

    public List<FKTeam> teams;

    public TeamsConfig() {
        teams = new ArrayList<>();
        teams.add(new FKTeam("The_Green_Team", Formatting.GREEN.name(), List.of("Skyfy16"), new Base("The_HADDA_BASE", new Square((short) 9, 20, 60, 20))));
        teams.add(new FKTeam("The_Red_Team", Formatting.RED.name(), List.of("AmibeSkyfy16"), new Base("The_DRIDROU_BASE", new Square((short) 9, -20, 60, -20))));
    }
}

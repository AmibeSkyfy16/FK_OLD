package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.Cube;

import java.util.ArrayList;
import java.util.List;

public class UnbreakableAreaConfig {

    public List<Cube> unbreakableAreas;

    public UnbreakableAreaConfig() {
        unbreakableAreas = new ArrayList<>();
        unbreakableAreas.add(new Cube((short)5, 100, 100, 20, 120, 20));
    }
}

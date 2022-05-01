package ch.skyfy.fk.config;

import ch.skyfy.fk.config.data.Base;
import ch.skyfy.fk.config.data.Square;

import java.util.ArrayList;
import java.util.List;

public class BasesConfig {

    public List<Base> bases;

    public BasesConfig() {
        bases = new ArrayList<>();
        bases.add(new Base("The yellow base", List.of("Skyfy16"), new Square((short) 9, 20, 60, 20)));
        bases.add(new Base("The green base", List.of("Alex"), new Square((short) 9, -20, 60, -20)));
    }
}

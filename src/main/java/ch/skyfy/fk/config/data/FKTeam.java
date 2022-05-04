package ch.skyfy.fk.config.data;

import lombok.Getter;

import java.util.List;

public class FKTeam {

    @Getter
    private final String name;

    @Getter
    private final String color;

    @Getter
    private final List<String> players;

    @Getter
    private final Base base;

    public FKTeam(String name, String color, List<String> team, Base base) {
        this.name = name;
        this.color = color;
        this.players = team;
        this.base = base;
    }
}

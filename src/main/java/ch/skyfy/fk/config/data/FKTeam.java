package ch.skyfy.fk.config.data;

import java.util.List;

public class FKTeam {

    private String name;

    private String color;

    private List<String> players;

    private Base base;

    public FKTeam(String name, String color, List<String> team, Base base) {
        this.name = name;
        this.color = color;
        this.players = team;
        this.base = base;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }
}

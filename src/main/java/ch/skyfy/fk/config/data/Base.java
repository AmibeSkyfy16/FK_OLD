package ch.skyfy.fk.config.data;

import java.util.List;

@SuppressWarnings("unused")
public class Base {

    private String name;

    private List<String> team; // The list of names of the players who own this base

    private Square square;

    public Base() {}

    public Base(String name, List<String> team, Square square) {
        this.name = name;
        this.team = team;
        this.square = square;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTeam() {
        return team;
    }

    public void setTeam(List<String> team) {
        this.team = team;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }
}

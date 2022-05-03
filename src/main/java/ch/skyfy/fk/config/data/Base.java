package ch.skyfy.fk.config.data;

@SuppressWarnings("unused")
public class Base {

    private String name;

    private Cube cube;

    public Base() {}

    public Base(String name, Cube cube) {
        this.name = name;
        this.cube = cube;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cube getSquare() {
        return cube;
    }

    public void setSquare(Cube cube) {
        this.cube = cube;
    }
}

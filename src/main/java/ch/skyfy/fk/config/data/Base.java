package ch.skyfy.fk.config.data;

@SuppressWarnings("unused")
public class Base {

    private String name;

    private Square square;

    public Base() {}

    public Base(String name, Square square) {
        this.name = name;
        this.square = square;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }
}

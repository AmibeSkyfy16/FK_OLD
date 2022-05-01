package ch.skyfy.fk.config.data;

public class Square {

    private short size; // A base is always square

    private int x, y, z; // Represents the center of the base (the center of the square)

    public Square(){}

    public Square(short size, int x, int y, int z) {
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public short getSize() {
        return size;
    }

    public void setSize(short size) {
        this.size = size;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}

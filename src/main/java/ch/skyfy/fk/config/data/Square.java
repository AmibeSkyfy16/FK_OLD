package ch.skyfy.fk.config.data;

public class Square {

    private short size; // A base is always square

    private double x, y, z; // Represents the center of the base (the center of the square)

    public Square(){}

    public Square(short size, double x, double y, double z) {
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}

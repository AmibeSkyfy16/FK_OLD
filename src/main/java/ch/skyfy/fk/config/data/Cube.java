package ch.skyfy.fk.config.data;

public class Cube {

    private short size; // A base is always square

    private int numberOfBlocksDown; // The number of blocks down from the center

    private int numberOfBlocksUp; // The number of blocks up from the center

    private double x, y, z; // Represents the center of the base (the center of the square)

    public Cube(){}

    public Cube(short size, int numberOfBlocksDown, int numberOfBlocksUp, double x, double y, double z) {
        this.size = size;
        this.numberOfBlocksDown = numberOfBlocksDown;
        this.numberOfBlocksUp = numberOfBlocksUp;
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

    public int getNumberOfBlocksDown() {
        return numberOfBlocksDown;
    }

    public void setNumberOfBlocksDown(int numberOfBlocksDown) {
        this.numberOfBlocksDown = numberOfBlocksDown;
    }

    public int getNumberOfBlocksUp() {
        return numberOfBlocksUp;
    }

    public void setNumberOfBlocksUp(int numberOfBlocksUp) {
        this.numberOfBlocksUp = numberOfBlocksUp;
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

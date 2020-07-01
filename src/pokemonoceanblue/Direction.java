package pokemonoceanblue;

public enum Direction {
    LEFT, RIGHT, UP, DOWN;

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
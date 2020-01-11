package pokemonoceanblue;

public enum Direction {
    LEFT, RIGHT, UP, DOWN;

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public Direction getDirection(int d)
    {
        if (d == 0)
        {
            return Direction.UP;
        }
        else if (d == 1)
        {
            return Direction.RIGHT;
        }
        else if (d == 3)
        {
            return Direction.LEFT;
        }
        else
        {
            return Direction.DOWN;
        }
    }
}
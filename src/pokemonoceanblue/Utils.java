package pokemonoceanblue;

import java.sql.Timestamp;
import java.util.Calendar;

public class Utils 
{
    // list of legendary pokemon that can only be caught once and cannot be shiny
    public static int[] LEGENDARY_POKEMON = new int[] {
        144, 145, 146, 150, 151, 243, 244, 245, 249, 250, 251,
        377, 378, 379, 380, 381, 382, 383, 384, 385, 386,
        480, 481, 482, 483, 484, 485, 486, 487, 488, 490, 491, 492, 493
    };

    /**
     * Returns the id of the current time of day
     * @return 0 for day time, 1 for night time
     */
    public static byte getTimeOfDayId()
    {
        int hour =  Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour <= 7 || hour > 19)
        {
            return 1;
        }
        else 
        {
            return 0;
        }
    }

    /**
     * Returns true if the two dates refer to the same day
     */
    public static boolean isDifferentDay(Timestamp c, Timestamp t)
    {
        if (c == null || t == null)
        {
            return true;
        }

        Calendar calCurrent = Calendar.getInstance();
        calCurrent.setTime(c);
        Calendar calCheck = Calendar.getInstance();
        calCheck.setTime(t);

        if (calCurrent.get(Calendar.YEAR) != calCheck.get(Calendar.YEAR)
            || calCurrent.get(Calendar.DAY_OF_YEAR) != calCurrent.get(Calendar.DAY_OF_YEAR))
        {
            return true;
        } 

        return false;
    }

    /**
     * Converts a numerical direction into the Direction enum
     * @param d an int correspond to a direction, ex. 0 means up
     * @return an Direction enum value
     */
    public static Direction getDirection(int d)
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

    public static Direction getDirection(int x, int y)
    {
        if (y < 0)
        {
            return Direction.UP;
        }
        else if (x > 0)
        {
            return Direction.RIGHT;
        }
        else if (x < 0)
        {
            return Direction.LEFT;
        }
        else
        {
            return Direction.DOWN;
        }
    }

    /**
     * Move the x-coordinate one unit in the given direction
     * @param x the initial x-coordinate
     * @param d the direction to move in
     * @return the shifted x-coordinate
     */
    public static int applyXOffset(int x, Direction d)
    {
        if (d == Direction.LEFT)
        {
            return x - 1;
        }
        else if (d == Direction.RIGHT)
        {
            return x + 1;
        }
        else
        {
            return x;
        }
    }

    /**
     * Move the y-coordinate one unit in the given direction
     * @param y the initial y-coordinate
     * @param d the direction to move in
     * @return the shifted y-coordinate
     */
    public static int applyYOffset(int y, Direction d)
    {
        if (d == Direction.UP)
        {
            return y - 1;
        }
        else if (d == Direction.DOWN)
        {
            return y + 1;
        }
        else
        {
            return y;
        }
    }
}
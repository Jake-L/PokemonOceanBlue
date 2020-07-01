package pokemonoceanblue;

import java.util.Calendar;

public class Utils 
{
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
     * Converts a numerical direction into the Direction enum
     * @param d an int correspond to a direction, ex. 0 means down
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
}
package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;

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

    /**
     * Determines the first evolution of the given Pokemon
     * @param pokemon_id the Pokemon to look up
     * @return the first evolution of the Pokemon
     */
    public static int getFirstEvolution(int pokemon_id)
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
            SELECT COALESCE(em2.pre_species_id, em1.pre_species_id, p.pokemon_id)
            FROM pokemon p
            LEFT JOIN evolution_methods em1
            ON p.pokemon_id = em1.evolved_species_id
            LEFT JOIN evolution_methods em2
            ON em1.pre_species_id = em2.evolved_species_id
            WHERE pokemon_id = 
            """ + pokemon_id; 

            ResultSet rs = db.runQuery(query);

            return rs.getInt(1);
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  

        return -1;
    }

    /**
     * Creates a list of exclusive moves
     * Used an input for moveset generation functions
     * and used to verify movesets in unit tests
     * @return a hashtable mapping move_id to an array of pokemon_ids that learn the move
     */
    public static Hashtable<Integer, Integer[]> getExclusiveMoves()
    {
        Hashtable<Integer, Integer[]> exclusiveMoves = new Hashtable<Integer, Integer[]>();

        // grass starters should learn Frenzy Plant
        exclusiveMoves.put(338, new Integer[]{3, 154, 254, 389});

        // fire starters should learn Blast Burn
        exclusiveMoves.put(307, new Integer[]{6, 157, 257, 392});

        // water starters should learn Hydro Cannon
        exclusiveMoves.put(308, new Integer[]{9, 160, 160, 395});

        // Mewtwo should learn Psystrike
        exclusiveMoves.put(540, new Integer[]{150});

        // Hitmontop should learn Triple Kick
        exclusiveMoves.put(167, new Integer[]{237});

        // Miltank should learn Milk Drink
        exclusiveMoves.put(208, new Integer[]{241});

        // Lugia should learn Aeroblast
        exclusiveMoves.put(177, new Integer[]{249});

        // Ho-oh should learn Sacret Fire
        exclusiveMoves.put(221, new Integer[]{250});

        // Makuhita and Hariyama should learn Smelling Salts
        exclusiveMoves.put(265, new Integer[]{296, 297});

        // Blaziken and Hitmonlee should learn Blaze Kick
        exclusiveMoves.put(299, new Integer[]{106, 257});

        // Cacnea family should learn Needle Arm
        exclusiveMoves.put(302, new Integer[]{331, 332});

        // Spheal family should learn Ice Ball
        exclusiveMoves.put(301, new Integer[]{363, 364, 365});

        // Latias should learn Mist Ball
        exclusiveMoves.put(296, new Integer[]{380});

        // Latios should learn Luster Purge
        exclusiveMoves.put(295, new Integer[]{381});

        // Jirachi should learn Doom Desire
        exclusiveMoves.put(353, new Integer[]{385});

        // Deoxys should learn Psycho Boost
        exclusiveMoves.put(354, new Integer[]{386});

        // Rhyperior should learn Rock Wrecker
        exclusiveMoves.put(439, new Integer[]{464});

        return exclusiveMoves;
    }
}
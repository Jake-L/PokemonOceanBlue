package pokemonoceanblue;

import java.sql.*;
import java.util.Calendar;

public class EvolutionCheck 
{
    /** 
     * Constructor
     */
    public EvolutionCheck(){}

    /** 
     * Check if the given Pokemon has met all the conditions for evolving
     * @return the id the pokemon can evolve into, or -1 otherwise
     */
    public int checkEvolution(PokemonModel pokemon, int currentMapId)
    {
        int evolve = -1;
        int preSpeciesId = pokemon.id;

        try
        {
            DatabaseUtility db = new DatabaseUtility();
            int minimumLevel;
            int minimumHappiness;
            int triggerItemId;
            int genderId;
            int mapId;
            String timeOfDay;
            int hour =  Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            // load move data
            String query = "SELECT * FROM evolution_methods "
                            + "WHERE pre_species_id = " + preSpeciesId;

            ResultSet rs = db.runQuery(query);

            while (rs.next())
            {
                evolve = rs.getInt("evolved_species_id");
                minimumLevel = rs.getInt("minimum_level");
                minimumHappiness = rs.getInt("minimum_happiness");
                triggerItemId = rs.getInt("trigger_item_id");
                genderId = rs.getInt("gender_id");
                mapId = rs.getInt("map_id");
                timeOfDay = rs.getString("time_of_day");

                if (pokemon.id != preSpeciesId)
                {
                    evolve = -1;
                }
                else if (pokemon.level < minimumLevel)
                {
                    evolve = -1;
                }
                else if (pokemon.happiness < minimumHappiness)
                {
                    evolve = -1;
                }
                else if (triggerItemId > 0)
                {
                    evolve = -1;
                }
                else if (mapId != currentMapId)
                {
                    evolve = -1;
                }
                else if ((timeOfDay == "day" && (hour <= 7 || hour > 19))
                    || (timeOfDay == "night" && (hour > 7 || hour < 19)))
                {
                    return -1;
                }
                else if (genderId > 0 && pokemon.genderId != genderId)
                {
                    return -1;
                }
                if (evolve != -1)
                {
                    return evolve;
                }
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
            evolve = -1;
        }  
        
        return evolve;
    }
}
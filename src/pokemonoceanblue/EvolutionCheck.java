package pokemonoceanblue;

import java.sql.*;

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
    public int checkEvolution(PokemonModel pokemon, int currentMapId, int itemId)
    {
        int evolve = -1;
        int preSpeciesId = pokemon.pokemon_id;

        try
        {
            DatabaseUtility db = new DatabaseUtility();
            int minimumLevel;
            int minimumHappiness;
            int triggerItemId;
            int genderId;
            int mapId;
            int timeOfDay;

            // load move data
            String query = "SELECT * FROM evolution_methods "
                            + "WHERE pre_species_id = " + preSpeciesId
                            + " ORDER BY trigger_item_id DESC, map_id DESC";

            ResultSet rs = db.runQuery(query);

            while (rs.next())
            {
                evolve = rs.getInt("evolved_species_id");
                minimumLevel = rs.getInt("minimum_level");
                minimumHappiness = rs.getInt("minimum_happiness");
                triggerItemId = rs.getInt("trigger_item_id");
                genderId = rs.getInt("gender_id");
                mapId = rs.getInt("map_id");
                timeOfDay = rs.getInt("time_of_day");

                if (pokemon.pokemon_id != preSpeciesId)
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
                else if (triggerItemId > 0 && itemId != triggerItemId)
                {
                    evolve = -1;
                }
                else if (mapId > -1 && mapId != currentMapId)
                {
                    evolve = -1;
                }
                else if (timeOfDay >= 0 && Utils.getTimeOfDayId() == timeOfDay)
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
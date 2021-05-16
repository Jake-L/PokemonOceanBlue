package pokemonoceanblue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WildPokemonModel {
    private final int mapId;
    private Map<Integer, List<WildPokemonDataModel>> wildPokemon = new HashMap<Integer, List<WildPokemonDataModel>>();

    /**
     * load a list of wild pokemon that can appear on the current map
     * @param mapId
     */
    public WildPokemonModel(int mapId)
    {
        this.mapId = mapId;

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT area_id, tile_id, pokemon_id, rarity, time_of_day FROM pokemon_location WHERE map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            while (rs.next()) 
            {
                int indexId = rs.getInt("area_id") * 1000 + rs.getInt("tile_id");
                if (this.wildPokemon.get(indexId) == null)
                {
                    this.wildPokemon.put(indexId, new ArrayList<WildPokemonDataModel>());
                }
                this.wildPokemon.get(indexId).add(new WildPokemonDataModel(rs.getInt("pokemon_id"), rs.getInt("rarity"), rs.getInt("time_of_day")));
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * Pick a random Pokemon that can appear for the given areaId and tileId
     * @param areaId
     * @param tileId
     * @return
     */
    public int getPokemonId(int areaId, byte tileId)
    {
        int index = areaId * 1000 + tileId;
        int maxCounter = 0;
        int counter = 0;
        Random rand = new Random();

        if (this.wildPokemon.get(index) == null)
        {
            return -1;
        }
        
        // decide the maximum value for the random draw
        for (WildPokemonDataModel data : this.wildPokemon.get(index))
        {
            if (data.timeOfDay == -1 || data.timeOfDay == Utils.getTimeOfDayId())
            {
                maxCounter += data.rarity;
            }
        }

        counter = rand.nextInt(maxCounter);

        // search through the Pokemon to find the one corresponding to the random choice
        for (WildPokemonDataModel data : this.wildPokemon.get(index))
        {
            if (data.timeOfDay == -1 || data.timeOfDay == Utils.getTimeOfDayId())
            {
                counter -= data.rarity;

                if (counter <= 0)
                {
                    return data.pokemonId;
                }
            }
        }

        return -1;
    }
    
    class WildPokemonDataModel {
        public final int pokemonId;
        public final int timeOfDay;
        public final int rarity;

        public WildPokemonDataModel(int pokemonId, int rarity, int timeOfDay)
        {
            this.pokemonId = pokemonId;
            this.timeOfDay = timeOfDay;
            this.rarity = rarity;
        }
    }
}

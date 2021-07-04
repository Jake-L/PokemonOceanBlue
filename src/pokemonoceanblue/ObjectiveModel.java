package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ObjectiveModel {

    public List<ObjectiveTaskModel> tasks = new ArrayList<ObjectiveTaskModel>();
    public final int objectiveId;
    public int objectiveGroupId;
    public int rewardPokemonId;
    public ItemModel reward;
    public String name;
    public String description;    
    public String icon;

    public ObjectiveModel(int objectiveId)
    {
        this.objectiveId = objectiveId;

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM objective WHERE objective_id = " + objectiveId;

            ResultSet rs = db.runQuery(query);

            this.objectiveGroupId = rs.getInt("objective_group_id");
            this.name = rs.getString("name");
            this.description = rs.getString("description");
            this.icon = rs.getString("icon");
            this.rewardPokemonId = rs.getInt("reward_pokemon_id");

            if (rs.getInt("reward_id") > -1)
            {
                this.reward = new ItemModel(rs.getInt("reward_id"), rs.getInt("reward_quantity"));
            }

            query = "SELECT * FROM objective_task WHERE objective_id = " + objectiveId;
            rs = db.runQuery(query);

            while (rs.next())
            {
                tasks.add(new ObjectiveTaskModel(rs));
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    public void setPokemonEvolved(PokemonModel pokemon)
    {
        incrementCounter("evolve", 1);
        
        for (int i = 0; i < pokemon.types.length; i++)
        {
            incrementCounter("evolveType", 1, String.valueOf(pokemon.types[i]));
        }
    }

    public void setPokemonCaught(PokemonModel pokemon)
    {
        incrementCounter("catchCount", 1);
        incrementCounter("catchSpecific", 1, String.valueOf(pokemon.base_pokemon_id));

        if (pokemon.shiny)
        {
            incrementCounter("shiny", 1);
        }
    }

    /**
     * called when a new pokedex entry is made, checks which achievements should progress
     * @param pokemonId is the id of the newly obtained pokemon
     */
    public void setNewPokemonCaught(int pokemonId)
    {
        //checks if pokemon is from kanto, johto, hoenn, or sinnoh national pokedex, excludes later gen and alt forms
        if (pokemonId < 494)
        {
            //increase counter for completing all four national dex
            incrementCounter("pokedex", 1);
            
            //check if pokemon is from kanto, increase kanto dex counter then check if pokemon is legendary
            //if legendary, complete achievement for catching that legendary
            if (pokemonId <= 151)
            {
                incrementCounter("pokedexKanto", 1);
            }
            //check if pokemon is from johto, if so increase johto dex counter
            else if (pokemonId <= 251)
            {
                incrementCounter("pokedexJohto", 1);
            }
            //check if pokemon is from hoenn, if so increase hoenn dex counter
            else if (pokemonId <= 386)
            {
                incrementCounter("pokedexHoenn", 1);
            }
            //increse sinnoh dex counter
            else if (pokemonId <= 493)
            {
                incrementCounter("pokedexSinnoh", 1);
            }
        }
    }

    /** 
     * increment counters for events which have progressed
     * @param objectiveType the type of objective to progress
     * @param increment is the number of proceeding achievements with the same requirements as the first
     */
    public void incrementCounter(String objectiveType, int increment, String identifier)
    {
        for (ObjectiveTaskModel task : this.tasks)
        {
            // note that for general tasks without an identifier,
            // both task.identifier and identifier = ""
            if (task.objectiveType.equals(objectiveType) 
                // simplify rocketGruntBoy to just rocketGrunt
                && (task.identifier.equals(identifier.replace("Boy", "").replace("Girl", ""))
                    || task.identifier.equals("")))
            {
                task.incrementCounter(increment);
            }
        }
    }

    public void incrementCounter(String objectiveType, int increment)
    {
        this.incrementCounter(objectiveType, increment, "");
    }

    public int getCounter()
    {
        int counter = 0;

        for (ObjectiveTaskModel task : this.tasks)
        {
            counter += task.counter;
        }

        return counter;
    }

    public int getRequired()
    {
        int requiredValue = 0;

        for (ObjectiveTaskModel task : this.tasks)
        {
            requiredValue += task.requiredValue;
        }

        return requiredValue;
    }

    class ObjectiveTaskModel
    {
        public int objectiveId;
        public int taskId;
        public String objectiveType;
        public String identifier;
        public int counter;
        public int requiredValue;
        public String description;
        
        public ObjectiveTaskModel(ResultSet rs) throws SQLException
        {
            this.objectiveId = rs.getInt("objective_id");
            this.taskId = rs.getInt("task_id");
            this.objectiveType = rs.getString("objective_type");
            this.counter = rs.getInt("counter");
            this.requiredValue = rs.getInt("required_value");
            this.identifier = rs.getString("identifier");
            this.description = rs.getString("description");
        }

        public void incrementCounter(int increment)
        {
            this.counter += increment;
        }
    }
}
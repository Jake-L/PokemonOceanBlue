package pokemonoceanblue;

import java.sql.*;

public class PokedexModel extends BaseModel
{
    // TODO: add tasks for defeating Pokemon, catching them 20 times, etc
    public int[] caughtPokemon = new int[506];
    public String[] pokemonDescription = new String[506];
    public int uniqueCaught = 0;

    public PokedexModel()
    {
        this.optionMax = this.caughtPokemon.length - 1;
        this.loadPokedex();
        this.loadPokemonData();
    }

    /**
     * Set variables when creating a new view
     */
    @Override
    public void initialize()
    {
        this.actionCounter = ACTION_DELAY;
        this.optionIndex = 1;
        this.returnValue = -2;
        this.acceleration = 0;
        this.accelerationCounter = 10;
        this.optionMin = 1;
    }

    /**
     * Increment the number of times a Pokemon has been caught
     * @param pokemonId the identifier of the caught pokemon
     * @return true if it's the first time the Pokemon was caught
     */
    public boolean setCaught(int pokemonId)
    {
        this.caughtPokemon[pokemonId]++;

        if (this.caughtPokemon[pokemonId] == 1)
        {
            this.uniqueCaught++;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Loads the descriptions of each Pokemon
     */
    private void loadPokemonData()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT pokemon_id, [description] FROM pokemon WHERE pokemon_id <= 505 ";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.pokemonDescription[rs.getInt("pokemon_id")] = rs.getString("description");
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * Loads saved Pokedex data
     */
    private void loadPokedex()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT pokemon_id, number_caught FROM player_pokedex";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.caughtPokemon[rs.getInt("pokemon_id")] = rs.getInt("number_caught");
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * get the shiny rate for a specific Pokemon
     * @param pokemonId the Pokemon's identifier
     * @return the probability that the Pokemon is shiny, in range (0,1)
     */
    public double getShinyRate(int pokemonId)
    {
        return (Math.log10(this.caughtPokemon[pokemonId] + 1) + 1) / 2000;
    }
}
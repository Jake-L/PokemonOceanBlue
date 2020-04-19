package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PokemonStorageModel extends BaseModel
{
    public PokemonModel currentPokemon;
    public int categoryIndex = 0;

    public List<PokemonModel> pokemonStorage = new ArrayList<PokemonModel>();

    public PokemonStorageModel()
    {
        this.loadPlayerStorage();
    }

    /**
     * Read the Pokemon in storage from the database
     */
    private void loadPlayerStorage()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM player_pokemon "
                        + " WHERE pokemon_index >= 6 " 
                        + " ORDER BY pokemon_index ASC";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.pokemonStorage.add(new PokemonModel(rs));
            }          
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    @Override
    public void exitScreen()
    {
        // only exit the screen when not hovering a Pokemon
        if (this.currentPokemon == null)
        {
            this.returnValue = -1;
        }
    }

    @Override
    public void confirmSelection()
    {
        if (this.categoryIndex == 1)
        {
            // pick up the currently hovered Pokemon
            if (this.currentPokemon == null && this.textOptions == null)
            {
                // don't show the text options if hovering the empty space at the end of the list
                if (this.optionIndex < this.pokemonStorage.size())
                {
                    this.textOptions = new String[]{"MOVE", "SUMMARY", "CANCEL"};
                }
            }
            else if (this.textOptions != null && this.textOptionIndex == 0)
            {
                this.currentPokemon = this.pokemonStorage.remove(this.optionIndex);
                this.textOptions = null;
            }
            // view the Pokemon's summary
            else if (this.textOptionIndex == 1)
            {
                this.textOptions = null;
            }
            // exit the text options
            else if (this.textOptionIndex == 2)
            {
                this.textOptions = null;
            }
            else if (this.optionIndex < this.pokemonStorage.size())
            {
                // swap the Pokemon you're hovering with the one you were holding
                PokemonModel newPokemon = this.pokemonStorage.remove(this.optionIndex);
                this.addPokemon(this.optionIndex, this.currentPokemon);
                this.currentPokemon = newPokemon;
            }
            else
            {
                // drop the Pokemon into an open space
                this.addPokemon(this.optionIndex, this.currentPokemon);
                this.currentPokemon = null;
            }

            // redetermine the maximum index
            this.optionMax = this.pokemonStorage.size();
            this.textOptionIndex = 0;
        }
    }
    
    /**
     * Adds the given Pokemon to the end of the pokemon storage list
     * @param pokemon
     */
    public void addPokemon(PokemonModel pokemon)
    {
        this.pokemonStorage.add(pokemon);
    }

    /**
     * Adds a Pokemon at the specified index in the pokemon storage list
     * @param index
     * @param pokemon
     */
    public void addPokemon(int index, PokemonModel pokemon)
    {
        if (index < this.pokemonStorage.size())
        {
            this.pokemonStorage.add(index, pokemon);
        }
        else
        {
            this.pokemonStorage.add(pokemon);
        }  
    }
}

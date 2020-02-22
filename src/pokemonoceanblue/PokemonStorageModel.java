package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;

public class PokemonStorageModel extends BaseModel
{
    public PokemonModel currentPokemon;
    public int categoryIndex = 0;

    public List<PokemonModel> pokemonStorage = new ArrayList<PokemonModel>();

    public PokemonStorageModel(){}

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
            if (this.currentPokemon == null)
            {
                this.currentPokemon = this.pokemonStorage.remove(this.optionIndex);
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
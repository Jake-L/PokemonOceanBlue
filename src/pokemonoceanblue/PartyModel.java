package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;

public class PartyModel 
{
    public PokemonModel[] team;
    public int optionIndex;
    public final int INPUTDELAY = 6;
    public int counter;
    public int currentPokemon;
    public boolean isBattle;
    
    public int returnValue;
    public List<PokemonModel> pokemonStorage = new ArrayList<PokemonModel>();

    public PartyModel(PokemonModel[] model)
    {
        this.team = model;
    }

    /**
     * Set variables when creating a new view
     * @param currentPokemon the Pokemon currently selected, or -1 if not in a battle
     */
    public void initialize(int currentPokemon)
    {
        this.currentPokemon = currentPokemon;
        this.counter = this.INPUTDELAY;
        this.optionIndex = 0;
        this.returnValue = -2;
        
        if (this.currentPokemon == -1)
        {
            this.isBattle = false;
        }

        else
        {
            this.isBattle = true;
        }
    }

    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }
    }

    public void confirmSelection()
    {
        this.returnValue = this.optionIndex;
    }

    public int getSelection()
    {
        return this.returnValue;
    }
}
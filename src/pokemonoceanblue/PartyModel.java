package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;

public class PartyModel 
{
    public PokemonModel[] team;
    public int optionIndex = 0;
    public final int INPUTDELAY = 6;
    public int counter;
    public int currentPokemon;
    public boolean isBattle;
    public boolean isSummary = false;
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
        if (this.isBattle)
        {
            this.returnValue = this.optionIndex;
        }
        else
        {
            this.isSummary = true;
        }
    }

    public int getSelection()
    {
        return this.returnValue;
    }

    /**
     * Fully heals all the Pokemon in the player's team
     */
    public void healTeam()
    {
        for (PokemonModel pokemon : this.team)
        {
            pokemon.currentHP = pokemon.stats[0];
        }
    }
}
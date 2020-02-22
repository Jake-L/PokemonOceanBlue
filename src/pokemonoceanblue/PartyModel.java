package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;


public class PartyModel extends BaseModel
{
    public List<PokemonModel> team = new ArrayList<PokemonModel>();
    public boolean isSummary = false;
    public int currentPokemon;

    public PartyModel(List<PokemonModel> team)
    {
        this.team = team;
        this.optionIndex = -1;
    }

    /**
     * Set variables when creating a new view
     * @param currentPokemon the Pokemon currently selected, or -1 if not in a battle
     */
    public void initialize(int currentPokemon)
    {
        this.actionCounter = ACTION_DELAY;
        this.returnValue = -2;

        // track which Pokemon is active in battle
        this.currentPokemon = currentPokemon;

        // set the initial position as the current Pokemon in battle or the first Pokemon
        if (this.optionIndex == -1)
        {
            this.optionIndex = Math.max(currentPokemon, 0);
        }

        this.optionMax = this.team.size() - 1;

        // when looking at summaries, move through one dimension
        if (this.isSummary)
        {
            this.optionWidth = 1;
            this.optionHeight = this.optionMax;
        }
        else
        {
            this.optionWidth = 2;
            this.optionHeight = 3;
        }
    }

    @Override
    public void confirmSelection()
    {
        // if not in battles, open summary screen
        if (this.currentPokemon == -1)
        {
            this.isSummary = !this.isSummary;
            if (this.isSummary)
            {
                this.returnValue = this.optionIndex;
            }
        }
        // if in battle, return the chosen Pokemon
        else if (this.currentPokemon != this.optionIndex)
        {
            this.returnValue = this.optionIndex;
            this.optionIndex = -1;
        }
        // don't substitute a Pokemon for itself
        else 
        {
            this.exitScreen();
        }
    }

    public PokemonModel[] getTeamArray()
    {
        PokemonModel[] teamArray = new PokemonModel[this.team.size()];
        for (int i = 0; i < this.team.size(); i++)
        {
            teamArray[i] = this.team.get(i);
        }
        return teamArray;
    }

    @Override
    public void exitScreen()
    {
        if (this.isSummary)
        {
            this.isSummary = false;
        }
        else
        {
            this.returnValue = -1;
            this.optionIndex = -1;
        }
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

    public void addPokemon(int index, PokemonModel pokemon)
    {
        if (this.team.size() < 6)
        {
            if (index < this.team.size())
            {
                this.team.add(index, pokemon);
            }
            else
            {
                this.team.add(pokemon);
            }
        }
        
    }
}
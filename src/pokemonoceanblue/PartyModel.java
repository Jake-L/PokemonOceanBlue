package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;


public class PartyModel extends BaseModel
{
    public List<PokemonModel> team = new ArrayList<PokemonModel>();
    public boolean isSummary = false;
    public int battleActivePokemon;
    public int switchPokemonIndex = -1;
    public boolean updateOrder = false;

    public PartyModel(List<PokemonModel> team)
    {
        this.team = team;
        this.optionIndex = -1;
    }

    /**
     * Set variables when creating a new view
     * @param battleActivePokemon the Pokemon currently selected, or -1 if not in a battle
     */
    public void initialize(int battleActivePokemon)
    {
        this.actionCounter = ACTION_DELAY;
        this.returnValue = -2;

        // track which Pokemon is active in battle
        this.battleActivePokemon = battleActivePokemon;

        // set the initial position as the current Pokemon in battle or the first Pokemon
        if (this.optionIndex == -1)
        {
            this.optionIndex = Math.max(battleActivePokemon, 0);
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

        this.textOptions = null;
        this.textOptionIndex = 0;
        this.switchPokemonIndex = -1;
    }

    @Override
    public void confirmSelection()
    {
        // if not in battles, open summary screen
        if (this.battleActivePokemon == -1)
        {
            // exit summary screen
            if (this.isSummary)
            {
                this.isSummary = !this.isSummary;
            }
            // swap the current Pokemon with the previously selected Pokemon
            else if (this.switchPokemonIndex > -1)
            {
                int firstIndex = this.switchPokemonIndex < this.optionIndex ? this.switchPokemonIndex : this.optionIndex;
                int secondIndex = this.switchPokemonIndex > this.optionIndex ? this.switchPokemonIndex : this.optionIndex;
                this.team.add(secondIndex, this.team.get(firstIndex));
                this.team.add(firstIndex, this.team.get(secondIndex + 1));
                this.team.remove(firstIndex + 1);
                this.team.remove(secondIndex + 1);
                this.switchPokemonIndex = -1;
                this.updateOrder = true;
            }
            // show pop-up box of options
            else if (this.textOptions == null)
            {
                this.textOptions = new String[] {"SUMMARY", "SWITCH", "CANCEL"};
            }
            // open summary screen
            else if (this.textOptionIndex == 0)
            {
                this.isSummary = !this.isSummary;
                this.returnValue = this.optionIndex;
            }
            // select a Pokemon to be swapped
            else if (this.textOptionIndex == 1)
            {
                this.switchPokemonIndex = this.optionIndex;
                this.textOptions = null;
            }
            // close the pop-up window
            else if (this.textOptionIndex == 2)
            {
                this.textOptions = null;
            }

            this.textOptionIndex = 0;
        }
        // if in battle, return the chosen Pokemon
        else if (this.battleActivePokemon != this.optionIndex && this.team.get(this.optionIndex).level > 0)
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
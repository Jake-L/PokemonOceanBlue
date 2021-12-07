package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PartyModel extends BaseModel
{
    public List<PokemonModel> team = new ArrayList<PokemonModel>();
    public boolean isSummary = false;
    public boolean returnSelection = false;
    public int battleActivePokemon;
    public int switchPokemonIndex = -1;
    public boolean updateOrder;
    public boolean openMap;
    public int itemId;

    public PartyModel()
    {
        this.loadPlayerTeam();
        this.optionIndex = -1;
    }

    /**
     * Set variables when creating a new view
     * @param battleActivePokemon the Pokemon currently selected, or -1 if not in a battle
     * @param returnSelection true if the screen was opened for the player to select a Pokemon 
     */
    public void initialize(int battleActivePokemon, boolean returnSelection)
    {
        this.actionCounter = ACTION_DELAY;
        this.returnValue = -2;
        this.returnSelection = returnSelection;

        // track which Pokemon is active in battle
        this.battleActivePokemon = battleActivePokemon;

        // set the initial position as the current Pokemon in battle or the first Pokemon
        if (this.optionIndex == -1)
        {
            this.optionIndex = Math.max(battleActivePokemon, 0);
        }

        this.optionMax = this.team.size() - 1;

        this.optionWidth = 2;
        this.optionHeight = 3;

        this.textOptions = null;
        this.textOptionIndex = 0;
        this.switchPokemonIndex = -1;
        this.openMap = false;
        this.updateOrder = false;
    }

    /**
     * Read the player's team from the database
     */
    private void loadPlayerTeam()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM player_pokemon "
                        + " WHERE pokemon_index < 6 " 
                        + " ORDER BY pokemon_index ASC";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.team.add(new PokemonModel(rs));
            }          
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    @Override
    public void confirmSelection()
    {
        // exit the Pokemon's summary
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
            if (returnSelection)
            {
                this.textOptions = new String[] {"SELECT", "SUMMARY", "CANCEL"};
            }
            // use fly with a dragon or flying type
            else if (Type.typeIncludes(Type.FLYING, this.team.get(this.optionIndex).types)
                || Type.typeIncludes(Type.DRAGON, this.team.get(this.optionIndex).types))
            {
                this.textOptions = new String[] {"SUMMARY", "FLY", "SWITCH", "CANCEL"};
            }
            else
            {
                this.textOptions = new String[] {"SUMMARY", "SWITCH", "CANCEL"};
            }
        }
        // open summary screen
        else if (this.textOptions[this.textOptionIndex].equals("SUMMARY"))
        {
            this.isSummary = !this.isSummary;
            this.returnValue = this.optionIndex;
        }
        // select a Pokemon to be swapped
        else if (this.textOptions[this.textOptionIndex].equals("SWITCH"))
        {
            this.switchPokemonIndex = this.optionIndex;
            this.textOptions = null;
        }
        // open the map to select a location to fly
        else if (this.textOptions[this.textOptionIndex].equals("FLY"))
        {
            this.openMap = true;
            this.textOptions = null;
        }
        // close the pop-up window
        else if (this.textOptions[this.textOptionIndex].equals("CANCEL"))
        {
            this.textOptions = null;
        }
        else if (this.textOptions[this.textOptionIndex].equals("SELECT"))
        {
            // if not in battle, return the chosen Pokemon
            if (this.battleActivePokemon == -1)
            {
                this.returnValue = this.optionIndex;
                this.optionIndex = -1; 
            }
            // if in battle, and trying to select a dead Pokemon or egg, close the pop-up window and do not exit the party screen
            else if (this.battleActivePokemon > -1 
                && this.team.get(this.battleActivePokemon).currentHP == 0
                && (this.team.get(this.optionIndex).level == 0 || this.team.get(this.optionIndex).currentHP == 0))
            {
                this.textOptions = null;
            }
            // if in battle, return the chosen Pokemon if it isn't the currently active Pokemon
            else if (this.battleActivePokemon != this.optionIndex && this.team.get(this.optionIndex).level > 0 &&
                this.team.get(this.optionIndex).currentHP > 0)
            {
                this.returnValue = this.optionIndex;
                this.optionIndex = -1;
            }
            // if trying to select the Pokemon currently in battle, exit the party screen
            else 
            {
                this.exitScreen();
            }
        }

        this.textOptionIndex = 0;
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
        // do not exit the screen if in battle and your current Pokemon is dead
        else if (this.battleActivePokemon == -1 
            || this.team.get(this.battleActivePokemon).currentHP > 0)
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

            this.optionMax = this.team.size() - 1;
        }
    }

    public PokemonModel removePokemon(int index)
    {
        PokemonModel returnPokemon = this.team.remove(this.optionIndex);
        this.optionMax = this.team.size() - 1;
        return returnPokemon;
    }

    /**
     * @return true if all the player's Pokemon have fainted
     */
    public boolean isDefeated()
    {
        for (PokemonModel pokemon : this.team)
        {
            if (pokemon.currentHP > 0)
            {
                return false;
            }
        }

        return true;
    }

    public void setItem(int itemId)
    {
        this.itemId = itemId;
    }
}

package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PartyModel extends BaseModel
{
    public List<PokemonModel> team = new ArrayList<PokemonModel>();
    public boolean isSummary = false;
    public int battleActivePokemon;
    public int switchPokemonIndex = -1;
    public boolean updateOrder = false;
    public int hoverMoveIndex = -1;
    public MoveModel newMove;

    public PartyModel()
    {
        this.loadPlayerTeam();
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
        this.hoverMoveIndex = -1;
        this.newMove = null;
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

    /**
     * @param newMove the newMove to set
     */
    public void setNewMove(MoveModel newMove) 
    {
        this.newMove = newMove;
        this.hoverMoveIndex = 0;
        this.isSummary = true;
    }

    @Override
    public void moveIndex(int dx, int dy)
    {
        if (this.isSummary && this.hoverMoveIndex > -1)
        {
            // move through the list of moves to view their descriptions
            if ((dx > 0 || dy > 0) && this.hoverMoveIndex < this.team.get(this.optionIndex).moves.length)
            {
                this.hoverMoveIndex++;
            } 
            else if ((dx < 0 || dy < 0) && this.hoverMoveIndex > 0)
            {
                this.hoverMoveIndex--;
            } 
        }
        else if (this.newMove == null)
        {
            super.moveIndex(dx, dy);
        }   
    }

    @Override
    public void confirmSelection()
    {
        // replace one of the Pokemon's moves with the new move
        if (this.newMove != null)
        {
            // if player selects new move, don't learn it
            if (this.hoverMoveIndex == 4)
            {
                this.returnValue = -1;
            }
            else
            {
                this.returnValue = this.hoverMoveIndex;
            }
        }
        // look at the Pokemon's summary
        else if (this.isSummary)
        {
            this.isSummary = !this.isSummary;
        }
        // if not in battles, open summary screen
        else if (this.battleActivePokemon == -1)
        {
            // swap the current Pokemon with the previously selected Pokemon
            if (this.switchPokemonIndex > -1)
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
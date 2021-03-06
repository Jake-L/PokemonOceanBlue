package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class PokemonStorageController extends BaseController
{
    private PartyModel partyModel;
    private PokemonStorageModel pokemonStorageModel;

    /**
     * Constructor
     * @param pokemonStorageModel 
     * @param partyModel
     */
    public PokemonStorageController(PokemonStorageModel pokemonStorageModel, PartyModel partyModel)
    {
        super(pokemonStorageModel);
        this.partyModel = partyModel;
        this.partyModel.initialize(-1, false);
        this.partyModel.optionWidth = 1;
        this.partyModel.optionHeight = this.partyModel.optionMax;
        this.pokemonStorageModel = pokemonStorageModel;
        this.pokemonStorageModel.optionMax = this.pokemonStorageModel.pokemonStorage.size();
    }

    /** 
     * Reads user input and moves the player accordingly
     * @param keysDown currently pressed keys
     */
    @Override
    public void userInput(List<Integer> keysDown)
    {
        if (keysDown.size() > 0 && this.model.actionCounter == 0)
        {
            // open or close the menu
            if (keysDown.contains(KeyEvent.VK_ESCAPE))
            {
                this.pokemonStorageModel.exitScreen();
            }
            // if the player is hovering their party
            else if (this.pokemonStorageModel.categoryIndex == 0)
            {
                if (keysDown.contains(KeyEvent.VK_ENTER))
                {
                    // pick up the currently hovered Pokemon
                    if (this.pokemonStorageModel.currentPokemon == null 
                        && this.pokemonStorageModel.textOptions == null)
                    {
                        // don't show the text options if hovering the empty space at the end of the list
                        if (this.partyModel.optionIndex < this.partyModel.team.size())
                        {
                            this.pokemonStorageModel.textOptions = new String[]{"MOVE", "SUMMARY", "CANCEL"}; 
                        }
                    }
                    else if (this.pokemonStorageModel.textOptions != null 
                        && this.pokemonStorageModel.textOptionIndex == 0)
                    {
                        // don't withdraw the last Pokemon in their party
                        if (this.partyModel.team.size() > 1)
                        {
                            this.pokemonStorageModel.currentPokemon = this.partyModel.removePokemon(this.partyModel.optionIndex);
                        }
                        this.pokemonStorageModel.textOptions = null;
                    }
                    // view the Pokemon's summary
                    else if (this.pokemonStorageModel.textOptionIndex == 1)
                    {
                        this.pokemonStorageModel.returnValue = this.partyModel.optionIndex;
                        this.pokemonStorageModel.textOptions = null;
                    }
                    // exit the text options
                    else if (this.pokemonStorageModel.textOptionIndex == 2)
                    {
                        this.pokemonStorageModel.textOptions = null;
                    }
                    else if (this.partyModel.optionIndex < this.partyModel.team.size())
                    {
                        // swap the Pokemon you're hovering with the one you were holding
                        PokemonModel newPokemon = this.partyModel.removePokemon(this.partyModel.optionIndex);
                        this.partyModel.addPokemon(this.partyModel.optionIndex, this.pokemonStorageModel.currentPokemon);
                        this.pokemonStorageModel.currentPokemon = newPokemon;
                    }
                    else
                    {
                        // drop the Pokemon into an open space
                        this.partyModel.addPokemon(this.partyModel.optionIndex, this.pokemonStorageModel.currentPokemon);
                        this.pokemonStorageModel.currentPokemon = null;
                    }
                }

                // move from the party selection to the storage selection
                else if (keysDown.contains(KeyEvent.VK_RIGHT) && this.pokemonStorageModel.textOptions == null)
                {
                    this.pokemonStorageModel.categoryIndex++;
                    // find the appropriate spot to move the cursor
                    this.pokemonStorageModel.optionIndex = Math.min(
                        this.pokemonStorageModel.optionMax 
                            / this.pokemonStorageModel.optionWidth, 
                        this.partyModel.optionIndex)
                        * this.pokemonStorageModel.optionWidth;
                }

                else if (keysDown.contains(KeyEvent.VK_UP))
                {
                    if (this.pokemonStorageModel.textOptions == null)
                    {
                        this.partyModel.moveIndex(0, -1);
                    }
                    else
                    {
                        this.pokemonStorageModel.moveIndex(0, -1);
                    }
                }

                else if (keysDown.contains(KeyEvent.VK_DOWN))
                {
                    if (this.pokemonStorageModel.textOptions == null)
                    {
                        if (this.pokemonStorageModel.currentPokemon != null && this.partyModel.optionIndex == this.partyModel.optionMax)
                        {
                            this.partyModel.optionIndex++;
                        }
                        else
                        {
                            this.partyModel.moveIndex(0, 1);
                        }
                    }
                    else
                    {
                        this.pokemonStorageModel.moveIndex(0, 1);
                    }
                }

                this.model.update();
            }
            // if the player is hovering the Pokemon storage
            else
            {
                // move from the storage selection to the party selection
                if (keysDown.contains(KeyEvent.VK_LEFT) 
                    && this.pokemonStorageModel.optionIndex % this.pokemonStorageModel.optionWidth == 0)
                {
                    this.pokemonStorageModel.categoryIndex--;
                    // find the appropriate spot to move the cursor
                    this.partyModel.optionIndex = Math.min(
                        this.pokemonStorageModel.optionIndex 
                            / this.pokemonStorageModel.optionWidth, 
                        this.partyModel.optionMax);
                }
                else
                {
                    super.userInput(keysDown);
                }
            }

            this.model.actionCounter = this.model.ACTION_DELAY;
        }
        else
        {
            this.model.update();
        }
    }
}
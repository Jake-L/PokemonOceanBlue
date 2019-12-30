package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class NewPokemonController 
{
    private NewPokemonModel model;

    /**
     * Constructor
     * @param model 
     */
    public NewPokemonController(NewPokemonModel model)
    {
        this.model = model;
    }

    /**
     * Handle user input
     * @param keysDown keys currently pressed
     */
    public void userInput(List<Integer> keysDown)
    {
        if (this.model.counter > 0)
        {
            this.model.counter--;
        }

        if (this.model.counter == 0 && keysDown.contains(KeyEvent.VK_ENTER))
        {
            this.model.confirmSelection();
        }
    }

    /**
     * @return true if this screen is finished
     */
    public boolean isComplete()
    {
        return this.model.complete;
    }
}
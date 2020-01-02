package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class PokedexController 
{
    private PokedexModel model;
    private boolean complete = false;

    public PokedexController(PokedexModel model)
    {
        this.model = model;
    }

    public void userInput(List<Integer> keysDown)
    {
        if (keysDown.size() > 0)
        {
            if (this.model.counter == 0)
            {
                if (keysDown.contains(KeyEvent.VK_ESCAPE))
                {
                    this.complete = true;
                }
    
                else if (keysDown.contains(KeyEvent.VK_LEFT))
                {
                    model.moveCursor(-1, 0);
                }
    
                else if (keysDown.contains(KeyEvent.VK_RIGHT))
                {
                    model.moveCursor(1, 0);
                }

                else if (keysDown.contains(KeyEvent.VK_UP))
                {
                    model.moveCursor(0, -1);
                }
    
                else if (keysDown.contains(KeyEvent.VK_DOWN))
                {
                    model.moveCursor(0, 1);
                }

                this.model.counter = this.model.INPUTDELAY - this.model.acceleration;
            }
        }
        else
        {
            // clear acceleration when player releases keys
            this.model.acceleration = 0;
            this.model.accelerationCounter = 0;
        }
    }

    public boolean isComplete()
    {
        return complete;
    }
}
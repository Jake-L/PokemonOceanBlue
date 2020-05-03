package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class BaseController 
{
    protected BaseModel model;

    /**
     * Constructor
     * @param model any BaseModel object or subclass of BaseModel
     */
    public BaseController(BaseModel model)
    {
        this.model = model;
    }

    /**
     * Handle user input
     * @param keysDown keys currently pressed
     */
    public void userInput(List<Integer> keysDown)
    {
        if (this.model.actionCounter == 0)
        {
            if (keysDown.size() > 0)
            {
                this.model.actionCounter = this.model.ACTION_DELAY - Math.max(this.model.acceleration, 0);

                if (keysDown.contains(KeyEvent.VK_ESCAPE))
                {
                    this.model.exitScreen();
                }

                else if (keysDown.contains(KeyEvent.VK_ENTER))
                {
                    this.model.confirmSelection();
                }

                else if (keysDown.contains(KeyEvent.VK_LEFT))
                {
                    this.model.moveIndex(-1, 0);
                }

                else if (keysDown.contains(KeyEvent.VK_RIGHT))
                {
                    this.model.moveIndex(1, 0);
                }

                else if (keysDown.contains(KeyEvent.VK_UP))
                {
                    this.model.moveIndex(0, -1);
                }

                else if (keysDown.contains(KeyEvent.VK_DOWN))
                {
                    this.model.moveIndex(0, 1);
                }
            }
            else if (this.model.acceleration > 0)
            {
                // clear acceleration when player releases keys
                this.model.acceleration = 0;
                this.model.accelerationCounter = 0;
            }
        }

        this.model.update();
    }

    public boolean isComplete()
    {
        return this.model.getSelection() >= -1;
    }
}
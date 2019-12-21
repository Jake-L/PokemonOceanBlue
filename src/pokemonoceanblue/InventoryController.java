package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class InventoryController {
    private InventoryModel model;

    /** 
     * Constructor for the InventoryController class
     * @param model the model of the Inventory to be managed by the controller
     */
    public InventoryController(InventoryModel model){
        this.model = model;
    }

    /** 
     * Reads user input and moves the cursor accordingly
     * @param keysDown currently pressed keys
     */
    public void userInput(List<Integer> keysDown)
    {
        if (keysDown.contains(KeyEvent.VK_ESCAPE))
        {
            model.returnValue = -1;
        }
        else if (model.counter == 0)
        {
            if (keysDown.contains(KeyEvent.VK_UP))
            {
                model.moveCursor(-1);
            }
            else if (keysDown.contains(KeyEvent.VK_DOWN))
            {
                model.moveCursor(1);
            }
            else if (keysDown.contains(KeyEvent.VK_LEFT))
            {
                model.movePocket(2);
            }
            else if (keysDown.contains(KeyEvent.VK_RIGHT))
            {
                model.movePocket(1);
            }
        }
    }
}
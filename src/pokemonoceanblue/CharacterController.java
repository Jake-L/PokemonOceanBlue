package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class CharacterController {
    private CharacterModel model;

    /** 
     * Constructor for the CharacterController class
     * @param model the model of the character to be managed by the controller
     */
    public CharacterController(CharacterModel model){
        this.model = model;
    }

    /** 
     * Reads user input and moves the player accordingly
     * @param keysDown currently pressed keys
     */
    public void userInput(List<Integer> keysDown)
    {
        if (keysDown.contains(KeyEvent.VK_UP))
        {
            model.setMovement(0, -1, 16);
        }
        else if (keysDown.contains(KeyEvent.VK_DOWN))
        {
            model.setMovement(0, 1, 16);
        }
        else if (keysDown.contains(KeyEvent.VK_LEFT))
        {
            model.setMovement(-1, 0, 16);
        }
        else if (keysDown.contains(KeyEvent.VK_RIGHT))
        {
            model.setMovement(1, 0, 16);
        }
    }
}
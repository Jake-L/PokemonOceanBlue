package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class BattleController {
    private BattleModel model;

    /** 
     * Constructor for the CharacterController class
     * @param model the model of the character to be managed by the controller
     */
    public BattleController(BattleModel model){
        this.model = model;
    }

        /** 
     * Reads user input and moves between and selects battle options accordingly
     * @param keysDown currently pressed keys
     */
    public void userInput(List<Integer> keysDown)
    {
        if (model.counter == 0)
        {
            if (keysDown.contains(KeyEvent.VK_ENTER))
            {
                model.confirmSelection();
            }

            else if (keysDown.contains(KeyEvent.VK_UP) && (model.optionIndex == 2 || model.optionIndex == 3))
            {
                model.optionIndex -= 2;
            }

            else if (keysDown.contains(KeyEvent.VK_DOWN) && (model.optionIndex == 0 || model.optionIndex == 1))
            {
                if (model.optionIndex + 2 < model.battleOptions.length)
                {
                    model.optionIndex += 2;
                }

                else if (model.battleOptions.length == 3 && model.optionIndex == 1)
                {
                    model.optionIndex = model.battleOptions.length - 1;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_LEFT) && (model.optionIndex == 1 || model.optionIndex == 3))
            {
                model.optionIndex -= 1;
            }

            else if (keysDown.contains(KeyEvent.VK_RIGHT) && (model.optionIndex == 0 || model.optionIndex == 2))
            {
                if (model.optionIndex + 1 < model.battleOptions.length)
                {
                    model.optionIndex += 1;
                }
            }
        }
    }
}
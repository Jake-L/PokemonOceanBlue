package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

public class OverworldController extends BaseController {
    private OverworldModel overworldModel;
    private CharacterModel playerModel;

    /** 
     * Constructor for the CharacterController class
     * @param model the model of the character to be managed by the controller
     */
    public OverworldController(OverworldModel model){
        super(model);
        this.overworldModel = model;
        this.playerModel = model.playerModel;
    }

    /** 
     * Reads user input and moves the player accordingly
     * @param keysDown currently pressed keys
     */
    @Override
    public void userInput(List<Integer> keysDown)
    {
        this.playerModel.update(true);

        if (this.model.actionCounter == 0)
        {
            if (keysDown.size() > 0)
            {
                // open or close the menu
                // move around in a menu if its open
                if (keysDown.contains(KeyEvent.VK_ESCAPE)
                    || this.model.textOptions != null 
                    || this.overworldModel.itemOptions.size() > 0)
                {
                    super.userInput(keysDown);
                }
                // otherwise move player around on map
                else
                {
                    int movespeed = 1;
                    if (keysDown.contains(KeyEvent.VK_SHIFT))
                    {
                        movespeed = 2;
                    }
                    if (keysDown.contains(KeyEvent.VK_ENTER))
                    {
                        this.callCheckAction();
                    }
                    else if (keysDown.contains(KeyEvent.VK_UP))
                    {
                        this.playerModel.setMovement(0, -1, movespeed);
                    }
                    else if (keysDown.contains(KeyEvent.VK_DOWN))
                    {
                        this.playerModel.setMovement(0, 1, movespeed);
                    }
                    else if (keysDown.contains(KeyEvent.VK_LEFT))
                    {
                        this.playerModel.setMovement(-1, 0, movespeed);
                    }
                    else if (keysDown.contains(KeyEvent.VK_RIGHT))
                    {
                        this.playerModel.setMovement(1, 0, movespeed);
                    }
                    this.model.update();
                }
            }
            else if (this.model.acceleration > 0)
            {
                // clear acceleration when player releases keys
                this.model.acceleration = 0;
                this.model.accelerationCounter = 0;
                this.model.update();
            }
            else
            {
                this.model.update();
            }
        }
        else
        {
            this.model.update();
        }
    }

    /** 
     * Check which position the player is trying to interact with
     * Pass the check position to the overworld model
     */
    private void callCheckAction()
    {
        int checkX = this.playerModel.getX();
        int checkY = this.playerModel.getY();

        // get the coordinates the player is interacting with
        if (this.playerModel.getDirection() == Direction.RIGHT)
        {
            checkX++;
        }
        else if (this.playerModel.getDirection() == Direction.LEFT)
        {
            checkX--;
        }
        else if (this.playerModel.getDirection() == Direction.UP)
        {
            checkY--;
        }
        else if (this.playerModel.getDirection() == Direction.DOWN)
        {
            checkY++;
        }

        // check if there is anything to interact with
        this.overworldModel.checkAction(checkX, checkY);
    }
}
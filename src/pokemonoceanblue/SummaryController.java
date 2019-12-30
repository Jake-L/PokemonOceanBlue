package pokemonoceanblue;

import java.awt.event.KeyEvent;
import java.util.List;

/** 
 * Renders the summary screen
 */
public class SummaryController{

    private PartyModel model;

    public SummaryController(PartyModel model)
    {
        this.model = model;
    }

    public void userInput(List<Integer> keysDown)
    {
        if (this.model.counter == 0 && keysDown.size() > 0)
        {
            if (keysDown.contains(KeyEvent.VK_ESCAPE))
            {
                this.model.isSummary = false;
            }

            else if (keysDown.contains(KeyEvent.VK_LEFT) || keysDown.contains(KeyEvent.VK_UP))
            {
                if (this.model.optionIndex > 0)
                {
                    this.model.optionIndex--;
                }
                else
                {
                    this.model.optionIndex = this.model.team.length - 1;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_RIGHT) || keysDown.contains(KeyEvent.VK_DOWN))
            {
                if (this.model.optionIndex < this.model.team.length - 1)
                {
                    this.model.optionIndex++;
                }
                else
                {
                    this.model.optionIndex = 0;
                }
            }
            this.model.counter = this.model.INPUTDELAY;
        }
    }
}
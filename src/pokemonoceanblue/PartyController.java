package pokemonoceanblue;
import java.awt.event.KeyEvent;
import java.util.List;

public class PartyController 
{
    private PartyModel model;

    public PartyController(PartyModel model)
    {
        this.model = model;
    }

    public void userInput(List<Integer> keysDown)
    {
        if (keysDown.contains(KeyEvent.VK_ESCAPE))
        {
            model.returnValue = -1;
        }

        if (model.counter == 0)
        {
            if (keysDown.contains(KeyEvent.VK_ENTER))
            {
                model.confirmSelection();
            }

            else if (keysDown.contains(KeyEvent.VK_UP))
            {
                if (model.optionIndex - 2 >= 0)
                {
                    model.optionIndex -= 2;
                    model.counter = model.INPUTDELAY;
                }

                else
                {
                    model.optionIndex = model.team.length - (model.team.length % 2);
                    model.counter = model.INPUTDELAY;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_DOWN) && model.team.length > 2)
            {
                if (model.optionIndex + 2 > model.team.length - 1)
                {
                    model.optionIndex = (model.team.length + 1) % 2 + model.optionIndex % 2;
                    model.counter = model.INPUTDELAY;
                }

                else
                {
                    model.optionIndex += 2;
                    model.counter = model.INPUTDELAY;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_LEFT))
            {
                if (model.optionIndex % 2 == 1)
                {
                    model.optionIndex--;
                    model.counter = model.INPUTDELAY;
                }
                
                else if (model.team.length > model.optionIndex + 1)
                {
                    model.optionIndex++;
                    model.counter = model.INPUTDELAY;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_RIGHT))
            {
                if (model.optionIndex % 2 == 1)
                {
                    model.optionIndex--;
                    model.counter = model.INPUTDELAY;
                }

                else if (model.optionIndex + 1 < model.team.length)
                {
                    model.optionIndex++;
                    model.counter = model.INPUTDELAY;
                }
            }
        }
    }
}
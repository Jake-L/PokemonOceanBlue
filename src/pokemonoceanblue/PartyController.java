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
            this.model.returnValue = -1;
        }

        if (this.model.counter == 0 && keysDown.size() > 0)
        {
            if ((keysDown.contains(KeyEvent.VK_ENTER)) && (this.model.team[this.model.optionIndex].currentHP > 0 || !this.model.isBattle))
            {
                if (this.model.optionIndex != this.model.currentPokemon)
                {
                    this.model.confirmSelection();
                }

                else
                {
                    this.model.returnValue = -1;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_UP))
            {
                if (this.model.optionIndex - 2 >= 0)
                {
                    this.model.optionIndex -= 2;
                }

                else if (this.model.team.length % 2 == 0)
                {
                    this.model.optionIndex = this.model.team.length - 1 - ((this.model.optionIndex + 1) % 2);
                }

                else
                {
                    this.model.optionIndex = this.model.team.length - (this.model.team.length % 2);
                }
            }

            else if (keysDown.contains(KeyEvent.VK_DOWN) && this.model.team.length > 2)
            {             
                if (this.model.optionIndex + 2 < this.model.team.length)
                {
                    this.model.optionIndex += 2;
                }

                else if (this.model.team.length % 2 == 0)
                {
                    this.model.optionIndex = this.model.optionIndex % 2;
                }

                else
                {
                    this.model.optionIndex = (this.model.team.length + 1) % 2 + this.model.optionIndex % 2;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_LEFT))
            {
                if (this.model.optionIndex % 2 == 1)
                {
                    this.model.optionIndex--;
                }
                
                else if (this.model.team.length > this.model.optionIndex + 1)
                {
                    this.model.optionIndex++;
                }
            }

            else if (keysDown.contains(KeyEvent.VK_RIGHT))
            {
                if (this.model.optionIndex % 2 == 1)
                {
                    this.model.optionIndex--;
                }

                else if (this.model.optionIndex + 1 < this.model.team.length)
                {
                    this.model.optionIndex++;
                }
            }
            this.model.counter = this.model.INPUTDELAY;
        }
    }
}
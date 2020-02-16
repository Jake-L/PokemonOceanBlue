package pokemonoceanblue;

public class BaseModel {
    public int actionCounter;
    public int optionIndex;
    public int returnValue;
    public int optionMin = 0;
    public int optionMax;
    public int optionWidth;
    public int optionHeight;
    public final int ACTION_DELAY = 7;
    public int acceleration = -1;
    public int accelerationCounter;

    /**
     * Class for all models to inherit from
     * For the cursor to work, the inheriting model must set 
     *   optionMax, optionWidth, and optionHeight to define the cursor's movement area
     * If an inheriting model wants the cursor to accelerate while moving,
     *   they must set acceleration = 0 in their constructor or initialize method
     */
    public BaseModel()
    {     
        this.initialize();
    }

    public void initialize()
    {
        this.actionCounter = ACTION_DELAY;
        this.returnValue = -2;
        this.optionIndex = 0;
        this.accelerationCounter = 0;
    }

    /**
     * Change the cursor position
     * @param dx x-direction movement
     * @param dy y-direction movement
     */
    public void moveIndex(final int dx, final int dy)
    {
        // only move left and right if width > 1
        if (this.optionWidth > 1 && dx != 0)
        {
            if (dx > 0 && this.optionIndex < this.optionMax)
            {
                this.optionIndex++;
            }
            else if (dx < 0 && this.optionIndex > this.optionMin)
            {
                this.optionIndex--;
            }
        }
        else if (dy != 0)
        {
            if (dy > 0 && this.optionIndex + this.optionWidth <= this.optionMax)
            {
                this.optionIndex = this.optionIndex + this.optionWidth;
            }
            else if (dy < 0 && this.optionIndex - this.optionWidth >= this.optionMin)
            {
                this.optionIndex = this.optionIndex - this.optionWidth;
            }
        }
        // if choosing from a one dimensional list, left/right treated as up/down
        else if (dx != 0)
        {
            if (dx > 0 && this.optionIndex < this.optionMax)
            {
                this.optionIndex++;
            }
            else if (dx < 0 && this.optionIndex > this.optionMin)
            {
                this.optionIndex--;
            }
        }
    }

    /**
     * Set the return value as the current selection
     */
    public void confirmSelection()
    {
        if (this.optionMax > 0)
        {
            this.returnValue = this.optionIndex;
        }
        else
        {
            this.returnValue = -1;
        }
    }

    public void exitScreen()
    {
        this.returnValue = -1;
    }

    public void update()
    {
        if (this.actionCounter > 0)
        {
            this.actionCounter--;
        }

        // scroll faster the longer the player holds the button
        if (this.acceleration >= 0 && this.acceleration < 5)
        {
            this.accelerationCounter--;

            if (this.accelerationCounter <= 0)
            {
                this.acceleration++;
                this.accelerationCounter = 10;
            }
            
        }
    }

    /**
     * Returns information about what the player has selected
     * @return -2 means the player hasn't made a selection yet
     * @return -1 means they exited the screen without making a selection
     * @return 0 or greater gives the index/identifier of the player's selection
     */
    public int getSelection()
    {
        return this.returnValue;
    }
}
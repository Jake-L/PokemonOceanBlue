package pokemonoceanblue;

public class NotificationModel {
    public final String text;
    private int counter;
    private int directionMultiplier;

    public NotificationModel(String text, int directionMultiplier)
    {
        this.text = text;
        this.directionMultiplier = directionMultiplier;
    }

    public int getYOffset()
    {
        if (counter < 50)
        {
            return counter * directionMultiplier;
        }
        else if (counter < 100)
        {
            return 50 * directionMultiplier;
        }
        else 
        {
            return 50 + (100 - counter) * directionMultiplier;
        }
    }

    public void update()
    {
        this.counter++;
    }

    public boolean isComplete()
    {
        return this.counter > 150;
    }
}

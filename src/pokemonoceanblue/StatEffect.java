package pokemonoceanblue;

/**
 * Store stat change effects
 */
public class StatEffect
{
    public int statId;
    public int statChange;

    /** 
     * Constructor
     * @param statId the stat that is affected
     * @param statChange the amount to modify the stat
     */
    public StatEffect(int statId, int statChange)
    {
        this.statId = statId;
        this.statChange = statChange;
    }
}

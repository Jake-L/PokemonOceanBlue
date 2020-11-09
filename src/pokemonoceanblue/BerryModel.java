package pokemonoceanblue;

public class BerryModel extends SpriteModel {
    final int berryId;
    long plantTime;
    long timePerPhase = 2000;

    public BerryModel(int x, int y, int berryId, long plantTime)
    {
        super("generic_0", x, y, 0);
        this.berryId = berryId;
        this.plantTime = plantTime;
    }

    @Override
    public String getSpriteName()
    {
        int growthPhase = (int)Math.min((System.currentTimeMillis() - this.plantTime) / this.timePerPhase, 4);
        String spriteName = "";

        if (growthPhase < 2)
        {
            spriteName = "generic_";
        }
        else
        {
            //spriteName = this.berryId + "_";
            spriteName = "121_";
        }

        spriteName += String.valueOf(growthPhase);

        if (growthPhase > 0)
        {
            spriteName += "_" + (System.currentTimeMillis() / 500) % 2;
        }

        return spriteName;
    }

    public boolean isHarvestable()
    {
        return ((System.currentTimeMillis() - this.plantTime) / this.timePerPhase == 3);
    }

    public boolean isExpired()
    {
        return ((System.currentTimeMillis() - this.plantTime) / this.timePerPhase > 3);
    }
}
package pokemonoceanblue;

public class LocationModel {
    int x;
    int y;
    int mapId;

    public LocationModel(int x, int y, int mapId)
    {
        this.x = x;
        this.y = y;
        this.mapId = mapId;
    }

    public void applyOffset(int xOffset, int yOffset)
    {
        this.x += xOffset;
        this.y += yOffset;
    }

    public void applyOffset(Direction direction)
    {
        if (direction == Direction.DOWN)
        {
            this.applyOffset(0, 1);
        }
        else if (direction == Direction.UP)
        {
            this.applyOffset(0, -1);
        }
        else if (direction == Direction.LEFT)
        {
            this.applyOffset(-1, 0);
        }
        else if (direction == Direction.RIGHT)
        {
            this.applyOffset(1, 0);
        }
    }

    public boolean equals(LocationModel other)
    {  
        // return true if the LocationModels are the exact same object
        if (other == this) 
        { 
            return true; 
        } 
          
        // return true if the x, y, and mapId are all identical  
        return other.x == this.x && other.y == this.y && other.mapId == this.mapId;
    } 
}
package pokemonoceanblue;

import java.sql.*;

public class AreaModel 
{
    public String name;
    public final int mapId;
    public final int areaId;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    public int musicId;
    
    /** 
     * Constructor
     * @param areaId the unique identifier for this area
     */
    public AreaModel(int mapId, int areaId)
    {
        this.areaId = areaId;
        this.mapId = mapId;

        this.loadData();
    }

    /** 
     * Read all the information about the area
     */
    private void loadData()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            // load move data
            String query = "SELECT * FROM area WHERE area_id = " + this.areaId + " and map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            this.name = rs.getString("name");
            this.minX = rs.getInt("min_x");
            this.maxX = rs.getInt("max_x");
            this.minY = rs.getInt("min_y");
            this.maxY = rs.getInt("max_y");
            this.musicId = rs.getInt("music_id");     
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * @param x
     * @param y
     * @return true if the X and Y are contained within this area
     */
    public boolean checkArea(int x, int y)
    {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
    }
}
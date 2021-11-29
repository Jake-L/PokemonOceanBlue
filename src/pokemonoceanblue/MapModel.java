package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MapModel extends BaseModel {
    public boolean isFly;
    public int pokemonId; 
    public byte[][] mapIds;
    public byte[][] areaIds;
    public int x;
    public int y;
    public String areaName;
    public int destX;
    public int destY;

    /**
     * Holds data about the world map
     * @param isFly whether the player is picking somewhere to fly
     * @param pokemonId wild pokemon location to be shown
     */
    public MapModel(boolean isFly, int pokemonId)
    {
        super();
        this.isFly = isFly;
        this.pokemonId = pokemonId;
        this.optionMax = 18 * 18;
        this.optionWidth = 18;
        this.optionHeight = 18;
        this.loadData();
    }

    /**
     * Reads the map id and area id coordinates
     */
    private void loadData()
    {
        this.mapIds = new byte[18][18];
        this.areaIds = new byte[18][18];

        // create an instance of BufferedReader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/rawdata/map.csv"), "UTF-8"))) 
        {    
            // loop through all the lines of tile data
            int lineCounter = 0;
            String line = br.readLine();

            while (lineCounter < this.mapIds.length) 
            {
                if (line != null && line.length() >= 2)
                {
                    // split the line into an array of values
                    String[] data = line.split(",");

                    // convert the string into a byte and insert into the array
                    for (int i = 0; i < data.length; i++)
                    {
                        if (!data[i].equals(""))
                        {
                            String[] idPair = data[i].split("#");
                            this.mapIds[lineCounter][i] = Byte.parseByte(idPair[0]);
                            this.areaIds[lineCounter][i] = Byte.parseByte(idPair[1]);
                        }
                        else 
                        {
                            this.mapIds[lineCounter][i] = -1;
                            this.areaIds[lineCounter][i] = -1;
                        }
                    }
                }

                // read next line before looping
                line = br.readLine();
                lineCounter++;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Change the cursor position
     * Overrides the BaseModel function to load area data after moving cursor
     * @param dx x-direction movement
     * @param dy y-direction movement
     */
    @Override
    public void moveIndex(final int dx, final int dy)
    {
        super.moveIndex(dx, dy);
        this.x = this.optionIndex % this.mapIds[0].length;
        this.y = this.optionIndex / this.mapIds.length;
        this.loadAreaData(this.getMapId(), this.areaIds[this.y][this.x]);
    }

    public int getMapId()
    {
        return this.mapIds[this.y][this.x];
    }

    /**
     * Loads data for a specific area
     */
    private void loadAreaData(int mapId, int areaId)
    {
        this.areaName = "";
        this.destX = -1;
        this.destY = -1;

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT name, min_x + safe_x as safe_x, min_y + safe_y as safe_y "
                         + "FROM area "
                         + "WHERE map_id = " + mapId
                         + " AND area_id = " + areaId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.areaName = rs.getString("name");
                this.destX = rs.getInt("safe_x");
                this.destY = rs.getInt("safe_y");
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}

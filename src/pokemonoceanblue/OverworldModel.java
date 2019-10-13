package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class OverworldModel {
    public int mapId;
    public byte[][] tiles;
    public List<SpriteModel> mapObjects = new ArrayList<SpriteModel>(); 
    public CharacterModel[] CPUModel = new CharacterModel[0];
    private Portal[] portals = new Portal[0];
    
    /** 
     * @param mapId unique identifier for the current map
     */
    public OverworldModel(int mapId){
        this.mapId = mapId;
        readMapFile();
        if (this.mapId == 0)
        {
            CPUModel = new CharacterModel[1];
            CPUModel[0] = new CharacterModel("cassie", 5, 4);
            CPUModel[0].setOverworldModel(this);

            portals = new Portal[6];
            // houses
            portals[0] = new Portal(8, 35, 1, 3, 8);
            portals[1] = new Portal(19, 35, 2, 3, 8);
            portals[2] = new Portal(8, 42, 3, 3, 8);
            portals[3] = new Portal(19, 42, 4, 3, 8);
            portals[4] = new Portal(19, 49, 5, 3, 8);

            // oak's lab
            portals[5] = new Portal(10, 49, 6, 6, 12);
        }
        else if (mapId == 1)
        {
            portals = new Portal[1];
            portals[0] = new Portal(3, 9, 0, 8, 11);
        }
        else if (mapId == 2)
        {
            portals = new Portal[1];
            portals[0] = new Portal(3, 9, 0, 19, 11);
        }
        else if (mapId == 3)
        {
            portals = new Portal[1];
            portals[0] = new Portal(3, 9, 0, 8, 18);
        }
        else if (mapId == 4)
        {
            portals = new Portal[1];
            portals[0] = new Portal(3, 9, 0, 19, 18);
        }
        else if (mapId == 5)
        {
            portals = new Portal[1];
            portals[0] = new Portal(3, 9, 0, 19, 25);
        }
        else if (mapId == 6)
        {
            portals = new Portal[1];
            portals[0] = new Portal(6, 13, 0, 10, 25);
        }
    }

    /** 
     * read the tile grid for the current map from a CSV file
     */
    public void readMapFile(){
        Path pathToFile = Paths.get(String.format("src/maps/map%s.csv", mapId));

        // create an instance of BufferedReader
        try (BufferedReader br = Files.newBufferedReader(pathToFile,
                StandardCharsets.US_ASCII)) {

            // read the first line which gives the number of rows and columns
            String line = br.readLine();
            tiles = new byte[Integer.parseInt(line.split(",")[0])][Integer.parseInt(line.split(",")[1])];
            int lineCounter = 0;
            
            // loop through all the lines of tile data
            line = br.readLine();
            while (lineCounter < tiles.length) {

                // split the line into an array of values
                String[] data = line.split(",");

                // convert the string into a byte and insert into the array
                for (var i = 0; i < data.length; i++)
                {
                    tiles[lineCounter][i] = Byte.parseByte(data[i]);
                }

                // read next line before looping
                line = br.readLine();
                lineCounter++;
            }

            // loop through all the mapObjects
            while (line != null) 
            {
                // split the line into an array of values
                String[] data = line.split(",");

                // create the mapObject
                mapObjects.add(new SpriteModel(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2])));

                // read next line before looping
                line = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
     * @param x x position of the character
     * @param y y position of the character
     * @return true if the position is free or false if it is occupied
     */
    public boolean checkPosition(int x, int y)
    {
        if (y < 0 || y >= this.tiles.length
            || x < 0 || x >= this.tiles[y].length
            || this.tiles[y][x] < 1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /** 
     * @param x x position of the character
     * @param y y position of the character
     * @return portal that the player stepped on or null if there is no portal at that position
     */
    public Portal checkPortal(int x, int y)
    {
        for (int i = 0; i < portals.length; i++)
        {
            if (x == portals[i].x && y == portals[i].y)
            {
                return portals[i];
            }
        }
        return null;
    }
}
package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class OverworldModel {
    int mapId;
    public byte[][] tiles;
    public List<SpriteModel> mapObjects = new ArrayList<SpriteModel>(); 
    public CharacterModel[] CPUModel;
    
    /** 
     * @param mapId unique identifier for the current map
     */
    public OverworldModel(int mapId){
        this.mapId = mapId;
        readMapFile();
        mapObjects.add(new SpriteModel("house1",9,10));
        mapObjects.add(new SpriteModel("house1",9,17));
        mapObjects.add(new SpriteModel("house1",20,10));
        mapObjects.add(new SpriteModel("house1",20,17));
    }

    public void setCPUModel(CharacterModel[] CPUModel){
        this.CPUModel = CPUModel;
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
            int line_counter = 0;
            
            // loop until all lines are read
            line = br.readLine();
            while (line != null) {

                // split the line into an array of value
                String[] data = line.split(",");

                // convert the string into a byte and insert into the array
                for (var i = 0; i < data.length; i++)
                {
                    tiles[line_counter][i] = Byte.parseByte(data[i]);
                }

                // read next line before looping
                line = br.readLine();
                line_counter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OverworldModel {
    public int mapId;
    public byte[][] tiles;
    public List<SpriteModel> mapObjects = new ArrayList<SpriteModel>(); 
    public CharacterModel[] cpuModel = new CharacterModel[0];
    public CharacterModel playerModel;
    private Portal[] portals = new Portal[0];
    public ConversationModel conversation;
    private App app;
    private List<Integer> wildPokemon = new ArrayList<Integer>();
    public String[] textOptions;
    public int optionIndex;

    // prevent players from accidently repeating actions by holdings keys
    private int actionCounter = 15;
    
    /** 
     * @param mapId unique identifier for the current map
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public OverworldModel(int mapId, CharacterModel playerModel, App app){
        this.mapId = mapId;
        this.playerModel = playerModel;
        this.app = app;
        readMapFile();
        loadWildPokemon();
        if (this.mapId == 0)
        {
            cpuModel = new CharacterModel[1];
            cpuModel[0] = new CharacterModel("cassie", 6, 6, 3);
            cpuModel[0].setOverworldModel(this);

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

            cpuModel = new CharacterModel[2];
            cpuModel[0] = new CharacterModel("oak", 6, 3, 0);
            cpuModel[0].setOverworldModel(this);
            cpuModel[1] = new CharacterModel("scientist", 8, 8, 2);
            cpuModel[1].setOverworldModel(this);
        }
    }

    /** 
     * read the tile grid for the current map from a CSV file
     */
    public void readMapFile()
    {
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

    public void update()
    {
        // decrement action counter
        if (this.actionCounter > 0)
        {
            this.actionCounter--;
        }

        // update the CPUs
        Random rand = new Random();
        for (int i = 0; i < cpuModel.length; i++)
        {
            cpuModel[i].update(false);
            
            // generate random movement
            if (cpuModel[i].getMovementCounter() < 0)
            {
                int n = rand.nextInt(100);
                int dx = 0;
                int dy = 0;

                if (n < 3)
                {
                    dx = n - 1;
                }
                else if (n < 6)
                {
                    dy = n - 4;
                }

                if (dx != 0 || dy != 0)
                {
                    if (Math.abs(cpuModel[i].spawn_x - cpuModel[i].getX() - dx) + Math.abs(cpuModel[i].spawn_y - cpuModel[i].getY() - dy) <= 2)
                    {
                        cpuModel[i].setMovement(dx, dy, 1);
                    }
                }
            }
        }

        // update the current conversation
        if (this.conversation != null)
        {
            this.conversation.update();
        }
    }

    /** 
     * @param x x position of the character
     * @param y y position of the character
     * @return true if the position is free or false if it is occupied
     */
    public boolean checkPosition(int x, int y)
    {
        // check if the map allows movement
        if (y < 0 || y >= this.tiles.length
            || x < 0 || x >= this.tiles[y].length
            || this.tiles[y][x] < 1)
        {
            return false;
        }
        // check if the player is already standing there
        if (x == playerModel.getX() && y == playerModel.getY())
        {
            return false;
        }
        // check if a CPU is already standing there
        for (int i = 0; i < cpuModel.length; i++)
        {
            if (x == cpuModel[i].getX() && y == cpuModel[i].getY())
            {
                return false;
            }
        }

        return true;
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

    /** 
     * Prevent characters from moving during conversations
     * @return true if characters can walk around or false otherwise
     */
    public boolean canMove()
    {
        if (this.conversation != null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /** 
     * Enable to player to interact with other characters
     */
    public void checkAction(int x, int y)
    {
        // if already in a conversation, check if it's time to move on to next dialog
        if (this.conversation != null)
        {
            // delete the conversation if it is over
            if (this.conversation.isComplete())
            {
                this.conversation = null;
                this.actionCounter = 15;
                this.app.createBattle(0);
            }
            else
            {
                this.conversation.nextEvent();
            }
        }
        // otherwise check for a cpu to interact with
        else if (this.actionCounter == 0)
        {
            for (CharacterModel cpu : cpuModel)
            {
                if (x == cpu.getX() && y == cpu.getY())
                {
                    if (cpu.conversationId == -1)
                    {
                        // no other CPU can occur the same spot so no point in continuing to search
                        break;
                    }
                    else
                    {
                        // make the CPU face the player
                        if (cpu.getX() > this.playerModel.getX())
                        {
                            cpu.setDirection(Direction.LEFT);
                        }
                        else if (cpu.getX() < this.playerModel.getX())
                        {
                            cpu.setDirection(Direction.RIGHT);
                        }
                        else if (cpu.getY() > this.playerModel.getY())
                        {
                            cpu.setDirection(Direction.UP);
                        }
                        else if (cpu.getY() < this.playerModel.getY())
                        {
                            cpu.setDirection(Direction.DOWN);
                        }

                        // start the conversation
                        this.conversation = new ConversationModel(cpu.conversationId);
                        break;
                    }
                }
            }
        }
    }

    /** 
     * Check for any interactions based on player movement
     * This function creates wild pokemon encounters
     */
    public void checkMovement(int x, int y)
    {
        if (this.tiles[y][x] == 4)
        {
            Random rand = new Random();
            int n = rand.nextInt(this.wildPokemon.size() * 5);
            if (n < this.wildPokemon.size())
            {
                PokemonModel[] team = new PokemonModel[1];
                boolean shiny = rand.nextInt(5) == 1 ? true : false;
                team[0] = new PokemonModel(this.wildPokemon.get(n), 5, shiny);
                this.app.createBattle(team);
            }
        }
    }

    public void openMenu()
    {
        if (this.actionCounter == 0)
        {
            // open the menu
            if (this.textOptions == null)
            {
                this.textOptions = new String[]{"Pokemon", "Bag"};
                this.actionCounter = 15;
            }
            // exit the menu if it was already open
            else
            {
                this.textOptions = null;
                this.actionCounter = 15;
            }
        }
    }

    public void confirmSelection()
    {
        if (this.textOptions[this.optionIndex] == "Pokemon")
        {
            app.openParty();
            this.openMenu();
        }
        else if (this.textOptions[this.optionIndex] == "Bag")
        {
            app.openInventory();
            this.openMenu();
        }
    }

    public void moveCursor(int dy)
    {
        if (dy > 0 && this.optionIndex < this.textOptions.length - 1)
        {
            this.optionIndex++;
            this.actionCounter = 10;
        }
        else if (dy < 0 && this.optionIndex > 0)
        {
            this.optionIndex--;
            this.actionCounter = 10;
        }
    }

    /** 
     * load a list of wild pokemon that can appear on the current map
     */
    private void loadWildPokemon()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT pokemon_id FROM pokemon_location WHERE map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.wildPokemon.add(rs.getInt("pokemon_id"));
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}
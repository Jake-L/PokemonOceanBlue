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
    private List<ConversationTrigger> conversationTrigger = new ArrayList<ConversationTrigger>();

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
            this.cpuModel = new CharacterModel[1];
            this.cpuModel[0] = new CharacterModel("campBoy", 6, 6, 4, 3, 0, Direction.RIGHT);
            this.cpuModel[0].setOverworldModel(this);

            this.conversationTrigger.add(new ConversationTrigger(4, cpuModel[0], 7, 6, true));
            this.conversationTrigger.add(new ConversationTrigger(4, cpuModel[0], 8, 6, true));
            this.conversationTrigger.add(new ConversationTrigger(4, cpuModel[0], 9, 6, true));

            // portals = new Portal[6];
            // // houses
            // portals[0] = new Portal(8, 35, 1, 3, 8);
            // portals[1] = new Portal(19, 35, 2, 3, 8);
            // portals[2] = new Portal(8, 42, 3, 3, 8);
            // portals[3] = new Portal(19, 42, 4, 3, 8);
            // portals[4] = new Portal(19, 49, 5, 3, 8);

            // // oak's lab
            // portals[5] = new Portal(10, 49, 6, 6, 12);
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
            cpuModel[0] = new CharacterModel("oak", 6, 3, 1, 0, 0, Direction.DOWN);
            cpuModel[0].setOverworldModel(this);
            cpuModel[1] = new CharacterModel("scientist", 8, 8, 2, 2);
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
            if (cpuModel[i].getMovementCounter() < 0 && cpuModel[i].wanderRange > 0)
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
                    if (Math.abs(cpuModel[i].spawn_x - cpuModel[i].getX() - dx) + Math.abs(cpuModel[i].spawn_y - cpuModel[i].getY() - dy) <= cpuModel[i].wanderRange)
                    {
                        cpuModel[i].setMovement(dx, dy, 1);
                    }
                }
            }
        }

        // update the current conversation
        if (this.conversation != null)
        {
            // determine if a character needs to be moved
            int characterId = this.conversation.getMovementCharacterId();
            if (characterId > -1)
            {
                for (int i = 0; i < this.cpuModel.length; i++)
                {
                    if (this.cpuModel[i].characterId == characterId && this.cpuModel[i].getMovementCounter() <= 0)
                    {
                        // move the character
                        this.cpuModel[i].setMovement(
                            this.conversation.getMovementDx(), 
                            this.conversation.getMovementDy(), 
                            1
                        );
                        this.conversation.setCharacterMoved();
                        break;
                    }
                }                
            }
            this.conversation.update();
        }
    }

    /** 
     * @param x x position of the character
     * @param y y position of the character
     * @return true if the position is free or false if it is occupied
     */
    public boolean checkPosition(int x, int y, boolean surf)
    {
        // check if the map allows movement
        if (y < 0 || y >= this.tiles.length
            || x < 0 || x >= this.tiles[y].length
            || this.tiles[y][x] < 0
            || (this.tiles[y][x] == 0 && !surf))
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
    public boolean canMove(int characterId)
    {
        if (this.conversation != null && this.conversation.getMovementCharacterId() != characterId)
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
            }
            else
            {
                // start a battle
                if (this.conversation.getBattleId() >= 0)
                {
                    this.app.createBattle(this.conversation.getBattleId());
                }

                this.conversation.nextEvent();

                // delete the conversation if it is over
                if (this.conversation.isComplete())
                {
                    this.conversation = null;
                    this.actionCounter = 15;
                }
            }
        }
        // surf if facing water
        else if (!this.playerModel.surf && tiles[y][x] == 0 && this.playerModel.getMovementCounter() <= 0)
        {
            this.playerModel.surf = true;
            this.playerModel.setMovement(x - this.playerModel.getX(), y - this.playerModel.getY(), 1);
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
                        this.conversation = new ConversationModel(cpu.conversationId, this.playerModel, cpu);
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
        // player is no longer surfing when they step onto solid land
        if (this.tiles[y][x] > 0)
        {
            this.playerModel.surf = false;
        }

        ConversationTrigger current;
        for (int i = 0; i < this.conversationTrigger.size(); i++)
        {
            current = this.conversationTrigger.get(i);
            if (current.x == x && current.y == y)
            {
                this.conversation = new ConversationModel(current.conversationId, this.playerModel, current.cpuModel);
                
                // clear the trigger
                if (current.clearAfterUse)
                {
                    this.clearTriggers(current.conversationId);
                }

                break;
            }
        }
    }

    /**
     * After a conversationTrigger has been used, remove all the triggers for that conversation
     * @param conversationId the identifier for the triggers to be removed
     */
    private void clearTriggers(int conversationId)
    {
        int i = 0;
        while (i < this.conversationTrigger.size())
        {
            if (this.conversationTrigger.get(i).conversationId == conversationId)
            {
                this.conversationTrigger.remove(i);
            }
            else
            {
                i++;
            }
        } 
    }

    /**
     * Opens the main menu, allowing the player to look at their Pokemon, inventory, etc
     */
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

    /**
     * Take action based on what the player selects in the menu
     */
    public void confirmSelection()
    {
        if (this.textOptions[this.optionIndex] == "Pokemon")
        {
            app.openParty(-1);
            this.openMenu();
        }
        else if (this.textOptions[this.optionIndex] == "Bag")
        {
            app.openInventory();
            this.openMenu();
        }
    }

    /**
     * Change the currently selected option
     * @param dy the direction to move the cursor
     */
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

    /**
     * Holds a specific location on the map that starts a conversation when the player steps on it
     */
    class ConversationTrigger
    {
        public final int conversationId;
        public final CharacterModel cpuModel; 
        public final int x; 
        public final int y;
        public final boolean clearAfterUse;

        /**
         * Constructor
         * @param conversationId unique identifier for the conversation
         * @param cpuModel the cpu involved in the conversation
         * @param x the x-coordinate of the trigger
         * @param y the y-coordinate of the trigger
         * @param clearAfterUse whether the trigger remains after it's first use
         */
        public ConversationTrigger(int conversationId, CharacterModel cpuModel, int x, int y, boolean clearAfterUse)
        {
            this.conversationId = conversationId;
            this.cpuModel = cpuModel;
            this.x = x;
            this.y = y;
            this.clearAfterUse = clearAfterUse;
        }
    }
}
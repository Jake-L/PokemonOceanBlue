package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class OverworldModel extends BaseModel {
    public int mapId;
    public byte[][] tiles;
    public byte[][] tilesOverlay;
    public List<SpriteModel> mapObjects = new ArrayList<SpriteModel>(); 
    public List<CharacterModel> cpuModel = new ArrayList<CharacterModel>();
    public CharacterModel playerModel;
    private List<PortalModel> portals = new ArrayList<PortalModel>();
    public ConversationModel conversation;
    private App app;
    private Map<Integer, List<Integer>> wildPokemon = new HashMap<Integer, List<Integer>>();
    private List<ConversationTriggerModel> conversationTrigger = new ArrayList<ConversationTriggerModel>();
    private int areaId = -1;
    private List<AreaModel> areas = new ArrayList<AreaModel>();
    protected String mugshotBackground;
    protected String mugshotCharacter;
    public boolean removeCharacter = false;
    public byte battleBackgroundId = 6;
    public List<ItemModel> itemOptions = new ArrayList<ItemModel>(); 
    public InventoryModel inventoryModel;
    public DayCareModel dayCareModel;
    public byte weather;
    private boolean battle = false;
    
    /** 
     * @param mapId unique identifier for the current map
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public OverworldModel(int mapId, CharacterModel playerModel, App app, InventoryModel inventoryModel, DayCareModel dayCareModel){
        this.mapId = mapId;
        this.playerModel = playerModel;
        this.app = app;
        this.inventoryModel = inventoryModel;
        this.dayCareModel = dayCareModel;
        
        this.readMapFile();
        this.loadWildPokemon();
        this.loadMapObjects();
        this.loadPortals();
        this.loadCharacters();
        this.loadConversationTriggers();
        this.loadAreas();
        this.checkArea(playerModel.getX(), playerModel.getY());
        this.checkAutoTriggers(playerModel.getX(), playerModel.getY());

        if (this.tiles.length > 20 && this.mapId != 14)
        {
            // generate random weather when on a large map
            // since most large maps are outside
            this.weather = (byte)(new Random().nextInt(5));
        }
    }

    /**
     * Constructor used for creating tournament battle rooms
     * @param mapId
     * @param playerModel
     * @param app
     * @param tournamentModel
     */
    public OverworldModel(int mapId, CharacterModel playerModel, App app, TournamentModel tournamentModel)
    {
        this.mapId = mapId;
        this.playerModel = playerModel;
        this.app = app;

        this.readMapFile();
        this.loadMapObjects();
        this.loadPortals();
        this.loadCharacters();
        this.loadConversationTriggers();
        this.loadAreas();
        this.checkArea(playerModel.getX(), playerModel.getY());

        CharacterModel character = tournamentModel.getCharacter();
        character.setOverworldModel(this);
        this.cpuModel.add(character);
        
        this.conversationTrigger.add(new ConversationTriggerModel(character.conversationId, character, 7, 6, -1, true, false));
        this.checkAutoTriggers(playerModel.getX(), playerModel.getY());
    }

    /** 
     * read the tile grid for the current map from a CSV file
     */
    public void readMapFile()
    {
        int mapTemplateId = this.mapId;
        boolean overlay = false;

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT map_template_id, overlay FROM map_template WHERE map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            mapTemplateId = rs.getInt("map_template_id");
            overlay = rs.getInt("overlay") == 1;
            tiles = readMapFileAux("map" + mapTemplateId);
            if (overlay)
            {
                tilesOverlay = readMapFileAux("map" + mapTemplateId + "overlay");
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    private byte[][] readMapFileAux(String path)
    {
        byte[][] output;

        // create an instance of BufferedReader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(String.format("/maps/%s.csv", path)), "UTF-8"))) {

            // read the first line which gives the number of rows and columns
            String line = br.readLine();
            output = new byte[Integer.parseInt(line.split(",")[0])][];
            int lineCounter = 0;
            
            // loop through all the lines of tile data
            line = br.readLine();
            while (lineCounter < output.length) {
                if (line != null && line.length() >= 2)
                {
                    // split the line into an array of values
                    String[] data = line.split(",");
                    output[lineCounter] = new byte[data.length];

                    // convert the string into a byte and insert into the array
                    for (var i = 0; i < data.length; i++)
                    {
                        if (!data[i].equals(""))
                        {
                            output[lineCounter][i] = Byte.parseByte(data[i]);
                        }
                    }
                }
                else
                {
                    output[lineCounter] = new byte[0];
                }

                // read next line before looping
                line = br.readLine();
                lineCounter++;
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
            output = new byte[0][0];
        }
        
        return output;
    }

    @Override
    public void update()
    {
        // decrement action counter
        super.update();

        // update the CPUs
        Random rand = new Random();
        for (int i = 0; i < cpuModel.size(); i++)
        {
            cpuModel.get(i).update(false);
            
            // generate random movement
            if (cpuModel.get(i).getMovementCounter() < 0 && cpuModel.get(i).wanderRange > 0)
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
                    if (Math.abs(cpuModel.get(i).spawn_x - cpuModel.get(i).getX() - dx) + Math.abs(cpuModel.get(i).spawn_y - cpuModel.get(i).getY() - dy) <= cpuModel.get(i).wanderRange)
                    {
                        cpuModel.get(i).setMovement(dx, dy, 1);
                    }
                }
            }
        }

        // update the current conversation
        if (this.conversation != null)
        {
            this.mugshotCharacter =  this.conversation.getMugshotCharacter();
            this.mugshotBackground =  this.conversation.getMugshotBackground();
            this.removeCharacter = this.conversation.removeCharacter(this.cpuModel, this.conversationTrigger);
            this.conversation.update();
            int characterId = this.conversation.getMovementCharacterId();

            // delete the conversation if it is over
            if (this.conversation.isComplete())
            {
                this.conversation = null;
                this.actionCounter = 15;
            }

            // determine if a character needs to be moved
            else if (characterId > -2)
            {
                CharacterModel character = this.getCharacterModel(characterId);
            
                if (character.getMovementCounter() <= 0)
                {
                    // move the character
                    character.setMovement(
                        this.conversation.getMovementDx(), 
                        this.conversation.getMovementDy(), 
                        1
                    );
                    this.conversation.setCharacterMoved();            
                }
            }

            // check if the player's team should be healed as a result of the conversation
            else if (this.conversation.isHealTeam())
            {
                app.healTeam();
            }

            // get a gift Pokemon 
            else if (this.conversation.getGiftPokemonId() > -1)
            {
                this.app.addPokemon(this.conversation.getGiftPokemonId(), this.conversation.getGiftPokemonLevel());
            }
        }
        else
        {
            this.mugshotCharacter = null;
            this.mugshotBackground = null;
            this.removeCharacter = false;
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
            || ((this.tiles[y][x] == 0 || this.tiles[y][x] == 1) && !surf))
        {
            return false;
        }
        // check if the player is already standing there
        if (x == playerModel.getX() && y == playerModel.getY())
        {
            return false;
        }
        // check if a CPU is already standing there
        for (int i = 0; i < cpuModel.size(); i++)
        {
            if (x == cpuModel.get(i).getX() && y == cpuModel.get(i).getY())
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
    public PortalModel checkPortalModel(int x, int y)
    {
        for (int i = 0; i < portals.size(); i++)
        {
            if (x == portals.get(i).x && y == portals.get(i).y)
            {
                return portals.get(i);
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
        else if (this.battle)
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
            // start a battle
            if (this.conversation.getBattleId() >= 0)
            {
                this.battle = true;
                this.app.createTrainerBattle(this.conversation.getBattleId());
                this.conversation.setBattleStarted();
            }
            else
            {
                this.conversation.nextEvent();
                this.checkConversationAction();
            }
            this.actionCounter = 15;
        }
        // surf if facing water
        else if (!this.playerModel.surf 
            && (tiles[y][x] == 0 || tiles[y][x] == 1) 
            && this.playerModel.getMovementCounter() <= 0)
        {
            this.playerModel.surf = true;
            this.playerModel.setMovement(x - this.playerModel.getX(), y - this.playerModel.getY(), 1);
            this.actionCounter = 15;
        }
        // otherwise check for a cpu to interact with
        else
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
                        this.conversation = new ConversationModel(cpu.conversationId, this.playerModel, cpu, false);
                        this.checkConversationAction();
                        this.actionCounter = 15;
                        break;
                    }
                }
            }
        
            // check for an object to interact with
            for (ConversationTriggerModel current : this.conversationTrigger)
            {
                if (current.x == x && current.y == y && !current.autoTrigger)
                {
                    // open PC
                    if (current.conversationId == 9999)
                    {
                        app.openPokemonStorage();
                    }
                    else if (current.conversationId == 9998)
                    {
                        this.conversation = new DayCareConversationModel(this.dayCareModel);
                        this.checkConversationAction();
                    }
                    else
                    {
                        this.conversation = new ConversationModel(current.conversationId, this.playerModel, current.cpuModel, false);
                        this.checkConversationAction();
                    }
                    
                    this.actionCounter = 15;
                    break;
                }
            }
        }
    }

    /**
     * Actions happening at the start of each conversation event
     */
    private void checkConversationAction()
    {
        // show text options
        if (this.conversation.getOptions() != null)
        {
            this.textOptions = this.conversation.getOptions();
        }
        // open shop
        else if (this.conversation.getShopId() == 1)
        {
            this.itemOptions.add(new ItemModel(1, 1));
            this.itemOptions.add(new ItemModel(3, 1));
            this.optionMax = this.itemOptions.size() - 1;
            this.optionWidth = 1;
            this.optionHeight = this.optionMax;
            this.optionIndex = 0;
            this.acceleration = 0;
            this.accelerationCounter = 10;
        }
        // play music
        else if (this.conversation.getMusicId() > -1)
        {
            this.app.playSong(this.conversation.getMusicId(), true);
        }
        // open the party screen
        else if (this.conversation.openParty())
        {
            this.app.openParty(-1, true);
        }
        // silently add a Pokemon to the player's party/PC
        else if (this.conversation.getWithdrawnPokemon() > -1)
        {
            this.app.addPokemonSilent(this.dayCareModel.withdrawPokemon(this.conversation.getWithdrawnPokemon()));
        }
    }

    private void checkArea(int x, int y)
    {
        // check if the player entered a new area
        for (AreaModel area : this.areas)
        {
            if (area.checkArea(x, y))
            {
                if (area.areaId != this.areaId)
                {
                    this.areaId = area.areaId;
                    this.battleBackgroundId = area.battleBackgroundId;
                    this.app.playSong(area.musicId, false);
                    break;
                }
            }
        }

        // set the default area id to 0
        if (this.areaId == -1)
        {
            this.areaId = 0;
            this.battleBackgroundId = 6;
        }
    }

    /** 
     * Check for any interactions based on player movement
     * This function creates wild pokemon encounters
     */
    public void checkMovement(int x, int y)
    {
        // update movement trackers
        this.app.decrementStepCounter();

        // check if the player entered a new area
        this.checkArea(x, y);

        // check for wild Pokemon encounters
        if (this.tiles[y][x] == 0 
            || this.tiles[y][x] == 1 
            || this.tiles[y][x] == 5 
            || this.tiles[y][x] == 90)
        {
            int index = this.areaId * 1000 + this.tiles[y][x];
            if (this.wildPokemon.get(index) != null)
            {
                Random rand = new Random();
                int n = rand.nextInt(this.wildPokemon.get(index).size() * 5);
                if (n < this.wildPokemon.get(index).size())
                {
                    this.battle = true;
                    this.app.createWildBattle(this.wildPokemon.get(index).get(n), 5, false);
                }
            }
        }
        // player is no longer surfing when they step onto solid land
        if (this.tiles[y][x] > 1)
        {
            this.playerModel.surf = false;
        }

        this.checkAutoTriggers(x, y);
    }

    /**
     * Check if any conversations should be started automatically
     * @param x
     * @param y
     */
    private void checkAutoTriggers(int x, int y)
    {
        ConversationTriggerModel current;
        for (int i = 0; i < this.conversationTrigger.size(); i++)
        {
            current = this.conversationTrigger.get(i);
            if (current.x == x && current.y == y && current.autoTrigger)
            {
                this.conversation = new ConversationModel(current.conversationId, this.playerModel, current.cpuModel, current.approachPlayer);
                this.checkConversationAction();
                break;
            }
        }
    }

    /**
     * Opens the main menu, allowing the player to look at their Pokemon, inventory, etc
     */
    public void openMenu()
    {
        // exit a shop if it is open
        if (this.itemOptions.size() > 0)
        {
            this.itemOptions.clear();
            this.optionIndex = 0;
            this.optionHeight = 0;
            this.optionMax = 0;
            this.optionWidth = 0;
            this.acceleration = -1;
            this.conversation.nextEvent();
        }
        // exit the menu if it was already open
        if (this.textOptions != null)
        {
            this.textOptions = null;
            this.textOptionIndex = 0;
        }
        // open the menu
        else if (this.conversation == null)
        {
            this.textOptions = new String[]{"Pokedex", "Pokemon", "Bag", "Achievements", "Save"};
            this.textOptionIndex = 0;
        }
    }

    /**
     * Take action based on what the player selects in the menu
     */
    @Override
    public void confirmSelection()
    {
        if (this.conversation != null)
        {
            if (this.itemOptions.size() > 0)
            {
                this.inventoryModel.buyItem(this.itemOptions.get(this.optionIndex));
            }
            else
            {
                this.conversation.setOption(this.textOptionIndex);
                this.openMenu();
                this.checkConversationAction();
            }
        }
        else if (this.textOptions[this.textOptionIndex] == "Achievements")
        {
            app.openAchievements();
            this.openMenu();
        }
        else if (this.textOptions[this.textOptionIndex] == "Pokedex")
        {
            app.openPokedex();
            this.openMenu();
        }
        else if (this.textOptions[this.textOptionIndex] == "Pokemon")
        {
            app.openParty(-1, false);
            this.openMenu();
        }
        else if (this.textOptions[this.textOptionIndex] == "Bag")
        {
            app.openInventory();
            this.openMenu();
        }
        else if (this.textOptions[this.textOptionIndex] == "Save")
        {
            app.save();
            this.openMenu();
        }
    }

    /**
     * Tell the overworld model which Pokemon was selected in the party screen
     * Used for selecting a Pokemon to leave in the day care
     * or to learn a move from a move tutor, etc.
     * @return true if the Pokemon should be removed from the player's party
     */
    public boolean setPokemon(PokemonModel pokemon)
    {
        if (this.conversation != null)
        {
            return this.conversation.setPokemon(pokemon);
        }
        else
        {
            return false;
        }
    }

    /**
     * Wrapper for openMenu function to follow BaseModel structure
     */
    @Override
    public void exitScreen()
    {
        this.openMenu();
    }

    /** 
     * load a list of wild pokemon that can appear on the current map
     */
    private void loadWildPokemon()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT area_id, tile_id, pokemon_id FROM pokemon_location WHERE map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                int indexId = rs.getInt("area_id") * 1000 + rs.getInt("tile_id");
                if (this.wildPokemon.get(indexId) == null)
                {
                    this.wildPokemon.put(indexId, new ArrayList<Integer>());
                }
                this.wildPokemon.get(indexId).add(rs.getInt("pokemon_id"));
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /** 
     * load a list of map objects that can appear on the current map
     */
    private void loadMapObjects()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = String.format("""
                SELECT mo.name,
                mo.x + IFNULL(a.min_x,0) as x,
                mo.y + IFNULL(a.min_y,0) as y,
                mo.y_adjust
                FROM map_object mo
                LEFT JOIN area a
                ON a.area_id = mo.area_id 
                AND a.map_id = mo.map_id 
                WHERE mo.map_id = %s
                ORDER BY mo.y + IFNULL(a.min_y,0) + IFNULL(mo.y_adjust,0) ASC
                """, this.mapId);

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.mapObjects.add(new SpriteModel(
                    rs.getString("name"), 
                    rs.getInt("x"), 
                    rs.getInt("y"),
                    rs.getInt("y_adjust")
                ));
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /** 
     * load a list of areas within the current map
     */
    private void loadAreas()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT area_id "
                         + "FROM area "
                         + "WHERE map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.areas.add(new AreaModel(this.mapId, rs.getInt("area_id")));
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }


    /** 
     * load a list of portals that can appear on the current map
     */
    private void loadPortals()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
                SELECT p.x + IFNULL(a.min_x,0) as x,
                p.y + IFNULL(a.min_y,0) as y,
                p.dest_map_id,
                p.dest_x + IFNULL(dest.min_x,0) as dest_x,
                p.dest_y + IFNULL(dest.min_y,0) as dest_y
                FROM portal p
                LEFT JOIN area a
                ON a.area_id = p.area_id
                AND a.map_id = p.map_id
                LEFT JOIN area dest
                ON dest.area_id = p.dest_area_id
                AND dest.map_id = p.dest_map_id
                WHERE p.map_id = 
                """ + this.mapId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.portals.add(new PortalModel(
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("dest_map_id"),
                    rs.getInt("dest_x"),
                    rs.getInt("dest_y")
                ));
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /** 
     * load a list of characters that can appear on the current map
     */
    private void loadCharacters()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
                SELECT c.character_id,
                c.name, c.sprite_name,
                c.x + IFNULL(a.min_x,0) as x,
                c.y + IFNULL(a.min_y,0) as y,
                c.conversation_id,
                c.wander_range, c.direction
                FROM character c
                LEFT JOIN area a
                ON a.area_id = c.area_id
                AND a.map_id = c.map_id
                WHERE COALESCE(c.conversation_id, 0) < 1000 
                AND c.map_id = 
                """ + this.mapId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                CharacterModel currentModel = new CharacterModel(
                    rs.getString("sprite_name"), 
                    rs.getInt("x"), 
                    rs.getInt("y"),
                    rs.getInt("conversation_id"),
                    rs.getInt("character_id"),
                    rs.getInt("wander_range"),
                    Utils.getDirection(rs.getInt("direction"))
                );
                
                currentModel.setOverworldModel(this);
                this.cpuModel.add(currentModel);
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /** 
     * load a list of conversation triggers that can appear on the current map
     */
    private void loadConversationTriggers()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
                SELECT c.conversation_id,
                c.x + IFNULL(a.min_x,0) as x,
                c.y + IFNULL(a.min_y,0) as y,
                c.character_id, c.approach_player,
                c.clear_conversation_id, c.auto_trigger
                FROM conversation_trigger c
                LEFT JOIN area a
                ON a.area_id = c.area_id
                AND a.map_id = c.map_id
                WHERE c.map_id = 
                """ + this.mapId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                ConversationTriggerModel currentModel = new ConversationTriggerModel(
                    rs.getInt("conversation_id"), 
                    getCharacterModel(rs.getInt("character_id")),
                    rs.getInt("x"), 
                    rs.getInt("y"),
                    rs.getInt("clear_conversation_id"),
                    rs.getInt("auto_trigger") == 1,
                    rs.getInt("approach_player") == 1
                );
                
                this.conversationTrigger.add(currentModel);
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * @param characterId the id of the character to be returned
     * @return CharacterModel the character object corresponding to the given id
     */
    private CharacterModel getCharacterModel(int characterId)
    {
        if (characterId == -1)
        {
            return playerModel;
        }
        for (CharacterModel current : this.cpuModel)
        {
            if (current.characterId == characterId)
            {
                return current;
            }
        }  

        return null;
    }

    public void battleComplete()
    {
        // restart music
        this.battle = false;
        this.areaId = -1;
        this.checkArea(playerModel.getX(), playerModel.getY());

        if (this.conversation != null)
        {
            this.conversation.setBattleComplete();
        }
    }

    /**
     * @return the id for the type of background to be displayed in battles
     */
    public byte getBattleBackgroundId()
    {
        if (this.playerModel.surf)
        {
            return 2;
        }

        return this.battleBackgroundId;
    }
}
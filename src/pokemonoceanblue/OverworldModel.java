package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OverworldModel extends BaseModel {
    public int mapId;
    public byte[][] tiles;
    public byte[][] tilesOverlay;
    public List<SpriteModel> mapObjects = new ArrayList<SpriteModel>(); 
    public List<CharacterModel> cpuModel = new ArrayList<CharacterModel>();
    public CharacterModel playerModel;
    private List<PortalModel> portals = new ArrayList<PortalModel>();
    public List<BerryModel> plantedBerries = new ArrayList<BerryModel>();
    public ConversationModel conversation;
    private App app;
    private WildPokemonModel wildPokemon;
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
    public String tilesSuffix;
    public int completeConversation = -1;
    public int questId;
    public NotificationModel notification;
    
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
        this.loadMapObjects();
        this.wildPokemon = new WildPokemonModel(this.mapId);
        this.loadPortals();
        this.loadCharacters();
        this.loadConversationTriggers();
        this.loadAreas();
        this.checkArea(playerModel.getX(), playerModel.getY());
        this.checkAutoTriggers(playerModel.getX(), playerModel.getY());

        if (this.tiles.length > 20 && this.mapId != 14 && this.mapId != 47)
        {
            // generate random weather when on a large map
            // since most large maps are outside
            //this.weather = (byte)(new Random().nextInt(5));
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

            String query = "SELECT map_template_id, overlay, tiles_suffix FROM map_template WHERE map_id = " + this.mapId;

            ResultSet rs = db.runQuery(query);

            mapTemplateId = rs.getInt("map_template_id");
            overlay = rs.getInt("overlay") == 1;
            tiles = readMapFileAux("map" + mapTemplateId);
            if (overlay)
            {
                tilesOverlay = readMapFileAux("map" + mapTemplateId + "overlay");
            }
            this.tilesSuffix = rs.getString("tiles_suffix");
            if (this.tilesSuffix.equals("0"))
            {
                this.tilesSuffix = "";
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
                int n = rand.nextInt(200);
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
            if (this.conversation.getBattleId() == -1)
            {
                this.removeCharacter = this.conversation.removeCharacter(this.cpuModel, this.conversationTrigger);
            }
            
            this.mugshotCharacter =  this.conversation.getMugshotCharacter();
            this.mugshotBackground =  this.conversation.getMugshotBackground();
            this.conversation.update();
            int characterId = this.conversation.getMovementCharacterId();

            // delete the conversation if it is over
            if (this.conversation.isComplete())
            {
                this.completeConversation = this.conversation.conversationId;
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
            // get items 
            else if (this.conversation.getItem() != null)
            {
                this.inventoryModel.addItem(this.conversation.getItem());
            }
        }
        else
        {
            this.mugshotCharacter = null;
            this.mugshotBackground = null;
            this.removeCharacter = false;
        }

        // clear out any expired berries
        for (int i = this.plantedBerries.size() - 1; i >= 0; i--)
        {
            if (this.plantedBerries.get(i).isExpired())
            {
                this.plantedBerries.remove(i);
            }
        }

        // update notification
        if (this.notification != null)
        {
            this.notification.update();
            if (this.notification.isComplete())
            {
                this.notification = null;
            }
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
        if (!this.locationCheck(x, y)
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
        if (this.conversation != null 
            && (this.conversation.getMovementCharacterId() != characterId
                || this.conversation.getBattleId() >= 0))
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
    public void checkAction()
    {
        // get the coordinates the player is interacting with
        int x = Utils.applyXOffset(this.playerModel.getX(), this.playerModel.getDirection());
        int y = Utils.applyYOffset(this.playerModel.getY(), this.playerModel.getDirection());
        
        // if already in a conversation, check if it's time to move on to next dialog
        if (this.conversation != null)
        {
            // start a battle
            if (this.conversation.getBattleId() >= 0)
            {
                // rock smash
                if (this.conversation.getBattleId() == 999)
                {
                    // start an encounter with a rock type
                    int pokemonId = this.wildPokemon.getPokemonId(this.areaId, 999);
                    if (pokemonId > -1)
                    {
                        Random rand = new Random();
                        this.app.createWildBattle(pokemonId, 2 + rand.nextInt(4), false);
                    }
                }
                else
                {
                    this.app.createTrainerBattle(this.conversation.getBattleId());
                }
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
                        // make the CPU facing the player
                        cpu.setDirection(Utils.getDirection(this.playerModel.getX() - cpu.getX(), this.playerModel.getY() - cpu.getY()));

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
                    // talk to day care lady
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

            // check for a berry ready to be harvested
            for (int i = this.plantedBerries.size() - 1; i >= 0; i--)
            {
                if (this.plantedBerries.get(i).isHarvestable())
                {
                    // remove the plant and add the berries to the player's inventory
                    ItemModel item = new ItemModel(this.plantedBerries.get(i).berryId, 3);
                    this.conversation = new ConversationModel("Player harvested " + item.quantity + " " + item.name + " from the plant!", item);
                    this.plantedBerries.remove(i);
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
        // add a new quest if one is available
        this.questId = this.conversation.getQuestId();

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

                    if (area.name != null)
                    {
                        this.notification = new NotificationModel(area.name, 1);
                    }
                    
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
        boolean canEncounterWildPkmn = true;
        for (int i = 0; i < inventoryModel.items[InventoryModel.KEY_ITEMS].size(); i++) 
        {
            if (inventoryModel.items[InventoryModel.KEY_ITEMS].get(i).itemId == 188 && inventoryModel.items[InventoryModel.KEY_ITEMS].get(i).enabled)
            {
                canEncounterWildPkmn = false;
                break;
            }
        }
        if (canEncounterWildPkmn)
        {
            Random rand = new Random();
            if (rand.nextInt(5) == 1)
            {
                int pokemonId = this.wildPokemon.getPokemonId(this.areaId, this.tiles[y][x]);
                if (pokemonId > -1)
                {
                    this.app.createWildBattle(pokemonId, 2 + rand.nextInt(4), false);
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
            this.textOptions = new String[]{"Pokedex", "Pokemon", "Bag", "Quests", "Achievements", "Save"};
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
        else if (this.textOptions[this.textOptionIndex] == "Quests")
        {
            app.openQuests();
            this.openMenu();
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
                ORDER BY mo.y + IFNULL(a.min_y,0) + IFNULL(mo.y_adjust,0) ASC, mo.x ASC
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
            SELECT 
                p.x + IFNULL(a.min_x,0) AS x,
                p.y + IFNULL(a.min_y,0) AS y,
                p.dest_map_id,
                p.dest_x + IFNULL(dest.min_x,0) AS dest_x,
                p.dest_y + IFNULL(dest.min_y,0) AS dest_y,
                p.x_offset,
                p.y_offset
            FROM (
                SELECT 
                    map_id, 
                    area_id,
                    x, 
                    y,
                    dest_map_id, 
                    dest_area_id,
                    dest_x + dest_x_offset AS dest_x,
                    dest_y + dest_y_offset AS dest_y,
                    dest_x_offset AS x_offset,
                    dest_y_offset AS y_offset
                FROM portal 
                UNION ALL
                SELECT 
                    dest_map_id AS map_id, 
                    dest_area_id AS area_id, 
                    dest_x AS x, 
                    dest_y AS y,
                    map_id AS dest_map_id, 
                    area_id AS dest_area_id,
                    x + x_offset AS dest_x,
                    y + y_offset AS dest_y,
                    x_offset,
                    y_offset
                FROM portal 
            ) p
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
                    rs.getInt("dest_y"),
                    Utils.getDirection(rs.getInt("x_offset"), rs.getInt("y_offset"))
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
                WHERE (COALESCE(c.conversation_id, 0) < 1000 OR COALESCE(c.conversation_id, 0) > 1999)
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
        this.areaId = -1;
        this.checkArea(playerModel.getX(), playerModel.getY());

        if (this.conversation != null)
        {
            // check if a character should disappear before reloading the overworld
            // for example, legendaries that appear in the overworld should
            // disappear after battling them
            this.conversation.removeCharacter(this.cpuModel, this.conversationTrigger);
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

    /**
     * Immediately starts a conversation
     * @param conversationId idenifier for the conversation
     * @param direction the direction the player should be facing
     * @param character the character the player is talking with, or null
     */
    public void startConversation(int conversationId, Direction direction, CharacterModel character)
    {
        // start the conversation
        if (direction != null)
        {
            this.playerModel.setDirection(direction);
        }
        this.conversation = new ConversationModel(conversationId, this.playerModel, character, false);
        this.checkConversationAction();
        this.actionCounter = 15;
    }

    public boolean setItem(int itemId)
    {
        int x = Utils.applyXOffset(this.playerModel.getX(), this.playerModel.getDirection());
        int y = Utils.applyYOffset(this.playerModel.getY(), this.playerModel.getDirection());

        if (itemId >= 100 && locationCheck(x, y) && this.tiles[y][x] >= 105 && this.tiles[y][x] <= 107)
        {
            this.plantedBerries.add(new BerryModel(x, y, itemId, System.currentTimeMillis()));
            return true;
        }
        else
        {
            this.conversation = new ConversationModel("That item can't be used here!", null);
            return false;
        } 
    }

    /**
     * Checks if the given x and y are a valid position on the map
     * @param x x-coordinate to check
     * @param y y-coordinate to check
     * @return True if that is a valid map coordinate
     */
    public boolean locationCheck(int x, int y)
    {
        return (
            x >= 0 
            && y >= 0
            && y < this.tiles.length
            && x < this.tiles[y].length
        );
    }
}
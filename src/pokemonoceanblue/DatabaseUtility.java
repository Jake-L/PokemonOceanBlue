package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.List;

public class DatabaseUtility 
{    
    public static Connection conn = null;
    // db parameters
    String url;

    /** 
     * Constructor
     */
    public DatabaseUtility()
    {
        if (conn == null)
        {
            try
            {
                this.url =  "jdbc:sqlite::resource:" + this.getClass().getResource("/database/pokemon.db");
                
                // create a connection to the database
                conn = DriverManager.getConnection(this.url);

                conn.createStatement().execute("PRAGMA foreign_keys = ON");
                
                System.out.println("Connection to SQLite has been established.");
            } 
            catch (SQLException e) 
            {
                System.out.println(e.getMessage());
            } 
        }
        
    }

    /** 
     * Calls functions to create tables and fill them with data
     */
    public void prepareDatabase()
    {
        try
        {
            createTables();
        } 
        catch (SQLException e) 
        {
            System.out.println(e.getMessage());
        } 
    }

    /** 
     * Creates all the tables and calls functions to fill them with data
     */
    private void createTables() throws SQLException
    {
        String query;
        String path;

        // remove all the existing tables first
        String[] table_list = {
            "player_pokemon", "player_location", "conversation_options",
            "evolution_methods",  "pokemon_moves", "pokemon_location", 
            "conversation",  "type_effectiveness", "items", "battle_reward",
            "portal", "map_object", "area", "character", "move_stat_effect",
            "conversation_trigger", "achievements", "battle",
            "player_pokedex", "objective_task", "objective", 
            "moves",
            "map_template",
            "move_effect", 
            "pokemon",
        };

        for (String t : table_list)
        {
            query = "DROP TABLE IF EXISTS " + t;
            runUpdate(query);
        }

        conn.setAutoCommit(false);

        String dataTypes[];

        //==================================================================================
        // each Pokemon's name, stats, types
        query = """
                CREATE TABLE pokemon (
                    pokemon_id INT PRIMARY KEY,
                    name TEXT NOT NULL,
                    type1 INT NOT NULL,
                    type2 INT NULL DEFAULT 0,
                    hp INT NOT NULL,
                    attack INT NOT NULL,
                    defense INT NOT NULL,
                    special_attack INT NOT NULL,
                    special_defense INT NOT NULL,
                    speed INT NOT NULL,
                    iv_gain INT NOT NULL,
                    capture_rate INT NOT NULL,
                    base_pokemon_id INT NOT NULL,
                    [description] VARCHAR(130) NULL)
                """;
        runUpdate(query);

        // fill pokemon table with data
        path = "/rawdata/pokemon.csv";
        query = """
                INSERT INTO pokemon (
                    pokemon_id, name,
                    type1, type2,
                    hp, attack,
                    defense, special_attack,
                    special_defense, speed,
                    iv_gain, capture_rate,
                    base_pokemon_id, [description])
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "String", "int", "int", "int", "int", 
            "int", "int", "int", "int", "int", "int", "int", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // stats that are increased or decreased by using a move
        query = """
                CREATE TABLE move_effect (
                    effect_id INT PRIMARY KEY,
                    target_type INT NOT NULL,
                    removal_condition INT NOT NULL,
                    counter_min INT NOT NULL,
                    counter_max INT NOT NULL,
                    text VARCHAR(100) NULL)
                """;
        runUpdate(query);

        // fill move_stat_effect table with data
        path = "/rawdata/moveEffect.csv";
        query = """
                INSERT INTO move_effect (
                    effect_id, target_type, removal_condition, counter_min, counter_max, text)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "int", "int", "int", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // move's name, damage, accuracy, type
        query = """
                CREATE TABLE moves(
                    move_id INT PRIMARY KEY,
                    name VARCHAR(30) NOT NULL,
                    type_id INT NOT NULL,
                    power INT NULL,
                    accuracy INT NULL,
                    priority INT NOT NULL,
                    damage_class_id INT NOT NULL,
                    target_id INT NOT NULL,
                    flinch_chance INT NULL,
                    effect_chance INT NULL,
                    ailment_id INT NULL,
                    recoil INT NULL,
                    effect_id INT NOT NULL)
                """;
        runUpdate(query);

        // fill moves table with data
        path = "/rawdata/moves.csv";
        query = """
                INSERT INTO moves (
                    move_id, name, type_id, 
                    power, accuracy, priority,
                    damage_class_id, target_id,
                    flinch_chance, effect_chance,
                    ailment_id, recoil, effect_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "String", "int", "int", "int", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // each move a Pokemon can learn and at which level
        query = """
                CREATE TABLE pokemon_moves(
                    pokemon_id INT NOT NULL,
                    move_id INT NOT NULL,
                    level INT NOT NULL,
                    FOREIGN KEY(pokemon_id) REFERENCES pokemon(pokemon_id),
                    FOREIGN KEY(move_id) REFERENCES moves(move_id))
                """;
        runUpdate(query);

        // fill pokemon moves table with data
        path = "/rawdata/pokemonMoves.csv";
        query = """
                INSERT INTO pokemon_moves (
                    pokemon_id, move_id, level)
                VALUES (?, ?, ?)
                """;
       
        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // how specific Pokemon evolve, such as by level, item, or trade
        query = """
                CREATE TABLE evolution_methods(
                    pre_species_id INT NOT NULL,
                    evolved_species_id INT NOT NULL,
                    minimum_level INT NULL,
                    minimum_happiness INT NULL,
                    trigger_item_id INT NULL,
                    gender_id INT NULL,
                    map_id INT NULL,
                    held_item_id INT NULL,
                    time_of_day INT NULL,
                    relative_physical_stats INT NULL,
                    enemy_minimum_level INT NOT NULL,
                    FOREIGN KEY(pre_species_id) REFERENCES pokemon(pokemon_id),
                    FOREIGN KEY(evolved_species_id) REFERENCES pokemon(pokemon_id))
                """;
        runUpdate(query);

        // fill pokemon moves table with data
        path = "/rawdata/evolutionMethod.csv";
        query = """
                INSERT INTO evolution_methods (
                    pre_species_id, evolved_species_id, minimum_level, minimum_happiness, trigger_item_id, 
                    gender_id, map_id, held_item_id, time_of_day, relative_physical_stats, enemy_minimum_level)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
       
        dataTypes = new String[] {"int", "int", "int", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // re-use the same tile layout for multiple maps
        query = """
                CREATE TABLE map_template(
                    map_id INT PRIMARY KEY,
                    map_template_id INT NOT NULL,
                    overlay INT NULL,
                    tiles_suffix VARCHAR(10) NULL)
                """;
        runUpdate(query);

        // fills map_template table with data
        path = "/rawdata/mapTemplate.csv";
        query = """
                INSERT INTO map_template (
                    map_id, map_template_id, overlay, tiles_suffix)
                VALUES (?, ?, ?, ?)
                """;
        
        dataTypes = new String[] {"int", "int", "int", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // divides a map into areas, such as different routes or cities
        query = """
                CREATE TABLE area (
                    map_id INT NOT NULL,
                    area_id INT NOT NULL,
                    name VARCHAR(20) NOT NULL,
                    min_x INT NOT NULL,
                    max_x INT NOT NULL,
                    min_y INT NOT NULL,
                    max_y INT NOT NULL,
                    music_id INT NOT NULL,
                    battle_background_id INT NOT NULL,
                    FOREIGN KEY(map_id) REFERENCES map_template(map_id))
                """;
        runUpdate(query);

        // fills area table with data
        path = "/rawdata/areas.csv";
        query = """
                INSERT INTO area (
                    map_id, area_id, name, 
                    min_x, max_x,
                    min_y, max_y,
                    music_id, battle_background_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        
        dataTypes = new String[] {"int", "int", "String", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // the various objects that appear on the map, like trees and houses
        query = """
                CREATE TABLE map_object (
                    map_id INT NOT NULL,
                    area_id INT NOT NULL,
                    name VARCHAR(20) NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    y_adjust INT NULL DEFAULT 0,
                    PRIMARY KEY (map_id, area_id, x, y),
                    FOREIGN KEY(map_id) REFERENCES map_template(map_id))
                """;
        runUpdate(query);

        // fills map objects table with data
        path = "/rawdata/mapObjects.csv";
        query = """
                INSERT INTO map_object (
                    map_id, area_id, name, 
                    x, y, y_adjust) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        
        dataTypes = new String[] {"int", "int", "String", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // portals that teleport players from one map to another
        query = """
                CREATE TABLE portal (
                    map_id INT NOT NULL,
                    area_id INT NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    x_offset INT NOT NULL,
                    y_offset INT NOT NULL,
                    dest_map_id INT NOT NULL,
                    dest_area_id INT NOT NULL,
                    dest_x INT NOT NULL,
                    dest_y INT NOT NULL,
                    dest_x_offset INT NOT NULL,
                    dest_y_offset INT NOT NULL,
                    FOREIGN KEY(map_id) REFERENCES map_template(map_id),
                    FOREIGN KEY(dest_map_id) REFERENCES map_template(map_id))
                """;
        runUpdate(query);

        // fills portal table with data
        path = "/rawdata/portals.csv";
        query = """
                INSERT INTO portal (
                    map_id, area_id, 
                    x, y, x_offset, y_offset,
                    dest_map_id, dest_area_id,
                    dest_x, dest_y, dest_x_offset, dest_y_offset)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        
        dataTypes = new String[] {"int", "int", "int", "int", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // the map location where a Pokemon can be found
        // and in what type of tile, such as in grass or water
        query = """
                CREATE TABLE pokemon_location(
                    map_id INT NOT NULL,
                    area_id INT NOT NULL,
                    pokemon_id INT NOT NULL,
                    tile_id INT NOT NULL,
                    FOREIGN KEY(map_id) REFERENCES map_template(map_id),
                    FOREIGN KEY(pokemon_id) REFERENCES pokemon(pokemon_id))
                """;
        runUpdate(query);

        // fills pokemon location table with data
        path = "/rawdata/pokemonLocation.csv";
        query = """
                INSERT INTO pokemon_location (
                    map_id, 
                    area_id,
                    pokemon_id,
                    tile_id)
                VALUES (?, ?, ?, ?)
                """;
        
        dataTypes = new String[] {"int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // stats that are increased or decreased by using a move
        query = """
                CREATE TABLE move_stat_effect(
                move_id INT NOT NULL,
                stat_id INT NOT NULL,
                stat_change INT NOT NULL,
                FOREIGN KEY(move_id) REFERENCES moves(move_id))
                """;
        runUpdate(query);

        // fill move_stat_effect table with data
        path = "/rawdata/moveStatEffect.csv";
        query = """
                INSERT INTO move_stat_effect (
                    move_id, stat_id, stat_change)
                VALUES (?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the multipliers for different types
        query = """
                CREATE TABLE type_effectiveness(
                    src_type_id INT NOT NULL,
                    target_type_id INT NOT NULL,
                    damage_factor FLOAT NOT NuLL,
                    PRIMARY KEY (src_type_id, target_type_id))
                """;
        runUpdate(query);

        // fill type effectiveness table with data
        path = "/rawdata/typeEffectiveness.csv";
        query = """
                INSERT INTO type_effectiveness (
                    src_type_id, target_type_id, damage_factor)
                VALUES (?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "float"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // all the text displayed in conversations
        query = """
                CREATE TABLE conversation (
                    conversation_id INT NOT NULL,
                    conversation_event_id INT NOT NULL,
                    text VARCHAR(100) NULL,
                    battle_id INT DEFAULT -1,
                    heal_team INT DEFAULT 0,
                    character_id INT DEFAULT -1,
                    movement_direction INT DEFAULT -1,
                    option_id INT DEFAULT -1,
                    next_conversation_event_id INT DEFAULT -1, 
                    gift_pokemon_id INT DEFAULT -1,
                    gift_pokemon_level INT DEFAULT -1,
                    new_conversation_id INT DEFAULT -1,
                    mugshot_character VARCHAR(20) NULL,
                    mugshot_background VARCHAR(20) NULL,
                    music_id INT NULL DEFAULT -1,
                    shop_id INT NULL)
                """;
        runUpdate(query);

        // fill conversation table with data
        path = "/rawdata/conversation.csv";
        query = """
                INSERT INTO conversation (
                    conversation_id, conversation_event_id, text,
                    battle_id, heal_team, character_id, movement_direction,
                    option_id, next_conversation_event_id,
                    gift_pokemon_id, gift_pokemon_level,
                    new_conversation_id,
                    mugshot_character, mugshot_background, music_id,
                    shop_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "String", "int", "int", "int", "int", "int", 
            "int", "int", "int", "int", "String", "String", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // options displayed in conversations
        query = """
                CREATE TABLE conversation_options(
                    option_id INT NOT NULL,
                    text VARCHAR(100) NOT NULL,
                    next_conversation_event_id INT NOT NULL)
                """;
        runUpdate(query);

        // fill conversation table with data
        path = "/rawdata/conversationOption.csv";
        query = """
                INSERT INTO conversation_options (
                    option_id, text, next_conversation_event_id)
                VALUES (?, ?, ?)
                """;

        dataTypes = new String[] {"int", "String", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // triggers to start conversations
        query = """
                CREATE TABLE conversation_trigger(
                map_id INT NOT NULL,
                area_id INT NOT NULL,
                x INT NOT NULL,
                y INT NOT NULL,
                conversation_id INT NOT NULL,
                character_id INT NOT NULL,
                clear_conversation_id INT NOT NULL,
                auto_trigger INT NOT NULL,
                approach_player INT NOT NULL,
                FOREIGN KEY(map_id) REFERENCES map_template(map_id))
                """;
        runUpdate(query);

        // fill conversation table with data
        path = "/rawdata/conversationTriggers.csv";
        query = """
                INSERT INTO conversation_trigger (
                    map_id, area_id, x, y, conversation_id,
                    character_id, clear_conversation_id,
                    auto_trigger, approach_player)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store all the items
        query = """
                CREATE TABLE items(
                    item_id INT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    category_id INT NOT NULL,
                    cost INT NOT NULL,
                    [description] VARCHAR(150) NOT NULL)
                """;
        runUpdate(query);

        // fill items table with data
        path = "/rawdata/items.csv";
        query = """
                INSERT INTO items (
                    item_id, name, category_id, cost, [description])
                VALUES (?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "String", "int", "int", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the teams used by trainers in battle
        query = """
                CREATE TABLE battle(
                    battle_id INT NOT NULL,
                    pokemon_id INT NOT NULL,
                    level INT NOT NULL,
                    FOREIGN KEY(pokemon_id) REFERENCES pokemon(pokemon_id))
                """;
        runUpdate(query);

        // fill battle table with data
        path = "/rawdata/battle.csv";
        query = """
                INSERT INTO battle (
                    battle_id, pokemon_id, level) 
                VALUES (?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the characters that appear in the overworld
        query = """
                CREATE TABLE character(
                    character_id INT NOT NULL,
                    map_id INT NOT NULL,
                    area_id INT NOT NULL,
                    name VARCHAR(50) NULL,
                    sprite_name VARCHAR(50) NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    conversation_id INT NOT NULL,
                    wander_range INT NOT NULL,
                    direction INT NULL,
                    music_id INT NULL,
                    FOREIGN KEY(map_id) REFERENCES map_template(map_id))
                """;
        runUpdate(query);

        // fill battle table with data
        path = "/rawdata/characters.csv";
        query = """
                INSERT INTO character (
                    character_id, map_id, area_id,
                    name, sprite_name, x, y,
                    conversation_id, wander_range, direction,
                    music_id) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "int", "String", "String", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // save the player's Pokemon
        query = """
                CREATE TABLE player_pokemon (
                    pokemon_id INT NOT NULL,
                    xp INT NOT NULL,
                    shiny INT NOT NULL,
                    gender_id INT NOT NULL,
                    current_hp INT NOT NULL,
                    iv_0 INT NOT NULL,
                    iv_1 INT NOT NULL,
                    iv_2 INT NOT NULL,
                    iv_3 INT NOT NULL,
                    iv_4 INT NOT NULL,
                    iv_5 INT NOT NULL,
                    status_effect INT NOT NULL,
                    happiness INT NOT NULL,
                    step_counter INT NOT NULL,
                    move_1 INT NULL,
                    move_2 INT NULL,
                    move_3 INT NULL,
                    move_4 INT NULL,
                    pokemon_index INT NOT NULL,
                    FOREIGN KEY(pokemon_id) REFERENCES pokemon(pokemon_id))
                """;
        runUpdate(query);

        //==================================================================================
        // save the player's location
        query = """
                CREATE TABLE player_location (
                    map_id INT NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    FOREIGN KEY(map_id) REFERENCES map_template(map_id))
                """;
        runUpdate(query);

        // set the default location (inside player's house)
        query = """
                INSERT INTO player_location (
                    map_id, x, y)
                VALUES (1, 3, 3)
                """;
        runUpdate(query);

        //==================================================================================
        // save the player's pokedex progress
        query = """
                CREATE TABLE player_pokedex (
                    pokemon_id INT NOT NULL,
                    number_caught INT NOT NULL,
                    FOREIGN KEY(pokemon_id) REFERENCES pokemon(pokemon_id))
                """;
        runUpdate(query);

        //==================================================================================
        // each data for each achievement/quest
        query = """
                CREATE TABLE objective (
                    objective_id INT PRIMARY KEY,
                    name VARCHAR(30) NOT NULL,
                    description VARCHAR(80) NULL,
                    reward_id INT NULL,
                    reward_quantity INT NULL,
                    icon VARCHAR(20) NULL)
                """;
        runUpdate(query);

        // fill objective table with data
        path = "/rawdata/objective.csv";
        query = """
                INSERT INTO objective (
                    objective_id, name, description, 
                    reward_id, reward_quantity, icon)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "String", "String", "int", "int", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // each data for each achievement/quest task
        query = """
                CREATE TABLE objective_task (
                    objective_id INT, 
                    task_id INT,
                    objective_type VARCHAR(30) NOT NULL,
                    counter INT NOT NULL, 
                    required_value INT NOT NULL, 
                    identifier VARCHAR(30) NOT NULL,
                    description VARCHAR(80) NOT NULL)
                """;
        runUpdate(query);

        // fill objective_task table with data
        path = "/rawdata/objective_task.csv";
        query = """
                INSERT INTO objective_task (
                    objective_id, task_id, 
                    objective_type, counter, 
                    required_value, identifier,
                    description)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "String", "int", "int", "String", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the reward earned by the player for winning a battle
        // each battle can only give a single reward
        query = """
                CREATE TABLE battle_reward (
                    battle_id INT NOT NULL,
                    item_id INT NOT NULL,
                    quantity INT NOT NULL)
                """;
        runUpdate(query);

        // fill battle table with data
        path = "/rawdata/battle_reward.csv";
        query = """
                INSERT INTO battle_reward (
                    battle_id, item_id, quantity) 
                VALUES (?, ?, ?)
                """;

        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        conn.setAutoCommit(true);
    }

    /** 
     * Fills the pokemon location table with data
     */
    private void loadTable(String path, String query, String[] dataTypes)
    {
        try
        {
            InputStreamReader istreamReader = new InputStreamReader(this.getClass().getResourceAsStream(path),"UTF-8");
            BufferedReader br = new BufferedReader(istreamReader);

            // skip the first line which just has column names
            String line = br.readLine();
            line = br.readLine();
            String[] data;
            PreparedStatement statement;

            statement = conn.prepareStatement(query);

            while (line != null)
            {
                data = line.split(",");

                for (int i = 0; i < dataTypes.length; i++)
                {
                    if (dataTypes[i] == "int")
                    {
                        if (i >= data.length || data[i].equals(""))
                        {
                            statement.setInt(i+1, -1);
                        }
                        else
                        {
                            statement.setInt(i+1, Integer.parseInt(data[i]));
                        }                        
                    }
                    else if (dataTypes[i] == "String")
                    {
                        if (i >= data.length || data[i].equals(""))
                        {
                            statement.setString(i+1, "");
                        }
                        else
                        {
                            statement.setString(i+1, data[i]);
                        }    
                    }
                    else if (dataTypes[i] == "float")
                    {
                        if (i >= data.length || data[i].equals(""))
                        {
                            statement.setFloat(i+1, -1);
                        }
                        else
                        {
                            statement.setFloat(i+1, Float.parseFloat(data[i]));
                        }   
                    }
                }  

                statement.addBatch();

                line = br.readLine();
            }

            statement.executeBatch();
            br.close();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** 
     * Runs INSERT or DELETE queries
     * @param query the SQL query to be executed
     */
    public void runUpdate(String query)
    {
        try
        {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }

    /** 
     * Runs SELECT queries and returns the result
     * @param query the SQL query to be executed
     * @return the resultset containing the results of the query
     */
    public ResultSet runQuery(String query) throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;   
    }

    /**
     * Save the player's Pokemon
     * @param team a list of the player's Pokemon
     * @param pokemonStorage a list of the player's Pokemon storage
     */
    public void savePokemon(List<PokemonModel> team, List<PokemonModel> pokemonStorage)
    {
        try
        {
            conn.setAutoCommit(false);
            
            // clear out past save data
            this.runUpdate("DELETE FROM player_pokemon");

            String query = """
                INSERT INTO player_pokemon (
                    pokemon_id, xp, shiny, gender_id, current_hp,
                    iv_0, iv_1, iv_2, iv_3, iv_4, iv_5,
                    status_effect, happiness, step_counter,
                    move_1, move_2, move_3, move_4,
                    pokemon_index)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            PreparedStatement statement;

            statement = conn.prepareStatement(query);

            // store the player's team
            for (int i = 0; i < team.size(); i++)
            {
                team.get(i).toSQL(statement, i);
                statement.addBatch();
            }
            
            // store the Pokemon in the Pokemon storage
            for (int i = 0; i < pokemonStorage.size(); i++)
            {
                pokemonStorage.get(i).toSQL(statement, 6 + i);
                statement.addBatch();
            }

            statement.executeBatch();
            conn.setAutoCommit(true);
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Save the player's map location
     * @param mapId the current map
     * @param x player's x position
     * @param y player's y position
     */
    public void savePlayerLocation(int mapId, int x, int y)
    {
        // clear out past save data
        this.runUpdate("DELETE FROM player_location");

        try
        {
            String query = """
                INSERT INTO player_location (
                    map_id, x, y)
                VALUES (?, ?, ?)
                """;

            PreparedStatement statement;

            statement = conn.prepareStatement(query);

            statement.setInt(1, mapId);
            statement.setInt(2, x);
            statement.setInt(3, y);
            statement.executeUpdate();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Save the number of times the player has caught each Pokemon
     * @param caughtPokemon the number of times the player has caught each Pokemon
     */
    public void savePokedex(int[] caughtPokemon)
    {
        try
        {
            conn.setAutoCommit(false);

            // clear out past save data
            this.runUpdate("DELETE FROM player_pokedex");

            String query = """
                INSERT INTO player_pokedex (
                    pokemon_id, number_caught)
                VALUES (?, ?)
                """;

            PreparedStatement statement;

            statement = conn.prepareStatement(query);

            // store the Pokemon in the Pokemon storage
            for (int i = 1; i < caughtPokemon.length; i++)
            {
                if (caughtPokemon[i] > 0)
                {
                    statement.setInt(1, i);
                    statement.setInt(2, caughtPokemon[i]);
                    statement.addBatch();
                }
            }

            statement.executeBatch();
            conn.setAutoCommit(true);
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** 
     * Closes the database connection
     */
    public void close()
    {
        try 
        {
            // close the connection
            if (conn != null) 
            {
                conn.close();
            }
        } 
        catch (SQLException ex) 
        {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * @param battleId the battle with the legendary
     * @return an array with the pokemon id and level of the legendary
     */
    public int[] getLegendaryData(int battleId)
    {
        try 
        {
            String query = "SELECT pokemon_id, level FROM battle WHERE battle_id = " + battleId;
            ResultSet rs = this.runQuery(query);
            return new int[]{rs.getInt(1), rs.getInt(2)};
        } 
        catch (SQLException ex) 
        {
            System.out.println(ex.getMessage());
        }

        return new int[]{0,0};
    }
}
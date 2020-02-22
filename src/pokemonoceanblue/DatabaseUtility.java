package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;

public class DatabaseUtility 
{    
    Connection conn = null;
    // db parameters
    String url = "jdbc:sqlite:src/database/pokemon.db";

    /** 
     * Constructor
     */
    public DatabaseUtility()
    {
        try
        {
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
        } 
        catch (SQLException e) 
        {
            System.out.println(e.getMessage());
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
            "evolution_methods", "pokemon", "pokemon_moves", "pokemon_location", 
            "conversation", "moves", "type_effectiveness", "items", "battle",
            "portal", "map_object", "area", "character", "move_stat_effect",
            "evolution_methods", "conversation_trigger", "map_template",
            "conversation_options"
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
        query = "CREATE TABLE pokemon("
                + "id INT NOT NULL, "
                + "name TEXT NOT NULL, "
                + "type1 INT NOT NULL, "
                + "type2 INT NULL DEFAULT 0, "
                + "hp INT NOT NULL, "
                + "attack INT NOT NULL, "
                + "defense INT NOT NULL, "
                + "special_attack INT NOT NULL, "
                + "special_defense INT NOT NULL, "
                + "speed INT NOT NULL, "
                + "iv_gain INT NOT NULL, "
                + "capture_rate INT NOT NULL)";
        runUpdate(query);

        // fill pokemon table with data
        path = "src/rawdata/pokemon.csv";
        query = "INSERT INTO pokemon ("
                    + "id, name, "
                    + "type1, type2, "
                    + "hp, attack, "
                    + "defense, special_attack, "
                    + "special_defense, speed, "
                    + "iv_gain, capture_rate)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int", "int", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // each move a Pokemon can learn and at which level
        query = "CREATE TABLE pokemon_moves("
                + "pokemon_id INT NOT NULL, "
                + "move_id INT NOT NULL, "
                + "level INT NOT NULL)";
        runUpdate(query);

        // fill pokemon moves table with data
        path = "src/rawdata/pokemonMoves.csv";
        query = "INSERT INTO pokemon_moves ("
                    + "pokemon_id, move_id, level)"
                    + "VALUES (?, ?, ?)";
       
        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // how specific Pokemon evolve, such as by level, item, or trade
        query = "CREATE TABLE evolution_methods("
                + "pre_species_id INT NOT NULL, "
                + "evolved_species_id INT NOT NULL, "
                + "minimum_level INT NULL, "
                + "minimum_happiness INT NULL)";
        runUpdate(query);

        // fill pokemon moves table with data
        path = "src/rawdata/evolutionMethods.csv";
        query = "INSERT INTO evolution_methods ("
                    + "pre_species_id, evolved_species_id, minimum_level, minimum_happiness)"
                    + "VALUES (?, ?, ?, ?)";
       
        dataTypes = new String[] {"int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // divides a map into areas, such as different routes or cities
        query = "CREATE TABLE area("
                + "map_id INT NOT NULL, "
                + "area_id INT NOT NULL, "
                + "name VARCHAR(20) NOT NULL, "
                + "min_x INT NOT NULL, "
                + "max_x INT NOT NULL, "
                + "min_y INT NOT NULL, "
                + "max_y INT NOT NULL, "
                + "music_id INT NOT NULL)";
        runUpdate(query);

        // fills area table with data
        path = "src/rawdata/areas.csv";
        query = "INSERT INTO area ("
                    + "map_id, area_id, name, " 
                    + "min_x, max_x, "
                    + "min_y, max_y, "
                    + "music_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        dataTypes = new String[] {"int", "int", "String", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // re-use the same tile layout for multiple maps
        query = "CREATE TABLE map_template("
                + "map_id INT NOT NULL, "
                + "map_template_id INT NOT NULL)";
        runUpdate(query);

        // fills map_template table with data
        path = "src/rawdata/mapTemplate.csv";
        query = "INSERT INTO map_template ("
                    + "map_id, map_template_id) "
                    + "VALUES (?, ?)";
        
        dataTypes = new String[] {"int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // the various objects that appear on the map, like trees and houses
        query = "CREATE TABLE map_object ("
                + "map_id INT NOT NULL, "
                + "area_id INT NOT NULL, "
                + "name VARCHAR(20) NOT NULL, "
                + "x INT NOT NULL, "
                + "y INT NOT NULL)";
        runUpdate(query);

        // fills map objects table with data
        path = "src/rawdata/mapObjects.csv";
        query = "INSERT INTO map_object ("
                    + "map_id, area_id, name, " 
                    + "x, y) "
                    + "VALUES (?, ?, ?, ?, ?)";
        
        dataTypes = new String[] {"int", "int", "String","int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // portals that teleport players from one map to another
        query = "CREATE TABLE portal ("
                + "map_id INT NOT NULL, "
                + "area_id INT NOT NULL, "
                + "x INT NOT NULL, "
                + "y INT NOT NULL, "
                + "dest_map_id INT NOT NULL, "
                + "dest_area_id INT NOT NULL, "
                + "dest_x INT NOT NULL, "
                + "dest_y INT NOT NULL)";
        runUpdate(query);

        // fills portal table with data
        path = "src/rawdata/portals.csv";
        query = "INSERT INTO portal ("
                    + "map_id, area_id, " 
                    + "x, y, "
                    + "dest_map_id, dest_area_id, "
                    + "dest_x, dest_y) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        dataTypes = new String[] {"int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // the map location where a Pokemon can be found
        // and in what type of tile, such as in grass or water
        query = "CREATE TABLE pokemon_location("
                + "map_id INT NOT NULL, "
                + "area_id INT NOT NULL, "
                + "pokemon_id INT NOT NULL, "
                + "tile_id INT NOT NULL)";
        runUpdate(query);

        // fills pokemon location table with data
        path = "src/rawdata/pokemonLocation.csv";
        query = "INSERT INTO pokemon_location ("
                    + "map_id, " 
                    + "area_id, "
                    + "pokemon_id, "
                    + "tile_id) "
                    + "VALUES (?, ?, ?, ?)";
        
        dataTypes = new String[] {"int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // move's name, damage, accuracy, type
        query = "CREATE TABLE moves("
                + "move_id INT NOT NULL, "
                + "name VARCHAR(30) NOT NULL, "
                + "type_id INT NOT NULL, "
                + "power INT NULL, "
                + "accuracy INT NULL, "
                + "priority INT NOT NULL, "
                + "damage_class_id INT NOT NULL, "
                + "target_id INT NOT NULL, "
                + "flinch_chance INT NULL, "
                + "effect_chance INT NULL, "
                + "ailment_id INT NULL)";
        runUpdate(query);

        // fill moves table with data
        path = "src/rawdata/moves.csv";
        query = "INSERT INTO moves ("
                    + "move_id, name, type_id, " 
                    + "power, accuracy, priority, "
                    + "damage_class_id, target_id, "
                    + "flinch_chance, effect_chance, "
                    + "ailment_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // stats that are increased or decreased by using a move
        query = "CREATE TABLE move_stat_effect("
                + "move_id INT NOT NULL, "
                + "stat_id INT NOT NULL, "
                + "stat_change INT NOT NULL)";
        runUpdate(query);

        // fill move_stat_effect table with data
        path = "src/rawdata/moveStatEffect.csv";
        query = "INSERT INTO move_stat_effect ("
                    + "move_id, stat_id, stat_change) "
                    + "VALUES (?, ?, ?)";

        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the multipliers for different types
        query = "CREATE TABLE type_effectiveness("
                + "src_type_id INT NOT NULL, "
                + "target_type_id INT NOT NULL, "
                + "damage_factor FLOAT NOT NuLL)";
        runUpdate(query);

        // fill type effectiveness table with data
        path = "src/rawdata/typeEffectiveness.csv";
        query = "INSERT INTO type_effectiveness ("
                    + "src_type_id, target_type_id, damage_factor)"
                    + "VALUES (?, ?, ?)";

        dataTypes = new String[] {"int", "int", "float"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // all the text displayed in conversations
        query = "CREATE TABLE conversation("
                + "conversation_id INT NOT NULL, "
                + "conversation_event_id INT NOT NULL, "
                + "text VARCHAR(100) NOT NULL, "
                + "battle_id INT DEFAULT -1, "
                + "heal_team INT DEFAULT 0, "
                + "character_id INT NOT NULL, "
                + "movement_direction INT DEFAULT -1, "
                + "option_id INT DEFAULT -1, "
                + "next_conversation_event_id INT DEFAULT -1, " 
                + "gift_pokemon_id INT DEFAULT -1, "
                + "gift_pokemon_level INT DEFAULT -1)";
        runUpdate(query);

        // fill conversation table with data
        path = "src/rawdata/conversation.csv";
        query = "INSERT INTO conversation ("
                + "conversation_id, conversation_event_id, text, "
                + "battle_id, heal_team, character_id, movement_direction, "
                + "option_id, next_conversation_event_id, "
                + "gift_pokemon_id, gift_pokemon_level)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "int", "String", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // options displayed in conversations
        query = "CREATE TABLE conversation_options("
                + "option_id INT NOT NULL, "
                + "text VARCHAR(100) NOT NULL, "
                + "next_conversation_event_id INT NOT NULL)";
        runUpdate(query);

        // fill conversation table with data
        path = "src/rawdata/conversationOption.csv";
        query = "INSERT INTO conversation_options ("
                + "option_id, text, next_conversation_event_id)"
                + "VALUES (?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // triggers to start conversations
        query = "CREATE TABLE conversation_trigger("
                + "map_id INT NOT NULL, "
                + "area_id INT NOT NULL, "
                + "x INT NOT NULL, "
                + "y INT NOT NULL, "
                + "conversation_id INT NOT NULL, "
                + "character_id INT NOT NULL, "
                + "clear_after_use INT NOT NULL, "
                + "auto_trigger INT NOT NULL)";
        runUpdate(query);

        // fill conversation table with data
        path = "src/rawdata/conversationTriggers.csv";
        query = "INSERT INTO conversation_trigger ("
                + "map_id, area_id, x, y, conversation_id, "
                + "character_id, clear_after_use, auto_trigger)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store all the items
        query = "CREATE TABLE items("
                + "item_id INT NOT NULL,"
                + "name VARCHAR(50) NOT NULL,"
                + "category_id INT NOT NULL,"
                + "cost INT NOT NULL,"
                + "[description] VARCHAR(150) NOT NULL)";
        runUpdate(query);

        // fill items table with data
        path = "src/rawdata/items.csv";
        query = "INSERT INTO items ("
                    + "item_id, name, category_id, cost, [description])"
                    + "VALUES (?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int", "int", "String"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the teams used by trainers in battle
        query = "CREATE TABLE battle("
                + "battle_id INT NOT NULL,"
                + "pokemon_id INT NOT NULL,"
                + "level INT NOT NULL)";
        runUpdate(query);

        // fill battle table with data
        path = "src/rawdata/battle.csv";
        query = "INSERT INTO battle ("
                    + "battle_id, pokemon_id, level) "
                    + "VALUES (?, ?, ?)";

        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        //==================================================================================
        // store the characters that appear in the overworld
        query = "CREATE TABLE character("
                + "character_id INT NOT NULL,"
                + "map_id INT NOT NULL,"
                + "area_id INT NOT NULL,"
                + "name VARCHAR(50) NULL,"
                + "sprite_name VARCHAR(50) NOT NULL,"
                + "x INT NOT NULL,"
                + "y INT NOT NULL,"
                + "conversation_id INT NOT NULL,"
                + "wander_range INT NOT NULL,"
                + "direction INT NULL)";
        runUpdate(query);

        // fill battle table with data
        path = "src/rawdata/characters.csv";
        query = "INSERT INTO character ("
                    + "character_id, map_id, area_id, "
                    + "name, sprite_name, x, y, "
                    + "conversation_id, wander_range, direction) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "int", "int", "String", "String", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        conn.commit();
    }

    /** 
     * Fills the pokemon location table with data
     */
    private void loadTable(String path, String query, String[] dataTypes)
    {
        try
        {
            InputStreamReader istreamReader = new InputStreamReader(new FileInputStream(path),"UTF-8");
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
                        if (i >= data.length)
                        {
                            // set null value, since there was no value
                            statement.setObject(i+1, null);
                        }
                        else
                        {
                            // use setObject to allow for null values
                            statement.setObject(i+1, data[i].equals("") ? null : Integer.parseInt(data[i]));
                        }
                    }
                    else if (dataTypes[i] == "String")
                    {
                        statement.setString(i+1, data[i]);
                    }
                    else if (dataTypes[i] == "float")
                    {
                        statement.setFloat(i+1, Float.parseFloat(data[i]));
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
     * Runs SELECT queries and returns the result
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
}
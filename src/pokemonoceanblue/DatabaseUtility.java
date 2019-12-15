package pokemonoceanblue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
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
            "conversation", "moves", "type_effectiveness", "items"
        };

        for (String t : table_list)
        {
            query = "DROP TABLE IF EXISTS " + t;
            runUpdate(query);
        }

        conn.setAutoCommit(false);

        String dataTypes[];

        // CREATE TABLE evolution_methods
        // how specific Pokemon evolve, such as by level, item, or trade

        // CREATE TABLE pokemon
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
                + "speed INT NOT NULL)";
        runUpdate(query);

        // fill pokemon table with data
        path = "src/rawdata/pokemon.csv";
        query = "INSERT INTO pokemon ("
                    + "id, name, "
                    + "type1, type2, "
                    + "hp, attack, "
                    + "defense, special_attack, "
                    + "special_defense, speed)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int", "int", "int", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        // CREATE TABLE pokemon_moves
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

        // CREATE TABLE pokemon_location
        // the map location where a Pokemon can be found
        // and in what type of tile, such as in grass or water
        query = "CREATE TABLE pokemon_location("
                + "pokemon_id INT NOT NULL, "
                + "map_id INT NOT NULL, "
                + "tile_id INT NOT NULL)";
        runUpdate(query);

        // fills pokemon location table with data
        path = "src/rawdata/pokemonLocation.csv";
        query = "INSERT INTO pokemon_location ("
                    + "map_id, " 
                    + "pokemon_id, "
                    + "tile_id) "
                    + "VALUES (?, ?, ?)";
        
        dataTypes = new String[] {"int", "int", "int"};
        loadTable(path, query, dataTypes);

        // CREATE TABLE moves
        // move's name, damage, accuracy, type
        query = "CREATE TABLE moves("
                + "move_id INT NOT NULL, "
                + "name VARCHAR(30) NOT NULL, "
                + "type_id INT NOT NULL, "
                + "power INT NULL, "
                + "accuracy INT NULL, "
                + "priority INT NOT NULL, "
                + "damage_class_id INT NOT NULL)";
        runUpdate(query);

        // fill moves table with data
        path = "src/rawdata/moves.csv";
        query = "INSERT INTO moves ("
                    + "move_id, name, type_id, " 
                    + "power, accuracy, priority, "
                    + "damage_class_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int", "int", "int", "int", "int"};
        loadTable(path, query, dataTypes);

        // CREATE TABLE move_effects
        // effect type, probability

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

        // CREATE TABLE conversation
        // all the text displayed in conversations
        query = "CREATE TABLE conversation("
                + "conversationId INT NOT NULL, "
                + "conversationEventId INT NOT NULL, "
                + "text VARCHAR(100) NOT NULL, "
                + "battleId INT NOT NULL)";
        runUpdate(query);

        // fill conversation table with data
        path = "src/rawdata/conversation.csv";
        query = "INSERT INTO conversation ("
                + "conversationId, conversationEventId, text, battleId)"
                + "VALUES (?, ?, ?, ?)";

        dataTypes = new String[] {"int", "int", "String", "int"};
        loadTable(path, query, dataTypes);

        // store all the items
        query = "CREATE TABLE items("
                + "item_id INT NOT NULL,"
                + "name VARCHAR(50) NOT NULL,"
                + "category_id INT NOT NULL,"
                + "cost INT NOT NULL)";
        runUpdate(query);

        // fill items table with data
        path = "src/rawdata/items.csv";
        query = "INSERT INTO items ("
                    + "item_id, name, category_id, cost)"
                    + "VALUES (?, ?, ?, ?)";

        dataTypes = new String[] {"int", "String", "int", "int"};
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
            BufferedReader br = getFileReader(path);

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
                        // use setObject to allow for null values
                        statement.setObject(i+1, data[i].equals("") ? null : Integer.parseInt(data[i]));
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
     * Creates a BufferedReader object with proper decoding
     * @param filename path to the filename to be read
     * @return A BufferedReader for reading the given file
     */
    private BufferedReader getFileReader(String filename) throws FileNotFoundException, IOException
    {
        // prepare to read the file and handle any encoding errors
        FileInputStream input = new FileInputStream(new File(filename));
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        InputStreamReader reader = new InputStreamReader(input, decoder);
        return new BufferedReader(reader);
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
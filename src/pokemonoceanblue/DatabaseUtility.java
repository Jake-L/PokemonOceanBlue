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

    }

    /** 
     * Calls functions to create tables and fill them with data
     */
    public void prepareDatabase()
    {
        try
        {
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");

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

        // remove all the existing tables first
        String[] table_list = {"evolution_methods", "pokemon", "pokemon_moves", "pokemon_location"};

        for (String t : table_list)
        {
            query = "DROP TABLE IF EXISTS " + t;
            runUpdate(query);
        }

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

        loadPokemonTable();

        // CREATE TABLE pokemon_moves
        // each move a Pokemon can learn and at which level
        query = "CREATE TABLE pokemon_moves("
                + "pokemon_id INT NOT NULL, "
                + "move_id INT NOT NULL, "
                + "level INT NOT NULL)";
        runUpdate(query);

        // CREATE TABLE pokemon_location
        // the map location where a Pokemon can be found
        // and in what type of tile, such as in grass or water
        query = "CREATE TABLE pokemon_location("
                + "pokemon_id INT NOT NULL, "
                + "map_id INT NOT NULL, "
                + "tile INT NOT NULL)";
        runUpdate(query);

        // CREATE TABLE moves
        // move's name, damage, accuracy, type

        // CREATE TABLE move_effects
        // effect type, probability
        
    }

    /** 
     * Fills the Pokemon table with data
     */
    private void loadPokemonTable() throws SQLException
    {
        try
        {
            // prepare to read the file and handle any encoding errors
            FileInputStream input = new FileInputStream(new File("src/rawdata/pokemon.csv"));
            CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            InputStreamReader reader = new InputStreamReader(input, decoder);
            BufferedReader br = new BufferedReader(reader);

            // skip the first line which just has column names
            String line = br.readLine();
            line = br.readLine();
            String query;
            String[] data;
            PreparedStatement statement;

            while (line != null)
            {
                data = line.split(",");

                query = "INSERT INTO pokemon ("
                    + "id, name, "
                    + "type1, type2, "
                    + "hp, attack, "
                    + "defense, special_attack, "
                    + "special_defense, speed)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                statement = conn.prepareStatement(query);

                if (data[3].equals(""))
                {
                    data[3] = "0";
                }

                statement.setInt(1, Integer.parseInt(data[0]));
                statement.setString(2, data[1]);
                statement.setInt(3, Integer.parseInt(data[2]));
                statement.setInt(4, Integer.parseInt(data[3]));
                statement.setInt(5, Integer.parseInt(data[4]));
                statement.setInt(6, Integer.parseInt(data[5]));
                statement.setInt(7, Integer.parseInt(data[6]));
                statement.setInt(8, Integer.parseInt(data[7]));
                statement.setInt(9, Integer.parseInt(data[8]));
                statement.setInt(10, Integer.parseInt(data[9]));

                statement.execute();

                line = br.readLine();
            }

            br.close();
        } 
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) 
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
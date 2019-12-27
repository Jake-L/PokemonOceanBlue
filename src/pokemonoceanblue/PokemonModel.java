package pokemonoceanblue;

import java.sql.*;

public class PokemonModel 
{
    int id;
    String name;
    int xp;
    int level;
    byte statusEffect;

    int[] types;

    int currentHP;
    int[] stats = new int[6];

    int[] ivs = new int[6];
    MoveModel[] moves;
    public final boolean shiny;
    
    /** 
     * Constructor
     * @param id the Pokemon's number from the national pokedex
     * @param level the Pokemon's current level
     */
    public PokemonModel(int id, int level, boolean shiny)
    {
        this.id = id;
        this.level = level;
        this.xp = (int) Math.pow(level, 3);
        this.shiny = shiny;

        this.loadStats();
        this.loadMoves();
    }

    /** 
     * Recalculates a Pokemon's level based off it's current experience
     */
    public void calcLevel()
    {
        this.level = (int) Math.pow(xp, 1.00 / 3.00);
    }

    /** 
     * Read the Pokemon's stats, types, etc from a database
     */
    private void loadStats()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM pokemon WHERE id = " + this.id;

            ResultSet rs = db.runQuery(query);

            this.name = rs.getString("name");

            // check if the Pokemon has one type or two
            if (rs.getInt("type2") == 0)
            {
                this.types = new int[1];
            }
            else
            {
                this.types = new int[2];
                this.types[1] = rs.getInt("type2");
            }

            this.types[0] = rs.getInt("type1");
            
            // set the Pokemon's stats
            this.stats[Stat.HP] = (int)Math.floor(2.0 * rs.getInt("hp") * this.level / 100) + this.level + 10;
            this.currentHP = this.stats[Stat.HP];

            String[] stats = {"hp", "attack","defense","special_attack","special_defense","speed"};

            for (int i = 1; i < stats.length; i++)
            {
                this.stats[i] = (int)Math.floor((2.0 * rs.getInt(stats[i]) + this.ivs[i]) * this.level / 100) + 5;
            }

            this.statusEffect = 0;
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /** 
     * Read the Pokemon's moves from a database
     */
    private void loadMoves()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT move_id FROM pokemon_moves WHERE pokemon_id = " + this.id + " LIMIT 4";

            ResultSet rs = db.runQuery(query);

            int index = 0;

            this.moves = new MoveModel[4];

            while(rs.next()) 
            {
                this.moves[index] = new MoveModel(rs.getInt("move_id"));
                index++;
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}
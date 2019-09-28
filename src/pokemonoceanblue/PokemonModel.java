package pokemonoceanblue;

import java.sql.*;

public class PokemonModel 
{
    int id;
    String name;
    int xp;
    int level;

    int[] types;

    int hp;
    int attack;
    int defense;
    int special_attack;
    int special_defense;
    int speed;
    
    /** 
     * Constructor
     * @param id the Pokemon's number from the national pokedex
     * @param level the Pokemon's current level
     */
    public PokemonModel(int id, int level)
    {
        this.id = id;
        this.level = level;
        this.xp = (int) Math.pow(level, 3);

        this.loadStats();
    }

    /** 
     * Recalculates a Pokemon's level based off it's current experience
     */
    private void calcLevel()
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
            this.hp = rs.getInt("hp");
            this.attack = rs.getInt("attack");
            this.defense = rs.getInt("defense");
            this.special_attack = rs.getInt("special_attack");
            this.special_defense = rs.getInt("special_defense");
            this.speed = rs.getInt("speed");
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}
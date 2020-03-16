package pokemonoceanblue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PokemonModel 
{
    int id;
    String name;
    int xp;
    int level;
    public byte statusEffect;

    int[] types;

    int currentHP;
    int[] stats = new int[6];

    int[] ivs = new int[6];
    int ivGain;
    public MoveModel[] moves = new MoveModel[0];
    public final boolean shiny;
    int pokeballId = 3;
    int happiness = 70;
    int captureRate;
    int stepCounter;
    
    /** 
     * Constructor
     * @param id the Pokemon's number from the national pokedex
     * @param level the Pokemon's current level
     */
    public PokemonModel(int id, int level, boolean shiny)
    {
        this.id = id;
        this.level = level;
        this.shiny = shiny;

        if (level > 0)
        {
            this.stepCounter = 500;
            this.xp = (int) Math.pow(level, 3);
            this.loadMoves();
        }
        else
        {
            this.stepCounter = 1500;
        }

        this.loadStats();
        
    }

    /** 
     * Recalculates a Pokemon's level based off it's current experience
     * Recalculates stats when a Pokemon levels up
     */
    private void calcLevel()
    {
        int oldLevel = this.level;
        this.level = (int) Math.floor(Math.cbrt(xp));

        if (oldLevel < this.level)
        {
            this.loadStats();
        }
    }

    /**
     * Adds XP to the Pokemon and then recalculates level
     * @param xp the xp to be added
     */
    public void addXP(int xp)
    {
        this.xp += xp;
        this.calcLevel();
        if (this.xp <= 1)
        {
            this.loadMoves();
        }
    }

    /** 
     * add or remove happiness
     */
    public void updateHappiness(int happinessChange)
    {
        if (happinessChange > 0)
        {
            this.happiness += Math.min(200 - this.happiness, happinessChange);
        }
        else
        {
            this.happiness += Math.max(0 - this.happiness, happinessChange);
        }
    }

    /** 
     * add IVs
     */
    public void updateIVs(int ivChange)
    {
        this.ivs[ivChange]++;
    }

    /**
     * @param evolvedPokemonId the new Id the Pokemon evolves into
     */
    public void evolve(int evolvedPokemonId)
    {
        this.id = evolvedPokemonId;
        this.loadStats();
    }

    /**
     * @return the id of a Pokemon or "egg" if the Pokemon is an egg
     */
    public String getSpriteId()
    {
        if (this.level > 0)
        {
            return String.valueOf(this.id);
        }
        else
        {
            return "egg";
        }
    }

    /**
     * @return the name of a Pokemon or "EGG" if the Pokemon is an egg
     */
    public String getName()
    {
        if (this.level > 0)
        {
            return this.name;
        }
        else
        {
            return "EGG";
        }
    }

    /** 
     * Read the Pokemon's stats, types, etc from a database
     */
    private void loadStats()
    {
        // generate random IVs from 0 to 15
        Random rand = new Random();
        for (int i = 0; i < ivs.length; i++)
        {
            ivs[i] = rand.nextInt(16);
        }

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM pokemon WHERE id = " + this.id;

            ResultSet rs = db.runQuery(query);

            this.name = rs.getString("name").toUpperCase();
            this.ivGain = rs.getInt("iv_gain");
            this.captureRate = rs.getInt("capture_rate");

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

            String query = "SELECT move_id FROM pokemon_moves "
                        + " WHERE pokemon_id = " + this.id 
                        + " AND level >= 1 "
                        + " AND level <= " + this.level
                        + " ORDER BY level DESC LIMIT 4";

            ResultSet rs = db.runQuery(query);

            List<MoveModel> loadMoves = new ArrayList<MoveModel>();

            while(rs.next()) 
            {
                loadMoves.add(new MoveModel(rs.getInt("move_id")));
            }          

            this.moves = new MoveModel[loadMoves.size()];
            loadMoves.toArray(this.moves);
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    public void decrementStepCounter()
    {
        this.stepCounter--;

        if (this.level > 0 && this.stepCounter <= 0)
        {
            // increase happiness by 1 every 500 steps
            this.updateHappiness(1);
            this.stepCounter = 500;
        }
    }
}
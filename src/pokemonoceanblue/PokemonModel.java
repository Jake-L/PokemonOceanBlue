package pokemonoceanblue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PokemonModel 
{
    public int pokemon_id;
    public int base_pokemon_id;
    String name;
    public int xp;
    public int level;
    public double levelModifier;
    public byte statusEffect = 0;

    public int[] types;

    public int currentHP;
    public int[] stats = new int[6];

    public int[] ivs = new int[6];
    public int ivGain;
    public MoveModel[] moves = new MoveModel[0];
    public final boolean shiny;
    int pokeballId = 3;
    public int happiness = 70;
    int captureRate;
    int stepCounter;
    public int genderId;
    public boolean raidBoss;
    public AbilityModel ability;
    
    /** 
     * Constructor
     * @param id the Pokemon's number from the national pokedex
     * @param level the Pokemon's current level
     * @param shiny whether or not it's a shiny Pokemon
     */
    public PokemonModel(int id, int level, boolean shiny)
    {
        this.pokemon_id = id;
        this.level = level;
        this.shiny = shiny;
        this.genderId = new Random().nextInt(2);

        // generate random IVs from 0 to 15
        Random rand = new Random();
        for (int i = 0; i < ivs.length; i++)
        {
            ivs[i] = rand.nextInt(16);
        }

        this.loadStats();

        if (level > 0)
        {
            this.stepCounter = 500;
            this.xp = this.calcXP(0);
            this.loadMoves();
        }
        else
        {
            this.stepCounter = 1500;
        }
    }

    /** 
     * Constructor
     * @param id the Pokemon's number from the national pokedex
     * @param level the Pokemon's current level
     * @param shiny whether or not it's a shiny Pokemon
     * @param raidBoss whether or not the Pokemon is a raid boss battle
     */
    public PokemonModel(int id, int level, boolean shiny, boolean raidBoss)
    {
        this(id, level, shiny);
        this.raidBoss = raidBoss;
        if (this.raidBoss)
        {
            for (int i = 0; i < this.stats.length; i++)
            {
                this.stats[i] *= 2;
            }
            this.currentHP *= 2;
        }        
    }

    /**
     * Read the Pokemon's information from a SQL ResultSet
     * @param rs ResultSet containing the Pokemon's data
     * @throws SQLException
     */
    public PokemonModel(ResultSet rs) throws SQLException
    {
        this.pokemon_id = rs.getInt("pokemon_id");
        this.shiny = rs.getInt("shiny") == 1;
        this.addXP(rs.getInt("xp"));

        for (int i = 0; i < this.ivs.length; i++)
        {
            this.ivs[i] = rs.getInt("iv_" + i);
        }

        // loads the Pokemon's moves
        List<MoveModel> loadMoves = new ArrayList<MoveModel>();
        String[] move_ids = {"move_1", "move_2", "move_3", "move_4"};

        for (String move_id : move_ids)
        {
                if (rs.getInt(move_id) > -1)
            {
                loadMoves.add(new MoveModel(rs.getInt(move_id)));
            }
        }

        this.moves = new MoveModel[loadMoves.size()];
        loadMoves.toArray(this.moves);

        // load other attributes
        this.happiness = rs.getInt("happiness");
        this.statusEffect = (byte)rs.getInt("status_effect");
        this.stepCounter = rs.getInt("step_counter");
        this.genderId = rs.getInt("gender_id");
        this.currentHP = rs.getInt("current_hp");
    }

    /** 
     * Recalculates a Pokemon's level based off it's current experience
     */
    private int calcLevel()
    {
        int level = 0;
        level = (int) Math.floor(Math.cbrt(this.xp * (1 / this.levelModifier)));

        // if the pokemon has any xp at all, it cannot be level 0
        if (xp > 0)
        {
            level = Math.max(level, 1);
        }

        // can't exceed level 100
        level = Math.min(level, 100);

        return level;
    }

    /**
     * Calculates the minimum amount of XP needed for a specific level
     * @param levelOffset the amount to offset the Pokemon's level by
     * @return the minimum amount of XP needed 
     */
    public int calcXP(int levelOffset)
    {
        int level = this.level + levelOffset;

        if (level > 100)
        {
            return 1000000000;
        }
        else
        {
            return (int)Math.ceil(Math.pow(level, 3.0) * this.levelModifier);
        }
    }

    /**
     * Adds XP to the Pokemon and then recalculates level
     * Recalculates stats when a Pokemon levels up
     * @param xp the xp to be added
     */
    public List<MoveModel> addXP(int xp)
    {
        int oldLevel = this.level;
        int missingHP = this.stats[0] - this.currentHP;
        this.xp += xp;
        this.level = this.calcLevel();
        
        if (oldLevel < this.level)
        {
            this.loadStats();

            // load moves if hatching from a egg
            if (oldLevel == 0)
            {
                this.loadMoves();
            }
            // otherwise, check for any new moves that can be learned by leveling up
            else
            {
                // deduct any HP that was missing before leveling up
                this.currentHP -= missingHP;

                return this.checkNewMoves(oldLevel);
            }
        }
        
        return null;
    }

    /**
     * Generate a list of moves that can be learned by the Pokemon leveling up
     * @param oldLevel the Pokemon's previous level
     * @return a list of new moves to that can be learned
     */
    private List<MoveModel> checkNewMoves(int oldLevel)
    {
        List<MoveModel> newMoves = new ArrayList<MoveModel>();

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT move_id FROM pokemon_moves WHERE pokemon_id = " + this.pokemon_id
                + " AND level > " + oldLevel
                + " AND level <= " + this.level;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                newMoves.add(new MoveModel(rs.getInt("move_id")));
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
        
        return newMoves;
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
        this.pokemon_id = evolvedPokemonId;
        this.loadStats();
    }

    /**
     * @return the id of a Pokemon or "egg" if the Pokemon is an egg
     */
    public String getSpriteId()
    {
        if (this.level > 0)
        {
            return String.valueOf(this.pokemon_id);
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
     * @param statIndex the stat to have stat changes applied
     * @param modifier the stat changes applied to the pokemon
     * @return the stat with stat changes applied for battles
     */
    public int getStat(int statIndex, int modifier)
    {
        if (modifier < 0)
        {
            return (int)(this.stats[statIndex] * (2.0 / (Math.abs(modifier) + 2)));
        }
        else if (modifier > 0)
        {
            return (int)(this.stats[statIndex] * ((Math.abs(modifier) + 2) / 2.0));
        }
        else
        {
            return this.stats[statIndex];
        }
    }

    /**
     * Checks for Pokemon that automatically change forms based on weather, location, etc
     * @param weatherId the current weather
     * @param battleBackgroundId the environment of the current location
     * @return true if the Pokemon's form changes
     */
    public boolean checkFormChange(byte weatherId, byte battleBackgroundId)
    {
        int newPokemonId = -1;

        // change Castform based on the current weather
        if (this.base_pokemon_id == 351)
        {
            switch (weatherId)
            {
                // sunny Castform
                case 1:
                    newPokemonId = 10013;
                    break;
                // rain Castform
                case 2:
                    newPokemonId = 10014;
                    break;
                // snowy Castform
                case 4:
                    newPokemonId = 10015;
                    break;
                default:
                    newPokemonId = 351;
                    break;
            }
        }

        // change Burmy based on the environment of the current location
        else if (this.base_pokemon_id == 412)
        {
            switch (battleBackgroundId)
            {
                case 0:
                    newPokemonId = 412;
                    break;
                case 1:
                    newPokemonId = 10016;
                    break;
                case 2:
                    newPokemonId = 10016;
                    break;
                case 3:
                    newPokemonId = 412;
                    break;
                case 4:
                    newPokemonId = 10016;
                    break;
                case 5:
                    newPokemonId = 10017;
                    break;
                case 6:
                    newPokemonId = 10017;
                    break;
            }
        }

        if (newPokemonId > -1 && newPokemonId != this.pokemon_id)
        {
            this.evolve(newPokemonId);
            return true;
        }
        else
        {
            return false;
        }
    }

    /** 
     * Read the Pokemon's stats, types, etc from a database
     */
    public void loadStats()
    {
        this.raidBoss = false;

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM pokemon WHERE pokemon_id = " + this.pokemon_id;

            ResultSet rs = db.runQuery(query);

            this.base_pokemon_id = rs.getInt("base_pokemon_id");
            this.name = rs.getString("name").toUpperCase();
            this.ivGain = rs.getInt("iv_gain");
            this.captureRate = rs.getInt("capture_rate");
            this.levelModifier = rs.getDouble("level_modifier");

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
            if (rs.getInt("hp") > 1)
            {
                this.stats[Stat.HP] = (int)Math.floor(2.0 * rs.getInt("hp") * this.level / 100) + this.level + 10;
            }
            else
            {
                // Shedinja has base HP stat value of 1 for a constant 1 HP
                this.stats[Stat.HP] = 1;
            }
            this.currentHP = this.stats[Stat.HP];

            String[] stats = {"hp", "attack","defense","special_attack","special_defense","speed"};

            for (int i = 1; i < stats.length; i++)
            {
                this.stats[i] = (int)Math.floor((2.0 * rs.getInt(stats[i]) + this.ivs[i]) * this.level / 100) + 5;
            }
            
            int abilityId = rs.getInt("ability_id");
            if (abilityId > -1)
            {
                this.ability = new AbilityModel(abilityId);
            }
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
                        + " WHERE pokemon_id = " + this.pokemon_id 
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

    /**
     * Attempts to learn a new move if the Pokemon has a move slot available
     * @param newMove the move to be learned
     * @return true if there was an open slot and the move was learned
     */
    public boolean addMove(MoveModel newMove)
    {
        if (this.moves.length < 4)
        {
            MoveModel[] newMoves = new MoveModel[this.moves.length + 1];
            for (int i = 0; i < this.moves.length; i++)
            {
                newMoves[i] = this.moves[i];
            }
            newMoves[newMoves.length - 1] = newMove;
            this.moves = newMoves;
            return true;
        }
        return false;
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

    public void toSQL(PreparedStatement statement, int index) throws SQLException
    {
        // statement index starts at one
        statement.setInt(1, this.pokemon_id);
        statement.setInt(2, this.xp);
        statement.setInt(3, this.shiny ? 1 : 0);
        statement.setInt(4, this.genderId);
        statement.setInt(5, this.currentHP);
        
        for (int i = 0; i < this.ivs.length; i++)
        {
            statement.setInt(6 + i, this.ivs[i]);
        }

        statement.setInt(12, this.statusEffect);
        statement.setInt(13, this.happiness);
        statement.setInt(14, this.stepCounter);

        for (int i = 0; i < 4; i++)
        {
            statement.setObject(15 + i, this.moves.length > i ? this.moves[i].moveId : -1);
        }

        statement.setInt(19, index);
    }
}
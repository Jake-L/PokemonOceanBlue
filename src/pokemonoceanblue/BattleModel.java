package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BattleModel 
{
    public PokemonModel[][] team = new PokemonModel[2][6];
    public int[] currentPokemon = new int[2];
    public byte daytimeType = 0;
    public byte areaType = 0;
    public String[] battleOptions = new String[3];
    public int optionIndex = 0;
    public byte counter = 15;
    private float[][] typeEffectiveness = new float[19][19];

    /** 
     * Constructor
     * @param opponentTeam the opposing trainers pokemon team
     * @param playerTeam the players pokemon team
     */
    public BattleModel(PokemonModel[] opponentTeam, PokemonModel[] playerTeam)
    {
        this.team[0] = playerTeam;
        this.team[1] = opponentTeam;
        this.currentPokemon[0] = 0;
        this.currentPokemon[1] = 0;
        this.battleOptions[0] = "FIGHT";
        this.battleOptions[1] = "POKEMON";
        this.battleOptions[2] = "POKEBALLS";
        loadData();
    }

    public void confirmSelection()
    {
        if (this.battleOptions[this.optionIndex].equals("FIGHT"))
        {
            this.battleOptions = new String[this.team[0][this.currentPokemon[0]].moves.length];
            
            for (int i = 0; i < this.team[0][this.currentPokemon[0]].moves.length; i++)
            {
                this.battleOptions[i] = String.valueOf(this.team[0][this.currentPokemon[0]].moves[i]);
            } 
        }
    }

    /*
     * Read data on type effectiveness and load it into an array
     */ 
    private void loadData()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT src_type_id, target_type_id, damage_factor "
                         + "FROM type_effectiveness";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                this.typeEffectiveness[rs.getInt(1)][rs.getInt(2)] = rs.getFloat(3);
            }            
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    public boolean isComplete()
    {
        return false;
    }

    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }
    }
}    
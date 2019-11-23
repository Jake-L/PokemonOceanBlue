package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BattleModel 
{
    public PokemonModel[][] team = new PokemonModel[2][];
    public int[] currentPokemon = new int[2];
    public byte daytimeType = 0;
    public byte areaType = 0;
    public String[] battleOptions = new String[3];
    public int optionIndex = 0;
    public byte INPUTDELAY = 10;
    public byte counter = INPUTDELAY;
    public List<BattleEvent> events = new ArrayList<BattleEvent>();
    public byte battleIndex = 0;
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
        if (this.events.size() == 0)
        {
            if (this.battleIndex == 0)
            {
                if (this.battleOptions[this.optionIndex].equals("FIGHT"))
                {
                    this.battleOptions = new String[this.team[0][this.currentPokemon[0]].moves.length];
            
                    for (int i = 0; i < this.team[0][this.currentPokemon[0]].moves.length; i++)
                    {
                        this.battleOptions[i] = String.valueOf(this.team[0][this.currentPokemon[0]].moves[i]);
                    }
                }

                else if (this.battleOptions[this.optionIndex].equals("POKEMON"))
                {
                    this.battleOptions = null;
                }

                battleIndex++;
                this.counter = INPUTDELAY;
            }

            else
            {
                BattleEvent event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " used " + this.team[0][this.currentPokemon[0]].moves[optionIndex]);
                this.events.add(event);
                this.battleIndex++;
                this.counter = 60;
                event = new BattleEvent(this.team[1][this.currentPokemon[1]].name + " used " + this.team[1][this.currentPokemon[1]].moves[0]);
                this.events.add(event);
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
        int faintedPokemon = 0;

        for (int i = 0; i < team[0].length; i++)
        {
            if (team[0][i].currentHP == 0)
            {
                faintedPokemon++;
            }

            if (faintedPokemon == team[0].length)
            {
                return true;
            }
        }

        faintedPokemon = 0;

        for (int i = 0; i < team[1].length; i++)
        {
            if (team[1][i].currentHP == 0)
            {
                faintedPokemon++;
            }

            if (faintedPokemon == team[1].length)
            {
                return true;
            }
        }
        
        return false;
    }

    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }

        else if (this.events.size() > 0)
        {
            this.events.remove(0);
            this.counter = 60;
        }

        if (this.events.size() == 0 && battleIndex == 2)
        {
            this.battleIndex = 0;
            this.battleOptions = new String[3];            
            this.battleOptions[0] = "FIGHT";
            this.battleOptions[1] = "POKEMON";
            this.battleOptions[2] = "POKEBALLS";
            this.optionIndex = 0;
        }
    }
    
    /** 
     * @return current text to be rendered
     */
    public String getText()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).text;
        }
        return null;
    }

    class BattleEvent
    {
        public final String text;

        /** 
         * Constructor
         * @param text the text that will be displayed
         */
        public BattleEvent(String text)
        {
            this.text = text;
        }
    }
}    
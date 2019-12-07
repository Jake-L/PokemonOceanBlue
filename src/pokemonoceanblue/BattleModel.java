package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public Random ranNum = new Random();
    private int firstAttacker;

    //when battleIndex is: 0, main menu. 1, secondary menu. 2, first turn. 3, second turn. 4, end of rotation.
    public byte battleIndex = 0;
    private float[][] typeEffectiveness = new float[19][19];
    private float[] modifier = new float[2];

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
                        this.battleOptions[i] = String.valueOf(this.team[0][this.currentPokemon[0]].moves[i].name);
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
                BattleEvent playerAttackEvent = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " used " + this.team[0][this.currentPokemon[0]].moves[optionIndex].name,
                    this.damageCalc(optionIndex, 0, 1),
                    1);
                this.battleIndex++;
                this.counter = 60;
                int enemyMove = ranNum.nextInt(this.team[1][this.currentPokemon[1]].moves.length);
                BattleEvent enemyAttackEvent = new BattleEvent("Enemy " + this.team[1][this.currentPokemon[1]].name + " used " + this.team[1][this.currentPokemon[1]].moves[enemyMove].name,
                    this.damageCalc(enemyMove, 1, 0),
                    0);
                
                if (this.team[0][this.currentPokemon[0]].moves[optionIndex].priority > this.team[1][this.currentPokemon[1]].moves[enemyMove].priority)
                {
                    firstAttacker = 0;
                }

                else if (this.team[0][this.currentPokemon[0]].moves[optionIndex].priority < this.team[1][this.currentPokemon[1]].moves[enemyMove].priority)
                {
                    firstAttacker = 1;
                }

                else if (this.team[0][this.currentPokemon[0]].stats[Stat.SPEED] < this.team[1][this.currentPokemon[1]].stats[Stat.SPEED])
                {
                    firstAttacker = 1;
                }

                else if (this.team[0][this.currentPokemon[0]].stats[Stat.SPEED] > this.team[1][this.currentPokemon[1]].stats[Stat.SPEED])
                {
                    firstAttacker = 0;
                }

                else 
                {
                    firstAttacker = ranNum.nextInt(2);
                }
                
                if (firstAttacker == 1)
                {
                    this.events.add(enemyAttackEvent);
                    effectivenessMessage(modifier[1]);
                    this.events.add(playerAttackEvent);
                    effectivenessMessage(modifier[0]);
                }
                
                else
                {
                    this.events.add(playerAttackEvent);
                    effectivenessMessage(modifier[0]);
                    this.events.add(enemyAttackEvent);
                    effectivenessMessage(modifier[1]);
                }
            }
        }
    }

    /** 
     * @param moveIndex the current move of the attacker
     * @param attacker the attacking team
     * @param defender the defending team
     * @return damage
     */
    private int damageCalc(int moveIndex, int attacker, int defender)
    {
        int attack_stat;
        int defense_stat;
        float stab = 1.0f;
        this.modifier[attacker] = 1;

        if (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].damageClassId == 2)
        {
            attack_stat = Stat.ATTACK;
            defense_stat = Stat.DEFENSE;
        }
        else if (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].damageClassId == 3)
        {
            attack_stat = Stat.SPECIAL_ATTACK;
            defense_stat = Stat.SPECIAL_DEFENSE;
        }
        else
        {
            return 0;
        }

        for (int i = 0; i < this.team[defender][this.currentPokemon[defender]].types.length; i++)
        {
            this.modifier[attacker] = this.typeEffectiveness[this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].typeId][this.team[defender][this.currentPokemon[defender]].types[i]] * this.modifier[attacker];
        }

        if (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].typeId == this.team[attacker][this.currentPokemon[attacker]].types[0] || this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].typeId == this.team[attacker][this.currentPokemon[attacker]].types[this.team[attacker][this.currentPokemon[attacker]].types.length - 1])
        {
            stab = 1.5f;
        }
        return (int)Math.ceil(((this.team[attacker][this.currentPokemon[attacker]].level * 2.0 / 5.0 + 2.0) * (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].power) * (this.team[attacker][this.currentPokemon[attacker]].stats[attack_stat] * 1.0 / this.team[defender][this.currentPokemon[defender]].stats[defense_stat]) / 50 + 2) * this.modifier[attacker] * stab);
    }
    
    /** 
     * @param effectiveness damage modifier
     */
    private void effectivenessMessage(float effectiveness)
    {
        if (effectiveness > 1)
        {
            BattleEvent event = new BattleEvent("It's super effective!");
            this.events.add(event);
        }
        else if (effectiveness < 1)
        {
            BattleEvent event = new BattleEvent("It's not very effective.");
            this.events.add(event);
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
            if (this.events.get(0).damage > -1)
            {
                this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP -= Math.min((this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP), (this.events.get(0).damage));
                this.battleIndex++;
            }
            if (this.events.get(0).newPokemonIndex > -1)
            {
                this.currentPokemon[1] = this.events.get(0).newPokemonIndex;
            }

            this.events.remove(0);
            this.counter = 60;

            if (this.team[1][this.currentPokemon[1]].currentHP == 0 && this.battleIndex == 3)
            {
                BattleEvent event = new BattleEvent(this.team[1][this.currentPokemon[1]].name + " fainted.");
                this.events.add(event);
                this.events.remove(0);
                if (!isComplete())
                {
                    event = new BattleEvent("Enemy trainer sent out " + this.team[1][this.currentPokemon[1] + 1].name, this.currentPokemon[1] + 1);
                    this.events.add(event);
                }
                this.battleIndex++;
            }
        }

        if (this.events.size() == 0 && this.battleIndex == 4)
        {
            this.battleIndex = 0;
            this.battleOptions = new String[3];            
            this.battleOptions[0] = "FIGHT";
            this.battleOptions[1] = "POKEMON";
            this.battleOptions[2] = "POKEBALLS";
            this.optionIndex = 0;
            this.counter = INPUTDELAY;
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
        public int damage = -1;
        public int target;
        public int newPokemonIndex = -1;

        /** 
         * Constructor
         * @param text the text that will be displayed
         */
        public BattleEvent(String text)
        {
            this.text = text;
        }

        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param damage the damage that will be taken by target
         * @param target the pokemon that will recieve the damage
         */
        public BattleEvent(String text, int damage, int target)
        {
            this(text);
            this.damage = damage;
            this.target = target;
        }

        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param newPokemonIndex the pokemon that will be sent out
         */
        public BattleEvent(String text, int newPokemonIndex)
        {
            this(text);
            this.newPokemonIndex = newPokemonIndex;
        }
    }
}    
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
    public String[] battleOptions;
    public int optionIndex = 0;
    public final byte INPUTDELAY = 6;
    public byte counter = INPUTDELAY;
    public List<BattleEvent> events = new ArrayList<BattleEvent>();
    public Random ranNum = new Random();
    private int firstAttacker;
    private App app;
    private float[][] typeEffectiveness = new float[19][19];
    private float[] modifier = new float[2];
    private int enemyMove;
    public boolean isCaught = false;
    public boolean isWild;
    private boolean[] attackMissed = new boolean[2];
    private boolean[] isCrit = new boolean[2];
    public String trainerName;
    public String trainerSpriteName;
    private boolean[] isOneHit = new boolean[2];
    private boolean[] evolveQueue = new boolean[6];
    private boolean[] isParalyzed = new boolean[2];

    /** 
     * Constructor
     * @param opponentTeam the opposing trainers pokemon team
     * @param playerTeam the players pokemon team
     */
    public BattleModel(PokemonModel[] opponentTeam, PokemonModel[] playerTeam, App app, boolean isWild)
    {
        this.team[0] = playerTeam;
        this.team[1] = opponentTeam;
        this.app = app;
        this.isWild = isWild;
        this.initialize();
    }

    public BattleModel(PokemonModel[] playerTeam, int battleId, App app)
    {
        this.team[0] = playerTeam;
        this.loadTeam(battleId);
        this.app = app;
        this.isWild = false;
        this.initialize();
    }

    /**
     * Set initial variable values
     */
    private void initialize()
    {
        this.currentPokemon[0] = -1;
        this.currentPokemon[1] = -1;
        int firstPokemon = 0;
        BattleEvent event;

        while (this.team[0][firstPokemon].currentHP == 0)
        {
            firstPokemon++;
        }

        if (this.isWild)
        {
            event = new BattleEvent("A wild " + this.team[1][0].name + " appeared!", 0, true, 1, String.valueOf(this.team[1][0].id));
            this.events.add(event);
        }
        else
        {
            event = new BattleEvent(this.trainerName + " sent out " + this.team[1][0].name + ".", 0, true, 1, String.valueOf(this.team[1][0].id));
            this.events.add(event);
        }
        event = new BattleEvent("Trainer sent out " + this.team[0][firstPokemon].name + ".", firstPokemon, true, 0, String.valueOf(this.team[0][firstPokemon].id));
        this.events.add(event);
        this.counter = 100;
        this.loadData();
    }

    public void confirmSelection()
    {
        if (this.events.size() == 0)
        {
            this.counter = INPUTDELAY;
            
            if (this.battleOptions != null)
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
                    this.app.openParty(this.currentPokemon[0]);
                }

                else if (this.battleOptions[this.optionIndex].equals("POKEBALLS"))
                {
                    this.battleOptions = null;
                    this.app.openInventory();
                }

                else
                {
                    this.battleOptions = null;
                    BattleEvent playerAttackEvent = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " used " + this.team[0][this.currentPokemon[0]].moves[optionIndex].name + ".",
                        this.damageCalc(optionIndex, 0, 1),
                        1,
                        0,
                        getAttackSound(0));
                    this.counter = 60;
                    
                    BattleEvent enemyAttackEvent = enemyAttackEvent();
                    
                    if (this.team[0][this.currentPokemon[0]].moves[this.optionIndex].priority > this.team[1][this.currentPokemon[1]].moves[this.enemyMove].priority)
                    {
                        firstAttacker = 0;
                    }
    
                    else if (this.team[0][this.currentPokemon[0]].moves[this.optionIndex].priority < this.team[1][this.currentPokemon[1]].moves[this.enemyMove].priority)
                    {
                        firstAttacker = 1;
                    }

                    else if (this.team[0][this.currentPokemon[0]].statusEffect != 1 && this.team[1][this.currentPokemon[1]].statusEffect == 1)
                    {
                        firstAttacker = 0;
                    }

                    else if (this.team[1][this.currentPokemon[1]].statusEffect != 1 && this.team[0][this.currentPokemon[0]].statusEffect == 1)
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
                        effectivenessMessage(modifier[1], 1);
                        if (this.team[1][this.currentPokemon[1]].moves[this.enemyMove].ailmentId > 0)
                        {
                            this.statusEffect(1, 0, this.team[1][this.currentPokemon[1]].moves[this.enemyMove]);
                        }
                        this.events.add(playerAttackEvent);
                        effectivenessMessage(modifier[0], 0);
                        if (this.team[0][this.currentPokemon[0]].moves[this.optionIndex].ailmentId > 0)
                        {
                            this.statusEffect(0, 1, this.team[0][this.currentPokemon[0]].moves[this.optionIndex]);
                        }
                    }
                    
                    else
                    {
                        this.events.add(playerAttackEvent);
                        effectivenessMessage(modifier[0], 0);
                        if (this.team[0][this.currentPokemon[0]].moves[this.optionIndex].ailmentId > 0)
                        {
                            this.statusEffect(0, 1, this.team[0][this.currentPokemon[0]].moves[this.optionIndex]);
                        }
                        this.events.add(enemyAttackEvent);
                        effectivenessMessage(modifier[1], 1);
                        if (this.team[1][this.currentPokemon[1]].moves[this.enemyMove].ailmentId > 0)
                        {
                            this.statusEffect(1, 0, this.team[1][this.currentPokemon[1]].moves[this.enemyMove]);
                        }
                    }
                }
            }
        }
    }

    
    /**
     * @param itemId is the item that will be used
     */
    public void setItem(int itemId)
    {
        if (itemId == -1)
        {
            this.loadBattleMenu();
        }
        else
        {
            BattleEvent event = new BattleEvent("Trainer used a " + itemId + ".", itemId, false, 0, null);
            this.events.add(event);
            this.counter = 60;            
            this.events.add(this.enemyAttackEvent());
            this.effectivenessMessage(modifier[1], 1);
            if (this.team[1][this.currentPokemon[1]].moves[this.enemyMove].ailmentId > 0)
            {
                this.statusEffect(1, 0, this.team[1][this.currentPokemon[1]].moves[this.enemyMove]);
            }
        }
    }

    /**
     * @param pokemon is the pokemon that will be sent out
     */
    public void setPokemon(int pokemon)
    {
        if (pokemon == -1)
        {
            this.loadBattleMenu();
        }
        else
        {
            BattleEvent event = new BattleEvent("Trainer withdrew " + this.team[0][this.currentPokemon[0]].name + ".", 0, null);
            this.events.add(event);
            event = new BattleEvent("Trainer sent out " + this.team[0][pokemon].name + ".", pokemon, true, 0, String.valueOf(this.team[0][pokemon].id));
            this.events.add(event);

            if (this.team[0][this.currentPokemon[0]].currentHP > 0)
            {
                // let the enemy attack after player switches
                this.events.add(this.enemyAttackEvent());
                this.effectivenessMessage(modifier[1], 1);
                if (this.team[1][this.currentPokemon[1]].moves[this.enemyMove].ailmentId > 0)
                {
                    this.statusEffect(1, 0, this.team[1][this.currentPokemon[1]].moves[this.enemyMove]);
                }
            }

            this.counter = 60;
        }
    }

    /**
     * @return enemyAttackEvent is the battle event that stores the enemy attack
     */
    private BattleEvent enemyAttackEvent()
    {
        this.enemyMove = ranNum.nextInt(this.team[1][this.currentPokemon[1]].moves.length);
        BattleEvent enemyAttackEvent = new BattleEvent("Enemy " + this.team[1][this.currentPokemon[1]].name + " used " + this.team[1][this.currentPokemon[1]].moves[enemyMove].name + ".",
            this.damageCalc(enemyMove, 1, 0),
            0,
            1,
            getAttackSound(1));
        return enemyAttackEvent;
    }

    /**
     * @param attacker the Pokemon attacking
     * @return the name of the sound effect to be played
     */
    private String getAttackSound(int attacker)
    {
        if (this.modifier[attacker] > 1)
        {
            return "superdamage";
        }
        else if (this.modifier[attacker] < 1 && this.modifier[attacker] > 0)
        {
            return "weakdamage";
        }
        else if (this.modifier[attacker] == 1)
        {
            return "damage";
        }
        return null;
    }

    /** 
     * creates event for the status effect inflicted by current move
     * @param attacker the attacking team
     * @param defender the defending team
     * @param move the move that inflicts the status effect
     */
    private void statusEffect(int attacker, int defender, MoveModel move)
    {
        if (this.ranNum.nextInt(101) <= move.effectChance && this.team[defender][this.currentPokemon[defender]].statusEffect == 0)
        {
            if (move.ailmentId == 1)
            {
                BattleEvent event = new BattleEvent(this.team[defender][this.currentPokemon[defender]].name + " was paralyzed.", 
                    move.ailmentId, defender, null);
                this.events.add(event);
            }
            else if (move.ailmentId == 2)
            {
                BattleEvent event = new BattleEvent(this.team[defender][this.currentPokemon[defender]].name + " fell asleep.", 
                    move.ailmentId, defender, null);
                this.events.add(event);
            }
            else if (move.ailmentId == 3)
            {
                BattleEvent event = new BattleEvent(this.team[defender][this.currentPokemon[defender]].name + " was frozen solid.", 
                    move.ailmentId, defender, null);
                this.events.add(event);
            }
            else if (move.ailmentId == 4)
            {
                BattleEvent event = new BattleEvent(this.team[defender][this.currentPokemon[defender]].name + " was burned.",
                    move.ailmentId, defender, null);
                this.events.add(event);
            }
            else if (move.ailmentId == 5)
            {
                BattleEvent event = new BattleEvent(this.team[defender][this.currentPokemon[defender]].name + " was badly poisoned.",
                    move.ailmentId, defender, null);
                this.events.add(event);
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
        float crit = 1.0f;
        this.isCrit[attacker] = false;
        this.isOneHit[attacker] = false;
        this.isParalyzed[attacker] = false;

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
            this.attackMissed[attacker] = false;
            return 0;
        }

        if (this.team[attacker][this.currentPokemon[attacker]].statusEffect == 1 && this.ranNum.nextInt(3) == 0)
        {
            this.isParalyzed[attacker] = true;
            return 0;
        }

        if (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].accuracy == -1 || this.ranNum.nextInt(101) <= this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].accuracy)
        {
            this.attackMissed[attacker] = false;
            for (int i = 0; i < this.team[defender][this.currentPokemon[defender]].types.length; i++)
            {
                this.modifier[attacker] = this.typeEffectiveness[this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].typeId][this.team[defender][this.currentPokemon[defender]].types[i]] * this.modifier[attacker];
            }

            if (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].typeId == this.team[attacker][this.currentPokemon[attacker]].types[0] || this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].typeId == this.team[attacker][this.currentPokemon[attacker]].types[this.team[attacker][this.currentPokemon[attacker]].types.length - 1])
            {
                stab = 1.5f;
            }

            if (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].power == -1)
            {
                this.isOneHit[attacker] = true;
                return this.team[defender][this.currentPokemon[defender]].currentHP;
            }

            if (this.ranNum.nextInt(9) == 0 && this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].power > 0)
            {
                crit = 1.5f;                        
                this.isCrit[attacker] = true;
            }
            return (int)Math.ceil(((this.team[attacker][this.currentPokemon[attacker]].level * 2.0 / 5.0 + 2.0) * (this.team[attacker][this.currentPokemon[attacker]].moves[moveIndex].power) * (this.team[attacker][this.currentPokemon[attacker]].stats[attack_stat] * 1.0 / this.team[defender][this.currentPokemon[defender]].stats[defense_stat]) / 50 + 2) * this.modifier[attacker] * stab * crit);
        }

        else
        {
            this.attackMissed[attacker] = true;
            return 0;
        }
    }
    
    /** 
     * @param effectiveness damage modifier
     * @param attacker the team using the attack
     */
    private void effectivenessMessage(float effectiveness, int attacker)
    {
        if (this.isParalyzed[attacker])
        {
            BattleEvent event = new BattleEvent(this.team[attacker][this.currentPokemon[attacker]].name + " is paralyzed and unable to move.", attacker, null);
            this.events.add(event);
        }
        if (this.isOneHit[attacker])
        {
            BattleEvent event = new BattleEvent("It's a one hit KO!", attacker, null);
            this.events.add(event);
        }
        if (this.isCrit[attacker] && this.modifier[attacker] > 0)
        {
            BattleEvent event = new BattleEvent("A critical hit!", attacker, null);
            this.events.add(event);
        }
        if (this.attackMissed[attacker])
        {
            BattleEvent event = new BattleEvent(this.team[attacker][this.currentPokemon[attacker]].name + "'s attack missed!", attacker, null);
            this.events.add(event);
        }
        else if (effectiveness > 1)
        {
            BattleEvent event = new BattleEvent("It's super effective!", attacker, null);
            this.events.add(event);
        }
        else if (effectiveness == 0)
        {
            BattleEvent event = new BattleEvent("It doesn't affect " + this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]].name + "...", attacker, null);
            this.events.add(event);
        }
        else if (effectiveness < 1)
        {
            BattleEvent event = new BattleEvent("It's not very effective...", attacker, null);
            this.events.add(event);
        }
    }

    /** 
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

    /**
     * returns to menu
     */
    public void loadBattleMenu()
    {
        if (this.isWild)
        {
            this.battleOptions = new String[3];
            this.battleOptions[2] = "POKEBALLS";
        }
        else
        {
            this.battleOptions = new String[2];
        }
        this.battleOptions[0] = "FIGHT";
        this.battleOptions[1] = "POKEMON";
        this.optionIndex = 0;
        this.counter = this.INPUTDELAY;
    }

    /** 
     * checks if battle is complete
     */ 
    public boolean isComplete()
    {
        if (this.counter > 0 || this.events.size() > 0 || this.battleOptions != null)
        {
            return false;
        }

        if (teamFainted(0) || teamFainted(1) || this.isCaught)
        {
            return true;
        }

        return false;
    }

    public PokemonModel getNewPokemon()
    {
        if (this.isComplete() && this.isWild && this.isCaught)
        {
            return this.team[1][0];
        }
        else
        {
            return null;
        }
    }

    /**
     * Determines if all the Pokemon have fainted in one team
     * @param teamIndex 0 for player's team, 1 for enemy's team
     * @return true if all of the Pokemon in that team have fainted
     */
    private boolean teamFainted(int teamIndex)
    {
        int faintedPokemon = 0;

        for (int i = 0; i < this.team[teamIndex].length; i++)
        {
            if (this.team[teamIndex][i].currentHP == 0)
            {
                faintedPokemon++;
            }

            if (faintedPokemon == this.team[teamIndex].length)
            {
                return true;
            }
        }

        return false;
    }

    private int xpCalc(int enemyLevel)
    {
        return (int) Math.pow(enemyLevel, 2) * 2;
    }

    public void update()
    {
        // switch the current Pokemon
        // do this at the start of counter to show the switching animation
        if (this.counter == 100 && this.events.get(0).newPokemonIndex > -1)
        {
            int attacker = this.events.get(0).attacker;
            this.currentPokemon[attacker] = this.events.get(0).newPokemonIndex;
        }

        if (this.counter == 50 && this.events.size() > 0 && this.events.get(0).sound != null)
        {
            this.app.playSound(this.events.get(0).sound);
        }

        if (this.counter > 0)
        {
            this.counter--;
        }

        else if (this.events.size() > 0)
        {
            if (this.events.get(0).xp > 0)
            {
                int xpGain = this.events.get(0).xp;
                int xpMax = (int) Math.pow(this.team[0][this.currentPokemon[0]].level + 1, 3.0);
            
                // check if the pokemon levels up from the xp gain
                if (this.team[0][this.currentPokemon[0]].xp + xpGain >= xpMax)
                {
                    BattleEvent event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " reached level " + (this.team[0][this.currentPokemon[0]].level + 1) + "!", 0, null);
                    this.events.add(1, event);   

                    // add a new event that shows the progress bar moving with XP remaining after leveling up
                    event = new BattleEvent("", xpGain + this.team[0][this.currentPokemon[0]].xp - xpMax, 0);
                    this.events.add(2, event);

                    this.team[0][this.currentPokemon[0]].xp = xpMax;
                    this.team[0][this.currentPokemon[0]].calcLevel();

                    // flag that the pokemon leveled up and may evolve
                    this.evolveQueue[this.currentPokemon[0]] = true;
                }
                else
                {
                    // add the remianing XP that isn't enough to level up
                    this.team[0][this.currentPokemon[0]].xp += this.events.get(0).xp;
                }
            }
            else if (this.events.get(0).statusEffect > -1)
            {
                this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].statusEffect = (byte)this.events.get(0).statusEffect;
            }
            else if (this.events.get(0).damage > -1)
            {
                this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP -= 
                    Math.min((this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP), 
                    (this.events.get(0).damage));

                // only check if a Pokemon was defeated if damage was applied
                if (this.team[1][this.currentPokemon[1]].currentHP == 0)
                {
                    int i = 0;
                    while (i < this.events.size())
                    {
                        if (this.events.get(i).attacker == 1)
                        {
                            this.events.remove(i);
                        }
                        else
                        {
                            i++;
                        }
                    }
                    BattleEvent event = new BattleEvent(this.team[1][this.currentPokemon[1]].name + " fainted.", 1, null);
                    this.events.add(event);

                    event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " gained " + xpCalc(this.team[1][this.currentPokemon[1]].level) + " experience.",
                        xpCalc(this.team[1][this.currentPokemon[1]].level), 0);
                    this.events.add(event);

                    if (!teamFainted(1))
                    {
                        event = new BattleEvent(this.trainerName + " sent out " + this.team[1][this.currentPokemon[1] + 1].name, 
                            this.currentPokemon[1] + 1, 
                            true, 
                            1,
                            String.valueOf(this.team[1][this.currentPokemon[1] + 1].id));
                        this.events.add(event);
                    }
                }

                //prevents player's pokemon from fighting when fainted
                if (this.team[0][currentPokemon[0]].currentHP == 0)
                {
                    int i = 0;
                    while (i < this.events.size())
                    {
                        if (this.events.get(i).attacker == 0)
                        {
                            this.events.remove(i);
                        }
                        else
                        {
                            i++;
                        }
                    }
                    BattleEvent event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " fainted.", 0, null);
                    this.events.add(event);
                }
            }
            else if (this.events.get(0).itemId > -1)
            {
                if (ranNum.nextInt(2) == 0)
                {
                    int i = 0;
                    while (i < this.events.size())
                    {
                        if (this.events.get(i).attacker == 1)
                        {
                            this.events.remove(i);
                        }
                        else
                        {
                            i++;
                        }
                    }
                    BattleEvent event = new BattleEvent("Trainer caught wild " + this.team[1][this.currentPokemon[1]].name + "!", 0, null);
                    this.events.add(event);
                    this.isCaught = true;
                }
                else
                {
                    BattleEvent event = new BattleEvent("The wild " + this.team[1][this.currentPokemon[1]].name + " escaped!", 0, null);
                    this.events.add(0, event);
                }
            }
            this.events.remove(0);

            if (this.events.size() > 0)
            {
                // use a larger counter when switching Pokemon
                // to show switching animation
                if (this.events.get(0).newPokemonIndex > -1)
                {
                    this.counter = 100;
                }
                else
                {
                    this.counter = 60;
                }
            }
        }

        //player sends out new pokemon to replace fainted one or battle returns to battle menu
        else if (this.battleOptions == null && this.counter == 0)
        {
            if (!teamFainted(0) && this.team[0][this.currentPokemon[0]].currentHP == 0)
            {
                this.battleOptions = null;
                this.app.openParty(this.currentPokemon[0]);
            }
            else
            {
                this.loadBattleMenu();
            }
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

    /**
     * Load the enemy team
     * @param battleId unique identifier for the enemy's team
     */
    private void loadTeam(int battleId)
    {
        List<PokemonModel> loadTeam = new ArrayList<PokemonModel>();
        
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT b.pokemon_id, b.level, ch.name, ch.sprite_name "
                         + "FROM battle b "
                         + "INNER JOIN conversation cv "
                         + "ON b.battle_id = cv.battle_id "
                         + "INNER JOIN character ch "
                         + "ON ch.conversation_id = cv.conversation_id "
                         + "WHERE b.battle_id = " + battleId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                loadTeam.add(new PokemonModel(rs.getInt(1), rs.getInt(2), false));
                this.trainerName = rs.getString(3);
                this.trainerSpriteName = rs.getString(4);
            }       
            
            this.team[1] = new PokemonModel[loadTeam.size()];
            loadTeam.toArray(this.team[1]);
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * @return a boolean for each IPokemon on the team, true if the Pokemon levelup up
     */
    public boolean[] getEvolveQueue()
    {
        return this.evolveQueue;
    }

    class BattleEvent
    {
        public final String text;
        public int damage = -1;
        public int target;
        public int newPokemonIndex = -1;
        public int itemId = -1;
        public int attacker;
        public int xp = 0;
        public String sound;
        public int statusEffect = -1;

        /** 
         * Constructor
         * @param text the text that will be displayed
         */
        public BattleEvent(String text, int attacker, String sound)
        {
            this.text = text;
            this.attacker = attacker;
            this.sound = sound;
        }

                /** 
         * Constructor
         * @param text the text that will be displayed
         * @param damage the damage that will be taken by target
         * @param target the pokemon that will recieve the damage
         * @param attacker the team performing event
         */
        public BattleEvent(String text, int statusEffect, int target, String sound)
        {
            this(text, target, sound);
            this.statusEffect = statusEffect;
            this.target = target;
        }

        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param damage the damage that will be taken by target
         * @param target the pokemon that will recieve the damage
         * @param attacker the team performing event
         */
        public BattleEvent(String text, int damage, int target, int attacker, String sound)
        {
            this(text, attacker, sound);
            this.damage = damage;
            this.target = target;
        }

        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param xp the amount of xp earned
         * @param attacker the team performing event
         */

        public BattleEvent(String text, int xp, int attacker)
        {
            this(text, attacker, null);
            this.xp = xp;
        }
        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param index is the index of the item or pokemon used
         * @param isPokemon determines whether index refers to item or pokemon
         * @param attacker the team performing event
         */
        public BattleEvent(String text, int index, boolean isPokemon, int attacker, String sound)
        {
            this(text, attacker, sound);
            
            if (!isPokemon)
            {
                this.itemId = index;
            }

            else
            {
                this.newPokemonIndex = index;
            }
        }
    }
}    
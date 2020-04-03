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
    public String[] battleOptions;
    public int optionIndex = 0;
    public final byte INPUTDELAY = 6;
    public byte counter = this.INPUTDELAY;
    public List<BattleEvent> events = new ArrayList<BattleEvent>();
    public Random ranNum = new Random();
    private App app;
    private float[][] typeEffectiveness = new float[19][19];
    private float[] modifier = new float[2];
    private int enemyMove;
    public boolean isCaught;
    public boolean isWild;
    private boolean[] attackMissed = new boolean[2];
    private boolean[] isCrit = new boolean[2];
    public String trainerName;
    public String trainerSpriteName;
    private boolean[] isOneHit = new boolean[2];
    private boolean[] evolveQueue = new boolean[6];
    private boolean[] unableToMove = new boolean[2];
    public boolean[] isSeen;
    private BattleEvent attackEvent[] = new BattleEvent[2];
    private String soundEffect;
    public int[][] statChanges = new int[2][8];
    public boolean[] willFlinch = new boolean[2];
    private boolean[] moveProcessed = new boolean[2];

    /** 
     * Constructor
     * @param opponentTeam the opposing trainers pokemon team
     * @param playerTeam the players pokemon team
     */
    public BattleModel(PokemonModel[] opponentTeam, PokemonModel[] playerTeam, App app)
    {
        this.team[0] = playerTeam;
        this.team[1] = opponentTeam;
        this.app = app;
        this.isWild = true;
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
        this.isSeen = new boolean[this.team[1].length];
        this.isSeen[0] = true;

        // send out the first Pokemon with HP > 0 and that's not an egg
        while (this.team[0][firstPokemon].currentHP == 0 || this.team[0][firstPokemon].level == 0)
        {
            firstPokemon++;
        }

        if (this.isWild)
        {
            event = new BattleEvent("A wild " + this.team[1][0].name + " appeared!", 0, true, 1, -1, String.valueOf(this.team[1][0].pokemon_id));
            this.events.add(event);
        }
        else
        {
            event = new BattleEvent(this.trainerName + " sent out " + this.team[1][0].name + ".", 0, true, 1, -1, String.valueOf(this.team[1][0].pokemon_id));
            this.events.add(event);
        }
        event = new BattleEvent("Trainer sent out " + this.team[0][firstPokemon].name + ".", firstPokemon, true, 0, -1, String.valueOf(this.team[0][firstPokemon].pokemon_id));
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
                switch(this.battleOptions[this.optionIndex])
                {    
                    case "FIGHT":
                        
                        this.battleOptions = new String[this.team[0][this.currentPokemon[0]].moves.length];
                
                        for (int i = 0; i < this.team[0][this.currentPokemon[0]].moves.length; i++)
                        {
                            this.battleOptions[i] = String.valueOf(this.team[0][this.currentPokemon[0]].moves[i].name);
                        } 
                        break;

                    case "POKEMON":

                        this.battleOptions = null;
                        this.app.openParty(this.currentPokemon[0]); 
                        break;

                    case "POKEBALLS":
                
                        this.battleOptions = null;
                        this.app.openInventory(); 
                        break;

                    default:
                        
                        int firstAttacker;
                        this.battleOptions = null;
                        this.attackEvent[0] = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " used " + this.team[0][this.currentPokemon[0]].moves[this.optionIndex].name + ".",
                            1,
                            1,
                            0,
                            this.team[0][this.currentPokemon[0]].moves[optionIndex],
                            0,
                            getAttackSound(0));
                        this.counter = 60;
                        
                        this.attackEvent[1] = enemyAttackEvent();
                        
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
        
                        else if (this.team[0][this.currentPokemon[0]].getStat(Stat.SPEED, this.statChanges[0][Stat.SPEED]) < this.team[1][this.currentPokemon[1]].getStat(Stat.SPEED, this.statChanges[1][Stat.SPEED]))
                        {
                            firstAttacker = 1;
                        }
        
                        else if (this.team[0][this.currentPokemon[0]].getStat(Stat.SPEED, this.statChanges[0][Stat.SPEED]) > this.team[1][this.currentPokemon[1]].getStat(Stat.SPEED, this.statChanges[1][Stat.SPEED]))
                        {
                            firstAttacker = 0;
                        }
        
                        else 
                        {
                            firstAttacker = ranNum.nextInt(2);
                        }
                        
                        this.events.add(this.attackEvent[firstAttacker]);
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
            BattleEvent event = new BattleEvent("Trainer used a " + itemId + ".", itemId, false, 0, -1, null);
            this.events.add(event);
            this.counter = 60;            
            this.events.add(this.enemyAttackEvent());
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
        else if (this.team[0][this.currentPokemon[0]].currentHP > 0)
        {
            BattleEvent event = new BattleEvent(
                "Trainer withdrew " + this.team[0][Math.abs(this.currentPokemon[0])].name + ".", 
                -1,
                true,
                0,
                -1,
                null);
            this.events.add(event);
            event = new BattleEvent("Trainer sent out " + this.team[0][pokemon].name + ".", pokemon, true, 0, -1, String.valueOf(this.team[0][pokemon].pokemon_id));
            this.events.add(event);
            this.counter = 60;

            // let the enemy attack after player switches
            this.events.add(this.enemyAttackEvent());
        }
        else
        {
            BattleEvent event = new BattleEvent("Trainer sent out " + this.team[0][pokemon].name + ".", pokemon, true, 0, -1, String.valueOf(this.team[0][pokemon].pokemon_id));
            this.events.add(event);
            this.counter = 100;
        }
    }

    /**
     * @return enemyAttackEvent is the battle event that stores the enemy attack
     */
    private BattleEvent enemyAttackEvent()
    {
        this.enemyMove = ranNum.nextInt(this.team[1][this.currentPokemon[1]].moves.length);
        BattleEvent enemyAttackEvent = new BattleEvent("Enemy " + this.team[1][this.currentPokemon[1]].name + " used " + this.team[1][this.currentPokemon[1]].moves[enemyMove].name + ".",
            1,
            0,
            1,
            this.team[1][this.currentPokemon[1]].moves[enemyMove],
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
            String[] statusEffectMessages = {" was paralyzed."," fell asleep."," was frozen solid."," was burned."," was badly poisoned."," became confused."};
            if (move.ailmentId < 7)
            {
                BattleEvent event = new BattleEvent(this.team[defender][this.currentPokemon[defender]].name + statusEffectMessages[move.ailmentId - 1], 
                    move.ailmentId, defender, defender, null);
                this.events.add(event);
            }
        }
    }

    /** 
     * checks if an attack can be used and if not removes the event and adds a message
     * @param attacker is the attacking team
     */
    private void canAttack(int attacker)
    {
        PokemonModel attackingPokemon = this.team[attacker][this.currentPokemon[attacker]];
        BattleEvent event;
        //check if attacker can attack while being paralyzed, asleep, or frozen
        if (attackingPokemon.statusEffect > 0 && attackingPokemon.statusEffect < 4)
        {
            if (attackingPokemon.statusEffect == 1 && this.ranNum.nextInt(101) < 34)
            {
                this.events.get(0).damage = damageCalc(this.events.get(0).move, attacker, (attacker + 1) % 2);
            }
            else if ((attackingPokemon.statusEffect == 2 && this.ranNum.nextInt(101) < 41) ||
                (attackingPokemon.statusEffect == 3 && this.ranNum.nextInt(101) < 31))
            {
                event = new BattleEvent(attackingPokemon.name + (attackingPokemon.statusEffect == 2 ? " woke up!" : " thawed out."),
                    0,
                    attacker,
                    attacker,
                    null);
                this.events.add(0, event);
                this.events.get(1).damage = damageCalc(this.events.get(1).move, attacker, (attacker + 1) % 2);
            }
            else
            {
                this.modifier[attacker] = 1.0f;
                this.unableToMove[attacker] = true;
                this.events.remove(0);
            }
        }
        else if (this.willFlinch[attacker])
        {
            this.modifier[attacker] = 1.0f;
            this.unableToMove[attacker] = true;
            this.events.remove(0);
            event = new BattleEvent(attackingPokemon.name + " flinched!", attacker, attacker, null);
        }
        //check if attacker is confused, if so use random move
        else if (attackingPokemon.statusEffect == 6)
        {
            //25% chance of snapping out of confusion
            if (this.ranNum.nextInt(101) < 26)
            {
                event = new BattleEvent(attackingPokemon.name + " snapped out of confusion!", attacker, attacker, null);
                this.events.add(0, event);
            }
            else
            {
                event = new BattleEvent(attackingPokemon.name + " is confused.", 0, attacker, attacker, null);
                this.events.add(0, event);
                this.events.get(1).move = attackingPokemon.moves[this.ranNum.nextInt(attackingPokemon.moves.length)];
            }
            this.events.get(1).damage = damageCalc(this.events.get(1).move, attacker, (attacker + 1) % 2);
        }
        else
        {
            this.events.get(0).damage = damageCalc(this.events.get(0).move, attacker, (attacker + 1) % 2);
        }
    }

    /** 
     * checks if either pokemon should suffer an end of turn effect (poison, leech seed, burn, etc.)
     */
    private void endOfTurnEffects()
    {
        String[] statusEffectMessages = {" is hurt by burn."," is hurt by poison."};
        for (int i = 0; i < 2; i++)
        {
            if (this.team[i][this.currentPokemon[i]].statusEffect > 3 && this.team[i][this.currentPokemon[i]].statusEffect < 6)
            {
                BattleEvent event = new BattleEvent(this.team[i][this.currentPokemon[i]].name + statusEffectMessages[this.team[i][this.currentPokemon[i]].statusEffect - 4],
                    (int)Math.ceil(this.team[i][this.currentPokemon[i]].stats[0] / 8.0),
                    i, i, null, i, null);
                this.events.add(event);
            }
        }
    }

    /** 
     * creates event for the self healing or recoil on attacking pokemon
     * @param attacker the attacking team
     * @param move the move that inflicts the heal or recoil
     */
    private void recoil(int attacker, MoveModel move)
    {
        BattleEvent event;
        if (move.recoil > 0)
        {
            event = new BattleEvent(this.team[attacker][this.currentPokemon[attacker]].name + " is hit with recoil.",
                (int)(Math.ceil(Math.min(this.events.get(0).damage, 
                this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]].currentHP) * (move.recoil / 100.0))),
                attacker,
                attacker,
                null,
                attacker,
                null);
        }
        else
        {
            event = new BattleEvent(this.team[attacker][this.currentPokemon[attacker]].name + " healed itself.",
                (int)(Math.floor(this.events.get(0).damage * (move.recoil / 100.0))),
                attacker,
                attacker,
                null,
                attacker,
                null);
        }
        this.events.add(event);
    }

    /** 
     * creates event for the stat changes applied by a move used
     * @param attacker the attacking team
     * @param move the move that inflicts the heal or recoil
     */
    private void statChanges(int attacker, MoveModel move)
    {
        if (ranNum.nextInt(101) <= move.effectChance)
        {
            String[] statChangeMessages = {" fell sharply.", " fell.", "", " rose.", " rose sharply."};
            String[] changedStat = {"", " attack", " special attack", " defense", " special defense", " speed", " accuracy", " evasiveness"};
            int target = (attacker + 1) % 2;
            //loop through all stat changes in case there are multiple
            for (int i = 0; i < move.moveStatEffects.length; i++)
            {
                BattleEvent event;
                int statId = move.moveStatEffects[i].statId;
                int statChange = move.moveStatEffects[i].statChange;
                //when target id is 7 or 10 the move applies stat changes to the user, otherwise applied to foe
                if (move.targetId == 7 || (move.targetId == 10 && move.power > 0))
                {
                    target = attacker;
                }
                //check if stat cannot be changed any further
                if ((this.statChanges[target][statId] == 6 && statChange > 0) || (this.statChanges[target][statId] == -6 && statChange < 0))
                {
                    event = new BattleEvent(this.team[target][this.currentPokemon[target]].name + "'s " + 
                        changedStat[statId] + " cannot be " + 
                        (this.statChanges[target][statId] < 0 ? "increased" : "decreased") + " any further.",
                        target,
                        target,
                        null);
                }
                else
                {
                    event = new BattleEvent(this.team[target][this.currentPokemon[target]].name + "'s " +
                        changedStat[statId] + statChangeMessages[statChange + 2],
                        target,
                        target,
                        null);
                    //apply stat change with limits of |6|
                    this.statChanges[target][statId] = (statChange / Math.abs(statChange)) * Math.min(6, Math.abs(statChange + this.statChanges[target][statId]));
                }
                this.events.add(event);
            }
        }
    }

    private int getAccuracy(int attacker, int moveAccuracy)
    {
        int defender = (attacker + 1) % 2;
        if (this.statChanges[attacker][6] == 0 && this.statChanges[defender][7] == 0)
        {
            return moveAccuracy;
        }
        return (int)(moveAccuracy * (2.0 / (Math.abs(this.statChanges[attacker][6]) + 2)) / 
            (statChanges[defender][7] > 0 ? (Math.abs(statChanges[defender][7]) + 2) / 2.0 : 2.0 / (Math.abs(statChanges[defender][7]) + 2)));
    }
    
    /** 
     * @param moveIndex the current move of the attacker
     * @param attacker the attacking team
     * @param defender the defending team
     * @return damage
     */
    private int damageCalc(MoveModel move, int attacker, int defender)
    {
        int attack_stat;
        int defense_stat;
        float stab = 1.0f;
        this.modifier[attacker] = 1;
        float crit = 1.0f;
        PokemonModel attackingPokemon = this.team[attacker][this.currentPokemon[attacker]];
        PokemonModel defendingPokemon = this.team[defender][this.currentPokemon[defender]];

        //if enemy is fainted, only self-effect moves can be used
        if (defendingPokemon.currentHP == 0 && move.targetId != 7)
        {
            this.attackMissed[attacker] = true;
            return 0;
        }

        if (move.damageClassId == 2)
        {
            attack_stat = Stat.ATTACK;
            defense_stat = Stat.DEFENSE;
        }

        else if (move.damageClassId == 3)
        {
            attack_stat = Stat.SPECIAL_ATTACK;
            defense_stat = Stat.SPECIAL_DEFENSE;
        }

        else if (move.accuracy == -1 || this.ranNum.nextInt(101) <= this.getAccuracy(attacker, move.accuracy))
        {
            return 0;
        }

        else
        {
            this.attackMissed[attacker] = true;
            return 0;
        }

        if (move.accuracy == -1 || this.ranNum.nextInt(101) <= this.getAccuracy(attacker, move.accuracy))
        {
            for (int i = 0; i < defendingPokemon.types.length; i++)
            {
                this.modifier[attacker] = this.typeEffectiveness[move.typeId][defendingPokemon.types[i]] * this.modifier[attacker];
            }

            if (move.typeId == attackingPokemon.types[0] || move.typeId == attackingPokemon.types[attackingPokemon.types.length - 1])
            {
                stab = 1.5f;
            }

            if (move.power == -1 && this.modifier[attacker] != 0)
            {
                this.isOneHit[attacker] = true;
                return defendingPokemon.currentHP;
            }

            if (this.ranNum.nextInt(9) == 0 && move.power > 0)
            {
                crit = 1.5f;                        
                this.isCrit[attacker] = true;
            }
            return (int)Math.ceil((
                        (attackingPokemon.level * 2.0 / 5.0 + 2.0) 
                        * (move.power) 
                        * (attackingPokemon.getStat(attack_stat, this.statChanges[attacker][attack_stat]) * 1.0
                        / defendingPokemon.getStat(defense_stat, this.statChanges[defender][defense_stat])) / 50 + 2) 
                    * this.modifier[attacker] * stab * crit);
        }

        else
        {
            this.attackMissed[attacker] = true;
            return 0;
        }
    }
    
    /** 
     * checks if a message should be displayed after an attack and if so, adds the message
     * @param effectiveness damage modifier
     * @param attacker the team using the attack
     */
    private void effectivenessMessage(float effectiveness, int attacker)
    {
        PokemonModel attackingPokemon = this.team[attacker][this.currentPokemon[attacker]];
        if (this.unableToMove[attacker] && attackingPokemon.statusEffect > 0 && attackingPokemon.statusEffect < 4)
        {
            String[] statusEffectMessages = {" is paralyzed and unable to move."," is fast asleep."," is frozen solid."};
            BattleEvent event = new BattleEvent(
                this.team[attacker][this.currentPokemon[attacker]].name + statusEffectMessages[this.team[attacker][this.currentPokemon[attacker]].statusEffect - 1], 
                attacker, attacker, null);
            this.events.add(event);
        }
        else if (this.unableToMove[attacker] && this.willFlinch[attacker])
        {
            BattleEvent event = new BattleEvent(
                this.team[attacker][this.currentPokemon[attacker]].name + " flinched.", 
                attacker, attacker, null);
            this.events.add(event);
        }
        else if (this.isOneHit[attacker])
        {
            BattleEvent event = new BattleEvent("It's a one hit KO!", attacker, attacker, null);
            this.events.add(event);
        }
        else if (this.isCrit[attacker] && this.modifier[attacker] > 0)
        {
            BattleEvent event = new BattleEvent("A critical hit!", attacker, attacker, null);
            this.events.add(event);
        }
        if (this.attackMissed[attacker])
        {
            BattleEvent event = new BattleEvent(this.team[attacker][this.currentPokemon[attacker]].name + "'s attack missed!", attacker, attacker, null);
            this.events.add(event);
        }
        else if (effectiveness > 1 && !this.isOneHit[attacker])
        {
            BattleEvent event = new BattleEvent("It's super effective!", attacker, attacker, null);
            this.events.add(event);
        }
        else if (effectiveness == 0)
        {
            BattleEvent event = new BattleEvent("It doesn't affect " + this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]].name + "...", attacker, attacker, null);
            this.events.add(event);
        }
        else if (effectiveness < 1 && !this.isOneHit[attacker])
        {
            BattleEvent event = new BattleEvent("It's not very effective...", attacker, attacker, null);
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
     * checks if any pokemon have fainted after damage has been applied
     */ 
    private void checkFainted()
    {
        if (this.team[1][this.currentPokemon[1]].currentHP == 0)
        {
            //remove all events performed by or affecting the fainted pokemon
            boolean faintEventExists = false;
            int i = 0;
            while (i < this.events.size())
            {
                if (this.events.get(i).newPokemonIndex == -1 && this.events.get(i).attacker == 1)
                {
                    faintEventExists = true;
                    break;
                }
                if (this.events.get(i).removalCondition == 1)
                {
                    this.events.remove(i);
                }
                else
                {
                    i++;
                }
            }
            if (!faintEventExists)
            {
                this.team[0][this.currentPokemon[0]].updateHappiness(1);
                this.team[0][this.currentPokemon[0]].updateIVs(this.team[1][this.currentPokemon[1]].ivGain);
                BattleEvent event = new BattleEvent(
                    this.team[1][this.currentPokemon[1]].name + " fainted.", 
                    -1, 
                    true, 
                    1,
                    -1,
                    null);
                this.events.add(event);

                event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " gained " + xpCalc(this.team[1][this.currentPokemon[1]].level) + " experience.",
                    xpCalc(this.team[1][this.currentPokemon[1]].level), 0, 0);
                this.events.add(event);

                //send out enemy's next pokemon if any remain
                if (!teamFainted(1))
                {
                    event = new BattleEvent(this.trainerName + " sent out " + this.team[1][this.currentPokemon[1] + 1].name, 
                        this.currentPokemon[1] + 1, 
                        true, 
                        1,
                        -1,
                        String.valueOf(this.team[1][this.currentPokemon[1] + 1].pokemon_id));
                    this.events.add(event);
                }
            }
        }

        if (this.team[0][this.currentPokemon[0]].currentHP == 0)
        {
            //remove all events performed by or affecting the fainted pokemon
            boolean faintEventExists = false;
            int i = 0;
            while (i < this.events.size())
            {
                if (this.events.get(i).newPokemonIndex == -1 && this.events.get(i).attacker == 0)
                {
                    faintEventExists = true;
                    break;
                }
                if (this.events.get(i).removalCondition == 0)
                {
                    this.events.remove(i);
                }
                else
                {
                    i++;
                }
            }
            if (!faintEventExists)
            {
                this.team[0][this.currentPokemon[0]].updateHappiness(-5);
                BattleEvent event = new BattleEvent(
                    this.team[0][this.currentPokemon[0]].name + " fainted.", 
                    -1, 
                    true, 
                    0,
                    -1,
                    null);
                this.events.add(event);
            }
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
            //reset stat changes on switched pokemon
            this.statChanges[attacker] = new int[8];
            if (attacker == 1)
            {
                this.isSeen[this.events.get(0).newPokemonIndex] = true;
            }
        }

        //calculate damage for an upcoming attack event and add second attack event if applicable
        else if (this.counter == 60 && this.events.size() > 0 && this.events.get(0).damage > -1 && this.events.get(0).move != null && !this.moveProcessed[this.events.get(0).attacker])
        {
            int attacker = this.events.get(0).attacker;
            this.canAttack(attacker);
            this.effectivenessMessage(this.modifier[attacker], attacker);
            if (!this.unableToMove[attacker] && this.events.get(0).move != null && !this.attackMissed[attacker])
            {
                if (this.attackEvent[(attacker + 1) % 2] != null && ranNum.nextInt(101) <= this.events.get(0).move.flinchChance)
                {
                    this.willFlinch[(attacker + 1) % 2] = true;
                }
                else if (this.events.get(0).damage * this.events.get(0).move.recoil != 0)
                {
                    this.recoil(attacker, this.events.get(0).move);
                }
                else if (this.events.get(0).move.ailmentId > 0)
                {
                    this.statusEffect(attacker, (attacker + 1) % 2, this.events.get(0).move);
                }
                if (this.events.get(0).move.moveStatEffects.length > 0)
                {
                    this.statChanges(attacker, this.events.get(0).move);
                }
            }
            if (this.attackEvent[(attacker + 1) % 2] != null)
            {
                this.events.add(this.attackEvent[(attacker + 1) % 2]);
            }
            else
            {
                this.endOfTurnEffects();
            }
            this.attackEvent[0] = null;
            this.attackEvent[1] = null;
            this.moveProcessed[attacker] = true;
        }

        else if (this.events.size() > 0 && this.events.get(0).sound != null)
        {
            this.soundEffect = this.events.get(0).sound;
            this.events.get(0).sound = null;
        }

        if (this.counter > 0)
        {
            this.counter--;
        }

        // logic that happen at the end of an event
        else if (this.events.size() > 0)
        {
            if (this.events.get(0).xp > 0)
            {
                int xpGain = this.events.get(0).xp;
                int xpMax = (int) Math.pow(this.team[0][this.currentPokemon[0]].level + 1, 3.0);
            
                // check if the pokemon levels up from the xp gain
                if (this.team[0][this.currentPokemon[0]].xp + xpGain >= xpMax)
                {
                    BattleEvent event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " reached level " + (this.team[0][this.currentPokemon[0]].level + 1) + "!", 0, 0, null);
                    this.events.add(1, event);   

                    // add a new event that shows the progress bar moving with XP remaining after leveling up
                    event = new BattleEvent("", xpGain + this.team[0][this.currentPokemon[0]].xp - xpMax, 0, 0);
                    this.events.add(2, event);

                    this.team[0][this.currentPokemon[0]].addXP(xpMax - this.team[0][this.currentPokemon[0]].xp);

                    // flag that the pokemon leveled up and may evolve
                    this.evolveQueue[this.currentPokemon[0]] = true;
                }
                else
                {
                    // add the remianing XP that isn't enough to level up
                    this.team[0][this.currentPokemon[0]].addXP(this.events.get(0).xp);
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

                this.checkFainted();
            }
            else if (this.events.get(0).damage < 0 && this.events.get(0).target != -1 && this.events.get(0).attacker == this.events.get(0).target)
            {
                // self-healing
                this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP = 
                    Math.min((this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].stats[0]), 
                    (this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP + 
                    Math.abs(this.events.get(0).damage)));
            }
            else if (this.events.get(0).itemId > -1)
            {
                int pokemonCaptureRate = this.team[1][this.currentPokemon[1]].captureRate;
                // determine if the Pokemon should be captured
                double captureChance = (pokemonCaptureRate / 2 - 1) 
                    + (pokemonCaptureRate / 2 + 1) 
                    * (1 - Math.sqrt(this.team[1][this.currentPokemon[1]].currentHP / (double)(this.team[1][this.currentPokemon[1]].stats[Stat.HP])));
                if (ranNum.nextInt(101) <= captureChance)
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
                    BattleEvent event = new BattleEvent("Trainer caught wild " + this.team[1][this.currentPokemon[1]].name + "!", 0, -1, null);
                    this.events.add(event);
                    this.isCaught = true;
                }
                else
                {
                    BattleEvent event = new BattleEvent("The wild " + this.team[1][this.currentPokemon[1]].name + " escaped!", 0, -1, null);
                    this.events.add(1, event);
                }
            }
            this.events.remove(0);

            if (this.events.size() > 0)
            {
                // use a larger counter when sending in Pokemon
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
            this.moveProcessed = new boolean[2];
            this.isCrit = new boolean[2];
            this.isOneHit = new boolean[2];
            this.willFlinch = new boolean[2];
            this.attackMissed = new boolean[2];
            if (!teamFainted(0) && this.team[0][this.currentPokemon[0]].currentHP == 0)
            {
                this.battleOptions = null;
                this.app.openParty(this.currentPokemon[0]);
            }
            else
            {
                //reset all attack related variables at the end of a turn and load the battle menu
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

            // if there are multiple characters with the same conversation id
            // the trainer shown in battle is the one with the lowest character id
            String query = "SELECT b.pokemon_id, b.level, c.name, c.sprite_name "
                         + "FROM battle b "
                         + "INNER JOIN (select ch.name, ch.sprite_name, cv.conversation_id, cv.battle_id, "
                         + "ROW_NUMBER() OVER(ORDER BY ch.character_id ASC) AS char_num "
                         + "FROM conversation cv "
                         + "INNER JOIN character ch "
                         + "ON ch.conversation_id = cv.conversation_id "
                         + "AND cv.battle_id = " + battleId + ") c "
                         + "ON b.battle_id = c.battle_id "
                         + "WHERE char_num = 1";

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

    /**
     * Returns the sound effect to be played and then clears it 
     * @return the sound effect to be played
     */
    public String getSoundEffect()
    {
        String sound = this.soundEffect;
        if (sound != null)
        {
            System.out.println(sound);
        }
        this.soundEffect = null;
        return sound;
    }

    class BattleEvent
    {
        public final String text;
        public int damage = -1;
        public int target = -1;
        public int newPokemonIndex = -2;
        public int itemId = -1;
        public int attacker;
        public int xp = 0;
        public String sound;
        public int statusEffect = -1;
        public MoveModel move;
        public int removalCondition;

        /** 
         * Constructor
         * @param text the text that will be displayed
         */
        public BattleEvent(String text, int attacker, int removalCondition, String sound)
        {
            this.text = text;
            this.attacker = attacker;
            this.removalCondition = removalCondition;
            this.sound = sound;
        }

        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param damage the damage that will be taken by target
         * @param target the pokemon that will recieve the damage
         * @param attacker the team performing event
         */
        public BattleEvent(String text, int statusEffect, int target, int removalCondition, String sound)
        {
            this(text, target, removalCondition, sound);
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
        public BattleEvent(String text, int damage, int target, int attacker, MoveModel move, int removalCondition, String sound)
        {
            this(text, attacker, removalCondition, sound);
            this.damage = damage;
            this.target = target;
            this.move = move;
        }

        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param xp the amount of xp earned
         * @param attacker the team performing event
         */

        public BattleEvent(String text, int xp, int attacker, int removalCondition)
        {
            this(text, attacker, removalCondition, null);
            this.xp = xp;
        }
        /** 
         * Constructor
         * @param text the text that will be displayed
         * @param index is the index of the item or pokemon used
         * @param isPokemon determines whether index refers to item or pokemon
         * @param attacker the team performing event
         */
        public BattleEvent(String text, int index, boolean isPokemon, int attacker, int removalCondition, String sound)
        {
            this(text, attacker, removalCondition, sound);
            
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

    // class MultiTurnEffect
    // {
    //     int effectId;
    //     int attacker;
    //     int target;
    //     int duration;
    //     int damage;
    //     int healing;
    //     byte timeEffectOccurs;

    //     public MultiTurnEffect(int effectId)
    //     {

    //     }
    // }
}    
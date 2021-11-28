package pokemonoceanblue.battle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pokemonoceanblue.AppManager;
import pokemonoceanblue.BaseModel;
import pokemonoceanblue.DatabaseUtility;
import pokemonoceanblue.ItemModel;
import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Stat;
import pokemonoceanblue.StatusEffect;

public class BattleModel extends BaseModel
{
    public PokemonModel[][] team = new PokemonModel[2][];
    public int[] currentPokemon = new int[2];
    public String[] battleOptions;
    public List<BattleEvent> events = new ArrayList<BattleEvent>();
    private Random ranNum = new Random();
    private AppManager app;
    public boolean isCaught;
    public String trainerName;
    public String trainerSpriteName;
    private boolean[] evolveQueue = new boolean[6];
    private boolean[] unableToMove = new boolean[2];
    public boolean[] isSeen;
    private Attack attacks[] = new Attack[2];
    private boolean[] moveProcessed = new boolean[2];
    public int musicId;
    public int badgeIndex = -1;
    public int weather;
    private ItemModel reward;
    public TurnEffectManager turnEffectManager = new TurnEffectManager();
    public BattleOperationsManager battleOperationsManager = new BattleOperationsManager();
    public boolean reloadSprites = false;

    /** 
     * Constructor
     * @param opponentTeam the opposing trainers pokemon team
     * @param playerTeam the players pokemon team
     */
    public BattleModel(PokemonModel[] opponentTeam, PokemonModel[] playerTeam, AppManager app, int weather)
    {
        this.team[0] = playerTeam;
        this.team[1] = opponentTeam;
        this.app = app;
        this.initializeBattle();
        this.weather = weather;
    }

    public BattleModel(PokemonModel[] playerTeam, int battleId, AppManager app, int enemyScalingFactor, int weather)
    {
        this.team[0] = playerTeam;
        this.loadTeam(battleId, enemyScalingFactor);
        this.app = app;
        this.initializeBattle();
        this.weather = weather;
    }

    /**
     * Set initial variable values
     */
    public void initializeBattle()
    {
        super.initialize();
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

        if (this.trainerName == null)
        {
            event = new BattleEvent("A wild " + this.team[1][0].name + " appeared!", 1, -1);
        }
        else
        {
            event = new BattleEvent(this.trainerName + " sent out " + this.team[1][0].name + ".", 1, -1);
        }
        event.setNewPokemon(0);
        this.events.add(event);

        event = new BattleEvent("Trainer sent out " + this.team[0][firstPokemon].name + ".", 0, -1);
        event.setNewPokemon(firstPokemon);
        this.events.add(event);
        this.actionCounter = 100;
    }

    @Override
    public void confirmSelection()
    {
        if (this.events.size() == 0)
        {
            this.actionCounter = this.ACTION_DELAY;
            
            if (this.battleOptions != null)
            {
                // clear the previous data on options, since it is going to change
                this.optionMax = 0;

                switch(this.battleOptions[this.optionIndex])
                {    
                    case "FIGHT":
                        
                        this.battleOptions = new String[this.team[0][this.currentPokemon[0]].moves.length];
                        for (int i = 0; i < this.team[0][this.currentPokemon[0]].moves.length; i++)
                        {
                            this.battleOptions[i] = String.valueOf(this.team[0][this.currentPokemon[0]].moves[i].name);
                        }
                        this.optionMax = this.battleOptions.length - 1;
                        break;

                    case "POKEMON":

                        if (turnEffectManager.canSwitch())
                        {
                            this.battleOptions = null;
                            this.app.openParty(this.currentPokemon[0], true); 
                            break;
                        }
                        break;

                    case "POKEBALLS":
                
                        this.battleOptions = null;
                        this.app.openInventory(); 
                        break;

                    default:
                        
                        this.battleOptions = null;
                        this.attacks[0] = new Attack(this.team[0][this.currentPokemon[0]].name, 
                            this.team[0][this.currentPokemon[0]].moves[optionIndex], 0);
                        this.actionCounter = 60;
                        int enemyMoveIndex = ranNum.nextInt(this.team[1][this.currentPokemon[1]].moves.length);
                        this.attacks[1] = enemyAttack(enemyMoveIndex);
                        int firstAttacker = battleOperationsManager.determineFirstAttacker(this.team[0][this.currentPokemon[0]], this.team[1][this.currentPokemon[1]],
                                                                                           this.optionIndex, enemyMoveIndex);
                        this.events.add(new BattleEvent(this.attacks[firstAttacker]));
                }
            }
        }
    }
    
    /**
     * @param itemId is the item that will be used
     */
    public void setItem(int itemId)
    {
        BattleEvent event;

        if (itemId == -1)
        {
            this.loadBattleMenu();
        }
        else
        {
            event = new BattleEvent("Trainer used a " + itemId + ".", 0, -1);
            event.setItem(itemId);
            this.events.add(event);

            event = new BattleEvent("Trainer used a " + itemId + ".", 1, -1);
            event.setItem(itemId);
            event.setNewPokemon(-1);
            this.events.add(event);
            this.actionCounter = 60;
            
            // find the probability of the Pokemon being captured
            double captureChance = battleOperationsManager.captureChanceCalc(this.team[1][this.currentPokemon[1]], itemId);
            
            int shakeCount = 3;

            // determine if the Pokemon should be captured
            if (ranNum.nextInt(100) + 1 <= captureChance)
            {
                event = new BattleEvent("Trainer caught wild " + this.team[1][this.currentPokemon[1]].name + "!", 0, -1);
                event.setItem(itemId);
                this.events.add(event);
                this.isCaught = true;
                this.team[1][this.currentPokemon[1]].pokeballId = itemId;
            }
            else
            {
                event = new BattleEvent("The wild " + this.team[1][this.currentPokemon[1]].name + " escaped!", 1, -1);
                event.setNewPokemon(0);
                this.events.add(2, event);
                this.events.add(new BattleEvent(this.enemyAttack(ranNum.nextInt(this.team[1][this.currentPokemon[1]].moves.length))));
                shakeCount = ranNum.nextInt(3);
            }

            // add events for the Pokeball shaking animation
            for (int i = 0; i < shakeCount; i++)
            {
                event = new BattleEvent("Trainer used a " + itemId + ".", 0, -1);
                event.setItem(itemId);
                event.setPokeballShake();
                this.events.add(2, event);
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
        else if (this.team[0][this.currentPokemon[0]].currentHP > 0)
        {
            BattleEvent event = new BattleEvent("Trainer withdrew " + this.team[0][Math.abs(this.currentPokemon[0])].name + ".", 0, -1);
            event.setNewPokemon(-1);
            this.events.add(event);
            event = new BattleEvent("Trainer sent out " + this.team[0][pokemon].name + ".", 0, -1);
            event.setNewPokemon(pokemon);
            this.events.add(event);
            this.actionCounter = 60;
            // this is the players turn
            this.moveProcessed[0] = true;
            // let the enemy attack after player switches
            this.events.add(new BattleEvent(this.enemyAttack(ranNum.nextInt(this.team[1][this.currentPokemon[1]].moves.length))));
        }
        else
        {
            BattleEvent event = new BattleEvent("Trainer sent out " + this.team[0][pokemon].name + ".", 0, -1);
            event.setNewPokemon(pokemon);
            this.events.add(0, event);
            this.actionCounter = 100;
            // the player lost their turn due to pokemon fainting, or moveProcessed already was true
            this.moveProcessed[0] = true;
        }
    }

    /**
     * Set which move the Pokemon will learn
     * @param newMove the move the Pokemon was trying to learn
     * @param newMoveIndex the index of the move to replace, or -1
     */
    public void setNewMove(MoveModel newMove, int newMoveIndex)
    {
        // don't teach the new move
        if (newMoveIndex == -1)
        {
            this.events.add(0, new BattleEvent(this.team[0][this.currentPokemon[0]].name + " did not learn " + newMove.name, 0, 0));
        }
        // teach the new move at newMoveIndex
        else
        {
            this.events.add(0, new BattleEvent(
                this.team[0][this.currentPokemon[0]].name + " forgot how to use " + this.team[0][this.currentPokemon[0]].moves[newMoveIndex].name 
                    + "and learned " + newMove.name + "!", 0, 0));
            this.team[0][this.currentPokemon[0]].moves[newMoveIndex] = newMove;
        }
        this.actionCounter = 60;
    }

    /**
     * @return the battle event that stores the enemy attack
     * @param enemyMoveIndex is the index of the move the enemy will use
     */
    private Attack enemyAttack(int enemyMoveIndex)
    {
        return new Attack(this.team[1][this.currentPokemon[1]].name, this.team[1][this.currentPokemon[1]].moves[enemyMoveIndex], 1);
    }

    /**
     * @param attacker the Pokemon attacking
     * @return the name of the sound effect to be played
     */
    private String getAttackSound(int attacker)
    {
        if (this.attacks[attacker] != null)
        {
            return this.attacks[attacker].sound;
        }
        return null;
    }

    /** 
     * returns attack event index in events list, returns -1 if attack event is removed
     * @param attacker is the attacking team
     */
    private int canAttack(int attacker)
    {
        PokemonModel attackingPokemon = this.team[attacker][this.currentPokemon[attacker]];
        int attackEventIndex = 0;
        // check if freze or sleep should end this turn
        if (attackingPokemon.statusEffect == StatusEffect.SLEEP || attackingPokemon.statusEffect == StatusEffect.FROZEN)
        {
            BattleEvent event = turnEffectManager.wakeUpOrThawOut(attackingPokemon, attacker);
            if (event != null) 
            {
                this.events.add(0, event);
                attackEventIndex = 1;
            }
        }
        // check if the attacking pokemon's status effect will prevent them from moving
        if ((attackingPokemon.statusEffect == StatusEffect.PARALYSIS && this.ranNum.nextInt(100) + 1 <= 50) || 
            (attackEventIndex == 0 && (attackingPokemon.statusEffect == StatusEffect.FROZEN || 
            (attackingPokemon.statusEffect == StatusEffect.SLEEP &&
              !(this.events.get(0).attack.move.moveEffect != null && this.events.get(0).attack.move.moveEffect.effectId == 93)))))
        {
            this.unableToMove[attacker] = true;
            this.events.remove(0);
            String[] statusEffectMessages = {" is paralyzed and unable to move."," is fast asleep."," is frozen solid."};
            this.events.add(new BattleEvent(attackingPokemon.name + statusEffectMessages[attackingPokemon.statusEffect - 1], attacker, attacker));
        }
        //check if attacker is confused, if so use random move
        else if (attackingPokemon.statusEffect == StatusEffect.CONFUSION)
        {
            //25% chance of snapping out of confusion
            if (this.ranNum.nextInt(100) + 1 <= 25)
            {
                BattleEvent event = new BattleEvent(
                    attackingPokemon.name + " snapped out of confusion!",
                    attacker,
                    attacker
                );
                event.setStatusEffect(0, attacker);
                this.events.add(0, event);
            }
            else
            {
                this.events.add(0, new BattleEvent(attackingPokemon.name + " is confused.", attacker, attacker));
                MoveModel ranMove = attackingPokemon.moves[this.ranNum.nextInt(attackingPokemon.moves.length)];
                this.events.get(1).attack.move = ranMove;
                this.events.get(1).text = attackingPokemon.name + " used " + ranMove.name + ".";
            }
            attackEventIndex = 1;
        }
        // check if Pokemon flinched
        if (!this.unableToMove[attacker])
        {
            if (this.moveProcessed[(attacker + 1) % 2] && this.attacks[(attacker + 1) % 2] != null && this.attacks[(attacker + 1) % 2].move != null && 
                this.ranNum.nextInt(100) + 1 <= this.attacks[(attacker + 1) % 2].move.flinchChance)
            {
                this.unableToMove[attacker] = true;
                this.events.remove(attackEventIndex);
                this.events.add(attackEventIndex, new BattleEvent(attackingPokemon.name + " flinched!", attacker, attacker));
            }
        }
        if (!this.unableToMove[attacker])
        {
            this.events.get(attackEventIndex).attack.useAttack(this.team[attacker][this.currentPokemon[attacker]],
                this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]], battleOperationsManager, turnEffectManager, weather);
            this.events.get(attackEventIndex).damage = this.events.get(attackEventIndex).attack.damage;
            return attackEventIndex;
        }
        else
        {
            return -1;
        }
    }

    private void moveEffect(MoveModel move, int attackEventIndex, int attacker)
    {
        int effectId = move.moveEffect.effectId;
        PokemonModel attackingPokemon = this.team[attacker][this.currentPokemon[attacker]];
        PokemonModel defendingPokemon = this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]];
        //effects that change the weather
        if (effectId < 141 && effectId > 136)
        {
            BattleEvent event;
            // changing the weather fails if the same weather already exists
            if (this.weather == effectId - 136)
            {
                event = new BattleEvent("But it failed.", attacker, attacker);
            }
            else
            {
                // create the event to start the weather
                String[] weatherMessages = {"the sunlight to turn harsh.","rain to fall.","a sandstorm.","hail to fall."};
                event = new BattleEvent(
                    attackingPokemon.name + " caused " + weatherMessages[effectId - 137], 
                    attacker, 
                    attacker
                );
                event.setWeather(effectId - 136);
            }
            this.events.add(event);
        }
        // multiTurnEffects
        else if (move.moveEffect.minCounter != 0 || move.moveEffect.maxCounter != 0)
        {
            turnEffectManager.addMultiTurnEffect(move, effectId, attacker, this.team, this.currentPokemon, this.events);
        }
        //splash
        else if (effectId == 86)
        {
            this.events.add(new BattleEvent("Nothing happened.", attacker, attacker));
        }
        //tri-attack
        else if (effectId == 37)
        {
            //decide whether move will paralyze, freeze, or burn
            int ailmentId = ranNum.nextInt(3);
            turnEffectManager.statusEffect(attacker, (attacker + 1) % 2, move.effectChance, ailmentId + (ailmentId % 2) * 2 + 1, 
                                           this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]], this.events);
        }
        //multiple hit moves
        // else if (effectId == 30 || effectId == 45 || effectId == 105)
        // {
        //     //duration is number of hits - 1 because first hit is done in update
        //     int duration = ranNum.nextInt(move.moveEffect.maxCounter - move.moveEffect.minCounter + 1) + move.moveEffect.minCounter - 1;
        //     //use remaining hp to check if defending pokemon will faint prematurely
        //     int remainingHp = this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]].currentHP - this.events.get(attackEventIndex).damage;
        //     for (int i = 0; i < duration; i++)
        //     {
        //         if (remainingHp <= 0 || this.attackMissed[attacker])
        //         {
        //             duration = i - (this.attackMissed[attacker] ? 1 : 0);
        //             break;
        //         }
        //         //check if move is triple kick to add multipliers for consecutive hits
        //         BattleEvent event = new BattleEvent(attackingPokemon.name + " used " + move.name + ".", 0, (attacker + 1) % 2, attacker, move, attacker);
        //         event.damage = event.attack.useAttack(attackingPokemon, defendingPokemon, battleOperationsManager, turnEffectManager, weather) * (effectId == 105 ? i + 1 : 1);
        //         this.events.add(1 + attackEventIndex, event);
        //         remainingHp -= event.damage;
        //     }
        //     BattleEvent event = new BattleEvent("Hit " + (duration + 1) + " time(s)!", attacker, -1);
        //     this.events.add(1 + duration + attackEventIndex, event);
        // }
        //self destruct and explosion
        else if (effectId == 11)
        {
            BattleEvent event = new BattleEvent(attackingPokemon.name + " used " + move.name + ".", attacker, -1);
            event.setDamage(attackingPokemon.stats[Stat.HP], attacker);
            this.events.add(event);
        }
        //high-jump-kick
        else if (effectId == 46 && this.events.get(attackEventIndex).damage == 0)
        {
            BattleEvent event = new BattleEvent(attackingPokemon.name + " kept going and crashed!", attacker, -1);
            event.setDamage(attackingPokemon.stats[Stat.HP] / 2, attacker);
            this.events.add(event);
        }
        //brick break
        else if (effectId == 187)
        {
            int i = 0;
            while (i < turnEffectManager.multiTurnEffects.size())
            {
                if ((turnEffectManager.multiTurnEffects.get(i).effectId == 36 || turnEffectManager.multiTurnEffects.get(i).effectId == 66) &&
                    turnEffectManager.multiTurnEffects.get(i).attacker == (attacker + 1) % 2)
                {
                    this.events.add(new BattleEvent(attackingPokemon.name + " broke through the " + (attacker == 0 ? "ally" : "opponent") + 
                        (turnEffectManager.multiTurnEffects.get(i).effectId == 36 ? " reflect!" : " light screen!"),
                        attacker, attacker));
                    turnEffectManager.multiTurnEffects.remove(i);
                }
                else
                {
                    i++;
                }
            }
        }
        //pain-split
        else if (effectId == 92)
        {
            int newCurrentHp = (attackingPokemon.currentHP + defendingPokemon.currentHP) / 2;
            String text = attackingPokemon.name + " and " + defendingPokemon.name + " shared their pain.";
            BattleEvent event = new BattleEvent(text, attacker, -1);
            event.setDamage(defendingPokemon.currentHP - newCurrentHp, (attacker + 1) % 2);
            this.events.add(event);
            event = new BattleEvent(text, attacker, -1);
            event.setDamage(attackingPokemon.currentHP - newCurrentHp, attacker);
            this.events.add(event);
        }
        //dream-eater
        else if (effectId == 9)
        {
            if (defendingPokemon.statusEffect != StatusEffect.SLEEP)
            {
                this.events.add(new BattleEvent("But it failed.", attacker, attacker));
            }
        }
        //snore
        else if (effectId == 93)
        {
            if (attackingPokemon.statusEffect != StatusEffect.SLEEP)
            {
                this.events.add(new BattleEvent("But it failed.", attacker, attacker));
            }
        }
        //haze
        else if (effectId == 26)
        {
            this.events.add(new BattleEvent("All Pokemon's stat changes were erased.", attacker, attacker));
            battleOperationsManager.statChanges = new int[2][8];
        }
        //aromatherapy/heal-bell
        else if (effectId == 103)
        {
            BattleEvent event = new BattleEvent(
                attackingPokemon.name + " cured its team's status conditions.",
                attacker,
                attacker
            );
            event.setStatusEffect(0, attacker);
            this.events.add(event);
            for (int i = 0; i < this.team[attacker].length; i++)
            {
                if (i != this.currentPokemon[attacker])
                {
                    this.team[attacker][i].statusEffect = (byte)StatusEffect.UNAFFLICTED;
                }
            }
        }
    }

    /**
     * returns to menu
     */
    public void loadBattleMenu()
    {
        if (this.events.size() == 0)
        {
            if (this.trainerName == null)
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
            this.actionCounter = this.ACTION_DELAY;
            this.optionMax = this.battleOptions.length - 1;
            this.optionWidth = 2;
            this.optionHeight = 2;
        }
    }

    /**
     * Wrapper for loadBattleMenu that gets called by BaseController when user presses ESC
     */
    @Override
    public void exitScreen()
    {
        this.loadBattleMenu();
    }

    /** 
     * checks if battle is complete
     */ 
    public boolean isComplete()
    {
        if (this.actionCounter > 0 || this.events.size() > 0 || this.battleOptions != null)
        {
            return false;
        }

        if (battleOperationsManager.teamFainted(this.team[0]) || battleOperationsManager.teamFainted(this.team[1]) || this.isCaught)
        {
            //remove confusion/curse
            for (int i = 0; i < this.team[0].length; i++)
            {
                if (this.team[0][i].statusEffect > 6)
                {
                    this.team[0][i].statusEffect = (byte)StatusEffect.UNAFFLICTED;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Needed for compatibility with BaseController
     */
    public int getSelection()
    {
        if (this.isComplete())
        {
            return -1;
        }
        else
        {
            return -2;
        }
    }

    public PokemonModel getNewPokemon()
    {
        if (this.isComplete() && this.trainerName == null && this.isCaught)
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
    private void checkFainted(int damagedTeamIndex)
    {
        if (this.team[damagedTeamIndex][this.currentPokemon[damagedTeamIndex]].currentHP == 0)
        {
            BattleEvent event;

            // remove all events performed by or affecting the fainted pokemon
            boolean faintEventExists = false;
            int i = 1;
            while (i < this.events.size())
            {
                if (this.events.get(i).newPokemonIndex == -1 && this.events.get(i).attacker == damagedTeamIndex)
                {
                    faintEventExists = true;
                }
                if (this.events.get(i).removalCondition == damagedTeamIndex)
                {
                    this.events.remove(i);
                }
                else
                {
                    i++;
                }
            }
            turnEffectManager.removeMultiTurnEffects(this.team[damagedTeamIndex][this.currentPokemon[damagedTeamIndex]], damagedTeamIndex, false);
            if (!faintEventExists)
            {
                event = new BattleEvent(this.team[damagedTeamIndex][this.currentPokemon[damagedTeamIndex]].name + " fainted.", damagedTeamIndex, -1);
                event.setNewPokemon(-1);
                this.events.add(event);

                if ((this.moveProcessed[0] || this.moveProcessed[1]) && (!this.moveProcessed[0] || !this.moveProcessed[1]))
                {
                    turnEffectManager.endOfTurnEffects(this.team, this.currentPokemon, this.events, this.weather);
                }
                if (damagedTeamIndex == 0)
                {
                    this.team[0][this.currentPokemon[0]].updateHappiness(-5);
                }
                else 
                {
                    this.team[0][this.currentPokemon[0]].updateHappiness(1);
                    this.team[0][this.currentPokemon[0]].updateIVs(this.team[1][this.currentPokemon[1]].ivGain);
                    int xp = xpCalc(this.team[1][this.currentPokemon[1]].level);
                    event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " gained " + xp + " experience.", 0, 0);
                    event.setXP(xp);
                    this.events.add(event);
                    //send out enemy's next pokemon if any remain
                    if (!battleOperationsManager.teamFainted(this.team[1]))
                    {
                        event = new BattleEvent(this.trainerName + " sent out " + this.team[1][this.currentPokemon[1] + 1].name, 1, -1);
                        event.setNewPokemon(this.currentPokemon[1] + 1);
                        this.events.add(event);
                    }
                    // give the player a reward for winning the battle if all their Pokemon are defeated
                    else if (this.reward != null)
                    {
                        if (this.reward.itemId == 1000)
                        {
                            this.events.add(new BattleEvent("Player received $" + this.reward.quantity + " for their victory", 0, 0));
                        }
                        else
                        {
                            this.events.add(new BattleEvent("Player received " + this.reward.quantity + " " + this.reward.name + " for their victory", 0, 0));
                        }
                        
                        this.actionCounter = 60;
                    }
                }
            }
        }
    }

    private int xpCalc(int enemyLevel)
    {
        return (int) Math.pow(enemyLevel, 2) * 2;
    }

    @Override
    public void update()
    {
        // switch Pokemon at the start of counter to show the switching animation
        if (this.actionCounter == 100 && this.events.get(0).newPokemonIndex > -1)
        {
            battleOperationsManager.switchPokemon(this.events.get(0).attacker, this);
        }
        //calculate damage for an upcoming attack event and add second attack event if applicable
        else if (this.actionCounter == 60 && this.events.size() > 0 && this.events.get(0).attack != null && !this.moveProcessed[this.events.get(0).attacker])
        {
            int attacker = this.events.get(0).attacker;
            int attackEventIndex = this.canAttack(attacker);
            if (this.attacks[attacker] != null && this.attacks[attacker].isUsed)
            {
                List<BattleEvent> attackMessages = this.attacks[attacker].getAttackMessages();
                for (BattleEvent message : attackMessages)
                {
                    this.events.add(message);
                }
            }
            //check if attack will occur or if move is self-destruct, explosion, or hi jump kick before applying effects
            if (attackEventIndex > -1 && this.events.get(attackEventIndex).attack != null && (
                (this.attacks[attacker] != null && !this.attacks[attacker].attackMissed) 
                || (this.events.get(attackEventIndex).attack.move.moveEffect != null 
                    && (this.events.get(attackEventIndex).attack.move.moveEffect.effectId == 46 || this.events.get(attackEventIndex).attack.move.moveEffect.effectId == 11))))
            {
                MoveModel move = this.events.get(attackEventIndex).attack.move;
                if (move.moveEffect != null && move.moveEffect.effectId > -1)
                {
                    this.moveEffect(move, attackEventIndex, attacker);
                }
                if (move.recoil != 0)
                {
                    BattleEvent event = battleOperationsManager.createRecoilEvent(attacker, move, 
                        this.team[attacker][this.currentPokemon[attacker]], 
                        this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]],
                        this.weather, this.events.get(attackEventIndex).damage);
                    if (event != null)
                    {
                        this.events.add(event);
                    }
                }
                if (move.ailmentId > 0)
                {
                    turnEffectManager.statusEffect(attacker, (attacker + 1) % 2, move.effectChance, move.ailmentId,
                                                   this.team[(attacker + 1) % 2][this.currentPokemon[(attacker + 1) % 2]], this.events);
                }
                if (move.moveStatEffects.length > 0)
                {
                    battleOperationsManager.addStatChanges(attacker, move, this);
                }
            }
            if (!this.moveProcessed[(attacker + 1) % 2])
            {
                this.events.add(new BattleEvent(this.attacks[(attacker + 1) % 2]));
            }
            this.moveProcessed[attacker] = true;
        }
        // shorter events for Pokeball throwing animations
        else if (this.actionCounter == 60 && this.events.size() > 0 && this.events.get(0).itemId > 0)
        {
            if (this.events.get(0).pokeballShake)
            {
                this.actionCounter = 30;
            }
            else if (this.events.get(0).newPokemonIndex == -2)
            {
                this.actionCounter = 30;
            }
        }

        //determine the sound that will be played for current event if applicable
        if (this.actionCounter == 60 && this.events.size() > 0)
        {
            if (this.events.get(0).damage > 0)
            {
                if (this.events.get(0).attack != null && this.events.get(0).attack.move != null)
                {
                    this.soundEffect = this.getAttackSound(this.events.get(0).attacker);
                }
                else
                {
                    this.soundEffect = "damage";
                }
            }
            else if (this.events.get(0).newPokemonIndex > -1)
            {
                this.soundEffect = String.valueOf(this.team[this.events.get(0).attacker][this.events.get(0).newPokemonIndex].base_pokemon_id);
            }
        }
        if (this.actionCounter > 0)
        {
            this.actionCounter--;
        }
        // logic that happen at the end of an event
        else if (this.events.size() > 0)
        {
            if (this.events.get(0).xp > 0)
            {
                this.addXPGainEvents();
            }
            else if (this.events.get(0).statusEffect > -1)
            {
                turnEffectManager.addStatusEffect(this.events.get(0).statusEffect, this.events.get(0).target, true, this);
            }
            else if (this.events.get(0).damage > 0)
            {
                this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP -= 
                    Math.min((this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP), 
                    (this.events.get(0).damage));

                this.checkFainted(this.events.get(0).target);
            }
            else if (this.events.get(0).damage < 0)
            {
                // self-healing
                this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP = 
                    Math.min((this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].stats[Stat.HP]), 
                    (this.team[this.events.get(0).target][this.currentPokemon[this.events.get(0).target]].currentHP + 
                    Math.abs(this.events.get(0).damage)));
            }
            // open summary screen for player to chose which move to replace with the new move
            else if (this.events.get(0).newMove != null)
            {
                this.app.openSummaryNewMove(this.currentPokemon[0], this.events.get(0).newMove);
            }
            // change weather
            else if (this.events.get(0).newWeatherId > -1)
            {
                this.weather = this.events.get(0).newWeatherId;
                // check if any Pokemon changed forms from the weather change
                for (int i = 0; i < this.team.length; i++)
                {
                    if (this.team[i][this.currentPokemon[i]].checkFormChange((byte)this.weather, (byte)-1))
                    {
                        this.events.add(1, new BattleEvent(this.team[i][this.currentPokemon[i]].name + " transformed!", i, i));
                        this.reloadSprites = true;
                    }
                }
            }
            //check if player should blackout
            if (battleOperationsManager.teamFainted(this.team[0]))
            {
                boolean eventExists = false;
                for (int i = this.events.size() - 1; i >= 0; i--)
                {
                    if (this.events.get(i).text.equals("Player blacked out!"))
                    {
                        eventExists = true;
                        break;
                    }
                }
                if (!eventExists)
                {
                    this.events.add(new BattleEvent("Player was defeated by " + (this.trainerName == null ? "the wild " + this.team[1][0].name : trainerName) + "!", 0, -1));
                    this.events.add(new BattleEvent("Player blacked out!", 0, -1));
                }
            }
            this.events.remove(0);
            if (this.events.size() > 0)
            {
                // use a larger counter when sending in Pokemon
                // to show switching animation
                if (this.events.get(0).newPokemonIndex > -1)
                {
                    this.actionCounter = 100;
                }
                else
                {
                    this.actionCounter = 60;
                }
            }
        }
        //player sends out new pokemon to replace fainted one or battle returns to battle menu
        else if (this.battleOptions == null && this.actionCounter == 0)
        {
            if (this.moveProcessed[0] || this.moveProcessed[1])
            {
                turnEffectManager.endOfTurnEffects(this.team, this.currentPokemon, this.events, this.weather);
            }
            if (this.events.size() > 0)
            {
                this.actionCounter = 60;
            }
            this.moveProcessed = new boolean[2];
            this.attacks = new Attack[2];
            this.unableToMove = new boolean[2];
            if (!battleOperationsManager.teamFainted(this.team[0]) && this.team[0][this.currentPokemon[0]].currentHP == 0)
            {
                this.battleOptions = null;
                this.optionMax = 0;
                this.app.openParty(this.currentPokemon[0], true);
            }
            else if (this.events.size() == 0 && !this.isCaught)
            {
                this.loadBattleMenu();
            }
        }
    }

    /**
     * Add events for awarding a Pokemon XP
     * and add events for learning new moves if necessary
     */
    private void addXPGainEvents()
    {
        int xpGain = this.events.get(0).xp;
        int xpMax = this.team[0][this.currentPokemon[0]].calcXP(1);
        List<MoveModel> newMoves;
    
        // check if the pokemon levels up from the xp gain
        if (this.team[0][this.currentPokemon[0]].xp + xpGain >= xpMax)
        {
            BattleEvent event = new BattleEvent(this.team[0][this.currentPokemon[0]].name + " reached level " + (this.team[0][this.currentPokemon[0]].level + 1) + "!", 0, 0);
            this.events.add(1, event);   

            // add a new event that shows the progress bar moving with XP remaining after leveling up
            event = new BattleEvent("", 0, 0);
            event.setXP(xpGain + this.team[0][this.currentPokemon[0]].xp - xpMax);
            this.events.add(2, event);

            // add events for any new moves that the Pokemon learned from leveling up
            newMoves = this.team[0][this.currentPokemon[0]].addXP(xpMax - this.team[0][this.currentPokemon[0]].xp);
            for (MoveModel move : newMoves)
            {
                // learn the move immediately if they have any open slots
                if (this.team[0][this.currentPokemon[0]].addMove(move))
                {
                    this.events.add(2, new BattleEvent(this.team[0][this.currentPokemon[0]].name + " learned " + move.name + "!", 0, 0));
                }
                // otherwise the player will need to choose a move to replace
                else
                {
                    event = new BattleEvent(
                        this.team[0][this.currentPokemon[0]].name + " wants to learn " + move.name + ", however it already knows four moves.", 
                        0, 0);
                    event.setNewMove(move);
                    this.events.add(2, event);
                }
            }
            // flag that the pokemon leveled up and may evolve
            this.evolveQueue[this.currentPokemon[0]] = true;
        }
        else
        {
            // add the remianing XP that isn't enough to level up
            this.team[0][this.currentPokemon[0]].addXP(this.events.get(0).xp);
        }
    }

    /**
     * Change the cursor position
     * @param dx x-direction movement
     * @param dy y-direction movement
     */
    @Override
    public void moveIndex(final int dx, final int dy)
    {
        if (this.events.size() == 0 && this.battleOptions != null)
        {
            super.moveIndex(dx, dy);
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
    private void loadTeam(int battleId, int enemyScalingFactor)
    {
        List<PokemonModel> loadTeam = new ArrayList<PokemonModel>();

        // Pokemon league lets Pokemon's level scale up by max 75
        if (battleId >= 1300)
        {
            enemyScalingFactor = Math.min(75, enemyScalingFactor);
        }
        // everywhere else Pokemon's level scale up by max 50
        else
        {
            enemyScalingFactor = Math.min(50, enemyScalingFactor);
        }

        // limit the number of Pokemon the enemy can have in early stages of the game
        int teamLimit = 3;
        if (enemyScalingFactor >= 35)
        {
            teamLimit = 6;
        }
        else if (enemyScalingFactor >= 25)
        {
            teamLimit = 5;
        }
        else if (enemyScalingFactor >= 10)
        {
            teamLimit = 4;
        }
        
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            // if there are multiple characters with the same conversation id
            // the trainer shown in battle is the one with the lowest character id
            String query = String.format("""
                SELECT 
                    COALESCE(em2.pre_species_id, em1.pre_species_id, b.pokemon_id), 
                    b.level + @level_scaling, 
                    c.name, 
                    c.sprite_name, 
                    COALESCE(c.music_id, 101) AS music_id
                FROM battle b
                INNER JOIN (select ch.name, ch.sprite_name, ch.music_id, cv.conversation_id, cv.battle_id,
                ROW_NUMBER() OVER(ORDER BY ch.character_id ASC) AS char_num
                FROM conversation cv
                INNER JOIN character ch
                ON ch.conversation_id = cv.conversation_id
                AND cv.battle_id = %s) c
                ON b.battle_id = c.battle_id 
                -- evolution is dynamic based on level scaling
                LEFT JOIN evolution_methods em1
                ON b.pokemon_id = em1.evolved_species_id
                AND b.level + @level_scaling < em1.enemy_minimum_level
                LEFT JOIN evolution_methods em2
                ON em1.pre_species_id = em2.evolved_species_id
                AND b.level + @level_scaling < em2.enemy_minimum_level
                WHERE char_num = 1
                LIMIT %s
                """.replace("@level_scaling", String.valueOf(enemyScalingFactor)), 
                battleId, teamLimit);

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                loadTeam.add(new PokemonModel(rs.getInt(1), rs.getInt(2), false));
                this.trainerName = rs.getString(3);
                this.trainerSpriteName = rs.getString(4);
                this.musicId = rs.getInt(5);

                // set the default trainer battle music
                if (this.musicId == -1)
                {
                    this.musicId = 101;
                }
            }       
            
            this.team[1] = new PokemonModel[loadTeam.size()];
            loadTeam.toArray(this.team[1]);

            // store if the player gets a badge for winning this battle
            switch (battleId)
            {
                case 10:
                    this.badgeIndex = 0;
                    break;
                case 12:
                    this.badgeIndex = 1;
                    break;
                case 14:
                    this.badgeIndex = 2;
                    break;
                case 15:
                    this.badgeIndex = 3;
                    break;
                case 16:
                    this.badgeIndex = 4;
                    break;
                case 17:
                    this.badgeIndex = 5;
                    break;
                case 18:
                    this.badgeIndex = 6;
                    break;
                case 19:
                    this.badgeIndex = 7;
                    break;
            }

            // load the reward for defeating the trainer in battle
            query = "SELECT item_id, quantity FROM battle_reward WHERE battle_id = " + battleId;
            rs = db.runQuery(query);

            // not all battles have a reward
            if (rs.next()) 
            {
                int itemId = rs.getInt(1);
                int quantity = rs.getInt(2);
                
                // money gets multiplied by the level of the their last pokemon
                if (itemId == 1000)
                {
                    quantity *= team[1][team[1].length - 1].level;
                }
                this.reward = new ItemModel(itemId, quantity);
            } 
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * @return a boolean for each Pokemon on the team, true if the Pokemon leveled up
     */
    public boolean[] getEvolveQueue()
    {
        return this.evolveQueue;
    }

    public ItemModel getBattleReward()
    {
        return this.reward;
    }
}    
package pokemonoceanblue.battle;

import java.util.Random;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Stat;
import pokemonoceanblue.StatusEffect;
import pokemonoceanblue.Type;
import pokemonoceanblue.Weather;

public class BattleOperationsManager {
    private Random ranNum = new Random();
    public int[][] statChanges = new int[2][8];
    private TurnEffectManager turnEffectManager;

    public BattleOperationsManager(TurnEffectManager turnEffectManager)
    {
        this.turnEffectManager = turnEffectManager;
    }

    /**
     * Logic for switching the current Pokemon in battle
     * @param attacker
     * @param model
     */
    public void switchPokemon(int attacker, BattleModel model)
    {
        if (model.currentPokemon[attacker] >= 0)
        {
            model.turnEffectManager.removeMultiTurnEffects(model.team[attacker][model.currentPokemon[attacker]], attacker, false);
        }
        this.statChanges[attacker] = new int[8];
        model.currentPokemon[attacker] = model.events.get(0).newPokemonIndex;
        if (attacker == 1)
        {
            model.isSeen[model.events.get(0).newPokemonIndex] = true;
        }
        // add multi turn effect for any status effect the new pokemon may have
        if (model.team[attacker][model.currentPokemon[attacker]].statusEffect > 0)
        {
            model.turnEffectManager.addStatusEffect(model.team[attacker][model.currentPokemon[attacker]].statusEffect, attacker, false, model);
        }
    }

    /**
     * Check if a move lands
     * @param attacker
     * @param moveAccuracy
     * @return True if the move hits
     */
    public boolean isHit(int attacker, int moveAccuracy)
    {
        int defender = (attacker + 1) % 2;
        if (this.statChanges[attacker][6] != 0 || this.statChanges[defender][7] != 0)
        {
            moveAccuracy = (int)(moveAccuracy * (2.0 / (Math.abs(this.statChanges[attacker][6]) + 2)) / 
                (this.statChanges[defender][7] > 0 ? (Math.abs(this.statChanges[defender][7]) + 2) / 2.0 : 2.0 / (Math.abs(this.statChanges[defender][7]) + 2)));
        }
        return moveAccuracy == -1 || this.ranNum.nextInt(100) + 1 <= moveAccuracy;
    }

    /**
     * Determines whether an attack is a critical hit
     * @param critChance the critical hit modifier, with a default value of 1
     * @return True if the attack is a critical hit
     */
    public boolean isCrit(int critChance)
    {
        return critChance > 0 && ranNum.nextInt(100) <= 100.0 / (16 / critChance);
    }

    /**
     * Determines if all the Pokemon in team have fainted
     * @param team the team that is being looked at (player team or enemy team)
     * @return true if all of the Pokemon in team have fainted
     */
    public boolean teamFainted(PokemonModel[] team)
    {
        int faintedPokemon = 0;
        for (int i = 0; i < team.length; i++)
        {
            if (team[i].currentHP == 0)
            {
                faintedPokemon++;
            }
            if (faintedPokemon == team.length)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine's which Pokemon attacks first
     * @param playerPokemon
     * @param enemyPokemon
     * @param playerMoveIndex
     * @param enemyMoveIndex
     * @return
     */
    public int determineFirstAttacker(PokemonModel playerPokemon, PokemonModel enemyPokemon, int playerMoveIndex, int enemyMoveIndex)
    {
        int firstAttacker;
        int playerSpeed = playerPokemon.getStat(Stat.SPEED, this.statChanges[0][Stat.SPEED]);
        int enemySpeed = enemyPokemon.getStat(Stat.SPEED, this.statChanges[0][Stat.SPEED]);
        if (playerPokemon.moves[playerMoveIndex].priority > enemyPokemon.moves[enemyMoveIndex].priority)
        {
            firstAttacker = 0;
        }
        else if (playerPokemon.moves[playerMoveIndex].priority < enemyPokemon.moves[enemyMoveIndex].priority)
        {
            firstAttacker = 1;
        }
        else if (playerPokemon.statusEffect != StatusEffect.PARALYSIS && enemyPokemon.statusEffect == StatusEffect.PARALYSIS)
        {
            firstAttacker = 0;
        }
        else if (enemyPokemon.statusEffect != StatusEffect.PARALYSIS && playerPokemon.statusEffect == StatusEffect.PARALYSIS)
        {
            firstAttacker = 1;
        }
        else if (playerSpeed < enemySpeed)
        {
            firstAttacker = 1;
        }
        else if (playerSpeed > enemySpeed)
        {
            firstAttacker = 0;
        }
        else 
        {
            firstAttacker = ranNum.nextInt(2);
        }
        return firstAttacker;
    }

    /** 
     * creates event for the stat changes applied by a move used
     * @param attacker the attacking team
     * @param move the move that inflicts the stat change
     */
    public void addStatChanges(int attacker, MoveModel move, BattleModel model)
    {
        if (ranNum.nextInt(100) + 1 <= move.effectChance)
        {
            String[] statChangeMessages = {" fell sharply.", " fell.", "", " rose.", " rose sharply."};
            String[] changedStat = {"", " attack", " defense", " special attack", " special defense", " speed", " accuracy", " evasiveness"};
            int target = (attacker + 1) % 2;
            //loop through all stat changes in case there are multiple
            for (int i = 0; i < move.moveStatEffects.length; i++)
            {
                BattleEvent event;
                int statId = move.moveStatEffects[i].statId;
                int statChange = move.moveStatEffects[i].statChange;
                boolean willFail = false;
                //when target id is 7 the move applies stat changes to the user, otherwise applied to foe
                if (move.targetId == 7)
                {
                    target = attacker;
                }
                //check if mist will prevent stat change
                if (model.turnEffectManager.multiTurnEffects.size() > 0)
                {
                    for (int j = 0; j < model.turnEffectManager.multiTurnEffects.size(); j++)
                    {
                        if (model.turnEffectManager.multiTurnEffects.get(j).effectId == 47 && model.turnEffectManager.multiTurnEffects.get(j).attacker == target)
                        {
                            willFail = true;
                            model.events.add(new BattleEvent("The mist prevented stat changes.", target, target));
                        }
                    }
                }
                if (!willFail)
                {
                    //check if stat cannot be changed any further
                    if ((this.statChanges[target][statId] == 6 && statChange > 0) || (this.statChanges[target][statId] == -6 && statChange < 0))
                    {
                        event = new BattleEvent(model.team[target][model.currentPokemon[target]].name + "'s " + changedStat[statId] + " cannot be " +
                            (this.statChanges[target][statId] < 0 ? "decreased" : "increased") + " any further.", target, target);
                    }
                    else
                    {
                        event = new BattleEvent(model.team[target][model.currentPokemon[target]].name + "'s " +
                            changedStat[statId] + statChangeMessages[statChange + 2], target, target);
                        //apply stat change with limits of |6|
                        this.statChanges[target][statId] = (statChange / Math.abs(statChange)) * Math.min(6, Math.abs(statChange + this.statChanges[target][statId]));
                    }
                    model.events.add(event);
                }
            }
        }
    }

    /** 
     * creates event for the self healing or recoil on attacking pokemon
     * @param attacker the attacking team
     * @param move the move that inflicts the heal or recoil
     */
    public BattleEvent createRecoilEvent(int attacker, MoveModel move, PokemonModel attackingPokemon, PokemonModel defendingPokemon, int attackDamage)
    {
        int damage;
        Weather weather = this.turnEffectManager.weather;

        //moves that have power=0 use attackers max hp to calc healing/recoil
        if (move.power == 0)
        {
            damage = (int)(attackingPokemon.stats[Stat.HP] * (move.recoil / 100.0));
            //morning sun, moonlight, synthesis heal for 2/3 hp when sun is shining 1/2 hp in clear weather 1/4 in other weather
            if (move.moveEffect != null && move.moveEffect.effectId == 141)
            {
                if (weather == Weather.SUNNY)
                    damage = (int)(damage * 4.0 / 3.0);
                else if (weather != Weather.NEUTRAL)
                    damage /= 2;
            }
        }
        else if (move.recoil > 0)
        {
            damage = (int)(Math.ceil(Math.min(attackDamage, attackingPokemon.currentHP) * (move.recoil / 100.0)));
        }
        else
        {
            damage = (int)(Math.floor(attackDamage * (move.recoil / 100.0)));
        }
        if (damage != 0)
        {
            BattleEvent event = new BattleEvent(attackingPokemon.name + (move.recoil < 0 ? " healed itself." : " is hit with recoil."), attacker, attacker);
            event.setDamage(damage, attacker);
            return event;
        }
        return null;
    }

    /**
     * Calculate the probability of the wild Pokemon being captured
     * @param pokemon
     * @param itemId
     * @return
     */
    public double captureChanceCalc(PokemonModel pokemon, int itemId)
    {
        // find the probability of the Pokemon being captured
        int pokemonCaptureRate = pokemon.captureRate;
        double pokeballModifier = 1.0;
        double statusModifier = 1.0;

        // master ball has 100% capture chance
        if (itemId == 0)
        {
            pokeballModifier = 999;
        }
        // ultra ball has 2x catch chance
        else if (itemId == 1)
        {
            pokeballModifier = 2;
        }
        // great ball has 1.5x catch chance
        else if (itemId == 2)
        {
            pokeballModifier = 1.5;
        }
        // net ball more effective on bug and water types
        else if (itemId == 5 
            && (Type.typeIncludes(Type.BUG, pokemon.types) 
                || Type.typeIncludes(Type.WATER, pokemon.types)))
        {
            pokeballModifier = 2;
        }

        // sleep and freeze give 2x capture chance
        if (pokemon.statusEffect == StatusEffect.SLEEP || pokemon.statusEffect == StatusEffect.FROZEN)
        {
            statusModifier = 2;
        }
        // any other status effect gives 1.5x capture chance
        else if (pokemon.statusEffect != StatusEffect.UNAFFLICTED)
        {
            statusModifier = 1.5;
        }
        
        double captureChance = pokemonCaptureRate * pokeballModifier * statusModifier
            - (pokemon.level / 10) * pokemon.currentHP / (double)(pokemon.stats[Stat.HP]);

        return captureChance;
    }

    /**
     * Determine's the type effectiveness multiplier for the Pokemon's current attack
     * @param attackingPokemon
     * @param defendingPokemon
     * @param move
     * @return
     */
    public static float getTypeModifier(PokemonModel attackingPokemon, PokemonModel defendingPokemon, MoveModel move)
    {
        float typeModifier = 1.0f;

        for (int i = 0; i < defendingPokemon.types.length; i++)
        {
            typeModifier *= Type.typeEffectiveness[move.typeId][defendingPokemon.types[i]];
        }

        // check for abilities that grant immunity to certain types
        if (move.typeId == Type.GROUND 
        && defendingPokemon.ability != null
        && defendingPokemon.ability.abilityId == 26)
        {
            typeModifier = 0.0f;
        }

        // check for effects that affect the type modifier
        if (move.moveEffect != null)
        {
            // super fang inflicts typeless damage
            if (move.moveEffect.effectId == 41)
            {
                typeModifier = 1.0f;
            }
        }

        return typeModifier;
    }

    /**
     * Determine's the multipliers to the Pokemon's attack, excluding critical hits or the 
     * type effectiveness multiplier
     * @param attackingPokemon
     * @param defendingPokemon
     * @param move
     * @param isCrit
     * @param defender
     * @return
     */
    public float getDamageMultiplier(PokemonModel attackingPokemon, PokemonModel defendingPokemon, 
                                     MoveModel move, boolean isCrit, int defender)
    {
        float otherModifiers = 1.0f;
        Weather weather = this.turnEffectManager.weather;

        // check for effects that affect the type modifier
        if (move.moveEffect != null)
        {
            int effectId = move.moveEffect.effectId;

            if (effectId == 170 && (attackingPokemon.statusEffect == StatusEffect.PARALYSIS || 
                                        attackingPokemon.statusEffect == StatusEffect.BURN || 
                                        attackingPokemon.statusEffect == StatusEffect.POISON ||
                                        attackingPokemon.statusEffect == StatusEffect.BADLY_POISON))
            {
                // attacks that do double damage when affected by status conditions
                otherModifiers *= 2.0f;
            }
            else if (effectId == 222 && defendingPokemon.currentHP < Math.ceil(defendingPokemon.stats[Stat.HP] / 2.0))
            {
                // attacks that do double damage on foes with less than 50% HP
                otherModifiers *= 2.0f;
            }
            else if (effectId == 284 && defendingPokemon.statusEffect == StatusEffect.POISON)
            {
                // attacks that do double damage on poisoned targets
                otherModifiers *= 2.0f;
            }
        }

        // apply weather modifiers
        if ((weather == Weather.RAIN || weather == Weather.SUNNY) && (move.typeId == 10 || move.typeId == 11))
        {
            otherModifiers *= (weather == Weather.RAIN && move.typeId == 10) || (weather == Weather.SUNNY && move.typeId == 11) ? 0.5 : 1.5;
        }

        // check for multiTurnEffects that apply a damage multiplier
        if (turnEffectManager.multiTurnEffects.size() > 0)
        {
            for (int i = 0; i < turnEffectManager.multiTurnEffects.size(); i++)
            {
                if (!isCrit && turnEffectManager.multiTurnEffects.get(i).attacker == defender &&
                    ((turnEffectManager.multiTurnEffects.get(i).effectId == 36 && move.damageClassId == 3) ||
                    (turnEffectManager.multiTurnEffects.get(i).effectId == 66 && move.damageClassId == 2)))
                {
                    otherModifiers *= 0.5;
                }
                else if ((move.typeId == Type.ELECTRIC && turnEffectManager.multiTurnEffects.get(i).effectId == 202) ||
                            (move.typeId == Type.FIRE && turnEffectManager.multiTurnEffects.get(i).effectId == 211))
                {
                    otherModifiers *= 0.5;
                }
            }
        }

        return otherModifiers;
    }

    /**
     * Calculates a Pokemon's attack's critical hit chance
     * @param move
     * @param attacker
     * @return
     */
    public int getCritChance(MoveModel move, int attacker, PokemonModel attackingPokemon, PokemonModel defendingPokemon)
    {
        int critChance = 1;

        // check for effects that affect the type modifier
        if (move.moveEffect != null)
        {
            if (move.moveEffect.effectId == 44)
            {
                // moves with a boosted critical hit chance
                critChance++;
            }
        }

        // Super Luck ability increases crit chance
        if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 105)
        {
          critChance++;
        }

        // focus-energy and lucky-chant
        if (turnEffectManager.multiTurnEffects.size() > 0)
        {
            for (int i = 0; i < turnEffectManager.multiTurnEffects.size(); i++)
            {
                if (turnEffectManager.multiTurnEffects.get(i).effectId == 241 
                    && turnEffectManager.multiTurnEffects.get(i).target == attacker)
                {
                    critChance = 0;
                    break;
                }
                else if (turnEffectManager.multiTurnEffects.get(i).effectId == 48 
                    && turnEffectManager.multiTurnEffects.get(i).attacker == attacker)
                {
                    critChance += 2;
                }
            }
        }

        // Shell Armor ability prevents critical hits
        if (defendingPokemon.ability != null && defendingPokemon.ability.abilityId == 75)
        {
          critChance = 0;
        }

        return critChance;
    }
}

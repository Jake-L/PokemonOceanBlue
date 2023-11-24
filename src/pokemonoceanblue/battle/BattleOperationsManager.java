package pokemonoceanblue.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Stat;
import pokemonoceanblue.StatusEffect;
import pokemonoceanblue.Type;
import pokemonoceanblue.Weather;
import pokemonoceanblue.StatEffect;
import pokemonoceanblue.battle.TurnEffectManager.MultiTurnEffect;

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
        PokemonModel attackingPokemon = model.team[attacker][model.currentPokemon[attacker]];

        if (attacker == 1)
        {
            model.isSeen[model.events.get(0).newPokemonIndex] = true;
        }
        // add multi turn effect for any status effect the new pokemon may have
        if (attackingPokemon.statusEffect > 0)
        {
            model.turnEffectManager.addStatusEffect(attackingPokemon.statusEffect, attacker, false, model);
        }

        // abilities that are triggered when a Pokemon is sent out, such as INTIMIDATE or DRIZZLE
        if (attackingPokemon.ability != null)
        {
            BattleEvent abilityEvent = null;

            if (attackingPokemon.ability.abilityId == 2)
            {
                abilityEvent = new BattleEvent(
                    attackingPokemon.name + "'s DRIZZLE made it rain.", 
                    attacker, 
                    attacker
                );
                abilityEvent.setWeather(Weather.RAIN);
            }
            else if (attackingPokemon.ability.abilityId == 70)
            {
                abilityEvent = new BattleEvent(
                    attackingPokemon.name + "'s DROUGHT intensified the heat.", 
                    attacker, 
                    attacker
                );
                abilityEvent.setWeather(Weather.SUNNY);
            }
            else if (attackingPokemon.ability.abilityId == 76)
            {
                abilityEvent = new BattleEvent(
                    attackingPokemon.name + "'s AIR LOCK cleared the weather.", 
                    attacker, 
                    attacker
                );
                abilityEvent.setWeather(Weather.NEUTRAL);
            }
            else if (attackingPokemon.ability.abilityId == 117)
            {
                abilityEvent = new BattleEvent(
                    attackingPokemon.name + "'s SNOW WARNING kicked up a snowstorm.", 
                    attacker, 
                    attacker
                );
                abilityEvent.setWeather(Weather.HAIL);
            }
            else if (attackingPokemon.ability.abilityId == 45)
            {
                abilityEvent = new BattleEvent(
                    attackingPokemon.name + "'s SAND STREAM kicked up a sandstorm.", 
                    attacker, 
                    attacker
                );
                abilityEvent.setWeather(Weather.SANDSTORM);
            }

            if (abilityEvent != null)
            {
                model.events.add(1, abilityEvent);
            }
        }
    }

    /**
     * Check if a move lands
     * @param attacker
     * @param moveAccuracy
     * @param defendingPokemon the Pokemon being targetted by the attack
     * @return True if the move hits
     */
    public boolean isHit(int attacker, int moveAccuracy, PokemonModel defendingPokemon)
    {
        if (moveAccuracy == -1)
        {
            return true;
        }

        int defender = (attacker + 1) % 2;

        moveAccuracy = (int)(moveAccuracy * (2.0 / (Math.abs(this.statChanges[attacker][6]) + 2)) / 
            (this.statChanges[defender][7] > 0 ? (Math.abs(this.statChanges[defender][7]) + 2) / 2.0 : 2.0 / (Math.abs(this.statChanges[defender][7]) + 2)));
        
        return this.ranNum.nextInt(100) + 1 <= moveAccuracy;
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
     * Calculates a Pokemon's stat, taking into effect stat changes, abilities, etc
     * @param pokemon the PokemonModel object
     * @param attacker the index of the team of the Pokemon
     * @param statIndex the index of the stat in the Stat enum
     * @return the Pokemon's adjusted stat
     */
    public int getStat(PokemonModel pokemon, int attacker, int statIndex)
    {
        int stat = pokemon.stats[statIndex];
        int modifier = this.statChanges[attacker][statIndex];

        if (pokemon.ability != null) 
        {
            // Chlorophyll doubles speed in sunny weather
            if (pokemon.ability.abilityId == 34 
                && this.turnEffectManager.weather == Weather.SUNNY
                && statIndex == Stat.SPEED)
            {
                stat *= 2;
            }
            // SWIFT SWIM doubles speed in rain weather
            if (pokemon.ability.abilityId == 33 
                && this.turnEffectManager.weather == Weather.RAIN
                && statIndex == Stat.SPEED)
            {
                stat *= 2;
            }

            // SAND VEIL increases evasion in a sandstorm
            if (pokemon.ability.abilityId == 8
                && this.turnEffectManager.weather == Weather.SANDSTORM
                && statIndex == Stat.EVASION
                && modifier < 6)
                {
                    modifier += 0.5;
                }
        }

        if (modifier < 0)
        {
            stat = (int)(stat * (2.0 / (Math.abs(modifier) + 2)));
        }
        else if (modifier > 0)
        {
            stat = (int)(stat * ((Math.abs(modifier) + 2) / 2.0));
        }

        return stat;
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
        int playerSpeed = this.getStat(playerPokemon, 0, Stat.SPEED);
        int enemySpeed = this.getStat(enemyPokemon, 1, Stat.SPEED);
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
     * @param target the team targetted by the stat change
     * @param moveStatEffects the stat changes to be applied
     * @param targetPokemon the Pokemon the stat changes are applied to
     */
    public List<BattleEvent> addStatChanges(int attacker, int target, StatEffect[] moveStatEffects, PokemonModel targetPokemon)
    {
        return this.addStatChanges(attacker, target, moveStatEffects, targetPokemon, -1);
    }

    /** 
     * creates event for the stat changes applied by a move used
     * @param attacker the attacking team
     * @param target the team targetted by the stat change
     * @param moveStatEffects the stat changes to be applied
     * @param targetPokemon the Pokemon the stat changes are applied to
     * @param abilityId the ability applying this stat change, or -1 if it's applied by a mvoe
     */
    public List<BattleEvent> addStatChanges(int attacker, int target, StatEffect[] moveStatEffects, PokemonModel targetPokemon, int abilityId)
    {
        List<BattleEvent> battleEvents = new ArrayList<BattleEvent>();

        String[] statChangeMessages = {" fell sharply.", " fell.", "", " rose.", " rose sharply."};
        String[] changedStat = {"", " attack", " defense", " special attack", " special defense", " speed", " accuracy", " evasiveness"};

        //loop through all stat changes in case there are multiple
        for (int i = 0; i < moveStatEffects.length; i++)
        {
            int statId = moveStatEffects[i].statId;
            int statChange = moveStatEffects[i].statChange;

            //check if mist will prevent stat change
            if (this.turnEffectManager.multiTurnEffects.size() > 0)
            {
                for (MultiTurnEffect multiTurnEffect : this.turnEffectManager.multiTurnEffects)
                {
                    if (multiTurnEffect.effectId == 47 && multiTurnEffect.attacker == target)
                    {
                        battleEvents.add(new BattleEvent("The mist prevented stat changes.", target, target));
                        // if all stat changes are prevented it can return immediately
                        return battleEvents;
                    }
                }
            }
            // KEEN EYE prevents accuracy reductions
            if (targetPokemon.ability != null 
                && targetPokemon.ability.abilityId == 51 
                && statId == Stat.ACCURACY
                && statChange < 0)
            {
                battleEvents.add(new BattleEvent(targetPokemon.name + "'s KEEN EYE prevents accuracy reductions.", target, target));
            }
            // HYPER CUTTER prevents attack reductions
            else if (targetPokemon.ability != null 
                && targetPokemon.ability.abilityId == 52 
                && statId == Stat.ATTACK
                && statChange < 0)
            {
                battleEvents.add(new BattleEvent(targetPokemon.name + "'s HYPER CUTTER prevents attack reductions.", target, target));
            }
            // CLEAR BODY prevents stat reduces from the foe (but not the user)
            else if (targetPokemon.ability != null 
                && targetPokemon.ability.abilityId == 29 
                && statChange < 0
                && target != attacker)
            {
                battleEvents.add(new BattleEvent(targetPokemon.name + "'s CLEAR BODY prevents stat reductions.", target, target));
            }
            // WHITE SMOKE prevents stat reduces from the foe (but not the user)
            else if (targetPokemon.ability != null 
                && targetPokemon.ability.abilityId == 73 
                && statChange < 0
                && target != attacker)
            {
                battleEvents.add(new BattleEvent(targetPokemon.name + "'s WHITE SMOKE prevents stat reductions.", target, target));
            }
        
            //check if stat cannot be changed any further
            else if ((this.statChanges[target][statId] == 6 && statChange > 0) || (this.statChanges[target][statId] == -6 && statChange < 0))
            {
                battleEvents.add(new BattleEvent(targetPokemon.name + "'s " + changedStat[statId] + " cannot be " +
                    (this.statChanges[target][statId] < 0 ? "decreased" : "increased") + " any further.", target, target));
            }

            else
            {
                //apply stat change with limits of |6|
                this.statChanges[target][statId] = (statChange / Math.abs(statChange)) * Math.min(6, Math.abs(statChange + this.statChanges[target][statId]));

                if (abilityId == 80)
                {
                    battleEvents.add(new BattleEvent(
                        targetPokemon.name + "'s STEADFAST raised it's speed.",
                        target, target));
                }
                else if (abilityId == 22)
                {
                    battleEvents.add(new BattleEvent("INTIMIDATE cuts " + targetPokemon.name + "'s " +
                        changedStat[statId] + statChangeMessages[statChange + 2], target, target));
                }
                else
                {
                    battleEvents.add(new BattleEvent(targetPokemon.name + "'s " +
                        changedStat[statId] + statChangeMessages[statChange + 2], target, target));
                }
            }
        }

        return battleEvents;
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

        // Pokemon with the ROCK HEAD ability do not take recoil damage
        if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 69)
        {
            return null;
        }
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
            damage = (int)(Math.ceil(Math.min(attackDamage, defendingPokemon.currentHP) * (move.recoil / 100.0)));
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

        // same type attack bonus
        if (move.typeId == attackingPokemon.types[0]
            || move.typeId == attackingPokemon.types[attackingPokemon.types.length - 1]) {
            otherModifiers *= 1.5f;
        }

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


        if (move.damageClassId == 2)
        {
            // GUTS ability increases physical damage under status conditions
            if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 62
                && (attackingPokemon.statusEffect == StatusEffect.PARALYSIS || 
                attackingPokemon.statusEffect == StatusEffect.BURN || 
                attackingPokemon.statusEffect == StatusEffect.POISON ||
                attackingPokemon.statusEffect == StatusEffect.BADLY_POISON))
            {
                otherModifiers *= 1.5f;
            }
            // Pokemon without guts ability have physical damage 
            else if (attackingPokemon.statusEffect == StatusEffect.BURN)
            {
                otherModifiers *= 0.5f;
            }
        }

        // Technician ability boosts damage of weak moves
        if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 101 && move.power <= 60) {
            otherModifiers *= 1.5f;
        }

        // Overgrow abliity boosts Grass damage when health falls below 1/3
        else if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 65
                && attackingPokemon.currentHP / attackingPokemon.stats[Stat.HP] <= 0.33
                && move.typeId == Type.GRASS) {
            otherModifiers *= 1.5f;
        }

        // Blaze abliity boosts Fire damage when health falls below 1/3
        else if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 66
                && attackingPokemon.currentHP / attackingPokemon.stats[Stat.HP] <= 0.33
                && move.typeId == Type.FIRE) {
            otherModifiers *= 1.5f;
        }

        // Torrent abliity boosts Water damage when health falls below 1/3
        else if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 67
                && attackingPokemon.currentHP / attackingPokemon.stats[Stat.HP] <= 0.33
                && move.typeId == Type.WATER) {
            otherModifiers *= 1.5f;
        }

        // Swarm abliity boosts Bug damage when health falls below 1/3
        else if (attackingPokemon.ability != null && attackingPokemon.ability.abilityId == 68
                && attackingPokemon.currentHP / attackingPokemon.stats[Stat.HP] <= 0.33
                && move.typeId == Type.BUG) {
            otherModifiers *= 1.5f;
        }

        // THICK FAT ability reduces Ice and Fire type damage
        if (defendingPokemon.ability != null && defendingPokemon.ability.abilityId == 47
            && (move.typeId == Type.ICE || move.typeId == Type.FIRE)) {
            otherModifiers *= 0.5f;
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

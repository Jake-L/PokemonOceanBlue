package pokemonoceanblue.battle;

import java.util.ArrayList;
import java.util.List;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Stat;
import pokemonoceanblue.StatusEffect;
import pokemonoceanblue.Type;

public class Attack {
    public List<String> eventText = new ArrayList<String>();
    public String moveUsedText;
    public MoveModel move;
    public int attacker;
    public boolean isUsed = false;
    public boolean attackMissed = false;
    public int damage = 0;
    public String sound = null;
    private String pokemonName;

    public Attack(String pokemonName, MoveModel move, int attacker)
    {
        this.moveUsedText = pokemonName + " used " + move.name + ".";
        this.pokemonName = pokemonName;
        this.move = move;
        this.attacker = attacker;
    }

    public List<BattleEvent> getAttackMessages()
    {
        List<BattleEvent> attackMessages = new ArrayList<BattleEvent>();
        for (int i = 0; i < eventText.size(); i++)
        {
            attackMessages.add(new BattleEvent(this.eventText.get(i), this.attacker, this.attacker));
        }
        return attackMessages;
    }

    public void useAttack(PokemonModel attackingPokemon, PokemonModel defendingPokemon, BattleOperationsManager battleOperationsManager,
                         TurnEffectManager turnEffectManager, int weather)
    {
        this.damage = this.damageCalc(attackingPokemon, defendingPokemon, battleOperationsManager, turnEffectManager, weather);
        isUsed = true;
    }

    private int damageCalc(PokemonModel attackingPokemon, PokemonModel defendingPokemon, BattleOperationsManager battleOperationsManager,
                           TurnEffectManager turnEffectManager, int weather)
    {
        int defender = (attacker + 1) % 2;
        int attack_stat;
        int defense_stat;
        int movePower = this.move.power;
        float typeModifier = 1.0f;
        float otherModifiers = 1.0f;
        // chance to crit is equal to critChance / 10
        int critChance = 1;

        if (!battleOperationsManager.isHit(this.attacker, this.move.accuracy) || (defendingPokemon.currentHP == 0 && move.targetId != 7))
        {
            this.eventText.add(attackingPokemon.name + "'s attack missed!");
            this.attackMissed = true;
            return 0;
        }
        else if (movePower == -1)
        {
            this.eventText.add("It's a one hit KO!");
            return defendingPokemon.currentHP;
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
        else
        {
            return 0;
        }
        for (int i = 0; i < defendingPokemon.types.length; i++)
        {
            typeModifier *= Type.typeEffectiveness[move.typeId][defendingPokemon.types[i]];
        }
        //check for move effect that determines moves damage
        if (move.moveEffect != null)
        {
            int effectId = move.moveEffect.effectId;
            if (typeModifier > 0)
            {
                if (effectId == 42)
                {
                    return movePower;
                }
                if (effectId == 88)
                {
                    return attackingPokemon.level;
                }
                if (effectId == 44)
                {
                    critChance ++;
                }
                else if (effectId == 122)
                {
                    movePower = (int)Math.ceil(attackingPokemon.happiness / 2.0);
                }
                else if (effectId == 124)
                {
                    movePower = (int)Math.ceil((200 - attackingPokemon.happiness) / 2.0);
                }
                else if (effectId == 170 && (attackingPokemon.statusEffect == StatusEffect.PARALYSIS || 
                                                attackingPokemon.statusEffect == StatusEffect.BURN || 
                                                attackingPokemon.statusEffect == StatusEffect.POISON ||
                                                attackingPokemon.statusEffect == StatusEffect.BADLY_POISON))
                {
                    otherModifiers *= 2.0f;
                }
                else if (effectId == 222 && defendingPokemon.currentHP < Math.ceil(defendingPokemon.stats[Stat.HP] / 2.0))
                {
                    otherModifiers *= 2.0f;
                }
                else if (effectId == 284 && defendingPokemon.statusEffect == StatusEffect.POISON)
                {
                    otherModifiers *= 2.0f;
                }
                else if (effectId == 238)
                {
                    movePower = 1 + 120 * defendingPokemon.currentHP / defendingPokemon.stats[Stat.HP];
                }
                else if (effectId == 294)
                {
                    int speedRatio = Math.min(attackingPokemon.stats[Stat.SPEED] / defendingPokemon.stats[Stat.SPEED], 4);
                    movePower = (speedRatio < 2 ? 60 : speedRatio * 40 - (speedRatio / 4) * 10);
                }
                else if (effectId == 220)
                {
                    movePower = (int)Math.min(1 + 25.0 * defendingPokemon.stats[Stat.SPEED] / attackingPokemon.stats[Stat.SPEED], 150);
                }
                else if ((effectId == 9 && defendingPokemon.statusEffect != StatusEffect.SLEEP) || 
                            (effectId == 93 && attackingPokemon.statusEffect != StatusEffect.SLEEP))
                {
                    typeModifier = 1.0f;
                    return 0;
                }
            }
            if (move.moveEffect.effectId == 41)
            {
                return (int)Math.ceil(defendingPokemon.currentHP / 2.0);
            }
        }
        // check for abilities that grant immunity to certain types
        if (move.typeId == Type.GROUND 
            && defendingPokemon.ability != null
            && defendingPokemon.ability.abilityId == 26)
        {
            typeModifier = 0.0f;
        }
        //move doesn't affect opponent
        if (typeModifier == 0)
        {
            return 0;
        }
        //focus-energy and lucky-chant
        if (turnEffectManager.multiTurnEffects.size() > 0)
        {
            for (int i = 0; i < turnEffectManager.multiTurnEffects.size(); i++)
            {
                if (turnEffectManager.multiTurnEffects.get(i).effectId == 241 && turnEffectManager.multiTurnEffects.get(i).target == attacker)
                {
                    critChance = 0;
                    break;
                }
                else if (turnEffectManager.multiTurnEffects.get(i).effectId == 48 && turnEffectManager.multiTurnEffects.get(i).attacker == attacker)
                {
                    critChance += 2;
                }
            }
        }
        //same type attack bonus
        if (move.typeId == attackingPokemon.types[0] || move.typeId == attackingPokemon.types[attackingPokemon.types.length - 1])
        {
            otherModifiers *= 1.5f;
        }
        //critical hit bonus
        if (movePower > 0 && battleOperationsManager.isCrit(critChance))
        {
            otherModifiers *= 1.5f;
            this.eventText.add("A critical hit!");
        }
        if ((weather == 2 || weather == 1) && (move.typeId == 10 || move.typeId == 11))
        {
            otherModifiers *= (weather == 2 && move.typeId == 10) || (weather == 1 && move.typeId == 11) ? 0.5 : 1.5;
        }
        //check for multiTurnEffects that apply a damage multiplier
        if (turnEffectManager.multiTurnEffects.size() > 0)
        {
            for (int i = 0; i < turnEffectManager.multiTurnEffects.size(); i++)
            {
                if (!this.eventText.contains("A critical hit!") && turnEffectManager.multiTurnEffects.get(i).attacker == defender &&
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
        this.determineEffectiveness(typeModifier);
        return (int)Math.ceil((
                    (attackingPokemon.level * 2.0 / 5.0 + 2.0)
                    * (movePower) 
                    * (attackingPokemon.getStat(attack_stat, battleOperationsManager.statChanges[attacker][attack_stat]) * 1.0
                    / defendingPokemon.getStat(defense_stat, battleOperationsManager.statChanges[defender][defense_stat])) / 50 + 2) 
                * typeModifier * otherModifiers);
    }
    
    private void determineEffectiveness(float typeModifier)
    {
        if (typeModifier > 1)
        {
            this.sound = "superdamage";
            this.eventText.add("It's super effective!");
        }
        else if (typeModifier == 1)
        {
            this.sound = "damage";
        }
        else if (typeModifier > 0)
        {
            this.sound = "weakdamage";
            this.eventText.add("It's not very effective...");
        }
        else
        {
            this.eventText.add("It doesn't affect " + pokemonName + "...");
        }
    }
}
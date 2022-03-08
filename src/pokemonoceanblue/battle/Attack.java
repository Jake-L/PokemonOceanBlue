package pokemonoceanblue.battle;

import java.util.ArrayList;
import java.util.List;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Stat;
import pokemonoceanblue.StatusEffect;

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
                         TurnEffectManager turnEffectManager)
    {
        this.damage = this.damageCalc(attackingPokemon, defendingPokemon, battleOperationsManager, turnEffectManager);
        isUsed = true;
    }

    private int damageCalc(PokemonModel attackingPokemon, PokemonModel defendingPokemon, BattleOperationsManager battleOperationsManager,
                           TurnEffectManager turnEffectManager)
    {
        int defender = (attacker + 1) % 2;
        int attack_stat;
        int defense_stat;
        int movePower = this.move.power;

        // chance to crit is equal to critChance / 10
        int critChance = battleOperationsManager.getCritChance(move, this.attacker);
        boolean isCrit = battleOperationsManager.isCrit(critChance);

        // get damage modifiers related to type effectiveness and other modifiers
        float typeModifier = BattleOperationsManager.getTypeModifier(attackingPokemon, defendingPokemon, move);
        float otherModifiers = battleOperationsManager.getDamageMultiplier(attackingPokemon, defendingPokemon, move, isCrit, defender);

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

        //check for move effect that determines moves damage
        if (move.moveEffect != null)
        {
            int effectId = move.moveEffect.effectId;

            if (effectId == 42)
            {
                // attacks that deal constant damage
                return movePower;
            }
            if (effectId == 88)
            {
                // attacks that deal damage equal to user's level
                return attackingPokemon.level;
            }
            else if (effectId == 122)
            {
                movePower = (int)Math.ceil(attackingPokemon.happiness / 2.0);
            }
            else if (effectId == 124)
            {
                movePower = (int)Math.ceil((200 - attackingPokemon.happiness) / 2.0);
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
            else if ((effectId == 9 && defendingPokemon.statusEffect != StatusEffect.SLEEP) 
                || (effectId == 93 && attackingPokemon.statusEffect != StatusEffect.SLEEP))
            {
                // moves that can only be used on sleeping Pokemon
                return 0;
            }
            else if (effectId == 41)
            {
                // super fang inflicts damage equal to half the enemy's current HP
                return (int)Math.ceil(defendingPokemon.currentHP / 2.0);
            }
        }

        //same type attack bonus
        if (move.typeId == attackingPokemon.types[0] || move.typeId == attackingPokemon.types[attackingPokemon.types.length - 1])
        {
            otherModifiers *= 1.5f;
        }
        //critical hit bonus
        if (movePower > 0 && isCrit)
        {
            otherModifiers *= 1.5f;
            this.eventText.add("A critical hit!");
        }

        this.getEffectivenessMessage(typeModifier);

        return (int)Math.ceil((
                    (attackingPokemon.level * 2.0 / 5.0 + 2.0)
                    * (movePower) 
                    * (attackingPokemon.getStat(attack_stat, battleOperationsManager.statChanges[attacker][attack_stat]) * 1.0
                    / defendingPokemon.getStat(defense_stat, battleOperationsManager.statChanges[defender][defense_stat])) / 50 + 2) 
                * typeModifier * otherModifiers);
    }
    
    private void getEffectivenessMessage(float typeModifier)
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
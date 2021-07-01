package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TurnEffectManager
{
    private Random ranNum = new Random();
    public List<MultiTurnEffect> multiTurnEffects = new ArrayList<MultiTurnEffect>();

    /** 
     * creates event for the status effect inflicted by current move
     * @param attacker the attacking team
     * @param defender the defending team
     * @param effectChance the chance that the move that inflicts the status effect
     * @param ailmentId the status effect to be inflicted
     */
    public void statusEffect(int attacker, int defender, int effectChance, int ailmentId, PokemonModel defendingPokemon, List<BattleEvent> events)
    {
        if (this.ranNum.nextInt(100) + 1 <= effectChance && defendingPokemon.statusEffect == StatusEffect.UNAFFLICTED)
        {
            boolean willFail = false;
            String[] statusEffectMessages = {" was paralyzed."," fell asleep."," was frozen solid."," was burned."," was poisoned."," was badly poisoned."," was cursed."," became confused."};
            if (this.multiTurnEffects.size() > 0)
            {
                for (int i = 0; i < this.multiTurnEffects.size(); i++)
                {
                    if (this.multiTurnEffects.get(i).effectId == 125 && this.multiTurnEffects.get(i).attacker == defender && attacker != defender)
                    {
                        willFail = true;
                        events.add(new BattleEvent(defendingPokemon.name + " was protected by safeguard.", defender, defender));
                        break;
                    }
                }
            }
            
            if (defendingPokemon.ability != null
                // check if Pokemon cannot sleep due to insomnia
                && ((ailmentId == StatusEffect.SLEEP && defendingPokemon.ability.name.equals("INSOMNIA"))
                    // check if a Pokemon cannot sleep due to vital spirit
                    || (ailmentId == StatusEffect.SLEEP && defendingPokemon.ability.name.equals("VITAL SPIRIT"))
                    // check if Pokemon cannot be poisoned due to immunity
                    || (ailmentId == StatusEffect.POISON && defendingPokemon.ability.name.equals("IMMUNITY"))
                    // check if Pokemon cannot be paralyzed due to limber
                    || (ailmentId == StatusEffect.PARALYSIS && defendingPokemon.ability.name.equals("LIMBER"))
                    // check if Pokemon cannot be confused due to own tempo
                    || (ailmentId == StatusEffect.CONFUSION && defendingPokemon.ability.name.equals("OWN TEMPO"))))
            {
                willFail = true;
                events.add(new BattleEvent(defendingPokemon.ability.battleText.replace("{defender}", defendingPokemon.name), defender, defender));
            }

            // apply type immunity for status effects
            // certain types are immune to certain status effects
            if (((ailmentId == StatusEffect.POISON || ailmentId == StatusEffect.BADLY_POISON) && (Type.typeIncludes(Type.STEEL, defendingPokemon.types) || Type.typeIncludes(Type.POISON, defendingPokemon.types))) 
                // FIRE types cannot be burned
                || (ailmentId == StatusEffect.BURN && Type.typeIncludes(Type.FIRE, defendingPokemon.types))
                // ICE types cannot be frozen
                || (ailmentId == StatusEffect.FROZEN && Type.typeIncludes(Type.ICE, defendingPokemon.types)))
            {
                // no message displayed when a pokemon is immune to the status effect
                willFail = true;
            }

            if (ailmentId < 9 && !willFail)
            {
                // create the event to inflict the status effect
                BattleEvent event = new BattleEvent(
                    defendingPokemon.name + statusEffectMessages[ailmentId - 1], 
                    attacker, 
                    defender
                );
                event.setStatusEffect(ailmentId, defender);
                events.add(event);
            }
        }
    }

    /*
     * checks if a multi turn effect should be added, and if so, adds a multi turn effect
     */
    public void addMultiTurnEffect(MoveModel move, int effectId, int attacker, PokemonModel[][] team, int currentPokemon[], List<BattleEvent> events)
    {
        boolean willFail = false;
        for (int i = 0; i < this.multiTurnEffects.size(); i++)
        {
            if (this.multiTurnEffects.get(i).effectId == effectId && this.multiTurnEffects.get(i).attacker == attacker)
            {
                willFail = true;
            }
        }
        BattleEvent event;
        if (willFail)
        {
            event = new BattleEvent("But it failed.", attacker, attacker);
        }
        else
        {
            MultiTurnEffect effect = new MultiTurnEffect(move, move.moveEffect, attacker, team, currentPokemon);
            PokemonModel attackingPokemon = team[attacker][currentPokemon[attacker]];
            PokemonModel defendingPokemon = team[(attacker + 1) % 2][currentPokemon[(attacker + 1) % 2]];
            if (effectId == 43)
            {
                event = new BattleEvent(defendingPokemon.name + " is trapped in a " + move.name + "!", attacker, (attacker + 1) % 2);
            }
            else if (effectId == 85 && !Type.typeIncludes(Type.GRASS, defendingPokemon.types))
            {
                event = new BattleEvent(defendingPokemon.name + " is seeded.", attacker, (attacker + 1) % 2);
            }
            else if (effectId == 36)
            {
                event = new BattleEvent("Light screen made " + attackingPokemon.name + "'s team stronger against special moves.",
                    attacker, attacker);
            }
            else if (effectId == 66)
            {
                event = new BattleEvent("Reflect made " + attackingPokemon.name + "'s team stronger against physical moves.",
                    attacker, attacker);
            }
            else if (effectId == 125)
            {
                event = new BattleEvent(attackingPokemon.name + "'s team is protected from status conditions.", attacker, attacker);
            }
            else if (effectId == 47)
            {
                event = new BattleEvent(attackingPokemon.name + "'s team are immune to stat changes.", attacker, attacker);
            }
            else if (effectId == 202)
            {
                event = new BattleEvent("Electricity's power was weakened.", attacker, attacker);
            }
            else if (effectId == 211)
            {
                event = new BattleEvent("Fire's power was weakened.", attacker, attacker);
            }
            else if (effectId == 48)
            {
                event = new BattleEvent(attackingPokemon.name + " is getting pumped up.", attacker, attacker);
            }
            else if (effectId == 241)
            {
                event = new BattleEvent(attackingPokemon.name + " protected its team from critical hits.", attacker, attacker);
            }
            else if (effectId == 252)
            {
                event = new BattleEvent(attackingPokemon.name + " surrounded itself with a veil of water.", attacker, attacker);
            }
            else
            {
                event = new BattleEvent("But it failed.", attacker, attacker);
            }
            this.multiTurnEffects.add(effect);
        }
        events.add(event);
    }

    /** 
     * checks if either pokemon should suffer an end of turn effect
     */
    public void endOfTurnEffects(PokemonModel team[][], int currentPokemon[], List<BattleEvent> events, int weather)
    {
        String[] effectMessages = {" is hurt by burn."," is hurt by poison."," is badly hurt by poison."," is hurt by the curse.","sandstorm.","hail."};
        //check for status effect end of turn effects
        for (int i = 0; i < 2; i++)
        {
            boolean isFainted = false;
            for (int j = 0; j < events.size(); j++)
            {
                // we dont apply end of turn effects to fainted pokemon
                if (events.get(j).newPokemonIndex == -1 && team[i][currentPokemon[i]].currentHP == 0)
                {
                    isFainted = true;
                }
            }
            if (isFainted)
            {
                continue;
            }
            byte statusEffect = team[i][currentPokemon[i]].statusEffect;
            if (statusEffect > StatusEffect.FROZEN && statusEffect < StatusEffect.CONFUSION)
            {
                BattleEvent event = new BattleEvent(team[i][currentPokemon[i]].name + effectMessages[team[i][currentPokemon[i]].statusEffect - 4],
                    (int)Math.ceil(team[i][currentPokemon[i]].stats[Stat.HP] / 8.0), i, i, null, i);

                //badly poisoned
                if (team[i][currentPokemon[i]].statusEffect == StatusEffect.BADLY_POISON)
                {
                    // determine damage multiplier first
                    int multiplier = 1;
                    for (int j = 0; j < this.multiTurnEffects.size(); j++)
                    {
                        if (this.multiTurnEffects.get(j).effectId == StatusEffect.BADLY_POISON)
                        {
                            multiplier += this.multiTurnEffects.get(j).counter;
                            break;
                        }
                    }
                    event.damage = (int)Math.ceil((team[i][currentPokemon[i]].stats[Stat.HP] * multiplier) / 8.0);
                }
                //curse
                else if (team[i][currentPokemon[i]].statusEffect == StatusEffect.CURSE)
                {
                    event.damage = (int)Math.ceil(team[i][currentPokemon[i]].stats[Stat.HP] / 4.0);
                }
                events.add(event);
            }
        }
        //check for multiTurnEffects by looping through list
        if (this.multiTurnEffects.size() > 0)
        {
            int i = 0;
            while (i < this.multiTurnEffects.size())
            {
                MultiTurnEffect effect = this.multiTurnEffects.get(i);
                // add damage/recoil event if applicable
                if ((effect.counter < effect.duration && effect.effectTimingId == 0) || effect.counter == effect.duration || effect.duration == -1)
                {
                    if (effect.damage != 0)
                    {
                        events.add(new BattleEvent(effect.text, effect.damage, effect.target, effect.attacker, null, effect.target));
                    }
                    if (effect.recoil != 0 && team[effect.attacker][currentPokemon[effect.attacker]].currentHP < team[effect.attacker][currentPokemon[effect.attacker]].stats[Stat.HP])
                    {
                        events.add(new BattleEvent(effect.text, effect.recoil, effect.attacker, effect.attacker, null, effect.target));
                    }
                }
                //increment effect counter
                this.multiTurnEffects.get(i).counter++;
                //remove effects that have finished, don't remove status effects
                if (effect.counter > effect.duration && effect.duration > -1 && effect.effectId > StatusEffect.CONFUSION)
                {
                    if (effect.effectId == 36 || effect.effectId == 66 || effect.effectId == 125 || effect.effectId == 47 || effect.effectId == 241)
                    {
                        events.add(new BattleEvent(effect.text, effect.attacker, -1));
                    }
                    this.multiTurnEffects.remove(i);
                }
                else
                {
                    i++;
                }
            }
        }
        //check for end of turn damage from hail/sandstorm
        if (weather > 2)
        {
            for (int i = 0; i < 2; i++)
            {
                PokemonModel pokemon = team[i][currentPokemon[i]];
                boolean isImmune = false;

                if (pokemon.currentHP == 0 
                    || (weather == 3 && (Type.typeIncludes(Type.STEEL, pokemon.types) || Type.typeIncludes(Type.ROCK, pokemon.types) || Type.typeIncludes(Type.GROUND, pokemon.types))) 
                    || (weather == 4 && Type.typeIncludes(Type.ICE, pokemon.types)))
                {
                    isImmune = true;
                }

                if (!isImmune)
                {
                    events.add(new BattleEvent(pokemon.name + " is hurt by the " + effectMessages[weather + 1], (int)Math.ceil(pokemon.stats[Stat.HP] / 16.0),
                        i, i, null, i));
                }
            }    
        }
    }

    class MultiTurnEffect
    {
        String text;
        int effectId;
        int attacker;
        int target;
        int duration;
        int damage = 0;
        int recoil = 0;
        int counter = 0;
        // -1 if never end, 0 if end when player pokemon leaves field, 1 if end enemy pokemon leaves field, 2 if end when either leaves field
        int removalCondition;
        // if 0 then effect occurs throughout duration, if 1 effect occurs at end of duration
        int effectTimingId;

        public MultiTurnEffect(MoveModel move, MoveEffectModel moveEffect, int attacker, PokemonModel team[][], int[] currentPokemon)
        {
            this.effectId = moveEffect.effectId;
            this.attacker = attacker;
            this.target = (moveEffect.targetId + attacker) % 2;
            this.effectTimingId = moveEffect.effectTimingId;
            if (moveEffect.removalCondition == -1 || moveEffect.removalCondition == 2)
            {
                this.removalCondition = moveEffect.removalCondition;
            }
            else
            {
                this.removalCondition = (moveEffect.removalCondition + attacker) % 2;
            }
            if (moveEffect.minCounter < 0)
            {
                this.duration = -1;
            }
            else
            {
                this.duration = ranNum.nextInt(moveEffect.maxCounter - moveEffect.minCounter + 1) + moveEffect.minCounter;
            }
            if (moveEffect.effectId == 43)
            {
                this.text = team[target][currentPokemon[target]].name + " is damaged by " + move.name + ".";
                this.damage = (int)Math.ceil(team[target][currentPokemon[target]].stats[Stat.HP] / 8.0);
            }
            else if (moveEffect.effectId == 85)
            {
                this.text = team[target][currentPokemon[target]].name + "'s health was sapped.";
                this.damage = (int)Math.ceil(team[target][currentPokemon[target]].stats[Stat.HP] / 8.0);
                this.recoil = this.damage * -1;
            }
            else if (moveEffect.effectId == 36 || moveEffect.effectId == 47 || moveEffect.effectId == 66 || moveEffect.effectId == 125 || moveEffect.effectId == 241)
            {
                this.text = move.name + " wore off.";
            }
            else if (moveEffect.effectId == 252)
            {
                this.text = team[target][currentPokemon[target]].name + " is healed by " + move.name + ".";
                this.recoil = (int)Math.floor(team[target][currentPokemon[target]].stats[Stat.HP] / -16.0);
            }
        }
    }
}
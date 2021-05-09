package pokemonoceanblue;

public class BattleEvent
{
    public String text;
    public int damage = 0;
    public int target = -1;
    public int newPokemonIndex = -2;
    public int itemId = -1;
    public int attacker;
    public int xp = 0;
    public int statusEffect = -1;
    public MoveModel move;
    public int removalCondition;
    public MoveModel newMove;
    public int newWeatherId = -1;
    public boolean pokeballShake = false;

    /** 
     * Base Constructor
     * @param text the text that will be displayed
     * @param attacker the team performing the event
     * @param removalCondition determines whether the event should be removed when a pokemon faints
     */
    public BattleEvent(String text, int attacker, int removalCondition)
    {
        this.text = text;
        this.attacker = attacker;
        this.removalCondition = removalCondition;
    }

    /** 
     * Constructor for moves used
     * @param damage the damage that will be taken by target
     * @param target the pokemon that will recieve the damage
     * @param move the move that inflicts the damage (null if damage is not applied by a move)
     */
    public BattleEvent(String text, int damage, int target, int attacker, MoveModel move, int removalCondition)
    {
        this(text, attacker, removalCondition);
        this.damage = damage;
        this.target = target;
        this.move = move;
    }

    /** 
     * Setter for status effects inflicted
     * @param statusEffect the status effect that will be applied
     * @param target the pokemon that will recieve the status effect
     */
    public void setStatusEffect(int statusEffect, int target)
    {
        this.statusEffect = statusEffect;
        this.target = target;
    }

    /** 
     * Setter for xp gained by the players pokemon
     * @param xp the amount of xp earned
     */
    public void setXP(int xp)
    {
        this.xp = xp;
    }

    /** 
     * Setter for using an item
     * @param index is the index of the item used
     */
    public void setItem(int index)
    {
        this.itemId = index;
    }

    /** 
     * Setter for changing Pokemon
     * @param index is the index of the new Pokemon being sent out
     */
    public void setNewPokemon(int index)
    {
        this.newPokemonIndex = index;
    }

    /** 
     * Setter for learning new Moves
     * @param newMove the move the pokemon wants to learn
     */
    public void setNewMove(MoveModel newMove)
    {
        this.newMove = newMove;
    }

    /**
     * Setter for changing the weather
     * @param newWeatherId
     */
    public void setWeather(int newWeatherId)
    {
        this.newWeatherId = newWeatherId;
    }

    /**
     * Setter for enabling the Pokeball shaking animation
     */
    public void setPokeballShake()
    {
        this.pokeballShake = true;
    }
}

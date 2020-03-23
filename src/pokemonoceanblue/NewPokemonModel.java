package pokemonoceanblue;

/** 
 * A class used for holding information used when a new Pokemon is caught, evolves, or hatches from an egg
 */
public class NewPokemonModel extends BaseModel {
    public PokemonModel[] pokemon;
    private PartyModel partyModel;
    private PokemonStorageModel storageModel;
    public String text;
    private int partyIndex = -1;
    
    /** 
     * Constructor for when a Pokemon is caught
     * @param pokemon the Pokemon that was caught
     * @param partyModel the player's team
     */
    public NewPokemonModel(PokemonModel pokemon, PartyModel partyModel, PokemonStorageModel storageModel)
    {
        super();
        this.pokemon = new PokemonModel[1];
        this.pokemon[0] = pokemon;
        this.partyModel = partyModel;
        this.storageModel = storageModel;
        this.actionCounter = 60;
        // update right away to make sure there is text to render
        this.update();
    }

    /**
     * Constructor for when a Pokemon evolves
     * @param pokemon the Pokemon that is evolving
     * @param partyModel the player's team
     * @param evolvedPokemonId the Pokemon Id the pokemon evolves into
     * @param partyIndex the index of the Pokemon in the player's team
     */
    public NewPokemonModel(PokemonModel pokemon, PartyModel partyModel, int evolvedPokemonId, int partyIndex)
    {
        super();
        this.pokemon = new PokemonModel[2];
        this.pokemon[0] = pokemon;
        this.pokemon[1] = new PokemonModel(evolvedPokemonId, 1, false);
        this.partyModel = partyModel;
        this.partyIndex = partyIndex;
        this.text = this.pokemon[0].name + " evolved into " + this.pokemon[1].name;
        this.actionCounter = 120;
    }

    /**
     * Constructor for when an egg hatches
     * @param pokemon the Pokemon that is hatching
     */
    public NewPokemonModel(PokemonModel pokemon)
    {
        super();
        this.pokemon = new PokemonModel[1];
        this.pokemon[0] = pokemon;
        this.text = this.pokemon[0].name + " hatched from the egg!";
        this.actionCounter = 120;
    }

    public void addPokemon()
    {
        if (this.partyModel.team.size() == 6)
        {
            this.storageModel.addPokemon(pokemon[0]);
            this.text = pokemon[0].name + " was transferred to the PC.";
        }
        else
        {
            this.partyModel.team.add(pokemon[0]);
            this.text = pokemon[0].name + " joined your team!";
        }
    }

    public void evolvePokemon()
    {
        this.partyModel.team.get(this.partyIndex).evolve(this.pokemon[1].pokemon_id);
    }

    @Override
    public void update()
    {
        // update the player's team to reflect the change
        if (this.actionCounter == 60)
        {
            if (this.pokemon.length > 1)
            {
                this.evolvePokemon();
            }
            else if (this.pokemon[0].level == 0)
            {
                this.pokemon[0].addXP(1);
            }
            else
            {
                this.addPokemon();
            }
        }

        if (this.actionCounter > 0)
        {
            this.actionCounter--;
        }
    }
}
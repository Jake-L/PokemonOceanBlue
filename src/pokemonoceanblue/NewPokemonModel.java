package pokemonoceanblue;

/** 
 * A class used for holding information used when a new Pokemon is caught, evolves, or hatches from an egg
 */
public class NewPokemonModel {
    public int counter = 60;
    public PokemonModel pokemon;
    public boolean complete = false;
    private final PartyModel partyModel;
    public String text;
    
    /** 
     * Constructor
     * 
     */
    public NewPokemonModel(PokemonModel pokemon, PartyModel partyModel)
    {
        this.pokemon = pokemon;
        this.partyModel = partyModel;
        this.addPokemon();
    }

    private void addPokemon()
    {
        if (this.partyModel.team.length == 6)
        {
            this.partyModel.pokemonStorage.add(pokemon);
            this.text = pokemon.name + " was transferred to the PC.";
        }
        else
        {
            PokemonModel[] newTeam = new PokemonModel[this.partyModel.team.length + 1];

            for (int i = 0; i < this.partyModel.team.length; i++)
            {
                newTeam[i] = this.partyModel.team[i];
            }

            newTeam[newTeam.length - 1] = this.pokemon;
            this.partyModel.team = newTeam;
            this.text = pokemon.name + " joined your team!";
        }
    }

    public void confirmSelection()
    {
        if (this.counter == 0)
        {
            this.complete = true;
        }
    }
}
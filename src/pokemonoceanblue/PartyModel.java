package pokemonoceanblue;

public class PartyModel 
{
    public PokemonModel[] team;
    public int optionIndex = 0;
    public int INPUTDELAY = 10;
    public int counter = this.INPUTDELAY;
    private int currentPokemon = -1;

    public PartyModel(PokemonModel[] model)
    {
        this.team = model;
    }

    public PartyModel(PokemonModel[] model, int currentPokemon)
    {
        this.team = model;
        this.currentPokemon = currentPokemon;
    }

    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }
    }

    public void confirmSelection()
    {
        
    }
}
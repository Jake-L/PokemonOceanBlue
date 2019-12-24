package pokemonoceanblue;

public class PartyModel 
{
    public PokemonModel[] team;
    public int optionIndex = 0;
    public int INPUTDELAY = 5;
    public int counter = this.INPUTDELAY;
    private int currentPokemon = -1;
    private boolean isBattle;
    public int returnValue = -2;

    public PartyModel(PokemonModel[] model)
    {
        this.team = model;
        this.isBattle = false;
    }

    public PartyModel(PokemonModel[] model, int currentPokemon)
    {
        this.team = model;
        this.currentPokemon = currentPokemon;
        this.isBattle = true;
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
        this.returnValue = this.optionIndex;
    }

    public int getSelection()
    {
        return this.returnValue;
    }
}
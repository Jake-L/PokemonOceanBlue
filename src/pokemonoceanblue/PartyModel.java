package pokemonoceanblue;

public class PartyModel 
{
    public PokemonModel[] team;
    public int optionIndex = 0;
    public int INPUTDELAY = 6;
    public int counter = this.INPUTDELAY;
    public int currentPokemon = -1;
    public boolean isBattle;
    
    public int returnValue = -2;

    public PartyModel(PokemonModel[] model, int currentPokemon)
    {
        this.team = model;
        this.currentPokemon = currentPokemon;
        
        if (this.currentPokemon == -1)
        {
            this.isBattle = false;
        }

        else
        {
            this.isBattle = true;
        }
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
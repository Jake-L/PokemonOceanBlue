package pokemonoceanblue;

public class PokedexModel extends BaseModel
{
    public int[] caughtPokemon = new int[494];;
    public int uniqueCaught = 0;

    public PokedexModel()
    {
        this.optionMax = this.caughtPokemon.length - 1;
    }

    /**
     * Set variables when creating a new view
     */
    @Override
    public void initialize()
    {
        this.actionCounter = ACTION_DELAY;
        this.optionIndex = 1;
        this.returnValue = -2;
        this.acceleration = 0;
        this.accelerationCounter = 10;
        this.optionMin = 1;
    }

    /**
     * Increment the number of times a Pokemon has been caught
     * @param pokemonId the identifier of the caught pokemon
     * @return true if it's the first time the Pokemon was caught
     */
    public boolean setCaught(int pokemonId)
    {
        this.caughtPokemon[pokemonId]++;

        if (this.caughtPokemon[pokemonId] == 1)
        {
            this.uniqueCaught++;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * get the shiny rate for a specific Pokemon
     * @param pokemonId the Pokemon's identifier
     * @return the probability that the Pokemon is shiny, in range (0,1)
     */
    public double getShinyRate(int pokemonId)
    {
        return (Math.log10(this.caughtPokemon[pokemonId] + 1) + 1) / 2000;
    }
}
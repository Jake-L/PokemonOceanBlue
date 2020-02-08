package pokemonoceanblue;

public class PokedexModel 
{
    public int[] caughtPokemon = new int[494];
    public int optionIndex;
    public final int INPUTDELAY = 4;
    public int acceleration;
    public int accelerationCounter;
    public int counter;
    public int pokemonPerRow = 10;
    public int uniqueCaught = 0;

    public PokedexModel()
    {
        this.initialize();
    }

    /**
     * Set variables when creating a new view
     */
    public void initialize()
    {
        this.counter = this.INPUTDELAY;
        this.optionIndex = 1;
        this.acceleration = 0;
        this.accelerationCounter = 0;
    }

    /**
     * Increment the number of times a Pokemon has been caught
     * @param pokemonId the identifier of the caught pokemon
     */
    public void setCaught(int pokemonId)
    {
        this.caughtPokemon[pokemonId]++;

        this.uniqueCaught = 0;

        for (int i = 0; i < this.caughtPokemon.length; i++)
        {
            if (this.caughtPokemon[i] > 0)
            {
                this.uniqueCaught++;
            }
        }
    }

    /**
     * Decrement counter and accelerate scrolling
     */
    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;

            // scroll faster the longer the player holds the button
            if (this.acceleration < 3)
            {
                this.accelerationCounter++;

                if (this.accelerationCounter > 20)
                {
                    this.acceleration++;
                    this.accelerationCounter = 20;
                }
            }
        }
    }

    /**
     * get the shiny rate for a specific Pokemon
     * @param pokemonId the Pokemon's identifier
     * @return the probability that the Pokemon is shiny, in range (0,1)
     */
    public double getShinyRate(int pokemonId)
    {
        return (Math.log10(this.caughtPokemon[pokemonId] + 0.1) + 1) / 10000;
    }

    /**
     * Change the pokemon currently being hovered
     * @param dx x-direction movement
     * @param dy y-direction movement
     */
    public void moveCursor(int dx, int dy)
    {
        if (dx > 0 && this.optionIndex < this.caughtPokemon.length - 1)
        {
            this.optionIndex++;
        }
        else if (dx < 0 && this.optionIndex > 1)
        {
            this.optionIndex--;
        }
        else if (dy > 0 && this.optionIndex + this.pokemonPerRow < this.caughtPokemon.length)
        {
            this.optionIndex = this.optionIndex + this.pokemonPerRow;
        }
        else if (dy < 0 && this.optionIndex - this.pokemonPerRow > 0)
        {
            this.optionIndex = this.optionIndex - this.pokemonPerRow;
        }
    }    
}
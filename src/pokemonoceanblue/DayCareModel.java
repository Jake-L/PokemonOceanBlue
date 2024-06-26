package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DayCareModel 
{
    // up to 2 Pokemon can be stored at the day care
    PokemonModel[] pokemon = new PokemonModel[2];
    List<Integer> eggCounters = new ArrayList<Integer>();
    int stepsToNextEgg = -1;

    public DayCareModel()
    {

    }

    /**
     * Deposit a Pokemon into the day care
     * @param newPokemon the Pokemon to be deposited
     * @return true if there was room to deposit the Pokemon
     */
    public boolean setPokemon(PokemonModel newPokemon)
    {
        for (int i = 0; i < pokemon.length; i++)
        {
            if (pokemon[i] == null)
            {
                pokemon[i] = newPokemon;
                
                // if two Pokemon are deposited, start progress towards hatching an egg
                if (pokemon[0] != null && pokemon[1] != null)
                {
                    stepsToNextEgg = new Random().nextInt(300) + 300;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Withdraws a Pokemon from the day care
     * @param index the index of the Pokemon to remove
     * @return the PokemonModel of the Pokemon being withdrawn
     */
    public PokemonModel withdrawPokemon(int index)
    {
        if (index >= 0 && index <= 1)
        {
            PokemonModel returnPokemon = pokemon[index];
            this.pokemon[index] = null;
            this.stepsToNextEgg = -1;
            return returnPokemon;
        }

        return null;
    }

    /**
     * Progresses towards new eggs being creating and existing eggs hatching
     * @return the Pokemon id of an egg ready to hatch or -1 otherwise
     */
    public int decrementStepCounter()
    {
        if (this.stepsToNextEgg > 0)
        {
            this.stepsToNextEgg--;

            if (this.stepsToNextEgg == 0)
            {
                // create a new egg and start progress towards the next one
                if ((this.pokemon[0].genderId == 0 && this.pokemon[1].genderId == 1)
                    || (this.pokemon[1].genderId == 0 && this.pokemon[0].genderId == 1))
                {
                    this.stepsToNextEgg = new Random().nextInt(300) + 300;
                    this.eggCounters.add(1500);
                }
            }
        }

        for (int i = this.eggCounters.size() - 1; i >= 0; i--)
        {
            // progress towards hatching existing eggs
            this.eggCounters.set(i, this.eggCounters.get(i) - 1);

            // if an egg is ready to hatch, return the new Pokemon's id
            if (this.eggCounters.get(i) <= 0)
            {
                this.eggCounters.remove(i);
                return this.pokemon[0].genderId == 0 
                    ? Utils.getFirstEvolution(this.pokemon[0].base_pokemon_id)
                    : Utils.getFirstEvolution(this.pokemon[1].base_pokemon_id);
            }
        }

        return -1;
    }
}
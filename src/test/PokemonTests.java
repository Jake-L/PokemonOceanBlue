package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import pokemonoceanblue.PokedexModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.DayCareModel;

public class PokemonTests {
    @Test
    public void testUpdateHappiness() {
        PokemonModel pokemon = new PokemonModel(1, 1, false);
        assertEquals(70, pokemon.happiness);

        // test happiness increases
        pokemon.updateHappiness(5);
        assertEquals(75, pokemon.happiness);
        pokemon.updateHappiness(200);
        assertEquals(200, pokemon.happiness);

        // test happiness decreases
        pokemon.updateHappiness(-5);
        assertEquals(195, pokemon.happiness);
        pokemon.updateHappiness(-200);
        assertEquals(0, pokemon.happiness);
    }

    @Test
    public void testEgg() {
        PokemonModel pokemon = new PokemonModel(1, 0, false);

        // test egg characteristics
        assertEquals(0, pokemon.level);
        assertEquals("EGG", pokemon.getName());
        assertEquals("egg", pokemon.getSpriteId());
        assertEquals(0, pokemon.moves.length);

        // check that characteristics are updated after hatching
        pokemon.addXP(1);
        assertEquals(1, pokemon.level);
        assertEquals("BULBASAUR", pokemon.getName());
        assertEquals("1", pokemon.getSpriteId());
        assertNotEquals(0, pokemon.moves.length);
    }

    @Test
    public void testShinyRate() {
        PokedexModel pokedexModel = new PokedexModel();
        double initialRate = pokedexModel.getShinyRate(1);
        assertTrue(initialRate > 0);
        pokedexModel.setCaught(1);
        assertTrue(pokedexModel.getShinyRate(1) > initialRate);
    }

    @Test
    public void testIVs() {
        PokemonModel pokemonStrong = new PokemonModel(1, 100, false);
        PokemonModel pokemonWeak = new PokemonModel(1, 1, false);
        // both Pokemon should have randomly generated IVs, so they should not be equal
        assertNotEquals(pokemonStrong.ivs, pokemonWeak.ivs);

        // test IV gain after winning a battle
        int previousIV = pokemonStrong.ivs[pokemonWeak.ivGain];
        pokemonStrong.updateIVs(pokemonWeak.ivGain);
        assertTrue(pokemonStrong.ivs[pokemonWeak.ivGain] > previousIV);
    }

    @Test
    public void testDayCare() 
    {
        // create the Pokemon
        PokemonModel[] pokemon = new PokemonModel[3];
        pokemon[0] = new PokemonModel(1, 10, false);
        pokemon[0].genderId = 0;
        pokemon[1] = new PokemonModel(4, 10, false);
        pokemon[1].genderId = 0;
        pokemon[2] = new PokemonModel(7, 10, false);
        pokemon[2].genderId = 1;
        DayCareModel dayCareModel = new DayCareModel();

        assertEquals(null, dayCareModel.withdrawPokemon(0));
        assertEquals(null, dayCareModel.withdrawPokemon(1));

        // make sure Day Care takes exactly two Pokemon
        assertTrue(dayCareModel.setPokemon(pokemon[0]));
        assertTrue(dayCareModel.setPokemon(pokemon[1]));
        assertFalse(dayCareModel.setPokemon(pokemon[2]));

        // make sure two Pokemon of same gender don't create an egg
        for (int i = 0; i < 2101; i++)
        {
            assertEquals(-1, dayCareModel.decrementStepCounter());
        }

        // withdraw a female and deposit a male
        assertEquals(4, dayCareModel.withdrawPokemon(1).pokemon_id);
        assertEquals(null, dayCareModel.withdrawPokemon(1));
        assertTrue(dayCareModel.setPokemon(pokemon[2]));

        // make sure a male and female do create an egg
        int maxValue = -1;

        for (int i = 0; i < 2101; i++)
        {
            maxValue = Math.max(maxValue, dayCareModel.decrementStepCounter());
        }

        assertNotEquals(-1, maxValue);
    }

    @Test
    public void testGetFirstEvolution() 
    {
        // make sure first stage evolutions are identified properly
        DayCareModel dayCareModel = new DayCareModel();
        assertEquals(150, dayCareModel.getFirstEvolution(150));
        assertEquals(1, dayCareModel.getFirstEvolution(1));
        assertEquals(1, dayCareModel.getFirstEvolution(2));
        assertEquals(1, dayCareModel.getFirstEvolution(3));
        assertEquals(133, dayCareModel.getFirstEvolution(471));
    }
}
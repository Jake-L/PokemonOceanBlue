package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pokemonoceanblue.PokedexModel;
import pokemonoceanblue.PokemonModel;

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
}
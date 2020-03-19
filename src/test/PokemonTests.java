package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

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
}
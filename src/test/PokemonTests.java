package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pokemonoceanblue.PokedexModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Type;

public class PokemonTests {
    @Test
    /**
     * Check that happinesss always falls within 0 and 200
     */
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
    /**
     * Test that eggs have the name and sprite of an egg rather than the underlying Pokemon
     */
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
    /**
     * Test that shiny rate increases after catching a pokemon of that species
     */
    public void testShinyRate() {
        PokedexModel pokedexModel = new PokedexModel();
        double initialRate = pokedexModel.getShinyRate(1);
        assertTrue(initialRate > 0);
        pokedexModel.setCaught(1);
        assertTrue(pokedexModel.getShinyRate(1) > initialRate);
    }

    @Test
    /**
     * Test that IVs are randomly generated and that they increase after a battle
     */
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
    /**
     * Check that Castform's type changes correctly based on the weather
     */
    public void testCastform() {
        // default Castform type is NORMAL
        PokemonModel pokemon = new PokemonModel(351, 100, false);
        assertEquals(pokemon.types[0], Type.NORMAL);

        // changes to FIRE in sunny weather
        pokemon.checkFormChange((byte)1, (byte)0);
        assertEquals(pokemon.types[0], Type.FIRE);

        // changes to WATER in rainy weather
        pokemon.checkFormChange((byte)2, (byte)0);
        assertEquals(pokemon.types[0], Type.WATER);

        // changes to ICE in snowy weather
        pokemon.checkFormChange((byte)4, (byte)0);
        assertEquals(pokemon.types[0], Type.ICE);
    }

    @Test
    /**
     * Check that different Pokemon need different amounts of XP
     * to reach level 100
     */
    public void testLevelModifier() {
        // mewtwo should need 1,250,000 xp to reach level 100
        // and 1,212,574 xp to reach level 99
        PokemonModel pokemon = new PokemonModel(150, 0, false);
        pokemon.addXP(1212873);
        assertEquals(98, pokemon.level);
        pokemon.addXP(1);
        assertEquals(99, pokemon.level);
        pokemon.addXP(1000000);
        assertEquals(100, pokemon.level);

        // test the above conditions using calcXP instead
        pokemon = new PokemonModel(150, 99, false);
        assertEquals(1212874, pokemon.calcXP(0));
        assertEquals(1250000, pokemon.calcXP(1));

        // smeargle should need 500,000 xp to reach level 100
        pokemon = new PokemonModel(235, 100, false);
        assertEquals(500000, pokemon.calcXP(0));

        // bulbasaur should need 1,050,000 xp to reach level 100
        pokemon = new PokemonModel(1, 100, false);
        assertEquals(1050000, pokemon.calcXP(0));
    }

    @Test
    /**
     * Check that a Pokemon missing HP will still be missing it after leveling up
     */
    public void testLevelUpCurrentHP() {
        // Create a level 10 Bulbasaur missing 5 HP
        PokemonModel pokemon = new PokemonModel(1, 10, false);
        assertEquals(10, pokemon.level);
        pokemon.currentHP -= 5;
        assertEquals(pokemon.stats[0] - 5, pokemon.currentHP);

        // add enough XP to level up at least once
        pokemon.addXP(1000);
        assertTrue(pokemon.level > 10);

        // check that the Pokemon is still missing 5 HP
        assertEquals(pokemon.stats[0] - 5, pokemon.currentHP);
    }

    @Test
    /**
     * Make sure Shedinja's unique HP stat works
     */
    public void testShedinja()
    {
        PokemonModel pokemon;

        // a level 1 Shedinja should have 1 HP
        pokemon = new PokemonModel(292, 1, false);
        assertEquals(1, pokemon.currentHP);
        assertEquals(1, pokemon.stats[0]);

        // a level 100 Shedinja should have 1 HP
        pokemon = new PokemonModel(292, 100, false);
        assertEquals(1, pokemon.currentHP);
        assertEquals(1, pokemon.stats[0]);
    }

    @Test
    /**
     * Check that raid boss encounters have much higher stats than usual
     */
    public void testRaidBoss()
    {
        PokemonModel pokemon = new PokemonModel(150, 100, false, false);
        PokemonModel raidBoss = new PokemonModel(150, 100, false, true);

        for (int i = 0; i < 6; i++)
        {
            // allow an interval around double to account for differences in IVs
            assertTrue(raidBoss.stats[i] * 1.00 / pokemon.stats[i] > 1.8);
            assertTrue(raidBoss.stats[i] * 1.00 / pokemon.stats[i] < 2.2);
        }
    }
}

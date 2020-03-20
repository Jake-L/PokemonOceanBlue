package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import pokemonoceanblue.EvolutionCheck;
import pokemonoceanblue.PokemonModel;

public class EvolutionTests {
    @Test
    public void testEvolveMapId() {
        EvolutionCheck evolutionCheck = new EvolutionCheck();

        // max happiness Eevee on map 14 should evolve into Leafeon
        PokemonModel pokemon = new PokemonModel(133, 1, false);
        pokemon.updateHappiness(200);
        assertEquals(470, evolutionCheck.checkEvolution(pokemon, 14));

        // max happiness Eevee on a map other than map 14 should not evolve into Leafeon
        assertNotEquals(470, evolutionCheck.checkEvolution(pokemon, 0));
    }

    @Test
    public void testEvolveLevel() {
        EvolutionCheck evolutionCheck = new EvolutionCheck();

        // Bulbasaur shouldn't evolve at level 1
        PokemonModel pokemon = new PokemonModel(1, 1, false);
        assertEquals(-1, evolutionCheck.checkEvolution(pokemon, 0));

        // Bulbasaur should evolve at level 16
        pokemon = new PokemonModel(1, 16, false);
        assertEquals(2, evolutionCheck.checkEvolution(pokemon, 0));
        
        // Bulbasaur should evolve into Ivysaur at level 40, not Venusaur
        pokemon = new PokemonModel(1, 40, false);
        assertEquals(2, evolutionCheck.checkEvolution(pokemon, 0));
    }
}
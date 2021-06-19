package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pokemonoceanblue.BattleOperationsManager;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.StatusEffect;

public class BattleOperationsManagerTests {

    @Test
    public void testCaptureRateLegendary() {
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager();
        PokemonModel pokemon = new PokemonModel(150, 100, false);
        
        // master ball should have capture chance significantly above 100%
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 0) > 1000);

        // pokeball should have less than zero capture chance at full health
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) < 0);

        // pokeball should have capture chance close to 2% with 1 hp
        pokemon.currentHP = 1;
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) > 1.9);
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) < 2);

        // great ball should have capture chance close to 3% with 1 hp
        pokemon.currentHP = 1;
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 2) > 2.9);
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 2) < 3);

        // ultra ball should have capture chance close to 4% with 1 hp
        pokemon.currentHP = 1;
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 1) > 3.9);
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 1) < 4);

        // pokeball should have capture chance close to 4% with 1 hp when Pokemon is asleep
        // pokemon.currentHP = 1;
        // pokemon.statusEffect = StatusEffect.SLEEP;
        // assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) > 3.9);
        // assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) < 4);
    }

    @Test
    /**
     * Checks that teams are correctly identified as all fainted or not
     */
    public void testTeamFainted() {
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager();
        PokemonModel[] team;
        
        // team just includes one full health Pokemon
        team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 1, false);
        assertFalse(battleOperationsManager.teamFainted(team));

        // team just includes one fainted Pokemon
        team[0].currentHP = 0;
        assertTrue(battleOperationsManager.teamFainted(team));

        // team includes five fainted Pokemon and one full health Pokemon
        team = new PokemonModel[6];
        team[0] = new PokemonModel(1, 1, false);
        team[0].currentHP = 0;
        team[1] = new PokemonModel(1, 1, false);
        team[1].currentHP = 0;
        team[2] = new PokemonModel(1, 1, false);
        team[2].currentHP = 0;
        team[3] = new PokemonModel(1, 1, false);
        team[3].currentHP = 0;
        team[4] = new PokemonModel(1, 1, false);
        team[4].currentHP = 0;
        team[5] = new PokemonModel(1, 1, false);
        assertFalse(battleOperationsManager.teamFainted(team));

        // team includes six fainted Pokemon
        team[5].currentHP = 0;
        assertTrue(battleOperationsManager.teamFainted(team));
    }
}

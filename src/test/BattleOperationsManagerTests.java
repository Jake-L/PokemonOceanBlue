package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.StatusEffect;
import pokemonoceanblue.battle.BattleOperationsManager;
import pokemonoceanblue.battle.TurnEffectManager;

public class BattleOperationsManagerTests {

    @Test
    public void testCaptureRateLegendary() 
    {
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager(new TurnEffectManager());
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
        pokemon.currentHP = 1;
        pokemon.statusEffect = (byte)StatusEffect.SLEEP;
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) > 3.9);
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) < 4);
    }

    @Test
    /**
     * Checks that teams are correctly identified as all fainted or not
     */
    public void testTeamFainted() 
    {
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager(new TurnEffectManager());
        PokemonModel[] team;
        
        // team just includes one full health Pokemon
        team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 1, false);
        assertFalse(battleOperationsManager.teamFainted(team));

        // team just includes one fainted Pokemon
        team[0].currentHP = 0;
        assertTrue(battleOperationsManager.teamFainted(team));

        // team includes six fainted Pokemon
        team = new PokemonModel[6];
        for (int i = 0; i < team.length; i++)
        {
            team[i] = new PokemonModel(1, 1, false);
            team[i].currentHP = 0;  
        }
        assertTrue(battleOperationsManager.teamFainted(team));

        // team includes five fainted Pokemon and one full health Pokemon
        team[5].currentHP = 1;
        assertFalse(battleOperationsManager.teamFainted(team));
    }

    @Test
    public void testFirstAttacker()
    {
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager(new TurnEffectManager());
        PokemonModel[] team = new PokemonModel[1];
        PokemonModel[] enemyTeam = new PokemonModel[1];
        int firstAttacker;
        
        // give both pokemon pound, which has normal priority
        // quick attack, which has positive priority
        // and counter, which has negative priority
        team[0] = new PokemonModel(1, 100, false);
        team[0].moves[0] = new MoveModel(1); 
        team[0].moves[1] = new MoveModel(98);
        team[0].moves[2] = new MoveModel(68);
        enemyTeam[0] = new PokemonModel(1, 100, false);
        enemyTeam[0].moves[0] = new MoveModel(1); 
        enemyTeam[0].moves[1] = new MoveModel(98);
        enemyTeam[0].moves[2] = new MoveModel(68);

        // enemy uses quick attack and player uses pound
        firstAttacker = battleOperationsManager.determineFirstAttacker(team[0], enemyTeam[0], 0, 1);
        assertEquals(1, firstAttacker);

        // enemy uses quick attack and player uses counter
        firstAttacker = battleOperationsManager.determineFirstAttacker(team[0], enemyTeam[0], 2, 1);
        assertEquals(1, firstAttacker);

        // enemy uses pound and player uses counter
        firstAttacker = battleOperationsManager.determineFirstAttacker(team[0], enemyTeam[0], 2, 0);
        assertEquals(1, firstAttacker);

        // player uses quick attack and enemy uses pound
        firstAttacker = battleOperationsManager.determineFirstAttacker(team[0], enemyTeam[0], 1, 0);
        assertEquals(0, firstAttacker);

        // player uses quick attack and enemy uses counter
        firstAttacker = battleOperationsManager.determineFirstAttacker(team[0], enemyTeam[0], 1, 2);
        assertEquals(0, firstAttacker);

        // player uses pound and enemy uses counter
        firstAttacker = battleOperationsManager.determineFirstAttacker(team[0], enemyTeam[0], 0, 2);
        assertEquals(0, firstAttacker);
    }
}

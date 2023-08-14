package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.StatusEffect;
import pokemonoceanblue.battle.BattleEvent;
import pokemonoceanblue.battle.BattleOperationsManager;
import pokemonoceanblue.battle.TurnEffectManager;

public class BattleOperationsManagerTests {

    @Test
    public void testCaptureRateLegendary() {
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

        // pokeball should have capture chance close to 4% with 1 hp when Pokemon is
        // asleep
        pokemon.currentHP = 1;
        pokemon.statusEffect = (byte) StatusEffect.SLEEP;
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) > 3.9);
        assertTrue(battleOperationsManager.captureChanceCalc(pokemon, 3) < 4);
    }

    @Test
    /**
     * Checks that teams are correctly identified as all fainted or not
     */
    public void testTeamFainted() {
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
        for (int i = 0; i < team.length; i++) {
            team[i] = new PokemonModel(1, 1, false);
            team[i].currentHP = 0;
        }
        assertTrue(battleOperationsManager.teamFainted(team));

        // team includes five fainted Pokemon and one full health Pokemon
        team[5].currentHP = 1;
        assertFalse(battleOperationsManager.teamFainted(team));
    }

    @Test
    public void testFirstAttacker() {
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

    @Test
    /**
     * Checks that critical hit odds are calculated correctly
     */
    public void testCritChance() {
        TurnEffectManager turnEffectManager = new TurnEffectManager();
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager(turnEffectManager);
        int critChance;
        MoveModel move;
        PokemonModel attackingPokemon = new PokemonModel(1, 5, false);
        PokemonModel defendingPokemon = new PokemonModel(4, 5, false);

        // Pound is a simple move with no crit modifiers
        move = new MoveModel(1);
        critChance = battleOperationsManager.getCritChance(move, 0, attackingPokemon, defendingPokemon);
        assertEquals(1, critChance);

        // Night Slash has a boosted crit chance
        move = new MoveModel(400);
        critChance = battleOperationsManager.getCritChance(move, 0, attackingPokemon, defendingPokemon);
        assertEquals(2, critChance);

        // Pokemon with Super Luck ability have boosted crit chance
        attackingPokemon = new PokemonModel(359, 5, false);
        move = new MoveModel(1);
        critChance = battleOperationsManager.getCritChance(move, 0, attackingPokemon, defendingPokemon);
        assertEquals(2, critChance);

        // Pokemon with Shell Armor ability cannot be hit by critical hits
        defendingPokemon = new PokemonModel(131, 5, false);
        move = new MoveModel(1);
        critChance = battleOperationsManager.getCritChance(move, 0, attackingPokemon, defendingPokemon);
        assertEquals(0, critChance);

        // Focus Energy adds a much boosted crit chance
        move = new MoveModel(116);
        PokemonModel[][] team = new PokemonModel[2][1];
        team[0][0] = new PokemonModel(1, 1, false);
        team[1][0] = new PokemonModel(1, 1, false);
        int currentPokemon[] = new int[2];
        attackingPokemon = new PokemonModel(1, 5, false);
        defendingPokemon = new PokemonModel(4, 5, false);
        BattleEvent event = turnEffectManager.addMultiTurnEffect(move, 0, team, currentPokemon);
        assertEquals("BULBASAUR is getting pumped up.", event.text);
        critChance = battleOperationsManager.getCritChance(move, 0, attackingPokemon, defendingPokemon);
        assertEquals(3, critChance);
    }

    @Test
    /**
     * Checks that critical hit random generation works
     */
    public void testCrit() {
        TurnEffectManager turnEffectManager = new TurnEffectManager();
        BattleOperationsManager battleOperationsManager = new BattleOperationsManager(turnEffectManager);
        int critCount = 0;

        // check that a regular attack (modifier = 1) crits roughly 6% of the time
        for (int i = 0; i < 1000; i++) {
            if (battleOperationsManager.isCrit(1)) {
                critCount++;
            }
        }

        assertTrue(critCount > 40);
        assertTrue(critCount < 85);

        // check that a triple boosted attack (modifier = 4) crits roughly 25% of the time
        critCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (battleOperationsManager.isCrit(4)) {
                critCount++;
            }
        }

        assertTrue(critCount > 220);
        assertTrue(critCount < 280);
    }
}

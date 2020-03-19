package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import pokemonoceanblue.BattleModel;
import pokemonoceanblue.PokemonModel;

public class BattleTests {
    /**
     * Test that status effects get applied
     * Done by using thunder wave 10 times, since it has 90% accuracy
     */
    @Test
    public void testParalyze() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(25, 21, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 1, false);
        // make sure Pikachu knows thunder wave
        assertEquals("THUNDER WAVE", team[0].moves[3].name);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, true);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to make sure thunder wave lands
        for (int i = 0; i < 10; i++)
        {
            assertEquals(0, battleModel.events.size());
            // choose "FIGHT"
            battleModel.confirmSelection();
            // choose "THUNDER WAVE"
            updateBattleModel(battleModel, 20);
            battleModel.optionIndex = 3;
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);
        }
        assertEquals(1, enemyTeam[0].statusEffect); 
    }
    
    /**
     * Test that one hit KO eventually kills
     * Done by using fissure on an opponent with no possible damage (lvl 1 magikarp)
     */
    @Test
    public void testOneHitKO() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(340, 57, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(129, 1, false);
        // make sure whiscash knows fissure
        assertEquals("FISSURE", team[0].moves[0].name);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, true);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run until fissure lands
        while (enemyTeam[0].currentHP != 0)
        {
            //make sure fissure hasnt landed yet
            boolean attackMissed = true;
            while (attackMissed && battleModel.events.size() != 0)
            {
                if (battleModel.getText() == "It's a one hit KO!")
                {
                    attackMissed = false;
                    assertEquals(0, enemyTeam[0].currentHP);
                }
                else
                {
                    assertNotEquals(0, enemyTeam[0].currentHP);
                }
            }
            // choose "FIGHT"
            battleModel.confirmSelection();
            // choose "FISSURE"
            updateBattleModel(battleModel, 20);
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);
        }
    }

    /**
     * Test that recoil damage is applied
     * Done by using double edge (100 accuracy + recoil) against an opponent with no possible damage (lvl 1 magikarp)
     */
    @Test
    public void testRecoil() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(19, 31, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(129, 1, false);
        // make sure rattata knows double edge
        assertEquals("DOUBLE-EDGE", team[0].moves[0].name);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, true);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run through a sequence of battle
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "double edge"
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        assertNotEquals(team[0].stats[0], team[0].currentHP);
        assertNotEquals(0, team[0].currentHP);
    }

    /**
     * Tests that enemies switch their Pokemon after one faints
     * Done by having a level 100 use a 100% accuracy move on a level 1
     */
    @Test
    public void testEnemySubstitution() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(25, 100, false);
        PokemonModel[] enemyTeam = new PokemonModel[2];
        enemyTeam[0] = new PokemonModel(1, 1, false);
        enemyTeam[1] = new PokemonModel(2, 1, false);
        // make sure Pikachu knows discharge
        assertEquals("DISCHARGE", team[0].moves[2].name);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, true);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // make sure first Pokemon is sent out
        assertEquals(0, battleModel.currentPokemon[1]);
        // choose "FIGHT"
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // choose "DISCHARGE"
        updateBattleModel(battleModel, 20);
        battleModel.optionIndex = 2;
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        assertEquals(0, battleModel.events.size());
        // now the second Pokemon should be sent out
        assertEquals(1, battleModel.currentPokemon[1]);
    }

    @Test
    public void testInitialPokemon() {
        PokemonModel[] team = new PokemonModel[3];
        // first Pokemon has no HP
        team[0] = new PokemonModel(1, 1, false);
        team[0].currentHP = 0;
        // second Pokemon is an egg
        team[1] = new PokemonModel(1, 0, false);
        team[2] = new PokemonModel(1, 1, false);

        // create the battle
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 1, false);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, true);
        // skip opening animations
        updateBattleModel(battleModel, 500);

        // should start with third Pokemon
        assertEquals(2, battleModel.currentPokemon[0]);
    }

    /**
     * Updates battleModel in a loop to skip through animations
     * and input delays
     * @param battleModel
     * @param counter the amount of times to call the update method
     */
    private void updateBattleModel(BattleModel battleModel, int counter)
    {
        for (int i = 0; i < counter; i++)
        {
            battleModel.update();
        }
    }
}
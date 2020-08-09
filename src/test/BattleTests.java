package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import pokemonoceanblue.BattleModel;
import pokemonoceanblue.MoveModel;
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
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
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
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
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
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
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
     * Test that a move with 0% effectiveness does no damage
     * Done by using tackle agianst a ghost type
     */
    @Test
    public void testZeroEffectiveness() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 1, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(92, 1, false);
        // make sure bulbasaur knows tackle and gastly only knows swords dance 
        assertEquals("TACKLE", team[0].moves[0].name);
        enemyTeam[0].moves[0] = new MoveModel(14);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run through a sequence of battle
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "tackle"
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        assertEquals(enemyTeam[0].stats[0], enemyTeam[0].currentHP);
    }

    /**
     * Test that pokemon flinch
     * Done by using fakeout
     */
    @Test
    public void testFlinch() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(169, 1, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 1, false);
        // give crobat fakeout
        team[0].moves[0] = new MoveModel(252);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run through a sequence of battle
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "fakeout"
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        assertEquals(team[0].stats[0], team[0].currentHP);
    }

    /**
     * Test that pokemon uses a random move when confused
     * Done by having enemy use confuse ray and checking if a random move is used
     */
    @Test
    public void testConfusion() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 1, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 1, false);
        // set both sides moves
        enemyTeam[0].moves = new MoveModel[1];
        enemyTeam[0].moves[0] = new MoveModel(109);
        team[0].moves = new MoveModel[4];
        team[0].moves[0] = new MoveModel(14);
        team[0].moves[1] = new MoveModel(33);
        team[0].moves[2] = new MoveModel(33);
        team[0].moves[3] = new MoveModel(33);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to check that a random attack is used
        int counter = 0;
        while(true)
        {
            assertEquals(0, battleModel.events.size());
            // choose "FIGHT"
            battleModel.confirmSelection();
            // choose "SWORDS DANCE"
            updateBattleModel(battleModel, 20);
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);
            //if enemy took damage, swords dance was not used. therefore confusion working as intended
            if (enemyTeam[0].currentHP != enemyTeam[0].stats[0])
            {
                break;
            }
            //it should take significantly less than 15 turns for confusion to have an effect
            if (counter == 30)
            {
                fail();
            }
            counter ++;
        } 
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
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // make sure first Pokemon is sent out
        assertEquals(0, battleModel.currentPokemon[1]);
        // choose "FIGHT"
        battleModel.confirmSelection();
        updateBattleModel(battleModel, 20);
        // choose "DISCHARGE"
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
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);

        // should start with third Pokemon
        assertEquals(2, battleModel.currentPokemon[0]);
    }

     /**
     * Test that stat effects change stats
     * Done by using a metapod that only knows harden against magikarp
     */
    @Test
    public void testStatChanges() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(11, 7, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(129, 1, false);
        // make sure metapod knows harden
        assertEquals("HARDEN", team[0].moves[0].name);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to check that metapods defense changes
        for (int i = 0; i < 5; i++)
        {
            assertEquals(0, battleModel.events.size());
            // choose "FIGHT"
            battleModel.confirmSelection();
            // choose "HARDEN"
            updateBattleModel(battleModel, 20);
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);
        }
        assertNotEquals((team[0].getStat(2, battleModel.statChanges[0][2])), team[0].stats[2]); 
        assertTrue(team[0].getStat(2, battleModel.statChanges[0][2]) > team[0].stats[2]);
    }

    /**
     * Test that an asleep pokemon will eventually wake up
     * Done by using spore (100 accuracy sleep inducer) on enemy followed by using tackle as a lvl 1 bulbasaur
     * against a lvl 100 bulbasaur
     */
    @Test
    public void testWakeUp() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 1, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 100, false);
        // set both sides moves
        enemyTeam[0].moves[0] = new MoveModel(14);
        enemyTeam[0].moves[1] = new MoveModel(14);
        enemyTeam[0].moves[2] = new MoveModel(14);
        enemyTeam[0].moves[3] = new MoveModel(14);
        team[0].moves = new MoveModel[2];
        team[0].moves[0] = new MoveModel(147);
        team[0].moves[1] = new MoveModel(33);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        //put lvl 100 asleep
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        updateBattleModel(battleModel, 20);
        // choose "spore"
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        assertEquals(2, enemyTeam[0].statusEffect);
        // run until lvl 100 dies or wakes up
        while (enemyTeam[0].currentHP > 0)
        {
            assertEquals(0, battleModel.events.size());
            // choose "FIGHT"
            battleModel.confirmSelection();
            // choose "tackle"
            updateBattleModel(battleModel, 20);
            battleModel.optionIndex = 1;
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);
            //check if lvl 100 woke up
            if(enemyTeam[0].statusEffect == 0)
            {
                break;
            }
        }
    }

    /**
     * Test accuracy
     * Done by using a butterfree that knows double team against magikarp that only knows water gun
     */
    @Test
    public void testAccuracy() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(12, 100, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(129, 1, false);
        // make sure buterfree knows double team and magikarp knows constrict (10 damage with no STAB)
        team[0].moves[0] = new MoveModel(104);
        assertEquals("DOUBLE TEAM", team[0].moves[0].name);
        enemyTeam[0].moves[0] = new MoveModel(132);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to check that metapods defense changes
        boolean attackMissed = false;
        while (!attackMissed)
        {
            int previousHP = team[0].currentHP;
            assertEquals(0, battleModel.events.size());
            // choose "FIGHT"
            battleModel.confirmSelection();
            // choose "DOUBLE TEAM"
            updateBattleModel(battleModel, 20);
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);
            if (previousHP == team[0].currentHP)
            {
                attackMissed = true;
            }
            //if butterfree dies before magikarp misses, 
            //accuracy does not work correctly (it will take a large amount of hits with what should be low accuracy)
            if (team[0].currentHP == 0)
            {
                fail();
            }
        } 
    }

    /**
     * Test that badly poison target takes more damage from second tick than first
     * Done giving toxic 100 accuracy then using it on lvl 1 rattata
     */
    @Test
    public void testBadlyPoison() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(19, 100, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(19, 1, false);
        //give lvl 100 rattata toxic with perfect accuracy and give both rattatas splash
        team[0].moves = new MoveModel[2];
        team[0].moves[0] = new MoveModel(92);
        team[0].moves[0].accuracy = -1;
        team[0].moves[1] = new MoveModel(150);
        enemyTeam[0].moves = new MoveModel[1];
        enemyTeam[0].moves[0] = new MoveModel(150);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "toxic"
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        assertEquals(6, enemyTeam[0].statusEffect);
        int firstTick = enemyTeam[0].stats[0] - enemyTeam[0].currentHP;
        //get through next turn with splash
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "splash"
        updateBattleModel(battleModel, 20);
        battleModel.optionIndex = 1;
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        //check if more damage was dealt by second tick of toxic
        int secondTick = enemyTeam[0].stats[0] - enemyTeam[0].currentHP - firstTick;
        if (firstTick >= secondTick)
        {
            fail();
        }
    }

    /**
     * Test that certain moveEffects will prevent player from switching pokemon
     * Done by using two bulbasaurs, enemy bulbasaur uses nightmare, player bulbasaur tries to switch out
     */
    @Test
    public void testUnableToSwitchPokemon() {
        PokemonModel[] team = new PokemonModel[2];
        team[0] = new PokemonModel(1, 1, false);
        team[1] = new PokemonModel(4, 3, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 1, false);
        //give enemy bulbasaur nightmare as only move
        enemyTeam[0].moves = new MoveModel[1];
        enemyTeam[0].moves[0] = new MoveModel(171);
        //give player bulbasaur splash for first turn
        team[0].moves[0] = new MoveModel(150);
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        assertEquals(0, battleModel.events.size());
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "splash"
        updateBattleModel(battleModel, 20);
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 600);
        //try switching pokemon
        assertEquals(0, battleModel.events.size());
        // choose "POKEMON"
        battleModel.optionIndex = 1;
        battleModel.confirmSelection();
        //make sure party screen was not opened
        assertNotEquals(battleModel.battleOptions, null); 
    }

    /**
     * Test that Snore and Dream Eater only work when a Pokemon is asleep
     * Done by using spore (100 accuracy sleep inducer) on enemy
     * uses level 20 bulbasaur against a level 1 bulbasaur
     */
    @Test
    public void testSleepEffects() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 20, false);
        PokemonModel[] enemyTeam = new PokemonModel[1];
        enemyTeam[0] = new PokemonModel(1, 1, false);

        // set both sides moves
        enemyTeam[0].moves = new MoveModel[1];
        enemyTeam[0].moves[0] = new MoveModel(173); // snore
        team[0].moves = new MoveModel[2];
        team[0].moves[0] = new MoveModel(147); // spore
        team[0].moves[1] = new MoveModel(138); // dream eater
        BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);

        // skip opening animations
        updateBattleModel(battleModel, 500);
        assertEquals(0, battleModel.events.size());

        // first turn, check that Snore and Dream Eater does nothing when Pokemon is awake
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "Dream Eater"
        updateBattleModel(battleModel, 20);
        battleModel.optionIndex = 1;
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        // player should not have taken any damage
        assertEquals(team[0].getStat(0, 0), team[0].currentHP);
        // enemy should not have taken any damage
        assertEquals(enemyTeam[0].getStat(0, 0), enemyTeam[0].currentHP);

        // second turn, put enemy to sleep and check that Snore does damage
        // choose "FIGHT"
        battleModel.confirmSelection();
        // choose "Spore"
        updateBattleModel(battleModel, 20);
        battleModel.optionIndex = 0;
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        // enemy should be asleep
        assertEquals(2, enemyTeam[0].statusEffect);
        // enemy should not have taken any damage
        assertEquals(enemyTeam[0].getStat(0, 0), enemyTeam[0].currentHP);
        // player should have taken damage from Snore
        assertTrue(team[0].currentHP < team[0].getStat(0, 0));

        // third turn, Dream Eater should do damage
        battleModel.confirmSelection();
        // choose "Dream Eater"
        updateBattleModel(battleModel, 20);
        battleModel.optionIndex = 1;
        battleModel.confirmSelection();
        // wait for all the battle text to process
        updateBattleModel(battleModel, 500);
        // enemy should have taken any damage
        assertTrue(enemyTeam[0].currentHP < enemyTeam[0].getStat(0, 0));
    }

    /**
     * Test that Heal Bell and Aromatherapy remove status effects from all allies
     */
    @Test
    public void testStatusClearMoves() 
    {
        // heal bell and aromatherapy
        int[] statusClearMoves = new int[]{215, 312};

        for (int moveId : statusClearMoves)
        {
            PokemonModel[] team = new PokemonModel[2];
            team[0] = new PokemonModel(1, 20, false);
            team[1] = new PokemonModel(4, 20, false);
            PokemonModel[] enemyTeam = new PokemonModel[1];
            enemyTeam[0] = new PokemonModel(1, 1, false);

            // set status effects on the Pokemon
            // make sure the first Pokemon has an effect that stills allows attacking
            team[0].statusEffect = (byte) 4;
            team[1].statusEffect = (byte) 1;

            // set both sides moves
            enemyTeam[0].moves = new MoveModel[1];
            enemyTeam[0].moves[0] = new MoveModel(10); // scratch
            team[0].moves = new MoveModel[1];
            team[0].moves[0] = new MoveModel(moveId); // heal bell
            BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);

            // skip opening animations
            updateBattleModel(battleModel, 500);
            assertEquals(0, battleModel.events.size());

            // double check that the Pokemon still have status effects
            assertEquals(4, team[0].statusEffect);
            assertEquals(1, team[1].statusEffect);

            // choose "FIGHT"
            battleModel.confirmSelection();
            updateBattleModel(battleModel, 20);
            // choose "Heal Bell"
            battleModel.optionIndex = 0;
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);

            // enemy should not have taken any damage
            assertEquals(enemyTeam[0].getStat(0, 0), enemyTeam[0].currentHP);

            // check that heal bell removed the status effects
            assertEquals(0, team[0].statusEffect);
            assertEquals(0, team[1].statusEffect);
        }
    }

    /**
     * Make sure Explosion and Self-Destruct damage the enemy and faints the user
     */
    @Test
    public void testExplosion() 
    {
        // Explosion and Self-Destruct
        int[] explosionMoves = new int[]{120, 153};

        for (int moveId : explosionMoves)
        {
            PokemonModel[] team = new PokemonModel[1];
            team[0] = new PokemonModel(1, 100, false);
            PokemonModel[] enemyTeam = new PokemonModel[1];
            enemyTeam[0] = new PokemonModel(1, 1, false);

            // set both sides moves
            enemyTeam[0].moves = new MoveModel[1];
            enemyTeam[0].moves[0] = new MoveModel(10); // scratch
            team[0].moves = new MoveModel[1];
            team[0].moves[0] = new MoveModel(moveId); // explosion
            BattleModel battleModel = new BattleModel(enemyTeam, team, null, (byte)0);

            // skip opening animations
            updateBattleModel(battleModel, 500);
            assertEquals(0, battleModel.events.size());

            // choose "FIGHT"
            battleModel.confirmSelection();
            updateBattleModel(battleModel, 20);
            // choose "Explosion"
            battleModel.optionIndex = 0;
            battleModel.confirmSelection();
            // wait for all the battle text to process
            updateBattleModel(battleModel, 500);

            // enemy should have 0 HP remaining
            assertEquals(0, enemyTeam[0].currentHP);

            // player should have 0 HP remaining
            assertEquals(0, team[0].currentHP);
        }
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
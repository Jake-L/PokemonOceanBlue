package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Stat;
import pokemonoceanblue.StatusEffect;
import pokemonoceanblue.Type;
import pokemonoceanblue.battle.WildPokemonBattle;

public class BattleTests {
    /**
     * Test that status effects get applied
     */
    @Test
    public void testParalyze() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(25, 21, false);
        
        PokemonModel enemyTeam = new PokemonModel(1, 1, false);
        // make sure Pikachu knows thunder wave and give it infinite accuracy
        assertEquals("THUNDER WAVE", team[0].moves[3].name);
        team[0].moves[3].accuracy = -1;
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // choose "THUNDER WAVE"
        chooseAttack(battleModel, 3);
        assertEquals(StatusEffect.PARALYSIS, enemyTeam.statusEffect); 
    }
    
    /**
     * Test that one hit KO is guaranteed to kill in one hit
     * Done by having a level 100 use fissure on a level 100 Parasect, 
     * which double resists ground type damage
     */
    @Test
    public void testOneHitKO() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(340, 100, false);
        // give fissure with 100% accuracy
        team[0].moves = new MoveModel[1];
        team[0].moves[0] = new MoveModel(90);
        team[0].moves[0].accuracy = -1;

        
        PokemonModel enemyTeam = new PokemonModel(47, 100, false);
        // give Parasect splash
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(150);

        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "fissure"
        chooseAttack(battleModel, 0);

        // make sure the level 100 died
        assertEquals(0, enemyTeam.currentHP);
    }

    /**
     * Test that recoil damage is applied
     * Done by using double edge (100 accuracy + recoil) against an opponent with no possible damage (lvl 1 magikarp)
     */
    @Test
    public void testRecoil() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(19, 31, false);
        
        PokemonModel enemyTeam = new PokemonModel(129, 1, false);
        // make sure rattata knows double edge
        assertEquals("DOUBLE-EDGE", team[0].moves[0].name);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "double edge"
        chooseAttack(battleModel, 0);

        // check that the user and enemy both took damage
        assertNotEquals(team[0].stats[0], team[0].currentHP);
        assertNotEquals(0, team[0].currentHP);
        assertNotEquals(enemyTeam.stats[0], enemyTeam.currentHP);
    }

    /**
     * Test that a move with 0% effectiveness does no damage
     * Done by using tackle agianst a ghost type
     */
    @Test
    public void testZeroEffectiveness() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 1, false);
        
        PokemonModel enemyTeam = new PokemonModel(92, 1, false);
        // make sure bulbasaur knows tackle and gastly only knows swords dance 
        assertEquals("TACKLE", team[0].moves[0].name);
        enemyTeam.moves[0] = new MoveModel(14);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "tackle"
        chooseAttack(battleModel, 0);

        assertEquals(enemyTeam.stats[0], enemyTeam.currentHP);
    }

    /**
     * Test that pokemon flinch
     * Done by using fakeout
     */
    @Test
    public void testFlinch() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(169, 1, false);
        
        PokemonModel enemyTeam = new PokemonModel(1, 1, false);
        // give crobat fakeout
        team[0].moves[0] = new MoveModel(252);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "fakeout"
        chooseAttack(battleModel, 0);

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
        
        PokemonModel enemyTeam = new PokemonModel(1, 1, false);
        // set both sides moves
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(109);
        team[0].moves = new MoveModel[4];
        team[0].moves[0] = new MoveModel(14);
        team[0].moves[1] = new MoveModel(33);
        team[0].moves[2] = new MoveModel(33);
        team[0].moves[3] = new MoveModel(33);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to check that a random attack is used
        int counter = 0;
        while(true)
        {
            // choose "SWORDS DANCE"
            chooseAttack(battleModel, 0);

            //if enemy took damage, swords dance was not used. therefore confusion working as intended
            if (enemyTeam.currentHP != enemyTeam.stats[0])
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
        
        PokemonModel enemyTeam = new PokemonModel(1, 1, false);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);
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
        
        PokemonModel enemyTeam = new PokemonModel(129, 1, false);
        // make sure metapod knows harden
        assertEquals("HARDEN", team[0].moves[0].name);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to check that metapods defense changes
        for (int i = 0; i < 5; i++)
        {
            // choose "HARDEN"
            chooseAttack(battleModel, 0);
        }
        assertNotEquals(battleModel.battleOperationsManager.getStat(team[0], 0, Stat.DEFENSE), team[0].stats[2]); 
        assertTrue(battleModel.battleOperationsManager.getStat(team[0], 0, Stat.DEFENSE) > team[0].stats[2]);
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
        
        PokemonModel enemyTeam = new PokemonModel(1, 100, false);
        // set both sides moves
        enemyTeam.moves[0] = new MoveModel(14);
        enemyTeam.moves[1] = new MoveModel(14);
        enemyTeam.moves[2] = new MoveModel(14);
        enemyTeam.moves[3] = new MoveModel(14);
        team[0].moves = new MoveModel[2];
        team[0].moves[0] = new MoveModel(147);
        team[0].moves[1] = new MoveModel(33);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "spore"
        chooseAttack(battleModel, 0);

        // make sure the enemy Pokemon is sleeping
        assertEquals(2, enemyTeam.statusEffect);

        // run until lvl 100 dies or wakes up
        while (enemyTeam.currentHP > 0)
        {
            // choose "tackle"
            chooseAttack(battleModel, 1);

            //check if lvl 100 woke up
            if(enemyTeam.statusEffect == 0)
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
        
        PokemonModel enemyTeam = new PokemonModel(129, 1, false);
        // make sure buterfree knows double team and magikarp knows constrict (10 damage with no STAB)
        team[0].moves[0] = new MoveModel(104);
        assertEquals("DOUBLE TEAM", team[0].moves[0].name);
        enemyTeam.moves[0] = new MoveModel(132);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);
        // skip opening animations
        updateBattleModel(battleModel, 500);
        // run a few loops to check that metapods defense changes
        boolean attackMissed = false;
        while (!attackMissed)
        {
            int previousHP = team[0].currentHP;

            // choose "DOUBLE TEAM"
            chooseAttack(battleModel, 0);

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
        
        PokemonModel enemyTeam = new PokemonModel(19, 1, false);
        //give lvl 100 rattata toxic with perfect accuracy and give both rattatas splash
        team[0].moves = new MoveModel[2];
        team[0].moves[0] = new MoveModel(92);
        team[0].moves[0].accuracy = -1;
        team[0].moves[1] = new MoveModel(150);
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(150);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "toxic"
        chooseAttack(battleModel, 0);

        assertEquals(6, enemyTeam.statusEffect);
        int firstTick = enemyTeam.stats[0] - enemyTeam.currentHP;

        // choose "splash"
        chooseAttack(battleModel, 1);

        //check if more damage was dealt by second tick of toxic
        int secondTick = enemyTeam.stats[0] - enemyTeam.currentHP - firstTick;
        if (firstTick >= secondTick)
        {
            fail();
        }
    }

    /**
     * Test that certain types are immune to certain status effects
     * Done by attempting to poison a steel type
     */
    @Test
    public void testStatusImmunity() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(212, 100, false);
        
        PokemonModel enemyTeam = new PokemonModel(212, 1, false);
        // give one Steelix poison powder and the other splash
        team[0].moves = new MoveModel[1];
        team[0].moves[0] = new MoveModel(77);
        team[0].moves[0].accuracy = -1;
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(150);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "poison powder"
        chooseAttack(battleModel, 0);

        // queue should be empty and Steelix should not be poisoned
        assertEquals(0, battleModel.events.size());
        assertEquals(0, enemyTeam.statusEffect);
    }

    /**
     * Test that Pokemon can faint from poisoning
     * and that a battle will end properly afterwards
     * Done by having a Rattata poison a rattata with 1 HP
     */
    @Test
    public void testDeathByPoison() {
        // passes when user's pokemon attacks first
        // fails when enemy's pokemon attacks first
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(19, 100, false);
        
        PokemonModel enemyTeam = new PokemonModel(19, 100, false);
        enemyTeam.currentHP = 1;
        // give one Rattata poison powder and the other splash
        team[0].moves = new MoveModel[1];
        team[0].moves[0] = new MoveModel(77);
        team[0].moves[0].accuracy = -1;
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(150);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "poison powder"
        chooseAttack(battleModel, 0);

        // enemy Rattata should be dead
        assertEquals(0, enemyTeam.currentHP);
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
        
        PokemonModel enemyTeam = new PokemonModel(1, 1, false);
        //give enemy bulbasaur nightmare as only move
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(171);
        //give player bulbasaur splash for first turn
        team[0].moves[0] = new MoveModel(150);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // choose "splash"
        chooseAttack(battleModel, 0);

        // try switching pokemon
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
        
        PokemonModel enemyTeam = new PokemonModel(1, 1, false);

        // set both sides moves
        enemyTeam.moves = new MoveModel[1];
        enemyTeam.moves[0] = new MoveModel(173); // snore
        team[0].moves = new MoveModel[2];
        team[0].moves[0] = new MoveModel(147); // spore
        team[0].moves[1] = new MoveModel(138); // dream eater
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // first turn, check that Snore and Dream Eater does nothing when Pokemon is awake
        // choose "Dream Eater"
        chooseAttack(battleModel, 1);
        // player should not have taken any damage
        assertEquals(team[0].stats[Stat.HP], team[0].currentHP);
        // enemy should not have taken any damage
        assertEquals(enemyTeam.stats[Stat.HP], enemyTeam.currentHP);

        // second turn, put enemy to sleep and check that Snore does damage
        // choose "Spore"
        chooseAttack(battleModel, 0);
        // enemy should be asleep
        assertEquals(2, enemyTeam.statusEffect);
        // enemy should not have taken any damage
        assertEquals(enemyTeam.stats[Stat.HP], enemyTeam.currentHP);
        // player should have taken damage from Snore
        assertTrue(team[0].currentHP < team[0].stats[Stat.HP]);

        // third turn, Dream Eater should do damage
        // choose "Dream Eater"
        chooseAttack(battleModel, 1);
        // enemy should have taken any damage
        assertTrue(enemyTeam.currentHP < enemyTeam.stats[Stat.HP]);
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
            
            PokemonModel enemyTeam = new PokemonModel(1, 1, false);

            // set status effects on the Pokemon
            // make sure the first Pokemon has an effect that stills allows attacking
            team[0].statusEffect = 4;
            team[1].statusEffect = 1;

            // set both sides moves
            enemyTeam.moves = new MoveModel[1];
            enemyTeam.moves[0] = new MoveModel(10); // scratch
            team[0].moves = new MoveModel[1];
            team[0].moves[0] = new MoveModel(moveId); // heal bell
            WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

            // skip opening animations
            updateBattleModel(battleModel, 500);

            // double check that the Pokemon still have status effects
            assertEquals(4, team[0].statusEffect);
            assertEquals(1, team[1].statusEffect);

            // choose "Heal Bell"
            chooseAttack(battleModel, 0);

            // enemy should not have taken any damage
            assertEquals(enemyTeam.stats[Stat.HP], enemyTeam.currentHP);

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
            
            PokemonModel enemyTeam = new PokemonModel(1, 1, false);

            // set both sides moves
            enemyTeam.moves = new MoveModel[1];
            enemyTeam.moves[0] = new MoveModel(10); // scratch
            team[0].moves = new MoveModel[1];
            team[0].moves[0] = new MoveModel(moveId); // explosion
            WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

            // choose "Explosion"
            chooseAttack(battleModel, 0);

            // enemy should have 0 HP remaining
            assertEquals(0, enemyTeam.currentHP);

            // player should have 0 HP remaining
            assertEquals(0, team[0].currentHP);
        }
    }

    @Test
    /**
     * Check that Castform's type changes correctly based on the weather
     */
    public void testCastform() {
        // default Castform type is NORMAL
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(351, 100, false);
        team[0].moves[0] = new MoveModel(240);
        team[0].moves[1] = new MoveModel(241);
        team[0].moves[2] = new MoveModel(258);
        
        PokemonModel enemyTeam = new PokemonModel(10, 1, false);
        assertEquals(team[0].types[0], Type.NORMAL);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // changes to FIRE after using Sunny Day
        this.chooseAttack(battleModel, 1);
        assertEquals(team[0].types[0], Type.FIRE);

        // changes to WATER after using Rain Dance
        this.chooseAttack(battleModel, 0);
        assertEquals(team[0].types[0], Type.WATER);

        // changes to ICE after using Hail
        this.chooseAttack(battleModel, 2);
        assertEquals(team[0].types[0], Type.ICE);
    }

    @Test
    /**
     * Test that ground type moves don't damage Pokemon with the ability Levitate
     */
    public void testLevitate() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(1, 100, false);
        // use Earthquake since it has 100% accuracy
        team[0].moves[0] = new MoveModel(89);
        
        PokemonModel enemyTeam = new PokemonModel(94, 1, false);
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);        

        // Gengar should have levitate ability
        assertEquals("LEVITATE", enemyTeam.ability.name);

        // choose "Earthquake"
        assertEquals("EARTHQUAKE", team[0].moves[0].name);
        chooseAttack(battleModel, 0);
        
        // enemy should not have taken any damage
        assertEquals(enemyTeam.stats[Stat.HP], enemyTeam.currentHP);
    }

    /**
     * Make sure Pokemon with the ability limber cannot be paralyzed
     */
    @Test
    public void testLimber() {
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(25, 21, false);
        
        PokemonModel enemyTeam = new PokemonModel(106, 1, false);
        
        // make sure Pikachu knows thunder wave and give it infinite accuracy
        assertEquals("THUNDER WAVE", team[0].moves[3].name);
        team[0].moves[3].accuracy = -1;
        WildPokemonBattle battleModel = new WildPokemonBattle(enemyTeam, team);

        // skip opening animations
        updateBattleModel(battleModel, 500);

        // choose "THUNDER WAVE"
        chooseAttack(battleModel, 3);

        // Hitmonlee should not be paralyzed
        assertEquals(StatusEffect.UNAFFLICTED, enemyTeam.statusEffect); 
    }

    /**
     * Helper function to skip start of turn animations, choose an attack,
     * and then skip end of turn animations and make sure no events remain
     * @param battleModel
     * @param optionIndex index of the attack to use
     */
    private void chooseAttack(WildPokemonBattle battleModel, int optionIndex)
    {
        // skip opening animations
        updateBattleModel(battleModel, 503);

        // choose "FIGHT"
        battleModel.confirmSelection();
        updateBattleModel(battleModel, 20);

        // choose a move
        battleModel.optionIndex = optionIndex;
        battleModel.confirmSelection();

        // skip end of turn animations
        updateBattleModel(battleModel, 503);
    }

    /**
     * Updates battleModel in a loop to skip through animations
     * and input delays
     * @param battleModel
     * @param counter the amount of times to call the update method
     */
    private void updateBattleModel(WildPokemonBattle battleModel, int counter)
    {
        for (int i = 0; i < counter; i++)
        {
            battleModel.update();
        }

        assertEquals(0, battleModel.events.size());
    }
}
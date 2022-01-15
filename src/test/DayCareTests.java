package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Utils;
import pokemonoceanblue.DayCareConversationModel;
import pokemonoceanblue.DayCareModel;

public class DayCareTests {
    @Test
    public void testDayCare() 
    {
        // create the Pokemon
        PokemonModel[] pokemon = new PokemonModel[3];
        pokemon[0] = new PokemonModel(1, 10, false);
        pokemon[0].genderId = 0;
        pokemon[1] = new PokemonModel(4, 10, false);
        pokemon[1].genderId = 0;
        pokemon[2] = new PokemonModel(7, 10, false);
        pokemon[2].genderId = 1;
        DayCareModel dayCareModel = new DayCareModel();

        assertEquals(null, dayCareModel.withdrawPokemon(0));
        assertEquals(null, dayCareModel.withdrawPokemon(1));

        // make sure Day Care takes exactly two Pokemon
        assertTrue(dayCareModel.setPokemon(pokemon[0]));
        assertTrue(dayCareModel.setPokemon(pokemon[1]));
        assertFalse(dayCareModel.setPokemon(pokemon[2]));

        // make sure two Pokemon of same gender don't create an egg
        for (int i = 0; i < 2101; i++)
        {
            assertEquals(-1, dayCareModel.decrementStepCounter());
        }

        // withdraw a female and deposit a male
        assertEquals(4, dayCareModel.withdrawPokemon(1).pokemon_id);
        assertEquals(null, dayCareModel.withdrawPokemon(1));
        assertTrue(dayCareModel.setPokemon(pokemon[2]));

        // make sure a male and female do create an egg
        int maxValue = -1;

        for (int i = 0; i < 2101; i++)
        {
            maxValue = Math.max(maxValue, dayCareModel.decrementStepCounter());
        }

        assertNotEquals(-1, maxValue);
    }

    @Test
    public void testGetFirstEvolution() 
    {
        // make sure first stage evolutions are identified properly
        assertEquals(150, Utils.getFirstEvolution(150));
        assertEquals(1, Utils.getFirstEvolution(1));
        assertEquals(1, Utils.getFirstEvolution(2));
        assertEquals(1, Utils.getFirstEvolution(3));
        assertEquals(133, Utils.getFirstEvolution(471));
    }

    @Test
    public void testDayCareConversation()
    {
        DayCareModel dayCareModel = new DayCareModel();

        // test depositing a Pokemon
        DayCareConversationModel dayCareConversationModel = new DayCareConversationModel(dayCareModel);
        assertEquals("You have no Pokemon deposited. Please select a Pokemon.", dayCareConversationModel.getText());
        for (int i = 0; i < 20; i++)
        {
            dayCareConversationModel.update();
        }
        dayCareConversationModel.nextEvent();
        assertTrue(dayCareConversationModel.openParty());
        PokemonModel pokemon = new PokemonModel(4, 10, false);
        assertTrue(dayCareConversationModel.setPokemon(pokemon));
        assertEquals("Thanks, we'll take care of your CHARMANDER.", dayCareConversationModel.getText());
        assertEquals(-1, dayCareConversationModel.getWithdrawnPokemon());
        for (int i = 0; i < 20; i++)
        {
            dayCareConversationModel.update();
        }
        dayCareConversationModel.nextEvent();
        assertTrue(dayCareConversationModel.isComplete());

        // test withdrawing a Pokemon
        dayCareConversationModel = new DayCareConversationModel(dayCareModel);
        assertEquals("We are currently taking care of one of your Pokemon. What would you like to do?", dayCareConversationModel.getText());
        dayCareConversationModel.setOption(1);
        assertEquals("Here is your CHARMANDER back.", dayCareConversationModel.getText());
        assertEquals(0, dayCareConversationModel.getWithdrawnPokemon());
        for (int i = 0; i < 20; i++)
        {
            dayCareConversationModel.update();
        }
        dayCareConversationModel.nextEvent();
        assertTrue(dayCareConversationModel.isComplete());
    }
}
package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import pokemonoceanblue.ConversationModel;
import pokemonoceanblue.Direction;
import pokemonoceanblue.CharacterModel;
import pokemonoceanblue.ConversationTriggerModel;

public class ConversationTests {
    @Test
    public void testConversationOption() {
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);
        CharacterModel cpuModel = new CharacterModel("oak", 5, 6, 0, 6, 0, Direction.DOWN);
        ConversationModel conversationModel = new ConversationModel(0, playerModel, cpuModel, false);
        // skip through 6 lines of dialog
        continueConversation(conversationModel, 6);
        updateConversation(conversationModel, 16);
        // choose a dialog option
        conversationModel.setOption(1);
        updateConversation(conversationModel, 14);
        // make sure the right gift Pokemon is recieved a result of the option
        assertEquals(4, conversationModel.getGiftPokemonId());
        continueConversation(conversationModel, 2);
        // make sure conversation completed
        assertTrue(conversationModel.isComplete());
    }

    @Test
    public void testRemoveCharacter() {
        // set up test
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);
        List<CharacterModel> characterModel = new ArrayList<CharacterModel>();
        characterModel.add(new CharacterModel("gary", 5, 6, 13, 13, 0, Direction.DOWN));
        characterModel.add(new CharacterModel("oak", 5, 6, 0, 6, 0, Direction.DOWN));
        List<ConversationTriggerModel> triggers = new ArrayList<ConversationTriggerModel>();
        ConversationModel conversationModel = new ConversationModel(13, playerModel, characterModel.get(0), false);

        // skip through 6 lines of dialog
        continueConversation(conversationModel, 4);
        conversationModel.setBattleComplete();
        continueConversation(conversationModel, 2);

        // remove the character
        for (int i = 0; i < 20; i++)
        {
            conversationModel.removeCharacter(characterModel, triggers);
            conversationModel.update();
        }
        conversationModel.nextEvent();

        // make sure conversation completed
        assertTrue(conversationModel.isComplete());

        // make sure the correct character was removed
        assertEquals(1, characterModel.size());
        assertEquals(0, characterModel.get(0).conversationId);
    }

    @Test
    public void testIsHealTeam() {
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);
        CharacterModel characterModel = new CharacterModel("joy", 5, 6, 17, 17, 0, Direction.DOWN);
        ConversationModel conversationModel = new ConversationModel(17, playerModel, characterModel, false);
        // skip through a line of dialog
        continueConversation(conversationModel, 1);
        
        boolean healTeam = false;
        for (int i = 0; i < 20; i++)
        {
            // make sure isHealTeam is true at some point in the event
            healTeam = conversationModel.isHealTeam();
            conversationModel.update();
            if (healTeam)
            {
                break;
            }
        }

        assertTrue(healTeam);

        // finish conversation
        continueConversation(conversationModel, 2);
        assertTrue(conversationModel.isComplete());
    }

    private void updateConversation(ConversationModel conversationModel, int counter)
    {
        for (int i = 0; i < counter; i++)
        {
            conversationModel.update();
        }
    }
    
    private void continueConversation(ConversationModel conversationModel, int counter)
    {
        for (int i = 0; i < counter; i++)
        {
            updateConversation(conversationModel, 16);
            conversationModel.nextEvent();
        }
    }
}
package pokemonoceanblue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationModel 
{
    private final int TEXT_LENGTH = 15;
    public final int conversationId;
    private int counter = 0;
    protected List<ConversationEvent> events = new ArrayList<ConversationEvent>();
    private boolean approachPlayer;
    private boolean battleStarted = false;
    
    /** 
     * Constructor
     * @param conversationId the unique identifier of this converstaion
     */
    public ConversationModel(int conversationId, CharacterModel player, CharacterModel cpu, boolean approachPlayer)
    {
        this.conversationId = conversationId;
        this.counter = TEXT_LENGTH;
        this.approachPlayer = approachPlayer;

        // move trainer to approach the player at the start of the conversation
        if (this.approachPlayer)
        {
            int xDistance = player.getX() - cpu.getX();
            if (xDistance < -1)
            {
                ConversationEvent event = new ConversationEvent(
                    0,
                    cpu.characterId,
                    -1,
                    0,
                    xDistance * -1 - 1,
                    0
                );
                this.events.add(event);
            }
            else if (xDistance > 1)
            {
                ConversationEvent event = new ConversationEvent(
                    0,
                    cpu.characterId,
                    1,
                    0,
                    xDistance - 1,
                    0
                );
                this.events.add(event);
            }
        }

        this.loadEvents();
    }

    /** 
     * Read the conversation data from a database
     */
    private void loadEvents()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT *"
                        + " FROM conversation WHERE conversation_id = " + this.conversationId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                ConversationEvent event = new ConversationEvent(rs);
                this.events.add(event);
            }

            // load conversation options
            for (ConversationEvent event : this.events)
            {
                if (event.optionId > -1)
                {
                    query = "SELECT text, next_conversation_event_id FROM conversation_options WHERE option_id = " + event.optionId;

                    rs = db.runQuery(query);

                    List<String> options = new ArrayList<String>();
                    List<Integer> optionOutcome = new ArrayList<Integer>();
        
                    while(rs.next()) 
                    {
                        options.add(rs.getString("text"));
                        optionOutcome.add(rs.getInt("next_conversation_event_id"));
                    }   
                    
                    // convert lists into arrays and pass to ConverstaionEvent object
                    event.setOptions(options.toArray(new String[1]), optionOutcome.stream().mapToInt(i->i).toArray());
                }
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /** 
     * Update a character's conversation
     */
    private void setConversation(int characterId, int conversationId)
    {
        DatabaseUtility db = new DatabaseUtility();

        if (conversationId == -1)
        {
            return;
        }
        if (conversationId == -2)
        {
            String query = "DELETE FROM character "
                        + " WHERE character_id = " + characterId;

            db.runUpdate(query);
        }
        else //(conversationId > 0)
        {
            String query = "UPDATE character "
                    + " SET conversation_id = " + conversationId
                    + " WHERE character_id = " + characterId;

            db.runUpdate(query);
        }
    }

    /** 
     * @return current text to be rendered
     */
    public String getText()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).text;
        }
        return null;
    }

    /**
     * @return the current text options or null
     */
    public String[] getOptions()
    {
        if (this.events.size() > 0 && this.events.get(0).options.length > 0)
        {
            return this.events.get(0).options;
        }
        return null;
    }

    /**
     * Process the player's selection
     * @param index the index of the option picked by the player
     */
    public void setOption(int index)
    {
        this.events.get(0).nextConversationEventId = this.events.get(0).optionOutcome[index];
        this.counter = 0;
        this.nextEvent();
    }

    /** 
     * Check if the previous event completed, and more on to the next if it did
     */
    public void nextEvent()
    {
        if (this.counter == 0 && this.events.size() > 0 && this.events.get(0).distance <= 0 && this.events.get(0).battleId == -1)
        {
            int conversationEventId = this.events.get(0).nextConversationEventId;

            // clear all events if you've reached the end
            if (conversationEventId == -1)
            {
                this.events.clear();
            }
            else
            {
                this.events.remove(0);
                this.counter = TEXT_LENGTH;

                // remove events until you reach the next step in your converstaion path
                while (this.events.size() > 0 && conversationEventId > this.events.get(0).conversationEventId)
                {
                    this.events.remove(0);
                }

                if (this.events.size() > 0 && this.events.get(0).newConversationId == -2)
                {
                    this.counter = 16;
                }
            }
        }
    }

    /** 
     * @return the battleId to be started or -1 if there is no battleId
     */
    public int getBattleId()
    {
        if (this.events.size() > 0 && !this.battleStarted)
        {
            return this.events.get(0).battleId;
        }
        return -1;
    }

    public void setBattleStarted()
    {
        this.battleStarted = true;
    }

    public void setBattleComplete()
    {
        this.events.get(0).battleId = -1;
        this.nextEvent();
    }

    /** 
     * Update the conversation
     */
    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }
        // auto advance through some events without waiting for player to press anything
        if (!this.isComplete() && this.counter == 0 && this.events.get(0).autoAdvance && this.events.get(0).options == null)
        {
            this.nextEvent();
        }
    }

    /** 
     * @return true if the conversation is over and can be ended
     */
    public boolean isComplete()
    {
        if (this.events.size() == 0)
        {
            return true;
        }
        return false;
    }

    /**
     * @return the characterId to be moved
     */
    public int getMovementCharacterId()
    {
        if (this.events.size() > 0 && this.events.get(0).distance > 0)
        {
            return this.events.get(0).characterId;
        }
        else
        {
            return -2;
        }
    }

    /**
     * @return the x-direction to move the character
     */
    public int getMovementDx()
    {
        if (this.events.size() > 0 && this.events.get(0).distance > 0)
        {
            return this.events.get(0).dx;
        }
        else
        {
            return 0;
        }
    }

    /**
     * @return the y-direction to move the character
     */
    public int getMovementDy()
    {
        if (this.events.size() > 0 && this.events.get(0).distance > 0)
        {
            return this.events.get(0).dy;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Confirm that a character has moved and decrease the remaining distance the need to move
     */
    public void setCharacterMoved()
    {
        this.events.get(0).distance--;
        this.counter = 16;
    }

    public boolean isHealTeam()
    {
        return !this.isComplete() && this.counter == 1 && this.events.get(0).healTeam;
    }

    /**
     * @return pokemonId of the Pokemon to be received or -1
     */
    public int getGiftPokemonId()
    {
        if (this.events.size() > 0 && this.counter == 1)
        {
            return this.events.get(0).giftPokemonId;
        }

        return -1;
    }

    /**
     * @return level of the Pokemon to be received or -1
     */
    public int getGiftPokemonLevel()
    {
        if (this.events.size() > 0 && this.counter == 1)
        {
            return this.events.get(0).giftPokemonLevel;
        }

        return -1;
    }

    public String getMugshotBackground()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).mugshotBackground;
        }
        return null;
    }

    public String getMugshotCharacter()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).mugshotCharacter;
        }
        return null;
    }

    public int getMusicId()
    {
        if (this.events.size() > 0 && this.events.get(0).musicId > 0)
        {
            return this.events.get(0).musicId;
        }

        return -1;
    }

    public int getShopId()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).shopId;
        }
        return -1;
    }

    /**
     * Remvoe characters and triggers, and update character's conversation
     * @param characters a list of all the characters on the current map
     * @param triggers a list of conversation triggers on the current map
     * @return true if a character is being removed during this event
     */
    public boolean removeCharacter(List<CharacterModel> characters, List<ConversationTriggerModel> triggers)
    {
        // if the next event is to update a character's conversation id, update it and remove the event
        while (this.events.size() > 1 && this.counter == 1 && this.events.get(1).newConversationId > 0)
        {
            // update the character's conversation in the database
            this.setConversation(this.events.get(1).characterId, this.events.get(1).newConversationId);
            this.removeTriggers(triggers, this.conversationId);

            for (int i = 0; i < characters.size(); i++)
            {
                if (characters.get(i).characterId == this.events.get(1).characterId)
                {
                    // update the character's conversation in the overworld
                    characters.get(i).conversationId = this.events.get(1).newConversationId;
                    this.events.remove(1);
                    break;
                }
            }
        }
        if (this.events.size() > 0 && this.events.get(0).newConversationId == -2)
        {
            if (this.counter == 8)
            {
                // update the character's conversation in the database
                this.removeTriggers(triggers, this.conversationId);
                this.setConversation(this.events.get(0).characterId, this.events.get(0).newConversationId);
            }
            for (int i = 0; i < characters.size(); i++)
            {
                if (characters.get(i).characterId == this.events.get(0).characterId)
                {
                    if (this.counter == 8)
                    {
                        // remove the character from the map
                        characters.remove(i);   
                    }
                                         
                    // tell the overworld model to do a fade to black transition
                    // only do this after confirming the character is on the current map
                    return true;                    
                }
            }
        }

        return false;
    }

    /**
     * Remove any triggers with clearConversationId equal to the given conversation Id
     * @param triggers a list o fconversation triggers on the current map
     * @param conversationId the conversation to filter out
     */
    private void removeTriggers(List<ConversationTriggerModel> triggers, int conversationId)
    {
        // remove triggers from the database 
        DatabaseUtility db = new DatabaseUtility();
        
        String query = "DELETE FROM conversation_trigger "
                    + "WHERE clear_conversation_id = " + conversationId;
    
        db.runUpdate(query);

        for (int i = 0; i < triggers.size(); i++)
        {
            if (triggers.get(i).clearConversationId == conversationId)
            {
                // remove the trigger
                triggers.remove(i);
            }
        }
    }

    public int getCounter()
    {
        return this.counter;
    }

    /**
     * @return true if the party screen needs to be opened for user to pick a Pokemon
     */
    public boolean openParty()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).openParty;
        }
        return false;
    }

    /**
     * Set which Pokemon the user selected
     * @return true if the Pokemon should be removed from player's party
     */
    public boolean setPokemon(PokemonModel pokemon)
    {
        return false;
    }

    /**
     * @return the index of the Pokemon being withrdrawn from the daycare
     */
    public int getWithdrawnPokemon()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).withdrawnPokemon;
        }
        return -1;
    }

    class ConversationEvent
    {
        public int conversationEventId;
        public final String text;
        public String[] options = new String[0];
        public int[] optionOutcome = new int[0];
        public int battleId = -1;
        public final boolean autoAdvance;
        public final boolean healTeam;
        public int nextConversationEventId;
        public final int newConversationId;
        public int giftPokemonId = -1;
        public int giftPokemonLevel = -1;
        private int optionId;
        public final String mugshotBackground;
        public final String mugshotCharacter;
        public int musicId;
        public int shopId;
        public boolean openParty = false;
        public int withdrawnPokemon = -1;

        // variables for moving CPUs
        public int characterId;
        public int dx = 0;
        public int dy = 0;
        public int distance;

        /** 
         * Constructor
         * @param conversationEventId the unique identifier of this part of the conversation
         * @param text the text that will be displayed
         * @param battleId unique identifer for the battle that will be started or -1 otherwise
         */
        public ConversationEvent(ResultSet rs) throws SQLException
        {
            if (rs.getString("text").equals(""))
            {
                this.text = null;
            }
            else
            {
                this.text = rs.getString("text");
            }
            this.conversationEventId = rs.getInt("conversation_event_id");
            this.battleId = rs.getInt("battle_id");
            this.healTeam = rs.getInt("heal_team") == 1;
            this.optionId = rs.getInt("option_id");
            this.nextConversationEventId = rs.getInt("next_conversation_event_id");
            this.characterId = rs.getInt("character_id");
            this.newConversationId = rs.getInt("new_conversation_id");

            switch (rs.getInt("movement_direction"))
            {
                case 0:
                    dy = -1;
                    break;
                case 1:
                    dx = 1;
                    break;
                case 2:
                    dy = 1;
                    break;
                case 3:
                    dx = -1;
                    break;
            }
            if (this.dx + this.dy != 0)
            {
                this.distance = 1;
            }
            
            if (this.distance > 0 || this.newConversationId != -1)
            {
                this.autoAdvance = true;
            }
            else
            {
                this.autoAdvance = false;
            }

            this.giftPokemonId = rs.getInt("gift_pokemon_id");
            this.giftPokemonLevel = rs.getInt("gift_pokemon_level");
            
            if (rs.getString("mugshot_character") != null && !rs.getString("mugshot_character").equals(""))
            {
                this.mugshotBackground = rs.getString("mugshot_background");
                this.mugshotCharacter = rs.getString("mugshot_character");
            }
            else
            {
                this.mugshotBackground = null;
                this.mugshotCharacter = null;
            }

            this.musicId = rs.getInt("music_id");
            this.shopId = rs.getInt("shop_id");
        }

        /**
         * Constructor
         * @param characterId the character to be moved
         * @param dx the x-direction to move the character
         * @param dy the y-direction to move the character
         * @param distance the distance to move the character
         */
        public ConversationEvent(int conversationEventId, int characterId, int dx, int dy, int distance, int nextConversationEventId)
        {
            this.conversationEventId = conversationEventId;
            this.characterId = characterId;
            this.dx = dx;
            this.dy = dy;
            this.distance = distance;
            this.autoAdvance = true;
            this.nextConversationEventId = nextConversationEventId;
            this.healTeam = false;
            this.mugshotBackground = null;
            this.mugshotCharacter = null;
            this.text = null;
            this.newConversationId = -1;
        }

        public ConversationEvent(int conversationEventId, int nextConversationEventId, String text, int withdrawnPokemon)
        {
            this.conversationEventId = conversationEventId;
            this.text = text;
            this.nextConversationEventId = nextConversationEventId;
            this.withdrawnPokemon = withdrawnPokemon;

            this.autoAdvance = false;
            this.healTeam = false;
            this.mugshotBackground = null;
            this.mugshotCharacter = null;
            this.newConversationId = -1;
            this.characterId = -1;
        }

        public void setOptions(String[] options, int[] optionOutcome)
        {
            this.options = options;
            this.optionOutcome = optionOutcome;
        }
    }
}

package pokemonoceanblue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationModel 
{
    private final int TEXT_LENGTH = 15;
    private final int conversationId;
    private int counter = 0;
    private List<ConversationEvent> events = new ArrayList<ConversationEvent>();
    private CharacterModel player;
    private CharacterModel cpu;
    private boolean initialMovement;
    private boolean battleStarted = false;
    
    /** 
     * Constructor
     * @param conversationId the unique identifier of this converstaion
     */
    public ConversationModel(int conversationId, CharacterModel player, CharacterModel cpu, boolean initialMovement)
    {
        this.conversationId = conversationId;
        this.player = player;
        this.cpu = cpu;
        this.counter = TEXT_LENGTH;
        this.initialMovement = initialMovement;

        // move trainer to approach the player at the start of the conversation
        if (this.initialMovement)
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

            String query = "SELECT conversation_event_id, text, battle_id, heal_team, "
                        + " movement_direction, character_id, "
                        + " option_id, next_conversation_event_id, "
                        + " gift_pokemon_id, gift_pokemon_level"
                        + " FROM conversation WHERE conversation_id = " + this.conversationId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                ConversationEvent event;

                // events where a person moves
                if (rs.getInt("movement_direction") > -1)
                {
                    int dx = 0;
                    int dy = 0;
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

                    event = new ConversationEvent(
                        rs.getInt("conversation_event_id"),
                        rs.getInt("character_id"),
                        dx,
                        dy,
                        1,
                        rs.getInt("next_conversation_event_id")
                    );
                }
                else
                {
                    // add the conversation event to the list of events
                    event = new ConversationEvent(
                        rs.getInt("conversation_event_id"),
                        rs.getString("text"),
                        rs.getInt("battle_id"),
                        rs.getInt("heal_team") == 1,
                        rs.getInt("option_id"),
                        rs.getInt("next_conversation_event_id")
                    );

                    event.setGiftPokemon(rs.getInt("gift_pokemon_id"), rs.getInt("gift_pokemon_level"));
                    
                }

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
            }
        }
    }

    /** 
     * @return the battleId to be started or -1 if there is no battleId
     */
    public int getBattleId()
    {
        if (this.events.size() > 0 && this.counter == 0 && !this.battleStarted)
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
        if (!this.isComplete() && this.counter == 0 && this.events.get(0).autoAdvance)
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

    class ConversationEvent
    {
        public int conversationEventId;
        public String text;
        public String[] options = new String[0];
        public int[] optionOutcome = new int[0];
        public int battleId = -1;
        public boolean autoAdvance = false;
        public boolean healTeam = false;
        public int nextConversationEventId = -1;
        public int giftPokemonId = -1;
        public int giftPokemonLevel = -1;
        private int optionId = -1;

        // variables for moving CPUs
        public int characterId;
        public int dx;
        public int dy;
        public int distance;

        /** 
         * Constructor
         * @param conversationEventId the unique identifier of this part of the conversation
         * @param text the text that will be displayed
         * @param battleId unique identifer for the battle that will be started or -1 otherwise
         */
        public ConversationEvent(int conversationEventId, String text, int battleId, boolean healTeam, int optionId, int nextConversationEventId)
        {
            this.conversationEventId = conversationEventId;
            this.text = text;
            this.battleId = battleId;
            this.healTeam = healTeam;
            this.optionId = optionId;
            this.nextConversationEventId = nextConversationEventId;
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
        }

        public void setGiftPokemon(int pokemonId, int level)
        {
            this.giftPokemonId = pokemonId;
            this.giftPokemonLevel = level;
        }

        public void setOptions(String[] options, int[] optionOutcome)
        {
            this.options = options;
            this.optionOutcome = optionOutcome;
        }
    }
}

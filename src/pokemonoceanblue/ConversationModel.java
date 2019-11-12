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
    
    /** 
     * Constructor
     * @param conversationId the unique identifier of this converstaion
     */
    public ConversationModel(int conversationId)
    {
        this.conversationId = conversationId;
        this.counter = TEXT_LENGTH;

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

            String query = "SELECT conversationEventId, text, battleId "
                        + " FROM conversation WHERE conversationId = " + this.conversationId;

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                // add the conversation event to the list of events
                ConversationEvent event = new ConversationEvent(rs.getInt("conversationEventId"),
                                                                rs.getString("text"),
                                                                rs.getInt("battleId"));
                events.add(event);
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
     * Check if the previous event completed, and more on to the next if it did
     */
    public void nextEvent()
    {
        if (this.counter == 0 && this.events.size() > 0)
        {
            this.events.remove(0);
            this.counter = TEXT_LENGTH;
        }
    }

    /** 
     * @return the battleId to be started or -1 if there is no battleId
     */
    public int getBattleId()
    {
        if (this.events.size() > 0)
        {
            return this.events.get(0).battleId;
        }
        return -1;
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
    }

    /** 
     * @return true if the conversation is over and can be ended
     */
    public boolean isComplete()
    {
        if (this.events.size() == 0 || (this.events.size() == 1 && this.counter == 0))
        {
            return true;
        }
        return false;
    }

    class ConversationEvent
    {
        private final int conversationEventId;
        public final String text;
        public final int battleId;

        /** 
         * Constructor
         * @param conversationEventId the unique identifier of this part of the conversation
         * @param text the text that will be displayed
         * @param battleId unique identifer for the battle that will be started or -1 otherwise
         */
        public ConversationEvent(int conversationEventId, String text, int battleId)
        {
            this.conversationEventId = conversationEventId;
            this.text = text;
            this.battleId = battleId;
        }
    }
}

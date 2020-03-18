package pokemonoceanblue;

/**
 * Holds a specific location on the map that starts a conversation when the player steps on it
 */
public class ConversationTriggerModel
{
    public final int conversationId;
    public final CharacterModel cpuModel; 
    public final int x; 
    public final int y;
    public final int clearConversationId;
    public final boolean autoTrigger;
    public final boolean approachPlayer;

    /**
     * Constructor
     * @param conversationId Unique identifier for the conversation. For player movements at the start of the conversation, needs a different ID from the conversation.
     * @param cpuModel the cpu involved in the conversation
     * @param x the x-coordinate of the trigger
     * @param y the y-coordinate of the trigger
     * @param clearConversationId which conversationId is reponsible for clearing this trigger
     * @param autoTrigger true if the trigger starts based on movement, false if the player must press enter to interact
     * @param approachPlayer true if the CPU should work towards the player if there is a gap
     */
    public ConversationTriggerModel(int conversationId, CharacterModel cpuModel, int x, int y, int clearConversationId, boolean autoTrigger, boolean appraochPlayer)
    {
        this.conversationId = conversationId;
        this.cpuModel = cpuModel;
        this.x = x;
        this.y = y;
        this.clearConversationId = clearConversationId;
        this.autoTrigger = autoTrigger;
        this.approachPlayer = appraochPlayer;
    }
}
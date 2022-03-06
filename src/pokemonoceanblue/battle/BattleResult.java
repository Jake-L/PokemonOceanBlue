package pokemonoceanblue.battle;

import pokemonoceanblue.ItemModel;

public class BattleResult {
    public ItemModel reward;
    public int badgeIndex = -1;

    /**
     * Generate the text displayed in battles saying what the player recieved for their victory
     * @return the text to be displayed in battle
     */
    public String getRewardText()
    {
        if (this.reward == null)
        {
            return null;
        }
        else if (this.reward.itemId == 1000)
        {
            return "Player received $" + this.reward.quantity + " for their victory";
        }
        else
        {
            return "Player received " + this.reward.quantity + " " + this.reward.name + " for their victory";
        }
    }

    /**
     * Determines if the player will recieve a badge for winning this battle
     * @param battleId the current battle
     */
    public void setBadgeIndex(int battleId)
    {
        switch (battleId)
        {
            case 10:
                this.badgeIndex = 0;
                break;
            case 12:
                this.badgeIndex = 1;
                break;
            case 14:
                this.badgeIndex = 2;
                break;
            case 15:
                this.badgeIndex = 3;
                break;
            case 16:
                this.badgeIndex = 4;
                break;
            case 17:
                this.badgeIndex = 5;
                break;
            case 18:
                this.badgeIndex = 6;
                break;
            case 19:
                this.badgeIndex = 7;
                break;
        }
    }
}

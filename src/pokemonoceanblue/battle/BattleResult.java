package pokemonoceanblue.battle;

import java.util.ArrayList;
import java.util.List;

import pokemonoceanblue.ItemModel;

public class BattleResult {
    public ItemModel reward;
    public int badgeIndex = -1;
    public List<Integer> defeatedPokemon = new ArrayList<Integer>();
    public List<int[]> attackUsed = new ArrayList<int[]>();

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

    /**
     * Indicate that the player has defeated the specified Pokemon
     * used for tracking progress on some tasks
     * @param pokemon_id
     */
    public void setDefeatedPokemon(int pokemon_id)
    {
        this.defeatedPokemon.add(pokemon_id);
    }

    /**
     * Indicates that the player has witnessed a Pokemon use a specific move
     * used for tracking progress on some tasks
     * @param pokemon_id
     * @param move_id
     */
    public void setAttackUsed(int pokemon_id, int move_id)
    {
        int[] data = new int[2];
        data[0] = pokemon_id;
        data[1] = move_id;
        this.attackUsed.add(data);
    }
}

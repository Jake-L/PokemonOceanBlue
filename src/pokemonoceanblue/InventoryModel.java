package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

/** 
 * Hold's a users items and handles adding or removing items from inventory
 */
public class InventoryModel extends BaseModel {

    public List<ItemModel>[] items; 
    public int bagIndex;
    private int money = 5001;

    public static final int POKEBALLS = 0;
    public static final int BERRIES = 1;
    public static final int KEY_ITEMS = 2;
    
    /** 
     * Constructor
     */
    @SuppressWarnings("unchecked")
    public InventoryModel()
    {
        this.items = new ArrayList[3];
        for (int i = 0; i < items.length; i++)
        {
            items[i] = new ArrayList<ItemModel>();
        }
        this.addItem(1, 2);
        this.addItem(3, 5);
        this.addItem(14, 2);
        this.addItem(12, 5);
        this.addItem(113, 5);
        this.addItem(114, 5);
        this.addItem(116, 8);
        this.addItem(118, 7);
        this.addItem(119, 6);
        this.addItem(120, 4);
        this.addItem(125, 3);
        this.addItem(127, 1);
        this.addItem(130, 9);
        this.addItem(131, 8);
        this.addItem(132, 9);
        this.addItem(133, 8);
        this.addItem(113, 8);
        this.addItem(116, 8);
        // add and enable cleanse tag
        ItemModel item = new ItemModel(188, 1);
        item.enabled = true;
        this.addItem(item);
        
        this.initialize();
    }

    /**
     * Set variables when creating a new view
     */
    @Override
    public void initialize()
    {
        this.bagIndex = 0;
        super.initialize();

        if (this.items != null)
        {
            this.optionMax = this.items[this.bagIndex].size() - 1;
            this.optionHeight = this.optionMax;
        }
    }

    /**
     * Add an item to your iventory
     * @param itemId unique identifier of the item
     * @param quantity the quantity to be added
     */
    public void addItem(int itemId, int quantity)
    {
        this.addItem(new ItemModel(itemId, quantity));
    }

    /**
     * Add an item to your iventory
     * @param newItem the item to be added
     */
    public void addItem(ItemModel newItem)
    {
        if (newItem == null)
        {
            return;
        }

        if (newItem.itemId == 1000)
        {
            this.addMoney(newItem.quantity);
            return;
        }

        int categoryIndex = Math.min(newItem.categoryId, 2);
        
        for (ItemModel item : this.items[categoryIndex])
        {
            if (item.itemId == newItem.itemId)
            {
                // increment the quantity of the item in your inventory
                item.quantity += newItem.quantity;    
                return;            
            }
        }

        // no match was found. so insert the item
        this.items[categoryIndex].add(newItem);
    }

    /**
     * Remove an item from your inventory
     * @param itemId unique identifier of the item
     * @param quantity the item to be removed
     * @return true if the item has sufficient quantity to remove
     */
    public boolean removeItem(int itemId, int quantity)
    {
        ItemModel newItem = new ItemModel(itemId, quantity);
        int categoryIndex = Math.min(newItem.categoryId, 2);

        for (int i = 0; i < this.items[categoryIndex].size(); i++)
        {
            ItemModel item = this.items[categoryIndex].get(i);

            // get the specified item
            if (item.itemId == newItem.itemId)
            {
                // reduce the quantity owned if you have enough
                if (item.quantity > newItem.quantity)
                {
                    item.quantity -= newItem.quantity;
                    return true;
                }
                // if the quantity removed is equal to the quantity owned,
                // remove the item from the inventory
                else if (item.quantity == quantity)
                {
                    this.items[categoryIndex].remove(i);
                    return true;
                }
                
                // insufficient quantity of the item
                return false;
            }
        }

        // item not contained in inventory
        return false;
    }

    public void addMoney(int quantity)
    {
        this.money += quantity;
    }

    /**
     * Attempts to spend money if the player has enough
     * @param item the item the player wishes to buy
     * @return true if the player has enough money or false otherwise
     */
    public boolean buyItem(ItemModel item)
    {
        if (this.money < item.quantity * item.cost)
        {
            return false;
        }
        else
        {
            this.addItem(item.itemId, item.quantity);
            this.money -= item.quantity * item.cost;
            return true;
        }
    }

    public int getMoney()
    {
        return this.money;
    }

    /**
     * @param itemId the item to search for
     * @return the quantity of the given item in the player's inventory
     */
    public int getQuantity(int itemId)
    {
        for (int i = 0; i < this.items.length; i++)
        {
            for (int j = 0; j < this.items[i].size(); j++)
            {
                if (this.items[i].get(j).itemId == itemId)
                {
                    return this.items[i].get(j).quantity;
                }
            }
        }

        return 0;
    }

    /**
     * Move the cursor around within the inventory screen
     * @param dx the difference between the current index and the index to move to
     * @param dy the direction to move up or down within the items/text options
     */
    @Override
    public void moveIndex(int dx, int dy)
    {
        if (this.textOptions == null && dx != 0)
        {
            this.bagIndex = (dx < 0 ? this.bagIndex + 3 + dx : this.bagIndex + dx) % 3;
            this.optionIndex = 0;
            this.optionMax = this.items[this.bagIndex].size() - 1;
            this.optionHeight = this.optionMax;
        }
        else if (dy != 0)
        {
            super.moveIndex(0, dy);
        }
    }

    /**
     * Selects the item that the player is hovering
     */
    @Override
    public void confirmSelection()
    {
        this.returnValue = this.items[this.bagIndex].get(this.optionIndex).itemId;
    }
}
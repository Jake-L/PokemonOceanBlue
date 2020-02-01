package pokemonoceanblue;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

/** 
 * Hold's a users items and handles adding or removing items from inventory
 */
public class InventoryModel {

    public List<ItemModel>[] items; 
    public int itemIndex;
    public int bagIndex;
    public int counter;
    public int returnValue;
    
    /** 
     * Constructor
     */
    public InventoryModel()
    {
        this.items = new ArrayList[3];
        for (int i = 0; i < items.length; i++)
        {
            items[i] = new ArrayList<ItemModel>();
        }
        this.addItem(1, 2);
        this.addItem(3, 5);
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

        this.initialize();
    }

    /**
     * Set variables when creating a new view
     */
    public void initialize()
    {
        this.itemIndex = 0;
        this.bagIndex = 0;
        this.counter = 5;
        this.returnValue = -2;
    }

    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }
    }

    /**
     * Add an item to your iventory
     * @param itemId unique identifier of the item
     * @param quantity the quantity to be added
     */
    public void addItem(int itemId, int quantity)
    {
        ItemModel newItem = new ItemModel(itemId, quantity);
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

    public void moveCursor(int dy)
    {
        if (dy > 0 && this.itemIndex < this.items[this.bagIndex].size() - 1)
        {
            this.itemIndex++;
            this.counter = 5;
        }
        else if (dy < 0 && this.itemIndex > 0)
        {
            this.itemIndex--;
            this.counter = 5;
        }
    }

    /**
     * Switch to a different category of items
     * @param dx the difference between the current index and the index to move to
     */
    public void movePocket(int dx)
    {
        this.bagIndex = (this.bagIndex + dx) % 3;
        this.counter = 5;
        this.itemIndex = 0;
    }

    /**
     * Returns information about what the player has selected
     * @return -2 means the player hasn't made a selection yet
     * @return -1 means they exited the screen without making a selection
     * @return 0 or greater gives the itemId of what they selected
     */
    public int getSelection()
    {
        return this.returnValue;
    }

    /**
     * Selects the item that the player is hovering
     */
    public void confirmSelection()
    {
        this.returnValue = this.items[this.bagIndex].get(this.itemIndex).itemId;
        this.removeItem(this.returnValue, 1);
    }

    class ItemModel
    {
        public int itemId;
        public int quantity;
        public String name;
        public int categoryId;
        public int cost;
        public String description;

        public ItemModel(int itemId, int quantity)
        {
            this.itemId = itemId;
            this.quantity = quantity;
            this.loadData();
        }

        /** 
         * Read all the information about the item, such as name and category
         */
        private void loadData()
        {
            try
            {
                DatabaseUtility db = new DatabaseUtility();

                String query = "SELECT * FROM items WHERE item_id = " + this.itemId;

                ResultSet rs = db.runQuery(query);

                this.itemId = rs.getInt("item_id");
                this.name = rs.getString("name");
                this.categoryId = rs.getInt("category_id");
                this.cost = rs.getInt("cost");
                this.description = rs.getString("description");
            }
            catch (SQLException e) 
            {
                e.printStackTrace();
            }  
        }
    }

}
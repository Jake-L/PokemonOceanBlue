package pokemonoceanblue;

import java.sql.*;


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
package pokemonoceanblue;

import java.sql.*;

public class MoveModel 
{
    private int moveId;
    public String name;
    public int typeId;
    public int power;
    public int accuracy;
    public int priority;
    public int damageClassId;
    
    /** 
     * Constructor
     * @param id the Pokemon's number from the national pokedex
     * @param level the Pokemon's current level
     */
    public MoveModel(int moveId)
    {
        this.moveId = moveId;

        this.loadData();
    }

    /** 
     * Read all the information about a move, such as power, type, and accuracy
     */
    private void loadData()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM moves WHERE move_id = " + this.moveId;

            ResultSet rs = db.runQuery(query);

            this.name = rs.getString("name").toUpperCase();
            this.typeId = rs.getInt("type_id");
            this.power = rs.getInt("power");
            this.accuracy = rs.getInt("accuracy");
            this.priority = rs.getInt("priority");
            this.damageClassId = rs.getInt("damage_class_id");   
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}
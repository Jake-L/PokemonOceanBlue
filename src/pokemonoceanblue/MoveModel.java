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
    public int targetId;
    public int flinchChance;
    public int effectChance;
    public int ailmentId;
    public int recoil;
    public MoveStatEffect[] moveStatEffects;
    
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

            // load move data
            String query = "SELECT * FROM moves WHERE move_id = " + this.moveId;

            ResultSet rs = db.runQuery(query);

            this.name = rs.getString("name").toUpperCase();
            this.typeId = rs.getInt("type_id");
            this.power = rs.getInt("power");
            this.accuracy = rs.getInt("accuracy");
            this.priority = rs.getInt("priority");
            this.damageClassId = rs.getInt("damage_class_id");   
            this.targetId = rs.getInt("target_id");
            this.flinchChance = rs.getInt("flinch_chance");
            this.effectChance = rs.getInt("effect_chance");
            this.ailmentId = rs.getInt("ailment_id");
            this.recoil = rs.getInt("recoil");

            // get number of stat effects
            query = "SELECT COUNT(*) FROM move_stat_effect WHERE move_id = " + this.moveId;
            rs = db.runQuery(query);
            moveStatEffects = new MoveStatEffect[rs.getInt(1)];

            // get stat effects
            query = "SELECT stat_id, stat_change FROM move_stat_effect WHERE move_id = " + this.moveId;
            rs = db.runQuery(query);
            int index = 0;

            while(rs.next()) 
            {
                moveStatEffects[index] = new MoveStatEffect(rs.getInt(1), rs.getInt(2));
                index++;
            }      
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * Store stat change effects
     */
    class MoveStatEffect
    {
        public int statId;
        public int statChange;

        /** 
         * Constructor
         * @param statId the stat that is affected
         * @param statChange the amount to modify the stat
         */
        public MoveStatEffect(int statId, int statChange)
        {
            this.statId = statId;
            this.statChange = statChange;
        }
    }
}
package pokemonoceanblue;

import java.sql.*;

public class MoveModel 
{
    public int moveId;
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
    public MoveEffect moveEffect;
    
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
            int effectId = rs.getInt("effect_id");

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
            
            // get move effect
            if (effectId >= 0)
            {
                query = "SELECT * FROM move_effect WHERE effect_id = " + effectId;
                rs = db.runQuery(query);
                this.moveEffect = new MoveEffect(
                    effectId, 
                    rs.getInt("target_type"), 
                    rs.getInt("counter_min"),
                    rs.getInt("counter_max"), 
                    rs.getString("text")
                );
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

    class MoveEffect
    {
        public final int effectId;
        public int targetId;
        public int minCounter;
        public int maxCounter;
        public int counter;
        public String text; 

        public MoveEffect(int effectId, int targetId, int minCounter, int maxCounter, String text)
        {
            this.effectId = effectId;
            this.targetId = targetId;
            this.minCounter = minCounter;
            this.maxCounter = maxCounter;
            this.text = text;
        }
    }
}
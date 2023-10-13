package pokemonoceanblue;

import java.sql.*;

import pokemonoceanblue.StatEffect;

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
    public byte ailmentId;
    public int recoil;
    public StatEffect[] moveStatEffects;
    public MoveEffectModel moveEffect;
    
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
            this.ailmentId = (byte)rs.getInt("ailment_id");
            this.recoil = rs.getInt("recoil");
            int effectId = rs.getInt("effect_id");

            // get number of stat effects
            query = "SELECT COUNT(*) FROM move_stat_effect WHERE move_id = " + this.moveId;
            rs = db.runQuery(query);
            moveStatEffects = new StatEffect[rs.getInt(1)];

            // get stat effects
            query = "SELECT stat_id, stat_change FROM move_stat_effect WHERE move_id = " + this.moveId;
            rs = db.runQuery(query);
            int index = 0;

            while(rs.next()) 
            {
                moveStatEffects[index] = new StatEffect(rs.getInt(1), rs.getInt(2));
                index++;
            }     
            
            // get move effect
            if (effectId >= 0)
            {
                this.moveEffect = new MoveEffectModel(effectId);
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}
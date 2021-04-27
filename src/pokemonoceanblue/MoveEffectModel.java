package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MoveEffectModel {
    public final int effectId;
    public int targetId;
    public int minCounter;
    public int maxCounter;
    public int removalCondition;
    public String text;

    public MoveEffectModel(int effectId)
    {
        this.effectId = effectId;

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM move_effect WHERE effect_id = " + effectId;
            ResultSet rs = db.runQuery(query);

            this.targetId = rs.getInt("target_type");
            this.removalCondition = rs.getInt("removal_condition");
            this.minCounter = rs.getInt("counter_min");
            this.maxCounter = rs.getInt("counter_max");
            this.text = rs.getString("text");
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        } 
    }
}

package pokemonoceanblue;

import java.sql.*;

public class AbilityModel 
{
    public int abilityId;
    public int effectChance;
    public String name;
    public String description;
    public String battleText;
    
    /** 
     * Constructor
     * @param abilityId unique identifier for the ability
     */
    public AbilityModel(int abilityId)
    {
        this.abilityId = abilityId;

        this.loadData();
    }

    /** 
     * Read all the information about an ability, such as name and description
     */
    private void loadData()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            // load move data
            String query = "SELECT * FROM ability WHERE ability_id = " + this.abilityId;

            ResultSet rs = db.runQuery(query);

            this.name = rs.getString("name").toUpperCase();
            this.description = rs.getString("description");
            int effectId = rs.getInt("effect_id");
            this.effectChance = rs.getInt("effect_chance");
            this.battleText = rs.getString("battle_text");

            // // get number of stat effects
            // query = "SELECT COUNT(*) FROM move_stat_effect WHERE move_id = " + this.moveId;
            // rs = db.runQuery(query);
            // moveStatEffects = new StatEffect[rs.getInt(1)];

            // // get stat effects
            // query = "SELECT stat_id, stat_change FROM move_stat_effect WHERE move_id = " + this.moveId;
            // rs = db.runQuery(query);
            // int index = 0;

            // while(rs.next()) 
            // {
            //     moveStatEffects[index] = new StatEffect(rs.getInt(1), rs.getInt(2));
            //     index++;
            // }     
            
            // // get move effect
            // if (effectId >= 0)
            // {
            //     this.moveEffect = new MoveEffectModel(effectId);
            // }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
}
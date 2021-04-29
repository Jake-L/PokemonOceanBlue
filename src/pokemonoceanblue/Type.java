package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Type {
    public static int UNKNOWN = 0;
    public static int NORMAL = 1;
    public static int FIGHTING = 2;
    public static int FLYING = 3;
    public static int POISON = 4;
    public static int GROUND = 5;
    public static int ROCK = 6;
    public static int BUG = 7;
    public static int GHOST = 8;
    public static int STEEL = 9;
    public static int FIRE = 10;
    public static int WATER = 11;
    public static int GRASS = 12;
    public static int ELECTRIC = 13;
    public static int PSYCHIC = 14;
    public static int ICE = 15;
    public static int DRAGON = 16;
    public static int DARK = 17;
    public static int FAIRY = 18;
    public static float[][] typeEffectiveness = loadTypeChart();

    /** 
    * Read data on type effectiveness and load it into an array
    */ 
    private static float[][] loadTypeChart()
    {
        System.out.println("loading type chart");
        float[][] typeChart = new float[19][19];
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT src_type_id, target_type_id, damage_factor "
                         + "FROM type_effectiveness";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                typeChart[rs.getInt(1)][rs.getInt(2)] = rs.getFloat(3);
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return typeChart;
    }
}
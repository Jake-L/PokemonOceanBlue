package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class MoveAnalysis {
    int[] movePriority = new int[999];
    List<MoveAnalysisData> pokemonMoveset;

    public MoveAnalysis() 
    {
        this.calcMovePriority();
        // for (int i = 1; i < 10; i++)
        // {
        //     this.calcMoveset(i);
        // }
        this.calcMoveset(6);
    }

    /**
     * Main function to determine a Pokemon's moveset
     * @param pokemonId
     */
    private void calcMoveset(int pokemonId)
    {
        pokemonMoveset = new ArrayList<MoveAnalysisData>();
        System.out.println(pokemonId);
        this.loadMovesUSUM(pokemonId);
        this.loadEggMovesUSUM(pokemonId);
        this.addExclusiveMoves(pokemonId);
        this.addEggMoves();
        this.updateMoveLevels();
        System.out.println("***********************");
    }

    /**
     * Find the average level each move gets learned
     */
    private void calcMovePriority()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
            SELECT m.move_id, IFNULL(NULLIF(AVG(pm.level), 0), 70) AS avg_level
            FROM moves m
            LEFT JOIN pokemon_moves pm
            ON pm.move_id = m.move_id
            WHERE m.move_id < 1000
            GROUP BY m.move_id
            ORDER BY avg_level;
            """;

            ResultSet rs = db.runQuery(query);
            while (rs.next())
            {
                int moveId = rs.getInt("move_id");
                int avgLevel = rs.getInt("avg_level");
                this.movePriority[moveId] = avgLevel;
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    } 

    /**
     * Load all a Pokemon's moves using the Ultra Sun and Ultra Moon movesets
     * @param pokemonId
     */
    private void loadMovesUSUM(int pokemonId)
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
            SELECT pm.move_id, pm.pokemon_move_method_id, pm.level, m.type_id
            FROM pokemon_moves_raw pm
            INNER JOIN moves m
            ON pm.move_id = m.move_id
            WHERE pm.version_group_id = 18
            AND pm.pokemon_id = 
            """ + pokemonId + """
             ORDER BY pm.level ASC
            """;

            ResultSet rs = db.runQuery(query);
            while (rs.next())
            {
                this.pokemonMoveset.add(new MoveAnalysisData(
                    rs.getInt("move_id"),
                    rs.getInt("pokemon_move_method_id"),
                    rs.getInt("level")
                ));
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * The moveset data only stores egg moves for first evolutions
     * so copy the egg moves for later evolutions
     * @param pokemonId
     */
    private void loadEggMovesUSUM(int pokemonId)
    {
        int firstEvolutionId = Utils.getFirstEvolution(pokemonId);
        if (pokemonId == firstEvolutionId)
        {
            return;
        }

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = """
            SELECT pm.move_id, pm.pokemon_move_method_id, pm.level, m.type_id
            FROM pokemon_moves_raw pm
            INNER JOIN moves m
            ON pm.move_id = m.move_id
            WHERE pm.version_group_id = 18
            AND pm.pokemon_move_method_id = 2
            AND pm.pokemon_id = 
            """ + firstEvolutionId + """
             ORDER BY pm.level ASC
            """;

            ResultSet rs = db.runQuery(query);
            while (rs.next())
            {
                this.pokemonMoveset.add(new MoveAnalysisData(
                    rs.getInt("move_id"),
                    rs.getInt("pokemon_move_method_id"),
                    rs.getInt("level")
                ));
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }
    
    // consider adding this if we add many new Pokemon from Gen 8
    // private void addMovesSWSH(int pokemonId)
    // {

    // } 

    /**
     * Move of a few of a Pokemon's egg moves into their movesets
     * TODO: improve logic for determining which egg moves to learn
     */
    private void addEggMoves()
    {
        List<MoveAnalysisData> eggMoves = new ArrayList<MoveAnalysisData>();

        // find all the egg and move tutor moves
        for (MoveAnalysisData move : this.pokemonMoveset)
        {
            if (move.pokemonMoveMethodId == 2)
            {
                // insert in sorted order, with the moves at the start of the list being the best
                int insertIndex = 0;
                while (insertIndex < eggMoves.size() 
                    && movePriority[move.moveId] <= movePriority[eggMoves.get(insertIndex).moveId])
                {
                    // don't insert duplicate moves if they can be learned by egg move and move tutor
                    if (move.moveId == eggMoves.get(insertIndex).moveId)
                    {
                        insertIndex = -1;
                        break;
                    }
                    insertIndex++;
                }

                if (insertIndex > -1)
                {
                    eggMoves.add(insertIndex, move);
                }
            }
        }

        // determine how many moves to add into their moveset
        int eggMovesToLearn = (int)Math.ceil(eggMoves.size() / 3);

        // add some of the best egg moves into their moveset
        int insertIndex = 0;
        for (int i = eggMovesToLearn; i >= 0; i--)
        {
            // find the right spot in their moveset to add the egg move
            while (insertIndex < this.pokemonMoveset.size() 
                && movePriority[eggMoves.get(i).moveId] >= movePriority[this.pokemonMoveset.get(insertIndex).moveId])
            {
                insertIndex++;
            }

            // add the egg move with a temporary level that will be updated later
            this.pokemonMoveset.add(insertIndex, new MoveAnalysisData(
                eggMoves.get(i).moveId, 
                1,
                movePriority[eggMoves.get(i).moveId]
            ));
            System.out.println("Teaching " + eggMoves.get(i).moveId);
        }
    }

    /**
     * Add moves designated exclusive moves, such as Psystrike for Mewtwo
     * and Hydro Cannon for water starters
     * @param pokemonId
     */
    private void addExclusiveMoves(Integer pokemonId)
    {
        Hashtable<Integer, Integer[]> exclusiveMoves = Utils.getExclusiveMoves();

        // iterate all of the exclusive moves
        Enumeration<Integer> enu = exclusiveMoves.keys();
        while (enu.hasMoreElements()) 
        {    
            int moveId = enu.nextElement();
            Integer pokemonIdList[] = exclusiveMoves.get(moveId);
            if (Arrays.binarySearch(pokemonIdList, pokemonId) >= 0)
            {
                // check if the pokemon already knows the exclusive move
                boolean move_learned = false;
                for (MoveAnalysisData move : this.pokemonMoveset)
                {
                    if (move.moveId == moveId && move.pokemonMoveMethodId == 1)
                    {
                        move_learned = true;
                    }
                }

                // if they don't learn the exclusive move by leveling up, then add it to their moveset
                if (move_learned == false)
                {
                    this.pokemonMoveset.add(new MoveAnalysisData(moveId, 1, 80));
                }
            }
        }
    }

    /**
     * Update the levels a Pokemon learns moves to make sure they are still spaced out properly
     * TODO: implement updateMoveLevels()
     */
    private void updateMoveLevels()
    {

    }

    /**
     * Insert the Pokemon's moveset into the moves table
     * TODO: implement saveMoveset()
     */
    private void saveMoveset()
    {

    }
}

class MoveAnalysisData 
{
    int moveId;
    int pokemonMoveMethodId;
    int level;

    public MoveAnalysisData(int moveId, int pokemonMoveMethodId, int level) 
    {
        this.moveId = moveId;
        this.pokemonMoveMethodId = pokemonMoveMethodId;
        this.level = level;
    }
}

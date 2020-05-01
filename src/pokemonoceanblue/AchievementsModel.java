package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AchievementsModel {

    List<AchievementDataModel> achievements = new ArrayList<AchievementDataModel>();
    
    public AchievementsModel()
    {
        loadEvents();
    }

    /**
     * increase counters for catching pokemon achievements and if the pokemon is shiny, complete obtain shiny achievement
     * @param isShiny is whether or not the caught pokemon is shiny
     */
    public void setPokemonCaught(boolean isShiny)
    {
        incrementCounter(0, 5, 1);

        if (isShiny)
        {
            incrementCounter(25, 0, 1);
        }
    }

    /**
     * increase evolve pokemon achievement counters and if pokemon was evolved with stone,
     * complete evolve with stone achievement
     * @param evolvedWithStone
     */
    public void setPokemonEvolved(boolean evolvedWithStone)
    {
        incrementCounter(19, 2, 1);

        if (evolvedWithStone)
        {
            incrementCounter(22, 0, 1);
        }
    }

    /**
     * called when a new pokedex entry is made, checks which achievements should progress
     * @param pokemonId is the id of the newly obtained pokemon
     */
    public void setNewPokemonObtained(int pokemonId)
    {
        //checks if pokemon is from kanto, johto, hoenn, or sinnoh national pokedex, excludes later gen and alt forms
        if (pokemonId < 494)
        {
            //increase counter for completing all four national dex
            incrementCounter(30, 0, 1);
            
            //check if pokemon is from kanto, increase kanto dex counter then check if pokemon is legendary
            //if legendary, complete achievement for catching that legendary
            if (pokemonId < 152)
            {
                incrementCounter(26, 0, 1);

                if (pokemonId > 143 && pokemonId < 147)
                {
                    incrementCounter(6, 0, 1);
                }
                else if (pokemonId == 150)
                {
                    incrementCounter(7, 0, 1);
                }
                else if (pokemonId == 151)
                {
                    incrementCounter(8, 0, 1);
                }
            }
            //check if pokemon is from johto, if so increase johto dex counter
            else if (pokemonId < 252)
            {
                incrementCounter(27, 0, 1);
            }
            //check if pokemon is from hoenn, if so increase hoenn dex counter
            else if (pokemonId < 386)
            {
                incrementCounter(28, 0, 1);
            }
            //increse sinnoh dex counter
            else
            {
                incrementCounter(29, 0, 1);
            }
        }
    }

    /**
     * increase counters for hatching pokemon achievements and if the pokemon is shiny, complete obtain shiny achievement
     * @param isShiny is whether or not the hatched pokemon is shiny
     */
    public void setEggsHatched(boolean isShiny)
    {
        incrementCounter(12, 3, 1);

        if (isShiny)
        {
            incrementCounter(25, 0, 1);
        }
    }

    public void setBerriesHarvested(int numHarvested)
    {
        incrementCounter(66, 5, numHarvested);
    }

    /**
     * called when a battle is won
     * @param trainerSpriteName is the defeated trainer's sprite name (their name/trainer class)
     */
    public void setBattlesWon(String trainerSpriteName)
    {
        //increase battles won achievements
        incrementCounter(43, 4, 1);
        //check for battle achievements for beating rocket grunts or specific trainers and increase achievement counters
        if (trainerSpriteName.equals("rocketGruntBoy") || trainerSpriteName.equals("rocketGruntGirl"))
        {
            incrementCounter(56, 2, 1);
        }
        else if (trainerSpriteName.equals("gary"))
        {
            incrementCounter(55, 0, 1);
        }
        else if (trainerSpriteName.equals("giovanni"))
        {
            incrementCounter(59, 0, 1);
        }
        else if (trainerSpriteName.equals("lance"))
        {
            incrementCounter(60, 0, 1);
        }
        else if (trainerSpriteName.equals("steven"))
        {
            incrementCounter(61, 0, 1);
        }
        else if (trainerSpriteName.equals("wallace"))
        {
            incrementCounter(62, 0, 1);
        }
        else if (trainerSpriteName.equals("cynthia"))
        {
            incrementCounter(63, 0, 1);
        }
        else if (trainerSpriteName.equals("oak"))
        {
            incrementCounter(64, 0, 1);
        }
        else if (trainerSpriteName.equals("mirorb"))
        {
            incrementCounter(65, 0, 1);
        }
    }

    //called when a trainer's pokemon has achieved max happiness, progresses max happiness achievement
    public void setMaxHappiness()
    {
        incrementCounter(24, 0, 1);
    }

    /**
     * progress achievements for using super effective or stab moves in battle
     * @param numSuperEffectiveMoves is the number of supereffective moves used in a given battle
     * @param numStabMoves is the number of stab moves used in a given battle
     */
    public void setMovesUsed(int numSuperEffectiveMoves, int numStabMoves)
    {
        incrementCounter(32, 4, numSuperEffectiveMoves);
        incrementCounter(37, 5, numStabMoves);
    }

    /** 
     * increment counters for events which have progressed
     * @param achievementId is the id of the first achievement progressed
     * @param numAchievements is the number of proceeding achievements with the same requirements as the first
     */
    private void incrementCounter(int achievementId, int numAchievements, int increment)
    {
        //loop through progressed achievements and increment counter if required value has not been reached
        for (int i = achievementId; i <= (achievementId + numAchievements); i++)
        {
            this.achievements.get(i).counter = Math.min(this.achievements.get(i).requiredValue, this.achievements.get(i).counter + increment);
        }
    }
    
    /** 
     * Read the conversation data from a database
     */
    private void loadEvents()
    {
        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM achievements";

            ResultSet rs = db.runQuery(query);

            while(rs.next()) 
            {
                AchievementDataModel achievement = new AchievementDataModel(rs);
                this.achievements.add(achievement);
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    class AchievementDataModel
    {
        public String name;
        public int counter;
        public int requiredValue;
        public int rewardId;
        public int rewardQuantity;
        public String description;
        
        public AchievementDataModel(ResultSet rs) throws SQLException
        {
            this.name = rs.getString("name");
            this.counter = rs.getInt("counter");
            this.requiredValue = rs.getInt("required_value");
            this.rewardId = rs.getInt("reward_id");
            this.rewardQuantity = rs.getInt("reward_quantity");
            this.description = rs.getString("description");
        }
    }
}

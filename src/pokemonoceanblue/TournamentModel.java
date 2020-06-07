package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TournamentModel {
    private final int tournamentId;
    private int round;
    private List<TournamentDataModel> tournamentData;

    public TournamentModel(int tournamentId)
    {
        this.tournamentId = tournamentId;
        this.loadCharacters();
    }

    private void loadCharacters()
    {
        tournamentData = new ArrayList<TournamentDataModel>();

        try
        {
            DatabaseUtility db = new DatabaseUtility();

            String query = "SELECT * FROM character WHERE character_id BETWEEN 1000 + 100 * " + this.tournamentId
                            + " AND 1099 + 100 * " + this.tournamentId;

            ResultSet rs = db.runQuery(query);
            Random rand = new Random();

            while(rs.next()) 
            {
                CharacterModel characterModel = new CharacterModel(
                    rs.getString("sprite_name"), 
                    rs.getInt("x"), 
                    rs.getInt("y"),
                    rs.getInt("conversation_id"),
                    rs.getInt("character_id"),
                    rs.getInt("wander_range"),
                    Direction.DOWN.getDirection(rs.getInt("direction"))
                );
                TournamentDataModel tournamentDataModel = new TournamentDataModel(characterModel, rs.getString("name"));

                if (this.tournamentData.size() == 0)
                {
                    this.tournamentData.add(tournamentDataModel);
                }
                else
                {
                    // add characters in random order
                    this.tournamentData.add(rand.nextInt(this.tournamentData.size() + 1), tournamentDataModel);
                }
                
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }  
    }

    /**
     * @return the next character the player will battle or null if no characters are left
     */
    public CharacterModel getCharacter()
    {
        for (int i = this.tournamentData.size() - 1; i >= 0; i--)
        {
            if (this.tournamentData.get(i).ranking == this.round)
            {
                return this.tournamentData.get(i).characterModel;
            }
        }

        return null;
    }

    /**
     * Determine the winners in each matchup
     * Assumes player won
     */
    public void nextRound()
    {
        Random rand = new Random();
        TournamentDataModel previous = null;

        this.round++;

        for (TournamentDataModel current : this.tournamentData)
        {
            // exclude characters who already lost
            if (current.ranking < this.round - 1)
            {
                continue;
            }
            // pick the first member of the matchup
            else if (previous == null)
            {
                previous = current;
            }
            // compare the two members of the matchup
            else
            {
                if (current.ranking - previous.ranking >= 2)
                {
                    // gym leaders will never beat champions
                    current.ranking++;
                }
                else if (previous.ranking - current.ranking >= 2)
                {
                    previous.ranking++;
                }
                else
                {
                    // randomly decide who will win if they are reasonably close in skill level
                    int winner = rand.nextInt(3 + previous.priority + current.priority);

                    if (winner > 1 + previous.priority)
                    {
                        current.ranking++;
                    }
                    else
                    {
                        previous.ranking++;
                    }
                }

                previous = null;
            }
        }
    }

    public class TournamentDataModel 
    {
        public CharacterModel characterModel;
        public int ranking = 0;
        public int priority = 0;

        public TournamentDataModel(CharacterModel characterModel, String characterName)
        {
            this.characterModel = characterModel;

            if (characterName.contains("Gym Leader") || characterName.contains("Pokemon Trainer"))
            {
                this.priority = 1;
            }
            else if (characterName.contains("Elite Four") || characterName.contains("Rival"))
            {
                this.priority = 2;
            }
            else if (characterName.contains("Champion"))
            {
                this.priority = 3;
            }
        }
    }
}
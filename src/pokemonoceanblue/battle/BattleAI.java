package pokemonoceanblue.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pokemonoceanblue.MoveModel;
import pokemonoceanblue.PokemonModel;

public class BattleAI {
    // only let trainers make one switch during battle
    private boolean haveSwitched;
    private Random ranNum = new Random();
    private int difficulty;

    public BattleAI(String trainerName, int battleId)
    {
        if (trainerName.contains("Gym Leader") || battleId >= 1000) 
        {
            difficulty = 1;
        }
        else 
        {
            difficulty = 0;
        }
    }

    /**
     * Decides what the enemy Pokemon or trainer will do for their turn of battle
     */
    public int getAction(PokemonModel[] team, PokemonModel enemy, int currentPokemon)
    {
        int action;

        // for difficulty 0, just use a random move
        //if (this.difficulty == 0)
        //{
        action = ranNum.nextInt(team[currentPokemon].moves.length);
        //}
        // for difficulty 1, prioritize super effective attacks
        // else 
        // {
        //     List<Integer> attackUtility = new ArrayList<Integer>();
        //     int totalUtility = 0;

        //     for (MoveModel move : team[currentPokemon].moves)
        //     {
                
        //     }
        // }

        return action;
    }
}

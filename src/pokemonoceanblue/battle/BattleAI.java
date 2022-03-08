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
        if ((trainerName != null && trainerName.contains("Gym Leader")) || battleId >= 1000) 
        {
            this.difficulty = 1;
        }
        else 
        {
            this.difficulty = 0;
        }
    }

    public BattleAI()
    {
        this.difficulty = 0;
    }

    /**
     * Decides what the enemy Pokemon or trainer will do for their turn of battle
     */
    public int getAction(PokemonModel[] team, PokemonModel enemy, int currentPokemon)
    {
        int action = 0;

        // for difficulty 0, just use a random move
        if (this.difficulty == 0)
        {
            action = ranNum.nextInt(team[currentPokemon].moves.length);
        }
        // for difficulty 1, prioritize super effective attacks
        else 
        {
            List<Integer> attackUtility = new ArrayList<Integer>();
            int totalUtility = 0;
            int utility = 0;

            // generate the utility of each move
            for (MoveModel move : team[currentPokemon].moves)
            {
                // status moves
                if (move.damageClassId == 1)
                {
                    utility = 50;
                }
                else
                {
                    float typeModifier = BattleOperationsManager.getTypeModifier(team[currentPokemon], enemy, move);

                    if (move.accuracy == -1)
                    {
                        // assign bonus utility for moves that never miss
                        utility = (int)(20 + move.power * typeModifier);
                    }
                    else
                    {
                        utility = (int)(move.power * (move.accuracy / 100.00) * typeModifier);
                    }
                }
                
                attackUtility.add(utility);
                totalUtility += utility;
            }

            // randomly pick a move, weighted based on utility
            utility = ranNum.nextInt(totalUtility);
            int index = 0;

            // find which option was randomly selected by utility
            while (utility > 0)
            {
                action = index;
                utility -= attackUtility.get(index);
                index++; 
            }

        }

        return action;
    }
}

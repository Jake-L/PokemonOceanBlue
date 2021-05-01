package pokemonoceanblue;

import java.util.Random;

public class BattleOperationsManager {
    Random ranNum = new Random();

    public int determineFirstAttacker(PokemonModel playerPokemon, PokemonModel enemyPokemon, int playerMoveIndex, int enemyMoveIndex,
                                      int playerSpeed, int enemySpeed)
    {
        int firstAttacker;
        if (playerPokemon.moves[playerMoveIndex].priority > enemyPokemon.moves[enemyMoveIndex].priority)
        {
            firstAttacker = 0;
        }
        else if (playerPokemon.moves[playerMoveIndex].priority < enemyPokemon.moves[enemyMoveIndex].priority)
        {
            firstAttacker = 1;
        }
        else if (playerPokemon.statusEffect != StatusEffect.PARALYSIS && enemyPokemon.statusEffect == StatusEffect.PARALYSIS)
        {
            firstAttacker = 0;
        }
        else if (enemyPokemon.statusEffect != StatusEffect.PARALYSIS && playerPokemon.statusEffect == StatusEffect.PARALYSIS)
        {
            firstAttacker = 1;
        }
        else if (playerSpeed < enemySpeed)
        {
            firstAttacker = 1;
        }
        else if (playerSpeed > enemySpeed)
        {
            firstAttacker = 0;
        }
        else 
        {
            firstAttacker = ranNum.nextInt(2);
        }
        return firstAttacker;
    }
}

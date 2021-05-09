package pokemonoceanblue;

import java.util.Random;

public class BattleOperationsManager {
    Random ranNum = new Random();

    public int getAccuracy(int attacker, int moveAccuracy, int[][] statChanges)
    {
        int defender = (attacker + 1) % 2;
        if (statChanges[attacker][6] == 0 && statChanges[defender][7] == 0)
        {
            return moveAccuracy;
        }
        return (int)(moveAccuracy * (2.0 / (Math.abs(statChanges[attacker][6]) + 2)) / 
            (statChanges[defender][7] > 0 ? (Math.abs(statChanges[defender][7]) + 2) / 2.0 : 2.0 / (Math.abs(statChanges[defender][7]) + 2)));
    }

    /**
     * Determines if all the Pokemon in team have fainted
     * @param team the team that is being looked at (player team or enemy team)
     * @return true if all of the Pokemon in team have fainted
     */
    public boolean teamFainted(PokemonModel[] team)
    {
        int faintedPokemon = 0;
        for (int i = 0; i < team.length; i++)
        {
            if (team[i].currentHP == 0)
            {
                faintedPokemon++;
            }
            if (faintedPokemon == team.length)
            {
                return true;
            }
        }
        return false;
    }

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

    /**
     * Calculate the probability of the wild Pokemon being captured
     * @param pokemon
     * @param itemId
     * @return
     */
    public double captureChanceCalc(PokemonModel pokemon, int itemId)
    {
        // find the probability of the Pokemon being captured
        int pokemonCaptureRate = pokemon.captureRate;
        double pokeballModifier = 1.0;
        double statusModifier = 1.0;

        // master ball has 100% capture chance
        if (itemId == 0)
        {
            pokeballModifier = 999;
        }
        // ultra ball has 2x catch chance
        else if (itemId == 1)
        {
            pokeballModifier = 2;
        }
        // great ball has 1.5x catch chance
        else if (itemId == 2)
        {
            pokeballModifier = 1.5;
        }
        // net ball more effective on bug and water types
        else if (itemId == 5 
            && (Type.typeIncludes(Type.BUG, pokemon.types) 
                || Type.typeIncludes(Type.WATER, pokemon.types)))
        {
            pokeballModifier = 2;
        }

        // sleep and freeze give 2x capture chance
        if (pokemon.statusEffect == StatusEffect.SLEEP || pokemon.statusEffect == StatusEffect.FROZEN)
        {
            statusModifier = 2;
        }
        // any other status effect gives 1.5x capture chance
        else if (pokemon.statusEffect != StatusEffect.UNAFFLICTED)
        {
            statusModifier = 1.5;
        }
        
        double captureChance = pokemonCaptureRate * pokeballModifier * statusModifier
            - (pokemon.level / 10) * pokemon.currentHP / (double)(pokemon.stats[Stat.HP]);

        return captureChance;
    }
}

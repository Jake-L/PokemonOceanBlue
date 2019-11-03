package pokemonoceanblue;


public class BattleModel 
{
    public PokemonModel[][] team = new PokemonModel[2][6];
    public int[] currentPokemon = new int[2];

    /** 
     * Constructor
     * @param opponentTeam the opposing trainers pokemon team
     * @param playerTeam the players pokemon team
     */
    public BattleModel(PokemonModel[] opponentTeam, PokemonModel[] playerTeam)
    {
        team[0] = playerTeam;
        team[1] = opponentTeam;
        currentPokemon[0] = 0;
        currentPokemon[1] = 0;
    }
}    
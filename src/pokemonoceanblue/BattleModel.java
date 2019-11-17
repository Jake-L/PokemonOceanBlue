package pokemonoceanblue;


public class BattleModel 
{
    public PokemonModel[][] team = new PokemonModel[2][6];
    public int[] currentPokemon = new int[2];
    public byte daytimeType = 0;
    public byte areaType = 0;
    public String[] battleOptions = new String[3];
    public int optionIndex = 0;
    public byte counter = 15;

    /** 
     * Constructor
     * @param opponentTeam the opposing trainers pokemon team
     * @param playerTeam the players pokemon team
     */
    public BattleModel(PokemonModel[] opponentTeam, PokemonModel[] playerTeam)
    {
        this.team[0] = playerTeam;
        this.team[1] = opponentTeam;
        this.currentPokemon[0] = 0;
        this.currentPokemon[1] = 0;
        this.battleOptions[0] = "FIGHT";
        this.battleOptions[1] = "POKEMON";
        this.battleOptions[2] = "POKEBALLS";
    }

    public void confirmSelection()
    {
        if (this.battleOptions[this.optionIndex].equals("FIGHT"))
        {
            this.battleOptions = new String[this.team[0][this.currentPokemon[0]].moves.length];
            
            for (int i = 0; i < this.team[0][this.currentPokemon[0]].moves.length; i++)
            {
                this.battleOptions[i] = String.valueOf(this.team[0][this.currentPokemon[0]].moves[i]);
            } 
        }
    }

    public boolean isComplete()
    {
        return false;
    }

    public void update()
    {
        if (this.counter > 0)
        {
            this.counter--;
        }
    }
}    
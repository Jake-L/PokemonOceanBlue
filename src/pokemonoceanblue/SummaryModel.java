package pokemonoceanblue;

import java.util.List;

public class SummaryModel extends BaseModel
{
    public final List<PokemonModel> pokemonList;
    public MoveModel newMove;
    public int hoverMoveIndex = -1;

    public SummaryModel(List<PokemonModel> pokemonList, int optionIndex, MoveModel newMove)
    {
        super();
        this.pokemonList = pokemonList;
        this.optionIndex = optionIndex;
        this.newMove = newMove;
        this.optionWidth = 1;
        this.optionMax = this.pokemonList.size() - 1;
        this.optionHeight = this.optionMax;

        if (newMove != null)
        {
            this.hoverMoveIndex = 0;
        }
    }

    @Override
    public void moveIndex(int dx, int dy)
    {
        if (this.hoverMoveIndex > -1)
        {
            // move through the list of moves to view their descriptions
            if ((dx > 0 || dy > 0) && this.hoverMoveIndex < this.pokemonList.get(this.optionIndex).moves.length)
            {
                this.hoverMoveIndex++;
            } 
            else if ((dx < 0 || dy < 0) && this.hoverMoveIndex > 0)
            {
                this.hoverMoveIndex--;
            } 
        }
        else if (this.newMove == null)
        {
            super.moveIndex(dx, dy);
        }   
    }

    @Override
    public void confirmSelection()
    {
        // replace one of the Pokemon's moves with the new move
        if (this.newMove != null)
        {
            // if player selects new move, don't learn it
            if (this.hoverMoveIndex == 4)
            {
                this.returnValue = -1;
            }
            else
            {
                this.returnValue = this.hoverMoveIndex;
            }
        }
        else
        {
            this.returnValue = this.optionIndex;
        }
    }

    @Override
    public void exitScreen()
    {
        if (this.newMove != null)
        {
            this.returnValue = -1;
        }
        else
        {
            this.returnValue = this.optionIndex;
        }
    }
}
package pokemonoceanblue;

/**
 * Controller for the overworld
 */
public class OverworldController {

    private OverworldModel model;
    
    /** 
     * Constructor for the overworld controller
     * @param model the overworld model
     */
    public OverworldController(OverworldModel model){
        this.model = model;
    }

    /** 
     * @param x x position of the character
     * @param y y position of the character
     * @return true if the position is free or false if it is occupied
     */
    public boolean checkPosition(int x, int y)
    {
        if (y < 0 || y >= model.tiles.length
            || x < 0 || x >= model.tiles[y].length
            || model.tiles[y][x] < 1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }


}
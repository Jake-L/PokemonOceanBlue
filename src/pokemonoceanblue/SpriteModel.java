package pokemonoceanblue;

/** 
 * A class used for simple objects on the map that only require a sprite and x,y coordinates
 */
public class SpriteModel {
    public String spriteName;
    public int x;
    public int y;
    
    /** 
     * Constructor
     * @param spriteName name of the sprite used in the image filename
     * @param x x position of the sprite
     * @param y y position of the sprite
     */
    public SpriteModel(String spriteName, int x, int y){
        this.spriteName = spriteName;
        this.x = x;
        this.y = y;
    }
}
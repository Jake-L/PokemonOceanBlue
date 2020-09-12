package pokemonoceanblue;

public class PortalModel {
    public final int x;
    public final int y;
    public final int destMapId;
    public final int destX;
    public final int destY;
    public final Direction direction;
    
    /** 
     * @param x x-cooordinate of the portal's location
     * @param y y-cooordinate of the portal's location
     */
    public PortalModel(int x, int y, int destMapId, int destX, int destY, Direction direction){
        this.x = x;
        this.y = y;
        this.destMapId = destMapId;
        this.destX = destX;
        this.destY = destY;
        this.direction = direction;
    }
}
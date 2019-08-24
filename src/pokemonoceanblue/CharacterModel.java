package pokemonoceanblue;

import javax.swing.ImageIcon;

public class CharacterModel {
    private String spriteName;
    private int x;
    private int y;
    private int height;
    private int movementCounter = 0;
    private int dy;
    private int dx;
    private Direction direction;
    private int animationOffset = 0;
    private OverworldController overworldController;

    /** 
     * @param spriteName name of the sprite used in the image filename
     * @param x x position of the sprite
     * @param y y position of the sprite
     */
    public CharacterModel(String spriteName, int x, int y){
        this.spriteName = spriteName;
        this.x = x;
        this.y = y;
        this.direction = Direction.DOWN;

        // get the sprite's height
        ImageIcon ii = new ImageIcon(String.format("src/characters/%sDown0.png", spriteName));
        this.height = ii.getImage().getHeight(null);
    }

    public void setOverworldController(OverworldController overworldController)
    {
        this.overworldController = overworldController;
    }

    public String getSpriteName(){
        return this.spriteName;
    }
    
    /** 
     * @return the filename of the sprite that should be displayed
     */
    public String getCurrentSprite(){
        if (this.movementCounter >= 0)
        {
            return String.format("%s%s%s", this.spriteName, this.direction.toString(), (int)(Math.floor((this.movementCounter + 16 * this.animationOffset) / 8)));
        }
        else
        {
            return String.format("%s%s0", this.spriteName, direction.toString());
        }
    }
    
    public int getX(){
        return this.x;
    }
    
    public int getRenderX(){
        return this.x * 16 - this.dx * this.movementCounter;
    }
    
    public int getDx(){
        return this.dx;
    }

    public int getY(){
        return this.y;
    }
    
    public int getRenderY(){
        return (this.y + 1) * 16 - this.height - this.dy * this.movementCounter;
    }

    public int getDy(){
        return this.dy;
    }
    
    public int getMovementCounter(){
        return this.movementCounter;
    }
    
    /** 
     * @param dx movement speed along x-axis
     * @param dy movement speed along y-axis
     * @param movementCounter the duration of their movement, usually 16
     */
    public void setMovement(int dx, int dy, int movementCounter)
    {
        // can only specify movement when not already moving
        if (this.movementCounter <= 0)
        {
            // toggle animation offset between 0 and 1
            animationOffset = Math.abs(animationOffset - 1);

            // set the players direction, even if they aren't able to move
            if (dx > 0)
            {
                this.direction = Direction.RIGHT;
            }
            else if (dx < 0)
            {
                this.direction = Direction.LEFT;
            }
            else if (dy < 0)
            {
                this.direction = Direction.UP;
            }
            else if (dy > 0)
            {
                this.direction = Direction.DOWN;
            }

            // animate the player walking, even if they can't move
            this.movementCounter = movementCounter;

            // check if the space they want to move into is open
            // check x direction
            if (dx != 0 && overworldController.checkPosition(this.x + dx, this.y))
            {
                // set their movement speed
                this.dx = dx;
                this.dy = 0;
            }
            // check y direction
            else if (dy != 0 && overworldController.checkPosition(this.x, this.y + dy))
            {
                // set their movement speed
                this.dy = dy;
                this.dx = 0;
            }

            // update the player's grid position immediately
            this.x += this.dx;
            this.y += this.dy;
        }
    }

    /** 
     * determines when the character should stop moving
     */
    public void update()
    {
        if (this.movementCounter >= 0)
        {
            this.movementCounter--;
        }
        if (this.movementCounter <= 0)
        {
            this.dx = 0;
            this.dy = 0;
        }
    }
}
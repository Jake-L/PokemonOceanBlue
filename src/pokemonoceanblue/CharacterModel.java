package pokemonoceanblue;

import javax.swing.ImageIcon;

public class CharacterModel {
    private String spriteName;
    private int x;
    private int y;
    private int height;
    private int movementCounter = 0;

    // must be 1 for walking or 2 for running, but never 0
    private int movementSpeed = 1;
    
    private int dy;
    private int dx;
    private Direction direction;
    private int animationOffset = 0;
    public OverworldModel overworldModel;
    public final int spawn_x;
    public final int spawn_y;
    public int conversationId;
    public final int characterId;
    public final int wanderRange;

    public boolean surf = false;

    /** 
     * @param spriteName name of the sprite used in the image filename
     * @param x x position of the sprite
     * @param y y position of the sprite
     * @param conversationId a unique identifier for their conversation or -1 otherwise
     * @param characterId a unique identifier for their character
     * @param wanderRange the distance from the character's initial position that they are allowed to move
     * @param direction the direction the character initially faces 
     */
    public CharacterModel(String spriteName, int x, int y, int conversationId, int characterId, int wanderRange, Direction direction){
        this.spriteName = spriteName;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.spawn_x = x;
        this.spawn_y = y;
        this.conversationId = conversationId;
        this.characterId = characterId;
        this.wanderRange = wanderRange;

        if (spriteName.contains("swimmer"))
        {
            this.surf = true;
        }

        // get the sprite's height
        ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/characters/%s.png", this.getCurrentSprite())));
        this.height = ii.getImage().getHeight(null);
    }

    public void setOverworldModel(OverworldModel overworldModel)
    {
        this.overworldModel = overworldModel;
    }

    public String getSpriteName(){
        return this.spriteName;
    }

    public int getMovementSpeed(){
        return this.movementSpeed;
    }

    public void setDirection(Direction direction)
    {
        this.direction = direction;
    }
    
    /** 
     * @return the filename of the sprite that should be displayed
     */
    public String getCurrentSprite(){
        if (this.surf)
        {
            return String.format("%sSurf%s%s", 
                this.spriteName,  
                this.direction.toString(), 
                System.currentTimeMillis() / 500 % 2);
        }
        else if (this.movementCounter >= 0)
        {
            // load different image if running or walking
            String running = this.movementSpeed == 2 ? "Run" : "";

            return String.format("%s%s%s%s", 
                this.spriteName, 
                running, 
                this.direction.toString(), 
                (int)(Math.floor((this.movementCounter + 16 * this.animationOffset) / 8)) % 4);
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

    public Direction getDirection()
    {
        return this.direction;
    }
    
    /** 
     * @param dx movement speed along x-axis
     * @param dy movement speed along y-axis
     * @param movementCounter the duration of their movement, usually 16
     */
    public void setMovement(int dx, int dy, int movementSpeed)
    {
        // can only specify movement when not already moving
        if (this.movementCounter <= 0 && this.overworldModel.canMove(this.characterId))
        {
            // toggle animation offset between 0 and 1
            animationOffset = Math.abs(animationOffset - 1);

            Direction oldDirection = this.direction;

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

            // if the player quickly taps a direction key, they can change direction without moving
            if (this.spriteName == "red" && oldDirection != this.direction && movementSpeed <= 1)
            {
                this.movementCounter = 3;
                return;
            }

            // animate the player walking, even if they can't move
            this.movementCounter = 16;            

            // check if the space they want to move into is open
            // check x direction
            if (dx != 0 && overworldModel.checkPosition(this.x + dx, this.y, this.surf))
            {
                // set their movement speed
                this.dx = dx;
                this.dy = 0;
                this.movementSpeed = movementSpeed;
            }
            // check y direction
            else if (dy != 0 && overworldModel.checkPosition(this.x, this.y + dy, this.surf))
            {
                // set their movement speed
                this.dy = dy;
                this.dx = 0;
                this.movementSpeed = movementSpeed;
            }

            // update the player's grid position immediately
            this.x += this.dx;
            this.y += this.dy;
        }
    }

    /** 
     * determines when the character should stop moving
     */
    public void update(boolean updateOverworld)
    {
        if (this.movementCounter >= 0)
        {
            this.movementCounter -= this.movementSpeed;
        
            if (this.movementCounter == 0)
            {
                this.dx = 0;
                this.dy = 0;
                this.movementSpeed = 1;

                if (updateOverworld)
                {
                    System.out.printf("new position %s, %s\n", this.x, this.y);
                    this.overworldModel.checkMovement(this.x, this.y);
                }
            }
        }
    }
}
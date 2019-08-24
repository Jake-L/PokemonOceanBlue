package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the overworld
 */
public class OverworldView {

    private OverworldModel model;
    private CharacterModel playerModel;
    private Image[] tileSprite = new Image[5];
    Map<String, Image> mapObjectSprite = new HashMap<String, Image>();
    Map<String, Image> characterSprite = new HashMap<String, Image>();
    private byte graphics_scaling;
    private int width;
    private int height;
    private int x_offset;
    private int y_offset;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public OverworldView(OverworldModel model, CharacterModel playerModel){
        this.model = model;
        this.playerModel = playerModel;
        loadImage();
    }

    
    /** 
     * Sets variables for rendering on screen
     * @param graphics_scaling a factor to multiply by all measurements to fit the screen
     * @param width width of the screen in pixels
     * @param height height of the screen in pixels
     */
    public void setViewSize(byte graphics_scaling, int width, int height)
    {
        this.graphics_scaling = graphics_scaling;
        this.width = width;
        this.height = height;
    }

    /** 
     * loads all the necessary sprites
     */
    private void loadImage() {
        // load tile sprites
        for (int i = 1; i < tileSprite.length; i++)
        {
            ImageIcon ii = new ImageIcon(String.format("src/tiles/%s.png", i));
            tileSprite[i] = ii.getImage();
        }

        // load all mapObject sprites 
        File folder = new File("src/mapObjects/");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                ImageIcon ii = new ImageIcon(String.format("src/mapObjects/%s", listOfFiles[i].getName()));
                mapObjectSprite.put(listOfFiles[i].getName().replace(".png",""), ii.getImage());
            }
        }

        // load character sprites
        String spriteName = playerModel.getSpriteName();
        String formattedName;
        // get their sprites for each direction
        for (Direction direction : Direction.values())
        {
            // each direction has 4 sprites
            for (int i = 0; i < 4; i++)
            {
                formattedName = String.format("%s%s%s", spriteName, direction.toString(), i);
                ImageIcon ii = new ImageIcon(String.format("src/characters/%s.png", formattedName));
                characterSprite.put(formattedName, ii.getImage());
            }
        }
    }

    /** 
     * renders the overworld graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    public void render(Graphics g, JPanel canvas) {
        Image sprite;
        int playerRenderX = playerModel.getRenderX();
        int playerRenderY = playerModel.getRenderY();

        calcOffset(playerRenderX, playerRenderY);

        // display the ground tiles
        for (int y = 0; y < model.tiles.length; y++)
        {
            for (int x = 0; x < model.tiles[y].length; x++)
            {
                g.drawImage(tileSprite[Math.abs(model.tiles[y][x])], 
                            (x * 16 - x_offset) * graphics_scaling, 
                            (y * 16 - y_offset) * graphics_scaling, 
                            16 * graphics_scaling, 
                            16 * graphics_scaling,
                            canvas);
            }
        }

        // draw map objects
        for (SpriteModel current : model.mapObjects)
        {
            sprite = mapObjectSprite.get(current.spriteName);
            g.drawImage(sprite, 
                        (current.x * 16 - x_offset + 8 - sprite.getWidth(null)/2) * graphics_scaling, 
                        ((current.y + 1) * 16 - sprite.getHeight(null) - y_offset) * graphics_scaling, 
                        sprite.getWidth(null) * graphics_scaling, 
                        sprite.getHeight(null) * graphics_scaling, 
                        canvas);
        }

        // get the player's sprite name
        sprite = characterSprite.get(playerModel.getCurrentSprite());

        // draw the player
        g.drawImage(sprite, 
                    (playerRenderX - x_offset) * graphics_scaling, 
                    (playerRenderY - y_offset) * graphics_scaling, 
                    sprite.getWidth(null) * graphics_scaling, 
                    sprite.getHeight(null) * graphics_scaling, 
                    canvas);        
    }

    
    /** 
     * Calculates the offset to use for screen scrolling
     * @param playerRenderX the x position to render the player, before applying graphics scaling multiplier
     * @param playerRenderY the y position to render the player, before applying graphics scaling multiplier
     */
    public void calcOffset(int playerRenderX, int playerRenderY){
        /* 
        / get horizontal offset
        */
        int left_offset = (int) (playerRenderX - (Math.ceil(width / (2.00 * graphics_scaling))));
		int right_offset = (int) (playerRenderX + (Math.floor(width / (2.00 * graphics_scaling))));

		// if the map doesn't fill the whole screen, center it
		if (model.tiles[0].length * 16 <= width / graphics_scaling)
		{
			x_offset = (int) ((model.tiles[0].length * 16 - Math.ceil(width / graphics_scaling)) / 2);
		}
		// check if the player is moving in the middle of the map and the screen needs to be moved
		else if (left_offset > 0 && right_offset <= model.tiles[0].length * 16)
		{
            x_offset = left_offset;
        }
        // check if player is on right side of the map and screen stops scrolling
		else if (left_offset > 0 && right_offset > model.tiles[0].length * 16)
		{
            x_offset = (int) (model.tiles[0].length * 16 - Math.ceil(width / graphics_scaling));
		}
		else
		{
			x_offset = 0;
        }
        
        /* 
        / get vertical offset
        */
        var top_offset = playerRenderY - (Math.round(height / graphics_scaling) / 2);
        var bot_offset = playerRenderY + (Math.round(height / graphics_scaling) / 2);

		// if the map doesn't fill the whole screen, center it
		if (model.tiles.length * 16 <= height / graphics_scaling)
		{
			y_offset = Math.round((model.tiles.length * 16 - (height / graphics_scaling)) / 2);
		}
		else if (top_offset > 0 && bot_offset <= model.tiles.length * 16)
		{
			y_offset = top_offset;
		}
		else if (top_offset > 0 && bot_offset > model.tiles.length * 16)
		{
			y_offset = (int)(model.tiles.length * 16 - Math.ceil(height / graphics_scaling));
		}
		else
		{
			y_offset = 0;
        }
    }
}
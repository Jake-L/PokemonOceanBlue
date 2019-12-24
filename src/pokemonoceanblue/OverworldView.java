package pokemonoceanblue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the overworld
 */
public class OverworldView extends ViewBase {

    private OverworldModel model;
    private Image[] tileSprite = new Image[40];
    private Map<String, Image> animatedTileSprite = new HashMap<String, Image>();
    private Map<String, Image> mapObjectSprite = new HashMap<String, Image>();
    private Map<String, Image> characterSprite = new HashMap<String, Image>();
    private int xOffset;
    private int yOffset;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     */
    public OverworldView(OverworldModel model){
        this.model = model;
        loadImage();
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

        // load animated water tile sprites
        for (int i = 0; i < 8; i++)
        {
            ImageIcon ii = new ImageIcon(String.format("src/tiles/0-%s.png", i));
            animatedTileSprite.put(String.format("0-%s", i), ii.getImage());
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
        String spriteName = model.playerModel.getSpriteName();
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

        for (CharacterModel cpuModel : model.cpuModel)
        {
            // load character sprites
            spriteName = cpuModel.getSpriteName();

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
    }

    /** 
     * renders the overworld graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) {
        // draw black background
        Color colour = new Color(0, 0, 0, 255);
        g.setColor(colour);
        g.fillRect(0, 0, width, height);
        
        Image sprite;
        int playerRenderX = model.playerModel.getRenderX();
        int playerRenderY = model.playerModel.getRenderY();

        calcOffset(playerRenderX, playerRenderY);

        // display the ground tiles
        for (int y = 0; y < model.tiles.length; y++)
        {
            for (int x = 0; x < model.tiles[y].length; x++)
            {
                if (model.tiles[y][x] == 0)
                {
                    //load animated water sprite
                    sprite = animatedTileSprite.get(String.format("0-%s", System.currentTimeMillis() / 500 % 8));
                }
                else
                {
                    sprite = tileSprite[Math.abs(model.tiles[y][x])];
                }
                g.drawImage(sprite, 
                            (x * 16 - xOffset) * graphicsScaling, 
                            (y * 16 - yOffset) * graphicsScaling, 
                            16 * graphicsScaling, 
                            16 * graphicsScaling,
                            canvas);
            }
        }

        // create a sorted list of all the characters to be rendered
        List<CharacterModel> renderList = new ArrayList<CharacterModel>();
        renderList.add(model.playerModel);
        for (CharacterModel current : model.cpuModel)
        {
            int i = 0;
            while (i < renderList.size() && renderList.get(i).getY() < current.getY())
            {
                i++;
            }
            renderList.add(i, current);
        }

        int objectIndex = 0;
        int characterIndex = 0;

        // move through lists of mapObjects and characters, rendering them in vertical order
        while (objectIndex < model.mapObjects.size() && characterIndex < renderList.size())
        {
            SpriteModel currentObject = model.mapObjects.get(objectIndex);
            CharacterModel currentCharacter = renderList.get(characterIndex);
            if (currentObject.y < currentCharacter.getY())
            {
                this.renderObject(g, canvas, model.mapObjects.get(objectIndex));                
                objectIndex++;
            }
            else
            {
                this.renderCharacter(g, canvas, renderList.get(characterIndex));                            
                characterIndex++;
            }
        }
        while (objectIndex < model.mapObjects.size())
        { 
            this.renderObject(g, canvas, model.mapObjects.get(objectIndex));
            objectIndex++;
        }
        while (characterIndex < renderList.size())
        {
            this.renderCharacter(g, canvas, renderList.get(characterIndex));
            characterIndex++;
        }

        // display conversation text
        if (this.model.conversation != null && this.model.conversation.getText() != null)
        {
            this.displayText(this.model.conversation.getText(), g, canvas);
        }

        // display text options
        if (this.model.textOptions != null)
        {
            this.displayOptions(this.model.textOptions, this.model.optionIndex, g, canvas);
        }
    }

    /** 
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     * @param object the object to be rendered
     */
    private void renderObject(Graphics g, JPanel canvas, SpriteModel object)
    {
        // render the mapObject
        Image sprite = mapObjectSprite.get(object.spriteName);
        g.drawImage(sprite, 
                    (object.x * 16 - xOffset) * graphicsScaling, // x refers to leftmost position
                    ((object.y + 1) * 16 - sprite.getHeight(null) - yOffset) * graphicsScaling, 
                    sprite.getWidth(null) * graphicsScaling, 
                    sprite.getHeight(null) * graphicsScaling, 
                    canvas);
    }

    /** 
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     * @param object the character to be rendered
     */
    private void renderCharacter(Graphics g, JPanel canvas, CharacterModel character)
    {
        // render the character
        Image sprite = characterSprite.get(character.getCurrentSprite());
        g.drawImage(sprite, 
                    (character.getRenderX() - xOffset) * graphicsScaling, 
                    (character.getRenderY() - yOffset) * graphicsScaling, 
                    sprite.getWidth(null) * graphicsScaling, 
                    sprite.getHeight(null) * graphicsScaling, 
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
        int leftOffset = (int) (playerRenderX - (Math.ceil(width / (2.00 * graphicsScaling))));
		int rightOffset = (int) (playerRenderX + (Math.floor(width / (2.00 * graphicsScaling))));

		// if the map doesn't fill the whole screen, center it
		if (model.tiles[0].length * 16 <= width / graphicsScaling)
		{
			xOffset = (int) ((model.tiles[0].length * 16 - Math.ceil(width / graphicsScaling)) / 2);
		}
		// check if the player is moving in the middle of the map and the screen needs to be moved
		else if (leftOffset > 0 && rightOffset <= model.tiles[0].length * 16)
		{
            xOffset = leftOffset;
        }
        // check if player is on right side of the map and screen stops scrolling
		else if (leftOffset > 0 && rightOffset > model.tiles[0].length * 16)
		{
            xOffset = (int) (model.tiles[0].length * 16 - Math.ceil(width / graphicsScaling));
		}
		else
		{
			xOffset = 0;
        }
        
        /* 
        / get vertical offset
        */
        var topOffset = playerRenderY - (Math.round(height / graphicsScaling) / 2);
        var botOffset = playerRenderY + (Math.round(height / graphicsScaling) / 2);

		// if the map doesn't fill the whole screen, center it
		if (model.tiles.length * 16 <= height / graphicsScaling)
		{
			yOffset = Math.round((model.tiles.length * 16 - (height / graphicsScaling)) / 2);
		}
		else if (topOffset > 0 && botOffset <= model.tiles.length * 16)
		{
			yOffset = topOffset;
		}
		else if (topOffset > 0 && botOffset > model.tiles.length * 16)
		{
			yOffset = (int)(model.tiles.length * 16 - Math.ceil(height / graphicsScaling));
		}
		else
		{
			yOffset = 0;
        }
    }

    @Override
    public String toString(){
        return "OverworldView";
    }
}
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
public class OverworldView extends BaseView {

    private OverworldModel model;
    private Image[] tileSprite = new Image[127];
    private Map<String, Image> animatedTileSprite = new HashMap<String, Image>();
    private Map<String, Image> mapObjectSprite = new HashMap<String, Image>();
    private Map<String, Image> characterSprite = new HashMap<String, Image>();
    private int xOffset = -1;
    private int yOffset = -1;
    private int[] ANIMATED_TILES = new int[]{0, 6, 7, 8, 67, 70};
    private Image mugshotBackgroundSprite;
    private Image mugshotCharacterSprite;
    private Image mugshotLightningSprite;
    private Image mugshotVsSprite;
    
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
            for (int tileId : this.ANIMATED_TILES)
            {
                ImageIcon ii = new ImageIcon(String.format("src/tiles/%s-%s.png", tileId, i));
                animatedTileSprite.put(String.format("%s-%s", tileId, i), ii.getImage());
            }
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
                // walking sprites
                formattedName = String.format("%s%s%s", spriteName, direction.toString(), i);
                ImageIcon ii = new ImageIcon(String.format("src/characters/%s.png", formattedName));
                characterSprite.put(formattedName, ii.getImage());

                // running sprites
                formattedName = String.format("%sRun%s%s", spriteName, direction.toString(), i);
                ii = new ImageIcon(String.format("src/characters/%s.png", formattedName));
                characterSprite.put(formattedName, ii.getImage());
            }

            // surfing sprites
            for (int i = 0; i < 2; i++)
            {
                // walking sprites
                formattedName = String.format("%sSurf%s%s", spriteName, direction.toString(), i);
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

                if (spriteName.contains("swimmer"))
                {
                    // surfing sprites
                    for (int i = 0; i < 4; i++)
                    {
                        formattedName = String.format("%sSurf%s%s", spriteName, direction.toString(), i);
                        ImageIcon ii = new ImageIcon(String.format("src/characters/%s.png", formattedName));
                        characterSprite.put(formattedName, ii.getImage());
                    }
                }
            }
        }

        // load mugshot sprites
        ImageIcon ii = new ImageIcon("src/mugshots/lightning.png");
        this.mugshotLightningSprite = ii.getImage();

        ii = new ImageIcon("src/mugshots/vs.png");
        this.mugshotVsSprite = ii.getImage();
    }

    /** 
     * renders the overworld graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) {
        // check for mugshot display
        if (this.mugshotCharacterSprite == null && this.model.mugshotCharacter != null)
        {
            ImageIcon ii = new ImageIcon(String.format("src/mugshots/%s.png", this.model.mugshotCharacter));
            this.mugshotCharacterSprite = ii.getImage();

            ii = new ImageIcon(String.format("src/mugshots/background%s.png", this.model.mugshotBackground));
            this.mugshotBackgroundSprite = ii.getImage();
        }
        else if (this.mugshotCharacterSprite != null && this.model.mugshotCharacter == null)
        {
            this.mugshotCharacterSprite = null;
            this.mugshotBackgroundSprite = null;
        }

        // draw black background
        Color colour = new Color(0, 0, 0, 255);
        g.setColor(colour);
        g.fillRect(0, 0, width, height);
        
        Image sprite;

        calcOffset(model.playerModel);

        // display the ground tiles
        for (int y = 0 + Math.max(yOffset / 16,0); y < Math.min(model.tiles.length, this.model.playerModel.getY() + height / (16 * graphicsScaling)); y++)
        {
            for (int x = 0 + Math.max(xOffset / 16,0); x < Math.min(model.tiles[y].length, this.model.playerModel.getX() + width / (16 * graphicsScaling)); x++)
            {
                if (this.isAnimatedSprite(this.model.tiles[y][x]))
                {
                    //load animated water sprite
                    sprite = animatedTileSprite.get(String.format("%s-%s", Math.abs(this.model.tiles[y][x]), System.currentTimeMillis() / 160 % 8));
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

        // display overlap tiles
        if (this.model.tilesOverlay != null)
        {
            for (int y = 0 + Math.max(yOffset / 16,0); y < Math.min(model.tilesOverlay.length, this.model.playerModel.getY() + height / (16 * graphicsScaling)); y++)
            {
                for (int x = 0 + Math.max(xOffset / 16,0); x < Math.min(model.tilesOverlay[y].length, this.model.playerModel.getX() + width / (16 * graphicsScaling)); x++)
                {
                    if (this.model.tilesOverlay[y][x] > 0)
                    {
                        sprite = tileSprite[model.tilesOverlay[y][x]];
                        g.drawImage(sprite, 
                                    (x * 16 - xOffset) * graphicsScaling, 
                                    (y * 16 - yOffset) * graphicsScaling, 
                                    16 * graphicsScaling, 
                                    16 * graphicsScaling,
                                    canvas);
                    }
                }
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
            if (currentObject.y + currentObject.yAdjust <= currentCharacter.getY())
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

        // display gym leader mugshot
        if (this.mugshotCharacterSprite != null)
        {
            for (int i = -1; i < 2; i++)
            {
                g.drawImage(
                    this.mugshotBackgroundSprite, 
                    (int)(System.currentTimeMillis() % this.mugshotBackgroundSprite.getWidth(null) + i * this.mugshotBackgroundSprite.getWidth(null)) * graphicsScaling, 
                    height / 2 - this.mugshotBackgroundSprite.getHeight(null) * graphicsScaling / 2, 
                    this.mugshotBackgroundSprite.getWidth(null) * graphicsScaling, 
                    this.mugshotBackgroundSprite.getHeight(null) * graphicsScaling,
                    canvas
                );

                g.drawImage(
                    this.mugshotLightningSprite, 
                    (int)(System.currentTimeMillis() % (this.mugshotLightningSprite.getWidth(null)) + i * this.mugshotLightningSprite.getWidth(null)) * graphicsScaling, 
                    height / 2 - this.mugshotLightningSprite.getHeight(null) * graphicsScaling / 2, 
                    this.mugshotLightningSprite.getWidth(null) * graphicsScaling, 
                    this.mugshotLightningSprite.getHeight(null) * graphicsScaling,
                    canvas
                );
            }

            g.drawImage(
                this.mugshotCharacterSprite, 
                width / 2, 
                height / 2 - (this.mugshotCharacterSprite.getHeight(null) + 4 - this.mugshotBackgroundSprite.getHeight(null) / 2) * graphicsScaling, 
                this.mugshotCharacterSprite.getWidth(null) * graphicsScaling, 
                this.mugshotCharacterSprite.getHeight(null) * graphicsScaling,
                canvas
            );

            g.drawImage(
                this.mugshotVsSprite, 
                width / 4 - (int)(System.currentTimeMillis() / 4 % 2) * graphicsScaling, 
                height / 2 - (int)(System.currentTimeMillis() / 6 % 3 + this.mugshotVsSprite.getHeight(null) / 2) * graphicsScaling, 
                this.mugshotVsSprite.getWidth(null) * graphicsScaling, 
                this.mugshotVsSprite.getHeight(null) * graphicsScaling,
                canvas
            );            
        }

        // fade to black when removing a character
        if (this.model.removeCharacter)
        {
            g.setColor(new Color(0, 0, 0, (int)(255 * (1 - Math.abs((this.model.conversation.getCounter() - 8) / 8.0)))));
            g.fillRect(0, 0, width, height);
        }

        //this.renderRain(g, canvas, 24);
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
                    (character.getRenderX() + 8 - sprite.getWidth(null) / 2 - xOffset) * graphicsScaling, 
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
    public void calcOffset(CharacterModel player)
    {
        // set initial x offset
        if (this.xOffset == -1)
        {
            this.xOffset = this.calcOffsetAux(player.getRenderX(), player.getDx(), width, model.tiles[player.getY()].length);
        }
        // shift the x offset while the player moves
        else if (player.getMovementCounter() >= 0)
        {
            int newXOffset = this.calcOffsetAux(player.getRenderX(), player.getDx(), width, model.tiles[player.getY()].length);

            if (newXOffset > this.xOffset && player.getDx() > 0)
            {
                this.xOffset += player.getMovementSpeed();
            }
            else if (newXOffset < this.xOffset && player.getDx() < 0)
            {
                this.xOffset -= player.getMovementSpeed();
            }
        }
        // set initial y offset
        if (this.yOffset == -1)
        {
            this.yOffset = this.calcOffsetAux(player.getRenderY(), player.getDy(), height, columnHeight(player.getX()));
        }
        // shift the y offset while the player moves
        else if (player.getMovementCounter() >= 0)
        {
            int newYOffset = this.calcOffsetAux(player.getRenderY(), player.getDy(), height, columnHeight(player.getX()));

            if (newYOffset > this.yOffset && player.getDy() > 0)
            {
                this.yOffset += player.getMovementSpeed();
            }
            else if (newYOffset < this.yOffset && player.getDy() < 0)
            {
                this.yOffset -= player.getMovementSpeed();
            }
        }
    }

    private int calcOffsetAux(int playerRenderPosition, int movement, int maximumPixels, int maximumPosition)
    {
        int negOffset = playerRenderPosition - (int)(Math.round(maximumPixels / (graphicsScaling * 2.0)));
        int posOffset = playerRenderPosition + (int)(Math.round(maximumPixels / (graphicsScaling * 2.0)));
        int newOffset = 0;

		// if the map doesn't fill the whole screen, center it
		if (maximumPosition * 16 <= maximumPixels / graphicsScaling)
		{
			newOffset = Math.round((maximumPosition * 16 - (maximumPixels / graphicsScaling)) / 2);
		}
		else if (negOffset > 0 && posOffset <= maximumPosition * 16)
		{
            newOffset = negOffset;
		}
		else if (negOffset > 0 && posOffset > maximumPosition * 16)
		{
            newOffset = (int)(maximumPosition * 16 - Math.ceil(maximumPixels / graphicsScaling));
		}
		else
		{
			newOffset = 0;
        }

        return newOffset;
    }

    private int columnHeight(int x)
    {
        int columnHeight = 0;

        while (columnHeight < this.model.tiles.length 
            && this.model.tiles[columnHeight].length > x)
        {
            columnHeight++;
        }

        return columnHeight;
    }

    private boolean isAnimatedSprite(int checkTileId)
    {
        for (int tileId : this.ANIMATED_TILES)
        {
            if (tileId == Math.abs(checkTileId))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return "OverworldView";
    }
}
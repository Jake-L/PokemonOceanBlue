package pokemonoceanblue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.util.Arrays;

/** 
 * Renders the overworld
 */
public class OverworldView extends BaseView {

    private OverworldModel model;
    private Image[] tileSprite = new Image[109];
    private Map<String, Image> animatedTileSprite = new HashMap<String, Image>();
    private Map<String, Image> mapObjectSprite = new HashMap<String, Image>();
    private Map<String, Image> characterSprite = new HashMap<String, Image>();
    private int xOffset = -1;
    private int yOffset = -1;
    private int[] ANIMATED_TILES = new int[]{0, 6, 7, 8, 67, 70, 88, 90};
    private int[] ANIMATED_TILE_LENGTH = new int[]{8, 8, 8, 8, 5, 5, 4, 5};
    private Image mugshotBackgroundSprite;
    private Image mugshotCharacterSprite;
    private Image mugshotLightningSprite;
    private Image mugshotVsSprite;
    private Image[] inventoryBorder = new Image[9];
    
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
        for (int i = 0; i < tileSprite.length; i++)
        {
            int animatedIndex = Arrays.binarySearch(ANIMATED_TILES, i);
            if (animatedIndex < 0)
            {
                ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/tiles/%s.png", i)));
                tileSprite[i] = ii.getImage();
            }
            else
            {
                // load animated tile sprites
                for (int j = 0; j < ANIMATED_TILE_LENGTH[animatedIndex]; j++)
                {
                    ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/tiles/%s-%s.png", i, j)));
                    animatedTileSprite.put(String.format("%s-%s", i, j), ii.getImage());
                }
            }
            
        }

        for (int i = 0; i < this.model.mapObjects.size(); i++) 
        {
            String spriteName = this.model.mapObjects.get(i).spriteName;

            if (this.mapObjectSprite.get(spriteName) == null)
            {
                ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/mapObjects/%s.png", spriteName)));
                mapObjectSprite.put(spriteName, ii.getImage());
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
                ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/characters/%s.png", formattedName)));
                characterSprite.put(formattedName, ii.getImage());

                // running sprites
                formattedName = String.format("%sRun%s%s", spriteName, direction.toString(), i);
                ii = new ImageIcon(this.getClass().getResource(String.format("/characters/%s.png", formattedName)));
                characterSprite.put(formattedName, ii.getImage());
            }

            // surfing sprites
            for (int i = 0; i < 2; i++)
            {
                // walking sprites
                formattedName = String.format("%sSurf%s%s", spriteName, direction.toString(), i);
                ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/characters/%s.png", formattedName)));
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
                if (spriteName.contains("swimmer"))
                {
                    // surfing sprites
                    for (int i = 0; i < 4; i++)
                    {
                        formattedName = String.format("%sSurf%s%s", spriteName, direction.toString(), i);
                        ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/characters/%s.png", formattedName)));
                        characterSprite.put(formattedName, ii.getImage());
                    }
                }
                else
                {
                    try
                    {
                        // each direction has 4 sprites
                        for (int i = 0; i < 4; i++)
                        {
                            formattedName = String.format("%s%s%s", spriteName, direction.toString(), i);
                            ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/characters/%s.png", formattedName)));
                            characterSprite.put(formattedName, ii.getImage());
                        }
                    }
                    catch (Exception e)
                    {
                        // this is expected because many sprites, like gym leaders
                        // do not have walking animations
                    }                    
                }
            }
        }

        // load mugshot sprites
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/mugshots/lightning.png"));
        this.mugshotLightningSprite = ii.getImage();

        ii = new ImageIcon(this.getClass().getResource("/mugshots/vs.png"));
        this.mugshotVsSprite = ii.getImage();

        // load sprites for in shops
        for (int i = 0; i < inventoryBorder.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/inventory/border" + i + ".png"));
            inventoryBorder[i]  = ii.getImage();
        }
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
            ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/mugshots/%s.png", this.model.mugshotCharacter)));
            this.mugshotCharacterSprite = ii.getImage();

            ii = new ImageIcon(this.getClass().getResource(String.format("/mugshots/background%s.png", this.model.mugshotBackground)));
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
                int animatedIndex = Arrays.binarySearch(ANIMATED_TILES, Math.abs(this.model.tiles[y][x]));
                if (animatedIndex >= 0)
                {
                    //load animated water sprite
                    sprite = animatedTileSprite.get(String.format("%s-%s", 
                        Math.abs(this.model.tiles[y][x]), 
                        System.currentTimeMillis() 
                            / (1280 / ANIMATED_TILE_LENGTH[animatedIndex]) 
                            % ANIMATED_TILE_LENGTH[animatedIndex]));
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

        if (this.model.mapId == 14)
        {
            // make Viridian forest appear darker
            this.renderDarkness(g, canvas);
        }
        else
        {
            this.renderWeather(this.model.weather, g, canvas);
        }

        // put text colour back to black
        g.setColor(colour);
        
        // display conversation text
        if (this.model.conversation != null && this.model.conversation.getText() != null)
        {
            this.displayText(this.model.conversation.getText(), g, canvas);
        }

        // display text options
        if (this.model.textOptions != null)
        {
            this.displayOptions(this.model.textOptions, this.model.textOptionIndex, g, canvas);
        }

        // display gym leader mugshot
        if (this.mugshotCharacterSprite != null)
        {
            // use separate loops for brackground and lightnight because background moves faster
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
            }

            int b = graphicsScaling * (int)((System.currentTimeMillis() / 3) % 256);

            for (int i = -1; i < 2; i++)
            {
                g.drawImage(
                    this.mugshotLightningSprite, 
                    b + (i * this.mugshotLightningSprite.getWidth(null) * graphicsScaling), 
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

        // display the current shop window
        if (this.model.itemOptions.size() > 0)
        {
            this.renderShop(g, canvas);
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
                    (character.getRenderX() + 8 - sprite.getWidth(null) / 2 - xOffset) * graphicsScaling, 
                    (character.getRenderY() - yOffset) * graphicsScaling, 
                    sprite.getWidth(null) * graphicsScaling, 
                    sprite.getHeight(null) * graphicsScaling, 
                    canvas); 
    }

    /**
     * Renders the shpo interface, showing items available for purchase and their prices
     * @param g
     * @param canvas
     */
    private void renderShop(Graphics g, JPanel canvas)
    {
        int textWidth;
        int fontSize = Math.max(16, height / 10);
        int fontSpacing = fontSize * 2 / 3;
        int iconScaling = (int)Math.floor(fontSpacing / 20);
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);

        // draw the box that holds all the item names
        this.displayTextbox(inventoryBorder, width * 3 / 10, 0, width * 2 / 3, height * 7 / 10, g, canvas);

        // draw the box that holds the player's current money
        this.displayTextbox(inventoryBorder, 0, 0, width / 4, height / 5, g, canvas);

        // display the player's current money
        String money = "$" + String.valueOf(this.model.inventoryModel.getMoney());
        textWidth = g.getFontMetrics(font).stringWidth(money);
        g.drawString(money,
            width / 4 - textWidth - 12 * graphicsScaling,
            height / 5 - 12 * graphicsScaling);

        // draw the box that shows the player's currently owned quantity
        this.displayTextbox(inventoryBorder, 0, height / 5 + 8 * graphicsScaling, width / 4, height / 5, g, canvas);
        g.drawString("In bag: ",
            12 * graphicsScaling,
            height * 2 / 5 - 4 * graphicsScaling);

        String quantityString = String.valueOf(this.model.inventoryModel.getQuantity(this.model.itemOptions.get(this.model.optionIndex).itemId));
        textWidth = g.getFontMetrics(font).stringWidth(quantityString);
        g.drawString(quantityString,
            width / 4 - textWidth - 12 * graphicsScaling,
            height * 2 / 5 - 4 * graphicsScaling);

        // display item description
        this.displayText(this.model.itemOptions.get(this.model.optionIndex).description, g, canvas);

        for (int i = this.model.optionMin; i <= this.model.optionMax; i++)
        {
            Image sprite = itemSprite[this.model.itemOptions.get(this.minRenderIndex + i).itemId];

            // draw the item's name
            g.drawString(this.model.itemOptions.get(this.minRenderIndex + i).name,
                width * 3 / 10 + (24 + sprite.getWidth(null)) * graphicsScaling,
                (i * fontSpacing) + 30 * graphicsScaling);

            // display an icon for the item
            g.drawImage(sprite,
                width * 3 / 10 + 16 * graphicsScaling,
                (i * fontSpacing) + 30 * graphicsScaling - sprite.getHeight(null) * iconScaling,
                sprite.getWidth(null) * iconScaling,
                sprite.getHeight(null) * iconScaling,
                canvas);

            // display the item's price
            String quantity = "$" + String.valueOf(this.model.itemOptions.get(this.minRenderIndex + i).cost);
            textWidth = g.getFontMetrics(font).stringWidth(quantity);

            g.drawString(quantity,
                width * 29 / 30 - 24 * graphicsScaling - textWidth,
                (i * fontSpacing) + 30 * graphicsScaling);
        }

        // draw an arrow showing the currently selected item
        g.drawImage(arrowSprite,
            width * 3 / 10 + 2 * graphicsScaling,
            (this.model.optionIndex - this.model.optionMin) * fontSpacing + 30 * graphicsScaling - arrowSprite.getHeight(null) * 2 * iconScaling,
            arrowSprite.getWidth(null) * 2 * iconScaling,
            arrowSprite.getHeight(null) * 2 * iconScaling,
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

    /**
     * Display a black rectangle over the screen at 20% opacity to make it look darker
     */
    private void renderDarkness(Graphics g, JPanel canvas)
    {
        Color colour = new Color(0, 0, 0, 51);
        g.setColor(colour);
        g.fillRect(0, 0, width, height);
    }
}
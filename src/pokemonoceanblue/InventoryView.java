package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the overworld
 */
public class InventoryView extends ViewBase {

    private InventoryModel model;
    private Image[] itemSprite = new Image[150];
    private Image[] bagSprite = new Image[3];
    private Image[] inventoryBorder = new Image[9];
    private Image background;
    private int minIndex = 0;
    private int rowLimit = 0;
    private int oldItemIndex = 0;
    private int oldBagIndex = 0;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public InventoryView(InventoryModel model){
        this.model = model;
        loadImage();
    }

    /** 
     * load's all the item's icons
     */
    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < itemSprite.length; i++)
        {
            try {
                ii = new ImageIcon("src/inventory/" + i + ".png");
                itemSprite[i]  = ii.getImage();
            } catch (Exception e) {
                //TODO: handle exception
            }
            
        }

        for (int i = 0; i < bagSprite.length; i++)
        {
            ii = new ImageIcon("src/inventory/bag" + i + ".png");
            bagSprite[i]  = ii.getImage();
        }

        for (int i = 0; i < inventoryBorder.length; i++)
        {
            ii = new ImageIcon("src/inventory/border" + i + ".png");
            inventoryBorder[i]  = ii.getImage();
        }

        ii = new ImageIcon("src/inventory/background.png");
        background = ii.getImage();
    }

    /** 
     * renders the party screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        // draw the background
        int blockSize = background.getWidth(null);
        for (int i = 0; i < width / blockSize; i++)
        {
            for (int j = 0; j < height / blockSize; j++)
            {
                g.drawImage(background, i * blockSize, j * blockSize, blockSize, blockSize, canvas);
            }
        }

        // draw the box that holds all the item names
        this.displayTextbox(inventoryBorder, width * 3 / 10, 0, width * 2 / 3, height * 7 / 10, g, canvas);

        // display bag
        g.drawImage(this.bagSprite[this.model.bagIndex],
                    width * 3 / 20 - this.bagSprite[this.model.bagIndex].getWidth(null) * this.graphicsScaling,
                    (int)(height * 0.3),
                    this.bagSprite[this.model.bagIndex].getWidth(null) * 2 * this.graphicsScaling,
                    this.bagSprite[this.model.bagIndex].getHeight(null) * 2 * this.graphicsScaling,
                    canvas);

        // display item description
        if (this.model.items[this.model.bagIndex].size() > 0)
        {
            this.displayText(this.model.items[this.model.bagIndex].get(this.model.itemIndex).description, g, canvas);
        }

        int fontSize = Math.max(16, height / 10);
        int fontSpacing = fontSize * 2 / 3;
        int iconScaling = (int)Math.floor(fontSpacing / 20);

        // set the font
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);
        g.setFont(font);

        // determine the first item name to show
        if (this.model.bagIndex != this.oldBagIndex)
        {
            this.minIndex = 0;
            this.oldItemIndex = 0;
            this.oldBagIndex = 0;
        }
        if (this.model.itemIndex != this.oldItemIndex)
        {
            this.calcIndices();
        }

        // determine the number of item names to show
        this.rowLimit = Math.min(
            this.model.items[this.model.bagIndex].size() - this.minIndex, 
            (int)Math.floor((height * 0.7 - 36 * graphicsScaling) / fontSpacing)
        );

        // if the screen resolution changes and the item the player selected is no longer displayed
        // increase the minimum index displayed until the selected item is shown
        if (this.model.itemIndex > this.minIndex + this.rowLimit)
        {
            this.minIndex = this.model.itemIndex - this.rowLimit;
        }

        for (int i = 0; i <= this.rowLimit; i++)
        {
            Image sprite = this.itemSprite[this.model.items[this.model.bagIndex].get(this.minIndex + i).itemId];

            // draw the item's name
            g.drawString(this.model.items[this.model.bagIndex].get(this.minIndex + i).name,
                width * 3 / 10 + (24 + sprite.getWidth(null)) * graphicsScaling,
                (i * fontSpacing) + 30 * graphicsScaling);

            // display an icon for the item
            g.drawImage(sprite,
                width * 3 / 10 + 16 * graphicsScaling,
                (i * fontSpacing) + 30 * graphicsScaling - sprite.getHeight(null) * iconScaling,
                sprite.getWidth(null) * iconScaling,
                sprite.getHeight(null) * iconScaling,
                canvas);

            // draw the item's quantity
            String quantity = "x" + String.valueOf(this.model.items[this.model.bagIndex].get(this.minIndex + i).quantity);
            int textWidth = g.getFontMetrics(font).stringWidth(quantity);

            g.drawString(quantity,
                width * 29 / 30 - 24 * graphicsScaling - textWidth,
                (i * fontSpacing) + 30 * graphicsScaling);
        }

        // draw an arrow showing the currently selected item
        g.drawImage(this.arrowSprite,
            width * 3 / 10 + 2 * graphicsScaling,
            (this.model.itemIndex - this.minIndex) * fontSpacing + 30 * graphicsScaling - arrowSprite.getHeight(null) * 2 * iconScaling,
            arrowSprite.getWidth(null) * 2 * iconScaling,
            arrowSprite.getHeight(null) * 2 * iconScaling,
            canvas);

    }

    /**
     * 
     */
    public void calcIndices() 
    {
        // if player is not at either visual endpoint of the inventory, just move the cursor
        if (this.model.itemIndex > this.minIndex && this.model.itemIndex < this.rowLimit + this.minIndex)
        {
            this.oldItemIndex = this.model.itemIndex;
        }
        // if the player is moving down at the bottom of the inventory
        if (this.model.itemIndex > this.oldItemIndex 
            && this.model.itemIndex > this.rowLimit + this.minIndex 
            // make sure there is still room to move down
            && this.rowLimit + this.minIndex < this.model.items[this.model.bagIndex].size())
        {
            this.minIndex++;
        }
        // if the player is moving up in the inventory
        else if (this.model.itemIndex < this.oldItemIndex
            && this.model.itemIndex < this.minIndex 
            // make sure there is still room to move up
            && this.minIndex > 0)
        {
            this.minIndex--;
        }

        this.oldItemIndex = this.model.itemIndex;
        this.oldBagIndex =this.model.bagIndex;
        
    }

    @Override
    public String toString()
    {
        return "InventoryView";
    }
}
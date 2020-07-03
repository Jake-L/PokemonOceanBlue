package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the overworld
 */
public class InventoryView extends BaseView {

    private InventoryModel model;
    private Image[] bagSprite = new Image[3];
    private Image[] inventoryBorder = new Image[9];
    private Image background;
    private int oldBagIndex = 0;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public InventoryView(InventoryModel model){
        super(model);
        this.model = model;
        loadImage();
    }

    /** 
     * load's all the item's icons
     */
    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < bagSprite.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/inventory/bag" + i + ".png"));
            bagSprite[i]  = ii.getImage();
        }

        for (int i = 0; i < inventoryBorder.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/inventory/border" + i + ".png"));
            inventoryBorder[i]  = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/inventory/background.png"));
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
        int blockSize = background.getWidth(null) * graphicsScaling;
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
            this.displayText(this.model.items[this.model.bagIndex].get(this.model.optionIndex).description, g, canvas);
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
            this.minRenderIndex = 0;
            this.oldOptionIndex = 0;
            this.oldBagIndex = this.model.bagIndex;
        }
        if (this.model.optionIndex != this.oldOptionIndex)
        {
            // determine the number of item names to show
            this.maxRenderRows = (int)Math.floor((height * 0.7 - 20 * graphicsScaling) / fontSpacing);
            this.calcIndices();
            this.oldBagIndex =this.model.bagIndex;
        }

        for (int i = 0; i < this.maxRenderRows; i++)
        {
            if (this.minRenderIndex + i > this.model.optionMax)
            {
                break;
            }

            Image sprite = itemSprite[this.model.items[this.model.bagIndex].get(this.minRenderIndex + i).itemId];

            // draw the item's name
            g.drawString(this.model.items[this.model.bagIndex].get(this.minRenderIndex + i).name,
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
            String quantity = "x" + String.valueOf(this.model.items[this.model.bagIndex].get(this.minRenderIndex + i).quantity);
            int textWidth = g.getFontMetrics(font).stringWidth(quantity);

            g.drawString(quantity,
                width * 29 / 30 - 24 * graphicsScaling - textWidth,
                (i * fontSpacing) + 30 * graphicsScaling);
        }

        // draw an arrow showing the currently selected item
        g.drawImage(arrowSprite,
            width * 3 / 10 + 2 * graphicsScaling,
            (this.model.optionIndex - this.minRenderIndex) * fontSpacing + 30 * graphicsScaling - arrowSprite.getHeight(null) * 2 * iconScaling,
            arrowSprite.getWidth(null) * 2 * iconScaling,
            arrowSprite.getHeight(null) * 2 * iconScaling,
            canvas);

    }

    @Override
    public String toString()
    {
        return "InventoryView";
    }
}
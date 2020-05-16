package pokemonoceanblue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

abstract class BaseView {
    protected byte graphicsScaling;
    protected int width;
    protected int height;
    protected Image[] textDisplayBox = new Image[9];
    protected BaseModel model;
    protected int minRenderIndex;
    protected int maxRenderRows;
    protected int oldOptionIndex = -1;
    protected static Image[] itemSprite;
    protected Image progressBar;
    protected Image progressBarFill;

    // sprites used by many subclasses
    protected Image arrowSprite;
    protected static Image[] rainSprite = new Image[6];
    protected static Image[] genderIcons = new Image[2];
    protected static Image[] typeSprites = new Image[18];

    public BaseView()
    {
        ImageIcon ii;

        for (int i = 0; i < 9; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/battle/TextBox" + i + ".png"));
            textDisplayBox[i]  = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/inventory/arrow.png"));
        arrowSprite = ii.getImage();

        for (int i = 0; i < rainSprite.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/battle/rain" + i + ".png"));
            rainSprite[i]  = ii.getImage();
        }

        // load sprites only once for all views
        if (itemSprite == null)
        {
            itemSprite = new Image[150];

            for (int i = 0; i < 4; i++)
            {
                ii = new ImageIcon(this.getClass().getResource("/inventory/" + i + ".png"));
                itemSprite[i]  = ii.getImage();
            } 

            for (int i = 113; i < 134; i++)
            {
                ii = new ImageIcon(this.getClass().getResource("/inventory/" + i + ".png"));
                itemSprite[i]  = ii.getImage();
            } 

            for (int i = 0; i < typeSprites.length; i++)
            {
                ii = new ImageIcon(this.getClass().getResource("/menus/type" + i + ".png"));
                typeSprites[i] = ii.getImage();
            }

            for (int i = 0; i < genderIcons.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/gender" + i + ".png"));
            genderIcons[i] = ii.getImage();
        }
        }

        ii = new ImageIcon(this.getClass().getResource("/menus/progressBar.png"));
        this.progressBar = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/menus/progressBarFill.png"));
        this.progressBarFill = ii.getImage();
    }

    public BaseView(BaseModel model)
    {    
        this(); 
        this.model = model;
        this.minRenderIndex = this.model.optionMin;
    }

    /** 
     * Sets variables for rendering on screen
     * @param graphicsScaling a factor to multiply by all measurements to fit the screen
     * @param width width of the screen in pixels
     * @param height height of the screen in pixels
     */
    public void setViewSize(byte graphicsScaling, int width, int height)
    {
        this.graphicsScaling = graphicsScaling;
        this.width = width;
        this.height = height;

        // force recalculation of indices to adjust for new screen size
        this.oldOptionIndex = -1;
    }

    /**
     * Renders a text box using the given sprites and dimensions
     * @param boxSprite an array of 9 images, where index 0 is the top left sprite, 1 is the top sprite, and 8 is the bottom right sprite
     * @param x x position of text box
     * @param y y position of text box
     * @param boxWidth width of text box
     * @param boxHeight height of text box
     * @param g graphics object
     * @param canvas JPanel object
     */
    protected void displayTextbox(Image boxSprite[], int x, int y, int boxWidth, int boxHeight, Graphics g, JPanel canvas)
    {
        // sprites are square, so only need to look at width or height
        int boxSize = boxSprite[0].getWidth(null);

        for (int j = y + boxSize * graphicsScaling; j < boxHeight + y - boxSize * graphicsScaling; j += boxSize * graphicsScaling)
        {
            //centre
            for (int i = x + boxSize * graphicsScaling; i < boxWidth + x - boxSize * graphicsScaling; i += boxSize * graphicsScaling)
            {
                g.drawImage(boxSprite[4],
                    i,
                    j,
                    boxSize * graphicsScaling,
                    boxSize * graphicsScaling,
                    canvas);
            }

            //left
            g.drawImage(boxSprite[3],
                x,
                j,
                boxSize * graphicsScaling,
                boxSize * graphicsScaling,
                canvas);

            //right
            g.drawImage(boxSprite[5],
                x + boxWidth - boxSize * graphicsScaling,
                j,
                boxSize * graphicsScaling,
                boxSize * graphicsScaling,
                canvas);
        }

        //top left
        g.drawImage(boxSprite[0],
            x,
            y,
            boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //top right
        g.drawImage(boxSprite[2],
            x + boxWidth - boxSize * graphicsScaling,
            y,
            boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //top
        g.drawImage(boxSprite[1],
            x + boxSize * graphicsScaling,
            y,
            boxWidth - boxSize * 2 * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //bottom left
        g.drawImage(boxSprite[6],
            x,
            y + boxHeight - boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //bottom right
        g.drawImage(boxSprite[8],
            x + boxWidth - boxSize * graphicsScaling,
            y + boxHeight - boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //bottom
        g.drawImage(boxSprite[7],
            x + boxSize * graphicsScaling,
            y + boxHeight - boxSize * graphicsScaling,
            boxWidth - boxSize * 2 * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
    } 

    /**
     * Renders the given text inside of a text box
     * @param text the text to be displayed
     * @param g Graphics object
     * @param canvas JPanel object
     */
    protected void displayText(String text, Graphics g, JPanel canvas)    
    {
        // display the text box at the bottom of the screen
        this.displayTextbox(textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);

        // fill the box with text
        int fontSize = Math.max(16, (int)(height * 0.105));
        this.displayText(text, fontSize, 0, height * 3 / 4, width, height / 4, g, canvas);
    }

    /**
     * Renders the given text within the given dimensions
     * @param text the string to be displayed
     * @param fontSize the size of the font
     * @param x the left position of the box the text is being drawn in
     * @param y the top position of the box the text is being drawn in
     * @param boxWidth the width of the box the text is being drawn in
     * @param boxHeight the height of the box the text is being drawn in
     * @param g Graphics object
     * @param canvas JPanel object
     */
    protected void displayText(String text, int fontSize, int x, int y, int boxWidth, int boxHeight, Graphics g, JPanel canvas)
    {
        if (text == null)
        {
            return;
        }

        int fontSpacing = fontSize * 2 / 3;
        
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);
        g.setFont(font);

        List<String> renderText = new ArrayList<String>();
        renderText.add("");
        String[] splitText = text.replace("$",",").split(" ");
        int index = 0;
        int line = 0;

        // split the string into multiple linse
        while (index < splitText.length && line <= (boxHeight - 16 * graphicsScaling) / fontSpacing)
        {
            // move to next line when end is reached
            if (g.getFontMetrics(font).stringWidth(renderText.get(line) + splitText[index]) >= boxWidth - 16 * graphicsScaling 
                // force at least on word per line
                && !renderText.get(line).equals(""))
            {
                line++;
                renderText.add("");
            }

            renderText.set(line, renderText.get(line) + splitText[index] + " ");
            index++;
        }

        // display the string
        for (int i = 0; i < renderText.size(); i++)
        {
            g.drawString(renderText.get(i), 
                x + 8 * graphicsScaling, 
                y + (3 * graphicsScaling) + fontSpacing * (i+1));
        }
    }

    /**
     * Displays the given text options at the right side of the screen
     * @param textOptions the array of text options to be displayed
     * @param g Graphics object
     * @param canvas JPanel object
     */
    protected void displayOptions(String[] textOptions, int optionIndex, Graphics g, JPanel canvas)
    {
        int fontSize = Math.max(16, height / 10);
        int fontSpacing = fontSize * 2 / 3;

        // set the font
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);
        g.setFont(font);

        // determine the width of the text box
        int textWidth = 0;
        for (int i = 0; i < textOptions.length; i++)
        {
            textWidth = Math.max(g.getFontMetrics(font).stringWidth(textOptions[i]), textWidth);
        }

        // render the text box that contains the options
        displayTextbox(textDisplayBox, 
            width - textWidth - 24 * graphicsScaling, 
            height * 3 / 4 - fontSpacing * textOptions.length - 16 * graphicsScaling, 
            textWidth + 24 * graphicsScaling, 
            fontSpacing * textOptions.length + 24 * graphicsScaling, 
            g, canvas);

        // render the text
        for (int i = 0; i < textOptions.length; i++)
        {
            g.drawString(textOptions[i], 
                width - textWidth - 8 * graphicsScaling, 
                height * 3 / 4 - fontSpacing * (textOptions.length - i - 1) - 8 * graphicsScaling);
        }  
        
        // render the arrow
        g.drawImage(this.arrowSprite,
            width - textWidth - 21 * graphicsScaling,
            height * 3 / 4 - fontSpacing * (textOptions.length - optionIndex - 1) - (8 + arrowSprite.getHeight(null) * 2) * graphicsScaling,
            arrowSprite.getWidth(null) * 2 * graphicsScaling,
            arrowSprite.getHeight(null) * 2 * graphicsScaling,
            canvas);
    }

    // render function that gets implemented by extended class
    abstract void render(Graphics g, JPanel canvas);

    /**
     * @param image image to be recoloured white
     */
    protected BufferedImage colorImage(BufferedImage image, int red, int green, int blue) 
    {
        WritableRaster raster = image.getRaster();

        for (int x = 0; x < image.getWidth(); x++) 
        {
            for (int y = 0; y < image.getHeight(); y++) 
            {
                int[] pixels = raster.getPixel(x, y, (int[]) null);
                pixels[0] = red;
                pixels[1] = green;
                pixels[2] = blue;
                raster.setPixel(x, y, pixels);
            }
        }

        return image;
    }

    protected void renderRain(Graphics g, JPanel canvas, int counter)
    {
        for (int i = 0; i < counter; i++)
        {
            int factor = (int)(((System.currentTimeMillis() + (85 * i)) / 85) % 6);
            int adjust_factor = (int)(((System.currentTimeMillis() + 85 * i) / (85*6)) % 2);
            g.drawImage(
                rainSprite[factor],
                50 + 250 * (i % 8) + 150 * adjust_factor,
                200 * (i % 5) + 20 * (i / 7) - 150 * adjust_factor,
                rainSprite[0].getWidth(null) * graphicsScaling,
                rainSprite[0].getHeight(null) * graphicsScaling,
                canvas
            );
        }

        g.setColor(new Color(0, 0.25f, 0.5f, 0.25f));
        g.fillRect(0, 0, width, height);
        
    }

    /**
     * For views where you scroll through objects, like the Pokedex or Inventory,
     * this function calculates which range of objects should be displayed
     */
    protected void calcIndices() 
    {
        if (this.model.optionIndex < this.minRenderIndex)
        { 
            this.minRenderIndex = Math.max(this.minRenderIndex - this.model.optionWidth, this.model.optionMin);
        }
        else if (this.model.optionIndex > this.minRenderIndex + this.maxRenderRows * this.model.optionWidth - 1)
        {
            this.minRenderIndex = Math.min(this.minRenderIndex + this.model.optionWidth, this.model.optionMax - 1);
        }

        this.oldOptionIndex = this.model.optionIndex;
    }

    /**
     * @param x the x coordinate of the progress bar
     * @param y the y coordinate of the progress bar
     * @param progress the percent progress of the bar to be filled
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    public void renderProgressBar(int x, int y, double progress, Graphics g, JPanel canvas)
    {
        g.drawImage(this.progressBar, x, y,
            this.progressBar.getWidth(null) * graphicsScaling,
            this.progressBar.getHeight(null) * graphicsScaling,
            canvas);

        g.drawImage(this.progressBarFill, 
            x + 3 * graphicsScaling, 
            y + 2 * graphicsScaling,
            (int)(this.progressBarFill.getWidth(null) * progress * 64.0 * graphicsScaling),
            this.progressBarFill.getHeight(null) * graphicsScaling,
            canvas);
    }
}
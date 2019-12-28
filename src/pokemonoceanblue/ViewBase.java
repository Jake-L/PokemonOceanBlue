package pokemonoceanblue;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ViewBase {
    protected byte graphicsScaling;
    protected int width;
    protected int height;
    protected Image[] textDisplayBox = new Image[9];
    protected Image arrowSprite;

    public ViewBase()
    {       
        ImageIcon ii;

        for (int i = 0; i < 9; i++)
        {
            ii = new ImageIcon("src/battle/TextBox" + i + ".png");
            textDisplayBox[i]  = ii.getImage();
        }

        ii = new ImageIcon("src/inventory/arrow.png");
        arrowSprite = ii.getImage();
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
        //left
        g.drawImage(boxSprite[3],
            x,
            y + boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            boxHeight - boxSize * 2 * graphicsScaling,
            canvas);
        //bottom right
        g.drawImage(boxSprite[8],
            x + boxWidth - boxSize * graphicsScaling,
            y + boxHeight - boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //right
        g.drawImage(boxSprite[5],
            x + boxWidth - boxSize * graphicsScaling,
            y + boxSize * graphicsScaling,
            boxSize * graphicsScaling,
            boxHeight - boxSize * 2 * graphicsScaling,
            canvas);
        //bottom
        g.drawImage(boxSprite[7],
            x + boxSize * graphicsScaling,
            y + boxHeight - boxSize * graphicsScaling,
            boxWidth - boxSize * 2 * graphicsScaling,
            boxSize * graphicsScaling,
            canvas);
        //centre
        g.drawImage(boxSprite[4],
            x + boxSize * graphicsScaling,
            y + boxSize * graphicsScaling,
            boxWidth - boxSize * 2 * graphicsScaling,
            boxHeight - boxSize * 2 * graphicsScaling,
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
        displayTextbox(textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);

        int fontSize = Math.max(16, (int)(height * 0.105));
        int fontSpacing = fontSize * 2 / 3;
        
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);
        g.setFont(font);

        String[] renderText = new String[3];
        renderText[0] = "";
        renderText[1] = "";
        renderText[2] = "";
        String[] splitText = text.replace("$",",").split(" ");
        int index = 0;
        int line = 0;

        // split the string into multiple linse
        while (index < splitText.length)
        {
            // move to next line when end is reached
            if (g.getFontMetrics(font).stringWidth(renderText[line] + splitText[index]) >= width - 16 * graphicsScaling 
                && !renderText[line].equals("")
                && line < 2)
            {
                line++;
            }

            renderText[line] = renderText[line] + splitText[index] + " ";
            index++;
        }

        // display the string
        for (int i = 0; i < renderText.length; i++)
        {
            g.drawString(renderText[i], 
                8 * graphicsScaling, 
                height * 3 / 4 + (3 * graphicsScaling) + fontSpacing * (i+1));
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
    public void render(Graphics g, JPanel canvas) {}
}
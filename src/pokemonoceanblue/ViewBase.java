package pokemonoceanblue;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ViewBase {
    protected byte graphicsScaling;
    protected int width;
    protected int height;
    private Image[] textDisplayBox = new Image[9];
    public ViewBase()
    {       
        ImageIcon ii;

        for (int i = 0; i < 9; i++)
        {
            ii = new ImageIcon("src/battle/TextBox" + i + ".png");
            textDisplayBox[i]  = ii.getImage();
        }
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

    protected void displayText(String text, Graphics g, JPanel canvas)
    {
        //top left
        g.drawImage(textDisplayBox[0],
            0,
            height * 3 / 4,
            8 * graphicsScaling,
            8 * graphicsScaling,
            canvas);
        //top right
        g.drawImage(textDisplayBox[2],
            width - 8 * graphicsScaling,
            height * 3 / 4,
            8 * graphicsScaling,
            8 * graphicsScaling,
            canvas);
        //top
        g.drawImage(textDisplayBox[1],
            8 * graphicsScaling,
            height * 3 / 4,
            width - 16 * graphicsScaling,
            8 * graphicsScaling,
            canvas);
        //bottom left
        g.drawImage(textDisplayBox[6],
            0,
            height - 8 * graphicsScaling,
            8 * graphicsScaling,
            8 * graphicsScaling,
            canvas);
        //left
        g.drawImage(textDisplayBox[3],
            0,
            height * 3 / 4 + 8 * graphicsScaling,
            8 * graphicsScaling,
            height / 4 - 16 * graphicsScaling,
            canvas);
        //bottom right
        g.drawImage(textDisplayBox[8],
            width - 8 * graphicsScaling,
            height - 8 * graphicsScaling,
            8 * graphicsScaling,
            8 * graphicsScaling,
            canvas);
        //right
        g.drawImage(textDisplayBox[5],
            width - 8 * graphicsScaling,
            height * 3 / 4 + 8 * graphicsScaling,
            8 * graphicsScaling,
            height / 4 - 16 * graphicsScaling,
            canvas);
        //bottom
        g.drawImage(textDisplayBox[7],
            8 * graphicsScaling,
            height - 8 * graphicsScaling,
            width - 16 * graphicsScaling,
            8 * graphicsScaling,
            canvas);
        //centre
        g.drawImage(textDisplayBox[4],
            8 * graphicsScaling,
            height * 3 / 4 + 8 * graphicsScaling,
            width - 16 * graphicsScaling,
            height / 4 - 16 * graphicsScaling,
            canvas);

        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 36 * graphicsScaling);
        g.setFont(font);

        String[] renderText; 

        int textWidth = g.getFontMetrics(font).stringWidth(text);

        // manual text wrapping if text is too wide
        if (textWidth > width - 16 * graphicsScaling)
        {
            renderText = new String[2];
            renderText[0] = "";
            renderText[1] = "";
            String[] splitText = text.split(" ");
            int index = 0;
            
            // put the first 1/3 of the words on the first line
            while (index < splitText.length / 3.0)
            {
                renderText[0] = renderText[0] + splitText[index] + " ";
                index++;
            }
            // continue adding to the first line until the edge is reached
            while (g.getFontMetrics(font).stringWidth(renderText[0] + splitText[index]) < width - 16 * graphicsScaling)
            {
                renderText[0] = renderText[0] + splitText[index] + " ";
                index++;
            }
            // add remaining text to the second line
            while (index < splitText.length)
            {
                renderText[1] = renderText[1] + splitText[index] + " ";
                index++;
            }
        }
        else
        {
            renderText = new String[1];
            renderText[0] = text;
        }

        // display the string
        for (int i = 0; i < renderText.length; i++)
        {
            g.drawString(renderText[i], 
                8 * graphicsScaling, 
                height * 3 / 4 + 30 * graphicsScaling * (i+1));
        }
    }

    // render function that gets implemented by extended class
    public void render(Graphics g, JPanel canvas) {}
}
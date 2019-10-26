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

        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 36 * graphicsScaling));

        // convert to 
        Graphics2D g2d = (Graphics2D) g;

        // display the string
        g2d.drawString(text, 
            8 * graphicsScaling, 
            height * 3 / 4 + 28 * graphicsScaling);
    }

    // render function that gets implemented by extended class
    public void render(Graphics g, JPanel canvas) {}
}
package pokemonoceanblue;

import java.awt.Graphics;
import javax.swing.JPanel;

public class ViewBase {
    protected byte graphicsScaling;
    protected int width;
    protected int height;

    public ViewBase(){}

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

    // render function that gets implemented by extended class
    public void render(Graphics g, JPanel canvas) {}
}
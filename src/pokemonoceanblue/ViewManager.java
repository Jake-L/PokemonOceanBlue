package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.JPanel;

public class ViewManager extends JPanel {

    private ViewBase view;
    private ViewBase oldView;
    private byte graphicsScaling = 3;
    private int width;
    private int height;
    
    /** 
     * Constructor
     */
    public ViewManager(){
        
    }
    
    /** 
     * Sets variables for rendering on screen
     * @param graphicsScaling a factor to multiply by all measurements to fit the screen
     * @param width width of the screen in pixels
     * @param height height of the screen in pixels
     */
    public void setViewSize(byte graphicsScaling, int width, int height){
        this.graphicsScaling = graphicsScaling;
        this.width = width;
        this.height = height;
    }
    
    /** 
     * @param view the current OverworldView to be rendered
     */
    public void setView(ViewBase view) {
        this.view = view;
        view.setViewSize(graphicsScaling, width, height);
    }

    /** 
     * Render the current view
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // render overworld graphics
        view.render(g, this);

        // update the display
        Toolkit.getDefaultToolkit().sync();
    }

    public void render(){
        repaint();
    }

    public String getCurrentView()
    {
        return this.view.toString();
    }
}
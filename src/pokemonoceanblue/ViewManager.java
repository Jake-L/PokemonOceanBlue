package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.JPanel;

public class ViewManager extends JPanel {

    private OverworldView view;
    private byte graphics_scaling = 3;
    private int width;
    private int height;
    
    /** 
     * Constructor
     */
    public ViewManager(){
        
    }
    
    /** 
     * Sets variables for rendering on screen
     * @param graphics_scaling a factor to multiply by all measurements to fit the screen
     * @param width width of the screen in pixels
     * @param height height of the screen in pixels
     */
    public void setViewSize(byte graphics_scaling, int width, int height){
        this.graphics_scaling = graphics_scaling;
        this.width = width;
        this.height = height;
    }

    
    /** 
     * @param view the current OverworldView to be rendered
     */
    public void setView(OverworldView view) {
        this.view = view;
        view.setViewSize(graphics_scaling, width, height);
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
}
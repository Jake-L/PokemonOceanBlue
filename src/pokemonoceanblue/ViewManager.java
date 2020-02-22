package pokemonoceanblue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.JPanel;

public class ViewManager extends JPanel {

    private BaseView view;
    private BaseView newView;
    private byte graphicsScaling;
    private int width;
    private int height;
    private int transitionCounter = 0;
    private int TRANSITION_MAX = 60;
    
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
    public void setViewSize(int width, int height){
        this.graphicsScaling = (byte) Math.max(Math.min(width, height) / 250, 1);
        this.width = width;
        this.height = height;

        if (this.view != null)
        {
            this.view.setViewSize(this.graphicsScaling, this.width, this.height);

            if (this.newView != null)
            {
                this.newView.setViewSize(this.graphicsScaling, this.width, this.height);
            }
        }
    }
    
    /** 
     * @param view the current view to be rendered
     */
    public void setView(BaseView view) 
    {
        if (this.newView != null)
        {
            // can't change the view while it is already being changed
            return;
        }
        if (this.view != null && 
            (this.view.toString().equals("TitleScreenView")
            || (this.view.toString().equals("OverworldView") && view.toString().equals("BattleView"))
            || (this.view.toString().equals("BattleView") && view.toString().equals("OverworldView")
            || (this.view.toString().equals("OverworldView") && view.toString().equals("OverworldView")))))
        {
            this.newView = view;
            this.transitionCounter = this.TRANSITION_MAX;
            this.newView.setViewSize(this.graphicsScaling, this.width, this.height);
        }
        else
        {
            this.view = view;
        }
        this.view.setViewSize(this.graphicsScaling, this.width, this.height);
    }

    /** 
     * Render the current view
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // render overworld graphics
        view.render(g, this);

        // display transitions between views
        if (this.transitionCounter > 0)
        {
            Color colour;

            if (this.transitionCounter > this.TRANSITION_MAX / 2)
            {
                colour = new Color(0, 0, 0, 255 - (this.transitionCounter - this.TRANSITION_MAX / 2) * 255 / (this.TRANSITION_MAX / 2));
            }
            else if (this.transitionCounter == this.TRANSITION_MAX / 2)
            {
                colour = new Color(0, 0, 0, 255);
                this.view = this.newView;
                this.newView = null;
            }
            else
            {
                colour = new Color(0, 0, 0, this.transitionCounter * 255 / (this.TRANSITION_MAX / 2));
            }

            g.setColor(colour);
            g.fillRect(0, 0, width, height);
            this.transitionCounter--;
        }

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

    public boolean previousViewComplete()
    {
        if (this.newView == null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
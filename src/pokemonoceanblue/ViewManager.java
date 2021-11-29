package pokemonoceanblue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import javax.swing.JPanel;

public class ViewManager extends JPanel {

    private static final long serialVersionUID = -5660188134766830479L;
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
    public ViewManager(BaseView initialView, int width, int height){
        this.setViewSize(width, height);
        this.setView(initialView);
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
            (this.view.getClass().getSimpleName().equals("TitleScreenView")
            || (this.view.getClass().getSimpleName().equals("OverworldView") && view.getClass().getSimpleName().equals("BattleView"))
            || (this.view.getClass().getSimpleName().equals("BattleView") && view.getClass().getSimpleName().equals("OverworldView")
            || (this.view.getClass().getSimpleName().equals("OverworldView") && view.getClass().getSimpleName().equals("OverworldView")))))
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

    public Color updateTransition()
    {
        Color colour;

        if (this.transitionCounter <= 0)
        {
            return null;
        }
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

        this.transitionCounter--;

        return colour;
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
            Color colour = updateTransition();
            g.setColor(colour);
            g.fillRect(0, 0, width, height);
        }

        // update the display
        Toolkit.getDefaultToolkit().sync();
    }

    public void render(){
        repaint();
    }

    public String getCurrentView()
    {
        return this.view.getClass().getSimpleName();
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
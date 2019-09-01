package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the overworld
 */
public class TitleScreenView extends ViewBase {

    private Image[] sprite = new Image[2];
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public TitleScreenView(){
        loadImage();
    }

    /** 
     * loads all the necessary sprites
     */
    private void loadImage() {
        ImageIcon ii = new ImageIcon("src/pokemon/9.png");
        sprite[0] = ii.getImage();

        ii = new ImageIcon("src/pokemon/9b.png");
        sprite[1] = ii.getImage();
    }

    /** 
     * renders the overworld graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) {
        byte i = (byte)(System.currentTimeMillis() / 500 % 2);

        // draw the player
        g.drawImage(sprite[i], 
                    width / 2 - sprite[i].getWidth(null) * graphicsScaling / 2, 
                    height / 2 - sprite[i].getHeight(null) * graphicsScaling / 2, 
                    sprite[i].getWidth(null) * graphicsScaling * 2, 
                    sprite[i].getHeight(null) * graphicsScaling * 2, 
                    canvas);        
    }

    @Override
    public String toString(){
        return "TitleScreen";
    }
}
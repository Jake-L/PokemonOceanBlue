package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the overworld
 */
public class TitleScreenView extends BaseView {

    private Image[] blastoiseSprite = new Image[2];
    private Image[] title = new Image[6];
    
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
        ImageIcon ii = new ImageIcon("src/pokemon/frame0/9.png");
        blastoiseSprite[0] = ii.getImage();

        ii = new ImageIcon("src/pokemon/frame1/9.png");
        blastoiseSprite[1] = ii.getImage();

        for (int i = 0; i < title.length; i++)
        {
            ii = new ImageIcon("src/titleScreen/Pokemon" + i + ".png");
            title[i] = ii.getImage();
        }
    }

    /** 
     * renders the overworld graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) {
        byte i = (byte)(System.currentTimeMillis() / 500 % 2);

        // display blastoise
        g.drawImage(blastoiseSprite[i], 
                    width / 2 - blastoiseSprite[i].getWidth(null) * graphicsScaling / 2, 
                    height / 2 - blastoiseSprite[i].getHeight(null) * graphicsScaling / 2, 
                    blastoiseSprite[i].getWidth(null) * graphicsScaling * 2, 
                    blastoiseSprite[i].getHeight(null) * graphicsScaling * 2, 
                    canvas);    
        
        i = (byte)(System.currentTimeMillis() / 50 % 18);
        
        if (i > 5)
        {
            i = 0;
        }
        
        // display Pokemon logo
        g.drawImage(title[i],
                    width / 2 - title[i].getWidth(null) * graphicsScaling /2,
                    height / 4 - title[i].getHeight(null) * graphicsScaling /2,
                    title[i].getWidth(null) * graphicsScaling, 
                    title[i].getHeight(null) * graphicsScaling, 
                    canvas);

        this.displayText("Welcome to Pokemon OceanBlue", g, canvas);
    }

    @Override
    public String toString(){
        return "TitleScreenView";
    }
}
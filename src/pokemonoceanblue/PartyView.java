package pokemonoceanblue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the overworld
 */
public class PartyView extends ViewBase {

    private Image[] pokemonSprite;
    private Image[] healthBarFill = new Image[3];
    private PartyModel model;
    private Image[] pokemonWindows = new Image[4];
    private Image hpBar;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public PartyView(PartyModel model)
    {
        this.model = model;
        pokemonSprite = new Image[model.team.length];
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprite icons
     */
    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < this.model.team.length; i++)
        {
            ii = new ImageIcon("src/pokemonicons/" + this.model.team[i].id + ".png");
            this.pokemonSprite[i]  = ii.getImage();
        }

        for (int i = 0; i < this.healthBarFill.length; i++)
        {  
            ii = new ImageIcon("src/battle/hp" + i + ".png");
            this.healthBarFill[i] = ii.getImage();
        }

        for (int i = 0; i < this.pokemonWindows.length; i++)  
        {
            ii = new ImageIcon("src/menus/party" + i + ".png");
            this.pokemonWindows[i] = ii.getImage();
        }

        ii = new ImageIcon("src/menus/hpBar.png");
        this.hpBar = ii.getImage();
    }

    /** 
     * renders the party screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 18 * graphicsScaling));

        //display pokemon windows
        for (int i = 0; i < 6; i++)
        {
            if (i < this.model.team.length)
            {
                g.drawImage(pokemonWindows[0],
                    (i % 2) * (width / 3 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    (i / 2) * (height / 4 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    width / 3,
                    height / 4,
                    canvas);
            }

            else
            {
                g.drawImage(pokemonWindows[3],
                    (i % 2) * (width / 3 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    (i / 2) * (height / 4 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    width / 3,
                    height / 4,
                    canvas);
            }
        }

        for (int i = 0; i < this.model.team.length; i++)
        {
            // display the Pokemon's icons
            g.drawImage(pokemonSprite[i], 
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 8 * graphicsScaling, 
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 8 * graphicsScaling, 
                pokemonSprite[i].getWidth(null) * graphicsScaling * 2, 
                pokemonSprite[i].getHeight(null) * graphicsScaling * 2, 
                canvas);  
            
            //display text with pokemon name
            g.drawString(this.model.team[i].name, 
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 78 * graphicsScaling, 
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 34 * graphicsScaling);

            //display hp bars
            g.drawImage(this.hpBar,
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 78 * graphicsScaling,
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 40 * graphicsScaling,
                this.hpBar.getWidth(null) * graphicsScaling,
                this.hpBar.getHeight(null) * graphicsScaling,
                canvas);
            
            //get health bar colour
            byte healthBarFillIndex = 0;
            if((double)this.model.team[i].currentHP / this.model.team[i].stats[Stat.HP] < 0.2)
            {
                healthBarFillIndex = 2;
            }
            else if((double)this.model.team[i].currentHP / this.model.team[i].stats[Stat.HP] < 0.5)
            {
                healthBarFillIndex = 1;
            }
            
            //fill health bars
            g.drawImage(healthBarFill[healthBarFillIndex],
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 94 * graphicsScaling, 
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 42 * graphicsScaling,
                (int)Math.ceil(healthBarFill[0].getWidth(null) * (this.model.team[i].currentHP * 48.0 / this.model.team[i].stats[Stat.HP]) * graphicsScaling),
                healthBarFill[0].getHeight(null) * graphicsScaling,
                canvas);
        }
    }

    @Override
    public String toString()
    {
        return "PartyView";
    }
}
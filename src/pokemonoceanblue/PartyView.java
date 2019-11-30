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

    private PokemonModel[] model;
    private Image[] pokemonSprite;
    private Image healthBar;
    private Image[] healthBarFill = new Image[3];
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public PartyView(PokemonModel[] model){
        this.model = model;
        pokemonSprite = new Image[model.length];
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprite icons
     */
    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < model.length; i++)
        {
            ii = new ImageIcon("src/pokemonicons/" + model[i].id + ".png");
            pokemonSprite[i]  = ii.getImage();
        }

        ii = new ImageIcon("src/battle/hpBarE.png");
        healthBar = ii.getImage();

         for (int i = 0; i < 3; i++)
         {  
            ii = new ImageIcon("src/battle/hp" + i + ".png");
            healthBarFill[i] = ii.getImage();
         }
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

        for (int i = 0; i < model.length; i++)
        {
            // display the Pokemon's icons
            g.drawImage(pokemonSprite[i], 
                width * (1 + (i%3)*2) / 6 - (pokemonSprite[i].getWidth(null) * graphicsScaling / 2), 
                height * ((i/3)*2) / 4, 
                pokemonSprite[i].getWidth(null) * graphicsScaling * 2, 
                pokemonSprite[i].getHeight(null) * graphicsScaling * 2, 
                canvas);  
            
            //display text with pokemon name and hp
            g.drawString(model[i].name + "    HP" + model[i].currentHP + "/" + model[i].stats[Stat.HP], 
                width * (1 + (i%3)*2) / 6 - (pokemonSprite[i].getWidth(null) * graphicsScaling / 2), 
                height * (1 + (i/3)*2) / 4 - (pokemonSprite[i].getHeight(null) * graphicsScaling / 2));
            
            //display healthbars
            g.drawImage(healthBar,
                width * (1 + (i%3)*2) / 6 - (healthBar.getWidth(null) * graphicsScaling), 
                height * (1 + (i/3)*2) / 4 - (healthBar.getHeight(null) * graphicsScaling / 2),
                healthBar.getWidth(null) * graphicsScaling * 2, 
                healthBar.getHeight(null) * graphicsScaling * 2, 
                canvas);
            
            //get health bar colour
            byte healthBarFillIndex = 0;
            if((double)model[i].currentHP / model[i].stats[Stat.HP] < 0.2)
            {
                healthBarFillIndex = 2;
            }
            else if((double)model[i].currentHP / model[i].stats[Stat.HP] < 0.5)
            {
                healthBarFillIndex = 1;
            }
            
            //fill health bars
            g.drawImage(healthBarFill[healthBarFillIndex],
                width * (1 + (i%3)*2) / 6 - (healthBar.getWidth(null) * graphicsScaling) + 16 * graphicsScaling * 2, 
                height * (1 + (i/3)*2) / 4 - (healthBar.getHeight(null) * graphicsScaling / 2) + 2 * graphicsScaling * 2,
                (int)Math.ceil(healthBarFill[0].getWidth(null) * (model[i].currentHP * 48.0 / model[i].stats[Stat.HP]) * graphicsScaling * 2),
                healthBarFill[0].getHeight(null) * graphicsScaling * 2,
                canvas);
        }
    }

    @Override
    public String toString()
    {
        return "PartyView";
    }
}
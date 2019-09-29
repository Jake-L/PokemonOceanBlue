package pokemonoceanblue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the overworld
 */
public class PartyView extends ViewBase {

    private PokemonModel[] model;
    private Image[] pokemonSprite;
    
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
    }

    /** 
     * renders the party screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        for (int i = 0; i < model.length; i++)
        {
            // display the Pokemon's icons
            g.drawImage(pokemonSprite[i], 
                width * (1 + (i%3)*2) / 6 - (pokemonSprite[i].getWidth(null) * graphicsScaling / 2), 
                height * (1 + (i/3)*2) / 4 - (pokemonSprite[i].getHeight(null) * graphicsScaling / 2), 
                pokemonSprite[i].getWidth(null) * graphicsScaling * 2, 
                pokemonSprite[i].getHeight(null) * graphicsScaling * 2, 
                canvas);  
        }
    }

    @Override
    public String toString(){
        return "PartyView";
    }
}
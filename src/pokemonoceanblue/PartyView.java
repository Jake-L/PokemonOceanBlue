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
        for (int i = 0; i < model.length; i++)
        {

        }
    }
}
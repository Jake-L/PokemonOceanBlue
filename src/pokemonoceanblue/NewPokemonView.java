package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the overworld
 */
public class NewPokemonView extends ViewBase {

    private Image[] pokemonSprite;
    private NewPokemonModel model;
    private Image background;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the overworld to be displayed
     * @param playerModel model for the player to display it and calculate screen offset
     */
    public NewPokemonView(NewPokemonModel model)
    {
        this.model = model;
        this.pokemonSprite = new Image[this.model.pokemon.length];
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage() 
    {
        ImageIcon ii = new ImageIcon("src/menus/pokemonBackground.png");
        this.background = ii.getImage();

        for (int i = 0; i < this.pokemonSprite.length; i++)
        {
            ii = new ImageIcon("src/pokemon/frame0/" + this.model.pokemon[i].id + ".png");
            this.pokemonSprite[i]  = ii.getImage();
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
        int multiplier = (int)Math.ceil(Math.max(width / this.background.getWidth(null), height / this.background.getHeight(null)));

        g.drawImage(
            background,
            width / 2 - background.getWidth(null) * multiplier / 2,
            height / 2 - background.getHeight(null) * multiplier / 2,
            this.background.getWidth(null) * multiplier,
            this.background.getHeight(null) * multiplier,
            canvas
        );

        int renderIndex = this.pokemonSprite.length - 1;

        if (this.model.counter > 60)
        {
            renderIndex = 0;
        }

        g.drawImage(
            this.pokemonSprite[renderIndex],
            width / 2 - this.pokemonSprite[0].getWidth(null) / 2 * graphicsScaling,
            height / 2 - this.pokemonSprite[0].getHeight(null) / 2 * graphicsScaling,
            this.pokemonSprite[0].getWidth(null) * graphicsScaling,
            this.pokemonSprite[0].getHeight(null) * graphicsScaling,
            canvas
        );

        this.displayText(this.model.text, g, canvas);
    }

    @Override
    public String toString()
    {
        return "NewPokemonView";
    }
}
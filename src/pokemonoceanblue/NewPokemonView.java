package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.awt.AlphaComposite;

/** 
 * Renders the overworld
 */
public class NewPokemonView extends ViewBase {

    private Image[] pokemonSprite;
    private BufferedImage[] pokemonBufferedSprite;
    
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
        this.pokemonBufferedSprite = new BufferedImage[this.model.pokemon.length];
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
            this.pokemonSprite[i] = ii.getImage();
            try
            {
                // load a copy of the Pokemon's sprite recoloured white
                this.pokemonBufferedSprite[i] = ImageIO.read(new File("src/pokemon/frame0/" + this.model.pokemon[i].id + ".png"));   
                colorImage(this.pokemonBufferedSprite[i]);
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemon/frame0/" + this.model.pokemon[i].id + ".png");
            }
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

        // fade the pokemon to white during evolution animation
        if (this.pokemonBufferedSprite.length > 1)
        {
            float opacity = 1.0f - (Math.abs(this.model.counter - 60) / 60.0f);
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.drawImage(
                this.pokemonBufferedSprite[renderIndex],
                width / 2 - this.pokemonSprite[0].getWidth(null) / 2 * graphicsScaling,
                height / 2 - this.pokemonSprite[0].getHeight(null) / 2 * graphicsScaling,
                this.pokemonSprite[0].getWidth(null) * graphicsScaling,
                this.pokemonSprite[0].getHeight(null) * graphicsScaling,
                canvas
            );
        }
        
    }

    @Override
    public String toString()
    {
        return "NewPokemonView";
    }

    /**
     * @param image image to be recoloured white
     */
    private void colorImage(BufferedImage image) 
    {
        WritableRaster raster = image.getRaster();

        for (int x = 0; x < image.getWidth(); x++) 
        {
            for (int y = 0; y < image.getHeight(); y++) 
            {
                int[] pixels = raster.getPixel(x, y, (int[]) null);
                pixels[0] = 255;
                pixels[1] = 255;
                pixels[2] = 255;
                raster.setPixel(x, y, pixels);
            }
        }
    }
}
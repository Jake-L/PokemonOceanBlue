package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** 
 * Renders the Pokedex
 */
public class PokedexView extends BaseView 
{
    private BufferedImage[] pokemonIconSprite = new BufferedImage[506];
    private Image[] indexHighlight = new Image[2];
    private BufferedImage pokemonSprite;
    private PokedexModel model;
    private Image background;
    private Image pokemonBackground;
    private int oldOptionIndex;
    
    /** 
     * Constructor for the Pokedex view
     * @param model model containing the Pokedex data to be displayed
     */
    public PokedexView(PokedexModel model)
    {
        super(model);
        this.model = model;
        this.loadImage();
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage() 
    {
        this.loadHoveredPokemon();

        for (int i = 0; i < pokemonIconSprite.length; i++)
        {
            try 
            {
                this.pokemonIconSprite[i] = ImageIO.read(this.getClass().getResource("/pokemonicons/" + i + ".png"));
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemonicons/" + i + ".png");
            }
            
        }

        ImageIcon ii;

        for (int i = 0; i < indexHighlight.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/selection" + i + ".png"));
            this.indexHighlight[i]  = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/menus/redBackground.png"));
        this.background = ii.getImage();

        ii = new ImageIcon(this.getClass().getResource("/menus/pokedexPokemonBackground.png"));
        this.pokemonBackground = ii.getImage();

        for (int i = 0; i < pokemonIconSprite.length; i++)
        {
            // turn the icon black if they player hasn't caught the Pokemon
            if (this.model.caughtPokemon[i] == 0)
            {
                this.pokemonIconSprite[i] = this.colorImage(this.pokemonIconSprite[i], Color.BLACK.getRGB());
            }
        }
    }

    private void loadHoveredPokemon()
    {
        try 
        {
            this.pokemonSprite = ImageIO.read(this.getClass().getResource("/pokemoncentered/frame0/" + this.model.optionIndex + ".png"));

            // turn the sprite black if they player hasn't caught the Pokemon
            if (this.model.caughtPokemon[this.model.optionIndex] == 0)
            {
                this.pokemonSprite = this.colorImage(this.pokemonSprite, Color.BLACK.getRGB());
            }
        }
        catch (IOException e)
        {
            System.out.println("Error loading /pokemoncentered/frame0/" + this.model.optionIndex + ".png");
        }
    }

    /** 
     * renders the pokedex screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        int fontSize = 12 * graphicsScaling;
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
        g.setFont(font);

        int iconHeight = this.pokemonIconSprite[0].getHeight(null) * graphicsScaling;
        int iconWidth = this.pokemonIconSprite[0].getWidth(null) * graphicsScaling;
        int sideBarWidth = 100 * graphicsScaling;

        this.maxRenderRows = height / (iconHeight);
        this.model.optionWidth = (width - sideBarWidth) / (iconWidth);
        this.model.optionHeight = this.model.optionMax / this.model.optionWidth;

        if (this.oldOptionIndex != this.model.optionIndex)
        {
            this.calcIndices();
            this.loadHoveredPokemon();
        }

        //draw the background
        int blockSize = background.getWidth(null) * graphicsScaling;
        for (int i = 0; i <= width / blockSize; i++)
        {
            for (int j = 0; j <= height / blockSize; j++)
            {
                g.drawImage(background, i * blockSize, j * blockSize, blockSize, blockSize, canvas);
            }
        }

        for (int i = 0; i < this.maxRenderRows; i++)
        {
            for (int j = 0; j < this.model.optionWidth; j++)
            {
                if (this.minRenderIndex + (i * this.model.optionWidth) + j < this.pokemonIconSprite.length)
                {
                    g.drawImage(
                        this.pokemonIconSprite[this.minRenderIndex + (i * this.model.optionWidth) + j],
                        sideBarWidth + j * iconWidth,
                        i * iconHeight,
                        iconWidth,
                        iconHeight,
                        canvas
                    );
                }
            }
        }

        // draw a box showing the current selection
        g.drawImage(
            this.indexHighlight[(int)(System.currentTimeMillis() / 500 % 2)],
            sideBarWidth + (this.model.optionIndex - this.minRenderIndex) % this.model.optionWidth * iconWidth - (graphicsScaling * 2),
            (this.model.optionIndex - this.minRenderIndex) / this.model.optionWidth * iconHeight - (graphicsScaling * 2),
            this.indexHighlight[0].getWidth(null) * graphicsScaling,
            this.indexHighlight[0].getHeight(null) * graphicsScaling,
            canvas
        );

        // display a box around the Pokemon sprite
        g.drawImage(
            this.pokemonBackground,
            2 * graphicsScaling,
            20 * graphicsScaling,
            this.pokemonBackground.getWidth(null) * graphicsScaling,
            this.pokemonBackground.getHeight(null) * graphicsScaling,
            canvas
        );
        
        // display the full sprite of the Pokemon being hovered
        g.drawImage(
            this.pokemonSprite,
            10 * graphicsScaling,
            28 * graphicsScaling,
            this.pokemonSprite.getWidth(null) * graphicsScaling,
            this.pokemonSprite.getHeight(null) * graphicsScaling,
            canvas
        );

        // display the Pokemon's description
        if (this.model.caughtPokemon[this.model.optionIndex] > 0)
        {
            this.displayText(
                this.model.pokemonDescription[this.model.optionIndex],
                fontSize,
                0, 
                height / 2, 
                sideBarWidth, 
                height * 4 / 5, 
                g, 
                canvas
            );
        }
    
        // display the number of unique pokemon caught
        g.drawString(
            "Species Caught: " + String.valueOf(this.model.uniqueCaught) + "/" + String.valueOf(pokemonIconSprite.length),
            2 * graphicsScaling,
            12 * graphicsScaling
        );

        int numberCaught = this.model.caughtPokemon[this.model.optionIndex];

        g.drawString(
            "Number Caught: " + String.valueOf(numberCaught),
            2 * graphicsScaling,
            height * 18 / 20
        );

        g.drawString(
            "Shiny Rate: " + String.format("%.2f", this.model.getShinyRate(this.model.optionIndex) * 100) + "%",
            2 * graphicsScaling,
            height * 19 / 20
        );

        //this.displayText(this.model.text, g, canvas);
    }
}
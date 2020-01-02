package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.image.ColorConvertOp;
import java.awt.color.ColorSpace;

/** 
 * Renders the Pokedex
 */
public class PokedexView extends ViewBase {

    private BufferedImage[] pokemonIconSprite = new BufferedImage[494];
    private Image[] indexHighlight = new Image[2];
    private Image pokemonSprite;
    private PokedexModel model;
    private Image background;
    private int oldOptionIndex;
    private int minIndex = 1;
    private int rowCount;
    
    /** 
     * Constructor for the Pokedex view
     * @param model model containing the Pokedex data to be displayed
     */
    public PokedexView(PokedexModel model)
    {
        this.model = model;
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage() 
    {
        ImageIcon ii = new ImageIcon("src/pokemon/frame0/" + this.model.optionIndex + ".png");
        this.pokemonSprite  = ii.getImage();

        for (int i = 0; i < pokemonIconSprite.length; i++)
        {
            try 
            {
                this.pokemonIconSprite[i] = ImageIO.read(new File("src/pokemonicons/" + i + ".png"));              
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemonicons/" + i + ".png");
            }
            
        }

        for (int i = 0; i < indexHighlight.length; i++)
        {
            ii = new ImageIcon("src/menus/selection" + i + ".png");
            this.indexHighlight[i]  = ii.getImage();
        }

        ii = new ImageIcon("src/menus/redBackground.png");
        this.background = ii.getImage();

        // converting all the images to greyscale takes about 2 seconds, so run it in a separate thread
        new Thread(() -> {
            for (int i = 0; i < pokemonIconSprite.length; i++)
            {
                if (this.model.caughtPokemon[i] == 0)
                {
                    BufferedImage out = new BufferedImage(this.pokemonIconSprite[i].getWidth(), this.pokemonIconSprite[i].getHeight(), BufferedImage.TYPE_INT_ARGB);
                    ColorConvertOp op = new ColorConvertOp(this.pokemonIconSprite[i].getColorModel().getColorSpace(), ColorSpace.getInstance(ColorSpace.CS_GRAY),  null);
                    op.filter(this.pokemonIconSprite[i], out);
                    this.pokemonIconSprite[i] = out; 
                }
            }
        }).start();
    }

    /** 
     * renders the pokedex screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 12 * graphicsScaling);      
        g.setFont(font);

        int iconHeight = this.pokemonIconSprite[0].getHeight(null) * graphicsScaling;
        int iconWidth = this.pokemonIconSprite[0].getWidth(null) * graphicsScaling;

        this.rowCount = height / (iconHeight);
        this.model.pokemonPerRow = (width * 4 / 5) / (iconWidth);

        // increase minIndex if screen size change causes the current index to be pushed off the bottom of the screen
        if (this.minIndex < this.model.optionIndex - this.rowCount * this.model.pokemonPerRow)
        {
            this.minIndex += this.model.pokemonPerRow;
        }

        if (this.oldOptionIndex != this.model.optionIndex)
        {
            this.calcIndices();
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

        for (int i = 0; i < this.rowCount; i++)
        {
            for (int j = 0; j < this.model.pokemonPerRow; j++)
            {
                if (this.minIndex + (i * this.model.pokemonPerRow) + j < this.pokemonIconSprite.length)
                {
                    g.drawImage(
                        this.pokemonIconSprite[this.minIndex + (i * this.model.pokemonPerRow) + j],
                        width / 5 + j * iconWidth,
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
            width / 5 + (this.model.optionIndex - this.minIndex) % this.model.pokemonPerRow * iconWidth - (graphicsScaling * 2),
            (this.model.optionIndex - this.minIndex) / this.model.pokemonPerRow * iconHeight - (graphicsScaling * 2),
            this.indexHighlight[0].getWidth(null) * graphicsScaling,
            this.indexHighlight[0].getHeight(null) * graphicsScaling,
            canvas
        );

        // display the full sprite of the Pokemon being hovered
        g.drawImage(
            this.pokemonSprite,
            2 * graphicsScaling,
            height / 2 - this.pokemonSprite.getHeight(null) / 2 * graphicsScaling,
            this.pokemonSprite.getWidth(null) * graphicsScaling,
            this.pokemonSprite.getHeight(null) * graphicsScaling,
            canvas
        );

        // display the number of unique pokemon caught
        g.drawString(
            "Species Caught: " + String.valueOf(this.model.uniqueCaught),
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
            "Shiny Rate: " + String.format("%.3f", (Math.log10(numberCaught + 0.1) + 1) / 100) + "%",
            2 * graphicsScaling,
            height * 19 / 20
        );

        //this.displayText(this.model.text, g, canvas);
    }

    private void calcIndices()
    {
        if (this.model.optionIndex < this.minIndex)
        { 
            this.minIndex = Math.max(this.minIndex - this.model.pokemonPerRow, 1);
        }
        else if (this.model.optionIndex > this.minIndex + this.rowCount * this.model.pokemonPerRow - 1)
        {
            this.minIndex = Math.min(this.minIndex + this.model.pokemonPerRow, this.model.caughtPokemon.length - 1);
        }
        this.oldOptionIndex = this.model.optionIndex;
        ImageIcon ii = new ImageIcon("src/pokemon/frame0/" + this.model.optionIndex + ".png");
        this.pokemonSprite  = ii.getImage();
    }

    @Override
    public String toString()
    {
        return "PokedexView";
    }
}
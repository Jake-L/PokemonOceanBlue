package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

/** 
 * Renders the Pokemon Storage
 */
public class PokemonStorageView extends BaseView {

    private Map<Integer, Image> pokemonIconSprite = new HashMap<Integer, Image>();
    private Image[] cursorSprite = new Image[3];
    private Image[] storageBorder = new Image[9];
    private Image[] partyBorder = new Image[9];
    private Image pokemonSprite;
    private Image pokemonIconBorder;
    private PokemonStorageModel storageModel;
    private PartyModel partyModel;
    private Image background;
    private int oldOptionIndex;
    private int oldPartyOptionIndex;
    private int minIndex = 0;
    private int rowCount;
    
    /** 
     * Constructor for the Pokemon Storage view
     * @param model model containing the Pokedex data to be displayed
     */
    public PokemonStorageView(PokemonStorageModel storageModel, PartyModel partyModel)
    {
        this.storageModel = storageModel;
        this.partyModel = partyModel;
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage() 
    {
        ImageIcon ii = new ImageIcon("src/pokemon/frame0/" + this.partyModel.team.get(0).id + ".png");
        this.pokemonSprite  = ii.getImage();

        // load team icons
        for (PokemonModel pokemon : this.partyModel.team)
        {
            if (this.pokemonIconSprite.get(pokemon.id) == null)
            {
                ii = new ImageIcon("src/pokemonicons/" + pokemon.id + ".png");
                this.pokemonIconSprite.put(pokemon.id, ii.getImage());
            }
        }

        // load storage Pokemon icons
        for (PokemonModel pokemon : this.storageModel.pokemonStorage)
        {
            if (this.pokemonIconSprite.get(pokemon.id) == null)
            {
                ii = new ImageIcon("src/pokemonicons/" + pokemon.id + ".png");
                this.pokemonIconSprite.put(pokemon.id, ii.getImage());
            }
        }

        for (int i = 0; i < storageBorder.length; i++)
        {
            ii = new ImageIcon("src/menus/pcborder" + i + ".png");
            storageBorder[i]  = ii.getImage();

            ii = new ImageIcon("src/menus/pcPartyBorder" + i + ".png");
            partyBorder[i]  = ii.getImage();
        }

        ii = new ImageIcon("src/menus/storageBackground.png");
        this.background = ii.getImage();

        ii = new ImageIcon("src/menus/pokemonIconBorder.png");
        this.pokemonIconBorder = ii.getImage();

        for (int i = 0; i < this.cursorSprite.length; i++)
        {
            ii = new ImageIcon("src/menus/pcCursor" + i + ".png");
            this.cursorSprite[i] = ii.getImage();
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
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 12 * graphicsScaling);      
        g.setFont(font);

        int iconHeight = this.pokemonIconSprite.get(this.partyModel.team.get(0).id).getHeight(null) * graphicsScaling;
        int iconWidth = this.pokemonIconSprite.get(this.partyModel.team.get(0).id).getWidth(null) * graphicsScaling;

        this.rowCount = height / (iconHeight);
        this.storageModel.optionWidth = (width * 3 / 5) / (iconWidth);
        this.storageModel.optionHeight = this.storageModel.optionMax / this.storageModel.optionWidth;

        // increase minIndex if screen size change causes the current index to be pushed off the bottom of the screen
        if (this.minIndex < this.storageModel.optionIndex - this.rowCount * this.storageModel.optionWidth)
        {
            this.minIndex += this.storageModel.optionWidth;
        }

        if (this.oldOptionIndex != this.storageModel.optionIndex || this.oldPartyOptionIndex != this.partyModel.optionIndex)
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

        // display the full sprite of the Pokemon being hovered
        if (this.pokemonSprite != null)
        {
            g.drawImage(
                this.pokemonSprite,
                width - (8 + this.pokemonSprite.getWidth(null)) * graphicsScaling,
                height / 2 - this.pokemonSprite.getHeight(null) / 2 * graphicsScaling,
                this.pokemonSprite.getWidth(null) * graphicsScaling,
                this.pokemonSprite.getHeight(null) * graphicsScaling,
                canvas
            );
        }

        // display the box holding all the Pokemon in storage
        displayTextbox(
            this.storageBorder, 
            width / 5, 
            height / 20,
            width * 3 / 5,
            height * 9 / 10, 
            g, 
            canvas
        );

        // display the box holding all the Pokemon in party
        displayTextbox(
            this.partyBorder, 
            0, 
            height / 20, 
            width / 6,
            height * 9 / 10, 
            g, 
            canvas
        );

        int heightPerPokemon = ((height * 9 / 10) - 16 * graphicsScaling) / 6;

        // display the player's team
        for (int i = 0; i < this.partyModel.team.size(); i++)
        {
            g.drawImage(
                this.pokemonIconBorder,
                width / 12 - this.pokemonIconBorder.getWidth(null) * graphicsScaling / 2,
                height / 20 + 9 * graphicsScaling + i * heightPerPokemon,
                this.pokemonIconBorder.getWidth(null) * graphicsScaling,
                this.pokemonIconBorder.getHeight(null) * graphicsScaling,
                canvas
            );

            g.drawImage(
                this.pokemonIconSprite.get(this.partyModel.team.get(i).id),
                width / 12 - iconWidth / 2,
                height / 20 + 9 * graphicsScaling + i * heightPerPokemon,
                iconWidth,
                iconHeight,
                canvas
            );
        }

        // draw the pokemon in storage
        for (int i = 0; i < this.rowCount; i++)
        {
            for (int j = 0; j < this.storageModel.optionWidth; j++)
            {
                if (this.minIndex + (i * this.storageModel.optionWidth) + j < this.storageModel.pokemonStorage.size())
                {
                    int pokemonId = this.storageModel.pokemonStorage.get(this.minIndex + (i * this.storageModel.optionWidth) + j).id;
                    g.drawImage(
                        this.pokemonIconSprite.get(pokemonId),
                        width / 5 + j * iconWidth,
                        height / 20 + i * heightPerPokemon,
                        iconWidth,
                        iconHeight,
                        canvas
                    );
                }
            }
        }

        // display cursor
        if (this.storageModel.categoryIndex == 0)
        {
            g.drawImage(
                this.cursorSprite[0],
                iconWidth / 2,
                height / 20 + this.partyModel.optionIndex * heightPerPokemon,
                this.cursorSprite[0].getWidth(null) * graphicsScaling,
                this.cursorSprite[0].getHeight(null) * graphicsScaling,
                canvas
            );

            if (this.storageModel.currentPokemon != null)
            {
                g.drawImage(
                    this.pokemonIconSprite.get(this.storageModel.currentPokemon.id),
                    width / 12 - iconWidth / 2,
                    height / 20 + 9 * graphicsScaling + this.partyModel.optionIndex * heightPerPokemon,
                    iconWidth,
                    iconHeight,
                    canvas
                );
            }
        }
        else
        {
            g.drawImage(
                this.cursorSprite[0],
                width / 5 + (this.storageModel.optionIndex % this.storageModel.optionWidth) * iconWidth,
                height / 20 + (this.storageModel.optionIndex / this.storageModel.optionWidth) * heightPerPokemon - (heightPerPokemon / 2),
                this.cursorSprite[0].getWidth(null) * graphicsScaling,
                this.cursorSprite[0].getHeight(null) * graphicsScaling,
                canvas
            );
            

            if (this.storageModel.currentPokemon != null)
            {
                g.drawImage(
                    this.pokemonIconSprite.get(this.storageModel.currentPokemon.id),
                    width / 5 + (this.storageModel.optionIndex % this.storageModel.optionWidth) * iconWidth,
                    height / 20 + (this.storageModel.optionIndex / this.storageModel.optionWidth) * heightPerPokemon - (heightPerPokemon / 4),
                    iconWidth,
                    iconHeight,
                    canvas
                );
            }
        }
    }

    private void calcIndices()
    {
        if (this.storageModel.optionIndex < this.minIndex)
        { 
            this.minIndex = Math.max(this.minIndex - this.storageModel.optionWidth, 1);
        }
        else if (this.storageModel.optionIndex > this.minIndex + this.rowCount * this.storageModel.optionWidth - 1)
        {
            this.minIndex = Math.min(this.minIndex + this.storageModel.optionWidth, this.storageModel.pokemonStorage.size() - 1);
        }

        this.oldOptionIndex = this.storageModel.optionIndex;
        this.oldPartyOptionIndex = this.partyModel.optionIndex;

        if (this.storageModel.categoryIndex == 0 && this.storageModel.optionIndex < this.storageModel.pokemonStorage.size())
        {
            ImageIcon ii = new ImageIcon("src/pokemon/frame0/" + this.partyModel.team.get(this.partyModel.optionIndex).id + ".png");
            this.pokemonSprite  = ii.getImage();
        }
        else if (this.storageModel.optionIndex < this.storageModel.pokemonStorage.size())
        {
            ImageIcon ii = new ImageIcon("src/pokemon/frame0/" + this.storageModel.pokemonStorage.get(this.storageModel.optionIndex).id + ".png");
            this.pokemonSprite  = ii.getImage();
        }
        else
        {
            this.pokemonSprite = null;
        }
        
    }

    @Override
    public String toString()
    {
        return "PokemonStorageView";
    }
}
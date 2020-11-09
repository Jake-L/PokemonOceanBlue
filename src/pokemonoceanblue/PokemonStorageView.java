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

    private Map<String, Image> pokemonIconSprite = new HashMap<String, Image>();
    private Image[] cursorSprite = new Image[3];
    private Image[] storageBorder = new Image[9];
    private Image[] partyBorder = new Image[9];
    private Image pokemonSprite;
    private Image pokemonIconBorder;
    private PokemonStorageModel storageModel;
    private PartyModel partyModel;
    private Image background;
    private int oldPartyOptionIndex;
    private int oldCategoryIndex;
    private int iconWidth;
    private int iconHeight;
    private int heightPerPokemon;
    
    /** 
     * Constructor for the Pokemon Storage view
     * @param model model containing the Pokedex data to be displayed
     */
    public PokemonStorageView(PokemonStorageModel storageModel, PartyModel partyModel)
    {
        super(storageModel);
        this.storageModel = storageModel;
        this.partyModel = partyModel;
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage() 
    {
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/pokemoncentered/frame0/" + this.partyModel.team.get(0).getSpriteId() + ".png"));
        this.pokemonSprite  = ii.getImage();

        // load team icons
        for (PokemonModel pokemon : this.partyModel.team)
        {
            if (this.pokemonIconSprite.get(pokemon.getSpriteId()) == null)
            {
                ii = new ImageIcon(this.getClass().getResource("/pokemonicons/" + pokemon.getSpriteId() + ".png"));
                this.pokemonIconSprite.put(pokemon.getSpriteId(), ii.getImage());
            }
        }

        // load storage Pokemon icons
        for (PokemonModel pokemon : this.storageModel.pokemonStorage)
        {
            if (this.pokemonIconSprite.get(pokemon.getSpriteId()) == null)
            {
                ii = new ImageIcon(this.getClass().getResource("/pokemonicons/" + pokemon.getSpriteId() + ".png"));
                this.pokemonIconSprite.put(pokemon.getSpriteId(), ii.getImage());
            }
        }

        for (int i = 0; i < storageBorder.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/pcborder" + i + ".png"));
            storageBorder[i]  = ii.getImage();

            ii = new ImageIcon(this.getClass().getResource("/menus/pcPartyBorder" + i + ".png"));
            partyBorder[i]  = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/menus/storageBackground.png"));
        this.background = ii.getImage();

        ii = new ImageIcon(this.getClass().getResource("/menus/pokemonIconBorder.png"));
        this.pokemonIconBorder = ii.getImage();

        for (int i = 0; i < this.cursorSprite.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/pcCursor" + i + ".png"));
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

        if (this.oldOptionIndex != this.storageModel.optionIndex 
            || this.oldPartyOptionIndex != this.partyModel.optionIndex
            || this.oldCategoryIndex != this.storageModel.categoryIndex)
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
                this.pokemonIconSprite.get(this.partyModel.team.get(i).getSpriteId()),
                width / 12 - iconWidth / 2,
                height / 20 + 9 * graphicsScaling + i * heightPerPokemon,
                iconWidth,
                iconHeight,
                canvas
            );
        }

        // draw the pokemon in storage
        for (int i = 0; i < this.maxRenderRows; i++)
        {
            for (int j = 0; j < this.storageModel.optionWidth; j++)
            {
                if (this.minRenderIndex + (i * this.storageModel.optionWidth) + j < this.storageModel.pokemonStorage.size())
                {
                    String pokemonId = this.storageModel.pokemonStorage.get(this.minRenderIndex + (i * this.storageModel.optionWidth) + j).getSpriteId();
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
            if (this.storageModel.currentPokemon != null)
            {
                g.drawImage(
                    this.pokemonIconSprite.get(this.storageModel.currentPokemon.getSpriteId()),
                    width / 12 - iconWidth / 2,
                    height / 20 + 9 * graphicsScaling + this.partyModel.optionIndex * heightPerPokemon - (heightPerPokemon / 4),
                    iconWidth,
                    iconHeight,
                    canvas
                );
            }

            g.drawImage(
                this.cursorSprite[0],
                iconWidth / 2,
                height / 20 + this.partyModel.optionIndex * heightPerPokemon - (heightPerPokemon / 4),
                this.cursorSprite[0].getWidth(null) * graphicsScaling,
                this.cursorSprite[0].getHeight(null) * graphicsScaling,
                canvas
            );
        }
        else
        {
            if (this.storageModel.currentPokemon != null)
            {
                g.drawImage(
                    this.pokemonIconSprite.get(this.storageModel.currentPokemon.getSpriteId()),
                    width / 5 + ((this.storageModel.optionIndex - this.minRenderIndex) % this.storageModel.optionWidth) * iconWidth,
                    height / 20 + ((this.storageModel.optionIndex - this.minRenderIndex) / this.storageModel.optionWidth) * heightPerPokemon - (heightPerPokemon / 4),
                    iconWidth,
                    iconHeight,
                    canvas
                );
            }

            g.drawImage(
                this.cursorSprite[0],
                width / 5 + ((this.storageModel.optionIndex - this.minRenderIndex) % this.storageModel.optionWidth) * iconWidth,
                height / 20 + ((this.storageModel.optionIndex - this.minRenderIndex) / this.storageModel.optionWidth) * heightPerPokemon - (heightPerPokemon / 2),
                this.cursorSprite[0].getWidth(null) * graphicsScaling,
                this.cursorSprite[0].getHeight(null) * graphicsScaling,
                canvas
            );
        }

        // display a pop-up box of text options
        if (this.storageModel.textOptions != null)
        {
            this.displayOptions(this.storageModel.textOptions, this.storageModel.textOptionIndex, g, canvas);
        }
    }

    protected void calcIndices()
    {
        this.iconHeight = this.pokemonIconSprite.get(this.partyModel.team.get(0).getSpriteId()).getHeight(null) * graphicsScaling;
        this.iconWidth = this.pokemonIconSprite.get(this.partyModel.team.get(0).getSpriteId()).getWidth(null) * graphicsScaling;
        this.heightPerPokemon = ((height * 9 / 10) - 16 * graphicsScaling) / 6;

        this.storageModel.optionWidth = (width * 3 / 5) / (iconWidth);
        this.storageModel.optionHeight = this.storageModel.optionMax / this.storageModel.optionWidth;
        this.maxRenderRows = ((height * 17 / 20) / (heightPerPokemon));

        super.calcIndices();
        this.oldPartyOptionIndex = this.partyModel.optionIndex;
        this.oldCategoryIndex = this.storageModel.categoryIndex;

        if (this.storageModel.categoryIndex == 0 && this.partyModel.optionIndex < this.partyModel.team.size())
        {
            ImageIcon ii = new ImageIcon(this.getClass().getResource("/pokemoncentered/frame0/" + this.partyModel.team.get(this.partyModel.optionIndex).getSpriteId() + ".png"));
            this.pokemonSprite  = ii.getImage();
        }
        else if (this.storageModel.categoryIndex == 1 && this.storageModel.optionIndex < this.storageModel.pokemonStorage.size())
        {
            ImageIcon ii = new ImageIcon(this.getClass().getResource("/pokemoncentered/frame0/" + this.storageModel.pokemonStorage.get(this.storageModel.optionIndex).getSpriteId() + ".png"));
            this.pokemonSprite  = ii.getImage();
        }
        else if (this.storageModel.currentPokemon != null)
        {
            ImageIcon ii = new ImageIcon(this.getClass().getResource("/pokemoncentered/frame0/" + this.storageModel.currentPokemon.getSpriteId() + ".png"));
            this.pokemonSprite  = ii.getImage();
        }
        else
        {
            this.pokemonSprite = null;
        }
    }
}
package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the party screen
 */
public class PartyView extends BaseView {

    private Image[] healthBarFill = new Image[3];
    private PartyModel model;
    private Image[] pokemonWindows = new Image[5];
    private Image hpBar;
    private Image[] faintedPokemonWindows = new Image[5];
    private Image background;
    private Image[] statusEffectImages = new Image[8];
    private Map<String, Image> pokemonIconSprite = new HashMap<String, Image>();
    private Map<String, Image> pokemonSprite = new HashMap<String, Image>();
    
    /** 
     * Constructor for the party view
     * @param model model for the party to be displayed
     */
    public PartyView(PartyModel model)
    {
        this.model = model;
        this.loadImage();
    }

    /** 
     * loads all the images used in partyview
     */
    private void loadImage() 
    {
        ImageIcon ii;

        // load team icons
        for (PokemonModel pokemon : this.model.team)
        {
            if (this.pokemonIconSprite.get(pokemon.getSpriteId()) == null)
            {
                ii = new ImageIcon(this.getClass().getResource("/pokemonicons/" + pokemon.getSpriteId() + ".png"));
                this.pokemonIconSprite.put(pokemon.getSpriteId(), ii.getImage());

                ii = new ImageIcon(this.getClass().getResource("/pokemoncentered/frame0/" + pokemon.getSpriteId() + ".png"));
                this.pokemonSprite.put(pokemon.getSpriteId(), ii.getImage());
            }
        }

        for (int i = 0; i < this.healthBarFill.length; i++)
        {  
            ii = new ImageIcon(this.getClass().getResource("/battle/hp" + i + ".png"));
            this.healthBarFill[i] = ii.getImage();
        }

        for (int i = 0; i < this.pokemonWindows.length; i++)  
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/party" + i + ".png"));
            this.pokemonWindows[i] = ii.getImage();
        }

        for (int i = 0; i < this.faintedPokemonWindows.length; i++)  
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/partyfainted" + i + ".png"));
            this.faintedPokemonWindows[i] = ii.getImage();
        }

        //loads status effect images
        for (int i = 0; i < this.statusEffectImages.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/ailment" + (i + 1) + ".png"));
            this.statusEffectImages[i] = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/menus/hpBar.png"));
        this.hpBar = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/inventory/background.png"));
        this.background = ii.getImage();
    }

    /** 
     * renders the party screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        //draw the background
        int blockSize = background.getWidth(null) * graphicsScaling;
        for (int i = 0; i < Math.ceil((double)width / blockSize); i++)
        {
            for (int j = 0; j < Math.ceil((double)height / blockSize); j++)
            {
                g.drawImage(background, i * blockSize, j * blockSize, blockSize, blockSize, canvas);
            }
        }

        // display the currently hovered Pokemon
        if (this.model.team.size() > 0)
        {
            this.renderPokemonSidebar(this.model.team.get(this.model.optionIndex), 
                this.pokemonSprite.get(this.model.team.get(this.model.optionIndex).getSpriteId()), 
                0,
                g, 
                canvas);

            // display the Pokemon's moves
            for (int j = 0; j < this.model.team.get(this.model.optionIndex).moves.length; j++)
            {
                this.renderMove(this.model.team.get(this.model.optionIndex).moves[j], 
                    width * 2 / 3 + 24 * graphicsScaling, 
                    height / 20 + (this.summaryHeader[1].getHeight(null) + this.pokemonBackground[1].getHeight(null) + 8 + 24 * j) * graphicsScaling, 
                    false, // no moves can be hovered at this screen
                    false, // don't show power or accuracy to save space
                    g, 
                    canvas);
            }
        }

        //display pokemon windows
        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 16 * graphicsScaling));
        for (int i = 0; i < 6; i++)
        {
            int renderIndex = 0;
            int x = (i % 2) * (width / 3 + 8 * graphicsScaling);
            int y = (i / 2) * (height / 4 + 8 * graphicsScaling);

            // give the box a red outline if you are trying to switch the Pokemon
            if (i < this.model.team.size() && i == this.model.switchPokemonIndex)
            {
                renderIndex = 4;
            }
            // different colour highlight for the currently hovered Pokemon
            else if (i < this.model.team.size() && i == this.model.optionIndex)
            {
                renderIndex = 2;
            }
            else if (i >= this.model.team.size())
            {
                renderIndex = 3;
            }

            if (i < this.model.team.size())
            {
                this.renderPokemonWindow(this.model.team.get(i), renderIndex, x, y, g, canvas);
            }
        }

        // display a pop-up box of text options
        if (this.model.textOptions != null)
        {
            this.displayOptions(this.model.textOptions, this.model.textOptionIndex, g, canvas);
        }
    }

    /**
     * Renders a Pokemon window, including the Pokemon's icon, name, and HP
     * @param pokemon the PokemonModel representing the Pokemon to be rendered
     * @param renderIndex the type of window sprite to use
     * @param x x-position of the window
     * @param y y-position of the window
     * @param g Graphics object
     * @param canvas JPanel object
     */
    private void renderPokemonWindow(PokemonModel pokemon, int renderIndex, int x, int y, Graphics g, JPanel canvas)
    {
        Image windowImage;
        
        if (pokemon.currentHP > 0)
        {
            windowImage = this.pokemonWindows[renderIndex];
        }
        else
        {
            windowImage = this.faintedPokemonWindows[renderIndex];
        }

        g.drawImage(windowImage,
            x + 8 * graphicsScaling,
            y + 8 * graphicsScaling,
            width / 3,
            height / 4,
            canvas);

        // display the Pokemon's icons
        g.drawImage(this.pokemonIconSprite.get(pokemon.getSpriteId()), 
            x + 12 * graphicsScaling, 
            y + 14 * graphicsScaling, 
            this.pokemonIconSprite.get(pokemon.getSpriteId()).getWidth(null) * graphicsScaling, 
            this.pokemonIconSprite.get(pokemon.getSpriteId()).getHeight(null) * graphicsScaling, 
            canvas);

        //display text with Pokemon level
        g.drawString("Lv. " + pokemon.level,
            x + 18 * graphicsScaling, 
            y + 60 * graphicsScaling);
        
        //display text with pokemon name
        g.drawString(pokemon.getName(),
            x + 94 * graphicsScaling, 
            y + 34 * graphicsScaling);

        //display hp bars
        g.drawImage(this.hpBar,
            x + 94 * graphicsScaling,
            y + 40 * graphicsScaling,
            this.hpBar.getWidth(null) * graphicsScaling,
            this.hpBar.getHeight(null) * graphicsScaling,
            canvas);
        
        //get health bar colour
        byte healthBarFillIndex = 0;
        if((double)pokemon.currentHP / pokemon.stats[Stat.HP] < 0.2)
        {
            healthBarFillIndex = 2;
        }
        else if((double)pokemon.currentHP / pokemon.stats[Stat.HP] < 0.5)
        {
            healthBarFillIndex = 1;
        }
        
        //fill health bars
        if (pokemon.level > 0)
        {
            g.drawImage(healthBarFill[healthBarFillIndex],
                x + 110 * graphicsScaling, 
                y + 42 * graphicsScaling,
                (int)Math.ceil(healthBarFill[0].getWidth(null) * (pokemon.currentHP * 48.0 / pokemon.stats[Stat.HP]) * graphicsScaling),
                healthBarFill[0].getHeight(null) * graphicsScaling,
                canvas);
        }
        
        //display status condition
        int statusEffect = pokemon.statusEffect;
        if (statusEffect > 0 && statusEffect < 8)
        {
            g.drawImage(this.statusEffectImages[statusEffect - 1],
                x + 72 * graphicsScaling,
                y + 40 * graphicsScaling,
                this.statusEffectImages[0].getWidth(null) * graphicsScaling,
                this.statusEffectImages[0].getHeight(null) * graphicsScaling,
                canvas);
        }
    }
}
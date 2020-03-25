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
    private Image[] faintedPokemonWindows = new Image[3];
    private Image background;
    private Image[] statusEffectImages = new Image[6];
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
                ii = new ImageIcon("src/pokemonicons/" + pokemon.getSpriteId() + ".png");
                this.pokemonIconSprite.put(pokemon.getSpriteId(), ii.getImage());

                ii = new ImageIcon("src/pokemon/frame0/" + pokemon.getSpriteId() + ".png");
                this.pokemonSprite.put(pokemon.getSpriteId(), ii.getImage());
            }
        }

        for (int i = 0; i < this.healthBarFill.length; i++)
        {  
            ii = new ImageIcon("src/battle/hp" + i + ".png");
            this.healthBarFill[i] = ii.getImage();
        }

        for (int i = 0; i < this.pokemonWindows.length; i++)  
        {
            ii = new ImageIcon("src/menus/party" + i + ".png");
            this.pokemonWindows[i] = ii.getImage();
        }

        for (int i = 0; i < this.faintedPokemonWindows.length; i++)  
        {
            ii = new ImageIcon("src/menus/partyfainted" + i + ".png");
            this.faintedPokemonWindows[i] = ii.getImage();
        }

        //loads status effect images
        for (int i = 0; i < this.statusEffectImages.length; i++)
        {
            ii = new ImageIcon("src/menus/ailment" + (i + 1) + ".png");
            this.statusEffectImages[i] = ii.getImage();
        }

        ii = new ImageIcon("src/menus/hpBar.png");
        this.hpBar = ii.getImage();
        ii = new ImageIcon("src/inventory/background.png");
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
        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 16 * graphicsScaling));

        //draw the background
        int blockSize = background.getWidth(null) * graphicsScaling;
        for (int i = 0; i < Math.ceil((double)width / blockSize); i++)
        {
            for (int j = 0; j < Math.ceil((double)height / blockSize); j++)
            {
                g.drawImage(background, i * blockSize, j * blockSize, blockSize, blockSize, canvas);
            }
        }

        //display pokemon windows
        for (int i = 0; i < 6; i++)
        {
            int renderIndex = 0;

            if (i < this.model.team.size() && i == this.model.optionIndex)
            {
                renderIndex = 2;
                g.drawImage(this.pokemonSprite.get(this.model.team.get(i).getSpriteId()),
                    (int)(width * (3.0 / 4.0)),
                    8 * graphicsScaling,
                    this.pokemonSprite.get(this.model.team.get(i).getSpriteId()).getWidth(null) * graphicsScaling,
                    this.pokemonSprite.get(this.model.team.get(i).getSpriteId()).getHeight(null) * graphicsScaling,
                    canvas);
                for (int j = 0; j < this.model.team.get(i).moves.length; j++)
                {
                    g.drawString(this.model.team.get(i).moves[j].name,
                        (int)(width * 0.72),
                        (int)(height * (2.0 / 5.0) + 14 * j * graphicsScaling));
                }
            }

            else if (i >= this.model.team.size())
            {
                renderIndex = 3;
            }

            // give the box a red outline if you are trying to switch the Pokemon
            if (i < this.model.team.size() && i == this.model.switchPokemonIndex)
            {
                renderIndex = 4;
            }

            if (renderIndex == 3 || this.model.team.get(i).currentHP > 0)
            {
                g.drawImage(this.pokemonWindows[renderIndex],
                    (i % 2) * (width / 3 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    (i / 2) * (height / 4 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    width / 3,
                    height / 4,
                    canvas);
            }

            else
            {
                g.drawImage(this.faintedPokemonWindows[renderIndex],
                    (i % 2) * (width / 3 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    (i / 2) * (height / 4 + 8 * graphicsScaling) + 8 * graphicsScaling,
                    width / 3,
                    height / 4,
                    canvas);
            }
        }

        for (int i = 0; i < this.model.team.size(); i++)
        {
            // display the Pokemon's icons
            g.drawImage(this.pokemonIconSprite.get(this.model.team.get(i).getSpriteId()), 
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 12 * graphicsScaling, 
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 14 * graphicsScaling, 
                this.pokemonIconSprite.get(this.model.team.get(i).getSpriteId()).getWidth(null) * graphicsScaling, 
                this.pokemonIconSprite.get(this.model.team.get(i).getSpriteId()).getHeight(null) * graphicsScaling, 
                canvas);

            //display text with Pokemon level
            g.drawString("Lv. " + this.model.team.get(i).level,
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 18 * graphicsScaling, 
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 60 * graphicsScaling);
            
            //display text with pokemon name
            g.drawString(this.model.team.get(i).getName(),
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 94 * graphicsScaling, 
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 34 * graphicsScaling);

            //display hp bars
            g.drawImage(this.hpBar,
                (i % 2) * (width / 3 + 8 * graphicsScaling) + 94 * graphicsScaling,
                (i / 2) * (height / 4 + 8 * graphicsScaling) + 40 * graphicsScaling,
                this.hpBar.getWidth(null) * graphicsScaling,
                this.hpBar.getHeight(null) * graphicsScaling,
                canvas);
            
            //get health bar colour
            byte healthBarFillIndex = 0;
            if((double)this.model.team.get(i).currentHP / this.model.team.get(i).stats[Stat.HP] < 0.2)
            {
                healthBarFillIndex = 2;
            }
            else if((double)this.model.team.get(i).currentHP / this.model.team.get(i).stats[Stat.HP] < 0.5)
            {
                healthBarFillIndex = 1;
            }
            
            //fill health bars
            if (this.model.team.get(i).level > 0)
            {
                g.drawImage(healthBarFill[healthBarFillIndex],
                    (i % 2) * (width / 3 + 8 * graphicsScaling) + 110 * graphicsScaling, 
                    (i / 2) * (height / 4 + 8 * graphicsScaling) + 42 * graphicsScaling,
                    (int)Math.ceil(healthBarFill[0].getWidth(null) * (this.model.team.get(i).currentHP * 48.0 / this.model.team.get(i).stats[Stat.HP]) * graphicsScaling),
                    healthBarFill[0].getHeight(null) * graphicsScaling,
                    canvas);
            }
            
            //display status condition
            int statusEffect = this.model.team.get(i).statusEffect;
            if (statusEffect > 0 && statusEffect < 7)
            {
                g.drawImage(this.statusEffectImages[statusEffect - 1],
                    (i % 2) * (width / 3 + 8 * graphicsScaling) + 72 * graphicsScaling,
                    (i / 2) * (height / 4 + 8 * graphicsScaling) + 40 * graphicsScaling,
                    this.statusEffectImages[0].getWidth(null) * graphicsScaling,
                    this.statusEffectImages[0].getHeight(null) * graphicsScaling,
                    canvas);
            }
        }

        // display a pop-up box of text options
        if (this.model.textOptions != null)
        {
            this.displayOptions(this.model.textOptions, this.model.textOptionIndex, g, canvas);
        }
    }

    @Override
    public String toString()
    {
        return "PartyView";
    }
}
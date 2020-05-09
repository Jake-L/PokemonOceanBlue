package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the summary screen
 */
public class SummaryView extends BaseView {

    private BaseModel model;
    private List<PokemonModel> pokemonList;
    private Image[] pokemonSprites;
    private Image[] pokeballSprites;
    private Image[] typeSprites = new Image[18];
    private Image pokemonBackground;
    private Image pokemonHeader;
    private Image[] summaryBox = new Image[1];

    public SummaryView(BaseModel model, List<PokemonModel> pokemonList)
    {
        this.model = model;
        this.pokemonList = pokemonList;
        this.pokeballSprites = new Image[this.pokemonList.size()];
        this.pokemonSprites = new Image[this.pokemonList.size()];
        this.loadImage();
    }

    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < this.pokemonList.size(); i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/pokemon/frame0/" + this.pokemonList.get(i).getSpriteId() + ".png"));
            this.pokemonSprites[i] = ii.getImage();
            ii = new ImageIcon(this.getClass().getResource("/inventory/" + this.pokemonList.get(i).pokeballId + ".png"));
            this.pokeballSprites[i] = ii.getImage();
        }

        for (int i = 0; i < this.typeSprites.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/type" + i + ".png"));
            this.typeSprites[i] = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/menus/pokemonSummaryBackground.png"));
        this.pokemonBackground = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/menus/pokemonSummaryHeader.png"));
        this.pokemonHeader = ii.getImage();

        for (int i = 0; i < this.summaryBox.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/pokemonSummaryBox" + (i+1) + ".png"));
            this.summaryBox[i] = ii.getImage();
        }
    }

    /** 
     * renders the summary screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 12 * graphicsScaling));

        int pokemonIndex = this.model.optionIndex;

        // draw pokemon background
        g.drawImage(this.pokemonBackground,
            width / 10 - this.pokemonBackground.getWidth(null) * graphicsScaling / 2,
            height / 3 - this.pokemonBackground.getHeight(null) * graphicsScaling / 2,
            this.pokemonBackground.getWidth(null) * graphicsScaling,
            this.pokemonBackground.getHeight(null) * graphicsScaling,
            canvas);
        
        //draws pokemon sprite
        g.drawImage(this.pokemonSprites[pokemonIndex],
            width / 10 - this.pokemonSprites[pokemonIndex].getWidth(null) * graphicsScaling / 2,
            height / 3 - this.pokemonSprites[pokemonIndex].getHeight(null) * graphicsScaling / 2,
            this.pokemonSprites[pokemonIndex].getWidth(null) * graphicsScaling,
            this.pokemonSprites[pokemonIndex].getHeight(null) * graphicsScaling,
            canvas);

        // display header to hold Pokemon's name and level
        g.drawImage(this.pokemonHeader,
            Math.max(width / 10 - this.pokemonHeader.getWidth(null) * graphicsScaling / 2, 4 * graphicsScaling),
            height / 20,
            this.pokemonHeader.getWidth(null) * graphicsScaling,
            this.pokemonHeader.getHeight(null) * graphicsScaling,
            canvas);
        
        //displays pokemon name
        g.drawString(this.pokemonList.get(pokemonIndex).getName(),
            width / 10 - 20 * graphicsScaling,
            height / 20 + 17 * graphicsScaling);

        // only show stats for Pokemon, not Eggs
        if (this.pokemonList.get(pokemonIndex).level > 0)
        {
            // g.drawImage(this.summaryBox[0],
            //     4 * graphicsScaling,
            //     height / 2,
            //     this.summaryBox[0].getWidth(null) * graphicsScaling,
            //     this.summaryBox[0].getHeight(null) * graphicsScaling,
            //     canvas);

            //displays pokemon level
            g.drawString("Lv." + this.pokemonList.get(pokemonIndex).level,
                width / 10 - 20 * graphicsScaling,
                height / 20 + 32 * graphicsScaling);

            //displays pokemon number
            g.drawString("No." + this.pokemonList.get(pokemonIndex).base_pokemon_id,
                24 * graphicsScaling,
                height * 9 / 16);

            //displays pokemon type(s)
            for (int i = 0; i < this.pokemonList.get(pokemonIndex).types.length; i++)
            {
                g.drawImage(this.typeSprites[this.pokemonList.get(pokemonIndex).types[i]],
                    (int)(24 * graphicsScaling + i * this.typeSprites[0].getWidth(null) * graphicsScaling / 1.5),
                    height * 9 / 16 + 4 * graphicsScaling,
                    this.typeSprites[0].getWidth(null) * graphicsScaling / 2,
                    this.typeSprites[0].getHeight(null) * graphicsScaling / 2,
                    canvas);
            }

            //displays pokemon stats
            String[] stats = {"HP","ATTACK","DEFENSE","SP. ATTACK","SP. DEFENSE","SPEED"};
            for (int i = 0; i < this.pokemonList.get(pokemonIndex).stats.length; i++)
            {
                g.drawString(String.valueOf(this.pokemonList.get(pokemonIndex).stats[i]),
                    width / 3 + 32 * graphicsScaling,
                    height * (i + 2) / 16);
                
                g.drawString(stats[i],
                    width / 4,
                    height * (i + 2) / 16);

                this.renderProgressBar(width * 7 / 16, height * (i + 2) / 16 - 8 * graphicsScaling, this.pokemonList.get(pokemonIndex).ivs[i] / 64.0, g, canvas);
            }

            //display pokemon moves
            for (int i = 0; i < this.pokemonList.get(pokemonIndex).moves.length; i++)
            {
                g.drawImage(this.typeSprites[this.pokemonList.get(pokemonIndex).moves[i].typeId],
                    (int)(width * (2.0 / 3.0)),
                    i * height / 6 + height / 8 - 8 * graphicsScaling,
                    this.typeSprites[0].getWidth(null) * graphicsScaling / 2,
                    this.typeSprites[0].getHeight(null) * graphicsScaling / 2,
                    canvas);
                
                g.drawString(this.pokemonList.get(pokemonIndex).moves[i].name,
                    (int)(width * (2.0 / 3.0) + 8 * graphicsScaling + this.typeSprites[i].getWidth(null)),
                    i * height / 6 + height / 8 - 2 * graphicsScaling);

                g.drawString("Dmg: " + String.valueOf(this.pokemonList.get(pokemonIndex).moves[i].power),
                    (int)(width * (2.0 / 3.0)),
                    i * height / 6 + height / 8 + 16 * graphicsScaling);

                g.drawString("Acc: " + String.valueOf(this.pokemonList.get(pokemonIndex).moves[i].accuracy),
                    (int)(width * (2.0 / 3.0) + graphicsScaling * 72),
                    i * height / 6 + height / 8 + 16 * graphicsScaling);
            }
        }
    }

    @Override
    public String toString()
    {
        return "SummaryView";
    }
}
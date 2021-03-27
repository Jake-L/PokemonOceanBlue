package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the summary screen
 */
public class SummaryView extends BaseView {

    private SummaryModel model;
    private Image[] pokemonSprites;
    private Image[] pokeballSprites;
    private Image[] summaryBox = new Image[2];

    public SummaryView(SummaryModel model)
    {
        this.model = model;
        this.pokeballSprites = new Image[this.model.pokemonList.size()];
        this.pokemonSprites = new Image[this.model.pokemonList.size()];
        this.loadImage();
    }

    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < this.pokemonSprites.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/pokemoncentered/frame0/" + this.model.pokemonList.get(i).getSpriteId() + ".png"));
            this.pokemonSprites[i] = ii.getImage();
            ii = new ImageIcon(this.getClass().getResource("/inventory/" + this.model.pokemonList.get(i).pokeballId + ".png"));
            this.pokeballSprites[i] = ii.getImage();
        }

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

        this.renderPokemonSidebar(this.model.pokemonList.get(pokemonIndex), 
            this.pokemonSprites[pokemonIndex], 
            1,
            g, 
            canvas);

        // only show stats for Pokemon, not Eggs
        if (this.model.pokemonList.get(pokemonIndex).level > 0)
        {
            this.renderMetadata(
                4 * graphicsScaling,
                height / 20 + (this.summaryHeader[0].getHeight(null) + this.pokemonBackground[0].getHeight(null)) * graphicsScaling,
                g, canvas
            );

            this.renderStats(width / 4, height / 16, g, canvas);

            //display pokemon moves
            int index = 0;
            while (index < this.model.pokemonList.get(pokemonIndex).moves.length)
            {
                this.renderMove(this.model.pokemonList.get(pokemonIndex).moves[index], 
                    width * 2 / 3, 
                    index * height / 6 + height / 8, 
                    this.model.hoverMoveIndex == index, 
                    true, // show power and accuracy
                    g, 
                    canvas);
                index++;
            }
            // display a new move if Pokemon is trying to learn it
            if (this.model.newMove != null)
            {
                this.renderMove(this.model.newMove, 
                    width * 2 / 3, 
                    index * height / 6 + height / 8, 
                    this.model.hoverMoveIndex == index, 
                    true, // show power and accuracy
                    g, 
                    canvas);
            }
        }
    }

    private void renderMetadata(int x, int y, Graphics g, JPanel canvas)
    {
        //displays pokemon stats
        int pokemonIndex = this.model.optionIndex;

        g.drawImage(this.summaryBox[0],
            x,
            y,
            this.summaryBox[0].getWidth(null) * graphicsScaling,
            this.summaryBox[0].getHeight(null) * graphicsScaling,
            canvas);

        //displays pokemon number
        g.drawString("No." + this.model.pokemonList.get(pokemonIndex).base_pokemon_id,
            x + 20 * graphicsScaling,
            y + height / 16);
    
        //displays pokemon type(s)
        for (int i = 0; i < this.model.pokemonList.get(pokemonIndex).types.length; i++)
        {
            g.drawImage(typeSprites[this.model.pokemonList.get(pokemonIndex).types[i]],
                x + (int)(20 * graphicsScaling + i * typeSprites[0].getWidth(null) * graphicsScaling / 1.5),
                y + height / 16 + 4 * graphicsScaling,
                typeSprites[0].getWidth(null) * graphicsScaling / 2,
                typeSprites[0].getHeight(null) * graphicsScaling / 2,
                canvas);
        }
    }


    private void renderStats(int x, int y, Graphics g, JPanel canvas)
    {
        //displays pokemon stats
        int pokemonIndex = this.model.optionIndex;

        g.drawImage(this.summaryBox[1],
                x,
                y,
                this.summaryBox[1].getWidth(null) * graphicsScaling,
                this.summaryBox[1].getHeight(null) * graphicsScaling,
                canvas);

        String[] stats = {"HP","ATTACK","DEFENSE","SP. ATTACK","SP. DEFENSE","SPEED"};
        for (int i = 0; i < this.model.pokemonList.get(pokemonIndex).stats.length; i++)
        {
            g.drawString(String.valueOf(this.model.pokemonList.get(pokemonIndex).stats[i]),
                x + 90 * graphicsScaling,
                y + (i * 17 + 12) * graphicsScaling);
            
            g.drawString(stats[i],
                x + 4 * graphicsScaling,
                y + (i * 17 + 12) * graphicsScaling);

            this.renderProgressBar(
                x + 61 * graphicsScaling, 
                y + (i * 17 + 13) * graphicsScaling, 
                this.model.pokemonList.get(pokemonIndex).ivs[i] / 64.0, 
                g, canvas);
        }
    }
}
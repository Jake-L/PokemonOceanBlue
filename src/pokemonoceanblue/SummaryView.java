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

    private PartyModel model;
    private int teamSize;
    private Image[] pokemonSprites;
    private Image[] pokeballSprites;
    private Image[] typeSprites = new Image[18];
    private Image progressBar;
    private Image progressBarFill;

    public SummaryView(PartyModel model)
    {
        this.model = model;
        this.teamSize = this.model.team.length;
        this.pokeballSprites = new Image[this.teamSize];
        this.pokemonSprites = new Image[this.teamSize];
        this.loadImage();
    }

    private void loadImage() 
    {
        ImageIcon ii;

        for (int i = 0; i < this.teamSize; i++)
        {
            ii = new ImageIcon("src/pokemon/frame0/" + this.model.team[i].id + ".png");
            this.pokemonSprites[i] = ii.getImage();
            ii = new ImageIcon("src/inventory/" + this.model.team[i].pokeballId + ".png");
            this.pokeballSprites[i] = ii.getImage();
        }

        for (int i = 0; i < this.typeSprites.length; i++)
        {
            ii = new ImageIcon("src/menus/type" + i + ".png");
            this.typeSprites[i] = ii.getImage();
        }

        ii = new ImageIcon("src/menus/progressBar.png");
        this.progressBar = ii.getImage();
        ii = new ImageIcon("src/menus/progressBarFill.png");
        this.progressBarFill = ii.getImage();
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
        this.teamSize = this.model.team.length;
        
        //draws pokemon sprite
        g.drawImage(this.pokemonSprites[pokemonIndex],
            8 * graphicsScaling,
            height / 4 - 8 * graphicsScaling,
            this.pokemonSprites[pokemonIndex].getWidth(null) * graphicsScaling,
            this.pokemonSprites[pokemonIndex].getHeight(null) * graphicsScaling,
            canvas);
        
        //displays pokemon name
        g.drawString(this.model.team[pokemonIndex].name,
            20 * graphicsScaling,
            height / 8);

        //displays pokemon level
        g.drawString("Lv." + this.model.team[pokemonIndex].level,
            20 * graphicsScaling,
            height / 8 + 16 * graphicsScaling);

        //displays pokemon number
        g.drawString("No." + this.model.team[pokemonIndex].id,
            24 * graphicsScaling,
            height * 9 / 16);

        //displays pokemon type(s)
        for (int i = 0; i < this.model.team[pokemonIndex].types.length; i++)
        {
            g.drawImage(this.typeSprites[this.model.team[pokemonIndex].types[i]],
                (int)(24 * graphicsScaling + i * this.typeSprites[0].getWidth(null) * graphicsScaling / 1.5),
                height * 9 / 16 + 4 * graphicsScaling,
                this.typeSprites[0].getWidth(null) * graphicsScaling / 2,
                this.typeSprites[0].getHeight(null) * graphicsScaling / 2,
                canvas);
        }

        //displays pokemon stats
        String[] stats = {"HP","ATTACK","DEFENSE","SP. ATTACK","SP. DEFENSE","SPEED"};
        for (int i = 0; i < this.model.team[pokemonIndex].stats.length; i++)
        {
            g.drawString(String.valueOf(this.model.team[pokemonIndex].stats[i]),
                width / 3 + 32 * graphicsScaling,
                height * (i + 2) / 16);
            
            g.drawString(stats[i],
                width / 4,
                height * (i + 2) / 16);

            this.renderProgressBar(width * 7 / 16, height * (i + 2) / 16 - 8 * graphicsScaling, this.model.team[pokemonIndex].ivs[i], g, canvas);
        }

        //display pokemon moves
        for (int i = 0; i < this.model.team[pokemonIndex].moves.length; i++)
        {
            g.drawImage(this.typeSprites[this.model.team[pokemonIndex].moves[i].typeId],
                (int)(width * (2.0 / 3.0)),
                i * height / 6 + height / 8 - 8 * graphicsScaling,
                this.typeSprites[0].getWidth(null) * graphicsScaling / 2,
                this.typeSprites[0].getHeight(null) * graphicsScaling / 2,
                canvas);
            
            g.drawString(this.model.team[pokemonIndex].moves[i].name,
                (int)(width * (2.0 / 3.0) + 8 * graphicsScaling + this.typeSprites[i].getWidth(null)),
                i * height / 6 + height / 8 - 2 * graphicsScaling);

            g.drawString("Dmg: " + String.valueOf(this.model.team[pokemonIndex].moves[i].power),
                (int)(width * (2.0 / 3.0)),
                i * height / 6 + height / 8 + 16 * graphicsScaling);

            g.drawString("Acc: " + String.valueOf(this.model.team[pokemonIndex].moves[i].accuracy),
                (int)(width * (2.0 / 3.0) + graphicsScaling * 72),
                i * height / 6 + height / 8 + 16 * graphicsScaling);
        }
    }

    /**
     * @param x the x coordinate of the progress bar
     * @param y the y coordinate of the progress bar
     * @param progress the percent progress of the bar to be filled
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    private void renderProgressBar(int x, int y, int progress, Graphics g, JPanel canvas)
    {
        g.drawImage(this.progressBar, x, y,
            this.progressBar.getWidth(null) * graphicsScaling,
            this.progressBar.getHeight(null) * graphicsScaling,
            canvas);

        g.drawImage(this.progressBarFill, 
            x + 3 * graphicsScaling, 
            y + 2 * graphicsScaling,
            (int)(this.progressBarFill.getWidth(null) * progress / 100.0 * 64.0 * graphicsScaling),
            this.progressBarFill.getHeight(null) * graphicsScaling,
            canvas);
    }

    @Override
    public String toString()
    {
        return "SummaryView";
    }
}
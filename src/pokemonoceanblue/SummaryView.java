package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the summary screen
 */
public class SummaryView extends ViewBase {

    private PartyModel model;
    private int teamSize;
    private Image[] pokemonSprites;
    private Image[] pokeballSprites;
    private Image[] typeSprites = new Image[18];

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
            height / 7,
            this.pokemonSprites[pokemonIndex].getWidth(null) * graphicsScaling,
            this.pokemonSprites[pokemonIndex].getHeight(null) * graphicsScaling,
            canvas);
        
        //displays pokemon name
        g.drawString(this.model.team[pokemonIndex].name,
            20 * graphicsScaling,
            20 * graphicsScaling);

        //displays pokemon level
        g.drawString("Lv." + this.model.team[pokemonIndex].level,
            20 * graphicsScaling,
            32 * graphicsScaling);

        //displays pokemon number
        g.drawString("No." + this.model.team[pokemonIndex].id,
            24 * graphicsScaling,
            height / 2);

        //displays pokemon type(s)
        for (int i = 0; i < this.model.team[pokemonIndex].types.length; i++)
        {
            g.drawImage(this.typeSprites[this.model.team[pokemonIndex].types[i]],
                (int)(24 * graphicsScaling + i * this.typeSprites[0].getWidth(null) * graphicsScaling / 1.5),
                height / 2 + 4 * graphicsScaling,
                this.typeSprites[0].getWidth(null) * graphicsScaling / 2,
                this.typeSprites[0].getHeight(null) * graphicsScaling / 2,
                canvas);
        }

        //displays pokemon stats
        String[] stats = {"HP","ATTACK","DEFENSE","SPECIAL ATTACK","SPECIAL DEFENSE","SPEED"};
        for (int i = 0; i < this.model.team[pokemonIndex].stats.length; i++)
        {
            g.drawString(String.valueOf(this.model.team[pokemonIndex].stats[i]),
                width / 2,
                height * (i + 1) / 16);
            
            g.drawString(stats[i],
                width / 3,
                height * (i + 1) / 16);
        }

        //display pokemon moves
        for (int i = 0; i < this.model.team[pokemonIndex].moves.length; i++)
        {
            g.drawImage(this.typeSprites[this.model.team[pokemonIndex].moves[i].typeId],
                (int)(width * (2.0 / 3.0)),
                i * height / 6 + 8 * graphicsScaling,
                this.typeSprites[0].getWidth(null) * graphicsScaling / 2,
                this.typeSprites[0].getHeight(null) * graphicsScaling / 2,
                canvas);
            
            g.drawString(this.model.team[pokemonIndex].moves[i].name,
                (int)(width * (2.0 / 3.0) + 8 * graphicsScaling + this.typeSprites[i].getWidth(null)),
                i * height / 6 + 14 * graphicsScaling);

            g.drawString("Dmg: " + String.valueOf(this.model.team[pokemonIndex].moves[i].power),
                (int)(width * (2.0 / 3.0)),
                i * height / 6 + 32 * graphicsScaling);

            g.drawString("Acc: " + String.valueOf(this.model.team[pokemonIndex].moves[i].accuracy),
                (int)(width * (2.0 / 3.0) + graphicsScaling * 72),
                i * height / 6 + 32 * graphicsScaling);
        }
    }

    @Override
    public String toString()
    {
        return "SummaryView";
    }
}
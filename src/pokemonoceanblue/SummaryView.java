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

    private SummaryModel model;
    private Image[] pokemonSprites;
    private Image[] pokeballSprites;
    private Image pokemonBackground;
    private Image pokemonHeader;
    private Image[] summaryBox = new Image[1];

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
            ii = new ImageIcon(this.getClass().getResource("/pokemon/frame0/" + this.model.pokemonList.get(i).getSpriteId() + ".png"));
            this.pokemonSprites[i] = ii.getImage();
            ii = new ImageIcon(this.getClass().getResource("/inventory/" + this.model.pokemonList.get(i).pokeballId + ".png"));
            this.pokeballSprites[i] = ii.getImage();
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
        g.drawString(this.model.pokemonList.get(pokemonIndex).getName(),
            width / 10 - 20 * graphicsScaling,
            height / 20 + 17 * graphicsScaling);

        // only show stats for Pokemon, not Eggs
        if (this.model.pokemonList.get(pokemonIndex).level > 0)
        {
            // g.drawImage(this.summaryBox[0],
            //     4 * graphicsScaling,
            //     height / 2,
            //     this.summaryBox[0].getWidth(null) * graphicsScaling,
            //     this.summaryBox[0].getHeight(null) * graphicsScaling,
            //     canvas);

            //displays pokemon level
            g.drawString("Lv." + this.model.pokemonList.get(pokemonIndex).level,
                width / 10 - 20 * graphicsScaling,
                height / 20 + 32 * graphicsScaling);

            // displays Pokemon gender
            g.drawImage(genderIcons[this.model.pokemonList.get(pokemonIndex).genderId],
                width / 10 + 40 * graphicsScaling,
                height / 20 + 10 * graphicsScaling,
                genderIcons[0].getWidth(null) * graphicsScaling,
                genderIcons[0].getHeight(null) * graphicsScaling,
                canvas);

            //displays pokemon number
            g.drawString("No." + this.model.pokemonList.get(pokemonIndex).base_pokemon_id,
                24 * graphicsScaling,
                height * 9 / 16);

            //displays pokemon type(s)
            for (int i = 0; i < this.model.pokemonList.get(pokemonIndex).types.length; i++)
            {
                g.drawImage(typeSprites[this.model.pokemonList.get(pokemonIndex).types[i]],
                    (int)(24 * graphicsScaling + i * typeSprites[0].getWidth(null) * graphicsScaling / 1.5),
                    height * 9 / 16 + 4 * graphicsScaling,
                    typeSprites[0].getWidth(null) * graphicsScaling / 2,
                    typeSprites[0].getHeight(null) * graphicsScaling / 2,
                    canvas);
            }

            //displays pokemon stats
            String[] stats = {"HP","ATTACK","DEFENSE","SP. ATTACK","SP. DEFENSE","SPEED"};
            for (int i = 0; i < this.model.pokemonList.get(pokemonIndex).stats.length; i++)
            {
                g.drawString(String.valueOf(this.model.pokemonList.get(pokemonIndex).stats[i]),
                    width / 3 + 32 * graphicsScaling,
                    height * (i + 2) / 16);
                
                g.drawString(stats[i],
                    width / 4,
                    height * (i + 2) / 16);

                this.renderProgressBar(width * 7 / 16, height * (i + 2) / 16 - 8 * graphicsScaling, this.model.pokemonList.get(pokemonIndex).ivs[i] / 64.0, g, canvas);
            }

            //display pokemon moves
            int index = 0;
            while (index < this.model.pokemonList.get(pokemonIndex).moves.length)
            {
                this.renderMove(this.model.pokemonList.get(pokemonIndex).moves[index], 
                    width * 2 / 3, 
                    index * height / 6 + height / 8, 
                    this.model.hoverMoveIndex == index, 
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
                    g, 
                    canvas);
            }
        }
    }

    /**
     * Display one of the Pokemon's moves
     * @param move the move to be displayed
     * @param x left position of render area
     * @param y top position of render area
     * @param isHovered where the player is currently hovering this move
     * @param g graphics object
     * @param canvas JPanel object
     */
    private void renderMove(MoveModel move, int x, int y, boolean isHovered, Graphics g, JPanel canvas)
    {
        g.drawImage(typeSprites[move.typeId],
            x,
            y,
            typeSprites[0].getWidth(null) * graphicsScaling / 2,
            typeSprites[0].getHeight(null) * graphicsScaling / 2,
            canvas);
                
        g.drawString(move.name,
            x + 2 * graphicsScaling + typeSprites[0].getWidth(null) * graphicsScaling / 2,
            y + 6 * graphicsScaling);

        g.drawString("Dmg: " + move.power,
            x,
            y + 16 * graphicsScaling);

        g.drawString("Acc: " + move.accuracy,
            x + 72 * graphicsScaling,
            y + 16 * graphicsScaling);

        if (isHovered)
        {
            g.drawImage(arrowSprite,
                x - 10 * graphicsScaling,
                y,
                arrowSprite.getWidth(null) * graphicsScaling,
                arrowSprite.getHeight(null) * graphicsScaling,
                canvas);
        }
    }

    @Override
    public String toString()
    {
        return "SummaryView";
    }
}
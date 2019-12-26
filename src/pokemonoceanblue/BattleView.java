package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the Battle
 */
public class BattleView extends ViewBase {

    private BattleModel model;
    private Image[][] pokemonSprite = new Image[2][];
    private Image[] healthBarFill = new Image[3];
    private Image[] statusWindow = new Image[2];
    private Image background;
    
    /** 
     * Constructor for the overworld view
     * @param model model for the battle to be displayed
     */
    public BattleView(BattleModel model)
    {
        this.model = model;
        loadImage();
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage() 
    {
        ImageIcon ii;
        String shinyPrefix;
        pokemonSprite[0] = new Image[model.team[0].length];
        pokemonSprite[1] = new Image[model.team[1].length];

        //load players pokemon back sprites
        for (int i = 0; i < pokemonSprite[0].length; i++)
        {
            shinyPrefix = model.team[0][i].shiny ? "shiny" : "";
            ii = new ImageIcon("src/pokemonback/" + shinyPrefix + "frame0/" + model.team[0][i].id + ".png");
            pokemonSprite[0][i]  = ii.getImage();
        }

        //load opponents pokemon front sprites
        for (int i = 0; i < pokemonSprite[1].length; i++)
        {
            shinyPrefix = model.team[1][i].shiny ? "shiny" : "";
            ii = new ImageIcon("src/pokemon/" + shinyPrefix + "frame0/" + model.team[1][i].id + ".png");
            pokemonSprite[1][i]  = ii.getImage();
        }

        //loads health bar fill images
        for (int i = 0; i < 3; i++)
        {  
            ii = new ImageIcon("src/battle/hp" + i + ".png");
            healthBarFill[i] = ii.getImage();
        }

        //loads status window images
        ii = new ImageIcon("src/battle/TrainerStatusWindow.png");
        statusWindow[0] = ii.getImage();
        ii = new ImageIcon("src/battle/OpponentStatusWindow.png");
        statusWindow[1] = ii.getImage();

        //loads background image
        ii = new ImageIcon("src/battle/Background" + model.areaType + model.daytimeType + ".png");
        background = ii.getImage();
    }

    /** 
     * renders the battle screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    { 
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 12 * graphicsScaling);      
        g.setFont(font);

        //renders background
        g.drawImage(background,
            width / 10,
            0,
            width * 8 / 10,
            height * 3 / 4,
            canvas);

        //renders players current pokemon
        g.drawImage(pokemonSprite[0][model.currentPokemon[0]],
            width / 8,
            (int)(height * (3.0 / 4.0) - (pokemonSprite[0][model.currentPokemon[0]].getHeight(null) * graphicsScaling)),
            pokemonSprite[0][model.currentPokemon[0]].getWidth(null) * graphicsScaling,
            pokemonSprite[0][model.currentPokemon[0]].getHeight(null) * graphicsScaling,
            canvas);

        //renders opposing trainers current pokemon
        g.drawImage(pokemonSprite[1][model.currentPokemon[1]],
            width / 2,
            height / 2 - (pokemonSprite[1][model.currentPokemon[1]].getHeight(null) * graphicsScaling),
            pokemonSprite[1][model.currentPokemon[1]].getWidth(null) * graphicsScaling,
            pokemonSprite[1][model.currentPokemon[1]].getHeight(null) * graphicsScaling,
            canvas);

        //renders player status window
        g.drawImage(statusWindow[0],
            (int)(width * (9.0 / 10.0)) - (statusWindow[0].getWidth(null) * graphicsScaling),
            height / 2,
            statusWindow[0].getWidth(null) * graphicsScaling,
            statusWindow[0].getHeight(null) * graphicsScaling,
            canvas);
        
        //renders opponent status window
        g.drawImage(statusWindow[1],
            width / 10,
            height / 10,
            statusWindow[1].getWidth(null) * graphicsScaling,
            statusWindow[1].getHeight(null) * graphicsScaling,
            canvas);

        //get health bar colour
        byte[] healthBarFillIndex = new byte[2];
        for (int i = 0; i < healthBarFillIndex.length; i++)
        {
            healthBarFillIndex[i] = 0;
            if((double)model.team[i][model.currentPokemon[i]].currentHP / model.team[i][model.currentPokemon[i]].stats[Stat.HP] < 0.2)
            {
                healthBarFillIndex[i] = 2;
            }
            else if((double)model.team[i][model.currentPokemon[i]].currentHP / model.team[i][model.currentPokemon[i]].stats[Stat.HP] < 0.5)
            {
                healthBarFillIndex[i] = 1;
            }
        }
        
        //renders player health bar fill
        g.drawImage(healthBarFill[healthBarFillIndex[0]],
            (int)(width * (9.0 / 10.0)) - (statusWindow[0].getWidth(null) * graphicsScaling) + (72 * graphicsScaling),
            height / 2 + (25 * graphicsScaling),
            (int)Math.ceil(healthBarFill[0].getWidth(null) * (model.team[0][model.currentPokemon[0]].currentHP * 48.0 / model.team[0][model.currentPokemon[0]].stats[Stat.HP]) * graphicsScaling),
            healthBarFill[0].getHeight(null) * graphicsScaling,
            canvas);

        //renders opponent health bar fill
        g.drawImage(healthBarFill[healthBarFillIndex[1]],
            width / 10 + (50 * graphicsScaling),
            height / 10 + (24 * graphicsScaling),
            (int)Math.ceil(healthBarFill[1].getWidth(null) * (model.team[1][model.currentPokemon[1]].currentHP * 48.0 / model.team[1][model.currentPokemon[1]].stats[Stat.HP]) * graphicsScaling),
            healthBarFill[1].getHeight(null) * graphicsScaling,
            canvas);

        //displays level of trainer pokemon
        g.drawString(String.valueOf(model.team[0][model.currentPokemon[0]].level),
            ((int)(width * (9.0 / 10.0)) - ((statusWindow[0].getWidth(null) - 106) * graphicsScaling)),
            height / 2 + (18 * graphicsScaling));

        //displays health of trainer pokemon
        g.drawString(String.valueOf(model.team[0][model.currentPokemon[0]].currentHP),
            (int)(width * (9.0 / 10.0)) - ((statusWindow[0].getWidth(null) - 88) * graphicsScaling) - g.getFontMetrics(font).stringWidth(String.valueOf(model.team[0][model.currentPokemon[0]].currentHP)),
            height / 2 + 38 * graphicsScaling);
        g.drawString(String.valueOf(model.team[0][model.currentPokemon[0]].stats[Stat.HP]),
            (int)(width * (9.0 / 10.0)) - ((statusWindow[0].getWidth(null) - 97) * graphicsScaling),
            height / 2 + 38 * graphicsScaling);

        //displays name of trainer pokemon
        g.drawString(String.valueOf(model.team[0][model.currentPokemon[0]].name),
            (int)(width * (9.0 / 10.0)) - ((statusWindow[0].getWidth(null) - 16) * graphicsScaling),
            height / 2 + (18 * graphicsScaling));

        //displays name of opponent pokemon
        g.drawString(String.valueOf(model.team[1][model.currentPokemon[1]].name),
            width / 10 + 2 * graphicsScaling,
            height / 10 + 17 * graphicsScaling);

        //displays level of opponent pokemon
        g.drawString(String.valueOf(model.team[1][model.currentPokemon[1]].level),
            width / 10 + 84 * graphicsScaling,
            height / 10 + 17 * graphicsScaling);
        
        displayTextbox(this.textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);
        
        if (model.battleOptions != null)
        {
            displayTextOptions(g, canvas);
        }

        else
        {
            displayBattleText(g, canvas);
        }
    }

    //displays battle options in a text box
    protected void displayTextOptions(Graphics g, JPanel canvas)
    {
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 24 * graphicsScaling);
        g.setFont(font);

        for (int i = 0; i < model.battleOptions.length; i++)
        {
            g.drawString(model.battleOptions[i], 
                24 * graphicsScaling + (width / 2) * (i % 2), 
                (height * 3 / 4 + 24 * graphicsScaling) + graphicsScaling * 24 * (i / 2));
        }

        int textWidth = g.getFontMetrics(font).stringWidth(">");

        g.drawString(">",
        24 * graphicsScaling + (width / 2) * (model.optionIndex % 2) - textWidth,
        (height * 3 / 4 + 24 * graphicsScaling) + graphicsScaling * 24 * (model.optionIndex / 2));
    }

    //displays text in a text box
    private void displayBattleText(Graphics g, JPanel canvas)
    {
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 24 * graphicsScaling);
        g.setFont(font);
        String text = model.getText();
        if (text != null)
        {
            g.drawString(text,
                24 * graphicsScaling,
                height * 3 / 4 + 24 * graphicsScaling);
        }
    }

    @Override
    public String toString(){
        return "BattleView";
    }
}
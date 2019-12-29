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
    private Image xp;
    private Image[] pokeballSprite = new Image[4];
    private Image[][] pokemonIconSprites = new Image[2][];
    
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
        this.pokemonSprite[0] = new Image[model.team[0].length];
        this.pokemonSprite[1] = new Image[model.team[1].length];

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
            this.pokemonSprite[1][i]  = ii.getImage();
        }

        //loads health bar fill images
        for (int i = 0; i < 3; i++)
        {  
            ii = new ImageIcon("src/battle/hp" + i + ".png");
            this.healthBarFill[i] = ii.getImage();
        }

        //loads pokeball sprites
        for (int i = 0; i < this.pokeballSprite.length; i ++)
        {
            ii = new ImageIcon("src/inventory/" + i + ".png");
            this.pokeballSprite[i] = ii.getImage();
        }

        //loads pokemon icons for both teams
        for (int i = 0; i < this.pokemonIconSprites.length; i++)
        {
            this.pokemonIconSprites[i] = new Image[this.model.team[i].length];
            for (int j = 0; j < this.pokemonIconSprites[i].length; j++)
            {
                ii = new ImageIcon("src/pokemonicons/" + this.model.team[i][j].id + ".png");
                this.pokemonIconSprites[i][j] = ii.getImage();
            }
        }

        //loads status window images and xp
        ii = new ImageIcon("src/battle/TrainerStatusWindow.png");
        this.statusWindow[0] = ii.getImage();
        ii = new ImageIcon("src/battle/OpponentStatusWindow.png");
        this.statusWindow[1] = ii.getImage();
        ii = new ImageIcon("src/battle/exp.png");
        this.xp = ii.getImage();
        

        //loads background image
        ii = new ImageIcon("src/battle/Background" + model.areaType + model.daytimeType + ".png");
        this.background = ii.getImage();
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
        g.drawImage(this.background,
            width / 10,
            0,
            width * 8 / 10,
            height * 3 / 4,
            canvas);

        //renders players current pokemon
        g.drawImage(pokemonSprite[0][this.model.currentPokemon[0]],
            width / 8,
            (int)(height * (3.0 / 4.0) - (this.pokemonSprite[0][this.model.currentPokemon[0]].getHeight(null) * graphicsScaling)),
            this.pokemonSprite[0][this.model.currentPokemon[0]].getWidth(null) * graphicsScaling,
            this.pokemonSprite[0][this.model.currentPokemon[0]].getHeight(null) * graphicsScaling,
            canvas);

        if (this.model.events.size() > 0 && this.model.events.get(0).itemId > -1)
        {
            int pokeballSpriteIndex = this.model.events.get(0).itemId;
            //renders a pokeball in place of enemy pokemon
            g.drawImage(this.pokeballSprite[pokeballSpriteIndex],
                width / 2,
                height / 2 - (this.pokeballSprite[pokeballSpriteIndex].getHeight(null) * graphicsScaling),
                this.pokeballSprite[pokeballSpriteIndex].getWidth(null) * graphicsScaling,
                this.pokeballSprite[pokeballSpriteIndex].getHeight(null) * graphicsScaling,
                canvas);
        }

        else if (!this.model.isCaught)
        {
            //renders current enemy pokemon
            g.drawImage(pokemonSprite[1][this.model.currentPokemon[1]],
                width / 2,
                height / 2 - (pokemonSprite[1][this.model.currentPokemon[1]].getHeight(null) * graphicsScaling),
                this.pokemonSprite[1][this.model.currentPokemon[1]].getWidth(null) * graphicsScaling,
                this.pokemonSprite[1][this.model.currentPokemon[1]].getHeight(null) * graphicsScaling,
                canvas);
        }

        //renders player status window
        g.drawImage(this.statusWindow[0],
            (int)(width * (9.0 / 10.0)) - (this.statusWindow[0].getWidth(null) * graphicsScaling),
            height / 2,
            this.statusWindow[0].getWidth(null) * graphicsScaling,
            this.statusWindow[0].getHeight(null) * graphicsScaling,
            canvas);
        
        //renders opponent status window
        g.drawImage(this.statusWindow[1],
            width / 10,
            height / 10,
            this.statusWindow[1].getWidth(null) * graphicsScaling,
            this.statusWindow[1].getHeight(null) * graphicsScaling,
            canvas);

        //allows for gradual change in health bar visual
        double damage[] = new double[2];
        damage[0] = 0.0;
        damage[1] = 0.0;
        if (this.model.events.size() > 0 && this.model.events.get(0).damage > -1)
        {
            int target = this.model.events.get(0).target;
            damage[target] = Math.min((this.model.team[target][this.model.currentPokemon[target]].currentHP / 60.0), (this.model.events.get(0).damage / 60.0));
        }

        //get health bar colour
        byte[] healthBarFillIndex = new byte[2];
        for (int i = 0; i < healthBarFillIndex.length; i++)
        {
            healthBarFillIndex[i] = 0;
            if((double)(this.model.team[i][this.model.currentPokemon[i]].currentHP - (damage[i] * (60 - this.model.counter))) / this.model.team[i][this.model.currentPokemon[i]].stats[Stat.HP] < 0.2)
            {
                healthBarFillIndex[i] = 2;
            }
            else if((double)(this.model.team[i][this.model.currentPokemon[i]].currentHP - (damage[i] * (60 - this.model.counter))) / this.model.team[i][this.model.currentPokemon[i]].stats[Stat.HP] < 0.5)
            {
                healthBarFillIndex[i] = 1;
            }
        }

        //renders player health bar fill
        g.drawImage(this.healthBarFill[healthBarFillIndex[0]],
            (int)(width * (9.0 / 10.0)) - (this.statusWindow[0].getWidth(null) * graphicsScaling) + (72 * graphicsScaling),
            height / 2 + (25 * graphicsScaling),
            (int)Math.ceil(this.healthBarFill[0].getWidth(null) * 
                ((this.model.team[0][model.currentPokemon[0]].currentHP - (damage[0] * (60 - this.model.counter))) * 48.0 / this.model.team[0][this.model.currentPokemon[0]].stats[Stat.HP]) * graphicsScaling),
            this.healthBarFill[0].getHeight(null) * graphicsScaling,
            canvas);

        //renders opponent health bar fill
        g.drawImage(this.healthBarFill[healthBarFillIndex[1]],
            width / 10 + (50 * graphicsScaling),
            height / 10 + (24 * graphicsScaling),
            (int)Math.ceil(this.healthBarFill[1].getWidth(null) * 
                ((this.model.team[1][this.model.currentPokemon[1]].currentHP - (damage[1] * (60 - this.model.counter))) * 48.0 / this.model.team[1][this.model.currentPokemon[1]].stats[Stat.HP]) * graphicsScaling),
            this.healthBarFill[1].getHeight(null) * graphicsScaling,
            canvas);

        int xpMin = (int) Math.pow(this.model.team[0][this.model.currentPokemon[0]].level, 3.0);
        int xpMax = (int) Math.pow(this.model.team[0][this.model.currentPokemon[0]].level + 1, 3.0);

        //renders player xp bar fill
        g.drawImage(this.xp,
            (int)(width * (9.0 / 10.0)) - (this.statusWindow[0].getWidth(null) * graphicsScaling) + (24 * graphicsScaling),
            height / 2 + (42 * graphicsScaling),
            (int)(96 * ((double)(this.model.team[0][this.model.currentPokemon[0]].xp - xpMin) / (xpMax - xpMin)) * graphicsScaling),
            this.xp.getHeight(null) * graphicsScaling,
            canvas);

        //displays level of trainer pokemon
        g.drawString(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].level),
            ((int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 106) * graphicsScaling)),
            height / 2 + (18 * graphicsScaling));

        //displays health of trainer pokemon
        g.drawString(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].currentHP),
            (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 88) * graphicsScaling) - g.getFontMetrics(font).stringWidth(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].currentHP)),
            height / 2 + 38 * graphicsScaling);
        g.drawString(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].stats[Stat.HP]),
            (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 97) * graphicsScaling),
            height / 2 + 38 * graphicsScaling);

        //displays name of trainer pokemon
        g.drawString(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].name),
            (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 16) * graphicsScaling),
            height / 2 + (18 * graphicsScaling));

        //displays name of opponent pokemon
        g.drawString(String.valueOf(this.model.team[1][this.model.currentPokemon[1]].name),
            width / 10 + 2 * graphicsScaling,
            height / 10 + 17 * graphicsScaling);

        //displays level of opponent pokemon
        g.drawString(String.valueOf(this.model.team[1][this.model.currentPokemon[1]].level),
            width / 10 + 84 * graphicsScaling,
            height / 10 + 17 * graphicsScaling);

        //renders player  and enemy team at sides of the screen
        for (int j = 0; j < this.pokemonIconSprites.length; j++)
        {
            for (int i = 0; i < this.pokemonIconSprites[j].length; i++)
            {
                g.drawImage(this.pokemonIconSprites[j][i],
                    (int)((width - (this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling)) * j - (8 * graphicsScaling) * Math.pow(-1 , j + 1)),
                    (int) (i * height * (0.75 / 6.0)), 
                    this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling,
                    this.pokemonIconSprites[j][i].getHeight(null) * graphicsScaling,
                    canvas);
            }
        }
        
        displayTextbox(this.textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);
        
        if (this.model.battleOptions != null)
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

        for (int i = 0; i < this.model.battleOptions.length; i++)
        {
            g.drawString(this.model.battleOptions[i], 
                24 * graphicsScaling + (width / 2) * (i % 2), 
                (height * 3 / 4 + 24 * graphicsScaling) + graphicsScaling * 24 * (i / 2));
        }

        int textWidth = g.getFontMetrics(font).stringWidth(">");

        g.drawString(">",
        24 * graphicsScaling + (width / 2) * (this.model.optionIndex % 2) - textWidth,
        (height * 3 / 4 + 24 * graphicsScaling) + graphicsScaling * 24 * (this.model.optionIndex / 2));
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
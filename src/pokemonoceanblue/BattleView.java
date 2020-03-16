package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.awt.AlphaComposite;
import javax.imageio.ImageIO;

/** 
 * Renders the Battle
 */
public class BattleView extends BaseView {

    private BattleModel model;
    private Image[][][] pokemonSprite = new Image[2][][];
    private BufferedImage[][] pokemonBufferedSprite = new BufferedImage[2][];
    private Image[] healthBarFill = new Image[3];
    private Image[] statusWindow = new Image[2];
    private Image background;
    private Image[] backgroundBase = new Image[2];
    private Image xp;
    private Image[] pokeballSprite = new Image[4];
    private Image[][] pokemonIconSprites = new Image[2][];
    private Image trainerSprite;
    private Image[] statusEffectImages = new Image[6];
    private boolean[] hidePokemon = new boolean[2];
    private Image[] partyBorder = new Image[9];
    
    /** 
     * Constructor for the overworld view
     * @param model model for the battle to be displayed
     */
    public BattleView(BattleModel model, byte battleBackgroundId)
    {
        this.model = model;
        loadImage(battleBackgroundId);
    }

    /** 
     * loads all the Pokemon's sprites
     */
    private void loadImage(byte battleBackgroundId) 
    {
        ImageIcon ii;
        String shinyPrefix;
        this.pokemonSprite[0] = new Image[model.team[0].length][2];
        this.pokemonSprite[1] = new Image[model.team[1].length][2];
        this.pokemonBufferedSprite[0] = new BufferedImage[model.team[0].length];
        this.pokemonBufferedSprite[1] = new BufferedImage[model.team[1].length];

        //load players pokemon back sprites
        for (int i = 0; i < pokemonSprite[0].length; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                shinyPrefix = model.team[0][i].shiny ? "shiny" : "";
                ii = new ImageIcon("src/pokemonback/" + shinyPrefix + "frame" + j + "/" + this.model.team[0][i].id + ".png");
                pokemonSprite[0][i][j]  = ii.getImage();
            }

            try
            {
                // load a copy of the Pokemon's sprite recoloured white
                this.pokemonBufferedSprite[0][i] = ImageIO.read(new File("src/pokemonback/frame0/" + this.model.team[0][i].id + ".png"));   
                this.pokemonBufferedSprite[0][i] = this.colorImage(this.pokemonBufferedSprite[0][i], 255, 255, 255);
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemon/frame0/" + this.model.team[0][i].id + ".png");
            }
        }

        //load opponents pokemon front sprites
        for (int i = 0; i < pokemonSprite[1].length; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                shinyPrefix = model.team[1][i].shiny ? "shiny" : "";
                ii = new ImageIcon("src/pokemon/" + shinyPrefix + "frame" + j + "/" + model.team[1][i].id + ".png");
                this.pokemonSprite[1][i][j]  = ii.getImage();
            }

            try
            {
                // load a copy of the Pokemon's sprite recoloured white
                this.pokemonBufferedSprite[1][i] = ImageIO.read(new File("src/pokemon/frame0/" + this.model.team[1][i].id + ".png"));   
                this.pokemonBufferedSprite[1][i] = this.colorImage(this.pokemonBufferedSprite[1][i], 255, 255, 255);
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemon/frame0/" + this.model.team[1][i].id + ".png");
            }
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
                ii = new ImageIcon("src/pokemonicons/" + this.model.team[i][j].getSpriteId() + ".png");
                this.pokemonIconSprites[i][j] = ii.getImage();
            }
        }

        //loads status effect images
        for (int i = 0; i < this.statusEffectImages.length; i++)
        {
            ii = new ImageIcon("src/menus/ailment" + (i + 1) + ".png");
            this.statusEffectImages[i] = ii.getImage();
        }

        //loads status window images and xp
        ii = new ImageIcon("src/battle/TrainerStatusWindow.png");
        this.statusWindow[0] = ii.getImage();
        ii = new ImageIcon("src/battle/OpponentStatusWindow.png");
        this.statusWindow[1] = ii.getImage();
        ii = new ImageIcon("src/battle/exp.png");
        this.xp = ii.getImage();

        //load trainer sprite
        if (this.model.trainerSpriteName != null)
        {
            ii = new ImageIcon("src/trainerBattleSprite/" + model.trainerSpriteName + ".png");
            this.trainerSprite = ii.getImage();
        }

        // load party border sprites
        for (int i = 0; i < partyBorder.length; i++)
        {
            ii = new ImageIcon("src/menus/pcPartyBorder" + i + ".png");
            partyBorder[i]  = ii.getImage();
        }

        // decide if it is day time or night time
        byte timeOfDayCode;
        int hour =  Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour <= 7 || hour > 19)
        {
            timeOfDayCode = 1;
        }
        else 
        {
            timeOfDayCode = 0;
        }
        for (int i = 0; i < this.backgroundBase.length; i++)
        {
            ii = new ImageIcon("src/battle/base" + battleBackgroundId + "" + i + "" + timeOfDayCode + ".png");
            this.backgroundBase[i]  = ii.getImage();
        }
        
        //loads background image
        ii = new ImageIcon("src/battle/Background" + battleBackgroundId + "" + timeOfDayCode + ".png");
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

        // draw base under the player's Pokemon
        g.drawImage(this.backgroundBase[0],
            width / 4 - this.backgroundBase[0].getWidth(null) * graphicsScaling / 2,
            height * 3 / 4 - this.backgroundBase[0].getHeight(null) * graphicsScaling,
            this.backgroundBase[0].getWidth(null) * graphicsScaling,
            this.backgroundBase[0].getHeight(null) * graphicsScaling,
            canvas);

        // draw base under the opponent's Pokemon
        g.drawImage(this.backgroundBase[1],
            width * 6 / 10 - this.backgroundBase[1].getWidth(null) * graphicsScaling / 2,
            height / 2 - this.backgroundBase[1].getHeight(null) * graphicsScaling,
            this.backgroundBase[1].getWidth(null) * graphicsScaling,
            this.backgroundBase[1].getHeight(null) * graphicsScaling,
            canvas);
    

        // renders party boxes at sides of the screen
        this.displayTextbox(this.partyBorder,
            0, 
            0,
            width / 10, 
            height * 3 / 4, 
            g, 
            canvas);

        this.displayTextbox(this.partyBorder,
            width * 9 / 10, 
            0,
            width / 10, 
            height * 3 / 4, 
            g, 
            canvas);

        if (this.model.currentPokemon[0] >= 0)
        {
            if (!this.hidePokemon[0]
                || (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1))
            {
                this.renderPokemon(0, g, canvas);
            }
            this.renderPokemonStatusWindow(0, g, canvas);

            //renders player xp bar fill
            int xpMin = (int) Math.pow(this.model.team[0][this.model.currentPokemon[0]].level, 3.0);
            int xpMax = (int) Math.pow(this.model.team[0][this.model.currentPokemon[0]].level + 1, 3.0);
            int xpCurrent = this.model.team[0][this.model.currentPokemon[0]].xp;

            if (this.model.events.size() > 0)
            {
                xpCurrent = (int) Math.min(xpCurrent + (this.model.events.get(0).xp * (60 - this.model.counter)) / 60.0, xpMax);
            }
            g.drawImage(this.xp,
                (int)(width * (9.0 / 10.0)) - (this.statusWindow[0].getWidth(null) * graphicsScaling) + (24 * graphicsScaling),
                height / 2 + (42 * graphicsScaling),
                (int)(96 * ((double)(xpCurrent - xpMin) / (xpMax - xpMin)) * graphicsScaling),
                this.xp.getHeight(null) * graphicsScaling,
                canvas);

            //displays health of trainer pokemon
            g.drawString(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].currentHP),
                (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 88) * graphicsScaling) - g.getFontMetrics(font).stringWidth(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].currentHP)),
                height / 2 + 38 * graphicsScaling);
            g.drawString(String.valueOf(this.model.team[0][this.model.currentPokemon[0]].stats[Stat.HP]),
                (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 97) * graphicsScaling),
                height / 2 + 38 * graphicsScaling);
        }

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

        else if (this.model.currentPokemon[1] >= 0)
        {
            if ((!this.model.isCaught && !this.hidePokemon[1])
                || (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1))
            {
                this.renderPokemon(1, g, canvas);
            }
            this.renderPokemonStatusWindow(1, g, canvas);
        }

        // render enemy trainer
        if (this.trainerSprite != null)
        {
            g.drawImage(this.trainerSprite,
                width * 17 / 20 - this.trainerSprite.getWidth(null) * graphicsScaling,
                height * 5 / 12 - this.trainerSprite.getHeight(null) * graphicsScaling,
                this.trainerSprite.getWidth(null) * graphicsScaling,
                this.trainerSprite.getHeight(null) * graphicsScaling,
                canvas);
        }

        //renders player  and enemy team at sides of the screen
        for (int j = 0; j < this.pokemonIconSprites.length; j++)
        {
            for (int i = 0; i < this.pokemonIconSprites[j].length; i++)
            {
                if (j == 0 || this.model.isSeen[i])
                {
                    g.drawImage(this.pokemonIconSprites[j][i],
                        (int)((width - (this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling)) * j - (8 * graphicsScaling) * Math.pow(-1 , j + 1)),
                        (int) (i * height * (0.75 / 6.0)), 
                        this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling,
                        this.pokemonIconSprites[j][i].getHeight(null) * graphicsScaling,
                        canvas);
                }
                else
                {
                    g.drawImage(this.pokeballSprite[3],
                        (int)(width - (this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling)),
                        (int) (i * height * (0.75 / 6.0)), 
                        this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling / 2,
                        this.pokemonIconSprites[j][i].getHeight(null) * graphicsScaling / 2,
                        canvas);
                }
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

    private void renderPokemonStatusWindow(int teamIndex, Graphics g, JPanel canvas)
    {
        int x = teamIndex == 0 ? width * 9 / 10 - (this.statusWindow[0].getWidth(null) * graphicsScaling): width / 10;
        int y = teamIndex == 0 ? height / 2 : height / 10;

        //renders Pokemon status window
        g.drawImage(this.statusWindow[teamIndex],
            x,
            y,
            this.statusWindow[teamIndex].getWidth(null) * graphicsScaling,
            this.statusWindow[teamIndex].getHeight(null) * graphicsScaling,
            canvas);

        //displays level of pokemon
        g.drawString(String.valueOf(this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].level),
            x + (106 - teamIndex * 20) * graphicsScaling,
            y + (19 - teamIndex) * graphicsScaling);

        //displays name of pokemon
        g.drawString(String.valueOf(this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].name),
            x + (18 - teamIndex * 14 ) * graphicsScaling,
            y + (18 * graphicsScaling));

        //display status condition
        int statusEffect = this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].statusEffect;
        if (statusEffect > 0)
        {
            g.drawImage(this.statusEffectImages[statusEffect - 1],
                x + (30 - 22 * teamIndex) * graphicsScaling,
                y + (23 - teamIndex) * graphicsScaling,
                this.statusEffectImages[0].getWidth(null) * graphicsScaling,
                this.statusEffectImages[0].getHeight(null) * graphicsScaling,
                canvas);
        }

        //allows for gradual change in health bar visual
        double damage = 0.0;
        if (this.model.events.size() > 0 && this.model.events.get(0).damage > -1 && this.model.events.get(0).target == teamIndex)
        {
            damage = Math.min((this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].currentHP / 60.0), (this.model.events.get(0).damage / 60.0));
        }

        //get health bar colour
        byte healthBarFillIndex = 0;

        if((double)(this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].currentHP 
            - (damage * (60 - this.model.counter))) / this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].stats[Stat.HP] < 0.2)
        {
            healthBarFillIndex = 2;
        }
        else if((double)(this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].currentHP 
            - (damage * (60 - this.model.counter))) / this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].stats[Stat.HP] < 0.5)
        {
            healthBarFillIndex = 1;
        }
        
        //renders player health bar fill
        g.drawImage(this.healthBarFill[healthBarFillIndex],
            x + (72 - 22 * teamIndex) * graphicsScaling,
            y + (25 - teamIndex) * graphicsScaling,
            (int)Math.ceil(this.healthBarFill[0].getWidth(null) * 
                ((this.model.team[teamIndex][model.currentPokemon[teamIndex]].currentHP - (damage * (60 - this.model.counter))) * 48.0 / this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].stats[Stat.HP]) * graphicsScaling),
            this.healthBarFill[0].getHeight(null) * graphicsScaling,
            canvas);
    }

    private void renderPokemon(int teamIndex, Graphics g, JPanel canvas)
    {
        float pokemonScale = this.getPokemonScale(teamIndex);
        int pokemonFrame = this.getPokemonFrame(teamIndex);

        // set x position
        double x = width * 0.25;
        if (teamIndex == 1)
        {
            x = width * 0.6;
        }

        // set y position
        double y = height * 0.75;
        if (teamIndex == 1)
        {
            y = height * 0.5;
        }

        //renders players current pokemon
        g.drawImage(pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][pokemonFrame],
            (int) (x - (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][pokemonFrame].getWidth(null) * pokemonScale * graphicsScaling) / 2),
            (int) (y - (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][pokemonFrame].getHeight(null) * pokemonScale * graphicsScaling)),
            (int) (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][pokemonFrame].getWidth(null) * pokemonScale * graphicsScaling),
            (int) (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][pokemonFrame].getHeight(null) * pokemonScale * graphicsScaling),
            canvas);

        if (pokemonScale < 1.0)
        {
            // render white version of Pokemon overtop of coloured sprite
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - pokemonScale));

            g.drawImage(pokemonBufferedSprite[teamIndex][this.model.currentPokemon[teamIndex]],
                (int) (x - (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][0].getWidth(null) * pokemonScale * graphicsScaling) / 2),
                (int) (y - (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][0].getHeight(null) * pokemonScale * graphicsScaling)),
                (int) (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][0].getWidth(null) * pokemonScale * graphicsScaling),
                (int) (this.pokemonSprite[teamIndex][this.model.currentPokemon[teamIndex]][0].getHeight(null) * pokemonScale * graphicsScaling),
                canvas);

            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private int getPokemonFrame(int teamIndex)
    {
        if (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1 && this.model.counter < 60)
        {
            if (this.model.events.get(0).attacker == teamIndex)
            {
                return this.model.counter / 12 % 2;
            }
        }

        return 0;
    }

    private float getPokemonScale(int teamIndex)
    {
        if (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1 && this.model.counter > 60)
        {
            if (this.model.events.get(0).attacker == teamIndex)
            {
                this.hidePokemon[teamIndex] = false;
                return 1.0f - (this.model.counter - 60) / 40.0f;
            }
        }
        else if (this.model.events.size() > 0 
            && this.model.events.get(0).newPokemonIndex == -1
            && this.model.counter <= 40)
        {
            if (this.model.events.get(0).attacker == teamIndex)
            {
                if (this.model.counter == 1)
                {
                    this.hidePokemon[teamIndex] = true;
                }
                return this.model.counter / 40.0f;
            }
        }

        return 1;
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
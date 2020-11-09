package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.AlphaComposite;
import javax.imageio.ImageIO;
import java.awt.Color;

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
    private Image[] pokeballSprite = new Image[15];
    private Image[][] pokemonIconSprites = new Image[2][];
    private Image trainerSprite;
    private Image[] statusEffectImages = new Image[8];
    private boolean[] hidePokemon = new boolean[2];
    private Image[] partyBorder = new Image[9];
    private Image[] shine = new Image[4];
    
    /** 
     * Constructor for the overworld view
     * @param model model for the battle to be displayed
     */
    public BattleView(BattleModel model, byte battleBackgroundId)
    {
        this.model = model;
        loadImage(battleBackgroundId);
        // if creating a new view for an existing battle, hide the enemy Pokemon if they already died
        if (this.model.currentPokemon[1] > -1 && this.model.team[1][this.model.currentPokemon[1]].currentHP == 0)
        {
            this.hidePokemon[1] = true;
        }
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
            if (this.model.team[0][i].level == 0)
            {
                // don't load battle sprites for eggs
                continue;
            }

            for (int j = 0; j < 2; j++)
            {
                shinyPrefix = model.team[0][i].shiny ? "shiny" : "";
                ii = new ImageIcon(this.getClass().getResource("/pokemonback/" + shinyPrefix + "frame" + j + "/" + this.model.team[0][i].getSpriteId() + ".png"));
                pokemonSprite[0][i][j]  = ii.getImage();
            }

            try
            {
                // load a copy of the Pokemon's sprite recoloured white
                this.pokemonBufferedSprite[0][i] = ImageIO.read(this.getClass().getResource("/pokemonback/frame0/" + this.model.team[0][i].getSpriteId() + ".png"));   
                this.pokemonBufferedSprite[0][i] = this.colorImage(this.pokemonBufferedSprite[0][i], 255, 255, 255);
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemon/frame0/" + this.model.team[0][i].getSpriteId() + ".png");
            }
        }

        //load opponents pokemon front sprites
        for (int i = 0; i < pokemonSprite[1].length; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                shinyPrefix = model.team[1][i].shiny ? "shiny" : "";
                ii = new ImageIcon(this.getClass().getResource("/pokemon/" + shinyPrefix + "frame" + j + "/" + model.team[1][i].getSpriteId() + ".png"));
                this.pokemonSprite[1][i][j]  = ii.getImage();
            }

            try
            {
                // load a copy of the Pokemon's sprite recoloured white
                this.pokemonBufferedSprite[1][i] = ImageIO.read(this.getClass().getResource("/pokemon/frame0/" + this.model.team[1][i].getSpriteId() + ".png"));   
                this.pokemonBufferedSprite[1][i] = this.colorImage(this.pokemonBufferedSprite[1][i], 255, 255, 255);
            }
            catch (IOException e)
            {
                System.out.println("Error loading src/pokemon/frame0/" + this.model.team[1][i].getSpriteId() + ".png");
            }
        }

        //loads health bar fill images
        for (int i = 0; i < 3; i++)
        {  
            ii = new ImageIcon(this.getClass().getResource("/battle/hp" + i + ".png"));
            this.healthBarFill[i] = ii.getImage();
        }

        //loads pokeball sprites
        for (int i = 0; i < this.pokeballSprite.length; i ++)
        {
            ii = new ImageIcon(this.getClass().getResource("/battle/ball3_" + i + ".png"));
            this.pokeballSprite[i] = ii.getImage();
        }

        //loads pokemon icons for both teams
        for (int i = 0; i < this.pokemonIconSprites.length; i++)
        {
            this.pokemonIconSprites[i] = new Image[this.model.team[i].length];
            for (int j = 0; j < this.pokemonIconSprites[i].length; j++)
            {
                ii = new ImageIcon(this.getClass().getResource("/pokemonicons/" + this.model.team[i][j].getSpriteId() + ".png"));
                this.pokemonIconSprites[i][j] = ii.getImage();
            }
        }

        //loads status effect images
        for (int i = 0; i < this.statusEffectImages.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/ailment" + (i + 1) + ".png"));
            this.statusEffectImages[i] = ii.getImage();
        }

        //loads status window images and xp
        ii = new ImageIcon(this.getClass().getResource("/battle/TrainerStatusWindow.png"));
        this.statusWindow[0] = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/battle/OpponentStatusWindow.png"));
        this.statusWindow[1] = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/battle/exp.png"));
        this.xp = ii.getImage();

        //load trainer sprite
        if (this.model.trainerSpriteName != null)
        {
            ii = new ImageIcon(this.getClass().getResource("/trainerBattleSprite/" + model.trainerSpriteName + ".png"));
            this.trainerSprite = ii.getImage();
        }

        // load party border sprites
        for (int i = 0; i < partyBorder.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/pcPartyBorder" + i + ".png"));
            partyBorder[i]  = ii.getImage();
        }

        for (int i = 0; i < this.backgroundBase.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/battle/base" + battleBackgroundId + "" + i + "" + Utils.getTimeOfDayId() + ".png"));
            this.backgroundBase[i]  = ii.getImage();
        }
        
        //loads background image
        ii = new ImageIcon(this.getClass().getResource("/battle/background" + battleBackgroundId + "" + Utils.getTimeOfDayId() + ".png"));
        this.background = ii.getImage();

        for (int i = 0; i < this.shine.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/battle/shine" + i + ".png"));
            this.shine[i]  = ii.getImage();
        }
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
            width / 10,
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

        // display player's pokemon
        if (this.model.currentPokemon[0] >= 0
            && (!this.hidePokemon[0]
            || (this.model.events.size() > 0 
            && this.model.events.get(0).newPokemonIndex > -1)))
        {
            this.renderPokemon(0, g, canvas);
        }

        if (this.model.events.size() > 0 && this.model.events.get(0).itemId > -1)
        {
            int pokeballSpriteIndex = this.model.events.get(0).itemId;
            int pokeballFrame = this.getPokeballFrame();

            double x = width * 0.6 - this.pokeballSprite[pokeballFrame].getWidth(null) * graphicsScaling / 2;
            double y = height / 2 - (this.backgroundBase[1].getHeight(null) / 2 + this.pokeballSprite[pokeballFrame].getHeight(null)) * graphicsScaling;

            // throwing animation
            if (this.model.events.get(0).newPokemonIndex == -2 
                && !this.model.events.get(0).pokeballShake
                && !this.model.events.get(0).text.contains(" caught wild "))
            {
                x *= (1 - (this.model.actionCounter / 30.0));
                y -= (height * 0.15) * (1 - Math.pow(this.model.actionCounter - 20, 2) / 400.0);
            }

            //renders a pokeball in place of enemy pokemon
            g.drawImage(this.pokeballSprite[pokeballFrame],
                (int)x,
                (int)y,
                this.pokeballSprite[pokeballFrame].getWidth(null) * graphicsScaling,
                this.pokeballSprite[pokeballFrame].getHeight(null) * graphicsScaling,
                canvas);
        }

        // render enemy Pokemon
        if (this.model.currentPokemon[1] >= 0)
        {
            if (!this.hidePokemon[1]
                || (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1))
            {
                this.renderPokemon(1, g, canvas);
            }
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

        this.renderWeather(this.model.weather, g, canvas);

        // set text colour back to black
        Color colour = new Color(0, 0, 0, 255);
        g.setColor(colour);

        // render trainer's pokemon status information
        if (this.model.currentPokemon[0] >= 0)
        {
            this.renderPokemonStatusWindow(0, g, canvas);

            //renders player xp bar fill
            int xpMin = (int) Math.pow(this.model.team[0][this.model.currentPokemon[0]].level, 3.0);
            int xpMax = (int) Math.pow(this.model.team[0][this.model.currentPokemon[0]].level + 1, 3.0);
            int xpCurrent = this.model.team[0][this.model.currentPokemon[0]].xp;

            if (this.model.events.size() > 0)
            {
                xpCurrent = (int) Math.min(xpCurrent + (this.model.events.get(0).xp * (60 - this.model.actionCounter)) / 60.0, xpMax);
            }
            g.drawImage(this.xp,
                (int)(width * (9.0 / 10.0)) - (this.statusWindow[0].getWidth(null) * graphicsScaling) + (24 * graphicsScaling),
                height / 2 + (42 * graphicsScaling),
                (int)(96 * ((double)(xpCurrent - xpMin) / (xpMax - xpMin)) * graphicsScaling),
                this.xp.getHeight(null) * graphicsScaling,
                canvas);
        }

        // render enemy Pokemon's status window
        if (this.model.currentPokemon[1] >= 0)
        {
            this.renderPokemonStatusWindow(1, g, canvas);
        }

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

        //renders player and enemy team at sides of the screen
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
                    g.drawImage(this.pokeballSprite[8],
                        (int)(width - (this.pokemonIconSprites[j][i].getWidth(null) * graphicsScaling)),
                        (int) (i * height * (0.75 / 6.0)) - (7 * graphicsScaling), 
                        this.pokeballSprite[8].getWidth(null) * graphicsScaling / 2,
                        this.pokeballSprite[8].getHeight(null) * graphicsScaling / 2,
                        canvas);
                }
            }
        }
        
        if (this.model.battleOptions != null)
        {
            this.displayTextbox(this.textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);
            this.displayTextOptions(g, canvas);
        }
        else if (this.model.getText() != null)
        {
            this.displayText(this.model.getText(), g, canvas);
        }
        else
        {
            // still show a text box even if there's no text to display
            this.displayTextbox(this.textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);
        }
    }

    private void renderPokemonStatusWindow(int teamIndex, Graphics g, JPanel canvas)
    {
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, 12 * graphicsScaling);    
        g.setFont(font);
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

        // displays EX beside the level if the pokemon is a raid boss
        if (this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].raidBoss)
        {
            Font exFont = new Font("Pokemon Fire Red", Font.ITALIC, 12 * graphicsScaling);  
            g.setFont(exFont);
            g.drawString("EX",
                x + (116 - teamIndex * 20) * graphicsScaling,
                y + (19 - teamIndex) * graphicsScaling);
            g.setFont(font);
        }
        
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

        //display gender symbol
        g.drawImage(genderIcons[this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].genderId],
            x + (87 - 22 * teamIndex) * graphicsScaling,
            y + (10 - teamIndex) * graphicsScaling,
            genderIcons[0].getWidth(null) * graphicsScaling,
            genderIcons[0].getHeight(null) * graphicsScaling,
            canvas);

        //allows for gradual change in health bar visual
        double damage = 0.0;
        if (this.model.events.size() > 0 && this.model.events.get(0).damage != 0 && this.model.events.get(0).target == teamIndex)
        {
            damage = Math.min((this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].currentHP / 50.0), (this.model.events.get(0).damage / 50.0))
                * Math.max(0, 50 - this.model.actionCounter);
        }

        int maxHP = this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].stats[Stat.HP];
        double renderHP = Math.min(Math.max(this.model.team[teamIndex][this.model.currentPokemon[teamIndex]].currentHP - damage, 0), maxHP);
        
        //get health bar colour
        byte healthBarFillIndex = 0;

        if (Math.ceil(renderHP) / maxHP < 0.2)
        {
            healthBarFillIndex = 2;
        }
        else if (Math.ceil(renderHP) / maxHP < 0.5)
        {
            healthBarFillIndex = 1;
        }
        
        //renders player health bar fill
        g.drawImage(this.healthBarFill[healthBarFillIndex],
            x + (72 - 22 * teamIndex) * graphicsScaling,
            y + (25 - teamIndex) * graphicsScaling,
            (int)Math.ceil(this.healthBarFill[0].getWidth(null) * renderHP * 48.0 / maxHP * graphicsScaling),
            this.healthBarFill[0].getHeight(null) * graphicsScaling,
            canvas);

        if (teamIndex == 0)
        {
            //displays health of trainer pokemon
            g.drawString(String.valueOf((int) Math.ceil(renderHP)),
                (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 88) * graphicsScaling) - g.getFontMetrics(font).stringWidth(String.valueOf((int) Math.ceil(renderHP))),
                height / 2 + 38 * graphicsScaling);
            g.drawString(String.valueOf(maxHP),
                (int)(width * (9.0 / 10.0)) - ((this.statusWindow[0].getWidth(null) - 97) * graphicsScaling),
                height / 2 + 38 * graphicsScaling);
        }
    }

    private void renderPokemon(int teamIndex, Graphics g, JPanel canvas)
    {
        float pokemonScale = this.getPokemonScale(teamIndex);
        int pokemonFrame = this.getPokemonFrame(teamIndex);
        int shinyAnimationCounter = this.getShinyAnimationCounter(teamIndex);

        // set x position
        int x = width / 10 + this.backgroundBase[0].getWidth(null) * graphicsScaling / 2;
        if (teamIndex == 1)
        {
            x = (int) (width * 0.6);
        }

        // set y position
        int y = (int) (height * 0.75);
        if (teamIndex == 1)
        {
            y = height / 2 - (this.backgroundBase[1].getHeight(null) / 2) * graphicsScaling;
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
        // display the sparkles when a shiny Pokemon is sent out
        else if (shinyAnimationCounter > 0)
        {
            int[] shineXOffset = new int[]{0, -10, -30};
            int[] shineYOffset = new int[]{-40, -80, -50};

            for (int i = 0; i < shineXOffset.length; i++)
            {
                g.drawImage(shine[(shinyAnimationCounter + i * 4) / 4 % 4], 
                    x + (shineXOffset[i] + 20 * ((shinyAnimationCounter + i * 4) / 4 % 8 / 4)) * graphicsScaling, 
                    y + (shineYOffset[i] + 30 * ((shinyAnimationCounter + i * 4) / 4 % 8 / 4)) * graphicsScaling, 
                    shine[0].getWidth(null) * graphicsScaling, 
                    shine[0].getHeight(null) * graphicsScaling, 
                    canvas
                );
            }
        }
    }

    private int getPokemonFrame(int teamIndex)
    {
        if (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1 && this.model.actionCounter < 60)
        {
            if (this.model.events.get(0).attacker == teamIndex)
            {
                return this.model.actionCounter / 12 % 2;
            }
        }

        return 0;
    }

    private float getPokemonScale(int teamIndex)
    {
        if (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1 && this.model.actionCounter > 60)
        {
            if (this.model.events.get(0).attacker == teamIndex)
            {
                this.hidePokemon[teamIndex] = false;
                return 1.0f - (this.model.actionCounter - 60) / 40.0f;
            }
        }
        else if (this.model.events.size() > 0 
            && this.model.events.get(0).newPokemonIndex == -1
            && this.model.actionCounter <= 40)
        {
            if (this.model.events.get(0).attacker == teamIndex)
            {
                if (this.model.actionCounter == 1)
                {
                    this.hidePokemon[teamIndex] = true;
                }
                return this.model.actionCounter / 40.0f;
            }
        }

        return 1;
    }

    /**
     * Determines the animation counter used for the sparkling animations
     * when a shiny Pokemon is sent out
     * @param teamIndex which team's Pokemon is being rendered
     * @return the animation counter, between 0 and 60
     */
    private int getShinyAnimationCounter(int teamIndex)
    {
        // check if a Pokemon is being sent out
        if (this.model.events.size() > 0 && this.model.events.get(0).newPokemonIndex > -1 && this.model.actionCounter < 60)
        {
            // check if it is shiny
            if (this.model.events.get(0).attacker == teamIndex && this.model.team[teamIndex][this.model.events.get(0).newPokemonIndex].shiny)
            {
                return this.model.actionCounter;
            }
        }

        return 0;
    }

    /**
     * Determines the frame of the pokeball throwing animation to display
     * @return the index of the frame the be rendered
     */
    private int getPokeballFrame()
    {
        if (this.model.events.size() > 0)
        {
            if (this.model.events.get(0).newPokemonIndex > -2)
            {
                return 9;
            }
            else if (this.model.events.get(0).pokeballShake)
            {
                if (this.model.actionCounter <= 15)
                {
                    return 11 + this.model.events.size() % 2;
                }
                else
                {
                    return 8;
                }
            }
            else if (this.model.events.get(0).text.contains(" caught wild "))
            {
                return 14;
            }
            else
            {
                return 8 - this.model.actionCounter / 8;
            }
        }
        return 8;
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
}
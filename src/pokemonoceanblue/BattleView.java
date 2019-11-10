package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** 
 * Renders the Battle
 */
public class BattleView extends ViewBase {

    private BattleModel model;
    private Image[][] pokemonSprite = new Image[2][6];
    private Image[] healthBarFill = new Image[3];
    private Image[] statusWindow = new Image[2];
    
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

        //load players pokemon back sprites
        for (int i = 0; i < pokemonSprite[0].length; i++)
        {
            ii = new ImageIcon("src/pokemonback/frame0/" + model.team[0][i].id + ".png");
            pokemonSprite[0][i]  = ii.getImage();
        }

        //load opponents pokemon front sprites
        for (int i = 0; i < pokemonSprite[1].length; i++)
        {
            ii = new ImageIcon("src/pokemon/frame0/" + model.team[1][i].id + ".png");
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
    }

    /** 
     * renders the battle screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
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
            if((double)model.team[i][model.currentPokemon[i]].currentHP / model.team[i][model.currentPokemon[i]].hp < 0.2)
            {
                healthBarFillIndex[i] = 2;
            }
            else if((double)model.team[i][model.currentPokemon[i]].currentHP / model.team[i][model.currentPokemon[i]].hp < 0.5)
            {
                healthBarFillIndex[i] = 1;
            }
        }
        
        //renders player health bar fill
        g.drawImage(healthBarFill[healthBarFillIndex[0]],
            (int)(width * (9.0 / 10.0)) - (statusWindow[0].getWidth(null) * graphicsScaling) + (72 * graphicsScaling),
            height / 2 + (25 * graphicsScaling),
            (int)Math.ceil(healthBarFill[0].getWidth(null) * (model.team[0][model.currentPokemon[0]].currentHP * 48.0 / model.team[0][model.currentPokemon[0]].hp) * graphicsScaling),
            healthBarFill[0].getHeight(null) * graphicsScaling,
            canvas);

        //renders opponent health bar fill
        g.drawImage(healthBarFill[healthBarFillIndex[1]],
            width / 10 + (50 * graphicsScaling),
            height / 10 + (24 * graphicsScaling),
            (int)Math.ceil(healthBarFill[1].getWidth(null) * (model.team[1][model.currentPokemon[1]].currentHP * 48.0 / model.team[1][model.currentPokemon[1]].hp) * graphicsScaling),
            healthBarFill[1].getHeight(null) * graphicsScaling,
            canvas);
    }

    @Override
    public String toString(){
        return "BattleView";
    }
}
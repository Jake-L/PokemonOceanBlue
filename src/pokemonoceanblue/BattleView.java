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
    private Image healthBar;
    private Image[] healthBarFill = new Image[3];
    
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

        //loads health bar image
        ii = new ImageIcon("src/battle/hpBarE.png");
        healthBar = ii.getImage();

         for (int i = 0; i < 3; i++)
         {  
            ii = new ImageIcon("src/battle/hp" + i + ".png");
            healthBarFill[i] = ii.getImage();
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
        //renders players current pokemon
        g.drawImage(pokemonSprite[0][model.currentPokemon[0]],
            (int)(width * (1/8.0)),
            (int)(height * (3/4.0) - (pokemonSprite[0][model.currentPokemon[0]].getHeight(null) * graphicsScaling)),
            pokemonSprite[0][model.currentPokemon[0]].getWidth(null) * graphicsScaling,
            pokemonSprite[0][model.currentPokemon[0]].getHeight(null) * graphicsScaling,
            canvas);

        //renders opposing trainers current pokemon
        g.drawImage(pokemonSprite[1][model.currentPokemon[1]],
            (int)(width * (1/2.0)),
            (int)(height * (1/2.0) - (pokemonSprite[1][model.currentPokemon[1]].getHeight(null) * graphicsScaling)),
            pokemonSprite[1][model.currentPokemon[1]].getWidth(null) * graphicsScaling,
            pokemonSprite[1][model.currentPokemon[1]].getHeight(null) * graphicsScaling,
            canvas);
    }

    @Override
    public String toString(){
        return "BattleView";
    }
}
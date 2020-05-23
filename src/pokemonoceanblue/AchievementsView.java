package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import pokemonoceanblue.AchievementsModel.AchievementDataModel;

import java.awt.Font;

public class AchievementsView extends BaseView{

    private AchievementsModel model;
    private Image background;
    private Image[] achievementWindow = new Image[2];

    public AchievementsView(AchievementsModel model)
    {
        super(model);
        this.model = model;
        loadImage();
    }

    private void loadImage()
    {
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/menus/party0.png"));
        this.achievementWindow[0]  = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/menus/party4.png"));
        this.achievementWindow[1]  = ii.getImage();

        ii = new ImageIcon(this.getClass().getResource("/menus/pokemonBackground.png"));
        this.background = ii.getImage();
    }

    /** 
     * renders the pokedex screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        int fontSize = 10 * graphicsScaling;
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
        g.setFont(font);

        g.drawImage(this.background, 0, 0, width, height, canvas);

        int iconHeight = (int)(this.achievementWindow[0].getHeight(null) * graphicsScaling / 1.25);
        int iconWidth = (int)(this.achievementWindow[0].getWidth(null) * graphicsScaling / 1.25);
        this.maxRenderRows = height / (iconHeight);
        this.model.optionWidth = (width * 4 / 5) / (iconWidth);
        this.model.optionHeight = this.model.optionMax / (this.model.optionWidth);
        
        if (this.oldOptionIndex != this.model.optionIndex)
        {
            this.calcIndices();
        }

        this.displayTextbox(textDisplayBox, 0, height / 12, width / 5, height * 4 / 5, g, canvas);

        // display information for the currently hovered achievement
        AchievementDataModel hoveredAchievement = this.model.achievements.get(this.model.optionIndex);
        g.drawString("Name: " + hoveredAchievement.name, 8 * graphicsScaling, height / 10 + 10 * graphicsScaling);
        g.drawString("Progress: " + hoveredAchievement.counter + "/" + hoveredAchievement.requiredValue,
             8 * graphicsScaling, height / 10 + 25 * graphicsScaling);
        this.renderProgressBar(8 * graphicsScaling, height / 10 + 35 * graphicsScaling,
            hoveredAchievement.counter / hoveredAchievement.requiredValue, g, canvas);

        // display achievement description
        this.displayText(
            hoveredAchievement.description, 
            fontSize,
            0, 
            height / 10 + 55 * graphicsScaling, 
            width / 5, 
            height * 4 / 5, 
            g, 
            canvas
        );

        int achievementIndex = 0;
        for (int i = 0; i < this.maxRenderRows; i++)
        {
            for (int j = 0; j < this.model.optionWidth; j++)
            {
                achievementIndex = this.minRenderIndex + (i * this.model.optionWidth) + j;
                if (achievementIndex < this.model.achievements.size())
                {
                    g.drawImage(this.achievementWindow[(achievementIndex == this.model.optionIndex ? 1 : 0)],
                        width / 5 + j * iconWidth + (j + 2) * graphicsScaling,
                        i * iconHeight + (i + 2) * graphicsScaling,
                        iconWidth,
                        iconHeight,
                        canvas);

                    g.drawString(this.model.achievements.get(achievementIndex).name,
                        width / 5 + j * iconWidth + (j + 6) * graphicsScaling,
                        i * iconHeight + (16 + i) * graphicsScaling);

                    renderProgressBar(width / 5 + j * iconWidth + (j + 4) * graphicsScaling,
                        i * iconHeight + (i + 20) * graphicsScaling, 
                        this.model.achievements.get(achievementIndex).counter / this.model.achievements.get(achievementIndex).requiredValue,
                        g, canvas);
                }
            }
        }
    }
    
    @Override
    public String toString()
    {
        return "AchievementsView";
    }
}
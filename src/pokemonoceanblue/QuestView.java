package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Font;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class QuestView extends BaseView {
    private BaseModel model;
    List<ObjectiveModel> quests;

    public QuestView(BaseModel model, List<ObjectiveModel> quests)
    {
        super(model);
        this.model = model;
        this.quests = quests;
        model.optionMax = quests.size() - 1;
    }

    /** 
     * renders the quests screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        int fontSize = 10 * graphicsScaling;
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
        g.setFont(font);

        renderQuestList(0, height / 10, g, canvas);

        if (this.quests.size() > 0)
        {
            renderQuestDetails(height / 4, height / 10, g, canvas);
        }
    }

    private void renderQuestList(int x, int y, Graphics g, JPanel canvas)
    {
        for (int i = 0; i < this.quests.size(); i++)
        {
            g.drawString(
                this.quests.get(i).name, 
                x + 8 * graphicsScaling, 
                y + i * 10 * graphicsScaling
            );
        }
    }

    private void renderQuestDetails(int x, int y, Graphics g, JPanel canvas)
    {
        // display information for the currently hovered quest
        ObjectiveModel hoveredQuest = this.quests.get(this.model.optionIndex);
        g.drawString(
            hoveredQuest.name, 
            x + 8 * graphicsScaling, 
            y + 10 * graphicsScaling
        );

        // display the quest description
        displayText(
            hoveredQuest.description, 
            10 * graphicsScaling, 
            x,
            y + 40 * graphicsScaling, 
            width - x,
            20 * graphicsScaling, 
            g, canvas
        );

        // display the tasks for each quest
        for (int i = 0; i < hoveredQuest.tasks.size(); i++)
        {
            g.drawString(
                hoveredQuest.tasks.get(i).description, 
                x + 8 * graphicsScaling, 
                y + (i * 10 + 60) * graphicsScaling
            );

            //if (hoveredQuest.getRequired() > 1)
            g.drawString(
                hoveredQuest.getCounter() + "/" + hoveredQuest.getRequired(),
                x + 300 * graphicsScaling, 
                y + (i * 10 + 60) * graphicsScaling
            );
        }
    }
}

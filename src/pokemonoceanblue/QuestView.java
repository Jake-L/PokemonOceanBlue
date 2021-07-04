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
        renderQuestList(0, height / 10, g, canvas);

        if (this.quests.size() > 0)
        {
            renderQuestDetails(width / 4, height / 10, g, canvas);
        }
    }

    private void renderQuestList(int x, int y, Graphics g, JPanel canvas)
    {
        int renderCount = 0;
        String[] headers = new String[]{"MAIN QUESTS","SIDE QUESTS","DAILY QUESTS"};
        int[] minIndex = new int[]{1000, 3000, 5000};
        int[] maxIndex = new int[]{2999, 4999, 6999};

        for (int i = 0; i < headers.length; i++)
        {
            int fontSize = 20 * graphicsScaling;
            Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
            g.setFont(font);
    
            g.drawString(
                headers[i], 
                x + 8 * graphicsScaling, 
                y + renderCount * 10 * graphicsScaling
            );
            renderCount++;

            fontSize = 10 * graphicsScaling;
            font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
            g.setFont(font);
    
            // render main quests
            for (int j = 0; j < this.quests.size(); j++)
            {
                if (this.quests.get(j).objectiveId >= minIndex[i] && this.quests.get(j).objectiveId <= maxIndex[i])
                {
                    g.drawString(
                        this.quests.get(j).name, 
                        x + 8 * graphicsScaling, 
                        y + renderCount * 10 * graphicsScaling
                    );

                    renderCount++;
                }
            }

            renderCount++;
        }        
    }

    private void renderQuestDetails(int x, int y, Graphics g, JPanel canvas)
    {
        int fontSize = 20 * graphicsScaling;
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
        g.setFont(font);

        // display information for the currently hovered quest
        ObjectiveModel hoveredQuest = this.quests.get(this.model.optionIndex);
        g.drawString(
            hoveredQuest.name, 
            x, 
            y
        );

        fontSize = 10 * graphicsScaling;
        font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
        g.setFont(font);


        // display the quest description
        displayText(
            hoveredQuest.description, 
            10 * graphicsScaling, 
            x,
            y + 20 * graphicsScaling, 
            width - x,
            20 * graphicsScaling, 
            g, canvas
        );

        // display the tasks for each quest
        for (int i = 0; i < hoveredQuest.tasks.size(); i++)
        {
            g.drawString(
                hoveredQuest.tasks.get(i).description, 
                x, 
                y + (i * 10 + 60) * graphicsScaling
            );

            g.drawString(
                hoveredQuest.tasks.get(i).counter + "/" + hoveredQuest.tasks.get(i).requiredValue,
                x + 300 * graphicsScaling, 
                y + (i * 10 + 60) * graphicsScaling
            );
        }
    }
}

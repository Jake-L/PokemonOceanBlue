package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;


import java.util.List;

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
        renderQuestList(0, height / 20, g, canvas);

        if (this.quests.size() > 0)
        {
            renderQuestDetails(width / 4, height / 10, g, canvas);
        }
    }

    private void renderQuestList(int x, int y, Graphics g, JPanel canvas)
    {
        // when we draw the strings, the y coordinate we specify is the bottom,
        // so we start on line 1 rather than line 0
        int renderCount = 1;
        String[] headers = new String[]{"MAIN QUESTS","SIDE QUESTS","DAILY QUESTS"};
        int[] minIndex = new int[]{1000, 3000, 5000};
        int[] maxIndex = new int[]{2999, 4999, 6999};

        Color colour;

        for (int i = 0; i < headers.length; i++)
        {
            // count how many quests appear under each category,
            // and draw the background box
            int questCount = 0;
            for (ObjectiveModel quest : this.quests) {
                if (quest.objectiveId >= minIndex[i] && quest.objectiveId <= maxIndex[i]) {
                    questCount++;
                }
            }

            this.renderHeader(
                x + 6 * graphicsScaling,
                y + 10 * (renderCount - 1) * graphicsScaling,
                questCount,
                g,
                canvas
            ); 

            // set text colour back to black
            colour = new Color(0, 0, 0, 255);
            g.setColor(colour);
            int fontSize = 20 * graphicsScaling;
            Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);      
            g.setFont(font);
    
            g.drawString(
                headers[i], 
                x + 8 * graphicsScaling, 
                y + (renderCount * 10 + 2) * graphicsScaling
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
                        y + (renderCount * 10 + 2) * graphicsScaling
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

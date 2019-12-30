package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

/** 
 * Renders the summary screen
 */
public class SummaryView extends ViewBase {

    private PartyModel model;

    public SummaryView(PartyModel model)
    {
        this.model = model;
    }
    @Override
    public String toString()
    {
        return "SummaryView";
    }
}
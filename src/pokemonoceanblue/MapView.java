package pokemonoceanblue;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Font;

public class MapView extends BaseView 
{
    MapModel model;
    private Image mapSprite;
    private Image[] mapCursor;

    public MapView(MapModel model)
    {
        super(model);
        this.model = model;
        this.loadImage();
    }

    /** 
     * load the map images
     */
    private void loadImage() 
    {
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/menus/map.png"));
        this.mapSprite = ii.getImage();

        this.mapCursor = new Image[2];
        for (int i = 0; i < mapCursor.length; i++)
        {
            ii = new ImageIcon(this.getClass().getResource("/menus/mapCursor" + i + ".png"));
            this.mapCursor[i] = ii.getImage();
        }
    }

    /** 
     * renders the map screen graphics
     * @param g graphics object
     * @param canvas JPanel to draw the images on
     */
    @Override
    public void render(Graphics g, JPanel canvas) 
    {
        g.drawImage(
            this.mapSprite, 
            0, 
            0, 
            this.mapSprite.getWidth(null) * graphicsScaling, 
            this.mapSprite.getHeight(null) * graphicsScaling, 
            canvas);

        g.drawImage(
            this.mapCursor[(int)(System.currentTimeMillis() / 500 % 2)], 
            (this.model.x * 8 - 1) * graphicsScaling, 
            (this.model.y * 8 - 1) * graphicsScaling, 
            this.mapCursor[0].getWidth(null) * graphicsScaling, 
            this.mapCursor[0].getHeight(null) * graphicsScaling, 
            canvas);

        this.displayText(this.model.areaName, g, canvas);
    }


}

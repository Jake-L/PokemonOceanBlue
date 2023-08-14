package pokemonoceanblue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

public abstract class BaseView {
    protected byte graphicsScaling;
    protected int width;
    protected int height;
    protected Image[] textDisplayBox = new Image[9];
    protected BaseModel model;
    protected int minRenderIndex;
    protected int maxRenderRows;
    protected int oldOptionIndex = -1;
    protected static Image[] itemSprite;
    protected Image progressBar;
    protected Image progressBarFill;
    private Image[] moveBox = new Image[2];
    protected Image[] pokemonBackground = new Image[2];
    protected Image[] summaryHeader = new Image[2];
    protected Image pokemonSideBar;

    // sprites used by many subclasses
    protected static Image arrowSprite;
    protected static Image[] sunnySprite = new Image[3];
    protected static Image[] rainSprite = new Image[6];
    protected static Image[] hailSprite = new Image[5];
    protected static Image[] snowSprite = new Image[3];
    protected static Image sandstormSprite;
    protected static Image[] genderIcons = new Image[2];
    protected static Image[] typeSprites = new Image[18];

    public BaseView() {
        ImageIcon ii;

        for (int i = 0; i < 9; i++) {
            ii = new ImageIcon(this.getClass().getResource("/battle/TextBox" + i + ".png"));
            textDisplayBox[i] = ii.getImage();
        }

        // load sprites only once for all views
        if (itemSprite == null) {
            // load item sprites
            itemSprite = new Image[293];

            for (int i = 0; i < itemSprite.length; i++) {
                try {
                    ii = new ImageIcon(this.getClass().getResource("/inventory/" + i + ".png"));
                    itemSprite[i] = ii.getImage();
                } catch (Exception e) {
                    // until all items are added to the game, some sprites are missing
                }
            }

            // load type sprites
            for (int i = 0; i < typeSprites.length; i++) {
                ii = new ImageIcon(this.getClass().getResource("/menus/type" + i + ".png"));
                typeSprites[i] = ii.getImage();
            }

            // load gender icons
            for (int i = 0; i < genderIcons.length; i++) {
                ii = new ImageIcon(this.getClass().getResource("/menus/gender" + i + ".png"));
                genderIcons[i] = ii.getImage();
            }

            // load arrow sprite
            ii = new ImageIcon(this.getClass().getResource("/inventory/arrow.png"));
            arrowSprite = ii.getImage();

            // load weather sprites
            for (int i = 0; i < sunnySprite.length; i++) {
                ii = new ImageIcon(this.getClass().getResource("/battle/sunny" + i + ".png"));
                sunnySprite[i] = ii.getImage();
            }

            for (int i = 0; i < rainSprite.length; i++) {
                ii = new ImageIcon(this.getClass().getResource("/battle/rain" + i + ".png"));
                rainSprite[i] = ii.getImage();
            }

            for (int i = 0; i < hailSprite.length; i++) {
                ii = new ImageIcon(this.getClass().getResource("/battle/hail" + i + ".png"));
                hailSprite[i] = ii.getImage();
            }

            for (int i = 0; i < snowSprite.length; i++) {
                ii = new ImageIcon(this.getClass().getResource("/battle/snow" + i + ".png"));
                snowSprite[i] = ii.getImage();
            }

            ii = new ImageIcon(this.getClass().getResource("/battle/sandstorm.png"));
            sandstormSprite = ii.getImage();
        }

        for (int i = 0; i < this.moveBox.length; i++) {
            ii = new ImageIcon(this.getClass().getResource("/menus/moveBox" + i + ".png"));
            this.moveBox[i] = ii.getImage();
        }

        ii = new ImageIcon(this.getClass().getResource("/menus/progressBar.png"));
        this.progressBar = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/menus/progressBarFill.png"));
        this.progressBarFill = ii.getImage();
        ii = new ImageIcon(this.getClass().getResource("/menus/pokemonSummarySideBox.png"));
        this.pokemonSideBar = ii.getImage();

        for (int i = 0; i < this.pokemonBackground.length; i++) {
            ii = new ImageIcon(this.getClass().getResource("/menus/pokemonBackground" + i + ".png"));
            this.pokemonBackground[i] = ii.getImage();
            ii = new ImageIcon(this.getClass().getResource("/menus/summaryHeader" + i + ".png"));
            this.summaryHeader[i] = ii.getImage();
        }
    }

    public BaseView(BaseModel model) {
        this();
        this.model = model;
        this.minRenderIndex = this.model.optionMin;
    }

    /**
     * Sets variables for rendering on screen
     * 
     * @param graphicsScaling a factor to multiply by all measurements to fit the
     *                        screen
     * @param width           width of the screen in pixels
     * @param height          height of the screen in pixels
     */
    public void setViewSize(byte graphicsScaling, int width, int height) {
        this.graphicsScaling = graphicsScaling;
        this.width = width;
        this.height = height;

        // force recalculation of indices to adjust for new screen size
        this.oldOptionIndex = -1;
    }

    /**
     * Renders a text box using the given sprites and dimensions
     * 
     * @param boxSprite an array of 9 images, where index 0 is the top left sprite,
     *                  1 is the top sprite, and 8 is the bottom right sprite
     * @param x         x position of text box
     * @param y         y position of text box
     * @param boxWidth  width of text box
     * @param boxHeight height of text box
     * @param g         graphics object
     * @param canvas    JPanel object
     */
    protected void displayTextbox(Image boxSprite[], int x, int y, int boxWidth, int boxHeight, Graphics g,
            JPanel canvas) {
        // sprites are square, so only need to look at width or height
        int boxSize = boxSprite[0].getWidth(null);

        for (int j = y + boxSize * graphicsScaling; j < boxHeight + y - boxSize * graphicsScaling; j += boxSize
                * graphicsScaling) {
            // centre
            for (int i = x + boxSize * graphicsScaling; i < boxWidth + x - boxSize * graphicsScaling; i += boxSize
                    * graphicsScaling) {
                g.drawImage(boxSprite[4], i, j, boxSize * graphicsScaling, boxSize * graphicsScaling, canvas);
            }

            // left
            g.drawImage(boxSprite[3], x, j, boxSize * graphicsScaling, boxSize * graphicsScaling, canvas);

            // right
            g.drawImage(boxSprite[5], x + boxWidth - boxSize * graphicsScaling, j, boxSize * graphicsScaling,
                    boxSize * graphicsScaling, canvas);
        }

        // top left
        g.drawImage(boxSprite[0], x, y, boxSize * graphicsScaling, boxSize * graphicsScaling, canvas);
        // top right
        g.drawImage(boxSprite[2], x + boxWidth - boxSize * graphicsScaling, y, boxSize * graphicsScaling,
                boxSize * graphicsScaling, canvas);
        // top
        g.drawImage(boxSprite[1], x + boxSize * graphicsScaling, y, boxWidth - boxSize * 2 * graphicsScaling,
                boxSize * graphicsScaling, canvas);
        // bottom left
        g.drawImage(boxSprite[6], x, y + boxHeight - boxSize * graphicsScaling, boxSize * graphicsScaling,
                boxSize * graphicsScaling, canvas);
        // bottom right
        g.drawImage(boxSprite[8], x + boxWidth - boxSize * graphicsScaling, y + boxHeight - boxSize * graphicsScaling,
                boxSize * graphicsScaling, boxSize * graphicsScaling, canvas);
        // bottom
        g.drawImage(boxSprite[7], x + boxSize * graphicsScaling, y + boxHeight - boxSize * graphicsScaling,
                boxWidth - boxSize * 2 * graphicsScaling, boxSize * graphicsScaling, canvas);
    }

    /**
     * Renders the given text inside of a text box
     * 
     * @param text   the text to be displayed
     * @param g      Graphics object
     * @param canvas JPanel object
     */
    protected void displayText(String text, Graphics g, JPanel canvas) {
        // display the text box at the bottom of the screen
        this.displayTextbox(textDisplayBox, 0, height * 3 / 4, width, height / 4, g, canvas);

        // fill the box with text
        int fontSize = Math.max(16, (int) (height * 0.105));
        this.displayText(text, fontSize, 0, height * 3 / 4, width, height / 4, g, canvas);
    }

    /**
     * Renders the given text within the given dimensions
     * 
     * @param text      the string to be displayed
     * @param fontSize  the size of the font
     * @param x         the left position of the box the text is being drawn in
     * @param y         the top position of the box the text is being drawn in
     * @param boxWidth  the width of the box the text is being drawn in
     * @param boxHeight the height of the box the text is being drawn in
     * @param g         Graphics object
     * @param canvas    JPanel object
     */
    protected void displayText(String text, int fontSize, int x, int y, int boxWidth, int boxHeight, Graphics g,
            JPanel canvas) {
        if (text == null) {
            return;
        }

        int fontSpacing = fontSize * 2 / 3;

        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);
        g.setFont(font);

        List<String> renderText = new ArrayList<String>();
        renderText.add("");
        String[] splitText = text.replace("$", ",").replace("#", ",").split(" ");
        int index = 0;
        int line = 0;

        // split the string into multiple linse
        while (index < splitText.length && line <= (boxHeight - 16 * graphicsScaling) / fontSpacing) {
            // move to next line when end is reached
            if (g.getFontMetrics(font).stringWidth(renderText.get(line) + splitText[index]) >= boxWidth
                    - 16 * graphicsScaling
                    // force at least on word per line
                    && !renderText.get(line).equals("")) {
                line++;
                renderText.add("");
            }

            renderText.set(line, renderText.get(line) + splitText[index] + " ");
            index++;
        }

        // display the string
        for (int i = 0; i < renderText.size(); i++) {
            g.drawString(renderText.get(i), x + 8 * graphicsScaling, y + (3 * graphicsScaling) + fontSpacing * (i + 1));
        }
    }

    /**
     * Displays the given text options at the right side of the screen
     * 
     * @param textOptions the array of text options to be displayed
     * @param g           Graphics object
     * @param canvas      JPanel object
     */
    protected void displayOptions(String[] textOptions, int optionIndex, Graphics g, JPanel canvas) {
        int fontSize = Math.max(16, height / 10);
        int fontSpacing = fontSize * 2 / 3;

        // set the font
        Font font = new Font("Pokemon Fire Red", Font.PLAIN, fontSize);
        g.setFont(font);

        // determine the width of the text box
        int textWidth = 0;
        for (int i = 0; i < textOptions.length; i++) {
            textWidth = Math.max(g.getFontMetrics(font).stringWidth(textOptions[i]), textWidth);
        }

        // render the text box that contains the options
        displayTextbox(textDisplayBox, width - textWidth - 24 * graphicsScaling,
                height * 3 / 4 - fontSpacing * textOptions.length - 16 * graphicsScaling,
                textWidth + 24 * graphicsScaling, fontSpacing * textOptions.length + 24 * graphicsScaling, g, canvas);

        // render the text
        for (int i = 0; i < textOptions.length; i++) {
            g.drawString(textOptions[i], width - textWidth - 8 * graphicsScaling,
                    height * 3 / 4 - fontSpacing * (textOptions.length - i - 1) - 8 * graphicsScaling);
        }

        // render the arrow
        g.drawImage(arrowSprite, width - textWidth - 21 * graphicsScaling,
                height * 3 / 4 - fontSpacing * (textOptions.length - optionIndex - 1)
                        - (8 + arrowSprite.getHeight(null) * 2) * graphicsScaling,
                arrowSprite.getWidth(null) * 2 * graphicsScaling, arrowSprite.getHeight(null) * 2 * graphicsScaling,
                canvas);
    }

    // render function that gets implemented by extended class
    public abstract void render(Graphics g, JPanel canvas);

    /**
     * @param image image to be recoloured
     * @param rgb   color to set the image
     */
    protected BufferedImage colorImage(BufferedImage image, int rgb) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color originalColor = new Color(image.getRGB(x, y), true);

                // only recolour the pixels that aren't transparent
                if (originalColor.getAlpha() == 255) {
                    image.setRGB(x, y, rgb);
                }
            }
        }
        return image;
    }

    protected void renderWeather(Weather weather, Graphics g, JPanel canvas) {
        switch (weather) {
        case SUNNY:
            this.renderSunny(g, canvas);
            break;
        case RAIN:
            this.renderRain(g, canvas);
            break;
        case SANDSTORM:
            this.renderSandstorm(g, canvas);
            break;
        case HAIL:
            this.renderHail(g, canvas);
            break;
        default:
            break;
        }
    }

    protected void renderSunny(Graphics g, JPanel canvas) {
        // tint the screen yellow
        g.setColor(new Color(1.0f, 1.0f, 0, 0.15f));
        g.fillRect(0, 0, width, height);
    }

    protected void renderRain(Graphics g, JPanel canvas) {
        int rainCounterWidth = (int) Math.round(width / (60.0 * graphicsScaling));
        int rainCounterHeight = (int) Math.round(height / (50.0 * graphicsScaling));
        int counter = width / (20 * graphicsScaling);

        for (int i = 0; i < counter; i++) {
            // pick the frame of the rain animation
            int factor = (int) (((System.currentTimeMillis() + (85 * i)) / 85) % 6);
            // introduces the appearance of randomness into the location of the rain
            int adjust_factor = (int) (((System.currentTimeMillis() + 85 * i) / (85 * 6)) % 2);
            g.drawImage(rainSprite[factor], (12 + 62 * (i % rainCounterWidth) + 37 * adjust_factor) * graphicsScaling,
                    (50 * (i % rainCounterHeight) + 5 * (i / 7) - 37 * adjust_factor) * graphicsScaling,
                    rainSprite[0].getWidth(null) * graphicsScaling, rainSprite[0].getHeight(null) * graphicsScaling,
                    canvas);
        }

        // tint the screen blue
        g.setColor(new Color(0, 0.25f, 0.5f, 0.25f));
        g.fillRect(0, 0, width, height);
    }

    protected void renderHail(Graphics g, JPanel canvas) {
        int rainCounterWidth = (int) Math.round(width / (60.0 * graphicsScaling));
        int rainCounterHeight = (int) Math.round(height / (50.0 * graphicsScaling));
        int counter = width / (20 * graphicsScaling);

        for (int i = 0; i < counter; i++) {
            // pick the frame of the hail animation
            int frame = (int) (((System.currentTimeMillis() + 100 * i) / 300) % 5) + (i % 2);
            int factor = frame;

            // some hail will never get to the large ice chunk stage
            if (i % 2 == 0) {
                frame /= 2;
            }
            // hold the large ice chunks twice as long so it appears "stuck" to the screen
            else if (frame == 5) {
                factor--;
                frame--;
            }

            g.drawImage(hailSprite[frame], (int) (62 * (i % rainCounterWidth) + (factor * 8)) * graphicsScaling,
                    (int) (50 * (i % rainCounterHeight) + 5 * (i / 7) + (factor * 15)) * graphicsScaling,
                    hailSprite[frame].getWidth(null) * graphicsScaling,
                    hailSprite[frame].getHeight(null) * graphicsScaling, canvas);
        }

        // tint the screen a blueish white
        g.setColor(new Color(0.5f, 0.5f, 1.0f, 0.4f));
        g.fillRect(0, 0, width, height);
    }

    protected void renderSandstorm(Graphics g, JPanel canvas) {
        // render the moving sand animation
        for (int i = -1; i <= width / (sandstormSprite.getWidth(null) * graphicsScaling); i++) {
            for (int j = -1; j <= height / (sandstormSprite.getHeight(null) * graphicsScaling); j++) {
                g.drawImage(sandstormSprite,
                        (int) (i * sandstormSprite.getWidth(null)
                                + (System.currentTimeMillis() / 8) % sandstormSprite.getWidth(null)) * graphicsScaling,
                        (int) ((j * sandstormSprite.getHeight(null)
                                + ((System.currentTimeMillis() % 4096) / ((float) sandstormSprite.getHeight(null)))
                                        % sandstormSprite.getHeight(null))
                                * graphicsScaling),
                        sandstormSprite.getWidth(null) * graphicsScaling,
                        sandstormSprite.getHeight(null) * graphicsScaling, canvas);
            }
        }

        // tint the screen brown
        g.setColor(new Color(0.8f, 0.4f, 0, 0.25f));
        g.fillRect(0, 0, width, height);
    }

    /**
     * For views where you scroll through objects, like the Pokedex or Inventory,
     * this function calculates which range of objects should be displayed
     */
    protected void calcIndices() {
        if (this.model.optionIndex < this.minRenderIndex) {
            this.minRenderIndex = Math.max(this.minRenderIndex - this.model.optionWidth, this.model.optionMin);
        } else if (this.model.optionIndex > this.minRenderIndex + this.maxRenderRows * this.model.optionWidth - 1) {
            this.minRenderIndex = Math.min(this.minRenderIndex + this.model.optionWidth, this.model.optionMax - 1);
        }

        this.oldOptionIndex = this.model.optionIndex;
    }

    /**
     * @param x        the x coordinate of the progress bar
     * @param y        the y coordinate of the progress bar
     * @param progress the percent progress of the bar to be filled
     * @param g        graphics object
     * @param canvas   JPanel to draw the images on
     */
    public void renderProgressBar(int x, int y, double progress, Graphics g, JPanel canvas) {
        g.drawImage(this.progressBar, x, y, this.progressBar.getWidth(null) * graphicsScaling,
                this.progressBar.getHeight(null) * graphicsScaling, canvas);

        g.drawImage(this.progressBarFill, x + 3 * graphicsScaling, y + 2 * graphicsScaling,
                (int) (this.progressBarFill.getWidth(null) * progress * 64.0 * graphicsScaling),
                this.progressBarFill.getHeight(null) * graphicsScaling, canvas);
    }

    /**
     * Display one of the Pokemon's moves
     * 
     * @param pokemon the Pokemon to be displayed
     * @param sprite  the Pokemon's front sprite
     * @param style   determines the sprites and position on the screen
     * @param g       graphics object
     * @param canvas  JPanel object
     */
    protected void renderPokemonSidebar(PokemonModel pokemon, Image sprite, int style, Graphics g, JPanel canvas) {
        int x;
        int y = height / 20;

        // render at right side of screen
        if (style == 0) {
            x = width - (88 * graphicsScaling);
        }
        // render at left side of screen
        else {
            x = 4 * graphicsScaling;
        }

        // draw pokemon background
        g.drawImage(this.pokemonSideBar, x,
                y + (this.summaryHeader[style].getHeight(null) + this.pokemonBackground[style].getHeight(null))
                        * graphicsScaling,
                this.pokemonSideBar.getWidth(null) * graphicsScaling,
                this.pokemonSideBar.getHeight(null) * graphicsScaling, canvas);

        // draw pokemon background
        g.drawImage(this.pokemonBackground[style],
                x + (this.summaryHeader[style].getWidth(null) / 2 - this.pokemonBackground[style].getWidth(null) / 2)
                        * graphicsScaling,
                y + this.summaryHeader[style].getHeight(null) * graphicsScaling,
                this.pokemonBackground[style].getWidth(null) * graphicsScaling,
                this.pokemonBackground[style].getHeight(null) * graphicsScaling, canvas);

        // draws pokemon sprite
        g.drawImage(sprite,
                x + (this.summaryHeader[style].getWidth(null) / 2 - sprite.getWidth(null) / 2) * graphicsScaling,
                y + (this.summaryHeader[style].getHeight(null)
                        + (this.pokemonBackground[style].getHeight(null) - 80) / 2) * graphicsScaling,
                sprite.getWidth(null) * graphicsScaling, sprite.getHeight(null) * graphicsScaling, canvas);

        // display header to hold Pokemon's name and level
        g.drawImage(this.summaryHeader[style], x, y, this.summaryHeader[style].getWidth(null) * graphicsScaling,
                this.summaryHeader[style].getHeight(null) * graphicsScaling, canvas);

        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 16 * graphicsScaling));

        // displays pokemon name
        g.drawString(pokemon.getName(), x + 16 * graphicsScaling, y + 17 * graphicsScaling);

        // only show stats for Pokemon, not Eggs
        if (pokemon.level > 0) {
            // displays pokemon level
            g.drawString("Lv." + pokemon.level, x + 16 * graphicsScaling, y + 32 * graphicsScaling);

            // displays Pokemon gender
            g.drawImage(genderIcons[pokemon.genderId], x + 75 * graphicsScaling, y + 9 * graphicsScaling,
                    genderIcons[0].getWidth(null) * graphicsScaling, genderIcons[0].getHeight(null) * graphicsScaling,
                    canvas);
        }
    }

    /**
     * Display one of the Pokemon's moves
     * 
     * @param move      the move to be displayed
     * @param x         left position of render area
     * @param y         top position of render area
     * @param isHovered where the player is currently hovering this move
     * @param g         graphics object
     * @param canvas    JPanel object
     */
    protected void renderMove(MoveModel move, int x, int y, boolean isHovered, boolean detailed, Graphics g,
            JPanel canvas) {
        int moveBoxIndex = detailed ? 0 : 1;

        g.drawImage(this.moveBox[moveBoxIndex], x, y, this.moveBox[moveBoxIndex].getWidth(null) * (graphicsScaling - 1),
                this.moveBox[moveBoxIndex].getHeight(null) * (graphicsScaling - 1), canvas);

        g.drawImage(typeSprites[move.typeId], x + 4 * graphicsScaling, y + 3 * graphicsScaling,
                typeSprites[0].getWidth(null) * (graphicsScaling - 1),
                typeSprites[0].getHeight(null) * (graphicsScaling - 1), canvas);

        g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 12 * graphicsScaling));

        g.drawString(move.name, x + 6 * graphicsScaling + (typeSprites[0].getWidth(null) * (graphicsScaling - 1)),
                y + 11 * graphicsScaling);

        if (detailed) {
            g.setFont(new Font("Pokemon Fire Red", Font.PLAIN, 8 * graphicsScaling));

            g.drawString("DMG: " + (move.power == 0 ? "--" : move.power), x + 4 * graphicsScaling,
                    y + 20 * graphicsScaling);

            g.drawString("ACC: " + (move.accuracy == -1 ? "--" : move.accuracy + "%"), x + 60 * graphicsScaling,
                    y + 20 * graphicsScaling);
        }

        if (isHovered) {
            g.drawImage(arrowSprite, x - 10 * graphicsScaling, y, arrowSprite.getWidth(null) * graphicsScaling,
                    arrowSprite.getHeight(null) * graphicsScaling, canvas);
        }
    }

    public void renderHeader(int x, int y, int rowCount, Graphics g, JPanel canvas) {
        Color colour;
        Graphics2D graphics2 = (Graphics2D) g;
        graphics2.setStroke(new BasicStroke(graphicsScaling));
        RoundRectangle2D roundedRectangle;

        // quest section background rectangle
        roundedRectangle = new RoundRectangle2D.Float(
            x, 
            y, 
            width / 5,
            (14 + 10 * rowCount) * graphicsScaling, 
            5 * graphicsScaling, 
            5 * graphicsScaling
        );

        // lighter fill
        colour = new Color(255, 230, 189, 255);
        g.setColor(colour);
        graphics2.fill(roundedRectangle);

        // header background rectangle
        roundedRectangle = new RoundRectangle2D.Float(
            x, 
            y, 
            width / 5,
            14 * graphicsScaling, 
            5 * graphicsScaling, 
            5 * graphicsScaling
        );

        // lighter fill
        colour = new Color(255, 197, 92, 255);
        g.setColor(colour);
        graphics2.fill(roundedRectangle);

        // quest section background rectangle
        roundedRectangle = new RoundRectangle2D.Float(
            x, 
            y,
            width / 5,
            (14 + 10 * rowCount) * graphicsScaling, 
            5 * graphicsScaling, 
            5 * graphicsScaling
        );

        // darker outline
        colour = new Color(255, 165, 0, 255);
        g.setColor(colour);
        graphics2.draw(roundedRectangle);
    }
}
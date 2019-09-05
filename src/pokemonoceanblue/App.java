package pokemonoceanblue;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

public class App extends JFrame implements KeyListener 
{
    CharacterController playerController;
    CharacterModel playerModel;
    CharacterModel oldPlayerModel;
    CharacterModel[] CPUModel = new CharacterModel[1];
    ViewManager viewManager;
    List<Integer> keysDown = new ArrayList<Integer>(); 
    OverworldModel overworldModel;

    // number of milliseconds between frames
    private final byte FRAME_LENGTH = 32;
    private long startTime;
    private byte graphicsScaling = (byte)(5);

    public App(){
        createAndShowGUI();
    }
    
    public static void main(String[] args) throws Exception 
    {
        JFrame frame = new App();
    }

    private void createAndShowGUI() {
       
        //Create and set up the window.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1800, 900));
        startTime = System.currentTimeMillis();        
        
        // display the view
        viewManager = new ViewManager();
        this.add(viewManager);

        // listen for key press and release
        addKeyListener(this);

        //Display the window.
        this.pack();
        this.setVisible(true);
        
        // set the size of the ViewManager, must come after pack()
        viewManager.setViewSize(graphicsScaling, this.getWidth(), this.getHeight());
        TitleScreenView titleView = new TitleScreenView();
        viewManager.setView(titleView);

        this.update();
    }

    /** unused function that needs to be included to implement KeyListener */
    public void keyTyped(KeyEvent e) {

    }

     /** Add the pressed key to list of pressed keys */
    public void keyPressed(KeyEvent e) {
        if (!keysDown.contains(e.getKeyCode()))
        {
            keysDown.add(e.getKeyCode());
        }

        // pressing any key will advance from the title screen
        if (viewManager.getCurrentView() == "TitleScreen" && System.currentTimeMillis() - startTime > 1000 && playerModel == null )
        {
            setMap(0, 4, 4);
        }
    }
     
    /** Remove the released key from the list of pressed keys */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));
    }

    /** 
     * Loads a new map and passed it to ViewManager
     * @param mapId unique identifier for the new map
     * @param playerX x-coordinate to spawn player
     * @param playerY y-coordinate to spawn player
     */
    public void setMap(int mapId, int playerX, int playerY)
    {
        // create the overworld
        overworldModel = new OverworldModel(mapId);

        // create the player
        if (playerModel != null)
        {
            // if the player moved from another map, keep them facing the same direction as before
            oldPlayerModel = playerModel;
            playerModel = new CharacterModel("red", playerX, playerY, oldPlayerModel.getDirection());
        }
        else
        {
            playerModel = new CharacterModel("red", playerX, playerY);
        }
        playerController = new CharacterController(playerModel);
        playerModel.setOverworldModel(overworldModel);

        OverworldView overworldView = new OverworldView(overworldModel, playerModel);
        viewManager.setView(overworldView);
    }

    public void update()
    {
        // the last time the function was run
        long lastRun;

        // the amount of time to sleep before running again
        int sleepLength;

        lastRun = System.currentTimeMillis();

        while (true) {

            // update on a set interval
            if (System.currentTimeMillis() - lastRun > FRAME_LENGTH)
            {
                // update the players position
                if (viewManager.getCurrentView().equals("Overworld"))
                {
                    playerModel.update();

                    if (oldPlayerModel != null)
                    {
                        // continue to animate the player's movement on the old map during a transition
                        oldPlayerModel.update();

                        if (viewManager.previousViewComplete())
                        {
                            oldPlayerModel = null;
                        }
                    }
                    else
                    {
                        // only read user input when previous map is no longer visible
                        playerController.userInput(keysDown);
                    }
                    
                    // check if player is entering a portal
                    if (playerModel.getMovementCounter() == 15)
                    {
                        Portal portal = overworldModel.checkPortal(playerModel.getX(), playerModel.getY());
                        if (portal != null)
                        {
                            // move to the new map
                            setMap(portal.destMapId, portal.destX, portal.destY);
                        }
                    }
                }
                lastRun = System.currentTimeMillis();
            }

            // render graphics at every opportunity
            viewManager.render();

            // check the amount of time to sleep until the next frame
            sleepLength = (int) (FRAME_LENGTH - (System.currentTimeMillis() - lastRun));

            // sleep until next frame
            if (sleepLength > 0) {
                try {
                    // sleep at most x milliseconds to keep a high graphics FPS
                    Thread.sleep(Math.min(sleepLength, 10));
                } catch (InterruptedException e) {
                    System.out.println(String.format("Thread interrupted: %s", e.getMessage()));
                }
            }

            
        }
    }
    
}
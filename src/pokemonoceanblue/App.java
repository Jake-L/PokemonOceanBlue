package pokemonoceanblue;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

public class App extends JFrame implements KeyListener {

    OverworldController overworldController;
    CharacterController playerController;
    CharacterModel playerModel;
    CharacterModel[] CPUModel = new CharacterModel[1];
    ViewManager viewManager;
    List<Integer> keysDown = new ArrayList<Integer>(); 

    // number of milliseconds between frames
    private final byte FRAME_LENGTH = 32;
    private long startTime;

    public App(){
        createAndShowGUI();
    }
    
    public static void main(String[] args) throws Exception {
        JFrame frame = new App();
    }

    private void createAndShowGUI() {
       
        //Create and set up the window.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(800, 700));
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
        viewManager.setViewSize((byte)(4), this.getWidth(), this.getHeight());
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
        if (viewManager.getCurrentView() == "TitleScreen" && System.currentTimeMillis() - startTime > 1000)
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
        OverworldModel overworldModel = new OverworldModel(mapId);
        overworldController = new OverworldController(overworldModel);

        // create the player
        playerModel = new CharacterModel("red", playerX, playerY);
        playerController = new CharacterController(playerModel);
        playerModel.setOverworldController(overworldController);

        CPUModel[0] = new CharacterModel("cassie", 5, 4);
        CPUModel[0].setOverworldController(overworldController);
        overworldModel.setCPUModel(CPUModel);

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
                    playerController.userInput(keysDown);
                    playerModel.update();
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
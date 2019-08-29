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

    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {

    }

     /** Add the pressed key to list of pressed keys */
    public void keyPressed(KeyEvent e) {
        if (!keysDown.contains(e.getKeyCode()))
        {
            keysDown.add(e.getKeyCode());
        }

        if (viewManager.getCurrentView() == "TitleScreen" && System.currentTimeMillis() - startTime > 1000)
        {
            setMap(0);
        }
    }
     
    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));
    }

    public void setMap(int mapId)
    {
        playerModel = new CharacterModel("red", 4, 4);
        OverworldModel overworldModel = new OverworldModel(0);
        overworldController = new OverworldController(overworldModel);
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
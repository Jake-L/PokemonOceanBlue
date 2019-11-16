package pokemonoceanblue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioSystem;
import java.awt.Font;

public class App extends JFrame implements KeyListener 
{
    CharacterController playerController;
    CharacterModel playerModel;
    CharacterModel oldPlayerModel;
    CharacterModel[] CPUModel = new CharacterModel[1];
    ViewManager viewManager;
    List<Integer> keysDown = new ArrayList<Integer>(); 
    OverworldModel overworldModel;
    PokemonModel[] pokemonTeam = new PokemonModel[6];

    // number of milliseconds between frames
    private final byte FRAME_LENGTH = 32;
    private long startTime;

    public App(){
        createAndShowGUI();
    }
    
    public static void main(String[] args) throws Exception 
    {
        JFrame frame = new App();
    }

    private void createAndShowGUI() {
        DatabaseUtility db = new DatabaseUtility();
        db.prepareDatabase();

        // load custom font
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try
        {
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/pokemonfont.ttf")));            
        }
        catch (Exception e)
        {
            System.out.println("Error loading font");
        }
       
        //Create and set up the window.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        this.setPreferredSize(new Dimension(screenSize.width, screenSize.height - scnMax.bottom));
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
        viewManager.setViewSize(this.getWidth(), this.getHeight());
        // listen for screen resizes
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Component c = (Component)e.getSource();
    
                // Get new size
                Dimension newSize = c.getSize();
                Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
                viewManager.setViewSize(newSize.width - 16, newSize.height - scnMax.bottom - scnMax.top);
            }
        });
        TitleScreenView titleView = new TitleScreenView();
        viewManager.setView(titleView);
        MusicPlayer.setSong("0");

        pokemonTeam[0] = new PokemonModel(1, 5);
        pokemonTeam[1] = new PokemonModel(26, 30);
        pokemonTeam[2] = new PokemonModel(9, 40);
        pokemonTeam[3] = new PokemonModel(34, 5);
        pokemonTeam[4] = new PokemonModel(150, 5);
        pokemonTeam[5] = new PokemonModel(306, 5);

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

            // pressing 'p' opens the party view
            if (e.getKeyCode() == KeyEvent.VK_P)
            {
                viewManager.setView(new PartyView(pokemonTeam));
            }
        }

        // pressing any key will advance from the title screen
        if (viewManager.getCurrentView() == "TitleScreenView" && System.currentTimeMillis() - startTime > 1000 && playerModel == null )
        {
            setMap(0, 4, 4);
        }
    }
     
    /** Remove the released key from the list of pressed keys */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));

        // releasing 'p' exits the party view
        if (e.getKeyCode() == KeyEvent.VK_P)
        {
            OverworldView overworldView = new OverworldView(overworldModel);
            viewManager.setView(overworldView);
        }
    }

    /** 
     * Loads a new map and passed it to ViewManager
     * @param mapId unique identifier for the new map
     * @param playerX x-coordinate to spawn player
     * @param playerY y-coordinate to spawn player
     */
    public void setMap(int mapId, int playerX, int playerY)
    {

        // create the player
        if (playerModel != null)
        {
            // if the player moved from another map, keep them facing the same direction as before
            oldPlayerModel = playerModel;
            playerModel = new CharacterModel("red", playerX, playerY, -1, oldPlayerModel.getDirection());
        }
        else
        {
            playerModel = new CharacterModel("red", playerX, playerY, -1);
        }
        playerController = new CharacterController(playerModel);
        // create the overworld
        overworldModel = new OverworldModel(mapId, playerModel);
        playerModel.setOverworldModel(overworldModel);

        OverworldView overworldView = new OverworldView(overworldModel);
        viewManager.setView(overworldView);
        MusicPlayer.setSong("1");
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
                if (viewManager.getCurrentView().equals("OverworldView"))
                {
                    playerModel.update();
                    overworldModel.update();

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

                // fade music during transition
                MusicPlayer.fadeVolume();

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

    /** 
     * Plays background music
     */
    public static class MusicPlayer 
    {
        private static String currentSong;
        private static String newSong;
        private static Clip currentClip;
        private static int transitionCounter = -40;

        /** 
         * Queues the background music for the given song number
         * @param song the number of the song to be played
         */
        public static void setSong(String song)
        {
            // only switch songs if the new song is different from the one currently being played
            if (currentSong == null)
            {
                newSong = song;
                transitionCounter = -40;
                playSong();
            }
            else if (song != currentSong)
            {
                newSong = song;
                transitionCounter = 40;
            } 
            else
            {
                transitionCounter = -40;
            }
        }

        /** 
         * Begins playing the queued song
         */
        private static void playSong()
        {
            currentSong = newSong;

            // stop any song currently being played
            if (currentClip != null)
            {
                currentClip.stop();
            }
            try
            {
                // open and play the new song
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(String.format("src/music/%s.wav", currentSong)).getAbsoluteFile());
                currentClip = AudioSystem.getClip();
                currentClip.open(audioStream);
                fadeVolume();
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
                //currentClip.start();
            }
            catch (Exception e)
            {
                System.out.println("Error playing music");
            }
        }

        /** 
         * Adjust the volume level
         */
        private static void setVolume(double gain)
        {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }

        /** 
         * Fade volume during transitions between songs
         */
        public static void fadeVolume()
        {
            double gain;

            if (transitionCounter > 0)
            {
                gain = 0.25 * (transitionCounter + 2) / 42;
                transitionCounter--;
            }
            else if (transitionCounter > -40)
            {
                if (currentSong != newSong)
                {
                    playSong();
                }

                gain = 0.25 * (Math.abs(transitionCounter) + 2) / 42;
                transitionCounter--;
            }
            else
            {
                gain = 0.25;
            }

            setVolume(gain);
            
        }
    }
}
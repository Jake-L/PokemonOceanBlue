package pokemonoceanblue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import java.awt.Font;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class App extends JFrame implements KeyListener
{
    private static final long serialVersionUID = -1949827959244745733L;
    private static AppManager appManager;
    private static ViewManager viewManager;
    MusicPlayer musicPlayer;
    List<Integer> keysDown = new ArrayList<Integer>();

    // number of milliseconds between frames
    private final byte FRAME_LENGTH = 32;
    private long startTime;
    

    public App(){
        this.musicPlayer = new MusicPlayer();
        // this code should be uncommented when testing database changes
        DatabaseUtility db = new DatabaseUtility();
        db.prepareDatabase();
        //new MoveAnalysis();
        createAndShowGUI();
    }

    public static void main(String[] args) throws Exception
    {
        new App();
    }

    protected void createAndShowGUI() 
    {
        // load custom font
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try
        {
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/pokemonfont.ttf")));
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

        appManager = new AppManager(screenSize.width, screenSize.height - scnMax.bottom);
        viewManager = appManager.viewManager;

        // set the size of the ViewManager, must come after pack()
        viewManager.setViewSize(this.getWidth(), this.getHeight());
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
        
        this.playSong(0, false);

        // fetch the weather every 10 minutes
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                getWeather(); 
            }
        }, 0, 1000 * 60 * 10);

        this.update();
    }

    /** unused function that needs to be included to implement KeyListener */
    public void keyTyped(KeyEvent e) {

    }

    /** Add the pressed key to list of pressed keys */
    public void keyPressed(KeyEvent e) {
        if (!this.keysDown.contains(e.getKeyCode()))
        {
            this.keysDown.add(e.getKeyCode());
        }

        // pressing any key will advance from the title screen
        if (viewManager.getCurrentView().equals("TitleScreenView") && System.currentTimeMillis() - startTime > 1000 && appManager.playerModel == null)
        {
            // load the player's position from the database
            // try
            // {
            //     DatabaseUtility db = new DatabaseUtility();

            //     String query = "SELECT * FROM player_location";

            //     ResultSet rs = db.runQuery(query);

            //     this.setMap(rs.getInt("map_id"), rs.getInt("x"), rs.getInt("y"), Direction.DOWN);
            // }
            // catch (SQLException ex) 
            // {
            //     ex.printStackTrace();
            //     this.setMap(1, 3, 3);
            // } 
            this.appManager.setMap(306, 12, 12, Direction.DOWN);
        }
    }

    /** Remove the released key from the list of pressed keys */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));
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
                appManager.update(keysDown);

                int musicId = appManager.getMusicId();
                if (musicId > -1)
                {
                    this.playSong(musicId, musicId >= 100);
                }

                String soundEffect = appManager.getSoundEffect();
                if (soundEffect != null)
                {
                    this.playSound(soundEffect);
                }

                // fade music during transition
                musicPlayer.fadeVolume();

                lastRun = System.currentTimeMillis();

                // render graphics
                viewManager.render();
            }

            // check the amount of time to sleep until the next frame
            sleepLength = (int) (FRAME_LENGTH - (System.currentTimeMillis() - lastRun));

            // sleep until next frame
            if (sleepLength > 0) {
                try
                {
                    Thread.sleep(sleepLength, 10);
                }
                catch (InterruptedException e)
                {
                    System.out.println(String.format("Thread interrupted: %s", e.getMessage()));
                }
            }
        }
    }

    /**
     * @param path the sound to be played
     */
    public void playSound(String path)
    {
        try
        {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.getClass().getResourceAsStream(String.format("/sounds/%s.wav", path)));
            Clip currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            currentClip.start();
        }
        catch (Exception e)
        {
            System.out.println("Error playing sound: " + path);
        }
    }

    public void playSong(int musicId, boolean skipTransition)
    {
        musicPlayer.setSong(musicId, skipTransition);
    }

    public static void getWeather()
    {
        try 
        {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=Toronto&appid=e77827ffa3e6cf01166c4a7ecd050960");
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    conn.getInputStream()));
            String inputLine = in.readLine();
            in.close();

            System.out.println(inputLine);
            
            // id beginning with 2 means thunderstorm
            // id beginning with 3 means drizzle
            // id beginning with 5 means rain
            if (inputLine.contains("\"weather\":[{\"id\":2")
                || inputLine.contains("\"weather\":[{\"id\":3")
                || inputLine.contains("\"weather\":[{\"id\":5"))
            {
                appManager.weather = (byte) Weather.RAIN;
                System.out.println("Weather: rain");
            }
            // id beginning with 6 means snow
            else if (inputLine.contains("\"weather\":[{\"id\":6"))
            {
                appManager.weather = (byte) Weather.HAIL;
                System.out.println("Weather: hail");
            }
            else
            {
                appManager.weather = (byte) Weather.NEUTRAL;
                System.out.println("Weather: clear");
            }
        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage());
        }
    }
}

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import java.awt.Font;

public class App extends JFrame implements KeyListener
{
    private static final long serialVersionUID = -1949827959244745733L;
    OverworldController overworldController;
    CharacterModel playerModel;
    CharacterModel oldPlayerModel;
    ViewManager viewManager;
    List<Integer> keysDown = new ArrayList<Integer>();
    OverworldModel overworldModel;
    BattleModel battleModel;
    BaseController battleController;
    BaseController partyController;
    PartyModel partyModel;
    BaseController inventoryController;
    InventoryModel inventoryModel;
    BaseController newPokemonController;
    BaseController summaryController;
    PokedexModel pokedexModel;
    BaseController pokedexController;
    EvolutionCheck evolveCheck;
    PokemonStorageModel pokemonStorageModel;
    PokemonStorageController pokemonStorageController;
    MusicPlayer musicPlayer;
    AchievementsModel achievementsModel;
    BaseController achievementsController;
    DayCareModel dayCareModel;

    List<NewPokemonModel> newPokemonQueue = new ArrayList<NewPokemonModel>();

    // number of milliseconds between frames
    private final byte FRAME_LENGTH = 32;
    private long startTime;

    public App(){
        this.musicPlayer = new MusicPlayer();
        createAndShowGUI();
    }

    public static void main(String[] args) throws Exception
    {
        new App();
    }

    private void createAndShowGUI() {
        // this code should be uncommented when testing database changes
        DatabaseUtility db = new DatabaseUtility();
        db.prepareDatabase();

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

        // display the view
        TitleScreenView titleView = new TitleScreenView();
        viewManager = new ViewManager(titleView, screenSize.width, screenSize.height - scnMax.bottom);
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

        this.partyModel = new PartyModel();
        this.partyModel.addPokemon(0, new PokemonModel(3, 30, false));
        this.partyModel.addPokemon(0, new PokemonModel(4, 15, false));
        this.partyModel.addPokemon(0, new PokemonModel(1, 12, false));
        this.partyModel.team.get(0).xp += 450;
        this.inventoryModel = new InventoryModel();
        this.pokedexModel = new PokedexModel();
        this.pokemonStorageModel = new PokemonStorageModel();
        this.achievementsModel = new AchievementsModel();
        this.evolveCheck = new EvolutionCheck();
        this.dayCareModel = new DayCareModel();

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
        if (viewManager.getCurrentView() == "TitleScreenView" && System.currentTimeMillis() - startTime > 1000 && playerModel == null)
        {
            // load the player's position from the database
            // try
            // {
            //     DatabaseUtility db = new DatabaseUtility();

            //     String query = "SELECT * FROM player_location";

            //     ResultSet rs = db.runQuery(query);

            //     this.setMap(rs.getInt("map_id"), rs.getInt("x"), rs.getInt("y"));
            // }
            // catch (SQLException ex) 
            // {
            //     ex.printStackTrace();
            //     this.setMap(1, 3, 3);
            // } 
            this.setMap(17, 7, 7);
        }
    }

    /** Remove the released key from the list of pressed keys */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));
    }

    public void createTrainerBattle(int battleId)
    {
        battleModel = new BattleModel(partyModel.getTeamArray(), battleId, this);
        this.playSong(battleModel.musicId, true);
        BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
        viewManager.setView(battleView);
        battleController = new BaseController(battleModel);
    }

    public void createWildBattle(int pokemonId, int level)
    {
        this.playSong(100, true);

        //determine if the wild Pokemon is shiny
        Random rand = new Random();
        boolean shiny = rand.nextDouble() < this.pokedexModel.getShinyRate(pokemonId) ? true : false;
        PokemonModel[] team = new PokemonModel[1];
        team[0] = new PokemonModel(pokemonId, level, shiny);

        // create the battle
        battleModel = new BattleModel(team, partyModel.getTeamArray(), this);
        BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
        viewManager.setView(battleView);
        battleController = new BaseController(battleModel);
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
            playerModel = new CharacterModel("red", playerX, playerY, -1, -1, 0, oldPlayerModel.getDirection());
        }
        else
        {
            playerModel = new CharacterModel("red", playerX, playerY, -1, -1, 0, Direction.DOWN);
        }

        // create the overworld
        overworldModel = new OverworldModel(mapId, playerModel, this, this.inventoryModel, this.dayCareModel);
        overworldController = new OverworldController(overworldModel);
        playerModel.setOverworldModel(overworldModel);

        OverworldView overworldView = new OverworldView(overworldModel);
        viewManager.setView(overworldView);
    }

    public void openInventory()
    {
        this.inventoryModel.initialize();
        viewManager.setView(new InventoryView(inventoryModel));
        inventoryController = new BaseController(inventoryModel);
    }

    public void openParty(int currentPokemon, boolean returnSelection)
    {
        this.partyModel.initialize(currentPokemon, returnSelection);
        viewManager.setView(new PartyView(partyModel));
        partyController = new BaseController(partyModel);
    }

    public void openSummary(int currentPokemon, boolean returnSelection)
    {
        this.partyModel.initialize(currentPokemon, returnSelection);
        viewManager.setView(new SummaryView(partyModel, partyModel.team));
        summaryController = new BaseController(partyModel);
    }

    public void openSummaryNewMove(int currentPokemon, MoveModel newMove)
    {
        this.partyModel.initialize(currentPokemon, false);
        this.partyModel.setNewMove(newMove);
        viewManager.setView(new SummaryView(partyModel, partyModel.team));
        summaryController = new BaseController(partyModel);
    }

    public void openPokedex()
    {
        this.pokedexModel.initialize();
        viewManager.setView(new PokedexView(pokedexModel));
        pokedexController = new BaseController(pokedexModel);
    }

    public void openPokemonStorage()
    {
        partyModel.initialize(-1, false);
        pokemonStorageModel.initialize();
        PokemonStorageView psv = new PokemonStorageView(pokemonStorageModel, partyModel);
        pokemonStorageController = new PokemonStorageController(pokemonStorageModel, partyModel);
        viewManager.setView(psv);
    }

    public void openAchievements()
    {
        this.achievementsModel.initialize();
        viewManager.setView(new AchievementsView(achievementsModel));
        achievementsController = new BaseController(achievementsModel);
    }

    /**
     * Save the player's current progress
     */
    public void save()
    {
        DatabaseUtility db = new DatabaseUtility();
        db.savePokemon(this.partyModel.team, this.pokemonStorageModel.pokemonStorage);
        db.savePlayerLocation(this.overworldModel.mapId, this.playerModel.getX(), this.playerModel.getY());
        db.savePokedex(this.pokedexModel.caughtPokemon);
    }

    public void decrementStepCounter()
    {
        for (PokemonModel pokemon : partyModel.team)
        {
            pokemon.decrementStepCounter();
            // add an animation for hatching eggs to the queue
            if (pokemon.level == 0 && pokemon.stepCounter <= 0)
            {
                newPokemonQueue.add(new NewPokemonModel(pokemon));
                this.achievementsModel.setEggsHatched(pokemon.shiny);
            }
        }

        // if any eggs hatched, show the animation
        if (newPokemonQueue.size() > 0 && overworldModel.conversation == null)
        {
            this.partyModel.initialize(-1, false);
            viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
            newPokemonController = new BaseController(newPokemonQueue.get(0));
            newPokemonQueue.remove(0);
        }

        // check if an egg has been generated in the day care
        int newEggId = this.dayCareModel.decrementStepCounter();
        if (newEggId > -1)
        {
            // if an egg has hatched, deposit it right into Pokemon Storage
            Random rand = new Random();
            boolean shiny = rand.nextDouble() < this.pokedexModel.getShinyRate(newEggId) ? true : false;
            this.pokemonStorageModel.addPokemon(new PokemonModel(newEggId, 1, shiny));
            this.achievementsModel.setEggsHatched(shiny);
        }
    }

    /**
     * Handles the catching of new Pokemon
     * @param pokemon the Pokemon that was caught
     */
    public void addPokemon(PokemonModel pokemon)
    {
        // register the new pokemon in pokedex
        if (this.pokedexModel.setCaught(pokemon.base_pokemon_id))
        {
            // track that a new Pokemon was caught
            this.achievementsModel.setNewPokemonObtained(pokemon.base_pokemon_id);
        }
        NewPokemonModel newPokemonModel = new NewPokemonModel(pokemon, partyModel, pokemonStorageModel);
        this.newPokemonQueue.add(newPokemonModel);
        this.achievementsModel.setPokemonCaught(pokemon.shiny);
    }

    /**
     * Handles the catching of new Pokemon
     * This constructor is used when the calling class doesn't have access to pokedexModel
     * so this function determines the shiny chance
     * @param pokemonId the identifier of the Pokemon to be added
     * @param pokemonLevel the level of the Pokemon to be added
     */
    public void addPokemon(int pokemonId, int pokemonLevel)
    {
        Random rand = new Random();
        boolean shiny = rand.nextDouble() < this.pokedexModel.getShinyRate(pokemonId) ? true : false;
        this.addPokemon(new PokemonModel(pokemonId, pokemonLevel, shiny));
    }

    /**
     * Add a Pokemon to your party if there is space, or PC otherwise, without displaying a notification
     */
    public void addPokemonSilent(PokemonModel pokemon)
    {
        if (pokemon == null)
        {
            return;
        }
        else if (this.partyModel.team.size() < 6)
        {
            this.partyModel.team.add(pokemon);
        }
        else 
        {
            this.pokemonStorageModel.addPokemon(pokemon);
        }
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
                // update the battle
                if (viewManager.getCurrentView().equals("BattleView") && this.battleModel != null)
                {
                    battleController.userInput(keysDown);

                    String sound = this.battleModel.getSoundEffect();
                    if (sound != null)
                    {
                        this.playSound(sound);
                    }

                    if (this.battleModel.isComplete())
                    {
                        this.overworldModel.battleComplete();
                        if (this.battleModel.trainerSpriteName != null)
                        {
                            this.achievementsModel.setBattlesWon(this.battleModel.trainerSpriteName);
                        }

                        // check which pokemon leveled up
                        boolean[] evolveQueue = this.battleModel.getEvolveQueue();

                        // if a new Pokemon was caught, show it
                        if (this.battleModel.getNewPokemon() != null)
                        {
                            this.addPokemon(this.battleModel.getNewPokemon());
                        }

                        this.checkEvolution(evolveQueue);

                        if (newPokemonQueue.size() > 0)
                        {
                            this.partyModel.initialize(-1, false);
                            viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
                            newPokemonController = new BaseController(newPokemonQueue.get(0));
                            newPokemonQueue.remove(0);
                        }
                        // return to overworld screen
                        else
                        {
                            OverworldView overworldView = new OverworldView(overworldModel);
                            viewManager.setView(overworldView);
                        }

                        this.battleModel = null;
                    }
                }
                // update the players position
                else if (viewManager.getCurrentView().equals("OverworldView"))
                {
                    playerModel.update(true);
                    overworldModel.update();

                    if (oldPlayerModel != null)
                    {
                        // continue to animate the player's movement on the old map during a transition
                        oldPlayerModel.update(false);

                        if (viewManager.previousViewComplete())
                        {
                            oldPlayerModel = null;
                        }
                    }
                    // check if player is entering a portal
                    else if (playerModel.getMovementCounter() == 14)
                    {
                        PortalModel portal = overworldModel.checkPortalModel(playerModel.getX(), playerModel.getY());
                        if (portal != null)
                        {
                            // move to the new map
                            setMap(portal.destMapId, portal.destX, portal.destY);
                        }
                    }
                    else if (newPokemonQueue.size() > 0 && overworldModel.conversation == null)
                    {
                        this.partyModel.initialize(-1, false);
                        viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
                        newPokemonController = new BaseController(newPokemonQueue.get(0));
                        newPokemonQueue.remove(0);
                    }
                    else if (this.battleModel == null)
                    {
                        // only read user input when previous map is no longer visible
                        overworldController.userInput(keysDown);
                    }
                }
                else if (viewManager.getCurrentView().equals("PartyView"))
                {
                    if (partyController != null)
                    {
                        partyController.userInput(keysDown);
                        int returnValue = partyModel.getSelection();
                        if (partyModel.isSummary && returnValue > -1)
                        {
                            this.partyController = null;
                            this.openSummary(-1, partyModel.returnSelection);
                        }

                        else if (returnValue >= -1)
                        {
                            this.partyController = null;

                            if (this.battleModel != null)
                            {
                                this.battleModel.setPokemon(returnValue);
                                // return to battle screen
                                BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
                                viewManager.setView(battleView);
                            }
                            else
                            {
                                // return to overworld screen
                                OverworldView overworldView = new OverworldView(overworldModel);
                                viewManager.setView(overworldView);
                                // pass the selected Pokemon to overworld model
                                if (overworldModel.setPokemon(returnValue >= 0 ? this.partyModel.team.get(returnValue) : null))
                                {
                                    // remove the Pokemon from the player's team if they were left in the day care, etc.
                                    this.partyModel.team.remove(returnValue);
                                }
                            }
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("SummaryView"))
                {
                    if (summaryController != null)
                    {
                        this.summaryController.userInput(keysDown);
                        if (this.partyModel.newMove != null)
                        {
                            int returnValue = partyModel.getSelection();
                            if (returnValue > -2)
                            {
                                this.summaryController = null;
                                this.battleModel.setNewMove(this.partyModel.newMove, returnValue);
                                // return to battle screen
                                BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
                                viewManager.setView(battleView);
                            }
                        }
                        else if (!partyModel.isSummary)
                        {
                            this.summaryController = null;
                            this.openParty(-1, partyModel.returnSelection);
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("InventoryView"))
                {
                    inventoryModel.update();

                    if (inventoryController != null)
                    {
                        inventoryController.userInput(keysDown);
                        int returnValue = inventoryModel.getSelection();
                        if (returnValue >= -1)
                        {
                            this.inventoryController = null;

                            if (this.battleModel != null)
                            {
                                this.battleModel.setItem(returnValue);
                                // return to battle screen
                                BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
                                viewManager.setView(battleView);
                            }
                            else
                            {
                                // return to overworld screen
                                OverworldView overworldView = new OverworldView(overworldModel);
                                viewManager.setView(overworldView);
                            }
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("NewPokemonView"))
                {
                    if (newPokemonController != null)
                    {
                        newPokemonController.userInput(keysDown);

                        if (newPokemonController.isComplete())
                        {
                            if (newPokemonQueue.size() > 0)
                            {
                                this.partyModel.initialize(-1, false);
                                viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
                                newPokemonController = new BaseController(newPokemonQueue.get(0));
                                newPokemonQueue.remove(0);
                            }
                            else
                            {
                                OverworldView overworldView = new OverworldView(overworldModel);
                                viewManager.setView(overworldView);
                                newPokemonController = null;
                            }
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("PokedexView"))
                {
                    if (pokedexController != null)
                    {
                        pokedexController.userInput(keysDown);

                        if (pokedexController.isComplete())
                        {
                            OverworldView overworldView = new OverworldView(overworldModel);
                            viewManager.setView(overworldView);
                            pokedexController = null;
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("AchievementsView"))
                {
                    if (achievementsController != null)
                    {
                        achievementsController.userInput(keysDown);

                        if (achievementsController.isComplete())
                        {
                            OverworldView overworldView = new OverworldView(overworldModel);
                            viewManager.setView(overworldView);
                            achievementsController = null;
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("PokemonStorageView"))
                {
                    if (pokemonStorageController != null)
                    {
                        pokemonStorageController.userInput(keysDown);
                        pokemonStorageModel.update();

                        if (pokemonStorageController.isComplete())
                        {
                            OverworldView overworldView = new OverworldView(overworldModel);
                            viewManager.setView(overworldView);
                            pokemonStorageController = null;
                        }
                    }
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
     * Checks if any Pokemon can evolve, and adds the evolution to a queue
     */
    private void checkEvolution(boolean[] evolveQueue)
    {
        int evolvedPokemonId = -1;

        for (int i = 0; i < evolveQueue.length; i++)
        {
            if (evolveQueue[i])
            {
                evolvedPokemonId = evolveCheck.checkEvolution(partyModel.team.get(i), overworldModel.mapId);

                if (evolvedPokemonId != -1)
                {
                    // register the new pokemon in pokedex
                    this.pokedexModel.setCaught(evolvedPokemonId);

                    // add the evolution to a queue
                    NewPokemonModel newPokemonModel = new NewPokemonModel(partyModel.team.get(i), partyModel, evolvedPokemonId, i);
                    this.newPokemonQueue.add(newPokemonModel);
                    this.achievementsModel.setPokemonEvolved(false);
                }
            }
        }
    }

    /**
     * Fully heals all the Pokemon in the player's team
     */
    public void healTeam()
    {
        this.partyModel.healTeam();
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
}

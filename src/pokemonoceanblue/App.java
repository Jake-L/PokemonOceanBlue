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
import java.sql.Timestamp;
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
    CharacterModel playerModel;
    CharacterModel oldPlayerModel;
    ViewManager viewManager;
    List<Integer> keysDown = new ArrayList<Integer>();
    OverworldModel overworldModel;
    BattleModel battleModel;
    PartyModel partyModel;
    InventoryModel inventoryModel;
    PokedexModel pokedexModel;
    EvolutionCheck evolveCheck;
    PokemonStorageModel pokemonStorageModel;
    PokemonStorageController pokemonStorageController;
    SummaryModel summaryModel;
    MusicPlayer musicPlayer;
    BaseModel achievementsModel;
    BaseModel questsModel;
    DayCareModel dayCareModel;
    boolean[] badges = new boolean[8];
    int enemyScalingFactor = 0;
    TournamentModel tournamentModel;
    BaseController currentController;

    List<ObjectiveModel> achievements = new ArrayList<ObjectiveModel>();
    List<ObjectiveModel> quests = new ArrayList<ObjectiveModel>();
    List<NewPokemonModel> newPokemonQueue = new ArrayList<NewPokemonModel>();
    List<BaseModel> modelQueue = new ArrayList<BaseModel>();

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

    protected void createAndShowGUI() {
        // this code should be uncommented when testing database changes
        DatabaseUtility db = new DatabaseUtility();
        db.prepareDatabase();

        loadDailyQuests();

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
        this.partyModel.addPokemon(0, new PokemonModel(3, 50, false));
        this.partyModel.addPokemon(0, new PokemonModel(9, 50, true));
        this.partyModel.addPokemon(0, new PokemonModel(6, 50, true));
        this.partyModel.addPokemon(0, new PokemonModel(412, 50, false));
        this.partyModel.addPokemon(0, new PokemonModel(351, 50, true));
        this.partyModel.addPokemon(0, new PokemonModel(15, 50, false));
        this.inventoryModel = new InventoryModel();
        this.pokedexModel = new PokedexModel();
        this.pokemonStorageModel = new PokemonStorageModel();
        this.achievementsModel = new BaseModel();
        this.questsModel = new BaseModel();
        this.evolveCheck = new EvolutionCheck();
        this.dayCareModel = new DayCareModel();

        for (int i = 0; i <= 73; i++)
        {
            this.achievements.add(new ObjectiveModel(i));
        }

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
        if (viewManager.getCurrentView().equals("TitleScreenView") && System.currentTimeMillis() - startTime > 1000 && playerModel == null)
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
            this.setMap(49, 12, 1, Direction.DOWN);
        }
    }

    /** Remove the released key from the list of pressed keys */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(Integer.valueOf(e.getKeyCode()));
    }

    public void createTrainerBattle(int battleId)
    {
        if (battleId >= 2000)
        {
            // legendary encounters also start through conversations
            int[] legendaryData = new DatabaseUtility().getLegendaryData(battleId);
            this.createWildBattle(legendaryData[0], legendaryData[1], true);
        }
        else
        {
            // actual trainer battles
            battleModel = new BattleModel(partyModel.getTeamArray(), battleId, this, this.enemyScalingFactor, overworldModel.weather);
            this.playSong(battleModel.musicId, true);
            BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
            viewManager.setView(battleView);
            this.addModelQueue(battleModel);
        }
    }

    public void createWildBattle(int pokemonId, int level, boolean raidBoss)
    {
        if (this.partyModel.team.size() == 0)
        {
            // prevent starting wild Pokemon battle when the player has no Pokemon
            return;
        }

        this.playSong(100, true);

        //determine if the wild Pokemon is shiny
        Random rand = new Random();
        boolean shiny = rand.nextDouble() < this.pokedexModel.getShinyRate(pokemonId) ? true : false;
        PokemonModel[] team = new PokemonModel[1];
        int levelScaling = 0;

        // scale up trainer's Pokemon and wild Pokemon
        // but not legendary encounters
        if (level < 50)
        {
            levelScaling = Math.min(this.enemyScalingFactor, 50);
        }
        team[0] = new PokemonModel(pokemonId, level + levelScaling, shiny, raidBoss);

        // create the battle
        battleModel = new BattleModel(team, partyModel.getTeamArray(), this, overworldModel.weather);
        BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
        viewManager.setView(battleView);
        this.addModelQueue(battleModel);
    }

    /**
     * Loads a new map and passed it to ViewManager
     * @param mapId unique identifier for the new map
     * @param playerX x-coordinate to spawn player
     * @param playerY y-coordinate to spawn player
     */
    public void setMap(int mapId, int playerX, int playerY, Direction direction)
    {
        // create the player
        oldPlayerModel = playerModel;
        playerModel = new CharacterModel("red", playerX, playerY, -1, -1, 0, direction);

        // create the overworld
        if (mapId == 1000 || mapId == 1100 || mapId == 1200 || mapId == 1300)
        {
            // use a special constructor when in a tournament
            if (this.tournamentModel == null)
            {
                this.tournamentModel = new TournamentModel((mapId - 1000) / 100);
            }

            this.overworldModel = new OverworldModel(mapId, playerModel, this, this.tournamentModel);
        }
        else
        {
            this.overworldModel = new OverworldModel(mapId, playerModel, this, this.inventoryModel, this.dayCareModel);
        }
        
        this.modelQueue.clear();
        this.addModelQueue(this.overworldModel);
        playerModel.setOverworldModel(this.overworldModel);

        OverworldView overworldView = new OverworldView(this.overworldModel);
        this.viewManager.setView(overworldView);

        // check for Pokemon form changes
        for (PokemonModel pokemon : this.partyModel.team)
        {
            pokemon.checkFormChange(this.overworldModel.weather, this.overworldModel.battleBackgroundId);
        }
    }

    public void openInventory()
    {
        this.inventoryModel.initialize();
        viewManager.setView(new InventoryView(inventoryModel));
        this.addModelQueue(this.inventoryModel);
    }

    public void openParty(int currentPokemon, boolean returnSelection)
    {
        loadDailyQuests();
        this.partyModel.initialize(currentPokemon, returnSelection);
        viewManager.setView(new PartyView(partyModel));
        if (!this.modelQueue.get(0).getClass().getSimpleName().equals("PartyModel"))
        {
            this.addModelQueue(this.partyModel);
        }
        
    }

    public void openSummary(int currentPokemon, List<PokemonModel> pokemonList)
    {
        this.summaryModel = new SummaryModel(pokemonList, currentPokemon, null);
        viewManager.setView(new SummaryView(this.summaryModel));
        this.addModelQueue(this.summaryModel);
    }

    public void openSummaryNewMove(int currentPokemon, MoveModel newMove)
    {
        this.summaryModel = new SummaryModel(partyModel.team, currentPokemon, newMove);
        viewManager.setView(new SummaryView(this.summaryModel));
        this.addModelQueue(this.summaryModel);
    }

    public void openPokedex()
    {
        this.pokedexModel.initialize();
        viewManager.setView(new PokedexView(pokedexModel));
        this.addModelQueue(this.pokedexModel);
    }

    public void openPokemonStorage()
    {
        partyModel.initialize(-1, false);
        pokemonStorageModel.initialize();
        PokemonStorageView psv = new PokemonStorageView(pokemonStorageModel, partyModel);
        viewManager.setView(psv);
        this.addModelQueue(this.pokemonStorageModel);
    }

    public void openAchievements()
    {
        this.achievementsModel.initialize();
        viewManager.setView(new AchievementsView(achievementsModel, this.achievements));
        this.addModelQueue(this.achievementsModel);
    }

    public void openQuests()
    {
        this.questsModel.initialize();
        viewManager.setView(new QuestView(questsModel, this.quests));
        this.addModelQueue(this.questsModel);
    }

    public void openController()
    {
        if (this.modelQueue.size() > 0)
        {
            BaseModel newModel = this.modelQueue.get(0);

            // PokemonStorageController has unique constructor arguments
            if (newModel.getClass().getSimpleName().equals("PokemonStorageModel"))
            {
                this.currentController = new PokemonStorageController(this.pokemonStorageModel, this.partyModel);
            }
            // Overworld has a unique controller
            else if (newModel.getClass().getSimpleName().equals("OverworldModel"))
            {
                this.currentController = new OverworldController(this.overworldModel);
            }
            // all other controllers just take the model as an argument
            else
            {
                this.currentController = new BaseController(newModel);
            }
        }
    }

    /**
     * Returns to the preview screen
     * For example, returns from the inventory screen to either the overworld or battle
     * If there are any new Pokemon / evolution animations to show, this function will show them
     */
    public void exitCurrentView()
    {
        this.modelQueue.remove(0);
        this.currentController = null;

        // return to a battle
        if (this.modelQueue.get(0).getClass().getSimpleName().equals("BattleModel"))
        {
            BattleView battleView = new BattleView(this.battleModel, this.overworldModel.getBattleBackgroundId());
            viewManager.setView(battleView);
        }
        // show the new pokemon screen
        else if (newPokemonQueue.size() > 0)
        {
            this.partyModel.initialize(-1, false);
            viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
            this.addModelQueue(newPokemonQueue.get(0));
            newPokemonQueue.remove(0);
        }
        // return to overworld
        else if (this.modelQueue.get(0).getClass().getSimpleName().equals("OverworldModel"))
        {
            OverworldView overworldView = new OverworldView(overworldModel);
            viewManager.setView(overworldView);
        }
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
                this.achievements.forEach((obj) -> obj.incrementCounter("hatchEgg", 1));
            }
        }

        // if any eggs hatched, show the animation
        if (newPokemonQueue.size() > 0 && overworldModel.conversation == null)
        {
            this.partyModel.initialize(-1, false);
            viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
            this.addModelQueue(this.newPokemonQueue.get(0));
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
            this.achievements.forEach((obj) -> obj.incrementCounter("hatchEgg", 1));
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
            this.achievements.forEach((obj) -> obj.setNewPokemonCaught(pokemon.base_pokemon_id));
        }

        // lower the Pokemon's stats to normal if it was caught in a raid
        if (pokemon.raidBoss)
        {
            pokemon.loadStats();
        }

        NewPokemonModel newPokemonModel = new NewPokemonModel(pokemon, partyModel, pokemonStorageModel);
        this.newPokemonQueue.add(newPokemonModel);
        this.achievements.forEach((obj) -> obj.setPokemonCaught(pokemon));
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
        PokemonModel pokemon = new PokemonModel(pokemonId, pokemonLevel, shiny);

        // silently add a Pokemon if it's an egg
        if (pokemonLevel == 0)
        {
            this.addPokemonSilent(pokemon);
        }
        else
        {
            this.addPokemon(pokemon);
        }
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

    /**
     * Add a model the queue to be displayed
     * @param model the new model to be displayed
     */
    public void addModelQueue(BaseModel model)
    {
        this.modelQueue.add(0, model);
        this.currentController = null;
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
                if (this.currentController != null)
                {
                    // update the existing controller
                    this.currentController.userInput(keysDown);
                }
                else if (this.viewManager.previousViewComplete())
                {
                    // create the new controller
                    this.openController();

                    // heal Pokemon after respawning in their house
                    // done here to their health bar doesn't get filled in the battle screen
                    if (this.partyModel.isDefeated())
                    {
                        this.healTeam();
                    }
                }

                // update the battle
                if (viewManager.getCurrentView().equals("BattleView") && this.battleModel != null)
                {
                    String sound = this.battleModel.getSoundEffect();
                    if (sound != null)
                    {
                        this.playSound(sound);
                    }

                    if (this.battleModel.isComplete() && this.partyModel.isDefeated())
                    {
                        // player is defeated, respawn in his house
                        this.battleModel = null;
                        this.setMap(1, 7, 4, Direction.DOWN);
                    }
                    else if (this.battleModel.isComplete())
                    {
                        if (this.battleModel.trainerSpriteName != null)
                        {
                            this.achievements.forEach((obj) -> obj.incrementCounter("battleWin", 1, this.battleModel.trainerSpriteName));
                        }

                        // check if the player earned money or other reward
                        this.inventoryModel.addItem(this.battleModel.getBattleReward());

                        // check if the player earned a badge
                        if (this.battleModel.badgeIndex > -1 
                            && !this.badges[this.battleModel.badgeIndex] 
                            // make sure they don't earn badges from fighting gym leaders in tournaments
                            && this.overworldModel.mapId < 1000)
                        {
                            this.badges[this.battleModel.badgeIndex] = true;
                            this.enemyScalingFactor += 5;
                        }

                        // check which pokemon leveled up
                        boolean[] evolveQueue = this.battleModel.getEvolveQueue();

                        // if a new Pokemon was caught, show it
                        if (this.battleModel.getNewPokemon() != null)
                        {
                            this.addPokemon(this.battleModel.getNewPokemon());
                        }

                        this.checkEvolution(evolveQueue);

                        // check for Pokemon form changes
                        for (PokemonModel pokemon : this.partyModel.team)
                        {
                            pokemon.checkFormChange(this.overworldModel.weather, this.overworldModel.battleBackgroundId);
                        }

                        // return to overworld screen
                        this.overworldModel.battleComplete();
                        this.exitCurrentView();

                        this.battleModel = null;
                    }
                }
                // update the players position
                else if (viewManager.getCurrentView().equals("OverworldView"))
                {
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
                            setMap(portal.destMapId, portal.destX, portal.destY, portal.direction);
                        }
                    }
                    else if (newPokemonQueue.size() > 0 && overworldModel.conversation == null)
                    {
                        this.partyModel.initialize(-1, false);
                        viewManager.setView(new NewPokemonView(newPokemonQueue.get(0)));
                        this.addModelQueue(newPokemonQueue.get(0));
                        newPokemonQueue.remove(0);
                    }
                    else if (this.tournamentModel != null && overworldModel.conversation == null && overworldModel.mapId % 100 == 0)
                    {
                        // player finished a round of the tournament, move them into waiting room
                        this.tournamentModel.nextRound();
                        if (this.tournamentModel.getCharacter() == null)
                        {
                            // player won tournament
                            this.setMap(this.overworldModel.mapId + 2, 4, 5, Direction.DOWN);
                            this.tournamentModel = null;
                            this.enemyScalingFactor += 1;

                            // start the conversation where the player gets their prize
                            if (this.overworldModel.mapId == 1002)
                            {
                                this.overworldModel.startConversation(82, Direction.UP, null);
                            }
                            else if (this.overworldModel.mapId == 1102)
                            {
                                this.overworldModel.startConversation(83, Direction.UP, null);
                            }
                            else if (this.overworldModel.mapId == 1202)
                            {
                                this.overworldModel.startConversation(84, Direction.UP, null);
                            }
                        }
                        else
                        {
                            // enter the waiting room before the next round
                            this.setMap(this.overworldModel.mapId + 1, 7, 7, Direction.DOWN);
                        }
                    }

                    // update quest progress for completing conversations
                    if (this.overworldModel.completeConversation >= 0)
                    {
                        this.quests.forEach((obj) -> obj.incrementCounter("conversation", 1, String.valueOf(this.overworldModel.completeConversation)));
                        this.overworldModel.completeConversation = -1;
                    }

                    // add new quests
                    if (this.overworldModel.questId > 0)
                    {
                        this.quests.add(new ObjectiveModel(this.overworldModel.questId));
                        this.overworldModel.questId = -1;
                    }
                }
                else if (viewManager.getCurrentView().equals("PartyView"))
                {
                    if (this.currentController != null)
                    {
                        int returnValue = partyModel.getSelection();
                        if (partyModel.isSummary && returnValue > -1)
                        {
                            this.openSummary(returnValue, partyModel.team);
                        }

                        else if (returnValue >= -1)
                        {
                            if (this.battleModel != null)
                            {
                                this.battleModel.setPokemon(returnValue);
                            }
                            else if (overworldModel.setPokemon(returnValue >= 0 ? this.partyModel.team.get(returnValue) : null))
                            {
                                // remove the Pokemon from the player's team if they were left in the day care, etc.
                                this.partyModel.team.remove(returnValue);
                            }

                            // return to overworld or battle screen
                            this.exitCurrentView();
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("SummaryView"))
                {
                    if (this.currentController != null)
                    {
                        if (this.summaryModel.newMove != null)
                        {
                            int returnValue = this.summaryModel.getSelection();
                            if (returnValue > -2)
                            {
                                this.battleModel.setNewMove(this.summaryModel.newMove, returnValue);

                                // return to battle screen
                                this.exitCurrentView();
                            }
                        }
                        else if (this.summaryModel.getSelection() > -2)
                        {
                            // return from the summary screen to the party screen or pokemon storage screen
                            this.modelQueue.remove(0);
                            if (this.modelQueue.get(0).getClass().getSimpleName().equals("PokemonStorageModel"))
                            {
                                this.openPokemonStorage();
                            }
                            else
                            {
                                this.openParty(this.partyModel.battleActivePokemon, partyModel.returnSelection);
                            }
                            this.currentController = null;
                            this.summaryModel = null;
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("InventoryView"))
                {
                    if (this.currentController != null)
                    {
                        int returnValue = inventoryModel.getSelection();
                        if (returnValue > -1)
                        {
                            // in battle, use the item and remove from inventory
                            if (this.battleModel != null)
                            {
                                this.battleModel.setItem(returnValue);
                                this.inventoryModel.removeItem(returnValue, 1);
                            }
                            // in overworld, check if item can be used before removing from inventory
                            else if (this.overworldModel.setItem(returnValue))
                            {
                                this.inventoryModel.removeItem(returnValue, 1);
                            }
                            // return to overworld or battle screen
                            this.exitCurrentView();
                        }
                        else if (returnValue == -1)
                        {
                            // return to overworld or battle screen
                            this.exitCurrentView();
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("PokedexView")
                    || viewManager.getCurrentView().equals("AchievementsView")
                    || viewManager.getCurrentView().equals("QuestView")
                    || viewManager.getCurrentView().equals("NewPokemonView"))
                {
                    if (this.currentController != null)
                    {
                        if (this.currentController.isComplete())
                        {
                            this.exitCurrentView();
                        }
                    }
                }
                else if (viewManager.getCurrentView().equals("PokemonStorageView"))
                {
                    if (this.currentController != null)
                    {
                        if (pokemonStorageModel.getSelection() > -1)
                        {
                            // open the summary for the current selected Pokemon
                            if (this.pokemonStorageModel.categoryIndex == 0)
                            {
                                this.openSummary(pokemonStorageModel.getSelection(), partyModel.team);
                            }
                            else
                            {
                                this.openSummary(pokemonStorageModel.getSelection(), pokemonStorageModel.pokemonStorage);
                            }
                        }

                        else if (this.currentController.isComplete())
                        {
                            this.exitCurrentView();
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
                    this.achievements.forEach((obj) -> obj.incrementCounter("evolve", 1));
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

    /**
     * Checks if new daily quests should be loaded
     * and then selects the daily quests at random
     */
    private void loadDailyQuests()
    {
        try
        { 
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            DatabaseUtility db = new DatabaseUtility();
            boolean[] reload = new boolean[2];

            String query = """ 
            SELECT objective_group_id, frequency, last_reset
            FROM objective_group
            """;

            ResultSet rs = db.runQuery(query);

            // check all of the objective groups to see if they should be loaded
            while (rs.next())
            {
                int objectiveGroupId = rs.getInt("objective_group_id");
                int frequency = rs.getInt("frequency");
                Timestamp lastReset = rs.getTimestamp("last_reset");

                if (frequency == 1)
                {
                    // if a daily quest hasn't been loaded since yesterday
                    // pick a new daily quest
                    if (Utils.isDifferentDay(timestamp, lastReset))
                    {
                        reload[objectiveGroupId] = true;
                    }
                }
            }

            for (int i = 0; i < reload.length; i++)
            {
                query = """ 
                UPDATE objective_group
                SET last_reset = datetime('now', 'localtime')
                WHERE objective_group_id = 
                """ + i;
                db.runUpdate(query);
            }

            // pick all the quests available in the group, and give one to the player
            for (int i = 0; i < reload.length; i++)
            {
                if (reload[i] == true)
                {
                    List<Integer> objectiveList = new ArrayList<Integer>();

                    query = """ 
                    SELECT objective_id
                    FROM objective
                    WHERE objective_group_id = 
                    """ + i;
    
                    rs = db.runQuery(query);
    
                    while (rs.next())
                    {
                        objectiveList.add(rs.getInt("objective_id"));
                    }
    
                    // add a random quest
                    Random rand = new Random();
                    this.quests.add(new ObjectiveModel(objectiveList.get(rand.nextInt(objectiveList.size()))));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}

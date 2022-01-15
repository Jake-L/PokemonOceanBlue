package pokemonoceanblue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pokemonoceanblue.battle.BattleModel;
import pokemonoceanblue.battle.BattleView;

public class AppManager {
    CharacterModel playerModel;
    CharacterModel oldPlayerModel;
    public ViewManager viewManager;
    OverworldModel overworldModel;
    public BattleModel battleModel;
    PartyModel partyModel;
    InventoryModel inventoryModel;
    PokedexModel pokedexModel;
    EvolutionCheck evolveCheck;
    PokemonStorageModel pokemonStorageModel;
    PokemonStorageController pokemonStorageController;
    SummaryModel summaryModel;
    BaseModel achievementsModel;
    BaseModel questsModel;
    DayCareModel dayCareModel;
    boolean[] badges = new boolean[8];
    int enemyScalingFactor = 0;
    TournamentModel tournamentModel;
    BaseController currentController;
    MapModel mapModel;
    TitleScreenView titleView;

    int musicId = -1;
    String soundEffect;

    List<ObjectiveModel> achievements = new ArrayList<ObjectiveModel>();
    List<ObjectiveModel> quests = new ArrayList<ObjectiveModel>();
    List<NewPokemonModel> newPokemonQueue = new ArrayList<NewPokemonModel>();
    List<BaseModel> modelQueue = new ArrayList<BaseModel>();

    public AppManager(int width, int height)
    {
        this.loadData();
        viewManager = new ViewManager(titleView, width, height);
    }

    protected void loadData() 
    {
        loadDailyQuests();

        // display the view
        titleView = new TitleScreenView();
        
        this.musicId = 0;

        this.partyModel = new PartyModel();
        this.partyModel.addPokemon(0, new PokemonModel(3, 70, false));
        this.partyModel.addPokemon(0, new PokemonModel(6, 70, false));
        this.partyModel.addPokemon(0, new PokemonModel(9, 70, false));
        this.inventoryModel = new InventoryModel();
        this.pokedexModel = new PokedexModel();
        this.pokemonStorageModel = new PokemonStorageModel();
        this.achievementsModel = new BaseModel();
        this.questsModel = new BaseModel();
        this.evolveCheck = new EvolutionCheck();
        this.dayCareModel = new DayCareModel();
        this.mapModel = new MapModel(true, -1);

        for (int i = 0; i <= 73; i++)
        {
            this.achievements.add(new ObjectiveModel(i));
        }
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
            this.musicId = battleModel.musicId;
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

        this.musicId = 100;

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

    public void openMap()
    {
        boolean canFly = false;
        if (inventoryModel.getQuantity(38) > 0)
        {
            canFly = true;
        }
        if (!canFly)
        {
            for (int i = 0; i < partyModel.team.size(); i++)
            {
                if (pokemonoceanblue.Type.typeIncludes(pokemonoceanblue.Type.FLYING, partyModel.team.get(i).types) || 
                    pokemonoceanblue.Type.typeIncludes(pokemonoceanblue.Type.DRAGON, partyModel.team.get(i).types))
                {
                    canFly = true;
                    break;
                }
            }
        }
        this.mapModel = new MapModel(canFly, -1);
        viewManager.setView(new MapView(this.mapModel));
        this.addModelQueue(this.mapModel);
    }

    public void openParty(int currentPokemon, boolean returnSelection)
    {
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
        // return to inventory
        else if (this.modelQueue.get(0).getClass().getSimpleName().equals("InventoryModel"))
        {
            InventoryView inventoryView = new InventoryView(inventoryModel);
            viewManager.setView(inventoryView);
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

    public void update(List<Integer> keysDown)
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
            // done here so their health bar doesn't get filled in the battle screen
            if (this.partyModel.isDefeated())
            {
                this.healTeam();
            }
        }

        // update the battle
        if (viewManager.getCurrentView().equals("BattleView") && this.battleModel != null)
        {
            this.soundEffect = this.battleModel.getSoundEffect();

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

                this.checkEvolution(evolveQueue, -1);

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
                this.addQuest(this.overworldModel.questId);
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

                else if (partyModel.openMap)
                {
                    partyModel.openMap = false;
                    this.openMap();
                }

                else if (returnValue >= -1)
                {
                    if (this.battleModel != null)
                    {
                        this.battleModel.setPokemon(returnValue);
                    }
                    else if (this.partyModel.itemId > 0)
                    {
                        if (returnValue > -1)
                        {
                            boolean[] evolveQueue = new boolean[this.partyModel.team.size()];
                            evolveQueue[returnValue] = true;
                            this.checkEvolution(evolveQueue, this.partyModel.itemId);
                            this.partyModel.team.get(returnValue).checkFormChange(this.partyModel.itemId);
                            // if they used an item, return to the overworld
                            this.modelQueue.remove(1);
                        }
                        
                        // if they exited the party screen without using an item,
                        // return them to their inventory
                        this.partyModel.itemId = -1;
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
                    int categoryId = inventoryModel.getCategory(returnValue);

                    // in battle, use the item and remove from inventory
                    if (this.battleModel != null)
                    {
                        // only pokeballs can be used in battle for now
                        if (categoryId == 0)
                        {
                            this.battleModel.setItem(returnValue);
                            this.inventoryModel.removeItem(returnValue, 1);
                        }
                        else
                        {
                            this.battleModel.setItem(-1);
                        }

                        // return to battle screen
                        this.exitCurrentView();
                    }
                    // items to be used on a Pokemon
                    else if (categoryId == 4 || categoryId == 5)
                    {
                        this.partyModel.setItem(returnValue);
                        this.openParty(-1, true);
                    }
                    // in overworld, check if item can be used before removing from inventory
                    else if (this.overworldModel.setItem(returnValue))
                    {
                        this.inventoryModel.removeItem(returnValue, 1);
                        // return to overworld
                        this.exitCurrentView();
                    }
                }
                else if (returnValue == -1)
                {
                    // return to overworld or battle screen
                    this.exitCurrentView();
                }
            }
        }
        else if (viewManager.getCurrentView().equals("MapView"))
        {
            if (this.currentController != null)
            {
                int returnValue = mapModel.getSelection();
                if (returnValue > -1 && mapModel.destX > -1)
                {
                    setMap(mapModel.getMapId(), mapModel.destX, mapModel.destY, Direction.DOWN);
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
                int returnValue = pokemonStorageModel.getSelection();
                if (returnValue > -1)
                {
                    // open the summary for the current selected Pokemon
                    if (this.pokemonStorageModel.categoryIndex == 0)
                    {
                        this.openSummary(returnValue, partyModel.team);
                    }
                    else
                    {
                        this.openSummary(returnValue, pokemonStorageModel.pokemonStorage);
                    }
                }

                else if (this.currentController.isComplete())
                {
                    this.exitCurrentView();
                }
            }
        }
    }

    /**
     * don't add the quest if it's already in the list
     * otherwise add the quest in sorted order
     * @param questId the quest to be added
     */
    private void addQuest(int questId)
    {
        // add to an empty list
        if (this.quests.size() == 0)
        {
            this.quests.add(new ObjectiveModel(questId));
            return;
        }
        int i = 0;
        while (i < this.quests.size() && questId > this.quests.get(i).objectiveId)
        {
            i++;
        }

        // add an objective to the end of the list
        if (i == this.quests.size())
        {
            // don't insert if the quest in already in the list
            if (questId > this.quests.get(i - 1).objectiveId)
            {
                this.quests.add(new ObjectiveModel(questId));
            }
            return;
        }

        // add an objective between two objectives
        else if (questId < this.quests.get(i).objectiveId)
        {
            this.quests.add(i, new ObjectiveModel(questId));
            return;
        }
    }

    /**
     * Checks if any Pokemon can evolve, and adds the evolution to a queue
     */
    private void checkEvolution(boolean[] evolveQueue, int itemId)
    {
        int evolvedPokemonId = -1;

        for (int i = 0; i < evolveQueue.length; i++)
        {
            if (evolveQueue[i])
            {
                evolvedPokemonId = evolveCheck.checkEvolution(partyModel.team.get(i), overworldModel.mapId, itemId);

                if (evolvedPokemonId != -1)
                {
                    // register the new pokemon in pokedex
                    this.pokedexModel.setCaught(evolvedPokemonId);

                    // add the evolution to a queue
                    NewPokemonModel newPokemonModel = new NewPokemonModel(partyModel.team.get(i), partyModel, evolvedPokemonId, i);
                    this.newPokemonQueue.add(newPokemonModel);
                    PokemonModel evolvedPokemon = new PokemonModel(evolvedPokemonId, 1, false);
                    this.achievements.forEach((obj) -> obj.setPokemonEvolved(evolvedPokemon));
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
                    this.addQuest(objectiveList.get(rand.nextInt(objectiveList.size())));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public int getMusicId()
    {
        int oldMusicId = this.musicId;
        this.musicId = -1;
        return oldMusicId;
    }

    public String getSoundEffect()
    {
        String oldSoundEffect = this.soundEffect;
        this.soundEffect = null;
        return oldSoundEffect;
    }
}

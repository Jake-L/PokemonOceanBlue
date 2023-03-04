package pokemonoceanblue.battle;

import pokemonoceanblue.PokemonModel;
import pokemonoceanblue.Weather;

public class WildPokemonBattle extends BattleModel {
    public boolean isCaught;

    public WildPokemonBattle(PokemonModel wildPokemon, PokemonModel[] playerTeam, Weather weather) {
        super(new PokemonModel[] { wildPokemon }, playerTeam, weather);
        this.battleAI = new BattleAI();
        start();
    }

    public WildPokemonBattle(PokemonModel wildPokemon, PokemonModel[] playerTeam) {
        this(wildPokemon, playerTeam, Weather.NEUTRAL);
  }

    @Override
    protected void sendOutOpponentPokemon() {
        BattleEvent event = new BattleEvent("A wild " + this.team[1][0].name + " appeared!", 1, -1);
        event.setNewPokemon(0);
        this.events.add(event);
    }

    @Override
    protected void addPlayerDefeatedEvent() {
        this.events.add(new BattleEvent("Player was defeated by the wild " + this.team[1][0].name + "!", 0, -1));
    }
    
    @Override
    public void update()
    {
        if (this.actionCounter == 1 && this.events.size() > 0 && this.events.get(0).isCaught) {
            this.isCaught = true;
        }
        super.update();
    }

    @Override
    public boolean isComplete() {
        return super.isComplete() || this.isCaught;
    }

    /**
     * @param itemId is the item that will be used
     */
    @Override
    public void setItem(int itemId) {
        BattleEvent event;

        if (itemId == -1) {
            this.loadBattleMenu();
        } else {
            event = new BattleEvent("Trainer used a " + itemId + ".", 0, -1);
            event.setItem(itemId);
            this.events.add(event);

            event = new BattleEvent("Trainer used a " + itemId + ".", 1, -1);
            event.setItem(itemId);
            event.setNewPokemon(-1);
            this.events.add(event);
            this.actionCounter = 60;

            // find the probability of the Pokemon being captured
            double captureChance = battleOperationsManager.captureChanceCalc(this.team[1][this.currentPokemon[1]],
                    itemId);

            int shakeCount = 3;

            // determine if the Pokemon should be captured
            if (ranNum.nextInt(100) + 1 <= captureChance) {
                event = new BattleEvent("Trainer caught wild " + this.team[1][this.currentPokemon[1]].name + "!", 0,
                        -1);
                event.setItem(itemId);
                event.setCaught();
                this.events.add(event);
                this.team[1][this.currentPokemon[1]].pokeballId = itemId;
            } else {
                event = new BattleEvent("The wild " + this.team[1][this.currentPokemon[1]].name + " escaped!", 1, -1);
                event.setNewPokemon(0);
                this.events.add(2, event);
                int enemyMoveIndex = this.battleAI.getAction(this.team[1], this.team[0][this.currentPokemon[0]],
                        this.currentPokemon[1]);
                this.events.add(new BattleEvent(this.enemyAttack(enemyMoveIndex)));
                shakeCount = ranNum.nextInt(3);
            }

            // add events for the Pokeball shaking animation
            for (int i = 0; i < shakeCount; i++) {
                event = new BattleEvent("Trainer used a " + itemId + ".", 0, -1);
                event.setItem(itemId);
                event.setPokeballShake();
                this.events.add(2, event);
            }
        }
    }

    @Override
    protected String[] getBattleOptions() {
        return new String[] { "FIGHT", "POKEMON", "POKEBALLS" };
    }

    @Override
    public PokemonModel getNewPokemon() {
        if (this.isComplete() && this.isCaught) {
            return this.team[1][0];
        } else {
            return null;
        }
    }
}

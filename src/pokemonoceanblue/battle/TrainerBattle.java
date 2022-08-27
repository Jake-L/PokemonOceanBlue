package pokemonoceanblue.battle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pokemonoceanblue.DatabaseUtility;
import pokemonoceanblue.ItemModel;
import pokemonoceanblue.PokemonModel;

public class TrainerBattle extends BattleModel {
    public String trainerName;
    public String trainerSpriteName;

    public TrainerBattle(PokemonModel[] playerTeam, int battleId, int enemyScalingFactor) {
        super(null, playerTeam);
        setUpTrainerBattle(battleId, enemyScalingFactor);
        this.battleAI = new BattleAI(this.trainerName, battleId);
        start(); // TODO: this is messy, should not call BattleModel functions from subclasses
    }

    @Override
    protected void sendOutOpponentPokemon() {
        int nextPokemonIndex = this.currentPokemon[1] + 1;
        BattleEvent event = new BattleEvent(this.trainerName + " sent out " + this.team[1][nextPokemonIndex].name + ".", 1, -1);
        event.setNewPokemon(nextPokemonIndex);
        this.events.add(event);
    }

    @Override
    public void setItem(int itemId) {
        this.loadBattleMenu();
    }

    @Override
    protected String[] getBattleOptions() {
        return new String[] { "FIGHT", "POKEMON" };
    }

    @Override
    public PokemonModel getNewPokemon() {
        return null;
    }

    @Override
    protected void addPlayerDefeatedEvent() {
        this.events.add(new BattleEvent("Player was defeated by " + trainerName + "!", 0, -1));
    }

    private void setUpTrainerBattle(int battleId, int enemyScalingFactor) {
        List<PokemonModel> loadTeam = new ArrayList<PokemonModel>();

        // Pokemon league lets Pokemon's level scale up by max 75
        if (battleId >= 1300) {
            enemyScalingFactor = Math.min(75, enemyScalingFactor);
        }
        // everywhere else Pokemon's level scale up by max 50
        else {
            enemyScalingFactor = Math.min(50, enemyScalingFactor);
        }

        // limit the number of Pokemon the enemy can have in early stages of the game
        int teamLimit = 3;
        if (enemyScalingFactor >= 35) {
            teamLimit = 6;
        } else if (enemyScalingFactor >= 25) {
            teamLimit = 5;
        } else if (enemyScalingFactor >= 10) {
            teamLimit = 4;
        }

        try {
            DatabaseUtility db = new DatabaseUtility();

            // if there are multiple characters with the same conversation id
            // the trainer shown in battle is the one with the lowest character id
            String query = String.format("""
                    SELECT
                        COALESCE(em2.pre_species_id, em1.pre_species_id, b.pokemon_id),
                        b.level + @level_scaling,
                        c.name,
                        c.sprite_name,
                        COALESCE(c.music_id, 101) AS music_id
                    FROM battle b
                    INNER JOIN (select ch.name, ch.sprite_name, ch.music_id, cv.conversation_id, cv.battle_id,
                    ROW_NUMBER() OVER(ORDER BY ch.character_id ASC) AS char_num
                    FROM conversation cv
                    INNER JOIN character ch
                    ON ch.conversation_id = cv.conversation_id
                    AND cv.battle_id = %s) c
                    ON b.battle_id = c.battle_id
                    -- evolution is dynamic based on level scaling
                    LEFT JOIN evolution_methods em1
                    ON b.pokemon_id = em1.evolved_species_id
                    AND b.level + @level_scaling < em1.enemy_minimum_level
                    LEFT JOIN evolution_methods em2
                    ON em1.pre_species_id = em2.evolved_species_id
                    AND b.level + @level_scaling < em2.enemy_minimum_level
                    WHERE char_num = 1
                    LIMIT %s
                    """.replace("@level_scaling", String.valueOf(enemyScalingFactor)), battleId, teamLimit);

            ResultSet rs = db.runQuery(query);

            while (rs.next()) {
                loadTeam.add(new PokemonModel(rs.getInt(1), rs.getInt(2), false));
                this.trainerName = rs.getString(3);
                this.trainerSpriteName = rs.getString(4);
                this.musicId = rs.getInt(5);

                // set the default trainer battle music
                if (this.musicId == -1) {
                    this.musicId = 101;
                }
            }

            this.team[1] = new PokemonModel[loadTeam.size()];
            loadTeam.toArray(this.team[1]);

            // store if the player gets a badge for winning this battle
            this.battleResult.setBadgeIndex(battleId);

            // load the reward for defeating the trainer in battle
            query = "SELECT item_id, quantity FROM battle_reward WHERE battle_id = " + battleId;
            rs = db.runQuery(query);

            // not all battles have a reward
            if (rs.next()) {
                int itemId = rs.getInt(1);
                int quantity = rs.getInt(2);

                // money gets multiplied by the level of the their last pokemon
                if (itemId == 1000) {
                    quantity *= loadTeam.get(loadTeam.size() - 1).level;
                }
                this.battleResult.reward = new ItemModel(itemId, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

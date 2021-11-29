package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;

import org.junit.Test;

import pokemonoceanblue.AppManager;
import pokemonoceanblue.Direction;
import pokemonoceanblue.PokemonModel;

public class AppManagerTests {
    AppManager appManager;
    PokemonModel pokemon;

    @Test
    public void testBattleSwitchPokemon() {
        createApp();

        // start the wild battle
        appManager.createWildBattle(1, 1, false);

        // move through the initial animations
        updateApp(-1);
        assertEquals("BattleView", appManager.viewManager.getCurrentView());
        assertEquals(0, appManager.battleModel.currentPokemon[0]);

        // select POKEMON
        updateApp(KeyEvent.VK_RIGHT);

        // press enter
        updateApp(KeyEvent.VK_ENTER);

        // check that the party screen was opened
        assertEquals("PartyView", appManager.viewManager.getCurrentView());

        // hover the second Pokemon in the party
        updateApp(KeyEvent.VK_RIGHT);

        // select the pokemon to open the text menu
        updateApp(KeyEvent.VK_ENTER);

        // select the pokemon to send into battle
        updateApp(KeyEvent.VK_ENTER);

        // check that the new pokemon is being used
        assertEquals("BattleView", appManager.viewManager.getCurrentView());
        assertEquals(1, appManager.battleModel.currentPokemon[0]);
    }

    private void createApp()
    {
        appManager = new AppManager(100, 100);
        pokemon = new PokemonModel(150, 100, false);
        appManager.addPokemonSilent(pokemon);
        pokemon = new PokemonModel(250, 100, false);
        appManager.addPokemonSilent(pokemon);
        appManager.setMap(0, 1, 1, Direction.DOWN);

        // advance through the title screen
        updateApp(KeyEvent.VK_ENTER);
    }

    private void updateApp(int key)
    {
        List<Integer> keysDown = new ArrayList<Integer>();

        for (int i = 0; i < 200; i++)
        {
            appManager.update(keysDown);
            appManager.viewManager.updateTransition();
        }

        if (key > -1)
        {
            keysDown.add(key);
            appManager.update(keysDown);

            keysDown.clear();

            for (int i = 0; i < 100; i++)
            {
                appManager.update(keysDown);
                appManager.viewManager.updateTransition();
            }
        }

    }
    
}

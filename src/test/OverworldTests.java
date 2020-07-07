package test;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import pokemonoceanblue.Direction;
import pokemonoceanblue.SpriteModel;
import pokemonoceanblue.CharacterModel;
import pokemonoceanblue.OverworldModel;
import pokemonoceanblue.DatabaseUtility;

public class OverworldTests {
    @Test
    public void testMapObjectOverlap() 
    {
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);

        for (int i = 0; i <= 25; i++)
        {
            System.out.println(i);
            OverworldModel overworldModel = new OverworldModel(i, playerModel, new DummyApp(), null, null);

            for (int j = 1; j < overworldModel.mapObjects.size(); j++)
            {
                SpriteModel object1 = overworldModel.mapObjects.get(j);
                SpriteModel object2 = overworldModel.mapObjects.get(j - 1);
                assertTrue("MapObjects array not sorted", object1.y + object1.yAdjust >= object2.y + object2.yAdjust);
                assertTrue(String.format("Identical MapObjects on map %s: %s, %s, %s", i, object1.spriteName, object1.x, object1.y),
                    object1.x != object2.x || object1.y != object2.y || !object1.spriteName.equals(object2.spriteName));
            }
        }
        
    }
}
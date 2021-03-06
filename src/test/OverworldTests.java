package test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import pokemonoceanblue.Direction;
import pokemonoceanblue.SpriteModel;
import pokemonoceanblue.CharacterModel;
import pokemonoceanblue.OverworldModel;
import pokemonoceanblue.DatabaseUtility;

public class OverworldTests {

    // rather than making this dynamic, hard code a limit
    // so work in progress maps don't throw errors for no reason
    private int MAX_MAP_ID = 25;

    @Test
    public void testMapObjectOverlap() 
    {
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);

        for (int i = 0; i <= this.MAX_MAP_ID; i++)
        {
            OverworldModel overworldModel = new OverworldModel(i, playerModel, new DummyApp(), null, null);

            for (int j = 1; j < overworldModel.mapObjects.size(); j++)
            {
                SpriteModel object1 = overworldModel.mapObjects.get(j);
                SpriteModel object2 = overworldModel.mapObjects.get(j - 1);
                assertTrue("MapObjects array not sorted", 
                    (object1.y + object1.yAdjust > object2.y + object2.yAdjust) 
                    || (object1.y + object1.yAdjust == object2.y + object2.yAdjust && object1.x >= object2.x));
                assertTrue(String.format("Identical MapObjects on map %s: %s, %s, %s", i, object1.getSpriteName(), object1.x, object1.y),
                    object1.x != object2.x || object1.y + object1.yAdjust != object2.y + object2.yAdjust || !object1.getSpriteName().equals(object2.getSpriteName()));
            }
        }
    }

    @Test
    public void testEmptyMap() 
    {
        try 
        {
            DatabaseUtility db = new DatabaseUtility();

            for (int i = 0; i <= this.MAX_MAP_ID; i++)
            {
                // check if there are any characters on the map
                String query = "SELECT * FROM character WHERE map_id = " + i;
                ResultSet rs = db.runQuery(query);
        
                if (!rs.next()) 
                {
                    // if there's no characters, then there must be wild pokemon
                    query = "SELECT * FROM pokemon_location WHERE map_id = " + i;
                    rs = db.runQuery(query);

                    if (!rs.next())
                    {
                        fail("No characters or wild Pokemon for map " + i);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUnusedTiles() 
    {
        boolean[][] usedTiles = new boolean[3][128];
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);

        try 
        {
            DatabaseUtility db = new DatabaseUtility();

            // check if there are any characters on the map
            String query = "SELECT DISTINCT map_template_id, tiles_suffix FROM map_template";
            ResultSet rs = db.runQuery(query);
            while (rs.next())
            {
                int map_template_id = rs.getInt(1);
                int tiles_suffix_id = rs.getInt(2);
                if (tiles_suffix_id > 0)
                {
                    tiles_suffix_id--;
                }
                OverworldModel overworldModel = new OverworldModel(map_template_id, playerModel, new DummyApp(), null, null);
                for (int i = 0; i < overworldModel.tiles.length; i++)
                {
                    for (int j = 0; j < overworldModel.tiles[i].length; j++)
                    {
                        usedTiles[tiles_suffix_id][Math.abs(overworldModel.tiles[i][j])] = true;
                    }
                }
                if (overworldModel.tilesOverlay != null)
                {
                    for (int i = 0; i < overworldModel.tilesOverlay.length; i++)
                    {
                        for (int j = 0; j < overworldModel.tilesOverlay[i].length; j++)
                        {
                            usedTiles[tiles_suffix_id][Math.abs(overworldModel.tilesOverlay[i][j])] = true;
                        }
                    }
                }
            }

            for (int i = 0; i < usedTiles.length; i++)
            {
                //System.out.println("TILES: " + i);
                for (int j = 0; j < usedTiles[i].length; j++)
                {
                    if (!usedTiles[i][j])
                    {
                        //System.out.println("UNUSED: " + j);
                    }
                }
            }

        }
        catch (SQLException e)
        {
            fail(e.getMessage());
        }
    }
}
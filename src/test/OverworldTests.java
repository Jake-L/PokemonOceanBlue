package test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.junit.Test;
import org.junit.BeforeClass;

import pokemonoceanblue.Direction;
import pokemonoceanblue.SpriteModel;
import pokemonoceanblue.CharacterModel;
import pokemonoceanblue.OverworldModel;
import pokemonoceanblue.DatabaseUtility;

public class OverworldTests {

    private static int[] map_id_array;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception 
    {
        DatabaseUtility db = new DatabaseUtility();
        String query = "SELECT map_id FROM map_template WHERE map_id < 999";

        ResultSet rs = db.runQuery(query);

        List<Integer> map_list = new ArrayList<Integer>();

        while(rs.next()) 
        {
            map_list.add(rs.getInt("map_id"));
        }   
        
        // convert lists into arrays and pass to ConverstaionEvent object
        map_id_array = map_list.stream().mapToInt(i->i).toArray();
    }
     

    @Test
    public void testMapObjectOverlap() 
    {
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);

        for (int i : map_id_array)
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
    public void testMapNoCharacters() 
    {
        try 
        {
            DatabaseUtility db = new DatabaseUtility();

            for (int i : map_id_array)
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
    public void testMapNoPortals() 
    {
        try 
        {
            DatabaseUtility db = new DatabaseUtility();

            for (int i : map_id_array)
            {
                // check if there are any portals on the map
                String query = "SELECT * FROM portal WHERE map_id = " + i 
                    + " OR dest_map_id = " + i;
                ResultSet rs = db.runQuery(query);
        
                if (!rs.next()) 
                {
                    fail("No portals for map " + i);
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
        boolean[][] usedTiles = new boolean[4][128];
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
                System.out.println(map_template_id);
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
            }

            for (int i = 0; i < usedTiles.length; i++)
            {
                System.out.println("TILES: " + i);
                for (int j = 0; j < usedTiles[i].length; j++)
                {
                    if (!usedTiles[i][j])
                    {
                        System.out.println("UNUSED: " + j);
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
    public void testUnusedOverlayTiles() 
    {
        boolean[] usedTiles = new boolean[128];
        CharacterModel playerModel = new CharacterModel("red", 5, 5, -1, -1, 0, Direction.DOWN);

        try 
        {
            DatabaseUtility db = new DatabaseUtility();

            // check if there are any characters on the map
            String query = "SELECT DISTINCT map_template_id FROM map_template WHERE overlay = 1";
            ResultSet rs = db.runQuery(query);
            while (rs.next())
            {
                int map_template_id = rs.getInt(1);
                System.out.println(map_template_id);
                OverworldModel overworldModel = new OverworldModel(map_template_id, playerModel, new DummyApp(), null, null);

                for (int i = 0; i < overworldModel.tilesOverlay.length; i++)
                {
                    for (int j = 0; j < overworldModel.tilesOverlay[i].length; j++)
                    {
                        usedTiles[Math.abs(overworldModel.tilesOverlay[i][j])] = true;
                    }
                }
            }

            for (int i = 0; i < usedTiles.length; i++)
            {
                if (!usedTiles[i])
                {
                    try
                    {
                        ImageIcon ii = new ImageIcon(this.getClass().getResource(String.format("/tilesOverlay/%s.png", i)));
                        ii.getImage();
                        System.out.println("UNUSED: " + i);
                    }
                    catch (Exception e)
                    {
                        // don't complain about missing tiles if there isn't a sprite
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
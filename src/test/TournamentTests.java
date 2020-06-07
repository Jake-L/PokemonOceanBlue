package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import pokemonoceanblue.TournamentModel;

public class TournamentTests 
{
    @Test
    public void testGetCharacter() 
    {
        TournamentModel tournamentModel = new TournamentModel(0);
        assertNotNull(tournamentModel.getCharacter());
        tournamentModel.nextRound();
        assertNotNull(tournamentModel.getCharacter());
        tournamentModel.nextRound();
        assertNotNull(tournamentModel.getCharacter());
        tournamentModel.nextRound();
        assertNotNull(tournamentModel.getCharacter());
        tournamentModel.nextRound();
        assertNotNull(tournamentModel.getCharacter());
        tournamentModel.nextRound();
        assertNull(tournamentModel.getCharacter());
    }
}
package pokemonoceanblue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioSystem;

/**
 * Plays background music
 */
public class MusicPlayer
{
    private int currentSong = -1;
    private int newSong;
    private Clip currentClip;
    private int transitionCounter = 0;

    public MusicPlayer()
    {

    }

    /**
     * Queues the background music for the given song number
     * @param song the number of the song to be played
     */
    public void setSong(int songId)
    {
        // only switch songs if the new song is different from the one currently being played
        if (currentSong == -1)
        {
            newSong = songId;
            transitionCounter = 0;
            playSong();
            setVolume(0.25);
        }
        else if (songId != currentSong)
        {
            newSong = songId;
            transitionCounter = 25;
        }
        else
        {
            transitionCounter = 0;
        }
    }

    /**
     * Begins playing the queued song
     */
    private void playSong()
    {
        currentSong = newSong;

        // stop any song currently being played
        if (currentClip != null)
        {
            currentClip.stop();
        }
        try
        {
            // open and play the new song
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.getClass().getResource(String.format("/music/%s.wav", currentSong)));
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            fadeVolume();
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        catch (Exception e)
        {
            System.out.println("Error playing music");
        }
    }

    /**
     * Adjust the volume level
     */
    private void setVolume(double gain)
    {
        FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
    }

    /**
     * Fade volume during transitions between songs
     */
    public void fadeVolume()
    {
        double gain;

        if (transitionCounter > 0)
        {
            gain = 0.25 * (transitionCounter + 15) / 40;
            setVolume(gain);
            transitionCounter--;
        }
        else if (currentSong != newSong)
        {
            playSong();
            gain = 0.25;
            setVolume(gain);
        }
    }
}

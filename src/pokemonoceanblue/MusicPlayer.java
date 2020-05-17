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
    boolean intro = false;

    public MusicPlayer()
    {

    }

    /**
     * Queues the background music for the given song number
     * @param song the number of the song to be played
     */
    public void setSong(int musicId, boolean skipTransition)
    {
        if (currentSong == -1 || (skipTransition && musicId != currentSong))
        {
            newSong = musicId;
            transitionCounter = 0;
            playSong();
            setVolume(0.25);
        }
        // only switch songs if the new song is different from the one currently being played
        else if (musicId != currentSong)
        {
            newSong = musicId;
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
            try
            {
                // if the song has an intro, play that first
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.getClass().getResource(String.format("/music/%sintro.wav", currentSong)));
                currentClip = AudioSystem.getClip();
                this.intro = true;
                currentClip.open(audioStream);
                currentClip.start();
            }
            catch (Exception e)
            {
                // otherwise just start with the looping part
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.getClass().getResource(String.format("/music/%s.wav", currentSong)));
                currentClip = AudioSystem.getClip();
                currentClip.open(audioStream);
                fadeVolume();
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            }   
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
        System.out.println(gain);
    }

    /**
     * Fade volume during transitions between songs
     */
    public void fadeVolume()
    {
        double gain;

        if (this.intro && currentClip.getMicrosecondLength() - currentClip.getMicrosecondPosition() < 50000)
        {
            try
            {
                // switch from the intro to the looping part
                AudioInputStream newAudioStream = AudioSystem.getAudioInputStream(this.getClass().getResource(String.format("/music/%s.wav", currentSong)));
                currentClip = AudioSystem.getClip();
                currentClip.open(newAudioStream);
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
                setVolume(0.25);
                this.intro = false;
            }
            catch (Exception ex)
            {
                System.out.println("Error playing music");
            }
            
        }

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

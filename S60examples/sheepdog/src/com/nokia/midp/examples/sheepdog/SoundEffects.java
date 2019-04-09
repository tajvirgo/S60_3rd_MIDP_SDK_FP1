/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.media.*;
import java.io.*;


/**
 * SoundEffects: Implements sound effects for a simple game.
 */
class SoundEffects
{
    private static SoundEffects instance;
    private Player sheepSoundPlayer;
    private Player dogSoundPlayer;


    private SoundEffects()
    {
        sheepSoundPlayer = createPlayer("/com/nokia/midp/examples/sheepdog/res/sheep.wav", "audio/x-wav");
        dogSoundPlayer = createPlayer("/com/nokia/midp/examples/sheepdog/res/dog.wav", "audio/x-wav");
    }


    static SoundEffects getInstance()
    {
        if (instance == null)
        {
            instance = new SoundEffects();
        }
        return instance;
    }


    void startSheepSound()
    {
        startPlayer(sheepSoundPlayer);
    }


    void startDogSound()
    {
        startPlayer(dogSoundPlayer);
    }


    void startGameOverSound()
    {
        startPlayer(createPlayer("/com/nokia/midp/examples/sheepdog/res/gameover.mid", "audio/midi"));
    }


    void startHighScoreSound()
    {
        startPlayer(createPlayer("/com/nokia/midp/examples/sheepdog/res/highscore.mid", "audio/midi"));
    }


    private void startPlayer(Player p)
    {
        if (p != null)
        {
            try
            {
                p.stop();
                p.setMediaTime(0L);
                p.start();
            }
            catch (MediaException me)
            {
                // ignore
            }
        }
    }


    private Player createPlayer(String filename, String format)
    {
        Player p = null;
        try
        {
            InputStream is = getClass().getResourceAsStream(filename);
            p = Manager.createPlayer(is, format);
            p.prefetch();
        }
        catch (IOException ioe)
        {
            // ignore
        }
        catch (MediaException me)
        {
            // ignore
        }
        return p;
    }
}

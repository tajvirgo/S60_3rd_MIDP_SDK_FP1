/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.*;


/**
 * GameOverScreen: Implements the End-Of-Game screen for a simple MIDlet game.
 */
class GameOverScreen
    extends Canvas
{
    private final SheepdogMIDlet midlet;
    private boolean wasBestTime;
    private long time;
    private long bestTime;


    GameOverScreen(SheepdogMIDlet midlet, long time)
    {
        super();
        this.midlet = midlet;
        this.time = time;
        setFullScreenMode(true);

        if (midlet.checkBestTime(time))
        {
            wasBestTime = true;
            bestTime = time;
            SoundEffects.getInstance().startHighScoreSound();
        }
        else
        {
            wasBestTime = false;
            bestTime = midlet.getBestTime();
            SoundEffects.getInstance().startGameOverSound();
        }
        midlet.flashBacklight(1000);    // 1 second
    }


    public void paint(Graphics g)
    {
        int width = getWidth();
        int height = getHeight();

        // clear screen to green
        g.setColor(0x0000FF00);
        g.fillRect(0, 0, width, height);

        // Write message. We use a trick to make outlined text: we draw it
        // offset one pixel to the top, bottom, left & right in white, then
        // centred in black.
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL,
                               Font.STYLE_BOLD,
                               Font.SIZE_LARGE));
        int centerX = width / 2;
        int centerY = height / 2;
        g.setColor(0x00FFFFFF);   // white
        drawText(g, centerX, centerY - 1);
        drawText(g, centerX, centerY + 1);
        drawText(g, centerX - 1, centerY);
        drawText(g, centerX + 1, centerY);
        g.setColor(0x00000000);   // black
        drawText(g, centerX, centerY);
    }


    private void drawText(Graphics g, int centerX, int centerY)
    {
        int fontHeight = g.getFont().getHeight();
        int textHeight = 3 * fontHeight;
        int topY = centerY - textHeight / 2;

        g.drawString("GAME OVER",
                     centerX,
                     topY,
                     Graphics.HCENTER | Graphics.TOP);
        g.drawString("Time: " + time + "s",
                     centerX,
                     topY + fontHeight,
                     Graphics.HCENTER | Graphics.TOP);
        g.drawString(wasBestTime ? "New best time!" :
                                   ("Best time: " + bestTime + "s"),
                     centerX,
                     topY + 2 * fontHeight,
                     Graphics.HCENTER | Graphics.TOP);
    }


    public void keyPressed(int keyCode)
    {
        midlet.gameOverDone();
    }
}

/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;


/**
 * HawkCanvas: Extends the MIDP 2.0 Game Canvas to create the main screen for a simple game.
 */
class SheepdogCanvas
    extends GameCanvas
    implements Runnable
{
    // shared direction constants
    static final int NONE = -1;
    static final int UP = 0;
    static final int LEFT = 1;
    static final int DOWN = 2;
    static final int RIGHT = 3;

    private static final int MILLIS_PER_TICK = 50;
    private static final int NUM_SHEEP = 5;

    private final SheepdogMIDlet midlet;
    private final Field field;
    private final Sheepdog sheepdog;
    private final Vector sheep = new Vector();
    private final LayerManager layerManager;

    private final Graphics graphics;
    private long gameDuration;
    private long startTime;
    private volatile Thread animationThread = null;


    SheepdogCanvas(SheepdogMIDlet midlet)
    {
        super(true);   // suppress key events for game keys
        this.midlet = midlet;
        setFullScreenMode(true);
        graphics = getGraphics();

        layerManager = new LayerManager();
        field = new Field();
        sheepdog = new Sheepdog(this);
        layerManager.append(sheepdog);
        for (int i = 0; i < NUM_SHEEP; ++i)
        {
            Sheep newSheep = new Sheep(this);
            layerManager.append(newSheep);
            sheep.addElement(newSheep);
        }

        layerManager.append(field);   // last layer, behind sprites

        init();
    }


    public void keyPressed(int keyCode)
    {
        // The constructor suppresses key events for game keys, so we'll
        // only get key events for non-game keys. The number keys, * & #
        // have positive keyCodes, so negative keyCodes mean non-game
        // special keys like soft-keys. We'll use key-presses on special
        // keys to take us to the menu.
        if (keyCode < 0)
        {
            stop();
            midlet.sheepdogCanvasMenu();
        }
    }


    void init()
    {
        sheepdog.setPosition(field.getSheepdogStartX(),
                             field.getSheepdogStartY());

        for (int i = 0; i < sheep.size(); ++i)
        {
            Sheep sh = (Sheep)(sheep.elementAt(i));

            // find a valid position for the sheep
            do
            {
                int x = SheepdogMIDlet.random(field.getWidth() - Sheep.WIDTH);
                int y = SheepdogMIDlet.random(field.getHeight() - Sheep.HEIGHT);
                sh.setPosition(x, y);
            } while (field.containsImpassableArea(sh.getX(),
                                                  sh.getY(),
                                                  sh.getWidth(),
                                                  sh.getHeight()) ||
                     overlapsSheepdog(sh) ||
                     overlapsSheep(sh, i) ||
                     field.inFold(sh));
        }
    }


    public synchronized void start()
    {
        animationThread = new Thread(this);
        animationThread.start();

        startTime = System.currentTimeMillis() - gameDuration;
    }


    public synchronized void stop()
    {
        gameDuration = System.currentTimeMillis() - startTime;
        animationThread = null;
    }


    public void run()
    {
        Thread currentThread = Thread.currentThread();

        try
        {
            // This ends when animationThread is set to null, or when
            // it is subsequently set to a new thread; either way, the
            // current thread should terminate
            while (currentThread == animationThread)
            {
                long startTime = System.currentTimeMillis();
                // Don't advance game or draw if canvas is covered by
                // a system screen.
                if (isShown())
                {
                    tick();
                    draw();
                    flushGraphics();
                }
                long timeTaken = System.currentTimeMillis() - startTime;
                if (timeTaken < MILLIS_PER_TICK)
                {
                    synchronized (this)
                    {
                        wait(MILLIS_PER_TICK - timeTaken);
                    }
                }
                else
                {
                    Thread.yield();
                }
            }
        }
        catch (InterruptedException e)
        {
            // won't be thrown
        }
    }


    private void tick()
    {
        // If player presses two or more direction buttons, we ignore them
        // all. But pressing fire is independent. The code below also ignores
        // direction buttons if GAME_A..GAME_D are pressed.
        int keyStates = getKeyStates();
        boolean bark = (keyStates & FIRE_PRESSED) != 0;
        keyStates &= ~FIRE_PRESSED;
        int direction = (keyStates == UP_PRESSED) ? UP :
                        (keyStates == LEFT_PRESSED) ? LEFT:
                        (keyStates == DOWN_PRESSED) ? DOWN :
                        (keyStates == RIGHT_PRESSED) ? RIGHT : NONE;
        sheepdog.tick(direction, bark);

        for (int i = 0; i < sheep.size(); ++i)
        {
            Sheep sh = (Sheep)(sheep.elementAt(i));
            sh.tick();
        }

        field.tick();
    }


    Field getField()
    {
        return field;
    }


    Sheepdog getSheepdog()
    {
        return sheepdog;
    }


    Vector getSheep()
    {
        return sheep;
    }


    void handleDogBark()
    {
        for (int i = 0; i < sheep.size(); ++i)
        {
            Sheep sh = (Sheep)(sheep.elementAt(i));
            sh.handleDogBark();
        }
    }


    boolean overlapsSheepdog(Sprite sprite)
    {
        return sprite.collidesWith(sheepdog, false); // false -> not pixelLevel
    }


    boolean overlapsSheep(Sprite sprite)
    {
        return overlapsSheep(sprite, sheep.size());
    }


    // whether the sprite overlaps the first 'count' sheep
    boolean overlapsSheep(Sprite sprite, int count)
    {
        for (int i = 0; i < count; ++i)
        {
            Sheep sh = (Sheep)(sheep.elementAt(i));
            if (sprite.collidesWith(sh, false))  // false -> not pixelLevel
            {
                return true;
            }
        }
        return false;
    }


    boolean overlapsOtherSheep(Sprite sprite)
    {
        for (int i = 0; i < sheep.size(); ++i)
        {
            Object obj = sheep.elementAt(i);
            if (obj != sprite)
            {
                Sheep sh = (Sheep)obj;
                if (sprite.collidesWith(sh, false))  // false -> not pixelLevel
                {
                    return true;
                }
            }
        }
        return false;
    }


    void vibrate(int millis)
    {
        midlet.vibrate(millis);
    }


    // draw game
    private void draw()
    {
        int width = getWidth();
        int height = getHeight();

        // clear screen to grey
        graphics.setColor(0x00888888);
        graphics.fillRect(0, 0, width, height);

        // clip and translate to centre
        int dx = origin(sheepdog.getX() + sheepdog.getWidth() / 2,
                        field.getWidth(),
                        width);
        int dy = origin(sheepdog.getY() + sheepdog.getHeight() / 2,
                        field.getHeight(),
                        height);
        graphics.setClip(dx, dy, field.getWidth(), field.getHeight());
        graphics.translate(dx, dy);

        // draw background and sprites
        layerManager.paint(graphics, 0, 0);

        // undo clip & translate
        graphics.translate(-dx, -dy);
        graphics.setClip(0, 0, width, height);

        // display time & score
        long time = (System.currentTimeMillis() - startTime) / 1000;
        int score = numSheepInFold();
        graphics.setColor(0x00FFFFFF); // white
        graphics.drawString(Integer.toString(score),
                            1,
                            1,
                            Graphics.TOP | Graphics.LEFT);
        graphics.drawString(Long.toString(time),
                            width - 2,
                            1,
                            Graphics.TOP | Graphics.RIGHT);

        if (score == sheep.size())
        {
            midlet.sheepdogCanvasGameOver(time);
        }
    }


    // If the screen is bigger than the field, we center the field
    // in the screen. Otherwise we center the screen on the focus, except
    // that we don't scroll beyond the edges of the field.
    private int origin(int focus, int fieldLength, int screenLength)
    {
        int origin;
        if (screenLength >= fieldLength)
        {
            origin = (screenLength - fieldLength) / 2;
        }
        else if (focus <= screenLength / 2)
        {
            origin = 0;
        }
        else if (focus >= (fieldLength - screenLength / 2))
        {
            origin = screenLength - fieldLength;
        }
        else
        {
            origin = screenLength / 2 - focus;
        }
        return origin;
    }


    int numSheepInFold()
    {
        int count = 0;
        for (int i = 0; i < sheep.size(); ++i)
        {
            Sheep sh = (Sheep)(sheep.elementAt(i));
            if (field.inFold(sh))
            {
                count++;
            }
        }
        return count;
    }
}

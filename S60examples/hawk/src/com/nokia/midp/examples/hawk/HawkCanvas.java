/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.hawk;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.media.*;
import java.io.*;
import java.util.*;


/**
 * HawkCanvas: Extends the MIDP 2.0 Game Canvas to create the main screen for a simple game prototype.
 */
class HawkCanvas extends GameCanvas implements Runnable
{
    private static final int MILLIS_PER_TICK = 25;
    private final int WIDTH = getWidth();
    private final int HEIGHT = getHeight();
    private static final int TILE_WIDTH = 16;
    private static final int TILE_HEIGHT = 16;
    private final int WIDTH_IN_TILES = WIDTH / TILE_WIDTH;
    private final int HEIGHT_IN_TILES = HEIGHT / TILE_HEIGHT;
    private static final int NUM_TILES = 16;

    private final HawkMIDlet midlet;
    private final TiledLayer background;
    private final Sprite hawk;
    private final Vector missiles = new Vector();
    private final LayerManager layerManager;
    private Image hawkImage;
    private Image missileImage;
    private final Random random = new Random();
    private final Player shotSoundPlayer;

    private int backgroundScroll;
    private boolean firing = false;

    private volatile Thread animationThread = null;


    HawkCanvas(HawkMIDlet midlet) {
        super(true);   // suppress key events for game keys

        this.midlet = midlet;

        Image backgroundTiles = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/terrain.png");
        background = new TiledLayer(WIDTH_IN_TILES,
                                    HEIGHT_IN_TILES + 1,
                                    backgroundTiles,
                                    TILE_WIDTH,
                                    TILE_HEIGHT);
        for (int row = 0; row < HEIGHT_IN_TILES + 1; ++row) {
            fillTileRow(row);
        }
        backgroundScroll = 1 - TILE_HEIGHT;
        background.setPosition(0, backgroundScroll);

        hawk = new Sprite(getHawkImage(), getHawkImageWidth(), getHawkImageHeight());
        hawk.setPosition(WIDTH / 2 - getHawkImageWidth() / 2, HEIGHT - 1 - getHawkImageHeight());

        layerManager = new LayerManager();
        layerManager.append(hawk);
        layerManager.append(background);

        shotSoundPlayer = createSoundPlayer("/com/nokia/midp/examples/hawk/res/shot.wav", "audio/x-wav");
    }


    public void keyPressed(int keyCode) {
        // The constructor suppresses key events for game keys, so we'll
        // only get key events for non-game keys. The number keys, * & #
        // have positive keyCodes, so negative keyCodes mean non-game
        // special keys like soft-keys. We'll use key-presses on special
        // keys to take us to the menu.
        if (keyCode < 0) {
            stop();
            midlet.hawkCanvasMenu();
        }
    }


    void init() {
        // reinit level
        for (int i = 0; i < missiles.size(); ++i) {
            Sprite missile = (Sprite)(missiles.elementAt(i));
            layerManager.remove(missile);
        }
        missiles.removeAllElements();

        hawk.setPosition(WIDTH / 2 - getHawkImageWidth() / 2, HEIGHT - 1 - getHawkImageHeight());
    }


    private Image getHawkImage() {
        if (this.hawkImage == null) {
            if ((WIDTH >= 200) && (HEIGHT >= 150)) {
                this.hawkImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/hawk2.png");;
            } else {    // Default to smaller hawk image
                this.hawkImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/hawk.png");
            }
        }
        return this.hawkImage;
    }


    private int getHawkImageWidth() {
        return getHawkImage().getWidth() / 3;
    }


    private int getHawkImageHeight() {
        return getHawkImage().getHeight();
    }


    private Image getMissileImage() {
        if (this.missileImage == null) {
            if ((WIDTH >= 200) && (HEIGHT >= 150)) {
                this.missileImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/missile2.png");
            } else {    // Default to smaller missile image
                this.missileImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/missile.png");
            }
        }
        return this.missileImage;
    }


    private int getMissileImageWidth() {
        return getMissileImage().getWidth();
    }


    private int getMissileImageHeight() {
        return getMissileImage().getHeight();
    }


    private Player createSoundPlayer(String filename, String format) {
        Player p = null;
        try {
            InputStream is = getClass().getResourceAsStream(filename);
            p = Manager.createPlayer(is, format);
            p.prefetch();
        }
        catch (IOException ex) {
            // ignore
        }
        catch (MediaException ex) {
            // ignore
        }
        return p;
    }


    private void startShotSound() {
        if (shotSoundPlayer != null) {
            try {
                shotSoundPlayer.stop();
                shotSoundPlayer.setMediaTime(0L);
                shotSoundPlayer.start();
            }
            catch (MediaException ex) {
                // ignore
            }
        }
    }


    private void fillTileRow(int row) {
        for (int column = 0; column < WIDTH_IN_TILES; ++column) {
            background.setCell(column, row, random(NUM_TILES) + 1);
        }
    }


    private int random(int size) {
        return (random.nextInt() & 0x7FFFFFFF) % size;
    }


    private void scrollTileRows() {
        for (int row = HEIGHT_IN_TILES; row >= 1; row--) {
            for (int column = 0; column < WIDTH_IN_TILES; column++) {
                int cell = background.getCell(column, row - 1);
                background.setCell(column, row, cell);
            }
        }
        fillTileRow(0);
    }


    public synchronized void start() {
        animationThread = new Thread(this);
        animationThread.start();
    }


    public synchronized void stop() {
        animationThread = null;
    }


    public void run() {
        Thread currentThread = Thread.currentThread();

        try {
            // This ends when animationThread is set to null, or when
            // it is subsequently set to a new thread; either way, the
            // current thread should terminate
            while (currentThread == animationThread) {
                long startTime = System.currentTimeMillis();
                // Don't advance game or draw if canvas is covered by
                // a system screen.
                if (isShown()) {
                    tick();
                    draw();
                    flushGraphics();
                }
                long timeTaken = System.currentTimeMillis() - startTime;
                if (timeTaken < MILLIS_PER_TICK) {
                    synchronized (this) {
                        wait(MILLIS_PER_TICK - timeTaken);
                    }
                } else {
                    Thread.yield();
                }
            }
        }
        catch (InterruptedException ex) {
            // won't be thrown
        }
    }


    void tick() {
        scrollBackground();

        int keyStates = getKeyStates();

        if (((keyStates & LEFT_PRESSED) != 0)
         && ((keyStates & RIGHT_PRESSED) == 0)) {
            hawk.setFrame(1);
            if (hawk.getX() > 0) {
                hawk.move(-1, 0);
            }
            if (hawk.getX() > 0) {
                hawk.move(-1, 0);
            }
        } else if (((keyStates & RIGHT_PRESSED) != 0)
                && ((keyStates & LEFT_PRESSED) == 0)) {
            hawk.setFrame(2);
            if (hawk.getX() < WIDTH - getHawkImageWidth() - 1) {
                hawk.move(1, 0);
            }
            if (hawk.getX() < WIDTH - getHawkImageWidth() - 1) {
                hawk.move(1, 0);
            }
        } else {
            hawk.setFrame(0);
        }

        if (((keyStates & UP_PRESSED) != 0)
         && ((keyStates & DOWN_PRESSED) == 0)) {
            if (hawk.getY() > 0) {
                hawk.move(0, -1);
            }
            if (hawk.getY() > 0) {
                hawk.move(0, -1);
            }
        } else if (((keyStates & DOWN_PRESSED) != 0)
               && ((keyStates & UP_PRESSED) == 0)) {
            if (hawk.getY() < HEIGHT - getHawkImageHeight() - 1) {
                hawk.move(0, 1);
            }
            if (hawk.getY() < HEIGHT - getHawkImageHeight() - 1) {
                hawk.move(0, 1);
            }
        }

        // implement a toggle, so fire only happens once per click
        // (will therefore not register very rapid multiple-clicks)
        if ((keyStates & FIRE_PRESSED) != 0) {
            if (!firing) {
                startShotSound();
                firing = true;
                Sprite missile = new Sprite(getMissileImage());
                missile.setPosition(hawk.getX() - getMissileImageWidth()/2 + getHawkImageWidth()/2,
                                    hawk.getY());
                missiles.addElement(missile);
                layerManager.insert(missile, 1);
            }
        } else {
            firing = false;
        }

        // handle missiles
        for (int i = 0; i < missiles.size(); ++i) {
            // missiles move faster than plane
            for (int j = 0; j < 5; ++j) {
                Sprite missile = (Sprite)(missiles.elementAt(i));
                missile.move(0, -1);
                if (missile.getY() < 0) {
                    missiles.removeElementAt(i);
                    layerManager.remove(missile);
                    i--;
                    break;
                }
            }
        }
    }


    private void scrollBackground() {
        backgroundScroll += 1;
        if (backgroundScroll > 0) {
            backgroundScroll = 2 - TILE_HEIGHT;
            scrollTileRows();
        }
        background.setPosition(0, backgroundScroll);
    }


    // draw game
    private void draw() {
        int width = getWidth();
        int height = getHeight();
        Graphics g = getGraphics();

        // clear screen to grey
        g.setColor(0x00888888);
        g.fillRect(0, 0, width, height);

        // clip and translate to centre
        int dx = (width - WIDTH) / 2;
        int dy = (height - HEIGHT) / 2;
        g.setClip(dx, dy, WIDTH, HEIGHT);
        g.translate(dx, dy);

        // draw background and sprites
        layerManager.paint(g, 0, 0);
    }
}

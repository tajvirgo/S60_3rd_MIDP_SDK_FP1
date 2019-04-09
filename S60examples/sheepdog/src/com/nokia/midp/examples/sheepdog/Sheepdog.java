/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.game.*;


/**
 * Sheepdog: Implements the sprite for the sheepdog piece in a simple MIDlet game.
 */
class Sheepdog
    extends Sprite
{
    static final int WIDTH = 15;
    static final int HEIGHT = 15;
    static final int VIBRATION_MILLIS = 200;

    private final SheepdogCanvas canvas;
    private boolean barking = false;
    private int[][][] animations = {{{0},                // stand up
                                     {1, 2, 3, 4}},      // run up
                                    {{5},                // stand left
                                     {6, 7, 8, 9}},      // run left
                                    {{10},               // stand down
                                     {11, 12, 13, 14}}}; // run down
    private int animationTick = 0;
    private static final int STAND = 0;
    private static final int RUN = 1;
    private int currentDirection = SheepdogCanvas.LEFT;

    Sheepdog(SheepdogCanvas canvas)
    {
        super(SheepdogMIDlet.createImage("/com/nokia/midp/examples/sheepdog/res/dog.png"), WIDTH, HEIGHT);
        defineCollisionRectangle(2, 2, WIDTH-4, WIDTH-4);
        defineReferencePixel(WIDTH/2, HEIGHT/2);

        this.canvas = canvas;
    }


    void tick(int direction, boolean bark)
    {
        animationTick++;

        Field field = canvas.getField();
        boolean moving = false;;
        switch (direction)
        {
        case SheepdogCanvas.UP:
            currentDirection = direction;
            if ((getY() > 0) &&
                !field.containsImpassableArea(getX(),
                                              getY() - 1,
                                              getWidth(),
                                              1) &&
                moveSuccessfully(0, -1))
            {
                moving = true;
            }
            else
            {
                canvas.vibrate(VIBRATION_MILLIS);
            }
            break;
        case SheepdogCanvas.LEFT:
            currentDirection = direction;
            if ((getX() > 0) &&
                !field.containsImpassableArea(getX() - 1,
                                              getY(),
                                              1,
                                              getHeight()) &&
                moveSuccessfully(-1, 0))
            {
                moving = true;
            }
            else
            {
                canvas.vibrate(VIBRATION_MILLIS);
            }
            break;
        case SheepdogCanvas.DOWN:
            currentDirection = direction;
            if ((getY() + getHeight() < field.getWidth()) &&
                !field.containsImpassableArea(getX(),
                                              getY() + getHeight(),
                                              getWidth(),
                                              1) &&
                moveSuccessfully(0, 1))
            {
                moving = true;
            }
            else
            {
                canvas.vibrate(VIBRATION_MILLIS);
            }
            break;
        case SheepdogCanvas.RIGHT:
            currentDirection = direction;
            if ((getX() + getWidth() < field.getWidth()) &&
                !field.containsImpassableArea(getX() + getWidth(),
                                              getY(),
                                              1,
                                              getHeight()) &&
                moveSuccessfully(1, 0))
            {
                moving = true;
            }
            else
            {
                canvas.vibrate(VIBRATION_MILLIS);
            }
            break;
        default:  // must be NONE
            break;
        }

        if (moving)
        {
            advanceRunningAnimation();
        }
        else
        {
            setStandingAnimation();
        }

        // implement a toggle, so bark only happens once per click
        // (will therefore not register very rapid multiple-clicks)
        if (bark)
        {
            if (!barking)
            {
                SoundEffects.getInstance().startDogSound();
                barking = true;
                canvas.handleDogBark();
            }
        }
        else
        {
            barking = false;
        }
    }


    private boolean moveSuccessfully(int dx, int dy)
    {
        move(dx, dy);
        if (canvas.overlapsSheep(this))
        {
            move(-dx, -dy);
            return false;
        }
        else
        {
            return true;
        }
    }


    private void advanceRunningAnimation()
    {
        int[] sequence;
        if (currentDirection == SheepdogCanvas.RIGHT)
        {
            sequence = animations[SheepdogCanvas.LEFT][RUN];
            setTransform(TRANS_MIRROR);
        }
        else
        {
            sequence = animations[currentDirection][RUN];
            setTransform(TRANS_NONE);
        }
        setFrame(sequence[(animationTick >> 1) % sequence.length]);
    }


    private void setStandingAnimation()
    {
        if (currentDirection == SheepdogCanvas.RIGHT)
        {
            setFrame(animations[SheepdogCanvas.LEFT][STAND][0]);
            setTransform(TRANS_MIRROR);
        }
        else
        {
            setFrame(animations[currentDirection][STAND][0]);
            setTransform(TRANS_NONE);
        }
    }
}

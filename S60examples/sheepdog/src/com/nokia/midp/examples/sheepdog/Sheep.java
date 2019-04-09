/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.game.*;
import java.util.*;


/**
 * Sheep: Implements the sprite for the sheep pieces in a simple MIDlet game.
 */
class Sheep
    extends Sprite
{
    static final int WIDTH = 15;
    static final int HEIGHT = 15;

    private final SheepdogCanvas canvas;
    private int[][][] animations = {{{0},                // stand up
                                     {1, 2, 3, 4}},      // run up
                                    {{5},                // stand left
                                     {6, 7, 8, 9}},      // run left
                                    {{10},               // stand down
                                     {11, 12, 13, 14}}}; // run down
    private int animationTick;
    private static int numSheep = 0;
    private static final int STAND = 0;
    private static final int RUN = 1;
    private int currentDirection = SheepdogCanvas.DOWN;

    private final int flockFactor;
    private final int minDogFactor;
    private final int maxDogFactor;
    private int dogFactor;

    Sheep(SheepdogCanvas canvas)
    {
        super(SheepdogMIDlet.createImage("/com/nokia/midp/examples/sheepdog/res/sheep.png"), WIDTH, HEIGHT);
        defineCollisionRectangle(2, 2, WIDTH-4, WIDTH-4);
        defineReferencePixel(WIDTH/2, HEIGHT/2);

        this.canvas = canvas;
        animationTick = numSheep++;

        flockFactor = 100 + SheepdogMIDlet.random(100);
        minDogFactor = SheepdogMIDlet.random(20);
        maxDogFactor = minDogFactor + 10;
        dogFactor = minDogFactor;
    }


    void tick()
    {
        // sheep are 4x as slow as dogs
        if ((animationTick++ % 4) != 0)
        {
            return;
        }

        // adjust dog factor
        adjustDogFactor();

        // ARTIFICIAL INTELLIGENCE SECTION
        // - wants to move away from dog, if dog is close
        // - wants to move closer to flock (average position of other
        //   sheep) if they are close
        // - if preferred direction is diagonal and major direction is
        //   blocked, take minor direction
        // - each sheep varies in how much it's scared of the dog, and
        //   how much it wants to flock
        // We do this by calculating a weighted direction vector

        // First calculate dog effect
        Sheepdog sheepdog = canvas.getSheepdog();
        int dx = getX() - sheepdog.getX();
        int dy = getY() - sheepdog.getY();
        int sumsq = dx * dx + dy * dy;
        Field field = canvas.getField();
        int dogEffectX =
            dogFactor * dx * field.getWidth() * field.getWidth() / sumsq;
        int dogEffectY =
            dogFactor * dy * field.getHeight() * field.getHeight() / sumsq;

        // Next calculate flock effect
        int flockDx = 0;
        int flockDy = 0;
        Vector sheep = canvas.getSheep();
        for (int i = 0; i < sheep.size(); ++i)
        {
            Sheep sh = (Sheep)(sheep.elementAt(i));
            if (sh != this)
            {
                flockDx += getX() - sh.getX();
                flockDy += getY() - sh.getY();
            }
        }
        int flockEffectX = (flockDx * flockFactor) / (sheep.size() - 1);
        int flockEffectY = (flockDy * flockFactor) / (sheep.size() - 1);

        // Now calculate total effect
        int totalEffectX = dogEffectX - flockEffectX;
        int totalEffectY = dogEffectY - flockEffectY;

        // Determine preferred directions
        int firstDirection;
        int secondDirection;
        int thirdDirection;
        if (Math.abs(totalEffectY) > Math.abs(totalEffectX))
        {
            // Prefer to move vertically
            if (totalEffectY > 0)
            {
                firstDirection = SheepdogCanvas.DOWN;
            }
            else
            {
                firstDirection = SheepdogCanvas.UP;
            }
            if (totalEffectX > 0)
            {
                secondDirection = SheepdogCanvas.RIGHT;
                thirdDirection = SheepdogCanvas.NONE;
            }
            else if (totalEffectX < 0)
            {
                secondDirection = SheepdogCanvas.LEFT;
                thirdDirection = SheepdogCanvas.NONE;
            }
            else  // totalEffectX == 0
            {
                if (SheepdogMIDlet.random(2) == 0)
                {
                    secondDirection = SheepdogCanvas.LEFT;
                    thirdDirection = SheepdogCanvas.RIGHT;
                }
                else
                {
                    secondDirection = SheepdogCanvas.RIGHT;
                    thirdDirection = SheepdogCanvas.LEFT;
                }
            }
        }
        else
        {
            // Prefer to move horizontally
            if (totalEffectX > 0)
            {
                firstDirection = SheepdogCanvas.RIGHT;
            }
            else
            {
                firstDirection = SheepdogCanvas.LEFT;
            }
            if (totalEffectY > 0)
            {
                secondDirection = SheepdogCanvas.DOWN;
                thirdDirection = SheepdogCanvas.NONE;
            }
            else if (totalEffectY < 0)
            {
                secondDirection = SheepdogCanvas.UP;
                thirdDirection = SheepdogCanvas.NONE;
            }
            else  // totalEffectY == 0
            {
                if (SheepdogMIDlet.random(2) == 0)
                {
                    secondDirection = SheepdogCanvas.UP;
                    thirdDirection = SheepdogCanvas.DOWN;
                }
                else
                {
                    secondDirection = SheepdogCanvas.DOWN;
                    thirdDirection = SheepdogCanvas.UP;
                }
            }
        }

        // if we can move in the preferred directions, do so, else stand
        // facing the dog
        if (tryMove(firstDirection) ||
            tryMove(secondDirection) ||
            ((thirdDirection != SheepdogCanvas.NONE) &&
             tryMove(thirdDirection)))
        {
            advanceRunningAnimation();
        }
        else
        {
            if (Math.abs(dx) > Math.abs(dy))
            {
                if (dx > 0)
                {
                    currentDirection = SheepdogCanvas.LEFT;
                }
                else
                {
                    currentDirection = SheepdogCanvas.RIGHT;
                }
            }
            else
            {
                if (dy > 0)
                {
                    currentDirection = SheepdogCanvas.UP;
                }
                else
                {
                    currentDirection = SheepdogCanvas.DOWN;
                }
            }
            setStandingAnimation();
        }

        // Will baa occasionally if dog is close. Dog distance ranges from
        // about 11 minimum to double width of field
        int dogDistance = Math.abs(dx) + Math.abs(dy);
        if (SheepdogMIDlet.random(dogDistance - 10) == 0)
        {
            SoundEffects.getInstance().startSheepSound();
        }
    }


    private void adjustDogFactor()
    {
        dogFactor += SheepdogMIDlet.random(4) - 2;  // -2..1
        if (dogFactor < minDogFactor)
        {
            dogFactor = minDogFactor;
        }
        else if (dogFactor > maxDogFactor)
        {
            dogFactor = maxDogFactor;
        }
    }


    private boolean tryMove(int direction)
    {
        Field field = canvas.getField();
        boolean blocked = true;
        int dx = 0;
        int dy = 0;
        switch (direction)
        {
        case SheepdogCanvas.UP:
            if ((getY() > 0) &&
                !field.containsImpassableArea(getX(),
                                              getY() - 1,
                                              getWidth(),
                                              1))
            {
                blocked = false;
                dy = -1;
            }
            break;
        case SheepdogCanvas.LEFT:
            if ((getX() > 0) &&
                !field.containsImpassableArea(getX() - 1,
                                              getY(),
                                              1,
                                              getHeight()))
            {
                blocked = false;
                dx = -1;
            }
            break;
        case SheepdogCanvas.DOWN:
            if ((getY() + getHeight() - 1 < field.getWidth()) &&
                !field.containsImpassableArea(getX(),
                                              getY() + getHeight(),
                                              getWidth(),
                                              1))
            {
                blocked = false;
                dy = 1;
            }
            break;
        case SheepdogCanvas.RIGHT:
            if ((getX() + getWidth() - 1 < field.getWidth()) &&
                !field.containsImpassableArea(getX() + getWidth(),
                                              getY(),
                                              1,
                                              getHeight()))
            {
                blocked = false;
                dx = 1;
            }
            break;
        default:
            // can't happen
            break;
        }

        boolean success = false;
        if (!blocked)
        {
            boolean wasInFold = field.inFold(this);
            move(dx, dy);
            if (canvas.overlapsOtherSheep(this) ||
                canvas.overlapsSheepdog(this) ||
                (wasInFold && !field.inFold(this)))
            {
                move(-dx, -dy);
            }
            else
            {
                currentDirection = direction;
                success = true;
            }
        }

        return success;
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
        setFrame(sequence[(animationTick >> 2) % sequence.length]);
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


    void handleDogBark()
    {
        // sheep should get nervous
        dogFactor += 5;
        if (dogFactor > maxDogFactor)
        {
            dogFactor = maxDogFactor;
        }
    }
}

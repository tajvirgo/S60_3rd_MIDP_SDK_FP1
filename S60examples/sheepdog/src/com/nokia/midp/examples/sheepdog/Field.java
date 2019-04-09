/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.game.*;


/**
 * Field: Implements the background field for a simple MIDlet game.
 */
class Field
    extends TiledLayer
{
    private static final int WIDTH_IN_TILES = 12;
    private static final int HEIGHT_IN_TILES = 12;
    private static final int TILE_WIDTH = 16;
    private static final int TILE_HEIGHT = 16;

    private static int[][] cellTiles =
        {{-3, -2, -3, -1, -2, -1, -3, -1, -2, -3, -1, -2},
         {-2,  3,  4,  3,  1,  2,  3,  2,  1,  5,  2, -3},
         {-1,  2,  1,  2,  3,  4,  5,  3,  2,  4,  3, -1},
         {-2,  1,  4,  9,  9,  9,  9,  4,  5,  2,  1, -2},
         {-3,  3,  5,  9, 10, 10, 10,  2,  1,  3,  5, -1},
         {-2,  2,  3,  9, 10, 10, 10,  5,  4,  2,  1, -3},
         {-1,  4,  2,  9,  9,  9,  9,  3,  1,  3,  2, -2},
         {-3,  2,  5,  1,  3,  1,  4,  2,  5,  4,  3, -3},
         {-2,  1,  4,  2,  5,  2,  3,  4,  2,  1,  2, -1},
         {-1,  5,  1,  4,  3,  4,  1,  2,  3,  4,  1, -2},
         {-3,  2,  4,  5,  2,  3,  2,  4,  1,  2,  3, -3},
         {-2, -3, -2, -1, -2, -1, -3, -2, -1, -3, -1, -2}};
    private static int FOLD_TILE = 10;
    private static int FENCE_TILE = 9;
    private static int[][] waterFrames = {{6, 7, 8}, {7, 8, 6}, {8, 6, 7}};

    private int tickCount = 0;


    Field()
    {
        super(WIDTH_IN_TILES,
              HEIGHT_IN_TILES,
              SheepdogMIDlet.createImage("/com/nokia/midp/examples/sheepdog/res/field.png"),
              TILE_WIDTH,
              TILE_HEIGHT);

        createAnimatedTile(waterFrames[0][0]);      // tile -1
        createAnimatedTile(waterFrames[1][0]);      // tile -2
        createAnimatedTile(waterFrames[2][0]);      // tile -3

        for (int row = 0; row < HEIGHT_IN_TILES; ++row)
        {
            for (int column = 0; column < WIDTH_IN_TILES; ++column)
            {
                setCell(column, row, cellTiles[row][column]);
            }
        }
    }


    int getSheepdogStartX()
    {
        return getWidth() - 50;
    }


    int getSheepdogStartY()
    {
        return getHeight() - 50;
    }


    void tick()
    {
        int tickState = (tickCount++ >> 3);   // slow down x8
        int tile = tickState % 3;
        setAnimatedTile(-1 - tile, waterFrames[tile][(tickState % 9) / 3]);
    }


    // return true if any part of the rectangle overlaps a water tile
    // or the fence
    boolean containsImpassableArea(int x, int y, int width, int height)
    {
        int rowMin = y / TILE_HEIGHT;
        int rowMax = (y + height - 1) / TILE_HEIGHT;
        int columnMin = x / TILE_WIDTH;
        int columnMax = (x + width - 1) / TILE_WIDTH;

        for (int row = rowMin; row <= rowMax; ++row)
        {
            for (int column = columnMin; column <= columnMax; ++column)
            {
                int cell = getCell(column, row);
                if ((cell < 0) || (cell == FENCE_TILE))
                {
                    return true;
                }
            }
        }

        return false;
    }


    // returns true if every pixel of the sprite is in the fold
    boolean inFold(Sprite s)
    {
        // we can assume that the sprite's reference pixel is unchanged
        int rowMin = s.getY() / TILE_HEIGHT;
        int rowMax = (s.getY() + s.getHeight() - 1) / TILE_HEIGHT;
        int columnMin = s.getX() / TILE_WIDTH;
        int columnMax = (s.getX() + s.getWidth() - 1) / TILE_WIDTH;

        for (int row = rowMin; row <= rowMax; ++row)
        {
            for (int column = columnMin; column <= columnMax; ++column)
            {
                if (getCell(column, row) != FOLD_TILE)
                {
                    return false;
                }
            }
        }

        return true;
    }
}

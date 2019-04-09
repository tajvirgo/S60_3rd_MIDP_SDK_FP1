/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.m3g.maze3d;

import javax.microedition.m3g.*;

/**
 * A Plane with a given texture and transform
 */
class Plane
{
  /**
   * A square in plane xy
   */
  private final static short POINTS[] = new short[]
  {
	(short) 1, (short) 1, (short) 0,
	(short) 1, (short) -1, (short) 0,
	(short) -1, (short) 1, (short) 0,
	(short) -1, (short) -1, (short) 0,
  };

  /**
   * Texture coordinates
   */
  private final static short TEXTCOORDINATES[] = new short[]
  {
	(short) 1, (short) 1,
	(short) 0, (short) 1,
	(short) 1, (short) 0,
	(short) 0, (short) 0,
  };

  /**
   * Triangle strip indices
   */
  private final static int INDICES[] =
  {
	2, 3, 0,
	1, 0, 3,
	0, 3, 2,
	3, 0, 1
  };

  /**
   * Strip lengths
   */
  private final static int[] LENGTHS = new int[] {3, 3, 3, 3};

  /** Vertex array of positions */
  private final static VertexArray POSITIONS_ARRAY;

  /** Vertext array of texture coordinates */
  private final static VertexArray TEXTURE_ARRAY;

  /** Defines how to connect vertices to form a geometric object. */
  private final static IndexBuffer INDEX_BUFFER;

  /** Wall transform for this plane */
  private Transform wallTransform = new Transform();

  /** Used when setting vertex buffer texture coordinates */
  private float textureRepeat;


  static
  {
	// initialize the common arrays
	POSITIONS_ARRAY = new VertexArray(POINTS.length / 3, 3, 2);
	POSITIONS_ARRAY.set(0, POINTS.length / 3, POINTS);
	TEXTURE_ARRAY = new VertexArray(TEXTCOORDINATES.length / 2, 2, 2);
	TEXTURE_ARRAY.set(0, TEXTCOORDINATES.length / 2, TEXTCOORDINATES);
	INDEX_BUFFER = new TriangleStripArray(INDICES, LENGTHS);
  }

  /**
   * Builds a new plane with a given transform
   * and the texture repeated n times
   * @param wallTransform the given transform
   * @param textureRepeat the number of time to repeat the texture
   */
  Plane(Transform wallTransform, float textureRepeat)
  {
	this.wallTransform = wallTransform;
	this.textureRepeat = textureRepeat;
  }

  /**
   * Build the mesh
   * @return the mesh that was built
   */
  Mesh createMesh()
  {
	VertexBuffer vertexBuffer = new VertexBuffer();
	vertexBuffer.setPositions(POSITIONS_ARRAY, 1.0f, null);
	vertexBuffer.setTexCoords(0,
							  TEXTURE_ARRAY,
							  (float) textureRepeat, null);

	Mesh mesh = new Mesh(vertexBuffer, INDEX_BUFFER, null);
	mesh.setTransform(wallTransform);
	return mesh;
  }

}


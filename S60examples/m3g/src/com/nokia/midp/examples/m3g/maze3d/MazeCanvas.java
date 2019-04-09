/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.m3g.maze3d;

import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.m3g.*;

/**
 * The main class used by the 3d maze midlet. It setups the 3D scene and does the movements
 * and rendering
 */
class MazeCanvas
    extends GameCanvas
    implements Runnable {
  /** Height of the walls */
  private final static float WALL_HEIGHT = 10.f;
  /** Overall side of the maze*/
  private final static float MAZE_SIDE_LENGTH = 200f;
  /** Amount to advance at each step */
  private final static float STEP = 3.0f;
  /** Amount to rotate each time */
  private final static float ANGLE_STEP = 8.0f;
  /** The number of milliseconds to wait in the main loop */
  private final static int MILLIS_PER_TICK = 5;

  /** time spent in the run loop */
  private long loopDuration = MILLIS_PER_TICK;

  /**
   * Size of the steps to directions z
   * stored to avoid extra trigonometric calculations
   */
  private float stepz = -STEP;

  /**
   * Size of the steps to directions x
   * stored to avoid extra trigonometric calculations
   */
  private float stepx = 0f;

  /** Player's x location */
  private float locationx = 0f;

  /** Player's z location */
  private float locationz = 0;

  /** Player's rotation */
  private float angley = 0f;

  /** Flags whether we are looking the game from the top */
  private boolean topView = false;

  /** Flags to keep the drawing going */
  private volatile boolean playing = true;

  /** Stores the amount of frames per second being displayed */
  private volatile float fps = 0;

  /** Stores how long has taken to run thorugh the maze */
  private volatile long gameStart = 0;

  /** Stores how long has taken to run thorugh the maze */
  private volatile long duration = 0;

  /** Indicates whether the game has finished */
  private boolean finished;

  /** Parent MIDlet */
  private final MazeMIDlet midlet;

  /** Graphics 3D global instance */
  private Graphics3D g3d;

  /** The virtual world of this application */
  private World world;

  /** One of the world's 2 cameras (normal view) */
  private Camera camera;

  /** One of the world's 2 cameras, (top view) */
  private Camera topCamera;

  /** World's background */
  private Background background = null;

  /** Simple rectangular mesh used to locate the player */
  private Mesh locationSquare = null;

  /** Wall alternate appearance for clear */
  private Appearance wallClearAppearance = new Appearance();

  /** Wall alternate appearance */
  private Appearance wallAppearance = new Appearance();

  /** Points to the mesh last made semi transparent */
  private Mesh transparentMesh = null;

  /** Actual maze object */
  private Maze maze = new Maze(10, MAZE_SIDE_LENGTH, WALL_HEIGHT);

  /** Main thread */
  private Thread mainThread = null;

  /**
   *   Construct the Displayable
   */
  MazeCanvas(MazeMIDlet midlet)
  {
	super(true);

	this.midlet = midlet;
  }

  /**
   * (Re)Starts the game
   */
  void start()
  {
	playing = true;
	gameStart = System.currentTimeMillis() - duration;

	mainThread = new Thread(this);
	mainThread.start();
  }

  /**
   *   Stops/pauses the game
   */
  void stop()
  {
	duration = System.currentTimeMillis() - gameStart;
	playing = false;
  }

  /**
   * Switches from top view to normal view and vice versa
   */
  void switchView()
  {
	topView = !topView;
	setView();
	// informs the MIDLet since this can be switched internally
	midlet.viewSwitched();
  }

  /**
   * Sets the view
   */
  void setView()
  {
	// reset the locaiton and draw on top of the maze
	locationSquare.setTranslation(locationx,
								  2 * WALL_HEIGHT + 3f, locationz);
	// the square is rendered only in top view
	locationSquare.setRenderingEnable(topView);
	// the background is removed in top view
	world.setBackground(topView ? null : background);
	// sets the actual active camera
	if (topView)
	{
	  world.setActiveCamera(topCamera);
	}
	else
	{
	  world.setActiveCamera(camera);
	}
  }

  /**
   * Builds the world
   */
  void init()
  {
        finished = false;
	duration = 0;
        setFullScreenMode(true);
	// get a Graphics3D instance
	g3d = Graphics3D.getInstance();
	// build the world and the cameras
	world = new World();
	camera = new Camera();
	topCamera = new Camera();
	world.addChild(camera);
	world.addChild(topCamera);
	float w = (float)getWidth();
	float h = (float)getHeight();

	// set the perspective
	camera.setPerspective(60.f, w / h, 0.1f, 1000.f);
	topCamera.setPerspective(60.f, w / h, 0.1f, 1000.f);

	// the top camer has a fixed transform looking from the top
	Transform topCameraTransform = new Transform();
	topCameraTransform.postRotate(90, -1f, 0f, 0f);
	topCameraTransform.postTranslate(0f, 0f, 1.2f * MAZE_SIDE_LENGTH);
	topCamera.setTransform(topCameraTransform);

	// Setup the background
	background = new Background();
	Image backgroundImage = MazeMIDlet.makeImage("/com/nokia/midp/examples/m3g/maze3d/content/background.png");
	if (backgroundImage != null)
	{
	  background.setImage(new Image2D(Image2D.RGB, backgroundImage));
	  background.setImageMode(Background.REPEAT, Background.REPEAT);
	}
	world.setBackground(background);

	createFloor();
	createLocationSquare();
	setUpMaze();
	createStartEndMarks();
	setView();

	// setup the initial location
	locationx = maze.findStartLocationX();
	locationz = maze.findStartLocationZ();
	// look at the center
	angley = -180f;


	setupCamera();

	start();
  }

  /**
   * Creates the label at the start and at the end of the maze
   */
  private void createStartEndMarks()
  {
	// The marks' appearance
	Appearance startMarkAppearance = new Appearance();
	Appearance endMarkAppearance = new Appearance();

	// The composite mode is ALPHA to show only the letters
	// the background is hidden using the alpha layer
	CompositingMode markCompositeMode = new CompositingMode();
	markCompositeMode.setBlending(CompositingMode.ALPHA);
	startMarkAppearance.setCompositingMode(markCompositeMode);
	endMarkAppearance.setCompositingMode(markCompositeMode);

	// The label's text is built using a texture
	Texture2D startMarkTexture = null;
	Image startMarkTextureImage =
		MazeMIDlet.makeImage("/com/nokia/midp/examples/m3g/maze3d/content/start_label.png");
	if (startMarkTextureImage != null)
	{
	  startMarkTexture = new Texture2D(
		  new Image2D(Image2D.RGBA, startMarkTextureImage));
	  // the texture is not repeated
	  startMarkTexture.setWrapping(Texture2D.WRAP_CLAMP,
								   Texture2D.WRAP_CLAMP);
	  startMarkTexture.setBlending(Texture2D.FUNC_REPLACE);
	  startMarkTexture.setFiltering(Texture2D.FILTER_NEAREST,
									Texture2D.FILTER_NEAREST);
	  startMarkAppearance.setTexture(0, startMarkTexture);
	}

	Texture2D endMarkTexture = null;
	Image endMarkTextureImage =
		MazeMIDlet.makeImage("/com/nokia/midp/examples/m3g/maze3d/content/end_label.png");
	if (endMarkTextureImage != null)
	{
	  endMarkTexture = new Texture2D(
		  new Image2D(Image2D.RGBA, endMarkTextureImage));
	  // the texture is not repeated
	  endMarkTexture.setWrapping(Texture2D.WRAP_CLAMP,
								 Texture2D.WRAP_CLAMP);
	  endMarkTexture.setBlending(Texture2D.FUNC_REPLACE);
	  endMarkTexture.setFiltering(Texture2D.FILTER_NEAREST,
								  Texture2D.FILTER_NEAREST);
	  endMarkAppearance.setTexture(0, endMarkTexture);
	}
	// create the start mesh
	Plane startMarkPlane = maze.createStartMark();
	Mesh startMarkMesh = startMarkPlane.createMesh();
	startMarkMesh.setAppearance(0, startMarkAppearance);
	// these are not pickable
	startMarkMesh.setPickingEnable(false);
	world.addChild(startMarkMesh);

	// creates the end mesh
	Plane endMarkPlane = maze.createEndMark();
	Mesh endMarkMesh = endMarkPlane.createMesh();
	endMarkMesh.setAppearance(0, endMarkAppearance);
	// is not pickable either
	endMarkMesh.setPickingEnable(false);

	// Create a sequence of 4 keyframes
	KeyframeSequence keyframes =
		new KeyframeSequence(4,3, KeyframeSequence.LINEAR);
	keyframes.setDuration(12000);
	keyframes.setRepeatMode(KeyframeSequence.LOOP);

	float[] trans = new float[4];
	endMarkMesh.getOrientation(trans);
	// create set the keyframes.
	// Basically keep x and z and animate in the y axis
	keyframes.setKeyframe(0, 0,
		new float[]{trans[0], trans[1], trans[2]});
	keyframes.setKeyframe(1, 4000,
		new float[]{trans[0], trans[1] - WALL_HEIGHT / 4, trans[2]});
	keyframes.setKeyframe(2, 8000,
		new float[]{trans[0], trans[1] + WALL_HEIGHT / 4, trans[2]});
	keyframes.setKeyframe(3, 12000,
		new float[]{trans[0], trans[1], trans[2]});
	AnimationTrack animationTrack =
		new AnimationTrack(keyframes, AnimationTrack.TRANSLATION);

	AnimationController anim = new AnimationController();
	animationTrack.setController(anim);

	// add the animation track
	endMarkMesh.addAnimationTrack(animationTrack);

	world.addChild(endMarkMesh);
  }

  /**
   * Creates the floor
   */
  private void createFloor()
  {
	float floorSide = MAZE_SIDE_LENGTH / 2;

	// define the location and size of the floor
	Transform floorTransform = new Transform();
	floorTransform.postRotate(90.0f, -1.0f, 0.0f, 0.0f);
	floorTransform.postScale(floorSide, floorSide, 1.0f);

	// The floor appearance. Basically a texture repeated many times
	Appearance floorAppearance = new Appearance();
	// The floor needs that perspective correction is enabled
	PolygonMode floorPolygonMode = new PolygonMode();
	floorPolygonMode.setPerspectiveCorrectionEnable(true);
	floorAppearance.setPolygonMode(floorPolygonMode);

	// load the texture
	Texture2D floorTexture = null;
	Image floorTextureImage = MazeMIDlet.makeImage("/com/nokia/midp/examples/m3g/maze3d/content/floor.png");
	if (floorTextureImage != null)
	{
	  floorTexture = new Texture2D(
		  new Image2D(Image2D.RGB, floorTextureImage));
	  // the texture is repeated many times
	  floorTexture.setWrapping(Texture2D.WRAP_REPEAT,
							   Texture2D.WRAP_REPEAT);
	  floorTexture.setBlending(Texture2D.FUNC_REPLACE);
	  floorTexture.setFiltering(Texture2D.FILTER_LINEAR,
								Texture2D.FILTER_NEAREST);
	  floorAppearance.setTexture(0, floorTexture);
	}
	Plane floor = new Plane(floorTransform, 10);

	// build the mesh
	Mesh floorMesh = floor.createMesh();
	floorMesh.setAppearance(0, floorAppearance);
	// the floor is not pickable
	floorMesh.setPickingEnable(false);
	// add to the world
	world.addChild(floorMesh);
  }

  /**
   * Create the player location square
   */
  private void createLocationSquare()
  {
	// initial location of the square
	// we are only interested in rotating and scaling it
	Transform locationSquareTransform = new Transform();
	locationSquareTransform.postRotate(90.0f, -1.0f, 0.0f, 0.0f);
	locationSquareTransform.postScale(8, 8, 1.0f);

	// the location apparance is a simple texture decal
	Appearance locationSquareAppearance = new Appearance();
	Texture2D locationSquareTexture = null;
	Image locationSquareImage = MazeMIDlet.makeImage("/com/nokia/midp/examples/m3g/maze3d/content/location.png");
	if (locationSquareImage != null)
	{
	  locationSquareTexture = new Texture2D(
		  new Image2D(Image2D.RGB, locationSquareImage));
	  // the texture is not repeated
	  locationSquareTexture.setWrapping(Texture2D.WRAP_CLAMP,
										Texture2D.WRAP_CLAMP);
	  locationSquareTexture.setBlending(Texture2D.FUNC_REPLACE);
	  locationSquareTexture.setFiltering(Texture2D.FILTER_NEAREST,
										 Texture2D.FILTER_NEAREST);
	  locationSquareAppearance.setTexture(0, locationSquareTexture);
	}
	Plane locationPlane = new Plane(locationSquareTransform, 1);
	locationSquare = locationPlane.createMesh();
	locationSquare.setAppearance(0, locationSquareAppearance);
	locationSquare.setRenderingEnable(false);
	locationSquare.setPickingEnable(false);

	world.addChild(locationSquare);
  }


  /**
   * Sets up the maze
   */
  private void setUpMaze()
  {
	// the walls need perspection correction enabled
	PolygonMode wallPolygonMode = new PolygonMode();
	wallPolygonMode.setPerspectiveCorrectionEnable(true);

	// build the wall semi transparent appearance
	wallClearAppearance.setPolygonMode(wallPolygonMode);
	// this is to make a wall semi transparent
	CompositingMode wallClearCompositeMode = new CompositingMode();
	wallClearCompositeMode.setBlending(CompositingMode.ALPHA_ADD);
	wallClearAppearance.setCompositingMode(wallClearCompositeMode);

	// build the normal wall appearance
	wallAppearance.setPolygonMode(wallPolygonMode);

	// load the wall texture
	Image wallTextureImage = MazeMIDlet.makeImage("/com/nokia/midp/examples/m3g/maze3d/content/wall.png");
	if (wallTextureImage != null)
	{
	  Texture2D wallTexture = null;
	  wallTexture = new Texture2D(
		  new Image2D(Image2D.RGB, wallTextureImage));
	  // the texture is repeated
	  wallTexture.setWrapping(Texture2D.WRAP_REPEAT,
							  Texture2D.WRAP_CLAMP);
	  wallTexture.setBlending(Texture2D.FUNC_REPLACE);
	  wallTexture.setFiltering(Texture2D.FILTER_LINEAR,
							   Texture2D.FILTER_NEAREST);
	  // set the texture
	  wallAppearance.setTexture(0, wallTexture);
	  wallClearAppearance.setTexture(0, wallTexture);
	}

	// The maze create the planes and they are added to the world
	Enumeration wallsEnum = maze.createPlanes();
	while (wallsEnum.hasMoreElements())
	{
	  Mesh wallMesh = ((Plane) wallsEnum.nextElement()).createMesh();
	  wallMesh.setAppearance(0, wallAppearance);
	  world.addChild(wallMesh);
	}

  }

  /**
   * decides whether it is allowed to move to a given place
   * otherwise it is possible to cross walls and go outside the maze
   * @param stepx the attempted x step
   * @param stepz the attempted z step
   * @param forward true if direction is forward
   * @return true if allowed to move to a given place
   */
  private boolean canMove(float stepx, float stepz, boolean forward)
  {
	float x = locationx + stepx;
	float z = locationz + stepz;
	// First check if inside the maze area
	float mazeSize = MAZE_SIDE_LENGTH / 2 - 2;
	if (x <= -mazeSize
		|| z <= -mazeSize
		|| x > mazeSize
		|| z > mazeSize)
	{
	  return false;
	}
	// collision with walls is easily implemented using picking
	// if there is something in front of the camera and the distance is too
	// small, the movement is not allowed
	RayIntersection ri = new RayIntersection();
	// inverse the point of view to detect if it is moving
	// bacwards
	if (!forward)
	{
	  camera.postRotate(180, 0f, 1f, 0f);
	}
	if (world.pick(-1, 0.5f, 0.5f, camera, ri))
	{
	  Node selected = ri.getIntersected();
	  if (selected instanceof Mesh)
	  {
		// multiply per 9 since that's the size of a step
		float distance = ri.getDistance() * 9;

		if (distance <= 0.1)
		{
		  return false;
		}
	  }
	}
	if (!forward)
	{
	  // revert to the original view
	  camera.postRotate(-180, 0f, 1f, 0f);
	}
	return true;
  }

  /**
   * Method checks whenever the keys are pressed
   */
  private void checkKeys()
  {
	int keyState = getKeyStates();
	boolean moved = false;

	// if fire is detected the mesh in front of you is made semi transparent
	if ((keyState & FIRE_PRESSED) != 0)
	{
	  RayIntersection ri = new RayIntersection();
	  if (world.pick(-1, 0.5f, 0.5f, camera, ri))
	  {
		// if there is something to pick
		Node selected = ri.getIntersected();
		if (selected instanceof Mesh)
		{
		  // make the wall semi transparent
		  transparentMesh = ((Mesh) selected);
		  transparentMesh.setAppearance(0, wallClearAppearance);
		  transparentMesh.setAlphaFactor(0.8f);
		}
	  }
	}
	// here we assume only one button is pressed otherwise the "second"
	// press is ignored
	if ((keyState & LEFT_PRESSED) != 0)
	{
	  // move to the left
	  angley += ANGLE_STEP;
	  stepx = STEP * (float) Math.sin(Math.toRadians(angley));
	  stepz = STEP * (float) Math.cos(Math.toRadians(angley));
	  // shift the background a bit to the right
	  background.setCrop(background.getCropX() + 3,
						 0,
						 getWidth(),
						 getHeight());
	  moved = true;
	}
	else if ((keyState & RIGHT_PRESSED) != 0)
	{
	  // move to the right
	  angley -= ANGLE_STEP;
	  stepx = STEP * (float) Math.sin(Math.toRadians(angley));
	  stepz = STEP * (float) Math.cos(Math.toRadians(angley));
	  // shift the background a bit to the left
	  background.setCrop(background.getCropX() - 3,
						 0,
						 getWidth(),
						 getHeight());
	  moved = true;
	}
	else if ((keyState & UP_PRESSED) != 0)
	{
	  // move forward, but first check if it is allowed
	  if (canMove(-stepx, -stepz, true))
	  {
		locationx -= stepx;
		locationz -= stepz;
	  }
	  moved = true;
	}
	else if ((keyState & DOWN_PRESSED) != 0)
	{
	  // move backwards, but first check if it is allowed
	  if (canMove(stepx, stepz, false))
	  {
		locationx += stepx;
		locationz += stepz;
	  }
	  moved = true;
	}

	// if moved we make the last transparent wall normal
	// and reset the view
	if (moved)
	{
	  if (!finished && maze.isAtTheEnd(locationx, locationz, 10f))
	  {
		finished = true;
	  }
	  // if there was a mesh semi transparent
	  // return to normal
	  if (transparentMesh != null)
	  {
		transparentMesh.setAppearance(0, wallAppearance);
		transparentMesh.setAlphaFactor(1f);
		transparentMesh = null;
	  }
	  // if we move we reset the top view state
	  if (topView)
	  {
		switchView();
	  }
	  setupCamera();
	}
  }


  /**
   * Key pressed used to show the main menu
   * @param key the key that was pressed
   */
  protected void keyPressed(int key)
  {
	if (key < 0)
	{
	  midlet.showMenu();
	}
  }


  /**
   * Sets the camera location, orientation, location.
   */
  private void setupCamera()
  {
	camera.setOrientation(angley, 0f, 1f, 0f);
	camera.setTranslation(locationx, 10f, locationz);
  }

  /**
   * Draw 2d information on top of the screen
   * @param g the graphics object
   */
  private void draw2D(Graphics g)
  {
       // text color
	g.setColor(45, 235, 45);
	StringBuffer status = new StringBuffer("");
	if (finished)
	{
	  status.append("Done");
	}
	else
	{
	  duration = System.currentTimeMillis() - gameStart;
	}
	status.append(" Time:").append(duration / 1000);

	// draw time on the top right
	g.drawString(status.toString(),
				 getWidth(),
				 0,
				 Graphics.TOP | Graphics.RIGHT);
	// draw FPS on the bottom left
	g.drawString("FPS: " + fps,
				 0,
				 getHeight(),
				 Graphics.BOTTOM | Graphics.LEFT);
  }

  /**
   * paints the scene
   * @param g graphics object
   */
  private void draw3D(Graphics g)
  {
	boolean bound = false;
	try
	{
	  // binds the target
	  g3d.bindTarget(g);
	  bound = true;
	  // advance the animation
	  world.animate((int)(System.currentTimeMillis() - gameStart));
	  // do the rendering
	  g3d.render(world);
	}
	finally
	{
	  // release the target
	  if (bound)
	  {
		g3d.releaseTarget();
	  }
	}
  }


  /**
   * The threads run method
   */
  public void run()
  {
	Graphics g = getGraphics();
	while (playing)
	{
	  try
	  {
		long startTime = System.currentTimeMillis();
		if (isShown())
		{
		  // update the world if the player has moved
		  checkKeys();
		  long drawingTime = System.currentTimeMillis();

		  // draw the 3d part
		  draw3D(g);
		  drawingTime = System.currentTimeMillis() - drawingTime;
		  // calculate fps
		  fps = (drawingTime <= 1) ? 1000 : 1000 / drawingTime;

		  // draw the 2d part
		  draw2D(g);
		  // flush to the screen
		  flushGraphics();
		}
		// wait for a little and give the other threads
		// the chance to run
		long timeTaken = System.currentTimeMillis() - startTime;
		synchronized (this)
		{
		  if (timeTaken < loopDuration)
		  {
			wait(loopDuration - timeTaken);
			if (loopDuration > MILLIS_PER_TICK)
			{
			  loopDuration--;
			}
		  }
		  else
		  {
		    loopDuration = timeTaken;
		    wait(1);
		  }
		}
	  }
	  catch (Exception e)
	  {
		// ignore. Not much to but we want to keep the loop running
	  }
	}
  }


  /**
   * It is possible that the canvas size may change and the application
   * must be prepared to resize itself accordingly.  This routine just
   * calls application methods stop, init to handle this
   * change in conditions.
   * @param w width
   * @param h height
   */
  protected void sizeChanged(int w, int h)
  {
      stop();
      init();
  }

}


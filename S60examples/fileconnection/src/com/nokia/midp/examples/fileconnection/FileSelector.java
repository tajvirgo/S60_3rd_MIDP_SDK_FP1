/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.fileconnection;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import com.nokia.midp.examples.fileconnection.OperationsQueue;
/**
 * A class used to access the file system, show currently available mid files
 * and play them.
 */
class FileSelector
    extends javax.microedition.lcdui.List
    implements CommandListener, FileSystemListener, PlayerListener {
  private final static Image FILE_IMAGE = FileConnDemo.makeImage(
      "/file.png");
  private final static Image ROOT_IMAGE = FileConnDemo.makeImage(
      "/root.png");
  private final OperationsQueue queue = new OperationsQueue();

  private final static String FILE_SEPARATOR =
      (System.getProperty("file.separator") != null) ?
      System.getProperty("file.separator") : "/";

  private final FileConnDemo midlet;
  private final Command playCommand = new Command("Play", Command.SCREEN, 1);
  private final Command stopCommand = new Command("Stop", Command.SCREEN, 1);
  private final Command deleteCommand = new Command("Delete", Command.SCREEN, 2);
  private final Command aboutCommand = new Command("About", Command.SCREEN, 3);

  private final static int INIT_OP = 0;
  private final static int OPEN_OP = 1;
  private final static int DELETE_OP = 2;

  private Vector rootsList = new Vector();

  // Stores the current root, if null we are showing all the roots
  private FileConnection currentRoot = null;
  private FileConnection fileConn = null;
  private InputStream fis = null;
  private Player player;
  private boolean playing = false;

  /**
   * Constructor
   * @param midlet FileConnDemo
   */
  FileSelector(FileConnDemo midlet) {
    super("Media Player", List.IMPLICIT);
    this.midlet = midlet;

    addCommand(playCommand);
    addCommand(deleteCommand);
    addCommand(aboutCommand);
    setSelectCommand(playCommand);
    setCommandListener(this);

    queue.enqueueOperation(new MediaPlayerOperations(INIT_OP));
    FileSystemRegistry.addFileSystemListener(FileSelector.this);
  }

  void stop() {
    queue.abort();
    FileSystemRegistry.removeFileSystemListener(this);
  }

  /**
   * The command listener command action implementation.
   * @param c Command
   * @param d Displayable
   */
  public void commandAction(Command c, Displayable d) {
    if ( (c == playCommand) || (c == List.SELECT_COMMAND && !playing)) {
      queue.enqueueOperation(new MediaPlayerOperations(OPEN_OP));
    }
    else if ( (c == stopCommand) || (c == List.SELECT_COMMAND && playing)) {
      stopPlaying();
      playingStopped();
    }
    else if (c == deleteCommand) {
      queue.enqueueOperation(new MediaPlayerOperations(DELETE_OP));
    }
    else if (c == aboutCommand) {
      midlet.displayAboutInfo();
    }
    else {
      stopPlaying();
      midlet.fileSelectorExit();
    }
  }

  public void playerUpdate(Player p, String event, Object eventData) {
    if (event == CLOSED) {
      player = null;
      playingStopped();     
    }
  }

  /**
   * Processing requires when playing is stopped.
   */
  void stopPlaying() {    
    if (player != null) {
      player.close();     
    }
    if (fileConn != null) {
      try {
        fis.close();
        fis = null;
        fileConn.close();
        fileConn = null;
      }
      catch (IOException e) {
        midlet.showError(e);
      }     
    }
  }

  /**
   * Used to start playing.
   */
  private void playingStarted() {    
    removeCommand(playCommand);
    addCommand(stopCommand);
    playing = true;   
  }

  /**
   * Used to stop playing.
   */
  private void playingStopped() {   
    removeCommand(stopCommand);
    addCommand(playCommand);
    playing = false;    
  }

  /**
   * Listen for changes in the roots.
   */
  public void rootChanged(int state, String rootName) {
    queue.enqueueOperation(new MediaPlayerOperations(INIT_OP));
  }

  /**
   * A method used to display all the roots.
   */
  private void displayAllRoots() {
    setTitle("Media Player - [Roots]");
    deleteAll();
    Enumeration roots = rootsList.elements();
    while (roots.hasMoreElements()) {
      String root = (String) roots.nextElement();
      append(root.substring(1), ROOT_IMAGE);
    }
    currentRoot = null;
  }

  /**
   * A method used to load the roots.
   */
  private void loadRoots() {
    if (!rootsList.isEmpty()) {
      rootsList.removeAllElements();
    }
    try {
      Enumeration roots = FileSystemRegistry.listRoots();
      while (roots.hasMoreElements()) {
        rootsList.addElement(FILE_SEPARATOR + (String) roots.nextElement());
      }
    }
    catch (Throwable e) {
      midlet.showMsg(e.getMessage());
    }
  }

  /**
   * A method used to delete the current selection.
   */
  private void deleteCurrent() {    
    if (playing == true) {
      stopPlaying();
      playingStopped();
    }
    int selectedIndex = getSelectedIndex();    
    if (selectedIndex >= 0) {
      String selectedFile = getString(selectedIndex);

      try {
        FileConnection fileToDelete =
            (FileConnection) Connector.open(currentRoot.getURL() + selectedFile);                
        if (fileToDelete.exists()) {          
          fileToDelete.delete();        
          fileToDelete.close();        
        }
        else {        
          midlet.showMsg("File " + fileToDelete.getName() + " does not exists");
        }
      }
      catch (IOException e) {
        midlet.showError(e);
      }
      catch (SecurityException e) {
        midlet.showError(e);
      }
      displayCurrentRoot();
    }
  }

  /**
   * A method used to open the current selection.
   */
  private void openSelected() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex >= 0) {
      String selectedFile = getString(selectedIndex);
      String url = currentRoot.getURL() + selectedFile;
      try {
        fileConn = (FileConnection) Connector.open(url, Connector.READ);
        fis = fileConn.openInputStream();
        player = Manager.createPlayer(fis, "audio/midi");
        player.realize();
        player.setLoopCount( -1);
        player.start();
        playingStarted();
      }
      catch (IOException e) {
        midlet.showError(e);
      }
      catch (MediaException e) {
        midlet.showError(e);
      }
    }
  }

  /**
   * A method used to display the current root.
   */
  private void displayCurrentRoot() {
    try {
      setTitle("Media Player - [" + currentRoot.getURL() + "]");
      // open the root
      deleteAll();

      Enumeration listOfFiles = currentRoot.list("*.mid", false);
      while (listOfFiles.hasMoreElements()) {
        String currentFile = (String) listOfFiles.nextElement();
        append(currentFile, FILE_IMAGE);
      }

    }
    catch (IOException e) {
      midlet.showError(e);
    }
    catch (SecurityException e) {
      midlet.showError(e);
    }
  }

  private class MediaPlayerOperations
      implements Operation {
    private final String parameter;
    private final int operationCode;

    MediaPlayerOperations(int operationCode) {
      this.parameter = null;
      this.operationCode = operationCode;
    }

    MediaPlayerOperations(String parameter, int operationCode) {
      this.parameter = parameter;
      this.operationCode = operationCode;
    }

    public void execute() {
      switch (operationCode) {
        case INIT_OP:          
          String initDir = System.getProperty("fileconn.dir.music");                  
          loadRoots();         
          if (initDir != null) {
            try {              
              currentRoot = (FileConnection) Connector.open(initDir,
                  Connector.READ);              
              displayCurrentRoot();              
            }
            catch (Exception e) {
              midlet.showError(e);
              displayAllRoots();
            }
          }
          else {
            displayAllRoots();
          }
          break;

        case OPEN_OP:
          openSelected();
          break;

        case DELETE_OP:
          deleteCurrent();
          break;
      }
    }
  }
}

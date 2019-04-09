/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.fileconnection;

import java.util.*;

/**
 * A class that implements a queue for Operations.
 * It runs in an independent thread and executes Operations serially.
 */
class OperationsQueue
    implements Runnable {

  private volatile boolean running = true;
  private final Vector operations = new Vector();

  OperationsQueue() {
    // Notice that all operations will be done in another
    // thread to avoid deadlocks with GUI thread
    new Thread(this).start();
  }

  /**
   * Adds an operation to the queue.
   * @param nextOperation Operation
   */
  void enqueueOperation(Operation nextOperation) {
    operations.addElement(nextOperation);
    synchronized (this) {
      notify();
    }
  }

  /**
   *  Stops the thread.
   */
  void abort() {
    running = false;
    synchronized (this) {
      notify();
    }
  }

  /**
   * The run method for the Thread.
   */
  public void run() {
    while (running) {
      while (operations.size() > 0) {
        try {
          // execute the first operation on the queue
          ( (Operation) operations.firstElement()).execute();
        }
        catch (Exception e) {
          // Nothing to do. It is expected that each operations handle
          // their own locally exception but this block is to ensure
          // that the queue continues to operate
        }
        operations.removeElementAt(0);
      }
      synchronized (this) {
        try {
          wait();
        }
        catch (InterruptedException e) {
          // it doesn't matter
        }
      }
    }
  }
}

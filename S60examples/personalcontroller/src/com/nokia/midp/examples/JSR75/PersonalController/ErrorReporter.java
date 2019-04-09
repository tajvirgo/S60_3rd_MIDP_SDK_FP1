/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

/**

ErrorReporter provides error printing services to the other classes.

*/


public final class ErrorReporter{
    public static void reportError(Exception e){
    e.printStackTrace();
    }
}

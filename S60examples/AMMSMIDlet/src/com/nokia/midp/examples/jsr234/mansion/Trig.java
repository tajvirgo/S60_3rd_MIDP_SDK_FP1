package com.nokia.midp.examples.jsr234.mansion;

/**
 * Trigonometrical function estimates.
 *
 */
public class Trig{
    private final static int SIN[]=
    {   
		0, 174, 342, 500, 643, 766, 866, 940, 985, 1000, 985, 940, 866, 766,
		643, 500, 342, 174, 0, -174, -342, -500, -643, -766, -866, -940, -985,
		-1000, -985, -940, -866, -766, -643, -500, -342, -174 
	};

    private final static int COS[]= 
    {
		1000, 985, 940, 866, 766, 643, 500, 342, 174, 0, -174, -342, -500, -643,
		-766, -866, -940, -985, -1000, -985, -940, -866, -766, -643, -500, -342,
		-174, 0, 174, 342, 500, 643, 766, 866, 940, 985 
	};

    /**
     * Sine estimator
     *
     * @param alpha in deci-degrees, between 0 and 35
     * @return in thousandths
     */
    private static int sin(int alpha) {
    	return SIN[alpha];
    }
    
    /**
     * Cosine estimator
     *
     * @param alpha in deci-degrees, between 0 and 35
     * @return in thousandths
     */
    private static int cos(int alpha) {
    	return COS[alpha];
    }  

    /**
     * Calculates the rotation
     *
     * @param theta in deci-degrees, between 0 and 35
     * @return new x 
     */
    public static int transX(int x, int y, int theta){
    	return (x * cos(theta) - y * sin(theta))/1000;
    }

    /**
     * Calculates the rotation
     *
     * @param theta in deci-degrees, between 0 and 35
     * @return new y
     */
    public static int transY(int x, int y, int theta){
		// minus because of y-coordinate of the screen upside down(?)
		return -(x * sin(theta) + y * cos(theta))/1000;
    }

   /**
     * Calculates the rotation for the screen coordinates directly
     *
     * @param theta in deci-degrees, between 0 and 35
     * @return new x 
     */
    public static int transXS(int x, int y, int theta){
    	return -(x * cos(theta) - y * sin(theta))/50000;
    }

    /**
     * Calculates the rotation for the screen coordinates directly
     *
     * @param theta in deci-degrees, between 0 and 35
     * @return new y
     */
    public static int transYS(int x, int y, int theta){
		// minus because of y-coordinate of the screen upside down(?)
		return (x * sin(theta) + y * cos(theta))/50000;
    }
}

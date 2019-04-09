/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.svg;


import java.util.*;
import java.io.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.io.*;

import javax.microedition.m2g.*;
import org.w3c.dom.svg.*;


/**
 * Simple canvas that uses JSR-226 to render SVG content.
 */
public class SvgCanvas2 extends GameCanvas  {

	/**
	 * Constructor.
	 */
    public SvgCanvas2 (boolean suppressKeyEvents)  {
	    super(suppressKeyEvents);

		// *** setup an instance of ScalableGraphics
    	sg = ScalableGraphics.createInstance();
		sg.setRenderingQuality(sg.RENDERING_QUALITY_HIGH);

        // *** load an svg image from a file
    	try {
	        InputStream svgStream = getClass().getResourceAsStream("content2.svg");
	        svgImage = (SVGImage)( SVGImage.createImage( svgStream, null ) );

			// ** set the width and height of the document to match the screen capabilities
	        svgImage.setViewportWidth( getWidth() );
	        svgImage.setViewportHeight( getHeight() );

            } catch ( Exception e ){
                e.printStackTrace();
	    }
    }


	/**
	 * Paint method.
	 */
    public void paint(Graphics g)  {
    	// *** clear the display
    	g.setColor(255, 255, 255);
    	g.fillRect(0, 0, getWidth(), getHeight());

    	// *** render the SVG image
    	sg.bindTarget( g );
	    sg.setTransparency(1f);
	    sg.render(0, 0, svgImage);
		sg.releaseTarget();
    }


	/**
	 * Restore the original view of the SVG Image.
	 */
    public void restoreView() {
	    SVGSVGElement myEl = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
	    myEl.setCurrentRotate(0);
	    myEl.setCurrentScale(1);
	    SVGPoint origin = myEl.getCurrentTranslate();
	    origin.setX(0);
	    origin.setY(0);
	    repaint();
    }

	/**
	 * Zoom in on the SVG Image.
	 */
    public void zoomIn() {
	    SVGSVGElement myEl = (SVGSVGElement)(svgImage.getDocument().getDocumentElement());
	    myEl.setCurrentScale(myEl.getCurrentScale() * 1.2f);
	    repaint();
    }

	/**
	 * Zoom out on the SVG Image.
	 */
    public void zoomOut() {
	    SVGSVGElement myEl = (SVGSVGElement)(svgImage.getDocument().getDocumentElement());
	    myEl.setCurrentScale(myEl.getCurrentScale() * 0.8f);
	    repaint();
    }


	/**
	 * Rotate out on the SVG Image.
	 */
    public void rotateOut() {
	    SVGSVGElement myEl = (SVGSVGElement)(svgImage.getDocument().getDocumentElement());
	    myEl.setCurrentRotate(myEl.getCurrentRotate() + 10);
	    repaint();
    }


	/**
	 * Rotate in on the SVG Image.
	 */
    public void rotateIn() {
	    SVGSVGElement myEl = (SVGSVGElement)(svgImage.getDocument().getDocumentElement());
	    myEl.setCurrentRotate(myEl.getCurrentRotate() - 10);
	    repaint();
    }


	/**
	 * Key repeat method.
	 */
    protected void keyRepeated(int keyCode){
		keyPressed(keyCode);
    }


	/**
	 * Handle key presses.
	 */
    protected void keyPressed(int keyCode) {
		SVGSVGElement svgDoc = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
		int action = getGameAction(keyCode);
		SVGPoint origin = svgDoc.getCurrentTranslate();
		switch (action) {
			case RIGHT:
				origin.setX(origin.getX() + 5f);
				break;
			case LEFT:
				origin.setX(origin.getX() - 5f);
				break;
			case UP:
				origin.setY(origin.getY() - 5f);
				break;
			case DOWN:
				origin.setY(origin.getY() + 5f);
				break;
		}
		repaint();
    }

    protected void sizeChanged(int w, int h)
    {
    	System.out.println("sizeChanged(): w = "+w+", h = "+h);
      	svgImage.setViewportWidth(w);
    	svgImage.setViewportHeight(h);
    	repaint();
    }    

	/*
	 * Private members
	 */

    private ScalableGraphics sg;
    private SVGImage svgImage;

}

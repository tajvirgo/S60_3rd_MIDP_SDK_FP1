/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.svg;


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

import javax.microedition.m2g.*;
import org.w3c.dom.svg.*;


/**
 * Simple canvas that uses JSR-226 to render SVG content.
 */
public class SvgCanvas1 extends GameCanvas  {
    
	/**
	 * Constructor.
	 */
    public SvgCanvas1 (boolean suppressKeyEvents)  {
	    super(suppressKeyEvents);

		// *** setup an instance of ScalableGraphics
    	sg = ScalableGraphics.createInstance();
		sg.setRenderingQuality(sg.RENDERING_QUALITY_HIGH);
		
		// *** create a simple SVG image
		
		// ** create an empty image
		svgImage = (SVGImage)( SVGImage.createEmptyImage( null ) );

		// ** grab the root <svg> element
		SVGSVGElement rootElement = (SVGSVGElement)(svgImage.getDocument().getDocumentElement());
		
		// ** create some basic colors to work with
		SVGRGBColor yellow = rootElement.createSVGRGBColor( 255, 255, 0 );
		SVGRGBColor black = rootElement.createSVGRGBColor( 0, 0, 0 );
		
		// ** create the face and add it to the document
		SVGElement circle = (SVGElement)(svgImage.getDocument().createElementNS( "http://www.w3.org/2000/svg", "circle" ));
		circle.setRGBColorTrait( "fill", yellow  );
		circle.setFloatTrait( "r", 50 );
		circle.setFloatTrait( "cx", 50 );
		circle.setFloatTrait( "cy", 50 );
		rootElement.appendChild( circle );
		
		// ** create the left eye and add it to the document
		circle = (SVGElement)(svgImage.getDocument().createElementNS( "http://www.w3.org/2000/svg", "circle" ));
		circle.setFloatTrait( "r", 5 );
		circle.setFloatTrait( "cx", 30 );
		circle.setFloatTrait( "cy", 30 );
		rootElement.appendChild( circle );

		// ** create the right eye and add it to the document
		circle = (SVGElement)(svgImage.getDocument().createElementNS( "http://www.w3.org/2000/svg", "circle" ));
		circle.setFloatTrait( "r", 5 );
		circle.setFloatTrait( "cx", 70 );
		circle.setFloatTrait( "cy", 30 );
		rootElement.appendChild( circle );
		
		// ** create the mouth and add it to the document
		SVGElement path = (SVGElement)(svgImage.getDocument().createElementNS( "http://www.w3.org/2000/svg", "path" ));
		SVGPath d = rootElement.createSVGPath();
		d.moveTo( 20, 60 );
		d.quadTo( 50, 80, 80, 60 );
		path.setPathTrait( "d", d );
		path.setRGBColorTrait( "stroke", black );
		path.setFloatTrait( "stroke-width", 3 );
		path.setTrait( "fill", "none" );
		rootElement.appendChild( path );
		
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


	/*
	 * Private members
	 */
	 
    private ScalableGraphics sg;
    private SVGImage svgImage;

}

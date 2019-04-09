package com.nokia.midp.examples.xmlparser;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * XMLMIDlet demonstrates how to parse a file written in XML format. 
 * The XMLMIDlet implements Web Services API (JSR-172) and its XML Parser package.
 */

public class XMLMIDlet extends MIDlet implements CommandListener
{
	private Form form;
	private Command exitCommand;
	private Command parseCommand;

	/**
	 * XMLMIDlet class
	 * This MIDlet shows an simple example, how data can be parsed out of and
	 * a file in xml format by using Web Services API (JSR-172) and its XML Parser package.  
	 */	

	public XMLMIDlet() {
		form = new Form("XMLMIDlet");
		parseCommand = new Command("Parse", Command.ITEM, 1);
		exitCommand = new Command("Exit", Command.EXIT, 1);
		form.addCommand(parseCommand);
		form.addCommand(exitCommand);
		form.setCommandListener(this);
		Display.getDisplay(this).setCurrent(form);		
	}

	protected void startApp() {} 

	/**
	 * This method does the parsing work: SAXParserFactory instance and SAXParser are 
	 * created. The xml data is read from a local file devicedata.xml by using InputStream
	 * and InputSource. EventHandler (extending DefaultHandler) instance is also created.
	 */
	protected void parse() 
	{
		try
		{
			alert("Initializing...\n");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			InputStream is = getClass().getResourceAsStream("/com/nokia/midp/examples/xmlparser/res/devicedata.xml");
			InputSource inputSource = new InputSource(is);
			alert("Parsing...\n");
			saxParser.parse(is,new EventHandler(this));
		}
		catch(IOException ioe) {
			alert("ioe:"+ioe.getMessage()+"\n");
		}
		catch(SAXException saxe) {
			alert("saxe:"+saxe.getMessage()+"\n");
		}
		catch(ParserConfigurationException pce) {
			alert("pce:"+pce.getMessage()+"\n");
		}
		catch(SecurityException se) {
			alert("se:"+se.getMessage()+"\n");
		}
		alert("Parsing done!");
	}
	
	/**
	 * This method shows String msg both in console and in the main form of the MIDlet.
	 * @param msg is the string to be shown.
	 */
	protected void alert(String msg)
	{
		System.out.println("" + msg);
		form.append("" + msg);
	}

	protected void pauseApp() {}

	protected void destroyApp(boolean bool) {}

	/**
	 * Handles the received commands.
	 * @param c is the Command to be handled
	 * @param d is the Displayable on which this event has occurred
	 */
	public void commandAction(Command c, Displayable d) {
		if (c == parseCommand) {
			parse();
		}
		if (c == exitCommand) {
			notifyDestroyed();
		}
	}
}
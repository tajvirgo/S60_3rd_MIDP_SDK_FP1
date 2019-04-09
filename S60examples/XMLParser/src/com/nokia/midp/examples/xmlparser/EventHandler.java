package com.nokia.midp.examples.xmlparser;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;

/**
 * EventHandler extends DefaultHandler, which is the default base class for SAX2 event handlers.
 * It utilises the following methods: startDocument(), startElement(), characters(), endElement() 
 * and endDocument(). 
 *
 */
class EventHandler extends DefaultHandler
{
	private XMLMIDlet midlet;
	private Vector phones = new Vector(); 
	private Stack stack = new Stack();

    public EventHandler (XMLMIDlet midlet)
    {
    	this.midlet = midlet;
    }

    public void startDocument() throws SAXException {}

    /**
     * Receive notification of the start of an element.
     * @param uri
     * @param localName
     * @param qName is the qualified name (with prefix), in this case "phone".
     * @param attributes
     * 
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{    	
		if(qName.equals("phone"))
		{
			Phone phone = new Phone();
			phones.addElement(phone);
		}
		stack.push(qName);
	}

    /**
     * 
     * @param ch is an array of the characters to be parsed.
     * @param start is start value used for creating a String chars.
     * @param length is end value used for creating a String chars.
     * @throws SAXException
     * 
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    { 	
    	String chars = new String(ch, start, length).trim();
    	if(chars.length() > 0)
    	{
    		String qName = (String)stack.peek();
    		Phone currentPhone = (Phone)phones.lastElement();   
    		if (qName.equals("name"))
    		{
    			currentPhone.setName(chars);
    		}
    		else if(qName.equals("midpver"))
    		{
    			currentPhone.setMIDP(chars);
    		}
    		else if(qName.equals("screensize"))
    		{
    			currentPhone.setScreenSize(chars);
    		}
    	}
    }

    /**
     * Receive notification of the end of an element. This method does only thing: it removes the object at the top of the stack 
     * (and would return that object as the value of this function).
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */
    public void endElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	stack.pop();
    }

    /**
     * Receive notification of the end of the document. A StringBuffer is created. The parsed data is appended to the StringBuffer
     * and added to the MIDlet's Form by using MIDlet's alert() method.
     * @throws SAXException
     */
    public void endDocument() throws SAXException
    {
    	StringBuffer result = new StringBuffer();
    	for (int i=0; i<phones.size(); i++)
    	{
    		Phone currentPhone = (Phone)phones.elementAt(i);
    		result.append(currentPhone.getName() + ":\n");
    		result.append(currentPhone.getMIDP() + "\n");
    		result.append(currentPhone.getScreenSize() + "\n");
    	}
    	midlet.alert(result.toString());
    }
}
package com.nokia.midp.examples.xmlparser;

/**
 * The Phone class creates an Phone object. 
 * The object can have three Strings as features.
 * Strings are: name, midpver and size, meaning phone model, MIDP version and screen size.
 */

public class Phone 
{
    public String name;
	public String midpver;
    public String size;
	
    public Phone()
	{
	}
    
    public String getName() {
        return (String)this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getMIDP() {
        return (String)this.midpver;
    }
    
    public void setMIDP(String midpver) {
        this.midpver = midpver;
    }    
    
    public String getScreenSize() {
        return (String)this.size;
    }
    
    public void setScreenSize(String size) {
        this.size = size;
    }
}
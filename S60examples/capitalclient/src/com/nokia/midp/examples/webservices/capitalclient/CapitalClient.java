/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.webservices.capitalclient;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import com.nokia.midp.examples.webservices.*;

/**
 * A simple example of using a Web Service from inside a MIDlet.
 */
public class CapitalClient
    extends MIDlet
    implements CommandListener {

  /** The interface to the server's service */
  private CapitalPortType_Stub client;

  /** Command to exit the MIDlet */
  private Command exitCommand;

  /** Command to query the capital over network */
  private Command queryCommand;

  /** MIDlet's main form */
  private Form mainForm;

  /** Text field that is used for entering the nation */
  private TextField nationTextField;

  /** String item that shows the capital of the nation */
  private StringItem capitalStringItem;

  /** String item that is used for entering the web server */
  private TextField serverAddressField;

  /** String item that shows the web server address */
  private TextField serverAddressItem;

  private static final String enterIPMsg = "Enter the server IP address.";
  private static final String enterNationMsg = "Enter the nation name.";



  /**
   * Constructs the MIDlet. Instantiates the stub that is used to call the Web
   * Service on the localhost.
   */
  public CapitalClient() {
    mainForm = new Form("Capital Client");

    nationTextField = new TextField("Enter nation:", "Finland", 50, 0);
    capitalStringItem = new StringItem("Capital:", "");

    serverAddressField = new TextField("Enter server IP address:", "localhost",70, 0);

    mainForm.append(serverAddressField);
    mainForm.append(nationTextField);
    mainForm.append(capitalStringItem);

    queryCommand = new Command("Get capital", Command.ITEM, 1);
    exitCommand = new Command("Exit", Command.EXIT, 2);

    mainForm.addCommand(queryCommand);
    mainForm.addCommand(exitCommand);
    mainForm.setCommandListener(this);

    // Construct the interface to server's service
    client = new CapitalPortType_Stub();
  }

  protected void startApp() {
    Display.getDisplay(this).setCurrent(mainForm);
  }

  protected void pauseApp() {
  }

  protected void destroyApp(boolean bool) {
  }

  /**
   * When query command is issued, MIDlet makes a call to the
   * web service and displays the result in the text box.
   */
  public void commandAction(Command cmd, Displayable disp) {
    if (cmd == exitCommand) {
      destroyApp(false);
      notifyDestroyed();
    }
    else if (cmd == queryCommand) {
      new Thread(new Runnable() {
        public void run() {
          try {
            if(serverAddressField.getString().trim().length()>0){
              client.setServerIPAddress(serverAddressField.getString().trim());
            }else{
              mainForm.append(enterIPMsg);
            }
              /** Call the Web Service */
            String response = client.getCapital(nationTextField
                                                .getString().trim());
            if(response.length()>0){
              /** Display response in the text box */
              capitalStringItem.setText(response);
            }else{
              mainForm.append(enterNationMsg);
            }
          }
          catch (Exception e) {
            capitalStringItem.setText("Error connecting to the Web Service");
          }
        }
      }).start();
    }
  }
}

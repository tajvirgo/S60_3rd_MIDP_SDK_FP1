/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.pki;

// SATSA-PKI
import javax.microedition.pki.*;
import javax.microedition.securityservice.*;

// CRYPTO
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

// OTHER
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;
import javax.microedition.io.*;

/**
 * This midlet is for generating signatures using CMSMessageSignatureService-class.
 * It demostrates how to
 * Verification for these midlet-made signatures is not done in this same midlet,
 * since CMSMessageSignatureService class-created Signatures are of CMS-format
 * and Signature-class verify()-method doesn´t accept CMS-formatted signatures.
 */

public class PKIMIDLET extends MIDlet implements CommandListener {
  private Form form;
  private Command exitCommand;
  private Command clearCommand;
  private Command authByteOpaqueCommand;
  private Command authByteDetachedCommand;
  private Command authStringOpaqueCommand;
  private Command authStringDetachedCommand;
  private Command signOpaqueCmd;
  private Command signDetachedCmd;

  // signed data
  // authenticate():
  private byte[] authByteOpaque = null;
  private byte[] authByteDetached = null;
  private byte[] authStringOpaque = null;
  private byte[] authStringDetached = null;
  // sign():
  private byte[] signOpaque = null;
  private byte[] signDetached = null;

  private TextField toBeSigned;
  private String stringToSign = "sign me";
  //The constants
  private static final String AUTH_SIGN_BYTE_CONTENT = "AuthenSignByteHasContent";
  private static final String AUTH_SIGN_BYTE_NO_CONTENT = "AuthenSignByteNoContent";
  private static final String AUTH_SIGN_STRING_CONTENT = "AuthenSignStringHasContent";
  private static final String AUTH_SIGN_STRING_NO_CONTENT = "AuthenSignStringNoContent";
  private static final String CMS_CONTENT = "CMS_WithContent";
  private static final String CMS_NO_CONTENT = "CMS_NoContent";
  private static final String CLEAR = "Clear";
  private static final String EXIT = "Exit";
  private static final String SIGN_LABEL = "Sign";
  private static final String SIGN_CONTENT = "ToBeSigned";
  private static final String USER_PROMPT = "Going to sign.";
  private static final String AUTH_SIGN_MSG = "Sign for authentification purpose.";
  private static final String AUTH_SIGN_MSG_BYTE_WITH_CONTENT = "Going to sign bytes with content for: ";
  private static final String AUTH_SIGN_MSG_BYTE_WITHOUT_CONTENT = "Going to sign bytes without content for: ";
  private static final String AUTH_SIGN_MSG_STRING_WITH_CONTENT = "Going to sign string with content for:  ";
  private static final String AUTH_SIGN_MSG_STRING_WITHOUT_CONTENT = "Going to sign string without content for:  ";
  private static final String CMS_SIGN_MSG = "Generate a CMS signed message.";
  private static final String CMS_SIGN_MSG_WITH_CONTENT = "Going to sign with content for ";
  private static final String CMS_SIGN_MSG_WITHOUT_CONTENT = "Going to sign without content for ";
  private static final String AFTER_SIGN_MSG = "Already signed. The length of the signed message for the above ";
  private static final String AFTER_BYTE_WITH_CONTENT = "bytes with content is: ";
  private static final String AFTER_BYTE_WITHOUT_CONTENT = "bytes without content is: ";
  private static final String AFTER_STRING_WITH_CONTENT = "string with content is: ";
  private static final String AFTER_STRING_WITHOUT_CONTENT = "string without content is: ";
  private static final String AFTER_STRING =  "string is: ";
  private static final String CANCEL = "Cancelled.";
  private static final String EXCEPTION = "Exception: ";
  private static final int AUTH_LENGTH = 128;
  private static final int SIGN_FILED_LENGTH = 100;


  public PKIMIDLET() {
    try {
      form = new Form("PKIMIDLET");
      authByteOpaqueCommand = new Command(AUTH_SIGN_BYTE_CONTENT, Command.OK, 1);
      authByteDetachedCommand = new Command(AUTH_SIGN_BYTE_NO_CONTENT, Command.OK, 2);
      authStringOpaqueCommand = new Command(AUTH_SIGN_STRING_CONTENT, Command.OK, 3);
      authStringDetachedCommand = new Command(AUTH_SIGN_STRING_NO_CONTENT, Command.OK, 4);
      signOpaqueCmd = new Command(CMS_CONTENT, Command.OK, 5);
      signDetachedCmd = new Command(CMS_NO_CONTENT, Command.OK, 6);
      clearCommand = new Command(CLEAR, Command.OK, 7);
      exitCommand = new Command(EXIT, Command.EXIT, 8);
      form.addCommand(authByteOpaqueCommand);
      form.addCommand(authByteDetachedCommand);
      form.addCommand(authStringOpaqueCommand);
      form.addCommand(authStringDetachedCommand);
      form.addCommand(signOpaqueCmd);
      form.addCommand(signDetachedCmd);
      form.addCommand(clearCommand);
      form.addCommand(exitCommand);
      form.setCommandListener(this);

      toBeSigned = new TextField(SIGN_LABEL, SIGN_CONTENT, SIGN_FILED_LENGTH, TextField.ANY);
      form.append(toBeSigned);
    }
    catch (Exception ex) {
      String cName = ex.getClass().getName();
      form.append(EXCEPTION + cName + ": " + ex.getMessage());
    }
  }
  public void startApp() {
    Display.getDisplay(this).setCurrent(form);
  }

  public void destroyApp(boolean b) {}
  public void pauseApp() {}

  public void commandAction (Command c, Displayable d) {
    if (c == exitCommand) {
      destroyApp(false);
      notifyDestroyed();
    }

    // getting der-encoded signature incl. byte array-content (opaque)
    // FOR AUTHENTICATION PURPOSES
    else if (c == authByteOpaqueCommand) {
      try {
        authByteOpaque = new byte[AUTH_LENGTH];
        String[] caNames = null; // implementation provides search
        byte[] byteDataToSign = stringToSign.getBytes();
        String userPrompt = USER_PROMPT;
        form.append(AUTH_SIGN_MSG);
        form.append(AUTH_SIGN_MSG_BYTE_WITH_CONTENT  + "\"" + stringToSign + ".\"");
        authByteOpaque = CMSMessageSignatureService.authenticate(
            byteDataToSign,
            CMSMessageSignatureService.SIG_INCLUDE_CERTIFICATE
            |CMSMessageSignatureService.SIG_INCLUDE_CONTENT,
            caNames, userPrompt);

        if(authByteOpaque != null) {
          form.append(AFTER_SIGN_MSG);
          form.append(AFTER_BYTE_WITH_CONTENT + authByteOpaque.length);
        }
        else {
          form.append(CANCEL);
        }
      }
      catch (SecurityException se){
        form.append("Too many incorrect pin entries.");
      }
      catch (CMSMessageSignatureServiceException cmsEx){
        form.append("Error occured during signature generation.");
        if(cmsEx.getReason()==CMSMessageSignatureServiceException.CRYPTO_NO_CERTIFICATE){
          form.append("The certificate is not available.");
        }
        if(cmsEx.getReason()==CMSMessageSignatureServiceException.CRYPTO_NO_OPAQUE_SIG){
          form.append("Opaque signature is not supported.");
        }
      }
      catch (UserCredentialManagerException ucmEx){
        if(ucmEx.getReason()== UserCredentialManagerException.SE_NOT_FOUND){
          form.append("A security element is not found.");
        }
      }
      catch (Exception ex) {
        String cName = ex.getClass().getName();
        form.append(EXCEPTION + cName + ": " + ex.getMessage());
      }
    }
    // getting der-encoded signature without byte array-content (detached)
    // FOR AUTHENTICATION PURPOSES
    else if (c == authByteDetachedCommand) {
      try {
        authByteDetached = null;
        byte[] byteDataToSign = stringToSign.getBytes();
        String[] caNames = null;
        String userPrompt = USER_PROMPT;
        form.append(AUTH_SIGN_MSG);
        form.append(AUTH_SIGN_MSG_BYTE_WITHOUT_CONTENT + "\"" + stringToSign + ".\"");
        authByteDetached = CMSMessageSignatureService.authenticate(
            byteDataToSign,
            CMSMessageSignatureService.SIG_INCLUDE_CERTIFICATE,
            caNames, userPrompt);
        if(authByteDetached != null) {
          form.append(AFTER_SIGN_MSG);
          form.append(AFTER_BYTE_WITHOUT_CONTENT + authByteDetached.length);
        }
        else {
          form.append(CANCEL);
        }
      }
      catch (SecurityException se){
        form.append("Too many incorrect pin entries.");
      }
      catch (CMSMessageSignatureServiceException cmsEx){
        form.append("Error occured during signature generation.");
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_CERTIFICATE){
          form.append("The certificate is not available.");
        }
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_DETACHED_SIG){
          form.append("Detached sigatures are not supported.");
        }

      }
      catch (UserCredentialManagerException ucmEx){
        if(ucmEx.getReason()== UserCredentialManagerException.SE_NOT_FOUND){
          form.append("A security element is not found.");
        }
      }
      catch (Exception ex) {
        String cName = ex.getClass().getName();
        form.append(EXCEPTION + cName + ": " + ex.getMessage());
      }
    }
    // getting der-encoded signature incl. String content (opaque)
    // FOR AUTHENTICATION PURPOSES
    else if (c == authStringOpaqueCommand) {
      try {
        authStringOpaque = null;
        String[] caNames = null;
        String userPrompt = USER_PROMPT;
        form.append(AUTH_SIGN_MSG);
        form.append(AUTH_SIGN_MSG_STRING_WITH_CONTENT + "\"" + stringToSign + ".\"");
        authStringOpaque = CMSMessageSignatureService.authenticate(
            stringToSign,
            CMSMessageSignatureService.SIG_INCLUDE_CERTIFICATE
            |CMSMessageSignatureService.SIG_INCLUDE_CONTENT,
            caNames, userPrompt);

        if(authStringOpaque != null) {
          form.append(AFTER_SIGN_MSG);
          form.append(AFTER_STRING_WITH_CONTENT + authStringOpaque.length);
        }
        else {
          form.append(CANCEL);
        }
      }
      catch (SecurityException se){
        form.append("Too many incorrect pin entries.");
      }
      catch (CMSMessageSignatureServiceException cmsEx){
        form.append("Error occured during signature generation.");
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_CERTIFICATE){
          form.append("The certificate is not available.");
        }
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_OPAQUE_SIG){
          form.append("Opaque signature is not supported.");
        }
      }
      catch (UserCredentialManagerException ucmEx){
        if(ucmEx.getReason()== UserCredentialManagerException.SE_NOT_FOUND){
          form.append("A security element is not found.");
        }
      }

      catch (Exception ex) {
        String cName = ex.getClass().getName();
        form.append(EXCEPTION + cName + ": " + ex.getMessage());
      }
    }
    // getting der-encoded signature without String content (detached)
    // FOR AUTHENTICATION PURPOSES
    else if (c == authStringDetachedCommand) {
      try {
        authStringDetached = null;
        String[] caNames = null;
        String userPrompt = USER_PROMPT;
        form.append(AUTH_SIGN_MSG);
        form.append(AUTH_SIGN_MSG_STRING_WITHOUT_CONTENT + "\"" + stringToSign + ".\"");
        authStringDetached = CMSMessageSignatureService.authenticate(
            stringToSign,
            CMSMessageSignatureService.SIG_INCLUDE_CERTIFICATE,
            caNames, userPrompt);

        if(authStringDetached != null) {
          form.append(AFTER_SIGN_MSG);
          form.append(AFTER_STRING_WITHOUT_CONTENT + authStringDetached.length);
        }
        else {
          form.append(CANCEL);
        }
      }
      catch (SecurityException se){
        form.append("Too many incorrect pin entries.");
      }
      catch (CMSMessageSignatureServiceException cmsEx){
        form.append("Error occured during signature generation.");
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_CERTIFICATE){
          form.append("The certificate is not available.");
        }
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_DETACHED_SIG){
          form.append("Detached sigatures are not supported.");
        }

      }
      catch (UserCredentialManagerException ucmEx){
        if(ucmEx.getReason()== UserCredentialManagerException.SE_NOT_FOUND){
          form.append("A security element is not found.");
        }
      }
      catch (Exception ex) {
        String cName = ex.getClass().getName();
        form.append(EXCEPTION + cName + ": " + ex.getMessage());
      }
    }

    // getting der-encoded signature incl. content (opaque)
    // FOR NON-REPUDIATION (authorization, signatures)
    else if (c == signOpaqueCmd) {
      try {
        signOpaque = null;
        String[] caNames = null;
        String userPrompt = USER_PROMPT;
        form.append(CMS_SIGN_MSG);
        form.append(CMS_SIGN_MSG_WITH_CONTENT + "\"" + stringToSign + ".\"");
        signOpaque = CMSMessageSignatureService.sign(
            stringToSign,
            CMSMessageSignatureService.SIG_INCLUDE_CERTIFICATE
            |CMSMessageSignatureService.SIG_INCLUDE_CONTENT,
            caNames, userPrompt);

        if(signOpaque != null) {
          form.append(AFTER_SIGN_MSG);
          form.append(AFTER_STRING + signOpaque.length);
        }
        else {
          form.append(CANCEL);
        }
      }
      catch (SecurityException se){
        form.append("Too many incorrect pin entries.");
      }
      catch (CMSMessageSignatureServiceException cmsEx){
        form.append("Error occured during signature generation.");
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_CERTIFICATE){
          form.append("The certificate is not available.");
        }
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_OPAQUE_SIG){
          form.append("Opaque signature is not supported.");
        }
      }
      catch (UserCredentialManagerException ucmEx){
        if(ucmEx.getReason()== UserCredentialManagerException.SE_NOT_FOUND){
          form.append("A security element is not found.");
        }
      }

      catch (Exception ex) {
        String cName = ex.getClass().getName();
        form.append(EXCEPTION + cName + ": " + ex.getMessage());
      }
    }
    // getting der-encoded signature without content (detached)
    // FOR NON-REPUDIATION (authorization, signatures)
    else if (c == signDetachedCmd) {
      try {
        signDetached = null;
        String[] caNames = null;
        String userPrompt = USER_PROMPT;
        form.append(CMS_SIGN_MSG);
        form.append(CMS_SIGN_MSG_WITHOUT_CONTENT + "\"" + stringToSign + ".\"");
        signDetached = CMSMessageSignatureService.sign(
            stringToSign,
            CMSMessageSignatureService.SIG_INCLUDE_CERTIFICATE,
            caNames, userPrompt);

        if(signDetached != null) {
          form.append(AFTER_SIGN_MSG);
          form.append(AFTER_STRING + signDetached.length);
        }
        else {
          form.append(CANCEL);
        }
      }
      catch (SecurityException se){
        form.append("Too many incorrect pin entries.");
      }
      catch (CMSMessageSignatureServiceException cmsEx){
        form.append("Error occured during signature generation.");
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_CERTIFICATE){
          form.append("The certificate is not available.");
        }
        if(cmsEx.getReason()== CMSMessageSignatureServiceException.CRYPTO_NO_DETACHED_SIG){
          form.append("Detached sigatures are not supported.");
        }

      }
      catch (UserCredentialManagerException ucmEx){
        if(ucmEx.getReason()== UserCredentialManagerException.SE_NOT_FOUND){
          form.append("A security element is not found.");
        }
      }
      catch (Exception ex) {
        String cName = ex.getClass().getName();
        form.append(EXCEPTION + cName + ": " + ex.getMessage());
      }
    }

    else if (c == clearCommand) {
      form.deleteAll();
      toBeSigned = new TextField(SIGN_LABEL, SIGN_CONTENT, SIGN_FILED_LENGTH, TextField.ANY);
      form.append(toBeSigned);
    }
  }
}

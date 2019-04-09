/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.rms.*;
import javax.microedition.lcdui.*;

/**
 * MessageStore manages all the message inventory in the RecordStore.
 */
class MessageStore
{

  private final SATSAMIDlet midlet;
  private RecordStore recordStore;
  private RecordEnumeration recordEnumeration;
  private int[] messageIdArray;
  private int numberOfMessages;

  /**
   * Creates a new MessageStore with an instance of RecordStore.
   * @param midlet the {@link SATSAMIDlet} object
   */
  MessageStore(SATSAMIDlet midlet)
  {
    this.midlet = midlet;
    openMessageStore();
  }

  private void openMessageStore()
  {
    try
    {
      recordStore = RecordStore.openRecordStore("SATSA_test", true);
    }
    catch (RecordStoreException rse)
    {
      midlet.showError("Record Store Exception. " + rse.getMessage());
    }
  }

  /**
   * Closes the record store.
   */
  void close()
  {
    try
    {
      recordStore.closeRecordStore();
    }
    catch (RecordStoreException rse)
    {
      // Ignore, since the application is closing anyway.
    }
  }

  /**
   * List all messages in RecordStore.
   * @param list The list to store records
   */
  void fillList(List list)
  {
    list.deleteAll();

    try
    {
      numberOfMessages = recordStore.getNumRecords();
      messageIdArray = new int[numberOfMessages+1];

      recordEnumeration = recordStore.enumerateRecords(null, null, false);
      int i=0;

      while (recordEnumeration.hasNextElement())
      {
        i++;
        int id=recordEnumeration.nextRecordId();

        list.append("Message " + id, null);
        messageIdArray[i]=id;
      }
    }
    catch (RecordStoreException rse)
    {
      midlet.showError("Record Store Exception. " + rse.getMessage());
    }
  }

  /**
   * Add a record to RecordStore.
   * @param messageData The actual message stored in byte format
   */
  void addMessage(byte[] messageData)
  {
    try
    {
      recordStore.addRecord(messageData, 0, messageData.length);
    }
    catch (RecordStoreException rse)
    {
      midlet.showError("Record Store Exception. " + rse.getMessage());
    }
  }

  /**
   * Get message from Record Store
   * @param index The key to identify each record
   * @return byte[] The message retrived in byte format
   */
  byte[] getMessage(int index)
  {
    try
    {
      byte[] messageData = recordStore.getRecord(messageIdArray[index]);
      return messageData;
    }
    catch (RecordStoreException rse)
    {
      midlet.showError("Record Store Exception. " + rse.getMessage());
      return null;
    }
  }

  /**
   * Delete a record from Record Store
   * @param index The index number for each message
   */
  void deleteMessage(int index)
  {
    try
    {
      recordStore.deleteRecord(messageIdArray[index]);
    }
    catch (RecordStoreException rse)
    {
      midlet.showError("Record Store Exception. " + rse.getMessage());
    }
  }
}

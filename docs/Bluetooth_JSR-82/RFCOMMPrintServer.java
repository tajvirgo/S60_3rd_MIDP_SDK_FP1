import java.lang.*;
import java.io.*;
import javax.microedition.io.*;
import javax.bluetooth.*;

/**
 * This class will start an RFCOMM service that will accept data and print it
 * to standard out.
 */
public class RFCOMMPrintServer {

    /**
     * This is the main method of the RFCOMM Print Server application.  It will
     * accept connections and print the data received to standard out.
     *
     * @param args the arguments provided to the application on the command
     * line
     */
    public static void main(String[] args) {
        StreamConnectionNotifier server = null;
        String message = "";
        byte[] data = new byte[20];
        int length;

        try {
            LocalDevice local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.GIAC);
        } catch (BluetoothStateException e) {
            System.err.println("Failed to start service");
            System.err.println("BluetoothStateException: " + e.getMessage());
            return;
        }

        try {
            server = (StreamConnectionNotifier)Connector.open(
                "btspp://localhost:11111111111111111111111111111111");
        } catch (IOException e) {
            System.err.println("Failed to start service");
            System.err.println("IOException: " + e.getMessage());
            return;
        }

        while (!(message.equals("Stop Server"))) {

            message = "";
            StreamConnection conn = null;

            try {
                try {
                    conn = server.acceptAndOpen();
                } catch (IOException e) {
                    System.err.println("IOException: " + e.getMessage());
                    return;
                }

                InputStream in = conn.openInputStream();

                length = in.read(data);
                while (length != -1) {
                    message += new String(data, 0, length);
System.out.println("Message = " + message);

                    try {
                        length = in.read(data);
                    } catch (IOException e) {
                        break;
                    }
System.out.println("Length = " + length);
                }

                System.out.println(message);

                in.close();

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        try {
            server.close();
        } catch (IOException e) {

        }
    }

}

import java.lang.*;
import java.io.*;
import javax.microedition.io.*;
import javax.bluetooth.*;

/**
 * This class will start an L2CAP service that will accept data and print it
 * to standard out.
 */
public class L2CAPPrintServer {

    /**
     * This is the main method of the L2CAP Print Server application.  It will
     * accept connections and print the data received to standard out.
     *
     * @param args the arguments provided to the application on the command
     * line
     */
    public static void main(String[] args) {
        L2CAPConnectionNotifier server = null;
        String message = "";
        byte[] data = null;
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
            server = (L2CAPConnectionNotifier)Connector.open(
                "btl2cap://localhost:1020304050d0708093a1b121d1e1f100");
        } catch (IOException e) {
            System.err.println("Failed to start service");
            System.err.println("IOException: " + e.getMessage());
            return;
        }

        System.out.println("Starting L2CAP Print Server");

        while (!(message.equals("Stop Server"))) {

            message = "";
            L2CAPConnection conn = null;

            try {
                conn = server.acceptAndOpen();

                length = conn.getReceiveMTU();
                data = new byte[length];

                length = conn.receive(data);

                while (length != -1) {
                    message += new String(data, 0, length);

                    try {
                        length = conn.receive(data);
                    } catch (IOException e) {
                        break;
                    }
                }

                System.out.println(message);

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

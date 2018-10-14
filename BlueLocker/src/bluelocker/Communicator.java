/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluelocker;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Vector;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.JOptionPane;

public class Communicator implements DiscoveryListener
{
    //object used for waiting
    private static final Object lock=new Object();

    //vector containing the devices discovered
    private static final Vector vecDevices=new Vector();

    private static String connectionURL=null;

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        //add the device to the vector
        if(!vecDevices.contains(btDevice)){
            vecDevices.addElement(btDevice);
            System.out.println(btDevice.getBluetoothAddress());
        }
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if(servRecord!=null && servRecord.length>0){
            connectionURL=servRecord[0].getConnectionURL(0,false);
        }
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void inquiryCompleted(int discType) {
        synchronized(lock){
            lock.notify();
        }
    }
    private BlueLocker parent;
    
    public boolean Connect(BlueLocker Parent) 
    {
        this.parent = Parent;
        // retrieve the local Bluetooth device object
        LocalDevice local = null;

        StreamConnectionNotifier notifier;
        StreamConnection connection = null;

        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            System.out.println(local.getBluetoothAddress());
            local.setDiscoverable(DiscoveryAgent.GIAC);

            UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
            notifier = (StreamConnectionNotifier)Connector.open(url);
        } catch (Exception e) {
            return false;
        }
                // waiting for connection
        while(true) {
            try {
                System.out.println("waiting for connection...");
                        connection = notifier.acceptAndOpen();

                Thread processThread = new Thread(new ProcessConnectionThread(connection));
                processThread.start();
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }
    
    public class ProcessConnectionThread implements Runnable {

        private final StreamConnection mConnection;

        // Constant that indicate command from devices

        public ProcessConnectionThread(StreamConnection connection)
        {
            mConnection = connection;
        }

        @Override
        public void run() {
            try {
                // prepare to receive data
                InputStream inputStream = mConnection.openInputStream();

                System.out.println("waiting for input");

                while (true) {
                    byte[] b = new byte[400]; 
                    int i = inputStream.read(b);
                    if (i != 0)
                    {
                        processCommand(b, i);
                        return;
                    }
                }
            } catch (Exception e) {
            }
        }

        /**
         * Process the command from client
         * @param command the command code
         */
        private void processCommand(byte[] command, int i) {
            byte[] newvals = new byte[i];
            for (int j = 0; j < i; j++)
            {
                newvals[j] = command[j];
            }
            PublicKey publicKey;
            try {
                X509EncodedKeySpec spec = new X509EncodedKeySpec(newvals);
                publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            } catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            JOptionPane.showMessageDialog(null, "Your RSA Key Starts With: " + newvals[0] + ", " + newvals[1] + ", " + newvals[2] + ", " + newvals[3] + ", " + newvals[4] + ", " + newvals[5] + ", " + newvals[6]);
            if (parent.Compress() == 0 && parent.Encrypt("password") == 0)
                JOptionPane.showMessageDialog(parent, "Encrypted Successfully");
        }
    }
}
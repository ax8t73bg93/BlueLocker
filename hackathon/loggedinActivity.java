package jbhunt.hackathon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

public class loggedinActivity extends AppCompatActivity {

    private Properties p;
    private byte[] pubkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        p = new Properties();
        p.putAll((HashMap) intent.getExtras().get("properties"));

        if(!intent.getBooleanExtra("isCurrent", false));
        {
            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                KeyPair kp = gen.genKeyPair();

                pubkey = kp.getPublic().getEncoded();

            } catch (NoSuchAlgorithmException e) {

            }
        }

        Button pair = (Button) findViewById(R.id.button);

        pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click();
            }
        });
    }

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private void Click()
    {
        ConnectThread acceptThread = new ConnectThread(mBluetoothAdapter.getRemoteDevice("00:02:72:C5:68:8E"));
        acceptThread.run();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb"));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
            OutputStream writer;
            try {
                writer = mmSocket.getOutputStream();
            } catch (IOException e) {
                return;
            }

            try {
                System.out.println(Arrays.toString(pubkey));
                writer.write(pubkey);
                TextView view = (TextView) findViewById(R.id.textView);
                view.setText("Your RSA Key starts with: " + pubkey[0] + "," + pubkey[1] + ","+ pubkey[2] + ","+ pubkey[3] + ","+ pubkey[4] + ","+ pubkey[5] + ","+ pubkey[6]);

            } catch (IOException e) {

            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}

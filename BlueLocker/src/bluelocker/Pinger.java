/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluelocker;

import javax.swing.JOptionPane;

/**
 *
 * @author Jtdav
 */
public class Pinger implements Runnable
{
    private Thread t;
    private final int SLEEPTIME = 3000;
    private final BlueLocker parent;
    private Communicator comms;
    
    public Pinger(BlueLocker parent)
    {
        t = null;
        this.parent = parent;
    }
    
    @Override
    public void run() 
    {
        Communicator comms = new Communicator();

        comms.Connect(parent);

        System.out.println("Thread exiting.");
    }

    void start() 
    {
        if (t == null)
        {
            t = new Thread(this, "pinger");
            t.start();
        }
    }
}

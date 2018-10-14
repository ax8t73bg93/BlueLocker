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
public class Locker implements Runnable{

    private final BlueLocker parent;
    private Thread t;
    
    public Locker(BlueLocker parent)
    {
        t = null;
        this.parent = parent;
    }
    
    @Override
    public void run() 
    {
        try {
            while(parent.getConnected()) 
            {
                System.out.println("Connected..."); 
                synchronized(parent)
                {
                    parent.wait();
                }
            }
            System.out.println("No longer connected.");
           
            if (parent.Decrypt("password") == 0 && parent.Decompress() == 0)
            {
                
            }
            else 
            {
                JOptionPane.showMessageDialog(parent, "Exited to save data.. there was an error.");
                parent.dispose();
            }
           
       } catch (InterruptedException e) 
       {
           System.out.println("Thread interrupted.");
       }
       System.out.println("Thread exiting.");
    }
    
    void start() 
    {
        if (t == null)
        {
            t = new Thread(this, "locker");
            t.start();
        }
    }
}

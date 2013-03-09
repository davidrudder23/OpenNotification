package net.reliableresponse.notification.smtp;

import java.io.BufferedReader;

public class MyTimeout extends Thread
{
   BufferedReader in;   
   String strTheLine;
   boolean bStilRunning;

   public MyTimeout(BufferedReader pin)
   {
      in = pin;
      strTheLine = null;
      bStilRunning = true;
   }

   public void run()
   {
      try
      {
         strTheLine = in.readLine();
      }
      catch(Exception e)
      {
         // something happend
      }
      bStilRunning = false;
      // keep running forever.  the caller will stop this thread
      while(true)
      {
         Thread.yield();
      }
   }

   public boolean StillRunning()
   {
      return bStilRunning;
   }
   
   public String TheLine()
   {
      return strTheLine;
   }

   
   // read a line, return the line or null if times out
   // does the read in a different thread
   // timeout is in miliseconds
   public static String TimedReadLine(BufferedReader in, long timeout)
   {
      MyTimeout mt;
      String strTheLine = null; 
      int i;
      long lngCurrentTime = 0;
      long lngEndTime = 0;

      mt = new MyTimeout(in);
      mt.setDaemon(true);
      mt.start();

      lngCurrentTime = System.currentTimeMillis();
      lngEndTime = lngCurrentTime + timeout;
      
      // wait for a given period of time for thread to quit
      while (lngCurrentTime <= lngEndTime)
      {
         try
         {
            // let other threads run for a while
            Thread.yield();
            lngCurrentTime = System.currentTimeMillis();
         }
         catch(Exception e)
         {
            // someone tried to stop us.  oh well ignore it :)
         }
         if (mt.StillRunning() == false)
         {
            break;
         }
      }

/*
      for (i = 0; i <= timeout; i += 3000)
      {
         try
         {
            Thread.sleep(3000);      
         }
         catch(Exception e)
         {
            // someone tried to stop up.  oh well ignore it :)
         }
         if (mt.StillRunning() == false)
         {
            break;
         }
      }
*/
      // terminate if still running
      if (mt.StillRunning() == true)
      {
         mt.stop();
      }
      else
      {
         // not still running so grab the line, 
         strTheLine = mt.TheLine();
         mt.stop();
      }

      return strTheLine;
   }
}


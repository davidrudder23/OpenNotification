// an interface for mail handlers - any kind of handler could exist

package net.reliableresponse.notification.smtp;


public interface MailHandler
{ 
   // can this handler send mail to this user?
   // if this returns true then assume that start will be called next
   // - although this may never happen, get ready just in case - 
   public boolean CheckToUser(String strUser);
   // can this handler recieve mail from this user?
   // if this returns true then assume that CheckToUser will be called next
   // - although this may never happen, get ready just in case - 
   public boolean CheckFromUser(String strUser);
   // start a new messasge
   public boolean Start();
   // called for every line of the message
   public void Line(String strLine);
   // end the message
   public void End();
   // abort the message
   public void Abort();
   // a message to mail after the current client hangs up
   public void ConnectionClosed(boolean bCleanExit);
}


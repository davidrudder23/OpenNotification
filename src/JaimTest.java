/*
 *   (C) 2002 Paul Wilkinson  wilko@users.sourceforge.net
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

/*
 * JaimTest.java
 *
 * Created on 3 May 2002, 12:26
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.levelonelabs.aim.AIMBuddy;
import com.levelonelabs.aim.AIMClient;

/**
 *
 * @author  paulw
 * @version $Revision: 1.2 $
 */
public class JaimTest {
	
    public static void main(String args[]) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: JaimTest <username> <password>");
        }
        
        // Make a new client
        AIMClient client = new AIMClient(args[0], args[1]);
        
        
        // Start the thread
        Thread thread = new Thread(client);
        thread.setDaemon(true);
        thread.start();
        
        // Add a buddy (drig23 is me, Dave Rudder)
        AIMBuddy buddy = client.getBuddy("drig23");
        client.addBuddy(buddy);
        
        // Send me a nice message
        client.sendMessage(buddy, "Hi Dave, how ya' doin?");
        
        // Wait until the user hits the enter button
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }
}

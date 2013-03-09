package net.reliableresponse.notification.aim;

/**
  * The JavaTOC framework is a set of classes used to allow
  * a Java program to communicate with AOL's TOC protocol.
  * The Chatable interface and JavaTOC classes can easily
  * be moved to any program needing TOC abilities.
  *
  *  Copyright 2002 by Jeff Heaton
  *
  *   This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * @author Jeff Heaton(http://www.jeffheaton.com)
  * @version 1.0
  */


public interface Chatable {

   /**
    * Called when an unknown TOC event occurs
    *
    * @param str The TOC event
    */
   public void unknown(String str);

   /**
    * Called when a TOC error occurs
    *
    * @param str The TOC error
    * @param var An optional vailable to insert into the error message
    */
   public void error(String str,String var);

   /**
    * Called when an IM is received
    *
    * @param from Who is the IM from
    * @param message What is the message
    */
   public void im(String from,String message);
}
#!/bin/sh
clear

# Welcome banner

echo
echo Welcome to Reliable Response Notification
echo HP OpenView Network Node Manager Integration
echo --------------------------------------------
echo Please press enter to continue
read eof

# License

more <<"EOF"
Reliable Response, LLC
License Agreement

1.  LICENSE TO USE.  
Reliable Response grants you a non-exclusive and non-transferable license for the internal use only of the accompanying software and documentation and any error corrections provided by Reliable Response (collectively "Software"), by the number of users and the class of computer hardware for which the corresponding fee has been paid.

2.  RESTRICTIONS.  
Software is confidential and copyrighted. Title to Software and all associated intellectual property rights is retained by Reliable Response and/or its licensors. Except as specifically authorized in any Supplemental License Terms, you may not make copies of Software, other than a single copy of Software for archival purposes.  Unless enforcement is prohibited by applicable law, you may not modify, decompile, or reverse engineer Software. Reliable Response, LLC. disclaims any express or implied warranty of fitness for such uses.  No right, title or interest in or to any trademark, service mark, logo or trade name of Reliable Response or its licensors is granted under this Agreement.

3.  LIMITED WARRANTY.  
Reliable Response warrants to you that for a period of ninety (90) days from the date of purchase, as evidenced by a copy of the receipt, the media on which Software is furnished (if any) will be free of defects in materials and workmanship under normal use.  Except for the foregoing, Software is provided "AS IS".  Your exclusive remedy and Reliable Response's entire liability under this limited warranty will be at Reliable Response's option to replace Software media or refund the fee paid for Software.

4.  DISCLAIMER OF WARRANTY.  
UNLESS SPECIFIED IN THIS AGREEMENT, ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT ARE DISCLAIMED, EXCEPT TO THE EXTENT THAT THESE DISCLAIMERS ARE HELD TO BE LEGALLY INVALID.

5.  LIMITATION OF LIABILITY.  
TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL RELIABLE RESPONSE OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF RELIABLE RESPONSE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  In no event will Reliable Response's liability to you, whether in contract, tort (including negligence), or otherwise, exceed the amount paid by you for Software under this Agreement.  The foregoing limitations will apply even if the above stated warranty fails of its essential purpose.

6.  Termination.  
This Agreement is effective until terminated.  You may terminate this Agreement at any time by destroying all copies of Software.  This Agreement will terminate immediately without notice from Reliable Response if you fail to comply with any provision of this
Agreement.  Upon Termination, you must destroy all copies of Software.

9.  Governing Law.  
Any action related to this Agreement will be governed by Colorado law and controlling U.S. federal law.  No choice of law rules of any jurisdiction will apply.

10. Severability. 
If any provision of this Agreement is held to be unenforceable, this Agreement will remain in effect with the provision omitted, unless omission would frustrate the intent of the parties, in which case this Agreement will immediately terminate.

11. Integration.  
This Agreement is the entire agreement between you and Reliable Response relating to its subject matter.  It supersedes all prior or contemporaneous oral or written communications, proposals, representations and warranties and prevails over any conflicting or additional terms of any quote, order, acknowledgment, or other communication between the parties relating to its subject matter during the term of this Agreement.  No modification of this Agreement will be binding, unless in writing and
signed by an authorized representative of each party.

For inquiries please contact: 
Reliable Response, LLC
1600 Broadway, Suite 2400
Denver, Colorado 80202
EOF

# make sure the user agrees

agreed=
while [ x$agreed = x ]; do
    echo
    echo "Do you agree to the above license terms? [yes or no] "
    read reply leftover
    case $reply in
        y* | Y*)
            agreed=1;;
        n* | N*)
    echo "If you don't agree to the license you can't install this software";
    exit 1;;
    esac
done

# Find the path to OpenView
OVPATH=""
if [ -d /opt/OV ]
then
	OVPATH="/opt/OV"
elif [ -d /usr/local/OV ]
then
	OVPATH="/usr/local/OV"
fi
echo -n "Location of OpenView Network Node Manager? [$OVPATH] "
read INPUT
if [ x$INPUT != x ]
then
	OVPATH=$INPUT
fi

while [ ! -d $OVPATH ]
do
	echo -n "Location of OpenView Network Node Manager? [$OVPATH] "
	read INPUT
	if [ x$INPUT != x ]
	then
		OVPATH=$INPUT
	fi
done

# Install the registration file
echo Installing /etc$OVPATH/share/registration/C/reliableresponsenotification
cp rrn_registrationfile /etc$OVPATH/share/registration/C/reliableresponsenotification

# Install the fields file
echo "Installing /etc$OVPATH/share/fields/C/reliableresponsenotification"
cp rrn_fields /etc$OVPATH/share/fields/C/reliableresponsenotification 

# Load the action and the changes to the events
echo Installing the changes to trapd.conf
$OVPATH/bin/xnmevents -load add_action
$OVPATH/bin/xnmevents -merge add_exec_to_event

# Done!
echo Thank you for installing the Reliable Response Notification plugin for
echo HP OpenView Network Node Manager
echo
echo Please restart OpenView Network Node Manager to see the changes take effect


<jsp:include page="blank_header.jsp" />

<h1>Notifications Sent To Me</h1>

<p>
This section explains the purpose of the section titled "Notifications Sent To Me"</p>
<h2>Using The Tab</h2>
<p>
<img src="images/pendingNotificationsTab.gif">
</p>
<p>
The "tab" (the rectangular grey region at the top of the "pending notifications" 
section) contains some useful controls.  The four colored buttons show how many 
active (ie, not expired or confirmed), confirmed or expired notifications are
in the list.  Clicking the colored box toggles display of these notifications 
on or off.  The final field allows you to increase or decrease the amount of time
that the system will search through to find the notifications to display
<p>
<a name="read"><h2><font color="#000000">Reading your Notifications</font></h2></a>
<p>Every notification has 3 sections; a quick summary, a list of messages, 
and a list of people that the messages were sent to.  </p>
<p>
The summary includes the subject of the notification, the person who sent it, 
the time it was sent, and the notification ID.  The notification ID is useful 
for confirming notifications via the dial-in automated system.</p>

<p>
The recipient list contains all the people who this notification was sent to.  
If it was sent to an escalation group, you will see all the members of the group 
in the list.  The person whom the notification is currently waiting for will 
be noted with asterix around their name.  If a notification has been sent to 
an escalation group which you are a member of, you may see that notification 
even if it has not reached you yet.</p>

<a name="confirm"><h2><font color="#000000">Confirm your Notifications</font></h2></a>
<p>
When you confirm a message, you are telling Reliable Response Notification&#8482; 
that you have received the message and you are responding to it.  If this message 
was sent to an escalation group, it will no longer escalate up the chain.  
</p>

<a name="pass"><h2><font color="#000000">Pass your notifications</font></h2></a>
<p>
If the notification was sent to an escalation group, you can pass the notification.  
This means that the notification will go to the next recipient without waiting for 
the specified time first.  This is mostly useful when you know that this notification 
doesn't apply to you, and you want it handled as quickly as possible.  
</p>

<a name="forward"><h2><font color="#000000">Forward your notifications</font></h2></a>
<p>
If you want to send this notification to another user of the system, use the forward 
action.  Forwarding a notification is identical to simply sending a new notification 
with the same subject and messages, but is usually considerably easier.  Forwarding 
does not confirm or erase the notification that you are forwarding.
</p>

<jsp:include page="footer.jsp" />
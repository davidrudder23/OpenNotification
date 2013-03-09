<jsp:include page="blank_header.jsp" />
<h1>Group Settings</h1>
<p>
This section explains the purpose of the section titled "Group Settings"</p>
<h2>Using The Tab</h2>
<p>
<img src="images/groupSettingsTab.gif">
</p>
<p>
The "tab" (the rectangular grey region at the top of the "pending notifications" 
section) shows you a quick list of how many groups you have. It lists escalation
and broadcast groups separately.
</p>
<h2>Group Summaries</h2>
<p>
<img src="images/groupSummaries.gif">
</p>
<p>
Each group you are a member of is displayed at the top of this section.  The 
group summary shows the group's name, the number of members, the group type,
and finally the priory of the group.  For more information on prorities, look
at the <a href="priorities.jsp">help section on priorities</a>.  You may use
the group summary to set the group's priorty by clicking on the check boxes
on the right.
</p>
<h3>Editing Groups</h3>
<p>
<img src="images/editGroup.gif">
</p>
<p>
When you open the summary, the group name changes from plain text to an edit
box which you can use to change the group's name.  Below it, you can see an
edit box with the group's description, a list of the group members, and buttons
you can use to add and delete users.  If the group is an escalation group, 
you can also see the amount of time before the escalation moves to the next
member.  To set the escalation time, select the user you are editing, and
change the time in the text area below.
</p>
<h3>Adding Members</h3>
<p>
<img src="images/searchAddGroup.gif">
</p>
<p>
You can add members to a group by clicking the "add" button in the group editing 
screen or opening the "add new recipient(s)" tab.  The search box that is displayed 
works identically to the search box in the <a href="sendNotification.jsp">section
titled "Sending A New Notification"</a>.  When you click on the "search", the 
system will find all users which match the supplied substring.  Checking a
checkbox next to the users and clicking "add selected" will add the users to the
group.  You can see the <a href="sendNotification.jsp">section titled "Sending A 
New Notification"</a> for more information.
</p>
<h2>Adding Yourself To An Existing Group</h2>
<h3>Searching For Groups</h3>
<p>
<img src="images/addToExistingGroup.gif">
</p>
<p>
You can add yourself to any existing group by using this section.  When opening 
the tab, you will see a search area much like the search areas in the 
<a href="sendNotification.jsp">section titled "Sending A New Notification"</a>.  
This area will only find groups.  You can add yourself to an existing group by
searching for the group, checking the checkbox to the left of the group, and 
clicking "add selected".
</p>
<h3>Selecting Groups From a List</h3>
<p>
<img src="images/addGroupFromList.gif">
</p>
<p>
You can also add yourself to any existing group by using this section.  When 
opening the tab, you will see an area which lists all the available groups.  
You can select any number of groups and click "add selected" to add yourself
to those groups.
</p>
<jsp:include page="footer.jsp" />

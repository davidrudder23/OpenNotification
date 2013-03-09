<jsp:include page="blank_header.jsp" />
<h1>Editing a User</h1>
<p>
This section explains how to edit a user.
</p>
<p>
<img src="images/editUser.gif">
</p>
<h2>Selecting The User</h2>
<p>
You can choose which user to edit by selecting the user in the box
titled "Select User".  Selecting the user changes the rest of the fields
to show the user's information.  If you have made any changes, these 
changes are not saved.
</p>
<h2>The User's Information</h2>
<p>
You may change the user's first name, last name, department, and set a 
new password.  This information does not affect how the user receives 
notifications, but changes the display of the user's information on the 
website and in the logs and reports.  Once you have changed this 
information, click "save" to save it.
</p>
<h2>The User's Work Hours</h2>
<p>
When editing the user's groups and devices, you may configure how the system uses
devices.  One of your options to is tell the system to not use a device when
the user is out of the office.  The "Work Hours" settings allow you to define 
what the user's work hours are, so the system knows when to contact the user.  You 
may look at the <a href="priorities.jsp">priorities section</a> for more information
on how work hours affects how notifications are received.
</p>
<h2>Administrator</h2>
<p>
Users which have the Administrator box checked have special rights.  Administrators
can add users, run jobs, via complete logs, and edit users.  Be careful when using 
this field.  If you leave no administrators, you will not be able to do any 
administrative tasks.
</p>
<h2>Keep In Cache</h2>
<p>
Reliable Response Notification&#8482; relies on an external database to store user 
and group information.  If the database fails, Notification's&#8482; ability to 
send notifications suffers.  By marking users with this field, you can allow
notifications to these users to succeed.  However, every user marked this way 
increases the RAM memory requirements of the server, so use it sparingly.  The
basic rule to follow is to check everyone who can respond to system outages and no 
one else.
</p>
<jsp:include page="footer.jsp" />
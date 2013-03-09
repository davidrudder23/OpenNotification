<jsp:include page="blank_header.jsp" />
<h1>Priorities</h1>
<p>Reliable Response Notification&#8482; routes notifications primarily 
by which group they are sent to.  In theory, a group should map more
or less directly to which system the members are responsible for, or 
the nature of the outage.  For instance, a group titled "Web Servers"
would include those people responsible for the web servers and will
deal primarily with notifications about problems with those web servers.
<p>
<p>
Some groups may be of greater or lesser importance than others.  The 
"Web Servers" group is probably very important, but the "Disk Usage" or 
"Community Forums Servers" group may be less important.  You can define
the importance by settings the groups <em>priorities</em>.
<p>
<h2>Setting the Priorities</h2>
<p>
<img src="images/groupSummaries.gif">
</p>
<p>
The groups priorities are set in the <a href="groupSettings.jsp">Group
Settings</a> area within the My Settings page.  Every group can be 
assigned a priority from 1 (the highest) to 3 (the lowest).  Every member
of the group may set a different priority.  You can manipulate the 
priorities by clicking the checkboxes in the <a href="groupSettings.jsp">
Group Settings</a> tab.
</p>
<h2>Defining the Devices Used</h2>
<p>
Setting a priority will change which devices will be used when you 
receive a notification that was sent to that group.  For instance, if 
you set the "Community Forums Servers" group to be priority 3, you may
not wish to have notifications that pertain to the forums sent to your
pager.  Receiving an email may be sufficient.  You can do this via the 
<a href="deviceSettings.jsp">Device Settings</a> section within the My
Settings page.
</p>
<p>
<img src="images/editDevice.gif">
</p>
<p>
When setting the device's priorities, you are setting when <em>not</em>
to use a particular device.  In the above example, you may want to choose
"Never Use" for your pager's priority 3.  
</p>
<p>
The other choices depend on the input from external sources.  "Off Hours" 
is defined by your work hours in the <a href="personalSettings.jsp">Personal
Settings</a> section.  "Vacation", "Out of Office", and "In A Meeting" are 
defined by your calendar and are only available if your system is 
configured to use an external calendaring program like Microsoft Exchange.
</p>
</p>
<jsp:include page="footer.jsp" />
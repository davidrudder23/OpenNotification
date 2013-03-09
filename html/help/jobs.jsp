<jsp:include page="blank_header.jsp" />
<h1>Managing Recurring Jobs</h1>
<p>
<img src="images/jobs.gif">
</p>
<p>
This section allows you to managing recurring jobs, like
syncing with a directory or retrieving mail from a POP
server.
</p>
<h2>Jobs List</h2>
<p>
At the top of this section is a list of the existing jobs.
Each job allows you to start it manually by clicking the 
"-->Run Now" link.  This is useful if you need the job to
run immediately, for example if there was a change to your
directory that you'd like to see reflected in Reliable
Response Notification&#8482; immediately.
</p>
<p>
By opening the job's tab, you can see a history of when 
every job was run, and whether the job succeeded or not.  If
a job is failing unexpectedly, you may wish to check the 
log files of your application server for more information.
</p>
<h2>Currently Running Jobs</h2>
<p>
This section shows you any jobs that are currently running 
and allows you to stop them.  
</p>
<jsp:include page="footer.jsp" />
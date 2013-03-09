<!--  this is the javascript needed for submitting the send -->
<script language="JavaScript">
var subjectNeedsErasing = true;
var messageNeedsErasing = true;

function checkSubjectErasing() {
	if (subjectNeedsErasing) {
		document.newnotif.subject.value="";
	}
	subjectNeedsErasing = false;
}

function checkMessageErasing() {
	if (messageNeedsErasing) {
		document.newnotif.newmessage.value="";
	}
	messageNeedsErasing = false;
}

function sendNewNotification() {
	var subject = document.newnotif.subject.value;
	var message = document.newnotif.newmessage.value;
	
    http.open('post', 'ajax/sendNotification.jsp?'+Math.random());
    http.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    http.onreadystatechange = receiveUserMessage;
    var args ='subject='+escape(subject)+'&message='+escape(message); 
	http.send(args);
	
	return false;
}
</script>
<!-- send a new notif -->
      <table width="284"  border="0" cellpadding="0" cellspacing="0" class="box">
        <tr>
          <td bgcolor="#EDEDED" class="header_box"> Send
            New Notification</td>
          <td bgcolor="#EDEDED" class="header_box"><img src="images/spacer.gif" width="1" height="20"></td>
        </tr>
        <tr>
          <td colspan="2">
            
			<table width="100%"  border="0" cellspacing="3" cellpadding="0">
            <tr><form name="newnotif" id="newnotif" method="post" action="#">
              <td valign="top">
                    <input name="subject" type="text" id="subject" value="subject" size="25" onfocus="checkSubjectErasing();">
                    <br>
                    <br>
                    <textarea name="newmessage" cols="25" rows="4" id="newmessage" onfocus="checkMessageErasing();">your message here</textarea>
                    <br>
                    <br>                    
                  <!-- <img src="images/btn_send.gif" alt="send" onclick="sendNewNotification();" style="cursor: pointer;">
                  -->
                  <span onclick="sendNewNotification();" class="button">&nbsp;Send&nbsp;</span><br><br></td>
              </form></tr>
          </table>          </td>
        </tr>
    </table>    
    
<!-- end send a new section -->

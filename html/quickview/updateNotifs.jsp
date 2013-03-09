<!-- <body onload="updateNotifs();">
<link href="../rr.css" rel="stylesheet" type="text/css"> -->
<script>
var notifications = {};

function initNotifs() {
    activeNotifsHttp.open('get', 'ajax/updateNotifs.jsp?clearSession=Y&'+Math.random());
    activeNotifsHttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    activeNotifsHttp.onreadystatechange = handleUpdateNotifs;
	activeNotifsHttp.send(null);
	
	setTimeout ("doNotifLoop()", 500);
	return false;
}

function doNotifLoop() {
	try {
		updateNotifs();
	} catch (e) {
		
	}
	setTimeout ("doNotifLoop()", 10000);	
	
	return true;
}

function updateNotifs() {

    activeNotifsHttp.open('get', 'ajax/updateNotifs.jsp?'+Math.random());
    activeNotifsHttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    activeNotifsHttp.onreadystatechange = handleUpdateNotifs;
	activeNotifsHttp.send(null);
	
	return false;
}

function handleUpdateNotifs() {
	//document.getElementById('debug').innerHTML = "State: "+activeNotifsHttp.readyState+"\nStatus: "+activeNotifsHttp.status;
try {
	if( (activeNotifsHttp.readyState == 4) && (activeNotifsHttp.status == 200)){ //Finished loading the response
		/* We have got the response from the server-side script,
			let's see just what it was. using the responseText property of 
			the XMLHttpRequest object. */
		
		var debugDiv = document.getElementById('debug');
		debugDiv.innerHTML = "<pre>"+activeNotifsHttp.responseText+"</pre>";
		debugDiv = null;
		
		var response = activeNotifsHttp.responseXML.documentElement;
		var xmlNotifications = response.getElementsByTagName("notification");
		
		if (xmlNotifications.length>0) {
		//alert (activeNotifsHttp.responseText);
		for (var i = 0; i < xmlNotifications.length; i++) {
			var uuid    = xmlNotifications[i].getAttribute("uuid");
			var status  = xmlNotifications[i].getAttribute("status");
			var date  = xmlNotifications[i].getAttribute("date");
			var subject = "";
			var subjectNode = xmlNotifications[i].getElementsByTagName("subject");
			if ((subjectNode.length>0) && (subjectNode[0].firstChild)) {
				subject = subjectNode[0].firstChild.data;
			}
			var message = "";
			var messageNode = xmlNotifications[i].getElementsByTagName("message");
			if ((messageNode.length>0) && (messageNode[0].firstChild)) {
				message = messageNode[0].firstChild.data;
			}
			
			notifications[uuid] = new Object();
			notifications[uuid].status  = status;
			notifications[uuid].subject = subject
			notifications[uuid].message = message;
			notifications[uuid].date = date;
		}
		
		response = null;
		
		style="list";

		// Count the notifs
		var numActive = 0;
		var numConfirmed = 0;
		var numExpired = 0;
		var numOnhold = 0;
		for (var i in notifications) {
			if (notifications[i].status == "active") {
				numActive++;
			} else if (notifications[i].status == "confirmed") {
				numConfirmed++;
			} else if (notifications[i].status == "expired") {
				numExpired++;
			} else if (notifications[i].status == "onhold") {
				numOnhold++;
			} 
		}
		
		if (numActive > 0) {
			document.title = numActive+" active - Notification";
		} else {
			document.title = "Notification";
		}
		
		// Get the sorted keys
		var uuids = new Array();
		var count = 0;
		for (var i in notifications) {
			uuids[count++] = i;
		}
		uuids.sort().reverse();
		        
		var activeText = "<div class=\"list\" >\n"+
			"<div align=\"right\">\n"+
			"<a href=\"#\" onClick=\"MM_openBrWindow('notification_list.jsp?list=active','pop','scrollbars=yes,resizable=yes,width=400,height=600')\">view/print all</a>\n"+
			"</div>\n"+
			"</div>\n";
			
		var numActive = 0;			
		// Update the active area
		for (var u = 0; u < uuids.length; u++) {
			var i = uuids[u];
			if (notifications[i].status == "active") {
				numActive++;
				var url = "notification.jsp?browse=active&uuid="+i;
			
				if (style == "list") {
					style = "shadedbg list";
				} else {
					style = "list";
				}
				var statusColor="green";
				if (notifications[i].status == "confirmed") {
					statusColor="yellow";
				} else if (notifications[i].status == "expired") {
					statusColor="red";
				} else if (notifications[i].status == "onhold") {
					statusColor="blue";
				}
				activeText = activeText +
				"<a href=\"#\" class=\""+style+"\" "+
				"onClick=\"MM_openBrWindow('"+url+"','pop',"+
				"'scrollbars=yes,width=400,height=600')\">\n"+
				"<strong>"+notifications[i].date+" - "+
				notifications[i].subject +"</strong><br>" +
				notifications[i].message +"</a>\n";
			}
		}				
		
		if (numActive == 0) {
			activeText = activeText+"You have no active notifications";
		}
				
		var recentText = "<div class=\"list\">\n"+
        "<div align=\"right\">\n"+
		"<span class=\"greytype\">"+
		"<img src=\"images/led_green.gif\" width=\"11\" height=\"11\"> "+
		"active:"+numActive+"<img src=\"images/spacer.gif\" width=\"5\" height=\"1\">\n"+
		"<img src=\"images/led_yellow.gif\" width=\"11\" height=\"11\"> confirmed:"+
		numConfirmed+"<img src=\"images/spacer.gif\" width=\"5\" height=\"1\">\n"+
		"<img src=\"images/led_red.gif\" width=\"11\" height=\"11\"> expired:"+
		numExpired+"<img src=\"images/spacer.gif\" width=\"5\" height=\"1\">\n"+
		"<img src=\"images/led_blue.gif\" width=\"11\" height=\"11\"> on hold:"+
		numOnhold+"</span><br>\n"+
		"<a href=\"#\" onClick=\"MM_openBrWindow('notification_list.jsp?list=all','pop','scrollbars=yes,resizable=yes,width=400,height=600')\">view/print all</a>\n"+
		"</div></div>";
		
		
		// Limit the recent notifs to the top 20
		var numNotifs = uuids.length;
		if (numNotifs > 20) {
			numNotifs = 20;
		}
		for (var u = 0; u < numNotifs; u++) {
			var i = uuids[u];
			var url = "notification.jsp?browse=all&uuid="+i;
			
			if (style == "list") {
				style = "shadedbg list";
			} else {
				style = "list";
			}
			var statusColor="green";
			if (notifications[i].status == "confirmed") {
				statusColor="yellow";
			} else if (notifications[i].status == "expired") {
				statusColor="red";
			} else if (notifications[i].status == "onhold") {
				statusColor="blue";
			}
			recentText = recentText +
			"<a href=\"#\" class=\""+style+"\" onClick=\"MM_openBrWindow('"+url+"',"+
			"'pop','scrollbars=yes,width=400,height=600')\">\n"+
			"<img src=\"images/led_"+statusColor+".gif\" width=\"11\" "+
			"height=\"11\" border=\"0\"> "+notifications[i].date+
			" - "+notifications[i].subject+"</a>\n";
			
		}

		//alert(recentText);
		
		var activeNotifsDiv = document.getElementById('activeNotifs');
		activeNotifsDiv.innerHTML = activeText;
		var recentNotifsDiv = document.getElementById('recentNotifs');
		recentNotifsDiv.innerHTML = recentText;
		
		}
		var notifLength = 0;
		for (var i in notifications) {
			notifLength++;
		}
		if (notifLength == 0) {
			activeNotifsDiv.innerHTML = "<b>You have no active notifications</b>";
			recentNotifsDiv.innerHTML = "<b>You have no recent notifications</b>";
		}
		activeNotifsDiv = null;
		recentNotifsDiv = null;
	}
	
} catch (e) {
}
}

</script>

<!-- 
<div id="activeNotifs">
<strong>Updating...</strong>
</div>
<hr>
<div id="recentNotifs">
<strong>Loading Recent Notifications...</strong>
</div>

</body> -->
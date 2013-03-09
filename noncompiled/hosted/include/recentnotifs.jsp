<script>
function getRecentNotifs() {

    recentNotifsHttp.open('get', 'ajax/getRecentNotifs.jsp?'+Math.random());
    recentNotifsHttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    recentNotifsHttp.onreadystatechange = receiveRecentNotifs;
	recentNotifsHttp.send(null);
	
	return false;
}

function receiveRecentNotifs() {
	if((recentNotifsHttp.readyState == 4) && (recentNotifsHttp.status == 200)){ //Finished loading the response
		/* We have got the response from the server-side script,
			let's see just what it was. using the responseText property of 
			the XMLHttpRequest object. */
		var response = recentNotifsHttp.responseText;
		/* And now we want to change the product_categories <div> content.
			we do this using an ability to get/change the content of a page element 
			that we can find: innerHTML. */
		var existingDiv = document.getElementById('recentNotifs');
		var existing = existingDiv.innerHTML;
		if (existing != response) {
			 existingDiv.innerHTML = response;
		}
		existingDiv = null
	}
	
}

</script>
<div id="recentNotifs">
<strong>Loading Recent Notifications...</strong>
</div>
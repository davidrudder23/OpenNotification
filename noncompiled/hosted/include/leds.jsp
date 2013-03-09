<script>
function getLeds() {
    http.open('get', 'ajax/getLeds.jsp?'+Math.random());
    http.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    http.onreadystatechange = receiveLeds;
	http.send(null);
	
	return false;
}

function receiveLeds() {
	if((http.readyState == 4) && (http.status == 200)){ //Finished loading the response
			/* We have got the response from the server-side script,
			let's see just what it was. using the responseText property of 
			the XMLHttpRequest object. */
		var response = http.responseText;
		/* And now we want to change the product_categories <div> content.
			we do this using an ability to get/change the content of a page element 
			that we can find: innerHTML. */
		var existingDiv = document.getElementById('leds');
		var existing = existingDiv.innerHTML;
		if (existing != response) {
			existingDiv.innerHTML = response;
		}
		existingDiv = null
	}
}

</script>
<div id="leds"></div>
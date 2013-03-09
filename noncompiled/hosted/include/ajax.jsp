<script language="JavaScript">

function isdefined( variable)
{
    return (typeof(window[variable]) == "undefined")?  false: true;
}

var http = createRequestObject();
var activeNotifsHttp = createRequestObject();
var recentNotifsHttp = createRequestObject();
function createRequestObject(){
        var request_o; //declare the variable to hold the object.
        var browser = navigator.appName; //find the browser name
        if(browser == "Microsoft Internet Explorer"){
                /* Create the object using MSIE's method */
                request_o = new ActiveXObject("Microsoft.XMLHTTP");
        }else{
                /* Create the object using other browser's method */
                request_o = new XMLHttpRequest();
        }
        return request_o; //return the object
}

function receiveUserMessage() {
	if((http.readyState == 4) && (http.status == 200)){ //Finished loading the response
		/* We have got the response from the server-side script,
			let's see just what it was. using the responseText property of 
			the XMLHttpRequest object. */
		var response = http.responseText;
		/* And now we want to change the product_categories <div> content.
			we do this using an ability to get/change the content of a page element 
			that we can find: innerHTML. */
		showError (response);
		setTimeout ("updateNotifs()", 1500);
	}
}
</script>
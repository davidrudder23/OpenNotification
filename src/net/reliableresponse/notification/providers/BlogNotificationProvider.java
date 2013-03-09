/*
 * Created on Dec 8, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.providers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.BlogDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.util.StringUtils;

public class BlogNotificationProvider extends AbstractNotificationProvider {

	String postID;
	
	public BlogNotificationProvider () {
	}
	
	public void init(Hashtable params) throws NotificationException {
	}
	
	public String getCurrentDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return format.format(new Date());
	}
	
	public Hashtable sendNotification(Notification notification, Device device)
			throws NotificationException {
		
		if (!(device instanceof BlogDevice)) {
			throw new NotificationException(400, "Can not send to a non-blog device");
		}
		
		BlogDevice blogDevice = (BlogDevice)device;
		
		StringBuffer text = new StringBuffer();
		NotificationMessage[] messages = notification.getMessages();
		for (int i = 0; i < messages.length; i++) {
			text.append(messages[i].toString());
			text.append("\n");
		}
		
		StringBuffer body = new StringBuffer();
		body.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		body.append("<entry xmlns=\"http://purl.org/atom/ns#\">\n");
		body.append("<title mode=\"escaped\" type=\"text/plain\">");
		body.append (StringUtils.escapeForXML(notification.getSubject()));
		body.append ("</title>\n");
		body.append("<issued>");
		body.append(getCurrentDate());
		body.append("</issued>\n");
		body.append("<generator url=\"http://www.reliableresponse.net/\">Reliable Response Notification</generator>\n");
		body.append("<content type=\"application/xhtml+xml\">\n");
		body.append("<div xmlns=\"http://www.w3.org/1999/xhtml\">");
		body.append (StringUtils.escapeForXML(text.toString()));
		body.append("</div>\n");
		body.append("</content>\n");
		body.append("</entry>");
		
		BrokerFactory.getLoggingBroker().logDebug("Sending this ATOM to "+blogDevice.getServer()+":"+blogDevice.getBlogID()+"\n"+body.toString());
		 
		HttpClient client = new HttpClient();
		client.getState().setCredentials(
	            new AuthScope(blogDevice.getServer(), 443, "Blogger"),
	            new UsernamePasswordCredentials(blogDevice.getUsername(), blogDevice.getPassword())
	        );
		PostMethod post = new PostMethod("https://www.blogger.com/atom/"+blogDevice.getBlogID());		
		post.setDoAuthentication( true );
		post.setRequestEntity(new StringRequestEntity(body.toString()));
		post.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
		
        try {
            // execute the GET
            int status = client.executeMethod( post );

            // print the status and response
            System.out.println(status + "\n" + post.getResponseBodyAsString());
        } catch (IOException ioExc) {
        	throw new NotificationException(NotificationException.FAILED, "Could not connect to "+blogDevice.getServer());

        } finally {
            // release any connection resources used by the method
            post.releaseConnection();
        }

        
		return getParameters(notification, device);
	}

	public Hashtable getParameters(Notification notification, Device device) {
		Hashtable params = new Hashtable();
		params.put ("postID", postID);
		return params;
	}

	public String[] getResponses(Notification notification) {
		return new String[0];
	}

	public boolean cancelPage(Notification notification) {
		return false;
	}

	public String getName() {
		return "Blog posting";
	}
}

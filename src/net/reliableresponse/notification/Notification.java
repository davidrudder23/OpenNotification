/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.actions.EscalationThread;
import net.reliableresponse.notification.actions.EscalationThreadManager;
import net.reliableresponse.notification.actions.SendNotification;
import net.reliableresponse.notification.broker.AuthenticationBroker;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.broker.GroupMgmtBroker;
import net.reliableresponse.notification.broker.UserMgmtBroker;
import net.reliableresponse.notification.broker.impl.caching.CacheException;
import net.reliableresponse.notification.broker.impl.caching.Cacheable;
import net.reliableresponse.notification.device.CellPhoneEmailDevice;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.sender.EmailSender;
import net.reliableresponse.notification.sender.NonResponseSender;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.OnCallGroup;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Notification implements UniquelyIdentifiable, Comparable,
		Cacheable {

	// These are the required parameters
	Member recipient;

	Vector messages;

	String summary;

	String id;

	Date time;

	int priority;

	// If this is set, send it regardless of event storm status
	boolean released = false;

	String uuid;

	// These are optional parameters
	boolean includeTimestamp;

	boolean requireConfirmation;

	NotificationSender sender;

	Vector options;

	Vector devices;

	private String owner;

	// We use this to keep track of who sent it
	Vector notificationProviders;

	public static final int NORMAL = 0;

	public static final int PENDING = 1;

	public static final int CONFIRMED = 2;

	public static final int EXPIRED = 3;

	public static final int DELIVERED = 4;

	public static final int FAILED = 5;

	public static final int ONHOLD = 5;

	int status;

	String parent;

	static Hashtable templates;

	static long templatesUpdated;

	boolean priorityOverride;

	boolean sortByStatus;

	boolean persistent;

	boolean autocommit;

	static {
		templates = new Hashtable();
		templatesUpdated = System.currentTimeMillis();
	}

	public Notification(String parent, Member recipient,
			NotificationSender sender, String summary, String message) {
		NotificationMessage[] messages = new NotificationMessage[1];
		messages[0] = new NotificationMessage(message, "", new Date());
		init(parent, recipient, sender, summary, messages);
	}

	public Notification(String parent, Member recipient,
			NotificationSender sender, String summary,
			NotificationMessage[] messages) {
		init(parent, recipient, sender, summary, messages);
	}

	public Notification(String parent, Member recipient, Vector devices,
			NotificationSender sender, String summary, String message) {
		NotificationMessage[] messages = new NotificationMessage[1];
		messages[0] = new NotificationMessage(message, "", new Date());
		init(parent, recipient, sender, summary, messages);
		this.devices = devices;
	}

	public Notification(String parent, Member recipient, Vector devices,
			NotificationSender sender, String summary,
			NotificationMessage[] messages) {
		init(parent, recipient, sender, summary, messages);
		this.devices = devices;
	}

	/**
	 * @param parent
	 * @param recipient
	 * @param sender
	 * @param summary
	 * @param messages
	 */
	private void init(String parent, Member recipient,
			NotificationSender sender, String summary,
			NotificationMessage[] messages) {
		this.parent = parent;
		setSender(sender);
		devices = null;

		persistent = true;

		sortByStatus = true;

		if (recipient.getType() == Member.ESCALATION) {
			requireConfirmation = true;
		} else {
			requireConfirmation = false;
		}

		sender = new EmailSender("unknown@unknown.com");
		options = new Vector();

		includeTimestamp = BrokerFactory.getConfigurationBroker()
				.getBooleanValue("paging.includeTimestamp", true);

		this.summary = summary;
		this.messages = new Vector();
		if (messages != null) {
			for (int i = 0; i < messages.length; i++) {
				BrokerFactory.getLoggingBroker().logDebug(
						"Adding message to notification: "
								+ messages[i].getMessage());
				this.messages.addElement(messages[i]);
			}
		} else {
			BrokerFactory.getLoggingBroker().logWarn(
					"Creating new notification with no content");
		}
		this.recipient = recipient;

		time = new Date();
		notificationProviders = new Vector();

		this.messages = new Vector();
		for (int i = 0; i < messages.length; i++) {
			this.messages.addElement(messages[i]);
		}

		// We use the priority override feature from SOAP calls
		// So that backend systems can set the priority
		// Like, Nagios can label it high, low or unknown
		priorityOverride = false;

		// This sets the default priority. If this is a group,
		// it will get overwritten later
		int priority = 3;

		if (recipient instanceof User) {
			String priorityString = ((User) recipient)
					.getInformation("priority");
			if (!StringUtils.isEmpty(priorityString)) {
				try {
					priority = Integer.parseInt(priorityString);
				} catch (NumberFormatException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
		}
		setPriority(priority);

		autocommit = true;
	}
	
	public Notification getUltimateParent() {
		Notification notification = this;
		int count = 0;
		while ((notification.getParentUuid() != null) && (count < 20)) {
			Notification newNotification = BrokerFactory
					.getNotificationBroker().getNotificationByUuid(
							notification.getParentUuid());
			if (newNotification == null) {
				return notification;
			//	return BrokerFactory.getNotificationBroker()
			//			.getNotificationByUuid(notification.getUuid());
			}
			notification = newNotification;
			count++;
		}

		if (notification == null) {
			return this;
		} else {
			return notification;
		}
	}
	
	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public Device[] getDevices() {
		if (devices != null) {
			BrokerFactory.getLoggingBroker().logDebug("Notification has "+devices.size());
			return (Device[]) devices.toArray(new Device[0]);
		}
		Member recipient = getRecipient();
		BrokerFactory.getLoggingBroker().logDebug("Getting devices for "+recipient);
		if (recipient instanceof Group) {
			return new Device[0];
		}

		User user = (User) recipient;
		BrokerFactory.getLoggingBroker().logDebug(recipient+" has "+user.getDevices().length+" devices");
		if ((devices == null) || (devices.size() == 0)) {
			return user.getDevices();
		}
		return (Device[]) devices.toArray(new Device[0]);
	}

	/**
	 * @return
	 */
	public boolean isIncludeTimestamp() {
		return includeTimestamp;
	}

	/**
	 * @return
	 */
	public NotificationMessage[] getMessages() {
		return getMessages(true);
	}
	
	public NotificationMessage[] getMessages(boolean recursive) {
		if (recursive) {
			return (NotificationMessage[])getUltimateParent().getMessages(false);			
		} else {
			return (NotificationMessage[])messages.toArray(new NotificationMessage[0]);
		}
	}

	public String getAllMessagesFormattedForDisplay() {
		StringBuffer message = new StringBuffer();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss z");

		NotificationMessage[] messages = getMessages();
		for (int m = 0; m < messages.length; m++) {
			String contentType = messages[m].getContentType();
			String addedby = messages[m].getAddedby();
			if ((addedby != null) && (addedby.length() > 0)) {
				User addedByUser = BrokerFactory.getUserMgmtBroker()
						.getUserByUuid(addedby);
				if (addedByUser != null)
					addedby = addedByUser.toString();
				message.append(addedby);
			} else {
				message.append(getSender().toString());
			}
			message.append(" - ");
			message.append(dateFormatter.format(messages[m].getAddedon()));
			message.append(" - ");
			if ((contentType.equalsIgnoreCase(NotificationMessage.NOTIFICATION_CONTENT_TYPE)) || (contentType.equalsIgnoreCase("text/plain"))) {
				message.append(messages[m].getMessage());
			} else if (BrokerFactory.getConfigurationBroker().getBooleanValue("show.attachments", true)){
				message.append("Attachment: "+messages[m].getFilename());
			}
			message.append("\n");
		}
		return message.toString();
	}
	
	/**
	 * Clears out the messages. This is most useful for resetting a notification
	 */
	public void clearMessages() {
		messages.removeAllElements();
	}

	public String getDisplayText() {
		NotificationMessage[] messages = getMessages();
		BrokerFactory.getLoggingBroker().logDebug(
				"We have " + messages.length + " notification messages");
		if ((messages == null) || (messages.length < 1)) {
			return "";
		}
		return getDisplayText(messages[0].getMessage());
	}

	private String loadTemplate(String templateFile) {
		if ((System.currentTimeMillis() - templatesUpdated) > 60000) {
			templates.clear();
			templatesUpdated = System.currentTimeMillis();
		}
		StringBuffer template = (StringBuffer) templates.get(templateFile);
		if (template == null) {
			template = new StringBuffer();
			try {
				BufferedReader in = new BufferedReader(new FileReader(
						BrokerFactory.getConfigurationBroker().getStringValue(
								"tomcat.location")
								+ "/webapps/notification/conf/" + templateFile));
				String line;
				while ((line = in.readLine()) != null) {
					template.append(line);
					template.append("\n");
				}
			} catch (Exception e) {
				// BrokerFactory.getLoggingBroker().logError(e);
				return null;
			}
			templates.put(templateFile, template);
		}

		return template.toString();
	}

	public String getDisplayText(String message) {
		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();
		String template = "%m";
		Notification parent = getUltimateParent();
		int type = parent.getRecipient().getType();
		if (type == Member.ESCALATION) {
			String newTemplate = loadTemplate("escalation.template");
			if (newTemplate != null)
				template = newTemplate;
		} else if (type == Member.BROADCAST) {
			String newTemplate = loadTemplate("broadcast.template");
			if (newTemplate != null)
				template = newTemplate;
		} else if (type == Member.ONCALL) {
			String newTemplate = loadTemplate("oncall.template");
			if (newTemplate != null)
				template = newTemplate;
		} else {
			String newTemplate = loadTemplate("individual.template");
			if (newTemplate != null) {
				template = newTemplate;
			}
		}

		String emailAddress = getUuid() + "@"
				+ config.getStringValue("smtp.server.hostname");
		String confirmUrl = config.getStringValue("base.url")
				+ "/ConfirmNotification?id=" + getUuid() + "&action=confirm";
		String passUrl = config.getStringValue("base.url")
				+ "/ConfirmNotification?id=" + getUuid() + "&action=pass";
		template = StringUtils.replaceString(template, "%m", message);
		template = StringUtils.replaceString(template, "%s", getSubject());

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss z");
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		template = StringUtils.replaceString(template, "%t", timeFormat
				.format(getTime()));
		template = StringUtils.replaceString(template, "%d", dateFormat
				.format(getTime()));
		template = StringUtils.replaceString(template, "%f", getSender()
				.toString());
		template = StringUtils.replaceString(template, "%i", getRecipient()
				.toString());
		template = StringUtils.replaceString(template, "%g", parent
				.getRecipient().toString());
		template = StringUtils.replaceString(template, "%ec", emailAddress);
		template = StringUtils.replaceString(template, "%ep", emailAddress);
		template = StringUtils.replaceString(template, "%uc", confirmUrl);
		template = StringUtils.replaceString(template, "%up", passUrl);
		template = StringUtils.replaceString(template, "\\n", "  \n");
		template = StringUtils.replaceString(template, "\\t", "\t");

		EscalationThread escThread = EscalationThreadManager.getInstance()
				.getEscalationThread(parent.getUuid());
		if (escThread != null) {
			template = StringUtils.replaceString(template, "%e", ""
					+ escThread.getRecipientNumber());
		} else {
			template = StringUtils.replaceString(template, "%e", "unknown");
		}

		return template;
	}

	/**
	 * @return
	 */
	public Member getRecipient() {
		return recipient;
	}

	public User[] getAllUserRecipients() {
		if (getRecipient().getType() == Member.USER) {
			return new User[] { (User) getRecipient() };
		}
		Vector users = new Vector();
		Notification[] children = BrokerFactory.getNotificationBroker()
				.getChildren(this);
		for (int i = 0; i < children.length; i++) {
			Member childRecipient = children[i].getRecipient();
			if (childRecipient.getType() == Member.USER) {
				users.addElement(childRecipient);
			} else {
				User[] childUsers = children[i].getAllUserRecipients();
				for (int u = 0; u < childUsers.length; u++) {
					users.addElement(childUsers[u]);
				}
			}
		}
		return (User[]) users.toArray(new User[0]);
	}

	public Notification[] getAllChildrenSentToUsers() {
		if (getRecipient().getType() == Member.USER) {
			return new Notification[] { this };
		}
		Vector notifs = new Vector();
		Notification[] children = BrokerFactory.getNotificationBroker()
				.getChildren(this);
		for (int i = 0; i < children.length; i++) {
			Notification[] descendents = children[i]
					.getAllChildrenSentToUsers();
			for (int d = 0; d < descendents.length; d++) {
				notifs.addElement(descendents[d]);
			}
		}
		return (Notification[]) notifs.toArray(new Notification[0]);
	}

	/**
	 * @return
	 */
	public boolean isRequireConfirmation() {
		return requireConfirmation;
	}

	/**
	 * @return
	 */
	public NotificationSender getSender() {
		return sender;
	}

	/**
	 * @param b
	 */
	public void setIncludeTimestamp(boolean b) {
		includeTimestamp = b;
	}

	/**
	 * @param string
	 */
	public void addMessage(NotificationMessage message) {
		addMessage(message, true);
	}

	public void addMessage(NotificationMessage message, boolean recursive) {
		// Make sure we're adding this to the ultimate parent
		BrokerFactory.getLoggingBroker().logDebug(
				"Adding message with parent uuid=" + getParentUuid());
		if (recursive && (getParentUuid() != null)) {
			Notification parent = getUltimateParent();
			boolean oldAutocommit = parent.isAutocommit();
			parent.setAutocommit(this.isAutocommit());
			parent.addMessage(message, false);
			parent.setAutocommit(oldAutocommit);
			return;
		}
		if ((message != null) && (message.getMessage() != null)) {
			messages.addElement(message);
			if ((autocommit) && persistent) {
				BrokerFactory.getNotificationBroker().addMessage(
						this.getUltimateParent(), message);
			}
		}
	}

	public void addMessage(String message, Member addedby) {
		BrokerFactory.getLoggingBroker().logDebug("Adding message " + message);
		String uuid = null;
		if (addedby != null)
			uuid = addedby.getUuid();
		NotificationMessage notif = new NotificationMessage(message, uuid,
				new Date());
		addMessage(notif);
	}

	public void addMessage(String message, Member addedby, String contentType) {
		BrokerFactory.getLoggingBroker().logDebug("Adding message " + message);
		String uuid = null;
		if (addedby != null)
			uuid = addedby.getUuid();
		NotificationMessage notif = new NotificationMessage(message, uuid,
				new Date());
		notif.setContentType(contentType);
		addMessage(notif);
	}

	public void clearOptions() {
		options = new Vector();
	}

	public void addOption(String option) {
		options.addElement(option);
	}

	/**
	 * @param member
	 */
	public void setRecipient(Member member) {
		recipient = member;
	}

	/**
	 * @param b
	 */
	public void setRequireConfirmation(boolean b) {
		requireConfirmation = b;
	}

	/**
	 * @param string
	 */
	public void setSender(NotificationSender sender) {
		this.sender = sender;
	}

	/**
	 * @return
	 */
	public String getSubject() {
		return summary;
	}

	/**
	 * @param string
	 */
	public void setSummary(String string) {
		summary = string;
	}

	/**
	 * @return Returns the time.
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time
	 *            The time to set.
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @return
	 */
	public Vector getOptions() {
		if (options == null) {
			options = new Vector();
		}
		if (options.size() == 0) {
			options.addElement("Confirm");
		}
		return options;
	}

	/**
	 * @param vector
	 */
	public void setOptions(Vector vector) {
		options = vector;
	}

	public String getID() {
		return getUuid();
	}

	public NotificationProvider[] getNotificationProviders() {
		return (NotificationProvider[]) notificationProviders
				.toArray(new NotificationProvider[0]);
	}

	public void addNotificationProvider(
			NotificationProvider NotificationProvider) {
		notificationProviders.addElement(NotificationProvider);
	}

	public synchronized String getUuid() {
		if (uuid == null) {
			uuid = BrokerFactory.getUUIDBroker().getUUID(this);
		}
		return uuid;
	}

	public synchronized void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getStatus() {
		return status;
	}

	public String getStatusAsString() {
		switch (status) {
		case Notification.PENDING:
			return "active";
		case Notification.NORMAL:
			return "active";
		case Notification.CONFIRMED:
			return "confirmed";
		case Notification.EXPIRED:
			return "expired";
		case Notification.ONHOLD:
			return "onhold";

		}

		return "active";
	}

	private void setParentStatus(int status, Member responder) {
		this.status = status;
		if (autocommit) {
			switch (status) {
			case Notification.CONFIRMED:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "confirmed");
				break;
			case Notification.EXPIRED:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "expired");
				break;
			case Notification.PENDING:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "pending");
				break;
			case Notification.NORMAL:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "pending");
				break;
			}
		}

		String parentUuid = getParentUuid();
		if (parentUuid != null) {
			Notification parent = BrokerFactory.getNotificationBroker()
					.getNotificationByUuid(parentUuid);
			if (parent != null) {
				parent.setParentStatus(status, responder);
			}
		}
	}

	private void setChildrenStatus(int status, Member responder, int depth) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Setting status of " + getUuid() + " to " + status
						+ " for responder=" + responder);
		this.status = status;
		if (autocommit) {
			switch (status) {
			case Notification.CONFIRMED:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "confirmed");
				break;
			case Notification.EXPIRED:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "expired");
				break;
			case Notification.PENDING:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "pending");
				break;
			case Notification.NORMAL:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "pending");
				break;
			}
		}

		if (depth< 50) {
			Notification[] children = BrokerFactory.getNotificationBroker()
				.getChildren(this);
			for (int i = 0; i < children.length; i++) {
				children[i].setChildrenStatus(status, responder, depth+1);
			}
		}
	}

	public void setStatus(int status) {
		setStatus(status, null);
	}

	public void setStatus(int status, Member responder) {
		this.status = status;
		BrokerFactory.getLoggingBroker().logDebug(
				"Setting status of " + getUuid() + " to " + status
						+ " for responder=" + responder);

		// recurse to parents and children
		if (autocommit) {
			Notification parent = getUltimateParent();
			BrokerFactory.getLoggingBroker().logDebug(
					"this=" + this.getUuid() + "\nparent=" + parent.getUuid());
			if (!(parent.equals(this))) {
				parent.setStatus(status, responder);
				return;
			}

			switch (status) {
			case Notification.CONFIRMED:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "confirmed");
				break;
			case Notification.EXPIRED:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "expired");
				break;
			case Notification.PENDING:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "pending");
				break;
			case Notification.NORMAL:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "pending");
				break;
			case Notification.ONHOLD:
				BrokerFactory.getNotificationBroker().setNotificationStatus(
						this, "onhold");
				break;
			}

			setChildrenStatus(status, responder, 0);
		}

		if (responder != null)
			notifyGroupMembers(recipient, responder, 0);

	}

	private void notifyGroupMembers(Member recipient, Member responder, int count) {
		if (count>10) return;
		String responderString = "Unknown Responder";
		if (responder != null) {
			responderString = responder.toString();
		}

		if (recipient instanceof BroadcastGroup) {
			BroadcastGroup group = (BroadcastGroup) recipient;
			Notification[] children = BrokerFactory.getNotificationBroker().getChildren(this);
			for (int i = 0; i < children.length; i++) {
				children[i].notifyGroupMembers(children[i].getRecipient(), responder, count++);
			}
		} else if (recipient instanceof EscalationGroup) {
			BrokerFactory.getLoggingBroker().logDebug(
					"Updating members of esc group " + recipient);
			EscalationGroup group = (EscalationGroup) recipient;
			EscalationThread thread = EscalationThreadManager.getInstance()
					.getEscalationThread(getUuid());
			Notification[] children = BrokerFactory.getNotificationBroker().getChildren(this);
			if (thread != null) {
				int memberNum = thread.getRecipientNumber();
				BrokerFactory.getLoggingBroker().logDebug("Escalation number="+memberNum);

				if (memberNum >= children.length)
					memberNum = children.length - 1;
				for (int i = 0; i <= memberNum; i++) {
					children[i].notifyGroupMembers(children[i].getRecipient(), responder, count++);
				}
			}
		} else if (recipient instanceof OnCallGroup) {
			OnCallGroup group = (OnCallGroup) recipient;
			Member[] members = group.getOnCallMembers(new Date());
			for (int memberNum = 0; memberNum < members.length; memberNum++) {
				notifyGroupMembers(members[memberNum], responder, count++);
			}
		} else {
			User user = (User)recipient;
			
			BrokerFactory.getLoggingBroker().logDebug(
					"Updating " + user+" with status of notif "+getUuid());
			Notification updateNotification = new Notification(null,
					user, new NonResponseSender(
							"Notification Update"), "Notification "
							+ getUuid() + " updated by "
							+ responderString,
					"The notification with the subject \""
							+ getSubject()
							+ "\" has been confirmed by "
							+ responderString);
			updateNotification.setPersistent(false);
			try {
				SendNotification.getInstance().doSend(
						updateNotification);
			} catch (NotificationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
	}

	public String getParentUuid() {
		if (parent == null) {
			return null;
		}
		if (getUuid().compareTo(parent)<=0) {
			return null;
		}
		return parent;
	}

	public void setParentUuid(String uuid) {
		this.parent = uuid;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * This stes a priority value that overrides the users' priority settings.
	 * Please be careful to use this only when you know the users' settings can
	 * be ignored. Used by the SOAP interface so that the backend system can
	 * specify the priority
	 * 
	 * @param priority
	 */
	public void overridePriority(int priority) {
		this.priority = priority;
		this.priorityOverride = true;
	}

	public boolean isAutocommit() {
		return autocommit;
	}

	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	public String getOwner() {
		if (owner == null) {
			owner = BrokerFactory.getConfigurationBroker().getStringValue(
					"cluster.name");
		}
		if (owner == null)
			owner = "single";
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
		if (owner == null) {
			owner = BrokerFactory.getConfigurationBroker().getStringValue(
					"cluster.name");
		}
		if (autocommit) {
			BrokerFactory.getNotificationBroker().setOwner(this, owner);
		}
	}

	/**
	 * Persistence determines whether the notification will be logged in the
	 * database, whether responses will be tracked, and whether this
	 * notification shows up in the various lists.
	 * 
	 * @return Whether the notification is persistent
	 */
	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public static Member findRecipient(String identifier) {
		UserMgmtBroker userBroker = BrokerFactory.getUserMgmtBroker();
		GroupMgmtBroker groupBroker = BrokerFactory.getGroupMgmtBroker();
		AuthenticationBroker authnBroker = BrokerFactory
				.getAuthenticationBroker();
		Member member = userBroker.getUserByUuid(identifier);

		while ((identifier.endsWith(" ")) || (identifier.endsWith("\""))) {
			identifier = identifier.substring(0, identifier.length() - 1);
		}
		while ((identifier.startsWith(" ")) || (identifier.startsWith("\""))) {
			identifier = identifier.substring(1, identifier.length());
		}
		BrokerFactory.getLoggingBroker().logDebug("Looking for recipient: "+identifier);

		if (member == null) {
			member = groupBroker.getGroupByUuid(identifier);
		}
		if (member == null) {
			member = groupBroker.getGroupByName(identifier);
		}
		if (member == null) {
			member = authnBroker.getUserByIdentifier(identifier);
		}
		if (member == null) {
			member = userBroker.getUserByEmailAddress(identifier);
		}
		if (member == null) {
			member = groupBroker.getGroupByEmail(identifier);
		}
		if (member == null) {
			if (identifier.indexOf(",") > 0) {
				String lastName = identifier.substring(0, identifier
						.indexOf(","));
				String firstName = identifier.substring(
						identifier.indexOf(",") + 1, identifier.length());
				while (firstName.startsWith(" "))
					firstName = firstName.substring(1, firstName.length());
				User[] users = userBroker.getUsersByName(firstName, lastName);
				if (users.length > 0)
					member = users[0];
			}
		}
		if (member == null) {
			if (identifier.indexOf(" ") > 0) {
				String firstName = identifier.substring(0, identifier
						.indexOf(" "));
				String lastName = identifier.substring(
						identifier.indexOf(" ") + 1, identifier.length());
				while (lastName.startsWith(" "))
					lastName = lastName.substring(1, lastName.length());
				while (firstName.startsWith(" "))
					firstName = firstName.substring(1, firstName.length());
				User[] users = userBroker.getUsersByName(firstName, lastName);
				if (users.length > 0)
					member = users[0];
			}
		}
		if (member == null) {
			if (identifier.indexOf(" ") > 0) {
				String firstName = identifier.substring(0, identifier
						.lastIndexOf(" "));
				String lastName = identifier.substring(identifier
						.lastIndexOf(" ") + 1, identifier.length());
				while (lastName.startsWith(" "))
					lastName = lastName.substring(1, lastName.length());
				while (firstName.startsWith(" "))
					firstName = firstName.substring(1, firstName.length());
				User[] users = userBroker.getUsersByName(firstName, lastName);
				if (users.length > 0)
					member = users[0];
			}
		}

		if (member == null) {
			if (CellPhoneEmailDevice.isCellPhoneAddress(identifier)) {
				BrokerFactory.getLoggingBroker().logDebug(
						identifier + " is a cell phone email address");
				User[] users = BrokerFactory
						.getUserMgmtBroker()
						.getUsersWithDeviceType(
								"net.reliableresponse.notification.device.CellPhoneEmailDevice");
				if (users != null) {
					for (int i = 0; i < users.length; i++) {
						Device[] devices = users[i].getDevices();
						for (int d = 0; d < devices.length; d++) {
							if (devices[d] instanceof CellPhoneEmailDevice) {
								CellPhoneEmailDevice cellDevice = (CellPhoneEmailDevice) devices[d];
								if (cellDevice.getEmailAddress()
										.equalsIgnoreCase(identifier)) {
									return users[i];
								}
							}
						}
					}
				}
			}
		}
		
		if (member == null) {
			Member[] members = BrokerFactory.getUserMgmtBroker().getUsersWithInformationLike("Incoming Email Aliases", ","+identifier);
			if ((members != null) && (members.length>0)) {
				member = members[0];
			}
		}
		BrokerFactory.getLoggingBroker().logDebug("Found recipient "+member);
		return member;
	}

	/**
	 * This tells compareTo to first sort by status, then by date within the
	 * status.
	 * 
	 */
	public void sortByStatus() {
		sortByStatus = true;
	}

	/**
	 * This tells compareTo to sort merely by date
	 * 
	 */
	public void sortByTime() {
		sortByStatus = false;
	}

	/**
	 * This is used for sorting
	 */

	public int compareTo(Object compareto) {
		if (compareto instanceof Notification) {
			Notification other = (Notification) compareto;
			if (other.getUuid().equals(getUuid())) {
				return 0;
			}
			int statusDiff = getStatus() - other.getStatus();
			// If we're not sorting by the status, zero out the
			// status differences
			if (!sortByStatus) {
				statusDiff = 0;
			}
			if (statusDiff == 0) {
				return other.getUuid().compareTo(getUuid());
			} else {
				return statusDiff;
			}
		} else {
			return -1;
		}
	}

	public boolean equals(Object other) {
		if (other instanceof Notification) {
			if (((Notification) other).getUuid().equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	public void refreshObject(Cacheable object) throws CacheException {
		if (!(object instanceof Cacheable)) {
			throw new CacheException(
					"Updated information is not from a Notification onject");
		}
		Notification other = (Notification) object;
		boolean autocommit = isAutocommit();

		setAutocommit(false);
		setStatus(other.getStatus());
		messages = new Vector();
		NotificationMessage[] otherMessages = other.getMessages();
		for (int i = 0; i < otherMessages.length; i++) {
			messages.addElement(otherMessages[i]);
		}

		setAutocommit(autocommit);
	}
	
	public String getMessagesAsXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append("<messages>\n");
		NotificationMessage[] messages = getMessages();
		for (int messageNum = 0; messageNum < messages.length; messageNum++) {
			xml.append("<message>\n");
			NotificationMessage message = messages[messageNum];
			if (message.getContentType().toLowerCase().startsWith("text/")) {
				xml.append ("<contents>");
				xml.append (new String(message.getContent()));
				xml.append ("</contents>\n");
			} else if (BrokerFactory.getConfigurationBroker().getBooleanValue("show.attachments", true)) {
				xml.append ("<link>");
				xml.append ("/notification/AttachmentServlet?notification="+getUuid()+"&messageNum="+messageNum);
				xml.append ("</link>\n");
			}
			xml.append("</message>\n");
		}
		xml.append("</messages>\n");
		return xml.toString();
	}
	
	public String getAsXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append("<notification>\n");

		xml.append("<url>");
		xml.append("/notification/rest/notifications/"+getUuid());
		xml.append("</url>\n");

		xml.append("<subject>");
		xml.append(getSubject());
		xml.append("</subject>\n");
		
		xml.append("<date>");
		xml.append(getTime());
		xml.append("</date>\n");

		xml.append("<recipient>");
		if (getRecipient() instanceof User) {
			xml.append("/notification/users/"+getRecipient().getUuid());
		} else {
			xml.append("/notification/groups/"+getRecipient().getUuid());
		}
		xml.append("</recipient>\n");

		xml.append(getMessagesAsXML());

		xml.append("<sender>");
		xml.append(getSender().toString());
		xml.append("</sender>\n");
		
		xml.append("</notification>\n");
		
		return xml.toString();
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		Member member = Notification.findRecipient(args[0]);
		System.out.println("member=" + member);
	}
}

/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.Stoppable;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.ConfigurationBroker;
import net.reliableresponse.notification.broker.impl.clustered.ClusteredServiceManager;
import net.reliableresponse.notification.usermgmt.BroadcastGroup;
import net.reliableresponse.notification.usermgmt.EscalationGroup;
import net.reliableresponse.notification.usermgmt.Group;
import net.reliableresponse.notification.usermgmt.InvalidGroupException;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class LDAPImporter implements StatefulJob, Stoppable {
	InitialLdapContext ctx;

	String searchString;
	String base;

	boolean stopped;

	public LDAPImporter(String userName, String password, String host,
			String base, String searchString) {
		this.searchString = searchString;
		this.base = base;
		stopped = false;

		ctx = new LDAPLibrary(host).getContext(userName, password);
	}

	public LDAPImporter() {

	}

	public void getAll(HashMap identifyingMap) {
		if (!ClusteredServiceManager.getInstance().willRun("LDAP Import")) {
			return;
		}
		
		boolean paged = BrokerFactory.getConfigurationBroker().getBooleanValue(
				"ldap.usepagecontrols");
		if (paged) {
			getAllUsersPaged(identifyingMap);
			getAllGroupsPaged();
		} else {
			getAllUsersNonPaged(identifyingMap);
			getAllGroupsNonPaged();
		}
	}

	private void getAllUsersPaged(HashMap identifyingMap) {
		try {
			stopped = false;
			PagedResultsControl prc = new PagedResultsControl(128, false);
			ctx.setRequestControls(new Control[] { prc });
			
			// Get the values that we want returned
			String[] keySet = (String[]) identifyingMap.keySet()
			.toArray(new String[0]); 
			String[] neededValues = {"userAccountControl", "extensionAttribute14"};
			String[] returningAttributes = new String[keySet.length+neededValues.length];
			System.arraycopy(neededValues, 0, returningAttributes, 0, neededValues.length);
			System.arraycopy(keySet, 0, returningAttributes, neededValues.length, keySet.length);
			
			
			SearchControls ctls = new SearchControls();
			ctls.setReturningObjFlag(true);
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returningAttributes);
			byte[] b = null;
			do {
				getAllUsers(identifyingMap, ctls);
				if (!stopped) {
					Control[] prrcs = (Control[]) ctx.getResponseControls();
					BrokerFactory.getLoggingBroker().logDebug("LDAP gave us "+prrcs.length+" controls");
					PagedResultsResponseControl prrc = null;
					for (int prrcNum = 0; prrcNum < prrcs.length; prrcNum++) {
						BrokerFactory.getLoggingBroker().logDebug("control["+prrcNum+"]="+prrcs[prrcNum]);
						if (prrcs[prrcNum] instanceof PagedResultsResponseControl) {
							prrc = (PagedResultsResponseControl) prrcs[0];
						}
					}
					BrokerFactory.getLoggingBroker().logDebug("prrc="+prrc);
					if (prrc != null) {	
						b = prrc.getCookie();
						if (b != null) {
							ctx.setRequestControls(new Control[] { new PagedResultsControl(
										128, b, Control.CRITICAL) });
						}
					}
				}
			} while ((b != null) && (!stopped));
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (NamingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

	}

	private void getAllUsersNonPaged(HashMap identifyingMap) {
		try {
			stopped = false;
			SearchControls ctls = new SearchControls();
			ctls.setReturningObjFlag(true);
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			getAllUsers(identifyingMap, ctls);
		} catch (NamingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}

	}


	/**
	 * @param identifyingMap
	 * @param ctls
	 * @param filter
	 * @throws NamingException
	 */
	private void getAllUsers(HashMap identifyingMap, SearchControls ctls) throws NamingException {
		String filter = "("+BrokerFactory.getConfigurationBroker().getStringValue("ldap.searchString", "objectClass=person")+")";

		NamingEnumeration namingEnum = ctx.search(base, filter, ctls);
		while ((namingEnum.hasMoreElements()) && (!stopped)) {
			User user = new User();
			SearchResult result = (SearchResult) namingEnum.nextElement();
			
			// Set the LDAP CN in the user's information.
			// This is used for authentication as well as comparisons
			// against the DB
			String ldapCN = result.getName() + "," + base;
			//ldapCN = ldapCN.replaceAll(", ", ",");
			BrokerFactory.getLoggingBroker().logDebug(
					"ldap cn = " + ldapCN);
			user.setInformation("LDAP CN", ldapCN);


			//BrokerFactory.getLoggingBroker().logDebug("result="+result);
			Attributes attribs = result.getAttributes();

			Iterator remoteLDAPSettings = identifyingMap.keySet()
					.iterator();
			while ((remoteLDAPSettings.hasNext()) && (!stopped)) {
				String key = (String) remoteLDAPSettings.next();
				LDAPSetting localLDAPSetting = (LDAPSetting) identifyingMap
						.get(key);

				BasicAttribute value = (BasicAttribute) attribs
						.get(key);
				if (value != null) {
					localLDAPSetting.addSetting(user, (String) value
							.get());
				}
			}

			if ((user.getFirstName() != null)
					|| (user.getLastName() != null)) {
				String[] identifyingKeys = (String[]) identifyingMap
						.keySet().toArray(new String[0]);

				User storedUser = null;
				storedUser = BrokerFactory.getUserMgmtBroker().getUserByInformation("LDAP CN", user.getInformation("LDAP CN"));
		
				if ((storedUser == null) && (user.getEmailAddress() != null)) {
					storedUser = BrokerFactory.getUserMgmtBroker()
							.getUserByEmailAddress(
									user.getEmailAddress());
				} 
				if (storedUser == null) {
					String firstName = user.getFirstName();
					String lastName = user.getLastName();
					if (firstName == null) firstName = "";
					if (lastName == null) lastName = "";
					User[] storedUsersLike = BrokerFactory
							.getUserMgmtBroker().getUsersByName(firstName, lastName);
					if (storedUsersLike.length > 0)
						storedUser = storedUsersLike[0];
				}
				
				if (storedUser == null) {
					// Check for a deleted user
					storedUser = BrokerFactory.getUserMgmtBroker().getDeletedUser(user.getFirstName(), user.getLastName());
				}
				
				
				// This is an AD-Specific disabled check
				int userAccountControl = 0;
				try {
					Attribute uACattrib = attribs.get("userAccountControl");
					if (uACattrib != null) {
						String userAccountControlString =(String)uACattrib.get();
						BrokerFactory.getLoggingBroker().logDebug("uAC String= "+userAccountControlString);
						int radix = 10;
						if (userAccountControlString.toLowerCase().startsWith("0x")) {
							radix=16;
							userAccountControlString = userAccountControlString.substring(2, userAccountControlString.length());
						}
						if (userAccountControlString !=null) {
							userAccountControl = Integer.parseInt (userAccountControlString, radix);
						}
					}
				} catch (NumberFormatException e2) {
					BrokerFactory.getLoggingBroker().logError(e2);
				} catch (NamingException e2) {
				}
				BrokerFactory.getLoggingBroker().logDebug(user+"'s userAccountControl = "+userAccountControl);
				boolean isDisabled = ((userAccountControl & 0x2)!= 0);
				
				// DEX-specific check for extensionAttributre14
				if (!isDisabled) {
					Attribute extensionAttribute = attribs.get("extensionAttribute14");
					BrokerFactory.getLoggingBroker().logDebug(user+"'s Extension 14 = "+extensionAttribute);
					if (extensionAttribute != null) {
						Object extension = extensionAttribute.get();
						BrokerFactory.getLoggingBroker().logDebug(user+"'s Extension 14 String = "+extensionAttribute);
						if ((extension!= null) && (extension instanceof String) && 
								(((String)extension).toLowerCase().indexOf("disable") >= 0)) {
							isDisabled = true;
						}
					}
				}
				// END DEX-specific check for extensionAttributre14
				
				// Fedora-DS/IPlanet check
				if (!isDisabled) {
					Attribute accountLockAttribute = attribs.get("nsAccountLock");
					BrokerFactory.getLoggingBroker().logDebug("NS Account Lock="+accountLockAttribute);
					if (accountLockAttribute != null) {
						isDisabled = true;
					}
				}
				
				BrokerFactory.getLoggingBroker().logInfo(user+" "+(isDisabled?"is":"is not")+" disabled");

				if (storedUser != null) {
					BrokerFactory
							.getLoggingBroker()
							.logDebug(
									user
											+ " already in system.  Loading changes");
					
					if (isDisabled) {
						if (!storedUser.isDeleted()) {
							try {
								storedUser.setInformation("Deleted By", "LDAP");
								BrokerFactory.getUserMgmtBroker().updateUser(storedUser);
								BrokerFactory.getUserMgmtBroker().deleteUser(
										storedUser);
							} catch (NotSupportedException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							}
						}
					} else {
						BrokerFactory.getLoggingBroker().logDebug(storedUser+" is Deleted? "+storedUser.isDeleted());
						if (storedUser.isDeleted()) {
							// This user is not disabled, but is currently deleted
							String deletedBy = storedUser.getInformation("Deleted By");
							BrokerFactory.getLoggingBroker().logDebug("deleted by = "+deletedBy);
							if ((deletedBy != null) && (deletedBy.equals("LDAP"))){
								// This person was deleted by LDAP, so undelete him/her								
								BrokerFactory.getUserMgmtBroker().undeleteUser(storedUser);
								storedUser = BrokerFactory.getUserMgmtBroker().getUserByUuid(storedUser.getUuid());
								storedUser.setInformation("Deleted By", "");
							}
						}
						for (int i = 0; i < identifyingKeys.length; i++) {
							((LDAPSetting) identifyingMap
									.get(identifyingKeys[i])).checkForUpdates(
									storedUser, user);
						}
						try {
							BrokerFactory.getUserMgmtBroker().updateUser(
									storedUser);
						} catch (NotSupportedException e1) {
							BrokerFactory.getLoggingBroker().logError(e1);
						}
						storedUser.setAutocommit(true);
					}
				} else {
					if (!isDisabled) {
						for (int i = 0; i < identifyingKeys.length; i++) {
							((LDAPSetting) identifyingMap
									.get(identifyingKeys[i])).postCheck(user);
						}

						try {
							BrokerFactory.getUserMgmtBroker().addUser(user);
							user.setAutocommit(true);
						} catch (NotSupportedException e1) {
							// TODO Auto-generated catch block
							BrokerFactory.getLoggingBroker().logError(e1);
						}
					}
				}
			}
		}
	}

	private void getAllGroupsPaged() {
		try {
			stopped = false;
			PagedResultsControl prc = new PagedResultsControl(128, false);
			ctx.setRequestControls(new Control[] { prc });
			String[] returningAttributes = {"cn", "uniqueMember", "description"};
			SearchControls ctls = new SearchControls();
			ctls.setReturningObjFlag(true);
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returningAttributes);
			byte[] b = null;
			do {
				getAllGroups(ctls);
				if (!stopped) {
					Control[] prrcs = (Control[]) ctx.getResponseControls();
					PagedResultsResponseControl prrc = null;
					for (int prrcNum = 0; prrcNum < prrcs.length; prrcNum++) {
						if (prrcs[prrcNum] instanceof PagedResultsResponseControl) {
							prrc = (PagedResultsResponseControl)prrcs[prrcNum];
						}
					}
					if (prrc != null) {
						b = prrc.getCookie();
						if (b != null) {
							ctx.setRequestControls(new Control[] { new PagedResultsControl(
									128, b, Control.CRITICAL) });
						}
					}
				}
			} while ((b != null) && (!stopped));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			BrokerFactory.getLoggingBroker().logError(e);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			BrokerFactory.getLoggingBroker().logError(e);
		}

	}

	private void getAllGroupsNonPaged() {
		try {
			stopped = false;
			SearchControls ctls = new SearchControls();
			ctls.setReturningObjFlag(true);
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			getAllGroups(ctls);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			BrokerFactory.getLoggingBroker().logError(e);
		}

	}

	
	/**
	 * @param identifyingMap
	 * @param ctls
	 * @param filter
	 * @throws NamingException
	 */
	private void getAllGroups(SearchControls ctls) throws NamingException {
		BrokerFactory.getLoggingBroker().logDebug("Loading groups from LDAP");
		String filter = "("+BrokerFactory.getConfigurationBroker().getStringValue("ldap.groupfilter", "objectClass=groupofuniquenames")+")";

		NamingEnumeration namingEnum = ctx.search(base, filter, ctls);
		while ((namingEnum.hasMoreElements()) && (!stopped)) {
			SearchResult result = (SearchResult) namingEnum.nextElement();

			Attributes attribs = result.getAttributes();

			String groupName = ((BasicAttribute) attribs.get("cn")).get().toString();
			BrokerFactory.getLoggingBroker().logDebug("Group name = "+groupName);
			Group group = BrokerFactory.getGroupMgmtBroker().getGroupByName(groupName);
			BrokerFactory.getLoggingBroker().logDebug("Group = "+group);
			if (group == null) {
				
				// find out whether to make it an escalation or a broadcast group
				boolean isEscalation = false;
				BasicAttribute objectClass = (BasicAttribute)attribs.get("objectClass");
				for (int oc = 0; oc < objectClass.size(); oc++) {
					String value = (String)objectClass.get(oc);
					BrokerFactory.getLoggingBroker().logDebug("OC["+oc+"="+value);
					if (value.equalsIgnoreCase("escalationgroup")) {
						isEscalation = true;
					}
				}
				if (isEscalation) {
					group = new EscalationGroup();
				} else {
					group = new BroadcastGroup();
				}
				
				// Set the name
				group.setGroupName(groupName);

				// set the description
				String description = ((BasicAttribute) attribs.get("description")).get().toString();
				if (description == null) {
					group.setDescription("Imported from the company directory");
				} else {
					group.setDescription(description);
				}

				try {
					BrokerFactory.getLoggingBroker().logDebug("Adding Group = "+group);
					BrokerFactory.getGroupMgmtBroker().addGroup(group);
				} catch (NotSupportedException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				}
			}
			group.setAutocommit(true);
			
						
			BasicAttribute memberList = (BasicAttribute) attribs.get("uniqueMember");
			BrokerFactory.getLoggingBroker().logDebug(groupName+" has "+memberList.size()+" members");
			for (int i = 0; i < memberList.size(); i++) {
				Object memberAttrib = memberList.get(i);
				BrokerFactory.getLoggingBroker().logDebug("Member attrib = "+memberAttrib);
				if (memberAttrib != null) {
					String memberCN = memberAttrib.toString();
					memberCN = memberCN.replaceAll(", ", ",");
					Member member = BrokerFactory.getUserMgmtBroker()
							.getUserByInformation("LDAP CN", memberCN);
					if (member != null) {
						Member[] existingMembers = group.getMembers();
						boolean isMember = false;
						for (int m = 0; m < existingMembers.length; m++) {
							if (existingMembers[m].equals(member))
								isMember = true;
						}
						if (!isMember) {
							try {
								group.addMember(member, existingMembers.length);
							} catch (InvalidGroupException e) {
								BrokerFactory.getLoggingBroker().logError(e);
							}
						}
					}
				}
			}
			BrokerFactory.getGroupMgmtBroker().updateGroup(group);

		}
	}

	public void execute(JobExecutionContext jeContext)
			throws JobExecutionException {
		BrokerFactory.getLoggingBroker().logDebug("Running LDAP Importer Job");
		if (!BrokerFactory.getConfigurationBroker().getBooleanValue(
				"ldap.import")) {
			BrokerFactory.getLoggingBroker().logDebug(
					"LDAP Importer turned off");
			return;
		}

		ConfigurationBroker config = BrokerFactory.getConfigurationBroker();
		String host = config.getStringValue("ldap.host");
		String userName = config.getStringValue("ldap.username");
		String password = config.getStringValue("ldap.password");
		searchString = config.getStringValue("ldap.searchString");
		base = config.getStringValue("ldap.base");

		ctx = new LDAPLibrary(host).getContext(userName, password);
		
		JobDataMap map = jeContext.getJobDetail().getJobDataMap();
		HashMap settings = new HashMap();

		String[] settingNames = BrokerFactory.getConfigurationBroker()
				.getParameterNames("ldap.setting.");
		for (int i = 0; i < settingNames.length; i++) {
			String ldapName = settingNames[i].substring(13, settingNames[i]
					.length());
			String className = BrokerFactory.getConfigurationBroker()
					.getStringValue(settingNames[i]);
			try {
				LDAPSetting setting = (LDAPSetting) (Class.forName(className)
						.newInstance());
				settings.put(ldapName, setting);
			} catch (InstantiationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IllegalAccessException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ClassNotFoundException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}

		// Add the start time
		long startMillis = System.currentTimeMillis();
		Date startDate = new Date();
		Vector startTimes = (Vector) map.get("starttimes");
		if (startTimes == null) {
			startTimes = new Vector();
		}
		startTimes.addElement(startDate);
		map.put("starttimes", startTimes);

		getAll(settings);
		
		long totalMillis = System.currentTimeMillis() - startMillis;

		BrokerFactory.getLoggingBroker().logDebug(
				"Finished importing from LDAP");
		// Add the run time
		Hashtable runTimes = (Hashtable) map.get("runtimes");
		if (runTimes == null) {
			runTimes = new Hashtable();
		}
		BrokerFactory.getLoggingBroker().logDebug(
				"LDAP Import ran for " + totalMillis + " millis");
		runTimes.put(startDate, new Long(totalMillis));
		map.put("runtimes", runTimes);
		jeContext.getJobDetail().setJobDataMap(map);
	}

	public void stop() {
		BrokerFactory.getLoggingBroker().logDebug(this + " stopped");
		stopped = true;
	}

	public void addUserToAD(User user) {
		try {
			String email = user.getEmailAddress();
			String name = email.substring(0, email.indexOf("@"));
			name = "cn=" + name + ",ou=People,dc=reliableresponse,dc=net";
			BasicAttribute objClasses = new BasicAttribute("objectclass");
			objClasses.add("person");
			objClasses.add("organizationalPerson");
			objClasses.add("user");

			BasicAttributes attrs = new BasicAttributes();
			attrs.put(objClasses);
			attrs.put("givenName", user.getFirstName());
			attrs.put("sn", user.getLastName());
			attrs.put("mail", user.getEmailAddress());
			attrs.put("userAccountControl", "66048");
			attrs.put("userPassword", name);

			ctx.createSubcontext(name, attrs);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}

	/**
	 * This is a class used to import ldapsearch dumps.
	 * 
	 * @param ldapDump
	 */
	public void addUserToAD(File ldapDump) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(ldapDump));
			String line;

			while ((line = in.readLine()) != null) {
				String accountControl = "66050";
				Hashtable table = new Hashtable();
				String name = line;

				if (name.indexOf(",") > 0) {
					name = name.substring(0, name.indexOf(","));
				}
				name += ",ou=People,dc=reliableresponse,dc=net";
				BasicAttribute objClasses = new BasicAttribute("objectclass");
								objClasses.add("person");
								objClasses.add("organizationalPerson");
								objClasses.add("inetOrgPerson");
				//				objClasses.add("user");

				BasicAttributes attrs = new BasicAttributes();
				//attrs.put(objClasses);
				while (((line = in.readLine()) != null) && (!line.equals(""))) {

					if ((line != null) && (line.indexOf("=") > 0)) {
						String key = line.substring(0, line.indexOf("="));
						String value = line.substring(line.indexOf("=") + 1,
								line.length());

						if (key.equals("objectClass")) {
							System.out.println("ObjClass value = " + value);
							//objClasses.add(value);
						} else if ((key.equals("sn")) 
								|| (key.equals("l"))
								|| (key.equals("cn"))
								|| (key.equals("mobile"))
								|| (key.equals("pager"))
								|| (key.equals("displayName"))
								|| (key.equals("initials"))
								|| (key.equals("title"))
								|| (key.equals("facsimileTelephoneNumber"))
								|| (key.equals("st"))
								|| (key.equals("postalCode"))
								|| (key.equals("streetAddress"))
								|| (key.equals("description"))
								|| (key.equals("givenName"))
								|| (key.equals("telephoneNumber"))
								|| (key.equals("mailNickName"))) {
							System.out.println("key = " + key);
							System.out.println("value = " + value);
							attrs.put(key, value);
						} else if (key.equals("mail")) {
							value = value.replaceAll("dexmedia.com", "reliableresponse.net");
							System.out.println("key = " + key);
							System.out.println("value = " + value);
							attrs.put(key, value);
						} else if (key.equals("userAccountControl")) {
							accountControl = value;
						}
					}

				}

				attrs.put(objClasses);
				try {
					ctx.createSubcontext(name, attrs);
					BrokerFactory.getLoggingBroker().logDebug(
							name + " added successfully");

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			BrokerFactory.getLoggingBroker().logError(e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(
				new FileInputStream("conf/reliable.properties"));

		LDAPImporter importer = new LDAPImporter(
				"uid=drig,ou=People,dc=reliableresponse,dc=net",
				"5isthe#", "localhost", "ou=People,dc=reliableresponse,dc=net", "cn=*");

		importer.addUserToAD(new File (args[0]));
	}
}

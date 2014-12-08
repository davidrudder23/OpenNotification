/*
 * Created on Sep 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationException;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.sender.NotificationSender;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;
import net.reliableresponse.notification.usermgmt.UnknownUser;
import net.reliableresponse.notification.util.StringUtils;

/**
 * @author drig
 * 
 * Copyright 2004 - David Rudder
 */
public abstract class GenericSQLNotificationBroker implements
		NotificationBroker {
	public abstract Connection getConnection();

	public void logConfirmation(Member confirmedBy, Notification notification) {
		String sql = "UPDATE notification SET confirmedby=?, status='confirmed' WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		String uuid = null;
		if ((!(confirmedBy instanceof UnknownUser)) && (confirmedBy != null))
			uuid = confirmedBy.getUuid();

		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, uuid);
				stmt.setString(2, notification.getUuid());
				stmt.executeUpdate();
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
	}

	public void logExpired(Notification notification) {
		String sql = "UPDATE notification SET status='expired' WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		String uuid = null;
		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, notification.getUuid());
				stmt.executeUpdate();
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
	}

	public void logEscalation(Member from, Member to, Notification notification) {
		logGenericEscalation(from, to, notification, false);
	}

	public void logPassed(Member from, Member to, Notification notification) {
		logGenericEscalation(from, to, notification, true);
	}

	/**
	 * @param from
	 * @param to
	 * @param notification
	 */
	private void logGenericEscalation(Member from, Member to,
			Notification notification, boolean passed) {
		String sql = "INSERT INTO escalationlog (notification, memberto, memberfrom, passed, escalationtime) values (?, ?, ?, ?, ?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, notification.getUuid());
			stmt.setString(2, to.getUuid());
			stmt.setString(3, from.getUuid());
			stmt.setString(4, passed ? "t" : "f");
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			stmt.executeUpdate();

		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}

	public void addNotification(Notification notification) {

		BrokerFactory.getLoggingBroker().logDebug(
				"Logging notification " + notification.getUuid());
		String sql = "INSERT INTO notification (uuid, senderclass, recipient, time, confirmedby, subject, owner, parent, senderinfo1, senderinfo2, senderinfo3, senderinfo4, senderinfo5, senderinfo6, senderinfo7, senderinfo8, senderinfo9, senderinfo10) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, notification.getUuid());
				stmt
						.setString(2, notification.getSender().getClass()
								.getName());
				stmt.setString(3, notification.getRecipient().getUuid());
				stmt.setTimestamp(4, new Timestamp(notification.getTime()
						.getTime()));
				stmt.setString(5, null);
				stmt.setString(6, notification.getSubject());
				stmt.setString(7, notification.getOwner());
				String parent = notification.getParentUuid();
				stmt.setString(8, parent);

				String[] senderInfo = notification.getSender().getVariables();
				for (int i = 0; i < senderInfo.length; i++) {
					stmt.setString(9 + i, senderInfo[i]);
				}
				for (int i = senderInfo.length; i < 10; i++) {
					stmt.setNull(9 + i, Types.VARCHAR);
				}

				stmt.executeUpdate();

				Vector options = notification.getOptions();

				for (int i = 0; i < options.size(); i++) {
					String option = (String) options.elementAt(i);

					PreparedStatement stmt2 = null;
					sql = "INSERT INTO notificationoptions(notification,optionname) VALUES(?,?)";
					try {
						stmt2 = connection.prepareStatement(sql);
						BrokerFactory.getLoggingBroker().logDebug(
								"sql=" + (sql));
						stmt2.setString(1, notification.getUuid());
						stmt2.setString(2, option);
						stmt2.executeUpdate();
					} catch (SQLException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} finally {
						try {
							if (stmt2 != null)
								stmt2.close();
						} catch (SQLException e1) {
							BrokerFactory.getLoggingBroker().logError(e1);
						}
					}
				}

				NotificationMessage[] messages = notification.getMessages();
				for (int i = 0; i < messages.length; i++) {
					addMessage(notification, messages[i]);
				}

			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
	}

	public void addMessage(Notification notification,
			NotificationMessage message) {

		String sql = "INSERT INTO notificationmessages(notification,message,addedby,addedon, contenttype, filename) VALUES(?,?,?,?,?, ?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			byte[] contents = message.getContent();
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, notification.getUuid());
			stmt.setBinaryStream(2, new ByteArrayInputStream(contents),
					contents.length);
			stmt.setString(3, message.getAddedby());
			stmt.setTimestamp(4, new Timestamp(message.getAddedon().getTime()));
			stmt.setString(5, message.getContentType());
			stmt.setString(6, message.getFilename());
			stmt.executeUpdate();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
	}

	public void addProviderInformation(Notification notification,
			NotificationProvider provider, Hashtable parameters, String status) {
		String sql = "INSERT INTO notificationprovider (uuid, notification, classname, status) values (?, ?, ?, ?)";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		String uuid = BrokerFactory.getUUIDBroker().getUUID(provider);
		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, uuid);
				stmt.setString(2, notification.getUuid());
				stmt.setString(3, provider.getClass().getName());
				stmt.setString(4, status);
				stmt.executeUpdate();

				Enumeration paramNames = parameters.keys();
				while (paramNames.hasMoreElements()) {
					String name = (String) paramNames.nextElement();
					String value = (String) parameters.get(name);

					PreparedStatement stmt2 = null;
					sql = "INSERT INTO notificationproviderinfo(provider,name,value) VALUES(?,?,?)";
					try {
						stmt2 = connection.prepareStatement(sql);
						BrokerFactory.getLoggingBroker().logDebug(
								"sql=" + (sql));
						BrokerFactory.getLoggingBroker().logDebug(
								"Adding notif inf, uuid=" + uuid + ", name="
										+ name + "value=" + value);
						stmt2.setString(1, uuid);
						stmt2.setString(2, name);
						stmt2.setString(3, value);
						stmt2.executeUpdate();
					} catch (SQLException e) {
						BrokerFactory.getLoggingBroker().logError(e);
					} finally {
						try {
							if (stmt2 != null)
								stmt2.close();
						} catch (SQLException e1) {
							BrokerFactory.getLoggingBroker().logError(e1);
						}
					}
				}

			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
	}

	public Notification getNotificationByUuid(String uuid) {
		String sql = getSQLBeginning() + "uuid=?";
		List<Notification> notifications = getNotificationsGeneric(uuid, sql);
		if ((notifications == null) || (notifications.size() == 0)) {
			return null;
		}
		return notifications.get(0);
	}
	
	public NotificationMessage[] getNotificationMessages(Notification notification) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();
		Vector<NotificationMessage> messages = new Vector<NotificationMessage>();

		String sql = "SELECT message,addedby,addedon, contenttype, filename FROM notificationmessages WHERE notification=? ORDER BY addedon";
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug(
					"sql=" + (sql));
			stmt.setString(1, notification.getUuid());
			rs = stmt.executeQuery();

			while (rs.next()) {
				InputStream simpleIn = rs.getBinaryStream(1);
				if (simpleIn != null) {
					String addedby = rs.getString(2);
					Timestamp addedon = rs.getTimestamp(3);
					BrokerFactory.getLoggingBroker().logDebug(
							"Loaded message " + notification.getUuid()
									+ " with timestamp hours "
									+ addedon.getHours());
					String contentType = rs.getString(4);

					StringBuffer message = new StringBuffer();
					byte[] contents = new byte[0];
					byte[] buf = new byte[1024];
					int size = 0;
					try {

						while ((size = simpleIn.read(buf, 0,
								buf.length)) > 0) {
							byte[] temp = new byte[contents.length];
							System.arraycopy(contents, 0, temp, 0,
									contents.length);
							contents = new byte[temp.length + size];
							System.arraycopy(temp, 0, contents, 0,
									temp.length);
							System.arraycopy(buf, 0, contents,
									temp.length, size);
						}
					} catch (IOException ioexc) {
						BrokerFactory.getLoggingBroker().logError(
								ioexc);
					}

					NotificationMessage notifMessage = new NotificationMessage(
							contents, addedby, new Date(addedon
									.getTime()), contentType);
					notifMessage.setFilename(rs.getString(5));
					messages.addElement(notifMessage);
				}
			}
		} catch (SQLException e2) {
			BrokerFactory.getLoggingBroker().logError(e2);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		return (NotificationMessage[])messages.toArray(new NotificationMessage[0]);
	}

	/**
	 * This is a generic loader, to keep from repeating the same code.
	 * 
	 * @param parameter
	 * @param sql
	 * @return
	 */
	private List<Notification> getNotificationsGeneric(Object parameter, String sql) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();
		Vector notifications = new Vector();

		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				if (parameter instanceof Timestamp) {
					stmt.setTimestamp(1, (Timestamp) parameter);
				} else {
					stmt.setString(1, (String) parameter);
				}
				rs = stmt.executeQuery();

				while (rs.next()) {
					String uuid = rs.getString(1);
					String senderClassname = rs.getString(2);
					String memberUuid = rs.getString(3);
					String confirmedBy = rs.getString(4);
					String subject = rs.getString(5);
					boolean requiresConfirmation = rs.getBoolean(6);
					String status = rs.getString(7);
					if (status == null) {
						status = "";
					} else {
						status = status.toLowerCase();
					}
					Timestamp time = rs.getTimestamp(8);
					String owner = rs.getString(9);
					String parent = rs.getString(10);
					String[] senderVariables = new String[10];
					for (int i = 0; i < 10; i++) {
						senderVariables[i] = rs.getString(11 + i);
					}

					Member member = BrokerFactory.getUserMgmtBroker()
							.getUserByUuid(memberUuid);
					if (member == null)
						member = BrokerFactory.getGroupMgmtBroker()
								.getGroupByUuid(memberUuid);

					Notification notification = new Notification(parent,
							member, null, subject, new NotificationMessage[0]);
					notification.setAutocommit(false);
					notification.setUuid(uuid);
					notification.setTime(new java.util.Date(time.getTime()));
					notification.setParentUuid(parent);
					notification.setOwner(owner);

					if (status == null) {
						notification.setStatus(Notification.NORMAL);
					} else if (status.equals("expired")) {
						notification.setStatus(Notification.EXPIRED);
					} else if (status.equals("confirmed")) {
						notification.setStatus(Notification.CONFIRMED);
					} else if (status.equals("pending")) {
						notification.setStatus(Notification.PENDING);
					} else if (status.equals("onhold")) {
						notification.setStatus(Notification.ONHOLD);
					} else if (confirmedBy != null) {
						notification.setStatus(Notification.CONFIRMED);
					} else if (requiresConfirmation) {
						notification.setStatus(Notification.PENDING);
					} else {
						notification.setStatus(Notification.NORMAL);
					}

					// Load the sender
					try {
						NotificationSender sender = (NotificationSender) Class
								.forName(senderClassname).newInstance();
						for (int i = 0; i < 10; i++) {
							sender.addVariable(i + 1, senderVariables[i]);
						}
						notification.setSender(sender);
					} catch (InstantiationException e1) {
						BrokerFactory.getLoggingBroker().logError(e1);
					} catch (IllegalAccessException e1) {
						BrokerFactory.getLoggingBroker().logError(e1);
					} catch (ClassNotFoundException e1) {
						BrokerFactory.getLoggingBroker().logError(e1);
					}

					notifications.addElement(notification);

					// Load the options
					PreparedStatement stmt2 = null;
					ResultSet rs2 = null;
					String sql2 = "SELECT optionname FROM notificationoptions WHERE notification=?";

					try {
						stmt2 = connection.prepareStatement(sql2);
						BrokerFactory.getLoggingBroker().logDebug(
								"sql=" + (sql2));
						stmt2.setString(1, uuid);
						rs2 = stmt2.executeQuery();

						while (rs2.next()) {
							notification.addOption(rs.getString(1));
						}
					} catch (SQLException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					} finally {
						try {
							if (stmt2 != null)
								stmt2.close();
							if (rs2 != null)
								rs2.close();
						} catch (SQLException e3) {
							BrokerFactory.getLoggingBroker().logError(e3);
						}
					}

					// Load the messages
					NotificationMessage[] messages = getNotificationMessages(notification);
					for (int msgNum = 0; msgNum < messages.length; msgNum++) {
						notification.addMessage(messages[msgNum], false);
					}
					// Load the providers
					sql2 = "SELECT uuid,classname,status FROM notificationprovider WHERE notification=?";
					try {
						stmt2 = connection.prepareStatement(sql2);
						BrokerFactory.getLoggingBroker().logDebug(
								"sql=" + (sql2));
						stmt2.setString(1, uuid);
						rs2 = stmt2.executeQuery();

						while (rs2.next()) {
							String providerUuid = rs2.getString(1);
							String classname = rs2.getString(2);
							String statusOfSend = rs2.getString(3);
							PreparedStatement stmt3 = null;
							ResultSet rs3 = null;
							String sql3 = "SELECT name,value FROM notificationproviderinfo WHERE provider=?";

							try {
								stmt3 = connection.prepareStatement(sql3);
								BrokerFactory.getLoggingBroker().logDebug(
										"sql=" + (sql3));
								stmt3.setString(1, providerUuid);
								rs3 = stmt3.executeQuery();

								Hashtable params = new Hashtable();
								while (rs3.next()) {
									String name = rs3.getString(1);
									if (name == null)
										name = "";
									String value = rs3.getString(2);
									if (value == null)
										value = "";
									params.put(name, value);
								}
								NotificationProvider provider = (NotificationProvider) (Class
										.forName(classname).newInstance());
								provider.init(params);
								provider.setStatusOfSend(notification,
										statusOfSend);
								notification.addNotificationProvider(provider);
							} catch (SQLException e3) {
								BrokerFactory.getLoggingBroker().logError(e3);
							} finally {
								try {
									if (stmt3 != null)
										stmt3.close();
									if (rs3 != null)
										rs3.close();
								} catch (SQLException e3) {
									BrokerFactory.getLoggingBroker().logError(
											e3);
								}
							}
						}

					} catch (SQLException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					} catch (NotificationException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					} catch (ClassNotFoundException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					} catch (InstantiationException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					} catch (IllegalAccessException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					} finally {
						try {
							if (stmt2 != null)
								stmt2.close();
							if (rs2 != null)
								rs2.close();
						} catch (SQLException e3) {
							BrokerFactory.getLoggingBroker().logError(e3);
						}
					}
					notification.setAutocommit(true);
				}
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
					if (rs != null)
						rs.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
		return notifications;
	}

	public List<Notification> getChildren(Notification parent) {
		String sql = getSQLBeginning() + "parent=?";
		BrokerFactory.getLoggingBroker().logDebug(
				"Looking for children of " + parent.getUuid());
		return getNotificationsGeneric(parent.getUuid(), sql);
	}

	public Member getConfirmedBy(Notification notification) {
		String sql = "SELECT confirmedby FROM notification WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, notification.getUuid());
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
			if (rs.next()) {
				String memberUuid = rs.getString(1);
				Member member = BrokerFactory.getUserMgmtBroker()
						.getUserByUuid(memberUuid);
				if (member == null) {
					member = BrokerFactory.getGroupMgmtBroker().getGroupByUuid(
							memberUuid);
				}
				return member;
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#setPageStatus(net.reliableresponse.notification.Notification,
	 *      java.lang.String)
	 */
	public void setNotificationStatus(Notification notification, String status) {
		String sql = "UPDATE notification SET status=? WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, status);
				stmt.setString(2, notification.getUuid());
				stmt.executeUpdate();
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
	}

	public List<Notification> getNotificationsSince(long since) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting notifs since " + since + ", current millis = "
						+ System.currentTimeMillis());
		if (since > System.currentTimeMillis()) {
			return getNotificationsSince(new Date(0));
		}
		return getNotificationsSince(new Date(System.currentTimeMillis()
				- since));
	}

	public List<Notification> getNotificationsSince(java.util.Date since) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting notifs since " + since);
		String sql = getSQLBeginning() + "time>?";
		return getNotificationsGeneric(new Timestamp(since.getTime()), sql);
	}

	public List<Notification> getUpdatedNotificationsTo(Member member,
			java.util.Date since) {
		List<String> uuids = getUpdatedUuidsTo(member, since);
		return uuids.stream().map(uuid->BrokerFactory.getNotificationBroker().getNotificationByUuid(uuid)).collect(Collectors.toList());
	}

	public List<String> getUpdatedUuidsTo(Member member, java.util.Date since) {
		List<String> uuids = new ArrayList<String>();
		String sql = "SELECT m.notification FROM notificationmessages m, notification n "
				+ "WHERE m.addedon>=? AND m.notification=n.uuid AND n.parent IS NULL AND n.recipient=? "
				+ "GROUP BY m.notification";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setTimestamp(1, new Timestamp(since.getTime()));
			stmt.setString(2, member.getUuid());
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
			while (rs.next()) {
				uuids.add(rs.getString(1));
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}

		return uuids;
	}

	public int deleteNotificationsBefore(java.util.Date before) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Deleting notifications before" + before);
		String sql = "DELETE FROM notification WHERE date<?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setTimestamp(1, new Timestamp(before.getTime()));
			return stmt.executeUpdate();
		} catch (SQLException sqlExc) {
			sqlExc.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
		return -1;
	}

	public List<Notification> getNotificationsBefore(java.util.Date before) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting notifs before " + before);
		String sql = getSQLBeginning() + "time<?";
		return getNotificationsGeneric(new Timestamp(before.getTime()), sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getPagesSentTo(net.reliableresponse.notification.usermgmt.Member)
	 */
	public List<Notification> getNotificationsSentBy(User user) {
		String sql = getSQLBeginning()
				+ "senderclass='net.reliableresponse.notification.sender.UserSender' AND senderinfo1=?";
		return getNotificationsGeneric(user.getUuid(), sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getPagesSentTo(net.reliableresponse.notification.usermgmt.Member)
	 */
	public List<Notification> getNotificationsSentTo(Member member) {
		String sql = getSQLBeginning() + "recipient=?";
		return getNotificationsGeneric(member.getUuid(), sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getAllUnconfirmedPages()
	 */
	public List<Notification> getAllUnconfirmedNotifications() {
		String sql = getSQLBeginning() + "status<>'confirmed' AND status<>?";
		return getNotificationsGeneric("expired", sql);
	}

	private String getSQLBeginning() {
		return "SELECT uuid, senderclass, recipient, confirmedby, subject, "
				+ "requiresconfirmation, status, time, owner, parent, "
				+ "senderinfo1, senderinfo2, senderinfo3, senderinfo4, senderinfo5, "
				+ "senderinfo6, senderinfo7, senderinfo8, senderinfo9, senderinfo10 "
				+ "FROM notification WHERE ";
	}

	private String getUuidsSQLBeginning() {
		return "SELECT uuid FROM notification WHERE ";
	}

	public int getNumPendingNotifications() {

		String sql = "SELECT COUNT(*) FROM notification WHERE status<>'confirmed' AND status<>'expired' AND time>?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()
					- (60 * 60 * 2 * 1000)));
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return 0;
	}

	public String getUltimateParentUuid(String child) {
		String sql = "SELECT parent FROM notification WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		String thisUuid = child;
		int count = 0;
		try {
			while (count < 20) {
				try {
					stmt = connection.prepareStatement(sql);
					stmt.setString(1, thisUuid);
					BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
					rs = stmt.executeQuery();
					if (rs.next()) {
						String tempUuid = rs.getString(1);
						if (StringUtils.isEmpty(tempUuid)) {
							return thisUuid;
						} else {
							thisUuid = tempUuid;
						}
					} else {
						return thisUuid;
					}
				} catch (SQLException e) {
					BrokerFactory.getLoggingBroker().logError(e);
				} finally {
					try {
						if (stmt != null)
							stmt.close();
						if (rs != null)
							rs.close();
					} catch (SQLException e1) {
						BrokerFactory.getLoggingBroker().logError(e1);
					}
				}
				count++;
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		sql = "UPDATE notification SET parent=null WHERE uuid=?";

		connection = getConnection();
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, thisUuid);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		BrokerFactory.getNotificationBroker().getNotificationByUuid(thisUuid)
				.setParentUuid(null);

		return thisUuid;
	}

	public int getNumNotifications() {
		String sql = "SELECT COUNT(*) FROM notification";
		PreparedStatement stmt = null;
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}
		}
		return 0;
	}

	public List<Notification> getAllPendingNotifications() {
		String sql = getSQLBeginning()
				+ "recipient like ? AND status<>'confirmed' AND status<>'expired'";
		return getNotificationsGeneric("%", sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getMembersUnconfirmedPages()
	 */
	public List<Notification> getMembersUnconfirmedNotifications(Member member) {
		String sql = getSQLBeginning()
				+ "recipient=? AND status<>'confirmed' AND status<>'expired'";
		return getNotificationsGeneric(member.getUuid(), sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getMembersPendingPages()
	 */
	public List<Notification> getMembersPendingNotifications() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getAllPendingUuids() {
		String sql = getUuidsSQLBeginning()+ "recipient like ? AND status<>'confirmed' AND status<>'expired'";
		return getUuidsGeneric("%", sql);
	}

	public List<String> getAllUnconfirmedUuids() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<String> getUuidsGeneric(Object parameter, String sql) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();
		Vector uuids = new Vector();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			if (parameter instanceof Timestamp) {
				stmt.setTimestamp(1, (Timestamp) parameter);
			} else {
				stmt.setString(1, (String) parameter);
			}
			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				uuids.addElement(uuid);
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
		return uuids;
	}

	public List<String> getChildrenUuids(Notification parent) {
		String sql = getUuidsSQLBeginning() + "parent=?";
		return getUuidsGeneric(parent.getUuid(), sql);
	}

	public List<String> getMembersPendingUuids() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getMembersUnconfirmedUuids(Member member) {
		String sql = getUuidsSQLBeginning()
				+ "recipient=? AND status<>'confirmed' AND status<>'expired'";
		return getUuidsGeneric(member.getUuid(), sql);
	}

	public List<String> getUuidsSentTo(Member member) {
		String sql = getUuidsSQLBeginning() + "recipient=?";
		return getUuidsGeneric(member.getUuid(), sql);
	}

	public List<String> getUuidsSentBy(User user) {
		String sql = "SELECT n.uuid FROM notification n WHERE "
				+ "n.senderclass='net.reliableresponse.notification.sender.UserSender' "
				+ "AND n.senderinfo1=? AND ((n.status<>'expired' AND n.status<>'confirmed') OR n.time>?)";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();
		Vector uuids = new Vector();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, user.getUuid());

			// Limit to 1 week
			stmt.setTimestamp(2, new java.sql.Timestamp(System
					.currentTimeMillis()
					- (1000 * 60 * 60 * 24 * 7)));
			rs = stmt.executeQuery();

			while (rs.next()) {
				String uuid = rs.getString(1);
				uuids.addElement(uuid);
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
		return uuids;
	}

	public List<String> getUuidsSince(java.util.Date since) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting notifs since " + new Timestamp(since.getTime()));
		String sql = getUuidsSQLBeginning() + "time>?";
		return getUuidsGeneric(new Timestamp(since.getTime()), sql);

	}

	public List<String> getUuidsBefore(java.util.Date before) {
		BrokerFactory.getLoggingBroker().logDebug(
				"Getting notifs before " + new Timestamp(before.getTime()));
		String sql = getUuidsSQLBeginning() + "time<?";
		return getUuidsGeneric(new Timestamp(before.getTime()), sql);

	}

	public List<String> getUuidsSince(long since) {
		if (since > System.currentTimeMillis()) {
			return getUuidsSince(new Date(0));
		}

		return getUuidsSince(new Date(System.currentTimeMillis() - since));
	}

	public String getEscalationStatus(Notification notification) {
		String sql = "SELECT passed from escalationlog where notification=?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, notification.getUuid());
			rs = stmt.executeQuery();

			if (rs.next()) {
				boolean passed = rs.getBoolean(1);
				if (passed)
					return "passed";
				return "expired";
			} else {
				Member confirmedBy = getConfirmedBy(notification);
				if ((confirmedBy != null)
						&& (confirmedBy.equals(notification.getRecipient()))) {
					return "confirmed";
				}
				return "hasn't responded";
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}

		return "unknown";
	}

	public void setOwner(Notification notification, String owner) {
		String sql = "UPDATE notification SET owner=? WHERE uuid=?";
		PreparedStatement stmt = null;
		Connection connection = getConnection();

		try {
			try {
				stmt = connection.prepareStatement(sql);
				BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
				stmt.setString(1, owner);
				stmt.setString(2, notification.getUuid());
				stmt.executeUpdate();
			} catch (SQLException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}
	}

	public java.util.Date getEarliestNotificationDate() {
		String sql = "SELECT MIN(time) FROM notification";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getTimestamp(1);
			} else {
				return new java.util.Date();
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}

		return new java.util.Date();
	}

	public int countPastNotifs(Member member, long pastMillis) {
		String sql = "SELECT count(*) FROM notification WHERE recipient=? AND time>?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = getConnection();

		try {
			stmt = connection.prepareStatement(sql);
			BrokerFactory.getLoggingBroker().logDebug("sql=" + (sql));
			stmt.setString(1, member.getUuid());
			stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()
					- pastMillis));
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e1) {
				BrokerFactory.getLoggingBroker().logError(e1);
			}

		}

		return 0;
	}

}

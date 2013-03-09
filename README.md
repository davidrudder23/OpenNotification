OpenNotification
================

OpenNotification is the open-source version of Reliable Response Notification.  It is an IT Emergency Notification server.-- Description
OpenNotification is an IT Emergency Notification service.  It started out as Reliable Response Notification and was opensourced by David Rudder on September 9th, 2012.  OpenNotification is used to provide emergency response to IT outages.  It accepts new alerts over SMTP, HTTP and email.  It then maps that alert to a group and manages the escalation chain, handles updates and can even update the originating server.  OpenNotification operates in either uni-directional, where the alert comes in and is handled exclusively by OpenNotification, or bi-directional, where OpenNotification sends updates back to the originating monitoring server.  OpenNotification has bi-directional support for Nagios, GroundWork, OpenView, JIRA, ProActiveNet and has an extensible system for adding new bi-directional integrations.  OpenNotification can alert over email, telephone (using Twilio or VoiceShot), SMS (using a MultiShot SMS modem), Jabber/GTalk, AIM, YahooIM, SameTime IM, or Alpha-numeric Pager.

Requirements
============
Java 1.5 or greater
10GB free space
A supported RDBMS (mysql, postgresql, IBM DB2, MS SqlServer, Oracle 10.x or higher)
An internet-available IP is required for some setups

Usage
=====
Startup is provided by the underlying Tomcat application server.  Please refer to the Tomcat documentation.  After startup, all operations are available via a web page.

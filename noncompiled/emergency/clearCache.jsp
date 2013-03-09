<%@ page import="net.reliableresponse.notification.broker.*" %>
<%@ page import="net.reliableresponse.notification.broker.impl.caching.*" %>
<%
	((CachingNotificationBroker)BrokerFactory.getNotificationBroker()).getCache().clear();
	((CachingUserMgmtBroker)BrokerFactory.getUserMgmtBroker()).getCache().clear();
	((CachingGroupMgmtBroker)BrokerFactory.getGroupMgmtBroker()).getCache().clear();
	%>
Caches cleared
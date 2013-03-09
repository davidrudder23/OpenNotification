/*
 * Created on Sep 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.reliableresponse.notification.NotSupportedException;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.device.Device;
import net.reliableresponse.notification.device.DeviceSetting;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class EditDeviceServlet extends HttpServlet {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(arg0, arg1);
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action=request.getParameter("action");
		if ((action == null) || (action.length() == 0)) {
			action = "edit";
		}
		
		String memberUuid = request.getParameter("user");
		if ((memberUuid == null) || (memberUuid.length()<1)) {
			request.getSession().setAttribute("Error", "Please enter a user who's device to edit");
			response.sendRedirect("index.jsp?notification=usermgmt");
			return;
		}
		User user = BrokerFactory.getUserMgmtBroker().getUserByUuid(memberUuid);
		
		if (action.equals("edit")) {
			String deviceUuid = request.getParameter("device");
			if ((deviceUuid == null) || (deviceUuid.length() == 0)) {
				request.getSession().setAttribute("Error", "Please enter a device to delete");
				response.sendRedirect("index.jsp?notification=usermgmt");
				return;
			}
			
			Device device = user.getDeviceWithUuid(deviceUuid);
			
			if (device == null) {
				request.getSession().setAttribute("Error", "Please enter a device to edit");
				response.sendRedirect("index.jsp?notification=usermgmt");
				return;
			}
			
			Hashtable settings = device.getSettings();
			Enumeration names = settings.keys();
			
			while (names.hasMoreElements()) {
				String name = (String)names.nextElement();
				
				String value = request.getParameter(name);
				if (value != null) {
					BrokerFactory.getLoggingBroker().logDebug("Setting "+user+"'s "+device+"'s "+
							name+" to "+value);
					
					settings.put (name, value);
				}
			}
			device.initialize(settings);
			try {
				BrokerFactory.getUserMgmtBroker().updateUser(user);
			} catch (NotSupportedException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		} else if (action.equals ("delete")) {
			String deviceUuid = request.getParameter("device");
			if ((deviceUuid == null) || (deviceUuid.length() == 0)) {
				request.getSession().setAttribute("Error", "Please enter a device to delete");
				response.sendRedirect("index.jsp?notification=usermgmt");
				return;
			}
			
			Device device = user.getDeviceWithUuid(deviceUuid);
			
			if (device == null) {
				request.getSession().setAttribute("Error", "Please enter a device to delete");
				response.sendRedirect("index.jsp?notification=usermgmt");
				return;
			}
			
			user.removeDevice(device);
		} else if (action.equals ("add")) {
			if (user == null) {
				request.getSession().setAttribute("Error", "Please enter a user who's device to edit");
				response.sendRedirect("index.jsp?notification=addDevice&user="+memberUuid);
				return;
			}

			String deviceClass = request.getParameter("deviceClass");
			if ((deviceClass == null) || (deviceClass.length() == 0)) {
				request.getSession().setAttribute("Error", "Please enter a device type to add");
				response.sendRedirect("index.jsp?notification=addDevice&user="+memberUuid);
				return;
			}
			
			try {
				Device device = (Device)Class.forName(deviceClass).newInstance();
				Hashtable options = new Hashtable();
				DeviceSetting[] settings = device.getAvailableSettings();
				for (int i = 0; i < settings.length; i++) {
					String value = request.getParameter(settings[i].getName());
					BrokerFactory.getLoggingBroker().logDebug("Adding option "+settings[i].getName()+":"+value);
					if (value != null) {
						options.put (settings[i].getName(), value);
					}
				}
				device.initialize(options);
				BrokerFactory.getLoggingBroker().logDebug("EditDevice Adding device "+device+" to "+user);
				user.addDevice(device);

				request.getSession().setAttribute("Info", device.getName()+" added to "+ user);
				BrokerFactory.getLoggingBroker().logDebug("Redirecting to usermgmt");
				response.sendRedirect("index.jsp?notification=usermgmt&user="+memberUuid);
				return;
			} catch (InstantiationException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (IllegalAccessException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (ClassNotFoundException e) {
				BrokerFactory.getLoggingBroker().logError(e);
			} catch (Exception e) {
				BrokerFactory.getLoggingBroker().logError(e);
			}
		}
		
		response.sendRedirect("index.jsp?notification=usermgmt");
	}
}
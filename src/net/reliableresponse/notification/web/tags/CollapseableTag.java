/*
 * Created on Oct 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.web.tags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CollapseableTag extends BodyTagSupport {

	public static String TOGGLE_COMMAND = "toggle_collapseable";
	public static String MAXIMIZE_COMMAND = "maximize_collapseable";
	public String title;
	public String tag;
	public String contentURL;
	public String color;
	public boolean isOpened;
	public String tagClass;
	public boolean opened;
	private boolean toggledOpen;
	
	public CollapseableTag() {
		isOpened = false;
	}
	
	private String getVariable (String baseName) {
		try {
			return baseName+"."+URLEncoder.encode(getTag(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			BrokerFactory.getLoggingBroker().logError(e);
			return baseName+"."+getTitle();
		}
	}
	

	public int doStartTag() throws JspException {
		
		try {
			JspWriter out = pageContext.getOut();
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();

			String isOpenedString = request.getParameter(getVariable("opened"));
			if (isOpenedString == null) {
				isOpenedString = isOpened()?"true":"false";
			}
			toggledOpen = isOpenedString.toLowerCase().startsWith("t");
			
			if (request.getParameter("action_"+getVariable(TOGGLE_COMMAND)+".x") != null) {
				toggledOpen = !toggledOpen;
			}
			
			// Write out the anchor
			out.write ("<a name=\"");
			out.write (getTag());
			out.write ("\"></a>");
			
			// Write out the variable which stores whether the
			// tag is opened or not
			out.write ("<input type=\"hidden\" name=\"");
			out.write (getVariable("opened"));
			if (toggledOpen) {
				out.write ("\" value=\"true\"\">\n\n");
			} else {
				out.write ("\" value=\"false\"\">\n\n");
			}
			
			writeTab(out);
			
			if (toggledOpen) {
				try {
					out.write ("<!-- Start included content from "+getContentURL()+" -->\n");
					if (getTagClass().equals ("headercell")) {
						out.write("<tr><td colspan=\"25\">\n\n");
						out.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
						///out.write("<tr><td class=\"abovecell\" colspan=\"4\"></td></tr>\n\n");
					} else if (getTagClass().equals ("cellrule")) {
						out.write ("<tr><td valign=\"top\" width=\"11\"><img src=\"images/spacer.gif\" width=\"1\" height=\"1\"></td>");
					} else {
						out.write ("<tr><td colspan=\"25\">");
					}
					try {
						pageContext.include(getContentURL());
					} catch (RuntimeException e2) {
						BrokerFactory.getLoggingBroker().logError(e2);
					}
					if (getTagClass().equals("headercell")) {
						out.write ("</table>\n");
					}
					out.write ("</td></tr>\n");
					out.write ("<!-- End included content from "+getContentURL()+" -->\n");
				} catch (ServletException e1) {
					BrokerFactory.getLoggingBroker().logError(e1);
				}
			} 
			
			if ((!getTagClass().equals("plain")) && (!getTagClass().equals("individualgroup"))) {
				out.write ("</table></td></tr>\n");
			}
			
			
		} catch (IOException e) {
			BrokerFactory.getLoggingBroker().logError(e);
		}
		return super.doStartTag();
	}
	

	/**
	 * @param out
	 * @throws IOException
	 */
	private void writeTab(JspWriter out) throws IOException {
		out.write ("<!-- Start collapseable tab "+getContentURL()+" -->\n");
		
		if (getTagClass().equals ("headercell")) {
			out.write("<tr><td colspan=\"4\">\n\n");
			out.write("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maintable\">\n");
			out.write("<tr><td class=\"headercell\"><input type=\"image\" border=\"0\" name=\"action_"
					+ getVariable(TOGGLE_COMMAND) + "\" src=\"images/");
			out.write(toggledOpen ? "arr_openbl" : "arr_clsdbl");
			out.write(".gif\">\n");

			// Show the title
			out.write(getTitle());

//			out.write("</td>\n</tr>\n");
			// Write out the help button
			out.write ("<td class=\"headercell\" align=\"right\"><a href=\"http://www.reliableresponse.net/dokuwiki/doku.php?id=");
			String wikiPage = getTag().toLowerCase().replace(" ", "_"); 
			out.write (wikiPage);
			out.write ("\" target=\"help\">");
			out.write ("<img src=\"images/btn_help.gif\" border=\"0\"></a></td>");
			out.write("</tr>\n");
		} else if (getTagClass().equals ("individualgroup")) {
				out.write("<tr><td><input type=\"image\" border=\"0\" name=\"action_"
						+ getVariable(TOGGLE_COMMAND) + "\" src=\"images/");
				out.write(toggledOpen ? "arr_openbl" : "arr_clsdbl");
				out.write(".gif\">\n");

				// Show the title
				out.write(getTitle());

//				out.write("</td>\n</tr>\n");
				out.write("</tr>\n");
		} else if (getTagClass().equals("cellrule")) {
			out.write ("<tr><td colspan=\"25\"><table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
			out.write ("<tr><td width=\"12\">");
			out.write ("<input type=\"image\" border=\"0\" name=\"action_"+getVariable(TOGGLE_COMMAND)+"\" src=\"images/");
			out.write (toggledOpen?"arr_openbl":"arr_clsdbl");
			out.write (".gif\">\n");
			
			out.write ("</td>\n<td class=\"cellrule\" width=\"%100\" colspan=\"25\">");

			// Show the title
			out.write (getTitle());
				
			out.write ("</td>\n</tr>\n");
		} else if (getTagClass().equals("plain")) {
			
			String color=getColor();
			
			out.write ("<tr bgcolor=\""+color+"\"><td width=\"12\">");
			out.write ("<input type=\"image\" border=\"0\" name=\"action_"+getVariable(TOGGLE_COMMAND)+"\" src=\"images/");
			out.write (toggledOpen?"arr_openbl":"arr_clsdbl");
			out.write (".gif\">\n");
			
			out.write ("</td>\n");

			// Show the title
			out.write (getTitle());
			out.write ("</tr>\n");
		}
		
		out.write ("<!-- End collapseable tab "+getContentURL()+" -->\n");
	}

	/**
	 * @return Returns the contentName.
	 */
	public String getContentURL() {
		return contentURL;
	}
	/**
	 * @param contentName The contentName to set.
	 */
	public void setContentURL(String contentName) {
		this.contentURL = contentName;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return Returns the color.
	 */
	public String getColor() {
		if (color == null) {
//			return "#EDEDED";
			return "#FFFFFF";
		}
		return color;
	}
	/**
	 * @param color The color to set.
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	
	public String getTagClass() {
		if (tagClass == null) tagClass = "headercell";
		return tagClass;
	}
	public void setTagClass(String tagClass) {
		this.tagClass = tagClass;
	}
	
	
	public boolean isOpened() {
		return isOpened;
	}
	public void setOpened(boolean isOpened) {
		this.isOpened = isOpened;
	}
}

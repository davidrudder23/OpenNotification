<%@ page import="java.util.Date" %>
<%@ page import="net.reliableresponse.notification.license.LicenseFile" %>
<%
	int year = new Date().getYear()+1900;
%>
  </table>
 <tr bgcolor="#999999">
    <td colspan="5"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td><img src="images/spacer.gif" width="3" height="3"></td>
    <%
    	LicenseFile licenseFile = LicenseFile.getInstance();
    	if ((!licenseFile.getInstallClass().equalsIgnoreCase("enterprise"))  && (licenseFile.isValid())) {
    	%><td>Demo Version expires on <%= licenseFile.getFormattedDate(licenseFile.getValidTo()) %></td><%
    	}
    %>
    <td>
    <td align="right" class="copyright" colspan="3"><a href="copyright.html">&copy; Copyright <%= year %> </a>Reliable Response, LLC. All rights reserved.  
    <img src="images/spacer.gif" width="3" height="3"><img src="images/spacer.gif" width="5" height="10"></td>
    <td align="right" bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  <tr>
    <td colspan="5" bgcolor="#999999"><img src="images/spacer.gif" width="3" height="3"></td>
  </tr>
  </table>
</form>
</body>
</html>
<!-- 
I think my IP address is <%= request.getLocalAddr() %>
 -->

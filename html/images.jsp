<%@ page import="java.util.Map" %>
<%
/**
This JSP file is used to support JaspReport's image handling
*/

	Map imagesMap = (Map)request.getSession().getAttribute("IMAGES_MAP");
         
    if (imagesMap != null)
    {
    	String imageName = request.getParameter("image");
        if (imageName != null)
        {
        	byte[] imageData = (byte[])imagesMap.get(imageName);
        	response.setContentType("image/gif");
         	response.setContentLength(imageData.length);
            ServletOutputStream ouputStream = response.getOutputStream();
            ouputStream.write(imageData, 0, imageData.length);
            ouputStream.flush();
            ouputStream.close();
		}
    }
%>
                                
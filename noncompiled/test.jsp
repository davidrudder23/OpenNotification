
<jsp:directive.page import="java.io.InputStream"/>
<jsp:directive.page import="java.io.OutputStream"/><%

InputStream in = request.getInputStream();
OutputStream outstream = response.getOutputStream();
int ch = 0;

while (ch >= 0) {
	outstream.write (ch);
}
%>
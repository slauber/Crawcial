<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.social.FbServlet" %>
<%@ page import="facebook4j.Account" %>
<%@ page import="facebook4j.ResponseList" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<h1>Facebook pages</h1>
<% if (AuthHelper.isAuthenticated(request)) {
    String fbtoken = null;
    Cookie[] c = request.getCookies();
    if (c != null) {
        for (Cookie cs : c) {
            if (cs.getName().equals("fbtoken")) {
                fbtoken = new String(Base64.decodeBase64(cs.getValue()));
            }
        }
    }
    if (fbtoken == null) {
%>
<form action="fbauth" method="post">
    <button><img alt="Facebook login" width=215 src="img/facebook.png"/></button>
</form>
<%
    }
    ResponseList<Account> accounts = FbServlet.getPages(request);
    if (accounts != null) {
%><p>Click to install crawcial on a page</p><%
    for (Account a : accounts) {%>
<a href="facebook?action=enablePage&pageid=<%=a.getId()%>"><%=a.getName()%> - ID: <%=a.getId()%>
</a><br><%
            }
        }
    }
%>
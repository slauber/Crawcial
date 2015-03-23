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
%><p>Click to install Crawcial on a page</p><%
    for (Account a : accounts) {%>
<a href="facebook?action=enablePage&pageid=<%=a.getId()%>"><%=a.getName()%> - ID: <%=a.getId()%>
</a>

<form action="facebook" method="post">
    <button>Download posts for <%=a.getName()%>
    </button>
    <input type="hidden" name="action" value="staticLoader">
    <input type="hidden" name="pageid" value="<%=a.getName()%>">
</form>
<br>
<%
    }%>
<div style="text-align: center; vertical-align: middle;width: 340px;">
    <form class="crawcial-login-form" action="facebook" method="post">

        <div class="crawcial-form-row">
            <input class="crawcial-form-large" type="text" name="callback" value=""
                   placeholder="Callback URL" autofocus="">
        </div>
        <div class="crawcial-form-row">
            <button>Setup callback</button>
        </div>
        <input type="hidden" name="action" value="setupSubscriptions">
    </form>
</div>
<%
        }
    }
%>
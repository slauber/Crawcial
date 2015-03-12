<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.social.TwitterServlet" %>
<%@ page import="de.crawcial.web.util.Modules" %>
<%@ page import="de.crawcial.web.util.Tokenmanager" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<h2>CraTwitter</h2>
<%if (TwitterServlet.isRunning() && AuthHelper.isAuthenticated(request)) {%>
<p><%=TwitterServlet.getStatus()%>
</p>
<% if (TwitterServlet.isShuttingDown()) {%>
<strong>CraTwitter is shutting down</strong><%} else {%>
<strong>CraTwitter is active</strong>

<form class="crawcial-login-form" action="twitter" method="post">
    <button>Shutdown now</button>
    <input type="hidden" name="action" value="shutdown">
</form>
<%
        }
    }%>
<% if (Tokenmanager.getTwitterOAuth(request) != null && AuthHelper.isAuthenticated(request)) {
    if (!TwitterServlet.isRunning()) {%>
<form class="crawcial-login-form" action="twitter" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="terms"
               value=""
               placeholder="Filter terms" autofocus="">
    </div>
    <div class="crawcial-form-row">
        <button>Go</button>
    </div>
    <input type="hidden" name="action" value="persist">
</form>
<% }
} else {
%><strong>CraTwitter is not ready. Please check your <a href="<%=Modules.CONFIGURATION%>">tokens</a> and <a
        href="<%=Modules.LOGIN%>">twitter login</a>.</strong>
<%}%>
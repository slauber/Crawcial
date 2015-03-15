<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.social.TwServlet" %>
<%@ page import="de.crawcial.web.util.Tokenmanager" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<h2>CraTwitter</h2>
<%if (TwServlet.isRunning() && AuthHelper.isAuthenticated(request)) {%>
<p><%=TwServlet.getStatus()%>
</p>
<% if (TwServlet.isShuttingDown()) {%>
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
    if (!TwServlet.isRunning()) {%>
<a href="twitter?action=trends">Preload terms with current world wide trending topic (it is not recommended to crawl
    them all)</a>

<form class="crawcial-login-form" action="twitter" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="terms"
               value="
               <%if(request.getParameter("trends")!=null){%><%=new String(Base64.decodeBase64(request.getParameter("trends")))%><%}%>"
               placeholder="Filter terms" autofocus="">
    </div>
    <div>
        <input type="checkbox" name="media" id="media" value="true"/> Persist media (consumes more bandwidth and
        storage)
    </div>
    <div class="crawcial-form-row">
        <button>Go</button>
    </div>
    <input type="hidden" name="action" value="persist">
</form>
<% }
} else {
%><strong>CraTwitter is not ready. Please check your <a href="<%=Constants.CONFIGURATION%>">tokens</a> and <a
        href="<%=Constants.LOGIN%>">twitter login</a>.</strong>
<%}%>
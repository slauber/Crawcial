<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.util.Tokenmanager" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% Map<String, String> values;
    if (AuthHelper.isAuthenticated(request)) {
        values = Tokenmanager.getSocialToken(request);
%>
<form class="crawcial-login-form" action="tokenmgr" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="fbappid"
               value="<%=values.containsKey("fbappid")?values.get("fbappid"):""%>"
               placeholder="Facebook App ID" autofocus="">
    </div>
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="fbappsecret"
               value="<%=values.containsKey("fbappsecret")?values.get("fbappsecret"):""%>"
               placeholder="Facebook App Secret">
    </div>
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="twconsumerkey"
               value="<%=values.containsKey("twconsumerkey")?values.get("twconsumerkey"):""%>"
               placeholder="Twitter Consumer Key">
    </div>
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="twconsumersecret"
               value="<%=values.containsKey("twconsumersecret")?values.get("twconsumersecret"):""%>"
               placeholder="Twitter Consumer Secret">
    </div>
    <div class="crawcial-form-row">
        <button <%=!AuthHelper.isAuthenticated(request) ? "disabled" : ""%>>Save</button>
    </div>
    <input type="hidden" name="action" value="update">
</form>
<%
} else { %>
<h2>Not authenticated</h2>
<% }
%>

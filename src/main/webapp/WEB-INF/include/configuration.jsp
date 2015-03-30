<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.util.Tokenmanager" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% Map<String, String> values;
    if (AuthHelper.isAuthenticated(request)) {
        values = Tokenmanager.getSocialToken(request);
        if (request.getParameter("e") != null && Integer.valueOf(request.getParameter("e")).equals(Constants.TWITTER_ERROR)) {%>
<p style="color: crimson;font-weight: bold">Crawcial could not connect to Twitter, please check your consumer key and
    secret.</p>
<%}%>
<p>In order to work properly, Crawcial needs some information about its corresponding apps on Facebook and Twitter.</p>

<form action="tokenmgr" method="post">
    <p><a href="/apptutorial.jsp" target="_blank">Click here to learn how to get these values</a></p>

    <div>
        <input type="text" name="fbappid"
               value="<%=values.containsKey("fbappid")?values.get("fbappid"):""%>"
               placeholder="Facebook App ID" autofocus="">
    </div>
    <div>
        <input type="text" name="fbappsecret"
               value="<%=values.containsKey("fbappsecret")?values.get("fbappsecret"):""%>"
               placeholder="Facebook App Secret">
    </div>
    <div>
        <input type="text" name="twconsumerkey"
               value="<%=values.containsKey("twconsumerkey")?values.get("twconsumerkey"):""%>"
               placeholder="Twitter Consumer Key">
    </div>
    <div>
        <input type="text" name="twconsumersecret"
               value="<%=values.containsKey("twconsumersecret")?values.get("twconsumersecret"):""%>"
               placeholder="Twitter Consumer Secret">
    </div>
    <div>
        <button <%=!AuthHelper.isAuthenticated(request) ? "disabled" : ""%>>Save</button>
    </div>
    <div>
        <input type="text" name="fbverifytoken"
               value="<%=values.containsKey("fbverifytoken")?values.get("fbverifytoken"):""%>"
               placeholder="Facebook Verify Token">
    </div>
    <input type="hidden" name="action" value="update">
</form>
<%
} else { %>
<h2>Not authenticated</h2>
<% }
%>

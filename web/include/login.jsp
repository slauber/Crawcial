<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.auth.UserServlet" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% String twtoken = null;
    String fbtoken = null;
    Cookie[] c = request.getCookies();
    if (c != null) {
        for (Cookie cs : c) {
            if (cs.getName().equals("twtoken")) {
                twtoken = new String(Base64.decodeBase64(cs.getValue()));
            }
            if (cs.getName().equals("fbtoken")) {
                fbtoken = new String(Base64.decodeBase64(cs.getValue()));
            }
        }
    }
%>
<% if (!AuthHelper.isAuthenticated(request)) {%>
<div class="crawcial-login-container" style="text-align: center; vertical-align: middle;width: 340px;">

    <img class="uk-margin-bottom" src="img/crawcial.png" width="256" height="256" alt="Crawcial">

    <form class="crawcial-login-form" action="auth" method="post">

        <div class="crawcial-form-row">
            <input class="crawcial-form-large" type="text" name="user" value=""
                   placeholder="Username" autofocus="">
        </div>
        <div class="crawcial-form-row">
            <div class="crawcial-form-password">
                <input class="crawcial-form-large" type="password" name="password" value=""
                       placeholder="Password">
            </div>
        </div>
        <div class="crawcial-form-row">
            <button>Login</button>
        </div>
        <div class="crawcial-form-row">
            <!--<label class="uk-float-left"><input type="checkbox" name="_remember_me">Remember me</label> -->
        </div>

        <input type="hidden" name="action" value="signin">
    </form>
</div>
<%
} else {
    if (UserServlet.isAdminParty(request.getServletContext())) {
%>
It's admin party!
<%}%>
You are logged in<%}%>
<% if (fbtoken == null) {%>
<form action="fbauth" method="post">
    <button><img alt="Facebook login" width=215 src="img/facebook.png"/></button>
</form>
<% }
    if (twtoken == null) {%>
<form action="twauth" method="post">
    <button><img alt="Twitter login" width=215 src="img/twitter.png"/></button>
</form>
<%
    }
%>
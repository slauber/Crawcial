<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.auth.UserServlet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% if (!AuthHelper.isAuthenticated(request)) {%>
<div class="crawcial-login-container" style="text-align: center; vertical-align: middle;width: 340px;margin: 0 auto;">

    <img src="img/crawcial.png" width="256" height="256" alt="Crawcial">

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
        <input type="hidden" name="action" value="signin">
    </form>
</div>
<%
} else {%>
<form action="auth" method="post"><input type="hidden" name="action" value="signout">
    <input type="submit" value="Sign out"></form>
<%
    if (UserServlet.isAdminParty(request.getServletContext())) {
%>
It's admin party!
<%}%>
You are logged in<%}%>
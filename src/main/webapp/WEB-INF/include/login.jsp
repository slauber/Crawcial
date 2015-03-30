<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.auth.UserServlet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% if (!AuthHelper.isAuthenticated(request)) {%>
<div style="text-align: center; vertical-align: middle;width: 340px;">

    <img src="img/crawcial.png" width="256" height="256" alt="Crawcial">

    <form action="auth" method="post">

        <div>
            <input type="text" name="user" placeholder="Username" autofocus="">
        </div>
        <div>
            <input type="password" name="password" placeholder="Password">
        </div>
        <div>
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
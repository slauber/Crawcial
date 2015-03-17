<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.auth.CrawcialUser" %>
<%@ page import="de.crawcial.web.auth.UserServlet" %>
<%@ page import="java.util.List" %>
<% if (AuthHelper.isAuthenticated(request)) {%>
<h2>Create new user</h2>
<% if (request.getParameter("e") != null && Integer.valueOf(request.getParameter("e")).equals(Constants.USER_ERROR)) { %>
<div><h3>Could not perform update</h3></div>
<%}%>
<form class="crawcial-login-form" action="user" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" name="user"
               value=""
               placeholder="Username" autofocus="">
    </div>
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="password" name="password"
               value=""
               placeholder="Password">
    </div>
    <div class="crawcial-form-row">
        <button>Create new user</button>
    </div>
    <input type="hidden" name="action" value="adduser">

</form>
<form class="crawcial-login-form" action="user" method="post">
    <div class="crawcial-form-row">
        <button>Delete current user</button>
    </div>
    <input type="hidden" name="action" value="deluser">
</form>
<div>
    <ul>
        <% List<CrawcialUser> user = UserServlet.getUserlist(getServletConfig().getServletContext());
            if (user != null) {%>
        User accounts:<%
        for (CrawcialUser u : user) {
    %>
        <li><%=u.getName()%>
            <form action="user" method="post">
                <button name="delusername" value="<%=u.getName()%>">Delete</button>
                <input type="hidden" name="action" value="deluser"></form>
        </li>
        <%
                }
            }
        %>
    </ul>
</div>
<% } else {
    response.sendRedirect(Constants.LOGIN);
} %>
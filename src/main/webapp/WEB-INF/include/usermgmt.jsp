<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.auth.CrawcialUser" %>
<%@ page import="de.crawcial.web.auth.UserServlet" %>
<%@ page import="java.util.List" %>

<script type="text/javascript">
    function checkPass() {
        var user = document.getElementById('username');
        var pass1 = document.getElementById('password');
        var pass2 = document.getElementById('password1');
        var button = document.getElementById('createUser');
        var goodColor = "#55aa55";
        var badColor = "#cc6666";
        if (pass1.value == pass2.value) {
            pass2.style.backgroundColor = goodColor;
            button.disabled = true;
            if (pass1.value.length > 3 && pass2.value.length > 3 && user.value.length > 3) {
                user.style.backgroundColor = goodColor;
                button.disabled = false;
            } else {
                user.style.backgroundColor = badColor;
            }
        } else {
            button.disabled = true;
            pass2.style.backgroundColor = badColor;
        }
    }
</script>


<% if (AuthHelper.isAuthenticated(request)) {%>
<h2>Create new user</h2>
<% if (request.getParameter("e") != null && Integer.valueOf(request.getParameter("e")).equals(Constants.USER_ERROR)) { %>
<div><h3>Could not perform update</h3></div>
<%}%>
<form action="user" method="post">
    <div>
        <input type="text" name="user"
               value=""
               placeholder="Username" autofocus="" id="username">
    </div>
    <div>
        <input type="password" name="password"
               value=""
               placeholder="Password" onkeyup="checkPass(); return false;" id="password">
    </div>
    <div>
        <input type="password" name="password2"
               value=""
               placeholder="Confirm password" onkeyup="checkPass(); return false;" id="password1">
    </div>
    <div>
        <button disabled id="createUser">Create new user</button>
    </div>
    <input type="hidden" name="action" value="adduser">

    <div>User name and password require at least four characters</div>
</form>
<form action="user" method="post">
    <div>
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
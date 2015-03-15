<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<nav>
    <ul>
        <li>
            <a href="<%=Constants.HOME%>">Home</a>
        </li>
        <li>
            <a href="/<%=Constants.LOGIN%>">Login</a>
        </li>
        <% if (AuthHelper.isAuthenticated(request)) {%>
        <li>
            <a href="/<%=Constants.FACEBOOK%>">Facebook</a>
        </li>
        <li>
            <a href="/<%=Constants.TWITTER%>">Twitter</a>
        </li>
        <li>
            <a href="/<%=Constants.CONFIGURATION%>">Configuration</a>
        </li>
        <li>
            <a href="/<%=Constants.USERMGMT%>">User management</a>
        </li>
        <% }%>
    </ul>
</nav>
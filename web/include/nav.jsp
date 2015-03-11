<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.util.Modules" %>
<nav>
    <ul>
        <li>
            <a href="<%=Modules.HOME%>">Home</a>
        </li>
        <li>
            <a href="/<%=Modules.LOGIN%>">Login</a>
        </li>
        <% if (AuthHelper.isAuthenticated(request)) {%>
        <li>
            <a href="/<%=Modules.TWITTER%>">Twitter Crawler</a>
        </li>
        <li>
            <a href="/<%=Modules.CONFIGURATION%>">Configuration</a>
        </li>
        <li>
            <a href="/<%=Modules.USERMGMT%>">User management</a>
        </li>
        <% }%>
    </ul>
</nav>
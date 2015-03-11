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
        <li>
            <a href="/<%=Modules.CLEARCOOKIES%>">Reset cookies</a>
        </li>
        <% if (AuthHelper.isAuthenticated(request)) {%>
        <li>
            <a href="/<%=Modules.DASHBOARD_CONFIG%>">Dashboard Config</a>
        </li>
        <li>
            <a href="/<%=Modules.EXPERIMENTS%>">Experiments</a>
        </li>
        <% }%>
    </ul>
</nav>
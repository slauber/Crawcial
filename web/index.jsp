<%@ page import="de.crawcial.web.Modules" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.setup.Validator" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="utf-8"/>
    <title>Crawcial</title>
    <link rel="stylesheet" href="style.css" type="text/css" media="screen"/>
    <link href="img/favicon.ico" rel="shortcut icon" type="image/x-icon">
    <link href="img/apple_touch_icon.png" rel="apple-touch-icon-precomposed">
</head>
<body>
<div id="container">
    <header>
        <a href="<%=Modules.HOME%>"><h1>Crawcial</h1></a>
    </header>

    <jsp:include page="/include/nav.jsp"/>

    <section id="content">
        <article>
            <% if (Validator.isDbConfigured(request.getServletContext()) != 0) {%>
            <jsp:include page="include/setup.jsp"/>
            <%} else {%>
            <% if (request.getParameter("p") != null) {
                String p = "include/" + request.getParameter("p") + ".jsp" +
                        (request.getParameter("e") != null ? "?e=" + request.getParameter("e") : "");
            %>
            <jsp:include page="<%=p%>"/>

            <%
            } else {
            %>
            <% if (AuthHelper.isAuthenticated(request)) {%>


            <form action="auth" method="post"><input type="hidden" name="action" value="signout">
                <input type="submit" value="Sign out"></form>
            <% } else {%>
            <h1>Welcome to Crawcial</h1>
            <a href="auth"><img alt="Plain login" width=215 style="margin-top: 38px;" src="./img/password.png"/></a>
            <% }
            }
            }%>
        </article>
    </section>
    <aside>
        <h2>Sidebarcontent</h2>
        <pre>OpenSource und so</pre>
    </aside>

    <footer>
        <% String twtoken = null;
            String fbtoken = null;
            String crtoken = null;
            Cookie[] c = request.getCookies();
            if (c != null) {
                for (Cookie cs : c) {
                    if (cs.getName().equals("twtoken")) {
                        twtoken = new String(Base64.decodeBase64(cs.getValue()));
                    }
                    if (cs.getName().equals("fbtoken")) {
                        fbtoken = new String(Base64.decodeBase64(cs.getValue()));
                    }
                    if (cs.getName().equals("crawcialsession")) {
                        crtoken = cs.getValue();
                    }
                }
            }
        %>
        <pre><%=(crtoken != null ? "Crawcial token: " + crtoken : "No Crawcial token")%></pre>
        <pre><%=(twtoken != null ? "Twitter token: " + twtoken : "No Twitter token")%></pre>
        <pre><%=(fbtoken != null ? "Facebook token: " + fbtoken : "No Facebook token")%></pre>
    </footer>
</div>
</body>
</html>

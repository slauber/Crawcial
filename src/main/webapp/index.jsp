<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.setup.Validator" %>
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
        <a href="<%=Constants.HOME%>"><h1>Crawcial</h1></a>
    </header>

    <jsp:include page="WEB-INF/include/nav.jsp"/>

    <section id="content">
        <article>
            <% if (Validator.isDbConfigured(request.getServletContext()) != 0) {%>
            <jsp:include page="WEB-INF/include/setup.jsp"/>
            <%
            } else {
                if (AuthHelper.isAuthenticated(request)) {
            %>
            <% if (request.getParameter("p") != null) {
                String p = "WEB-INF/include/" + request.getParameter("p") + ".jsp" +
                        (request.getParameter("e") != null ? "?e=" + request.getParameter("e") : "");
            %>
            <jsp:include page="<%=p%>"/>

            <%
            } else {
            %>
            <jsp:include page="WEB-INF/include/login.jsp"/>
            <%
                }
            } else {%>
            <jsp:include page="WEB-INF/include/login.jsp"/>
            <%
                    }
                }
            %>
        </article>
    </section>
    <aside>
        <jsp:include page="WEB-INF/include/sidebar.jsp"/>
    </aside>

    <footer>
        <jsp:include page="WEB-INF/include/footer.jsp"/>
    </footer>
</div>
</body>
</html>

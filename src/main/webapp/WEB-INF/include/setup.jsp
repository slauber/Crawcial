<%@ page import="de.crawcial.web.setup.Validator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% int code = Validator.isDbConfigured(getServletConfig().getServletContext());%>
<% if (Validator.isSetupEnabled(getServletConfig().getServletContext())) {%>
<script>
    EnableSubmit = function (val) {
        var sbmt = document.getElementById("update");
        sbmt.disabled = val.checked != true;
    }
</script>
<h1>Crawcial Setup Wizard</h1>
<%if (code == Validator.NO_CONFIG_FILE) {%>
<p>No configuration file was found, in order to work properly, you are required to provide your CouchDB
    credentials.</p>
<%
    }
    if (code == Validator.NO_DATABASE_CONNECTION) {
%>
<p>Connection to database failed. Please check your credentials (and maybe your database server).</p>
<%
    }
    if (code == Validator.NO_VALID_CONFIG) {
%>
<p>Your configuration is invalid, please provide valid data.</p>
<%}%>
<% String errorCode = request.getParameter("e");
    if (errorCode != null) {%>
<div>An error occurred while validating your configuration. Please try again.<br>Code: <%=errorCode%>
</div>
<%}%>
<form id="setup" name="setup" action="updateconfig" method="post">
    <p>
        We need to setup a connection to a CouchDB server. In order to prepare Crawcial's databases, you need to
        provide the database administrator credentials. Don't worry, they won't be persisted, a seperate user
        for Crawcial will be created.
    </p>

    <p>
        <label for="host">CouchDB host<br/>
            <input type="text" name="host" id="host" value="localhost" size="40"/></label>
    </p>

    <p>
        <label for="port">CouchDB port<br/>
            <input type="number" name="port" id="port" value="5984" size="40"/></label>
    </p>

    <p>
        <label for="protocol">CouchDB protocol<br/>
            <select id="protocol" name="protocol">
                <option>http</option>
                <option>https</option>
            </select></label>
    </p>
    <p>You are required to set a <a href="http://docs.couchdb.org/en/latest/intro/security.html#creating-new-admin-user"
                                    target="_blank">CouchDB administrator password</a>, CoachDB's admin party mode will
        not be accepted.</p>

    <p>
        <label for="user">CouchDB administrator username<br/>
            <input type="text" name="user" id="user" value="" size="20"/></label>
    </p>

    <p>
        <label for="password">CouchDB administrator password<br/>
            <input type="password" name="password" id="password" value="" size="20"/></label>
    </p>

    <p>
        <label for="feedback">Yes, please create new random
            database credentials and generate tables "crawcial_control", "crawcial_facebook" and "crawcial_twitter" (if
            they
            do not exist).
            Those new databases are only accessible with the generated credentials or an administrative account on this
            CouchDB instance.
            <input type="checkbox" name="feedback" id="feedback" value="Accept" onClick="EnableSubmit(this)"/> I know
            what I am doing.</label>
    </p>

    <p>
        <button type="submit" disabled id="update" name="action" value="update">Test and proceed</button>
    </p>
    <input type="hidden" name="code" value="<%=code%>">
</form>
<% } else {%>
<h1>This instance is ready for use</h1>

<p>Statuscode: <%=code%>
        <%
        if (code == 0) {
            response.sendRedirect("/");
        }
    %>
        <% if(code == 1){ %>

<p>Database unreachable</p>
<%}%>
</p>
<% } %>

<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% if (!AuthHelper.isAuthenticated(request)) {%>
<div class="crawcial-login-container" style="text-align: center; vertical-align: middle;width: 340px;">

    <img class="uk-margin-bottom" src="img/crawcial.png" width="256" height="256" alt="Crawcial">

    <form class="crawcial-login-form" action="auth" method="post">

        <div class="crawcial-form-row">
            <input class="crawcial-form-large" type="text" name="user" value=""
                   placeholder="Benutzername" autofocus="">
        </div>
        <div class="crawcial-form-row">
            <div class="crawcial-form-password">
                <input class="crawcial-form-large" type="password" name="password" value=""
                       placeholder="Passwort">
            </div>
        </div>
        <div class="crawcial-form-row">
            <button>Anmelden</button>
        </div>
        <div class="crawcial-form-row">
            <label class="uk-float-left"><input type="checkbox" name="_remember_me"> Angemeldet bleiben</label>
            <a class="uk-float-right uk-link uk-link-muted" data-uk-toggle="{ target: '.js-toggle' }">Passwort
                vergessen?</a>
        </div>

        <input type="hidden" name="action" value="signin">
    </form>

    <form action="fbauth" method="post">
        <button><img alt="Facebook login" width=215 src="img/facebook.png"/></button>
    </form>
    <form action="twauth" method="post">
        <button><img alt="Twitter login" width=215 src="img/twitter.png"/></button>
    </form>
</div>
<%} else {%>
You are logged in<%}%>
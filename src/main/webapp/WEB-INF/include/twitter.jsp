<%@ page import="de.crawcial.Constants" %>
<%@ page import="de.crawcial.web.auth.AuthHelper" %>
<%@ page import="de.crawcial.web.social.TwServlet" %>
<%@ page import="de.crawcial.web.util.Tokenmanager" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="java.nio.charset.Charset" %>
<%@ page import="java.util.Map" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script>
    EnableSubmit = function (val) {
        var mediaHttps = document.getElementById("mediahttps");
        var imgsize = document.getElementById("imgsize");
        mediaHttps.disabled = val.checked != true;
        imgsize.disabled = val.checked != true;
    }
</script>

<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true"></script>
<script>
    var rectangle;
    var map;

    function initialize() {
        var mapOptions = {
            center: new google.maps.LatLng(53.5, 8.5),
            zoom: 7
        };
        map = new google.maps.Map(document.getElementById('map-canvas'),
                mapOptions);

        var bounds = new google.maps.LatLngBounds(
                new google.maps.LatLng(54, 8),
                new google.maps.LatLng(53, 9)
        );

        // Define the rectangle and set its editable property to true.
        rectangle = new google.maps.Rectangle({
            bounds: bounds,
            editable: true,
            draggable: true
        });

        rectangle.setMap(map);

        // Add an event listener on the rectangle.
        google.maps.event.addListener(rectangle, 'bounds_changed', showNewRect);
    }

    function showNewRect(event) {
        var formNe = document.getElementById("ne");
        var formSw = document.getElementById("sw");
        formNe.value = rectangle.getBounds().getNorthEast();
        formSw.value = rectangle.getBounds().getSouthWest();
    }
    google.maps.event.addDomListener(window, 'load', initialize);
</script>
<script>
    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if (e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
        google.maps.event.trigger(map, 'resize');
    }
    function toggle_value(id) {
        var e = document.getElementById(id);
        if (e.value == 'false')
            e.value = 'true';
        else
            e.value = 'false';
    }

</script>

<h2>Crawcial for Twitter</h2>
<% String twtoken = null;
    Cookie[] c = request.getCookies();
    if (c != null) {
        for (Cookie cs : c) {
            if (cs.getName().equals("twtoken")) {
                twtoken = new String(Base64.decodeBase64(cs.getValue()));
            }
        }
    }
%>
<%if (TwServlet.isRunning() && AuthHelper.isAuthenticated(request)) {%>
<p><%=TwServlet.getStatus()%>
</p>
<% if (TwServlet.isShuttingDown()) {%>
<strong>Crawcial for Twitter is shutting down</strong>

<form class="crawcial-login-form" action="twitter" method="post">
    <button>Force shutdown (could cause data inconsistency)</button>
    <input type="hidden" name="action" value="shutdownNow">
</form>


<%} else {%>
<strong>Crawcial for Twitter is active</strong>

<form class="crawcial-login-form" action="twitter" method="post">
    <button>Shutdown now</button>
    <input type="hidden" name="action" value="shutdown">
</form>
<% if (TwServlet.isLowMemory()) { %>
<strong>Crawcial for Twitter ran out of memory and disabled the media downloader</strong>
<%
            }
        }
    }%>
<% if (AuthHelper.isAuthenticated(request)) {
    if (twtoken == null) {%>
<form action="twauth" method="post">
    <button><img alt="Twitter login" width=215 src="img/twitter.png"/></button>
</form>
<%
    }
%>

<% if (Tokenmanager.getTwitterOAuth(request) != null) {
    if (!TwServlet.isRunning()) {%>
<a href="twitter?action=trends">Preload terms with current world wide trending topics</a>
<button onclick="toggle_visibility('geoselector');toggle_value('geo')">Toggle Geo</button>
<form class="crawcial-login-form" action="twitter" method="post">
    <div class="crawcial-form-row">
        <input class="crawcial-form-large" type="text" size="60" name="terms"
               value="<%if(request.getParameter("terms")!=null){%><%=new String(Base64.decodeBase64(request.getParameter("terms")),Charset.forName("UTF-8")).trim()%><%}%>"
               placeholder="Filter terms" autofocus="">
    </div>
    <div>
        <input type="checkbox" name="media" id="media" value="true" onclick="EnableSubmit(this)"/> Persist media
        (consumes more bandwidth and
        storage, stops automatically if Crawcial runs out of memory)
    </div>
    <div>
        <input type="checkbox" name="mediahttps" id="mediahttps" value="true" disabled/> Download media via https
        (consumes more
        memory and cpu)
    </div>
    <div>
        <select disabled id="imgsize" name="imgsize">
            <option>thumb</option>
            <option>small</option>
            <option>medium</option>
            <option>large</option>
        </select>
    </div>


    <div id="geoselector" style="display: none">
        <div id="map-canvas" style="width: 100%; height: 400px;"></div>
        <div>
            <input type="text" id="ne" name="ne" value="(54, 8)"/><input type="text" id="sw" name="sw" value="(53, 9)"/>
            <input type="hidden" id="geo" name="geo" value="false"/>
        </div>
    </div>


    <div class="crawcial-form-row">
        <button>Go</button>
    </div>
    <input type="hidden" name="action" value="persist">
</form>
<% }
} else {
    Map<String, String> token = Tokenmanager.getSocialToken(request);
    if (!token.containsKey("twconsumerkey") || token.get("twconsumerkey").equals("")
            || !token.containsKey("twconsumersecret") || token.get("twconsumersecret").equals("")) { %>
<p style="color: crimson;font-weight: bold">Crawcial for Twitter is not ready. Please check your
    <a href="<%=Constants.CONFIGURATION%>">token configuration</a>.<%}%></p>

<p>In order to use Crawcial for Twitter, you need to sign in with Twitter.</p>
<%
        }
    }
%>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
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
<p><%=(crtoken != null ? "Crawcial token: " + crtoken : "No Crawcial token")%>
</p>

<p><%=(twtoken != null ? "Twitter token: " + twtoken : "No Twitter token")%>
</p>

<p><%=(fbtoken != null ? "Facebook token: " + fbtoken : "No Facebook token")%>
</p>
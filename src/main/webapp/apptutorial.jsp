<%--
  Created by IntelliJ IDEA.
  User: derdoenermann
  Date: 17.03.2015
  Time: 19:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Crawcial Help</title>
    <link href="//vjs.zencdn.net/4.12/video-js.css" rel="stylesheet">
    <script src="//vjs.zencdn.net/4.12/video.js"></script>
</head>

<body style="width: 720px;">
<div><p>Video tutorial for Facebook - <a href="https://developers.facebook.com/apps">Click here to go to
    https://developers.facebook.com/apps</a></p>
    <video id="facebook_tutorial" class="video-js vjs-default-skin"
           controls preload="auto" width="720" height="450">
        <source src="https://crawcial.de/storage/FacebookAppRegistration.mp4" type='video/mp4'/>
        <p class="vjs-no-js">To view this video please enable JavaScript, and consider upgrading to a web browser that
            <a
                    href="http://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a></p>
    </video>
</div>
<div>
    <p>Video tutorial for Twitter - <a href="https://apps.twitter.com">Click here to go to
        https://apps.twitter.com</a></p>
    <video id="twitter_tutorial" class="video-js vjs-default-skin"
           controls preload="auto" width="750" height="450">
        <source src="https://crawcial.de/storage/TwitterAppRegistration.mp4" type='video/mp4'/>
        <p class="vjs-no-js">To view this video please enable JavaScript, and consider upgrading to a web browser that
            <a
                    href="http://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a></p>
    </video>
</div>
</body>
</html>

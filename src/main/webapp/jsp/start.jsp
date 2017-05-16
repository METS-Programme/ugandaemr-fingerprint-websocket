<!DOCTYPE html>
<html>
<head>
    <title>Calculator App Using Spring 4 WebSocket</title>
    <script src="resources/sockjs-0.3.4.js"></script>
    <script src="resources/stomp.js"></script>
    <style>
        img {
            width: 100px;
            height: auto;
        }
    </style>
    <script type="text/javascript">

        var stompClient = null;

        var socket = new SockJS('/complete/add');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/showResult', function (calResult) {
                showResult(JSON.parse(calResult.body));
            });
        });

        function connect() {

        }

        function sendNum(finger) {
            document.getElementById('calResponse').innerHTML = "";
            document.getElementById('images').innerHTML = "";
            stompClient.send("/calcApp/add", {}, JSON.stringify({'finger': finger}));
        }

        function search(finger) {
            document.getElementById('calResponse').innerHTML = "";
            document.getElementById('images').innerHTML = "";
            stompClient.send("/calcApp/search", {});
        }

        function showResult(message) {
            var response = document.getElementById('calResponse');
            var imageDiv = document.getElementById('images');
            if (message.type === "image") {
                var imageTag = document.createElement('img');
                imageTag.src = "data:image/png;base64," + message.result;
                imageDiv.appendChild(imageTag);
            } else {
                response.innerHTML = message.result;
            }
        }
    </script>
</head>
<body>
<%--<noscript><h2>Enable Java script and reload this page to run Websocket Demo</h2></noscript>
<h1>UgandaEMR Fingerprint Application</h1>
<div>
    <div id="calculationDiv">
        <button id="thumb" onclick="sendNum(5);">Scan Right Thumb</button>
        <button id="index"  onclick="sendNum(6);">Scan Right Index</button>
        <button id="search" onclick="search();">Search Person</button>
        <p id="calResponse"></p>
        <div id="images"></div>
    </div>
</div>--%>
</body>
</html>
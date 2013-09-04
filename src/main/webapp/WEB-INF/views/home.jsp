<%@page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8"%>
<%@ include file="/WEB-INF/views/includes/taglibs.jsp"%>

<spring:url scope="page" var="jqueryJavascriptUrl" value="/resources/js/jquery-1.7.1.js"/>
<spring:url scope="page" var="jqueryTmplJavascriptUrl" value="/resources/js/jquery.tmpl.min.js"/>
<spring:url scope="page" var="jqueryAtmosphereUrl" value="/resources/js/jquery.atmosphere.js"/>
<spring:url scope="page" var="bootstrapUrl" value="/resources/js/bootstrap.js"/>
<spring:url scope="page" var="bootstrapCssUrl" value="/resources/css/bootstrap.css"/>
<spring:url scope="page" var="bootstrapResponsiveCssUrl" value="/resources/css/bootstrap-responsive.css"/>

<!DOCTYPE HTML>
<html>
    <head>
    	<meta charset="utf-8">
        <title>실시간 트위터 타임라인</title>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
		<script type="text/javascript" src="${pageScope.jqueryJavascriptUrl}"></script>
		<script type="text/javascript" src="${pageScope.jqueryTmplJavascriptUrl}"></script>
		<script type="text/javascript" src="${pageScope.jqueryAtmosphereUrl}"></script>
		<script type="text/javascript" src="${pageScope.bootstrapUrl}"></script>
		<link rel="stylesheet" href="${pageScope.bootstrapCssUrl}"/>
		<link rel="stylesheet" href="${pageScope.bootstrapResponsiveCssUrl}"/>
    </head>
    <body>
    	<section id="view-twitter-feed">
	        <div class="container-fluid">
	        	<header class="page-header">
	                <h3>
	                    실시간 트위터 타임라인 예제(실시간)
	                </h3>
	            </header>
	        	<div class="row-fluid">
	        		<div class="span2">
		                <ul class="nav nav-list">
						  <li class="nav-header">
						    프로토콜 선택
						  </li>
					      <li id="websockets-item" class="active">
					      	<a href="#">Websockets</a>
					      </li>
					      <li id="streaming-item">
					      	<a href="#">Streaming</a>
					      </li>
					      <li id="polling-item">
					      	<a href="#">Polling</a>
					      </li>
					      <li id="long-polling-item">
					      	<a href="#">Long Polling</a>
					      </li>
				        </ul>
				        <br />
				        <header class="nav-header">
	                        <h3>통계</h3>
	                    </header>
		                <table id="chartableStats" class="table-condensed">
		                    <thead>
		                        <tr>
		                            <th scope="col"></th>
		                            <th scope="col"></th>
		                        </tr>
		                    </thead>
		                    <tbody>
		                        <tr>
		                            <th scope="row" style="color: #1751A7">콜백수</th>
		                            <td id="numberOfCallbackInvocations">0</td>
		                        </tr>
		                        <tr>
		                            <th scope="row" style="color: #8AA717">트윗건수</th>
		                            <td id="numberOfTweets">0</td>
		                        </tr>
		                        <tr>
		                            <th scope="row" style="color: #A74217">에러건수</th>
		                            <td id="numberOfErrors">0</td>
		                        </tr>
		                    </tbody>
		                </table>
		            </div>
	        		<div class="span10">
		        		<table class="table-striped table-bordered">
			            	<thead>
		                        <tr>
		                            <th width="800">Twitter Messages</th>
		                        </tr>
		                    </thead>
		                    <tbody id="twitterMessages">
		                    	<tr id="placeHolder">
		                    		<td>Searching...</td>
		                    	</tr>
		                    </tbody>
		            	</table>
	        		</div>
	        	</div>
        	</div>
       	</section>
		
        <script id="template" type="text/x-jquery-tmpl">
        <tr>
			<td>
				<img align="left" alt='\${fromUser}' title='\${fromUser}' src='\${profileImageUrl}' width='48' height='48'>
					<div>
						&nbsp;&nbsp;&nbsp;<c:out value='\${text}'/>
					</div>
			</td>
		</tr>
        </script>

        <script type="text/javascript">
        	var socket = $.atmosphere;

        	function randomString() {
        		var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        		var string_length = 8;
        		var randomstring = '';
        		for (var i=0; i<string_length; i++) {
        			var rnum = Math.floor(Math.random() * chars.length);
        			randomstring += chars.substring(rnum,rnum+1);
        		}
        		return randomstring;
        	}
        	
            function handleAtmosphere( transport ) {
                var asyncHttpStatistics = {
                        transportType: 'N/A',
                        responseState: 'N/A',
                        numberOfCallbackInvocations: 0,
                        numberOfTweets: 0,
                        numberOfErrors: 0
                    };
                
                function refresh() {
                    console.log("Refreshing data tables...");
                    $('#responseState').html(asyncHttpStatistics.responseState);
                    $('#numberOfCallbackInvocations').html(asyncHttpStatistics.numberOfCallbackInvocations);
                    $('#numberOfTweets').html(asyncHttpStatistics.numberOfTweets);
                    $('#numberOfErrors').html(asyncHttpStatistics.numberOfErrors);
                }
                var request = new $.atmosphere.AtmosphereRequest();
                request.transport = transport;

                request.url = "<c:url value='http://localhost:8080/releasex-realtimeweb-1.0/twitter/concurrency'/>";
                request.contentType = "application/json; charset=utf-8";
                request.fallbackTransport = null;

                request.onMessage = function(response) {
                    buildTemplate(response);
                };

                request.onMessagePublished = function(response) {

                };
                request.onClose = function(response) {
					closeProcess(response);
                };

                request.onOpen = function() { $.atmosphere.log('info', ['socket open']); };
                request.onError =  function() { $.atmosphere.log('info', ['socket error']); };
                request.onReconnect =  function() { $.atmosphere.log('info', ['socket reconnect']); };
				//request.onClose =  function() {$.atmosphere.log('info', ['socket close']);};
                var subSocket = socket.subscribe(request);
                
                function closeProcess(response) {
                	if (response.state == "unsubscribe") {
                		socket.unsubscribe();
                	}
                }
                
                function buildTemplate(response){
                	asyncHttpStatistics.numberOfCallbackInvocations++;
                    asyncHttpStatistics.transportType = response.transport;
                    asyncHttpStatistics.responseState = response.responseState;

                    $.atmosphere.log('info', ["response.state: " + response.state]);
                    $.atmosphere.log('info', ["response.transport: " + response.transport]);
                    $.atmosphere.log('info', ["response.responseBody: " + response.responseBody]);
                    
                    if(response.state = "messageReceived"){                
	                	var data = response.responseBody;
	                    if (data) {
	
	                        try {
	                            var result =  $.parseJSON(data);
	                            var visible = $('#placeHolder').is(':visible');
	                            if (result.length > 0 && visible) {
	                                $("#placeHolder").fadeOut();
	                            }
	
	                            asyncHttpStatistics.numberOfTweets = asyncHttpStatistics.numberOfTweets + result.length;
	                            $( "#template" ).tmpl( result ).hide().prependTo( "#twitterMessages").fadeIn();
	
	                        } catch (error) {
	                            asyncHttpStatistics.numberOfErrors++;
	                            console.log("An error ocurred: " + error);
	                        }
	                    } else {
	                        console.log("response.responseBody is null - ignoring.");
	                    }
	
	                	refresh();
                	}
                }
            }

            handleAtmosphere("websocket");
            
            $(function() {
                $("#streaming-item").click(function() {
                	removeActive();
                	socket.unsubscribe();
                	handleAtmosphere("streaming");
                	$('#streaming-item').toggleClass("active");
                });
                $("#websockets-item").click(function() {
                	removeActive();
                	socket.unsubscribe();
                	handleAtmosphere("websocket");
                	$('#websockets-item').toggleClass("active");
                });
                $("#polling-item").click(function() {
                	removeActive();
                	socket.unsubscribe();
                	handleAtmosphere('polling');
                	$('#polling-item').toggleClass("active");
                });
                $("#long-polling-item").click(function() {
                	removeActive();
                	socket.unsubscribe();
                	handleAtmosphere('long-polling');
                	$('#long-polling-item').toggleClass("active");
                });
                
                function removeActive(){
                	$('#websockets-item').toggleClass("active", false);
                	$('#streaming-item').toggleClass("active", false);
                	$('#polling-item').toggleClass("active", false);
                	$('#long-polling-item').toggleClass("active", false);
                }
            });

        </script>
    </body>
</html>

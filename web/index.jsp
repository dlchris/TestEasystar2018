<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>$Title$</title>
    <meta charset="utf-8" />
    <script type="text/javascript" src="js/jquery/jquery-1.12.4.min.js" ></script>
  </head>
  <body>
  	<input id="txtJson" value='26D9F4580AF2AA59F552466085AD82C5{"a":"b","custlist":[{"deviceid":"1"}]}' style="width: 400px;"></input>
  	<button type="button" onclick="send()">提交</button>
  <script type="text/javascript">
  	function send() {
  		var md5 = "e587b18f9a7ddbdf408abfa2ccf51d14";
  		var json = $("#txtJson").val();
  		//alert(json);
  		$.ajax({
  			contentType: "application/json",
  			type:"post",
  			url:"/door/services/JYP/deletecustlst.ashx?sign=" + md5,
  			dataType: "json",
  			async:true,
  			cache: false,
  			data: json,
  			success:function (data) {
  				alert(decodeURIComponent(data.retmsg));
  			},
  			error: function (data) {
  				alert(data);
  			}
  		});
  	}
  </script>
  </body>
</html>

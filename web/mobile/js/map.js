
function addMarker(point, title, type) {
    if (point == null)
    	return;
    var marker = new BMap.Marker(point);
    var myIcon; 
    switch (type) {
    case 1:
    	myIcon = new BMap.Symbol(BMap_Symbol_SHAPE_POINT, {
    								scale: 2,//图标缩放大小
    								fillColor: "orange",//填充颜色
    								fillOpacity: 0.8//填充透明度
    				});
    	break;
    default:
    	myIcon = null;
    }
    map.addOverlay(marker);//, { icon: myIcon });

    
    var pointText = document.getElementById("point_text")
    if (pointText != null)
    {
        title = pointText.value;
    }

    var label = new BMap.Label(title,{offset:new BMap.Size(20,-10)});
    marker.setLabel(label);
}

function getAllPoint() {
	$.ajax({
		type:"post"
		,url:"http://192.168.0.1:8080/GetAllPoint"
		,async:true
		,dataType:"jsonp"  //数据格式设置为jsonp
        ,jsonp:"callback"  //Jquery生成验证参数的名称
        ,success:function(objs) {
			map.clearOverlays();
			objs.forEach(createMarker);
		}
		,error:function(event) {
			map.clearOverlays();
		}
	});
}
getAllPoint();

function createMarker(item, index) {
    var _point = new BMap.Point(item.x, item.y);
    addMarker(_point, item.title, item.type);
}

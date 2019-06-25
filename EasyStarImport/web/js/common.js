//来源表（xls,mdb等)信息
var sourceFields;
//来源表文件名
var sourceFileName;
//数据总数
var sourceRows;

//目标表（档案库表）信息
var targetClassId;
var targetClassType;
//目标表字段数组
var targetFields;
//主键字段名
var keyFieldName = "";
var importResult;

var logData;

var timer;

var myLoading;
layui.use(["table","laytpl","form","element", "upload"],function() {
	var table = layui.table;
	var form = layui.form;
	var element = layui.element;
	var upload = layui.upload;
	
	
	upload.render({
		elem: "#btnUploadFile"
		,url: "http://localhost/import/TSFileUploadAction"
		,accept: "file"
		,exts: "xls"
		,size: 1048576
		,data: {
			"classid":$("#classid").val(),
			"classtype":$("#classtype").val()
		}
		,before: function(obj){ //obj参数包含的信息，跟 choose回调完全一致，可参见上文。
    		myLoading = showLoading('上传中', true);
//			importResult = null;
//			table.reload('tableResult',{
//				data:null
//			})
  		}
		,done: function(res, index, upload){
    		showLoading(myLoading, false);
			//layer.closeAll('loading'); //关闭loading
            if (res.result != 0) {
                alert(res.error);
            } else {
                parseData(res);
            }
		}
		,error: function(index, upload){
			showLoading(myLoading, false); //关闭loading
			//showLoading('', false);
            alert("上传失败");
    		//当上传失败时，你可以生成一个“重新上传”的按钮，点击该按钮时，执行 upload() 方法即可实现重新上传
  		}
	});
	
    table.render({
    	elem: '#tableLog'
    	,cols: [[
    		{field:'keyField',title:'主键',width:60,align:'center'}
    		,{field:'msg',title:'结果'}
    	]]
		,skin: 'row' //表格风格
		,even: true
    	,page: false
    });

	table.render({
		elem: "#tableFields"
		, text: {
    		none: '暂无相关数据' //默认：无数据。
  		}
		,id: "tableFields"
		,height: 430
		,cols: [[ //标题栏
		    {checkbox: 'true', align: 'center', event:'a'} //默认全选
		    ,{field: 'DISSTR', title: '目标字段', width: '45%'}
		    ,{field: 'targetfield', title: '来源字段', width: '45%', templet: '#selecttpl'}
		     //默认全选
		    ]] 
		,skin: 'row' //表格风格
		,even: true
		,done: function(res, curr, count){
		  	$('#selectKeyField').append(new Option("",""));
		  	$.each(sourceFields,function(index,item){
            	$('#selectKeyField').append(new Option(item,item));//往下拉菜单里添加元素
        	})
			form.render('select');

			table.on('checkbox(tableFields)', function(obj){
		    	if (obj.type == "all") {
		    		for (var index in targetFields) {
		    			targetFields[index].checked = obj.checked;
		    		}
		    	} else {
		    		for (var index in targetFields) {
		    			if (targetFields[index].FIELDNAME == obj.data.FIELDNAME) {
				    		targetFields[index].checked = obj.checked;
							break;		    				
		    			}
		    		}
		    	}
		    });
		    
		    form.on('select(selectKeyField)', function(data){
		    	keyFieldName = data.value;
		    	verifyKeyName(data.value);
		    });
		}
	});
});

function verifyKeyName(keyName) {
	var jsonSend = {};
	jsonSend.type = 1;
	jsonSend.fieldname= keyName;
	$("#verifyIcon").removeClass();
	$("#verifyIcon").addClass("layui-icon layui-icon-loading layui-anim layui-anim-rotate layui-anim-loop");
	$("#verifyIcon").css("color", "#FF0000");
	$("#verifyIcon").css("display", "");
	$.ajax({
		type:"post",
		url:"http://localhost/import/VerifyFieldAction",
		cache: false,
        data: JSON.stringify(jsonSend),
        processData: false,
        contentType: false,
        dataType: "json",
        xhrFields: {
	    	withCredentials: true
   		},
        beforeSend: function (obj){
        },
        success: function (data) {
        	if (data && data.result == 0) {
				$("#verifyIcon").removeClass();
				$("#verifyIcon").addClass("layui-icon layui-icon-ok");
				$("#verifyIcon").css("color", "#0000FF");
				
			} else {
				$("#verifyIcon").removeClass();
				$("#verifyIcon").addClass("layui-icon layui-icon-close");
				$("#verifyIcon").css("color", "#FF0000");
			}
        },
        error: function (event) {
			$("#verifyIcon").removeClass();
			$("#verifyIcon").addClass("layui-icon layui-icon-close");
			$("#verifyIcon").css("color", "#FF0000");
        }
	});
	
}
function showLoading(msg, show) {
	if (show) {
		return layer.msg(msg, {icon: 16});
	} else {
		layer.close(msg);
	}
}

function showError(obj, data) {
	obj.focus();
	$(obj.id).css("display", "none");
	layer.alert(data.errmsg, {icon: 5});	
}

function btnCloseClick(){
	layer.closeAll();	
}

function startImport() {
	if (keyFieldName == "") {
		layer.alert("请设置一个主键!", {
			skin: 'layui-layer-molv' //样式类名  自定义样式
			,icon:5
		});
		return false;
	}

	$("#labelSuccessCount").html("0");
	$("#labelFailureCount").html("0");
	layui.use(['element'], function(){
		var element = layui.element;
		element.progress('progress', 0);
	});
	
    layui.use('layer',function(){
        var layer=layui.layer;
        layer.open({
            type:1
            ,title: '数据导入'
            ,closeBtn: false
            ,content:$("#windowImportResult")
            ,btn:[]
        })
    })
    
	var jsonSend = {};
	jsonSend.keyfield = keyFieldName;
	jsonSend.assfields = {};
	for (var index in targetFields) {
		var item = targetFields[index];
		if (item.checked && item.checked == true) {
			if (item.targetfield) {
				jsonSend.assfields[item.FIELDNAME] = item.targetfield;
			} else {
				jsonSend.assfields[item.FIELDNAME] = "";
			}
		}
	}
	
//  var jsonSend = {"filename":sourceFileName, "tablename":targetTableName, "keyfields":{"DOCID":"档案ID"}, "assfields":{"NOTENO":"件号","DOCID":"档案ID"}}
    $.ajax({
        url: "http://localhost/import/StartImportAction",
        type: 'POST',
        cache: false,
        data: JSON.stringify(jsonSend),
        processData: false,
        contentType: false,
        dataType: "json",
        xhrFields: {
	    	withCredentials: true
   		},
        beforeSend: function (obj){
			//showLoading('正在导入，请稍等', true);
            timer = setInterval(startGetResult, 1000);
        },
        success: function (data) {
        	//showLoading('正在导入，请稍等', false);
            if (data.result != 0) {
            	alert(data.error);
            }
        },
        error: function (event) {
        	//showLoading('正在导入，请稍等', false);
        	stopImport();
            alert("导入失败");
        }
    });
}

function stopImport() {
    clearInterval(timer);
	$("#btnClose").removeAttr("disabled");
	$("#btnClose").removeClass("layui-btn-disabled");
	$("#btnShowLog").removeAttr("disabled");
	$("#btnShowLog").removeClass("layui-btn-disabled");
}

function startGetResult() {
    $.ajax({
        url: "http://localhost/import/UploadMessageServlet",
        type: 'GET',
        cache: false,
        processData: false,
        contentType: false,
        dataType: "json",
        xhrFields: {
	    	withCredentials: true
   		},
        success: function (data) {
            showImportResult(data);
            if (data && data.finished) {
	        	stopImport();
                //alert('导入完成');
            }
            //console.log(importResult)
        },
        error: function (event) {
        	stopImport();
            console.log(event);
        }
    });
}

function showImportResult(data) {
	logData = data;
	$("#labelSuccessCount").html(data.success);
	$("#labelFailureCount").html(data.failure);
	layui.use(['element'], function(){
		var element = layui.element;
		element.progress('progress', data.progress);
	});
}

function parseData(sourceFile) {
    sourceFileName = sourceFile.files[0].filename;
    sourceRows = sourceFile.files[0].rows;
    sourceFields = sourceFile.files[0].fields;
    targetFields = sourceFile.tablefields;
    createFieldList();
}

function selectDome(obj) {
	var o = targetFields[obj.value];
	var index = obj.selectedIndex; // 选中索引
	var text = obj.options[index].text; // 选中文本
	o.targetfield = text;
	if (text != "") {
		var jsonSend = {};
		jsonSend.type = 2;
		jsonSend.sourcefieldname = text;
		jsonSend.targetfieldname = o.FIELDNAME;
		$.ajax({
			type:"post",
			url:"http://localhost/import/VerifyFieldAction",
			cache: false,
	        data: JSON.stringify(jsonSend),
	        processData: false,
	        contentType: false,
	        dataType: "json",
	        xhrFields: {
		    	withCredentials: true
	   		},
	        beforeSend: function (obj){
	        },
	        success: function (data) {
	        	if (data && data.result != 0) {
	        		showError(obj, data);
				}
	        },
	        error: function (event) {
	        	showError(obj, null);
	        }
		});
	}
}

function createFieldList() {
	
	layui.use(['table', 'form'], function(){
		var table = layui.table;
		var form = layui.form;
		
		//展示已知数据
  		table.reload('tableFields', {
  			data: targetFields
  			,limit: targetFields.length
  		});
  		
  		form.render("select", "selectKeyField");
	});
}

function btnShowLogClick() {
    layui.use(['layer', 'table'],function(){
        var layer=layui.layer;
        var table = layui.table;
        layer.open({
            type:1
            ,title: '导入日志'
            ,maxWidth: 800
            ,content:$("#windowLog")
            ,btn:[]
            ,success: function(layero, index){
		        table.reload("tableLog", {
		        	data: logData.values
		  			,limit: logData.values.length
		        });
            }
        })
    })
	
}

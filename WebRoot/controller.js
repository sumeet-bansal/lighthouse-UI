var idList = {
	'env': 'fabric',
	'fabric': 'node',
	'node': 'file'
};

var items = {left: {},right: {}};

isLeftSel = false;
isRightSel  = false;
context = {};

$(document).ready(function() {
	
    $('.multiselect-ui').multiselect({
        includeSelectAllOption: true
    });
    
    $('#compare-button').prop('disabled', false);
    
    $('#compare-button').on('click', function(evt) {
    	compare();
    });
});

retrieveList(true, 'env', '#env1-drop', true, context);
//////////////////////////////////////////////////////////////////////////////////////
// Actions for UI events..
//////////////////////////////////////////////////////////////////////////////////////
function onDropdown(type, isLeft, item) {
	var idx = isLeft ? 1 : 2;
	var objID = '#' + type + idx + '-drop';
	console.log(objID + "-  " + item + " selected!");
	updateCaption(objID, item);
	
	if(isLeft) {
		items.left[type] = item;
	} else {
		items.right[type] = item;
	}
	
	if(type != 'file') {
		// load list from server of next list..
		var nextType = idList[type];
		var nextID = '#' + nextType + idx + '-drop';
		console.log("retrieve list parameters - ", items);
		retrieveList(isLeft, nextType, nextID, false, items);
//	} else {
//		retrieveData(idx, isLeft);
	}
}

function updateCaption(objID, text) {
	var caption = $(objID + " button");
	caption.html(text + '<span class="caret"></span>');
}

function retrieveList(isLeft, type, objID, isFirst, context) {
	var url = "/lighthouse-UI/api/fetchList";
	var server="";
	if(isLeft){
		server="1";
	}else{
		server="2";
	}
	if(type=="env") {
		clearList("#fabric"+server+"-drop");
		clearList("#node"+server+"-drop");
		clearList("#file"+server+"-drop");
	}
	else if(type=="fabric") {
		clearList("#node"+server+"-drop");
		clearList("#file"+server+"-drop");
	}
	else if(type=="node") {
		clearList("#file"+server+"-drop");
	}
	$.support.cors = true;
	$.post(url, {listType: type,context: context}, function(data) {
		var response = JSON.parse(data);
		if(response!="[]"){
			if(response.result == 'SUCCESS') {
				console.log("RESULT", objID);
				setList(type, objID, response.list, isLeft);
				if(isFirst) {
					retrieveList(false, 'env', '#env2-drop');
				}
			} else {
				console.log("GET RESULT ERROR");
			}
		}
	}).fail(function(error) {
	      console.log("ERROR", error);
	});
}

function setList(type, objID, list, isLeft) {
	var obj = $(objID + " ul");
	var caption = $(objID + " button");
	var html = "";
	
	for(var i = 0; i < list.length; i++) {
		var item = list[i];
		html += "<li><a onclick=\"onDropdown('" + type + "', " + isLeft + ", '" + item + "')\">" + item + "</a></li>";
	}

	obj.html(html);
	caption.html(list[0] + '<span class="caret"></span>');
}

function clearList(objID) {
	$(objID).html('<button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">SELECT ITEM<span class="caret"></span></button><ul class="dropdown-menu"><li><a>NO ITEM!</a></li></ul>');
}

function compare(){
	var url = "/ng2Service/api/CSV";
	$.post(url, function(data) {
		var response = JSON.parse(data);
		$("#compare-data div.data-container").empty();
		$("#compare-data div.data-container").html(response);
	}).fail(function(error){
	      console.log("ERROR", error);
	});	
}

function retrieveData(idx, isLeft) {
	var url = "/ng2Service/api/fetchData";
	var params = isLeft ? items.left : items.right;
	params['index'] = idx;
	$.support.cors = true;
	$.post(url, params, function(data) {
		var response = JSON.parse(data);
		if(response.result == 'SUCCESS') {
			console.log("RESULT", response);
			updateData(idx, response.data, isLeft);
			if(isLeft) {
				isLeftSel = true;
			} else {
				isRightSel = true;
			}
			
			console.log("SELECTED RESULT", isLeftSel, isRightSel);
			if(isLeftSel || isRightSel) {
				console.log("DISABLE FALSE");
			    $('#compare-button').prop('disabled', false);
			}
		} else {
			console.log("GET RESULT ERROR");
		}
	}).fail(function(error){
	      console.log("ERROR", error);
	});	
}

function updateData(idx, data, isLeft) {
	console.log($(objID), objID);
	$("#compare-data div.data-container").html(data);
	//$(objID).html()+" "+
}

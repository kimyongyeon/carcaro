<%@ page contentType="text/html;charset=euc-kr" %> 
<% 
   String DYNAMIC_FILE = request.getParameter( "DYNAMIC_FILE" );   
   String DUMMY = "_"+System.currentTimeMillis();
   if ( DYNAMIC_FILE == null || DYNAMIC_FILE.equals("") )  DYNAMIC_FILE = "DYNAMIC_FILE" ;
   String DISPLAY_ITEM = "DISPLAY_ITEM" + DUMMY ;
   String DYNAMIC_FILE_ITEM = "DYNAMIC_FILE_ITEM" + DUMMY ; 
   String DYNAMIC_FILE_ITEMS = "DYNAMIC_FILE_ITEMS" + DUMMY ;
   String DISPLAY_ITEMS = "DISPLAY_ITEMS" + DUMMY;              
   String MAX_SIZE = request.getParameter( "MAX_SIZE" ); 
   if ( MAX_SIZE == null || MAX_SIZE.equals("") )  MAX_SIZE = "80" ;
   String DISPLAY_HEIGHT = request.getParameter( "DISPLAY_HEIGHT" ); 
   if ( DISPLAY_HEIGHT == null || DISPLAY_HEIGHT.equals("") )  DISPLAY_HEIGHT = "40" ;   
%>
<script language="JavaScript">

var scripts<%=DUMMY%> = new Array();  
var MAX_SIZE<%=DUMMY%> = <%=MAX_SIZE%> ;

function cfMakeArray<%=DUMMY%>(status, display_script) {
	this.status = status;
	this.display_script = display_script;
}

function cfAttach<%=DUMMY%>(obj) {
	var val = obj.value;
	var idx = obj.name.substring('<%=DYNAMIC_FILE%>_'.length);
	obj.style.display = 'none';
	
	cfAddItem<%=DUMMY%>(++idx, val);	
	cfItemList<%=DUMMY%>();
}

function cfTrimSize<%=DUMMY%>( val ) { 
  var maxSize = MAX_SIZE<%=DUMMY%> ;
  if ( val.length > maxSize ) return " ..."+val.substr(  val.length - maxSize ) ; 
  else return val ;
}

function cfAddItem<%=DUMMY%>(idx, val) {
	var seq = scripts<%=DUMMY%>.length;
	var display_script = '<span title="'+val+'" id='+'<%=DISPLAY_ITEM%>'+'_'+idx+'>'+ cfTrimSize<%=DUMMY%>(val) +' <b onclick=cfCancelItem<%=DUMMY%>('+seq+') style=cursor:pointer> &nbsp;&nbsp; <img src="/common/images/button_cancel.gif" style=cursor:pointer  align=absmiddle>  </b> </span><br>'; 
	var file_script = '<span id='+'<%=DYNAMIC_FILE_ITEM%>'+'_'+idx+'><input type=file name='+'<%=DYNAMIC_FILE%>'+'_'+idx+' id='+'<%=DYNAMIC_FILE%>'+'_'+idx+' onchange=cfAttach<%=DUMMY%>(this) size=1 style=width:0;cursor:pointer></span>';	
	scripts<%=DUMMY%>[seq] = new cfMakeArray<%=DUMMY%>(true, display_script);
	
	document.getElementById( '<%=DYNAMIC_FILE_ITEMS%>').insertAdjacentHTML("afterEnd", file_script);
}
 
function cfItemList<%=DUMMY%>() {
	var validate_cnt = 0;
	var display_scripts = '';

	for (var i = 0; i < scripts<%=DUMMY%>.length; i++) {
		if (scripts<%=DUMMY%>[i].status){
			validate_cnt++;
			display_scripts += '<b>'+validate_cnt+'</b>.'+scripts<%=DUMMY%>[i].display_script;
		}
	}
	
	if (validate_cnt == 0)
		display_scripts = '첨부 파일이 없습니다...';

	document.getElementById('<%=DISPLAY_ITEMS%>').innerHTML = display_scripts;
}

function cfGetFileName<%=DUMMY%>( seq ) { 
 return document.getElementById( '<%=DYNAMIC_FILE%>'+'_'+(seq+1)).value ;
}

function cfCancelItem<%=DUMMY%>(seq) {

    if ( !confirm( "해당 파일[" + cfGetFileName<%=DUMMY%>( seq ) + "]을 업로드 리스트에서 삭제하시겠습니까 ? " ) )  return ;
	scripts<%=DUMMY%>[seq].status = false;
	document.getElementById( '<%=DYNAMIC_FILE_ITEM%>' +'_'+(seq+1)).innerHTML = '';
	cfItemList<%=DUMMY%>();
}


/*  Netscape/Mozilla에서 insertAdjacentHTML을 emulation하는 스크립트 
 *  참고 사이트 http://forums.mozilla.or.kr/viewtopic.php?t=678, http://www.faqts.com/knowledge_base/view.phtml/aid/5756
**/
if(typeof HTMLElement!="undefined" && !HTMLElement.prototype.insertAdjacentElement){
	HTMLElement.prototype.insertAdjacentElement = function(where,parsedNode){
		switch (where){
			case 'beforeBegin':
			this.parentNode.insertBefore(parsedNode,this)
			break;
			case 'afterBegin':
			this.insertBefore(parsedNode,this.firstChild);
			break;
			case 'beforeEnd':
			this.appendChild(parsedNode);
			break;
			case 'afterEnd':
			if (this.nextSibling) this.parentNode.insertBefore(parsedNode,this.nextSibling);
			else this.parentNode.appendChild(parsedNode);
			break;
		}
	}

	HTMLElement.prototype.insertAdjacentHTML = function(where,htmlStr) {
		var r = this.ownerDocument.createRange();
		r.setStartBefore(this);
		var parsedHTML = r.createContextualFragment(htmlStr);
		this.insertAdjacentElement(where,parsedHTML)
	}
	
	
	HTMLElement.prototype.insertAdjacentText = function(where,txtStr){
		var parsedText = document.createTextNode(txtStr)
		this.insertAdjacentElement(where,parsedText)
	}
}
</script>

<fieldset style=padding:5px;background-color:#eeeeee;font-size:12px;>
<div style="width:100%;height:<%=DISPLAY_HEIGHT%>;overflow:auto" id='<%=DISPLAY_ITEMS%>'>첨부 파일이 없습니다...</div>
</fieldset>

<img src="./css/images/banner.png" style=cursor:pointer  align="middle">	 <font style=font-size:9pt;text-decoration:underline;> 첨부파일 추가 </font>

<div style='width:200;height:16;overflow:hidden;position:relative;top:-20px;filter:alpha(opacity=0);-moz-opacity:0'>
 <div id='<%=DYNAMIC_FILE_ITEMS%>'></div>
 <span id='<%=DYNAMIC_FILE_ITEM%>_1'><input type=file name=<%=DYNAMIC_FILE%>_1 id=<%=DYNAMIC_FILE%>_1 onchange=cfAttach<%=DUMMY%>(this) size=1 style=width:0;cursor:pointer></span>
</div>
  
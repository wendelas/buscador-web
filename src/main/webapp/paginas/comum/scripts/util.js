/* adiciona chamadas/eventos

	elemento - elemento a receber a função
	type - tipo de evento (load,click,etc)
	expression - função a ser chamada
	bubbling - habilita/desabilita o bubble
	
*/

function addListener(element, type, expression, bubbling) {
bubbling = bubbling || false;

	if(window.addEventListener) { // Standard
	element.addEventListener(type, expression, bubbling);
	return true;
	} 
	
	else if(window.attachEvent) { // IE
	element.attachEvent('on' + type, expression);
	return true;
	}
	
	else return false;
}

/* cria navegacao de abas a partir do holder selecionado
	
	holder_id - id dos holders de conteudo
	selecionado - index da tab que vai receber o estado inicial de selecionada
	
*/

function criarNavegacao(holder_id,selecionado) {
this.mainHolder = document.getElementById(holder_id);
this.tabHolder = document.getElementById("tabs");

if (!this.tabHolder) {
	return null;
}
this.contentHolders = this.mainHolder.getElementsByTagName("div");
this.controleAntigo = selecionado || 0;
this.contentArray = new Array();
this.controleNum = 0;

	if (this.contentHolders.length > 1) this.tabHolder.style.display = "block";
	
	for (var x=0; x<this.contentHolders.length; x++) {

	if (this.contentHolders[x].getAttribute("criarNavegacao") == "true") {
	
		this.contentArray.push(this.contentHolders[x]);
		this.tempTab = document.createElement("a");
		if (x == this.controleAntigo) this.tempTab.className = "selecionada rich-tab-header rich-tab-active";
		this.tempTab.controle = this.controleNum;
		this.controleNum++;
		this.tempTab.navegacao = this;
		this.tempTab.onclick = function() { this.navegacao.selecionar(this); };
		this.tempTab.href = "javascript:void(0)";
		this.leftSide = document.createElement("span");
		this.leftSide.className = "rich-tabhdr-side-border";
		this.tempTab.appendChild(this.leftSide);
		this.midSide = document.createElement("span");
		this.midSide.className = "mid rich-tabhdr-side-cell rich-tab-header rich-tab-active";
		this.midSide.innerHTML = this.contentHolders[x].className;
		this.tempTab.appendChild(this.midSide);
		this.rightSide = document.createElement("span");
		this.rightSide.className = "rich-tabhdr-side-border";
		this.tempTab.appendChild(this.rightSide);
		this.tabHolder.appendChild(this.tempTab);
		if (x != this.controleAntigo) this.contentHolders[x].style.display = "none";
		
	}
	
	}

this.tabs = this.tabHolder.getElementsByTagName("a");

	this.selecionar = function(obj) {
	this.tabs[this.controleAntigo].className = "";
	this.contentArray[this.controleAntigo].style.display = "none";
	this.contentArray[obj.controle].style.display = "";
	obj.className = "selecionada";
	this.controleAntigo = obj.controle;
	};
	
	this.selecionarAba = function(nome) {
		for (var x=0; x<this.tabs.length; x++) {
			if (this.tabs[x].innerHTML.indexOf(nome) !=-1) {
			this.selecionar(this.tabs[x]);
			}
		}
	};

}


window.size = function() {
	var w = 0;
	var h = 0;

	if(!window.innerWidth) {
		if(!(document.documentElement.clientWidth == 0)) {
			w = document.documentElement.clientWidth;
			h = document.documentElement.clientHeight;
		}
		else {
			w = document.body.clientWidth;
			h = document.body.clientHeight;
		}
	}
	else {
		w = window.innerWidth;
		h = window.innerHeight;
	}
	
	return {width:w,height:h};
}

/* realiza o scroll para a coordenada desejada

*/

function pageScroll(y) {
    window.scrollTo(0,y);
}

/* scripts especificos do formulario 

*/

function advancedForm() {
inputs = document.getElementsByTagName("input");
inputType = ['radio','checkbox','text','password','file'];

	for (x=0; x<inputs.length; x++) {
		for (y=0; y<inputType.length; y++) {
			if (inputs[x].type == inputType[y]) {
			inputs[x].onkeydown = function(e) { submitOnEnter(e); }
			}
		}
	}
}

/* enviar o formulario ao pressionar o ENTER

*/

function submitOnEnter(e) {
if (!e) e = window.event

var keycode;

codigo = e.keyCode ? e.keyCode : e.which;

	if (codigo == 13) {
            try {
            submeteNoEnter();
            } catch (e) {
                try {
                    var f= document.forms[0];
                    if (f.metodo.value == "" || 
                        f.metodo.value == 'consultar' || 
                        f.metodo.value == 'apresentar') {
                        consultar();
                    }
                }catch (ex) {
                }
            }
	}
}

/* seleciona todos os checkbox cujo alt sejam iguais ao id passado
	
	obj - checkbox
	id - int a ser comparado com o alt dos checkboxes
	fireEvent - caso o checkbox filho checado tenha um evento, dispara o evento

*/

function checkChild(obj,id,fireEvent) {
	state = obj.checked
	checks = document.getElementsByTagName("input")
		
	for (var x=0; x<checks.length; x++) {
		if (checks[x].type == "checkbox" && checks[x].alt == id) {
		checks[x].checked = state ? "checked" : "";
			if (fireEvent) {
				if (checks[x].onclick) { checks[x].onclick() }
			}
		}
	}		
}
	
/* ajusta o tamanho da tela, criando scroll de acordo com o conteudo do holder principal

*/

function ajustaTela() {
    var conteudoHolder = document.getElementById("content");
    
    if (conteudoHolder) {
        var principalHolder = document.getElementById("principal");
        var tam = 182;
        
        if (principalHolder && (principalHolder.className=='principal_modal')) {
            tam = 50;
            
            if (/MSIE 6.0/.test(navigator.userAgent)) {
                return;
            }
        }

        var alturaPermitida = window.size().height - tam;
        
        if (conteudoHolder.scrollHeight >= alturaPermitida) {
            conteudoHolder.style.height = alturaPermitida + "px";
        } else { 
            conteudoHolder.style.height = "auto"; 
        }

        spacer = document.createElement("div");
        spacer.style.height = "15px";
                
        conteudoHolder.appendChild(spacer);
    }
}

addListener(window,'resize',ajustaTela);

/* realiza a troca de imagens para efeito de hover

*/



/*	esconde/mostra elementos
	
	mostra - array com os ids dos elementos que vao ser visiveis
	esconde - arry com os ids dos elementos que vao ser ocultos
	
*/

function controleElemento(mostra,esconde) {

	if (mostra !=null) {
		if (typeof mostra !="undefined") {
			for (x=0; x<mostra.length; x++) {
			document.getElementById(mostra[x]).style.display = ""
			}
		}
	}
	
	if (esconde !=null) {
		if (typeof esconde !="undefined") {
			for (x=0; x<esconde.length; x++) {
			document.getElementById(esconde[x]).style.display = "none"
			}
		}
	}
}

/* coloca o foco em um determinado elemento 

	nameRef - atributo name do elemento que vai receber o foco
	index - caso o elemento seja radio/checkbox, passar o index do elemento a ser selecionado (o name funciona como array)

*/

function focusElm(nameRef,index) {
f = document.forms[0]
elms = f.elements

elms[nameRef].length ? elms[nameRef][index].focus() : elms[nameRef].focus()
}

String.prototype.trim = function() {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

function setCursor(el,st,end) {
    if(el.setSelectionRange) {
        el.focus();
        el.setSelectionRange(st,end);
    }
    else if(el.createTextRange) {
        range=el.createTextRange();
        range.collapse(true);
        range.moveEnd('character',end);
        range.moveStart('character',st);
        range.select();        
    }
} 

/*
 * Abre o modal
 */
function popup(titulo,url,largura, altura) {
    if (!altura) {
        altura = 430;
    }
    
    if (!largura) {
        largura = 800;
    }   
    
    reiniciaCountdown();

    modal(titulo,null,'<iframe src=\'' + url + '\' style=\'width:100%;height:'+ altura +'px; overflow:auto\'></iframe>',largura);    
}

/*
	aplica uma mascara especifica a um campo do tipo text
	parametros:
		o = referencia do objeto (input)
		m = mascara
			data (dd/mm/aaaa) - ##/##/####
			cpf - ###.###.###-##
		e = evento (array)
		v = valor monetario
		
	exemplos:
	f = document.forms[0];
	inputMask(f.cpf,"###.###.###-##",["keyup","blur"]);
	inputMask(f.data,"##/##/####",["keyup","blur"]);
	inputMask(f.processo,"#####-###",["keyup","blur"]);
	inputMask(f.valor,"##,##",["keyup","blur"],true);
*/
inputMask = function(o,m,e,v) {
if (o == null || m == null || e == null) return false

	function mask(e) {
		if (e.keyCode == 8) o.value = o.value.substring(0,o.value.length)
		
		else {	
				
			valor = getValorLimpo(o.value);
			novoValor = m;
			_charEspecial = getPosicaoEspecial(m);
			iterator = 0;
			
			if (v) {
				valor = reverterString(valor);				
				
				novoValor = reverterString(novoValor);
				
				for (var x=0; x<valor.length; x++) { novoValor = novoValor.replace("#",valor.substring(x,x*1+1)); }
				
				while (novoValor.indexOf("#") !=-1) { novoValor = novoValor.replace("#",0); }				
						
				o.value = reverterString(novoValor);
			}
			
			else {
				for (var x=0; x<valor.length; x++) { novoValor = novoValor.replace("#",valor.substring(x,x*1+1)); }	
				
				for (var x=0; x<_charEspecial.length; x++) {
					if (valor.length >= (_charEspecial[x] - x) ) { iterator++; }
				}

				o.value = novoValor.substring(0, (valor.length + iterator) );
			}
		}
	}
	
	function reverterString(s) {
		return s.split("").reverse().join("");
	}
	
	function getValorLimpo(v) {
	aux = "";
		for (var x=0; x<v.length; x++) {
			if (v.substring(x,x+1) >= 0 && v.substring(x,x+1) <= 9 && v.substring(x,x+1) !=" ") {
			aux+= v.substring(x,x+1);
			}
		}
	return aux;
	}
	
	function getPosicaoEspecial(v) {
	aux = new Array();
		for (var x=0; x<v.length; x++) {
			if (isNaN(v.substring(x,x+1)) && v.substring(x,x+1) !="#") {
			aux.push(x);
			}
		}
	return aux;
	}
	
	isArray = (typeof e == "object" && "splice" in e && "join" in e) ? true : false
	
		if (isArray) { for (var x in e) { addListener(o,e[x],mask) } }
		else { addListener(o,e,mask) }
	
}

/* aplica a classe readOnly aos inputs para possibilitar aplicação de estilo no ie6

*/

function ie6readOnly() {
	var inputs = document.getElementsByTagName("input");
	
	for (var x=0; x<inputs.length; x++) {
		if (inputs[x].getAttribute("readonly")) {
			inputs[x].className = inputs[x].className + " readonly";
		}
	}
}

function scrollTela() {
    pageScroll($("posicaoTela").value);    
}

function toggleBotoes() {
    try {
        var botoes = document.getElementsByClassName('buttonClass');
                
        if (botoes != null) {
	        for (i = 0 ; i< botoes.length;i++) {
	            if (botoes[i].id != 'btimprimir') {
	            	if (botoes[i].style.display == 'none') {
	            		botoes[i].style.display = '';
	            	} else {
	            		botoes[i].style.display = 'none';
	            	}
	            }
	        }   
	    }
    } catch (e) {}
}

/* chamada padrao dos scripts da pagina

*/

function init() {
    advancedForm();
    //ajustaTela();
    customForms();
    ie6readOnly();
    if (document.body.className != "body_modal") {
        navegacao = new criarNavegacao("content",0);
    }
}

function customForms() {
	try {
		var divs = document.getElementsByClassName("rich-panel-header");

		for (var x=0; x<divs.length; x++) {
			divs[x].onselectstart = function() { return false; };
			divs[x].onmousedown = function() { return false; };
			divs[x].onclick = function() {
				var divBody = this.parentNode.getElementsByTagName("div");
		
				for (var y=0; y<divBody.length; y++) {					
					if (divBody[y].className.indexOf('rich-panel-body') != -1) {
						this.ol = divBody[y];
			
						if (this.ol.style.display == "none") {
							this.ol.style.display = "";
						} else {
							this.ol.style.display = "none";
						}
					}
				}
				
			//ajustaTela();
			}
		}
	} catch (e) {}
		
}

var checks = {
  selecionarTudo : function(idElem){
    $$("#modulo_"+idElem+' input').each(function(e){
      e.checked = $("check_"+idElem).checked;
    });
  }
}

function getText(campoSelect) {
    for (i = 0;i < campoSelect.length;i++) {
        if (campoSelect[i].selected) {
            return campoSelect[i].text;
        }
    }
    
    return "";
}

function submeteForm() {
    modalProcessando();
    
    try {
    	reiniciaCountdown();
    }catch (e) {
        try {
        	parent.reiniciaCountdown();
        } catch (ex) {
    		
    	}
    }
    
    document.forms[0].submit();
}

function getScrollTop() {
  var scrollTop = 0;
  if( typeof( window.pageYOffset ) == 'number' ) {
  // Netscape compliant
	  scrollTop = window.pageYOffset;	    
  } else if( document.body && document.body.scrollTop) {
    // DOM compliant
	  scrollTop = document.body.scrollTop;	    
  } else if( document.documentElement && document.documentElement.scrollTop) {
    // IE6 standards compliant mode
	  scrollTop = document.documentElement.scrollTop;
  }
  return scrollTop; 
}
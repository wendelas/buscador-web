function toggle(targetId) {
	if (document.getElementById) {
		target = document.getElementById(targetId);
		if (target.style.display == "none" || target.style.display == "") {
			target.style.display = "block";
		} else if (target.style.display == "block") {
			target.style.display = "none";
		}
	}
}
function hide(targetId) {
	if (document.getElementById) {
		target = document.getElementById(targetId);
		if (target.style.display != "none") {
			target.style.display = "none";
		}
	}
}
function show(targetId) {
	if (document.getElementById) {
		target = document.getElementById(targetId);
		if (target.style.display != "block") {
			target.style.display = "block";
		}
	}
}
function mostra() {
	hide('msgErro');
	show('contInserirPessoa');
}
function reverte() {
	show('msgErro');
	hide('contInserirPessoa');
}

function foco(id) {
	document.getElementById(id).focus();
}

function addEvent(obj, evType, fn) {
	if (obj.addEventListener) {
		obj.addEventListener(evType, fn, false);
		return true;
	} else if (obj.attachEvent) {
		var r = obj.attachEvent("on" + evType, fn);
		return r;
	} else {
		return false;
	}
}

function ajaxRequestContainsErrors() {
	return document.getElementById("maximumSeverity").value == "2";
}

function redirecionaCaixa(contexto) {
	document.location.href = contexto + '/view/caixaentrada/caixaPessoal.faces';
}

function openSuggestionBox(suggestionBoxId, formId, textBoxId) {
	document.getElementById(formId + ':' + textBoxId).value = '';
	v = $(formId + ':' + suggestionBoxId).component.callSuggestion(true);
}

function focoNoPrimeiroItem() {
	var bFound = false;

	// for each form
	for (f = 0; f < document.forms.length; f++) {
		// for each element in each form
		for (i = 0; i < document.forms[f].length; i++) {
			// if it's not a hidden element
			if (document.forms[f][i].type != "hidden") {
				// and it's not disabled
				if (document.forms[f][i].disabled != true) {
					// set the focus to it
					document.forms[f][i].focus();
					var bFound = true;
				}
			}
			// if found in this element, stop looking
			if (bFound == true)
				break;
		}
		// if found in this form, stop looking
		if (bFound == true)
			break;
	}
}

function FormataValor(campo,tammax,teclapres) {
  vr = campo.value;
  if (document.all){
    var tecla = teclapres.keyCode;
  }else{
    var tecla = teclapres.which;
  } 

  if (tecla == 9 || tecla == 0){
    return false;
  }else{
    if (tecla >= 48 && tecla <= 57){
      vr = FiltraCampo(campo);
    }
  }
  
  tam = vr.length; 
  if (tam < tammax && tecla != 8){ 
    tam = vr.length + 1 ; } 
  if (tecla == 8 ){ 
    tam = tam - 1 ; 
  }
  if ( tecla >= 48 && tecla <= 57 ) {// || tecla >= 96 && tecla <= 105 ){
    if ( tam <= 2 ){ 
      campo.value = vr ; }
    if ( (tam > 2) && (tam <= 5) ){
      campo.value = vr.substr( 0, tam - 2 ) + ',' + vr.substr( tam - 2, tam ) ; }
    if ( (tam >= 6) && (tam <= 8) ){
      campo.value = vr.substr( 0, tam - 5 ) + '.' + vr.substr( tam - 5, 3 ) + ',' + vr.substr( tam - 2, tam ) ; }
    if ( (tam >= 9) && (tam <= 11) ){
      campo.value = vr.substr( 0, tam - 8 ) + '.' + vr.substr( tam - 8, 3 ) + '.' + vr.substr( tam - 5, 3 ) + ',' + vr.substr( tam - 2, tam ) ; }
    if ( (tam >= 12) && (tam <= 14) ){
      campo.value = vr.substr( 0, tam - 11 ) + '.' + vr.substr( tam - 11, 3 ) + '.' + vr.substr( tam - 8, 3 ) + '.' + vr.substr( tam - 5, 3 ) + ',' + vr.substr( tam - 2, tam ) ; }
    if ( (tam >= 15) && (tam <= 17) ){
      campo.value = vr.substr( 0, tam - 14 ) + '.' + vr.substr( tam - 14, 3 ) + '.' + vr.substr( tam - 11, 3 ) + '.' + vr.substr( tam - 8, 3 ) + '.' + vr.substr( tam - 5, 3 ) + ',' + vr.substr( tam - 2, tam ) ;}
  }
} 

function FiltraCampo(campo){
 var s = "";
 var cp = "";
 vr = campo.value;
 tam = vr.length;
 for (i = 0; i < tam ; i++) {  
  if (vr.substring(i,i + 1) != "/" && vr.substring(i,i + 1) != "-" && vr.substring(i,i + 1) != "."  && vr.substring(i,i + 1) != "," ){
    s = s + vr.substring(i,i + 1);}
 }
 campo.value = s;
 return campo.value;
}

function formataValorBlur(campo) {
  
  vr = campo.value;  
  vr = FiltraCampo(campo);
  
  tam = vr.length; 
  var valorAux = "";
  var j = 0;
  for (i = vr.length; i >= 0; i--,j++)  {                         
    
    if ( j <= 2 ){ 
      valorAux = vr ; }
    if ( (j > 2) && (j <= 5) ){
      valorAux = vr.substr( 0, j - 2 ) + ',' + vr.substr( j - 2, j ) ; }
    if ( (j >= 6) && (j <= 8) ){
      valorAux = vr.substr( 0, j - 5 ) + '.' + vr.substr( j - 5, 3 ) + ',' + vr.substr( j - 2, j ) ; }
    if ( (j >= 9) && (j <= 11) ){
      valorAux = vr.substr( 0, j - 8 ) + '.' + vr.substr( j - 8, 3 ) + '.' + vr.substr( j - 5, 3 ) + ',' + vr.substr( j - 2, j ) ; }
    if ( (j >= 12) && (j <= 14) ){
      valorAux = vr.substr( 0, j - 11 ) + '.' + vr.substr( j - 11, 3 ) + '.' + vr.substr( j - 8, 3 ) + '.' + vr.substr( j - 5, 3 ) + ',' + vr.substr( j - 2, j ) ; }
    if ( (j >= 15) && (j <= 17) ){
      valorAux = vr.substr( 0, j - 14 ) + '.' + vr.substr( j - 14, 3 ) + '.' + vr.substr( j - 11, 3 ) + '.' + vr.substr( j - 8, 3 ) + '.' + vr.substr( j - 5, 3 ) + ',' + vr.substr( j - 2, j ) ;}
  }
  if (valorAux.length > 0 && valorAux.length < 3) {
    valorAux = valorAux + ',00';
  }
  campo.value = valorAux;
}

/**
* Somente Números. 
* permiteTeclaX, se for true permite também o caracter X
*/
function somenteNumeros(e,permiteTeclaX){
 if (document.all){
  var tecla = event.keyCode;
 }else{
  var tecla = e.which;
 }
 
 var permiteX = false;
 
 // 88 = 'X'
 // 120 = 'x'
 if (permiteTeclaX && (tecla == 88 || tecla == 120)) {
    permiteX = true;
 }

 if ((tecla > 47 && tecla < 58) || permiteX){ // numeros de 0 a 9
  return true;
 }else{
  if (tecla != 8 && tecla != 46 && tecla != 9 && tecla != 0){
    //comentado por Ivan Mecenas para evitar a digitação do ', %, & e ( nos campos numéricos
    //tecla != 37 && tecla != 38 && tecla != 39 && tecla != 40 && 
   if (document.all){
    event.keyCode = 0;
   }else{
    e.preventDefault();
   }
  }else{
   return true;
  }
 }
}

function formatTelefone(element, e){
	  if (e.keyCode != 8){		  
	    tamanho = element.value.length;
	    if (tamanho == 2){
	      if (element.value.charAt(0)!="(")
	        element.value = "(" + element.value + ")";
	    }
	    if (tamanho == 3)
	      if (element.value.charAt(0)=="(")
	        element.value += ")";
	    if (tamanho == 8)
	      element.value += "-";
	  }
	}

function formatarCampo(objeto, mascara,teclapress){
  /* 
    Descrição: 
      Formatação para qualquer mascara 
    Sintaxe: 
      formatarCampo (objeto, mascara)
      objeto -> o campo que queira colocar a máscara
      mascara -> o formato da máscara, utiliza-se o caracter # (jogo da velha) para representar os números
      teclapress -> event (evento)
    Exemplo:
      CEP
      onkeypress="formatarCampo(this, '#####-###',event)"
      CPF
      onkeypress="formatarCampo(this, '###.###.###-##',event)"
      DATA
      onkeypress="formatarCampo(this, '##/##/####',event)"
  */
  var tecla = getKey(teclapress);
	   // Pega o valor ASCII da tecla que o usu?rio pressionou   
	  	var ctrl = teclapress.ctrlKey;   	   	  
	   // Teclas Insert, Tab, Del, Page UP, Page Down, Home, End, setas de movimenta??o, Shift e CTrl.
	   if (tecla == 8 || tecla == 9 || tecla == 37 || tecla == 38 || tecla == 39 || 
	       tecla == 40 || tecla == 46 || tecla == 36 || tecla == 35 || 
		   tecla == 33 || tecla == 34  || tecla == 45 || tecla == 16 || tecla == 17)
	   {
		    return true; 
	   }
		   
	   // Teclas ALT
	   if (tecla == 18){
	   		return true; 	
	   }
	   //CTRL + V 
	   if (ctrl && tecla==86) {
	   		return true;
	   }
	   //CTRL + C 
	   if (ctrl && tecla==67) {
	   		return true;
	   }
	   //CTRL + Z
	   if (ctrl && tecla==90) {
	   		return true;
	   }
	   //CTRL + X
	   if (ctrl && tecla==88) {
	   		return true;
	   }
  var i = objeto.value.length;
  var saida = mascara.substring(0,1);
  var texto = mascara.substring(i)
  if (texto.substring(0,1) != saida){
    objeto.value += texto.substring(0,1);
  }  
}


function getKey (event) {
    return event.keyCode?event.keyCode:(event.which?event.which:event.charCode);
}

function somenteLetrasNumeros(e){
 if (document.all){
  var tecla = event.keyCode;
 }else{
  var tecla = e.which;
 }
 
  // numeros de 0 a 9, letras A a Z, letras a a z
 if ((tecla > 47 && tecla < 58) || (tecla > 64 && tecla < 90) || (tecla > 96 && tecla < 122)){
  return true;
 }else{
  if (tecla != 8 && tecla != 9 && tecla != 0){
   if (document.all){
    event.keyCode = 0;
   }else{
    e.preventDefault();
   }
  }else{
   return true;
  }
 }
}

function limpaCaracteresInvalidos(valor){
        var tam, descricao;
        var elemento;
        var obj = valor.value;
        elemento = ""; 
        tam = parseInt(obj.length,10);
        for(var i=0; i < tam; i++){
                descricao = obj.charAt(i);
                if((descricao >= '0' && descricao <= '9') || (descricao > 64 && descricao < 90) || (descricao > 96 && descricao < 122)){
                        elemento = "" + elemento + descricao;
                }
        }
        return elemento;
}

function apenasNumeros(e){
 if (document.all){
  var tecla = event.keyCode;
 }else{
  var tecla = e.which;
 }

 if (tecla > 47 && tecla < 58){ // numeros de 0 a 9
  return true;
 }else{
  if (tecla != 8 && tecla != 44 && tecla != 9 && tecla != 0){
   if (document.all){
    event.keyCode = 0;
   }else{
    e.preventDefault();
   }
  }else{
   return true;
  }
 }
}

function zerosEsquerda(campo, tam){
	if (campo.value == null || campo.value == ""){
		return;
	}
	var tamanho = tam;
	var total = parseInt(tamanho - campo.value.length);
	var temp = "";
	var retorno = "";

	if (campo.value.indexOf(",") >= 0){
		var	j = 0;
	}else{
		var	j = 1;
	}
	
	for (i = j; i < total; i++){
		temp = temp + "0";
	}
	retorno = temp + "" + campo.value;
	campo.value = retorno;
}

function validaExclusao() {
    var f = document.forms[0];
    var selecionou = false;
    if (f.indiceExclusao) {
        if (!f.indiceExclusao.length) {
            if (f.indiceExclusao.checked) {
                selecionou = true;
            }            
        } else {
            for (i = 0; i < f.indiceExclusao.length; i++) {
                if (f.indiceExclusao[i].checked) {
                    selecionou = true;
                    break;
                }
            }
        }
    }
    
    if (!selecionou) {
        alert('Selecione algum item para efetuar a exclusão!');
    }
    
    return selecionou;
}

// Troca um caractere por outro em uma string
function ReplaceChar(valor, oldchar, newchar) {
  var resultado = valor;
  var temp = "";           
  while (resultado.indexOf(oldchar) > 0) {                   
    pos = resultado.indexOf(oldchar);
    temp = resultado.substring(0, pos) + newchar + resultado.substring(pos + 1, resultado.length);
    resultado = temp;
  }
  return resultado;
}

// Retorna um float partindo de uma string digitado
function StrToFloat(valor)   {
  
  var resultado = valor;                         
  var temp = "";
  // Verifica se existem separadores de milhar e virgulas
  if ((valor.indexOf(".") > 0) && (valor.indexOf(",") > 0)){                                             
    // Retira os separadores de milhar
    resultado = ReplaceChar(resultado, ".", "");
    // Troca a virgula por ponto
    resultado = ReplaceChar(resultado, ",", ".");
  }
  else {
    //  Caso so exista virgula...
    if ((valor.indexOf(".") <= 0) && (valor.indexOf(",") > 0)){              
      // Veirica quao longe do fim da string esta a virgula
        // Troca a virgula por ponto
        resultado = ReplaceChar(resultado, ",", ".");
    }   
    else { // So existe um ponto 
      // Verifica quao longe do fim da string esta o ponto
      if (resultado.indexOf(".") < (resultado.length - 3)) {
        // Se trata de um separador de milhar, remove o ponto
        resultado = ReplaceChar(resultado, ".", "");
      }
    }
  } 
  result=parseFloat(resultado);
  if (isNaN(result)) {
  	return parseFloat("0.00");
  } else {
  	return parseFloat((Math.round(result*100))/100);
        //essa formula garante que a precisão é sempre de 2 casas após a virgula
  }
} 

// Converte um Float para uma string no formato brasileiro
function FloatToStr(valor){

  var fracionario;
  var inteiro;
  var resultado;
  // Paga a parte inteira do numero
  inteiro = Math.floor(valor);
  // Paga parte fracionaria do numero
  fracionario = valor - inteiro;
  // Transforma a parte fracionario em um inteiro com 2 algarismos
  fracionario = Math.round(fracionario * 100);
  inteiro = inteiro.toString();
  if (inteiro.length > 3)
  {
    // Coloca os separadores de milhar
    for (i = inteiro.length; i > 0; i = i - 3)
    {                         
      if (i != inteiro.length)
       inteiro = inteiro.substring(0, i) + "." + inteiro.substring(i, inteiro.length);
    }
  }
  // Monta o valor final
  fracionario = fracionario.toString();
  if (parseInt(fracionario) < 10)
  {
    fracionario = "0" + fracionario;
  }
  resultado = inteiro + "," + fracionario;
  return resultado;

}
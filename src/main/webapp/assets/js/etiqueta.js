/* etiqueta.js - Central de Inteligência de Etiquetas CBA DIESEL */

/**
 * 1. DISPARADOR AUTOMÁTICO (MVC Purista)
 * Ao carregar a página, o JS procura se existe um barcode para desenhar.
 */
document.addEventListener("DOMContentLoaded", function() {
    const elementoBarcode = document.getElementById("barcode");

    // Verifica se estamos na página de impressão (que tem o elemento <svg id="barcode">)
    if (elementoBarcode) {
        // Busca os dados que o JSP injetou nos atributos "data-"
        const nf = elementoBarcode.getAttribute("data-nf");
        const largura = elementoBarcode.getAttribute("data-largura");
        
        // Executa o desenho do código de barras
        gerarBarcode("#barcode", nf, largura);
    }
});

/**
 * 2. LÓGICA DE DESENHO
 * Configura o código de barras baseado no tamanho da etiqueta
 */
function gerarBarcode(idElemento, valor, larguraEtiqueta) {
    if (!valor || valor === "null" || valor === "") return;

    const larguraNum = parseFloat(larguraEtiqueta) || 75;
    
    let configuracao = {
        format: "CODE128",
        displayValue: true,
        fontSize: 16,
        margin: 5,
        background: "none"
    };

    // Ajuste de escala baseado na largura física da etiqueta
    if (larguraNum >= 100) {
        configuracao.width = 3;   // Barras grossas para etiquetas grandes (100x149)
        configuracao.height = 70;
    } else if (larguraNum < 60) {
        configuracao.width = 1.2; // Barras finas para etiquetas pequenas (50x30)
        configuracao.height = 30;
        configuracao.fontSize = 12;
    } else {
        configuracao.width = 2;   // Padrão (75x50)
        configuracao.height = 45;
    }

    JsBarcode(idElemento, valor, configuracao);
}

/**
 * 3. LÓGICA DO FORMULÁRIO (Presets)
 * Chamada pelo onchange do select no configurarEtiqueta.jsp
 */
function aplicarPreset() {
    const select = document.getElementById('presetTamanhos');
    const inputW = document.getElementById('inputLargura');
    const inputH = document.getElementById('inputAltura');
    const selectU = document.getElementById('selectUnidade');

    if (!select || select.value === "") return;

    const valores = select.value.split(',');
    if (inputW) inputW.value = valores[0]; 
    if (inputH) inputH.value = valores[1]; 
    if (selectU) selectU.value = valores[2]; 
}
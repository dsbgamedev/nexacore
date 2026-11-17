// AVISO IMPORTANTE: ESTA VERSÃO DELEGA A ORDENAÇÃO E PAGINAÇÃO AO SERVIDOR
// É a abordagem mais robusta para grandes volumes de dados (milhares de fretes).

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('formConsultaFrete');
    const tabelaFretes = document.getElementById('tabelaFretes');
    const tbodyFretes = document.getElementById('tbodyFretesSessao');
    const btnConsultar = form.querySelector('button[type="submit"]');
    const statusMensagem = document.getElementById('statusMensagem');
    const paginacaoContainer = document.getElementById('paginacaoContainer');
	const contextPath = document.body.dataset.appContextPath || document.body.dataset.contextPath || '';
	const urlConsulta = contextPath + '/ConsultaFreteServlet';

    // Inputs Ocultos de Paginação e Ordenação (CRÍTICOS)
    const colunaOrdenacaoInput = document.getElementById('colunaOrdenacao');
    const direcaoOrdenacaoInput = document.getElementById('direcaoOrdenacao');
    const paginaInput = document.getElementById('pagina');
    
    // INPUTS DE EXPORTAÇÃO
    const inputExportType = document.getElementById('exportType');
    const inputDataType = document.getElementById('dataType'); 
    
    // Inputs de Data (Para inicialização)
    const dataInicioInput = document.getElementById('dataInicio');
    const dataFimInput = document.getElementById('dataFim');
    
    // Referências para Totais
    const totalVolumeElement = document.getElementById('totalVolume');
    const totalPesoLiquidoElement = document.getElementById('totalPesoLiquido');
    const totalValorNfElement = document.getElementById('totalValorNf');
    const totalValorFreteElement = document.getElementById('totalValorFrete');
    
    const exportExcelBtn = document.getElementById('exportExcelBtn');
    const exportPdfBtn = document.getElementById('exportPdfBtn');

    // Estado da Aplicação
    let colunaOrdenacao = colunaOrdenacaoInput.value || 'dataLancamento';
    let direcaoOrdenacao = direcaoOrdenacaoInput.value || 'desc';
    let paginaAtual = parseInt(paginaInput.value, 10) || 1;
    let consultaEmAndamento = false; 
    
    // --- Funções de Utilidade ---
    
    function limparMensagem() {
        if (statusMensagem) {
            statusMensagem.classList.add('d-none');
            statusMensagem.textContent = '';
        }
    }
    
    function exibirMensagem(msg, tipo) {
        if (statusMensagem) {
            statusMensagem.textContent = msg;
            statusMensagem.classList.remove('d-none', 'alert-success', 'alert-danger');
            statusMensagem.classList.add(`alert-${tipo}`);
        }
    }
    
    function formatarMoeda(valor) {
        if (valor == null || isNaN(valor)) return 'R$ 0,00';
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
    }
    
    function formatarDecimal(valor) {
        if (valor == null || isNaN(valor)) return '0,00';
        return new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(valor);
    }
    
    function formatarInteiro(valor) {
        if (valor == null || isNaN(valor)) return '0';
        return new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 }).format(valor);
    }

    function formatarData(dataString) {
        if (!dataString) return '';
        if (typeof dataString === 'string' && dataString.match(/^\d{4}-\d{2}-\d{2}$/)) {
            const [ano, mes, dia] = dataString.split('-');
            return `${dia}/${mes}/${ano}`;
        }
        if (dataString.year !== undefined) {
             const day = String(dataString.day).padStart(2, '0');
             const month = String(dataString.month).padStart(2, '0');
             return `${day}/${month}/${dataString.year}`;
        }
        return '';
    }
    
    function formatarHora(hora) {
         if (!hora) return '';
         if (typeof hora === 'string') return hora.substring(0, 5);
         if (hora.hour !== undefined) {
             const hour = String(hora.hour).padStart(2, '0');
             const minute = String(hora.minute).padStart(2, '0');
             return `${hour}:${minute}`;
         }
         return '';
    }
    
    function checkExportButtonsStatus(hasData) {
        if (exportExcelBtn) exportExcelBtn.disabled = !hasData;
        if (exportPdfBtn) exportPdfBtn.disabled = !hasData;
    }

    function getDataAtualFormatada() {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    function inicializarDatas() {
        const dataAtual = getDataAtualFormatada();
        if (dataFimInput) dataFimInput.value = dataAtual;
        if (dataInicioInput) dataInicioInput.value = dataAtual; 
    }

    // --- Renderização ---

    function renderizarTabela(fretes) {
        tbodyFretes.innerHTML = '';
        if (!fretes || fretes.length === 0) {
            const row = tbodyFretes.insertRow();
            const cell = row.insertCell();
            cell.colSpan = 13;
            cell.className = 'text-center text-muted';
            cell.textContent = 'Nenhum frete encontrado. Ajuste os filtros e consulte novamente.';
            checkExportButtonsStatus(false);
            return;
        }

		fretes.forEach(frete => {
		            const row = tbodyFretes.insertRow();
		            
		            // Função auxiliar para criar célula já com o title (tooltip)
		            const addCell = (texto, className = '') => {
		                const cell = row.insertCell();
		                if (className) cell.className = className;
		                const textoStr = texto != null ? String(texto) : '';
		                cell.textContent = textoStr;
		                if (textoStr) cell.setAttribute('title', textoStr);
		                return cell;
		            };

		            addCell(frete.id || '');
		            addCell(formatarData(frete.dataLancamento || frete.dataLancamentoJsp));
		            addCell(formatarHora(frete.horaLancamento || frete.horaLancamentoJsp));
		            addCell(frete.nomeEmpresa || '');
		            addCell(frete.nomeTransportadora || '');
		            addCell(frete.nomeCliente || '');
		            addCell(frete.notaFiscal || '');
		            addCell(formatarInteiro(frete.volume), 'text-end');
		            addCell(formatarDecimal(frete.pesoLiquido), 'text-end');
		            addCell(formatarMoeda(frete.valorNf), 'text-end');
		            addCell(frete.observacao || '');
		            addCell(formatarDecimal(frete.porcentagemFrete) + '%', 'text-end');
		            addCell(formatarMoeda(frete.valorFrete), 'text-end');
		        });
		        checkExportButtonsStatus(true);
    }
    
    function renderizarTotais(totais) {
        if (totalVolumeElement) totalVolumeElement.textContent = formatarInteiro(totais.totalVolume || 0);
        if (totalPesoLiquidoElement) totalPesoLiquidoElement.textContent = formatarDecimal(totais.totalPesoLiquido || 0);
        if (totalValorNfElement) totalValorNfElement.textContent = formatarMoeda(totais.totalValorNf || 0);
        if (totalValorFreteElement) totalValorFreteElement.textContent = formatarMoeda(totais.totalValorFrete || 0);
    }

    function renderizarPaginacao(totalPaginas, page) {
        paginacaoContainer.innerHTML = '';
        if (totalPaginas <= 1) return;
        const ul = document.createElement('ul');
        ul.className = 'pagination';

        // Botões de navegação simplificados para o exemplo
        const criarLink = (texto, alvo, habilitado) => {
            let li = document.createElement('li');
            li.className = `page-item ${habilitado ? '' : 'disabled'}`;
            let a = document.createElement('a');
            a.className = 'page-link';
            a.href = '#';
            a.textContent = texto;
            a.onclick = (e) => { e.preventDefault(); if(habilitado) navegarParaPagina(alvo); };
            li.appendChild(a);
            return li;
        };

        ul.appendChild(criarLink('« Primeira', 1, page > 1));
        ul.appendChild(criarLink('Anterior', page - 1, page > 1));
        
        // Janela de páginas
        for (let i = Math.max(1, page - 2); i <= Math.min(totalPaginas, page + 2); i++) {
            let li = document.createElement('li');
            li.className = `page-item ${i === page ? 'active' : ''}`;
            let a = document.createElement('a');
            a.className = 'page-link';
            a.textContent = i;
            a.onclick = (e) => { e.preventDefault(); if(i !== page) navegarParaPagina(i); };
            li.appendChild(a);
            ul.appendChild(li);
        }

        ul.appendChild(criarLink('Próxima', page + 1, page < totalPaginas));
        ul.appendChild(criarLink('Última »', totalPaginas, page < totalPaginas));
        paginacaoContainer.appendChild(ul);
    }
    
    // --- AJAX ---

    async function consultarFretesAjax() {
        if (consultaEmAndamento) return;
        consultaEmAndamento = true;
        limparMensagem();
        
        const originalText = btnConsultar.innerHTML;
        btnConsultar.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Consultando...';
        btnConsultar.disabled = true;

        colunaOrdenacaoInput.value = colunaOrdenacao;
        direcaoOrdenacaoInput.value = direcaoOrdenacao;
        paginaInput.value = paginaAtual;
        
        const formData = new FormData(form);
        formData.append('limite', 20); 
        formData.delete('exportType');
        formData.delete('dataType'); 
        
        const urlParams = new URLSearchParams(formData).toString();

        try {
            const response = await fetch(urlConsulta, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: urlParams
            });
            
            if (!response.ok) throw new Error('Erro na consulta');
            
            const data = await response.json();
            renderizarTabela(data.fretes);
            renderizarTotais(data);
            paginaAtual = data.paginaAtual || 1;
            renderizarPaginacao(data.totalPaginas || 0, paginaAtual);

        } catch (error) {
            exibirMensagem('Falha ao carregar dados.', 'danger');
        } finally {
            btnConsultar.innerHTML = originalText;
            btnConsultar.disabled = false;
            consultaEmAndamento = false;
        }
    }

    function navegarParaPagina(novaPagina) {
        paginaAtual = novaPagina;
        consultarFretesAjax();
    }
    
    // --- Event Listeners ---

    tabelaFretes.querySelectorAll('th.sortable').forEach(header => {
        header.addEventListener('click', function() {
            if (consultaEmAndamento) return;
            const sortField = header.dataset.sortField;
            direcaoOrdenacao = (sortField === colunaOrdenacao && direcaoOrdenacao === 'asc') ? 'desc' : 'asc';
            colunaOrdenacao = sortField;
            paginaAtual = 1;

            tabelaFretes.querySelectorAll('th.sortable').forEach(th => th.classList.remove('sorted-asc', 'sorted-desc'));
            header.classList.add(direcaoOrdenacao === 'asc' ? 'sorted-asc' : 'sorted-desc');
            consultarFretesAjax();
        });
    });

    form.addEventListener('submit', function(event) {
        if (inputExportType.value) return;
        event.preventDefault();
        paginaAtual = 1; 
        consultarFretesAjax();
    });

    // Exportações
    [exportExcelBtn, exportPdfBtn].forEach(btn => {
        if(btn) {
            btn.addEventListener('click', () => {
                inputExportType.value = (btn === exportExcelBtn) ? 'excel' : 'pdf';
                inputDataType.value = 'fretes';
                colunaOrdenacaoInput.value = colunaOrdenacao;
                direcaoOrdenacaoInput.value = direcaoOrdenacao;
                form.submit();
                inputExportType.value = '';
            });
        }
    });

    // LIMPAR FILTROS - CORRIGIDO E COMPLETO
    document.getElementById('btnLimparFiltros').addEventListener('click', function() {
        form.reset();
        
        // 1. Reset do NOVO Seletor de Coluna
        const campoPesquisaSelect = document.getElementById('campoPesquisa');
        if (campoPesquisaSelect) campoPesquisaSelect.value = 'todos'; 
        
        // 2. Reset da Transportadora
        const transportadoraSelect = document.getElementById('transportadoraId');
        if (transportadoraSelect) transportadoraSelect.value = '0';
        
        // 3. Reset do Termo de Pesquisa (usando o ID correto do JSP)
        const termoPesquisaInput = document.getElementById('termoPesquisa');
        if (termoPesquisaInput) termoPesquisaInput.value = '';
        
        // 4. Reset das Datas e Ordenação
        inicializarDatas();
        colunaOrdenacao = 'dataLancamento';
        direcaoOrdenacao = 'desc';
        paginaAtual = 1;
        
        tabelaFretes.querySelectorAll('th.sortable').forEach(th => th.classList.remove('sorted-asc', 'sorted-desc'));
        const defaultHeader = tabelaFretes.querySelector(`[data-sort-field="${colunaOrdenacao}"]`);
        if (defaultHeader) defaultHeader.classList.add('sorted-desc');
        
        consultarFretesAjax();
    });
	
	// Sincroniza o input de texto da transportadora com o ID oculto
	    const transportadoraBusca = document.getElementById('transportadoraBusca');
	    const transportadoraHidden = document.getElementById('transportadoraId');
	    const datalistTransportadoras = document.getElementById('listaTransportadorasDatalist');

	    if (transportadoraBusca && transportadoraHidden && datalistTransportadoras) {
	        transportadoraBusca.addEventListener('input', function() {
	            const valorDigitado = this.value.trim();
	            const opcoes = datalistTransportadoras.options;
	            let idEncontrado = '0';

	            for (let i = 0; i < opcoes.length; i++) {
	                if (opcoes[i].value === valorDigitado) {
	                    idEncontrado = opcoes[i].getAttribute('data-id');
	                    break;
	                }
	            }
	            transportadoraHidden.value = idEncontrado;
	        });
	    }

    // Inicialização
    inicializarDatas(); 
    if (tbodyFretes.children.length === 0 || (tbodyFretes.children.length === 1 && tbodyFretes.children[0].childElementCount < 3)) {
         consultarFretesAjax(); 
    }
});
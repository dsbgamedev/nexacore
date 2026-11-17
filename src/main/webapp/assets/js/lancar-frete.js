/**
 * Lógica de Lançamento e Gerenciamento de Fretes 
 * (Versão Final: Proteção contra duplicidade + Validação de Transportadora)
 */
(function() {
    // =========================================================
    // VARIÁVEIS DE ESTADO E CONFIGURAÇÃO
    // =========================================================
    let MODO_EDICAO = false;
    let fretesLancados = [];
    let totalRegistros = 0;
    let registrosPorPagina = parseInt(document.getElementById('registrosPorPaginaJSP')?.value) || 30; 
    let paginaAtual = parseInt(document.getElementById('paginaAtualJSP')?.value) || 1;
    let tipoBuscaAtual = 'fretesDoDia'; 
    let termoBuscaAtual = ''; 
    
    let colunaOrdenacao = 'data'; 
    let direcaoOrdenacao = 'DESC';
    
	// Captura corretamente o contexto /nexacore da aplicação
    const contextPath = document.body.dataset.appContextPath || document.body.dataset.contextPath || '';     
    const LANCAR_FRETE_URL = `${contextPath}/LancarFreteServlet`;
    const LISTAR_FRETE_URL = `${contextPath}/ListarFretesServlet`;
    const LISTAR_TRANSP_URL = `${contextPath}/ListarTransportadorasServlet`; 
    const ANALISAR_NOTA_URL = `${contextPath}/AnalisarNotaServlet`; // Nova URL para I.A. 28.04.2026

    const inputPesquisa = document.getElementById('inputPesquisa');
    const btnLimparPesquisa = document.getElementById('btnLimparPesquisa');

    // =========================================================
    // 1. FUNÇÕES DE UTILIDADE E FORMATAÇÃO
    // =========================================================
    
    const formatNumber = (value) => {
        if (value === null || value === undefined || value === '') return '0,00';
        const numberValue = typeof value === 'string' ? parseFloat(String(value).replace(',', '.')) : value;
        return isNaN(numberValue) ? '0,00' : new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(numberValue);
    };

    const formatCurrency = (value) => {
        if (value === null || value === undefined || value === '') return 'R$ 0,00';
        const numberValue = typeof value === 'string' ? parseFloat(String(value).replace(',', '.')) : value;
        return isNaN(numberValue) ? 'R$ 0,00' : new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(numberValue);
    };

    const formatToPlain = (formattedValue) => {
        if (!formattedValue) return '0.00';
        let plain = String(formattedValue).replace('R$', '').trim();
        if (plain.includes(',')) {
            plain = plain.replace(/\./g, '').replace(',', '.');
        }
        const num = parseFloat(plain);
        return isNaN(num) ? '0.00' : num.toFixed(2);
    };
    
    const applyMoneyFormat = (input) => {
        input.value = formatNumber(formatToPlain(input.value));
    };
    
    const validarLimiteNumeric = (input) => {
        let plainValue = formatToPlain(input.value);
        const limit = 99999999.99;
        if (parseFloat(plainValue) > limit) {
            input.value = formatNumber(limit.toFixed(2));
            showAlertModal('Limite Excedido', `Valor máximo: ${formatNumber(limit.toFixed(2))}`, 'warning');
            return false;
        }
        return true;
    };
    
	function calcularValorFrete() {
	    const valorNfInput = document.getElementById('valorNf');
	    const valorFreteHidden = document.getElementById('valorFreteHidden');
	    const porcentagemInput = document.getElementById('porcentagemFrete');
	    const unidadeId = document.getElementById('usuarioUnidadeId')?.value;

	    const valorNfPlain = parseFloat(formatToPlain(valorNfInput?.value));

	    if (!isNaN(valorNfPlain) && valorNfPlain > 0) {
	        let resultado = 0;
	        let percManual = parseFloat(formatToPlain(porcentagemInput?.value)) || 0;

	        // REGRA MATRIZ: Se for ID 8 (SP) e estiver travado, força 2.00%
	        if (unidadeId === "8" && porcentagemInput.readOnly) {
	            percManual = 2.00;
	            porcentagemInput.value = formatNumber(2.00);
	        }

	        resultado = (valorNfPlain * (percManual / 100)).toFixed(2);
	        valorFreteHidden.value = resultado;
	    } else {
	        if(valorFreteHidden) valorFreteHidden.value = '0.00';
	    }
	}

    function initInputMasks() {
        // --- IMPLEMENTAÇÃO DE LETRAS MAIÚSCULAS EM TEMPO REAL ---
		// --- NOVO: IMPLEMENTAÇÃO DE MAIÚSCULAS GLOBAL POR CLASSE ---
	    // Procura por qualquer input ou textarea que use a classe 'maiusculo'
		document.querySelectorAll('.maiusculo').forEach(campo => {
		            campo.addEventListener('input', function() {
		                this.value = this.value.toUpperCase();
		            });
		        });

        document.querySelectorAll('.money-mask').forEach(input => {
            input.addEventListener('focus', function() { 
                this.value = formatToPlain(this.value); 
                this.select(); 
            });
            input.addEventListener('blur', function() {
                validarLimiteNumeric(this);
                applyMoneyFormat(this); 
				// ADICIONE ESTA LINHA: se sair do valor NF ou da Porcentagem, calcula
				if (this.id === 'valorNf' || this.id === 'porcentagemFrete') calcularValorFrete();
				
            });
            input.addEventListener('input', function(e) {
                let value = e.target.value.replace(/[^0-9,]/g, ''); 
                e.target.value = value;
            });
            if (input.value) applyMoneyFormat(input);
        });

        const inputNotaFiscal = document.getElementById('notaFiscal');
        if (inputNotaFiscal) {
            inputNotaFiscal.addEventListener('input', (e) => {
                // Mantém apenas números e converte para maiúsculo (caso haja letras futuramente)
                e.target.value = e.target.value.replace(/\D/g, '').toUpperCase(); 
            });
        }
    }

    const formatIsoDateToBR = (isoDate) => {
        if (!isoDate) return '';
        const parts = String(isoDate).split('-');
        return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : isoDate; 
    };

    // =========================================================
    // 2. RENDERIZAÇÃO E TOTAIS
    // =========================================================

    function adicionarLinhaTabela(frete) {
        const tbody = document.getElementById('tbodyFretesSessao');
        const freteId = frete.idFrete || frete.id || ''; 
        const newRow = tbody.insertRow(-1);
        newRow.setAttribute('data-id', freteId);
        newRow.id = `frete-row-${freteId}`;

        newRow.insertCell(0).textContent = freteId;
        newRow.insertCell(1).textContent = formatIsoDateToBR(frete.dataLancamento); 
        newRow.insertCell(2).textContent = frete.horaLancamento || ''; 
        newRow.insertCell(3).textContent = frete.nomeEmpresa || '';
        newRow.insertCell(4).textContent = frete.nomeTransportadora || '';
        newRow.insertCell(5).textContent = frete.nomeCliente || '';
        newRow.insertCell(6).textContent = frete.notaFiscal || '';
        newRow.insertCell(7).textContent = frete.volume || '0';
        newRow.insertCell(8).textContent = formatNumber(frete.pesoLiquido);
        newRow.insertCell(9).textContent = formatCurrency(frete.valorNf);
        newRow.insertCell(10).textContent = frete.observacao || '';
        newRow.insertCell(11).textContent = formatCurrency(frete.valorFrete);
		// Exibe a porcentagem com o símbolo % (ex: 2,00%)
		newRow.insertCell(12).textContent = formatNumber(frete.porcentagemFrete) + '%';

        const acoes = newRow.insertCell(13);
        acoes.classList.add('td-actions'); 
        acoes.innerHTML = `
            <button class="btn btn-sm btn-info btn-editar-frete me-1" data-id="${freteId}"><i class="fas fa-edit"></i></button>
            <button class="btn btn-sm btn-danger btn-excluir-frete" data-id="${freteId}"><i class="fas fa-trash-alt"></i></button>`;
    }

    function renderizarTabela() {
        const tbody = document.getElementById('tbodyFretesSessao');
        if (!tbody) return;
        tbody.innerHTML = '';
        
        if (fretesLancados.length === 0) {
            document.getElementById('mensagemFretesVazio').style.display = 'block';
        } else {
            document.getElementById('mensagemFretesVazio').style.display = 'none';
            fretesLancados.forEach(f => adicionarLinhaTabela(f));
        }

        calcularTotais();
        renderizarPaginacao();
        atualizarIconesOrdenacao(); 
    }

    function calcularTotais() {
        let tVol = 0, tPeso = 0, tNf = 0, tFrete = 0;
        fretesLancados.forEach(f => {
            tVol   += parseInt(f.volume || 0, 10);
            tPeso  += parseFloat(f.pesoLiquido || 0);
            tNf    += parseFloat(f.valorNf || 0);
            tFrete += parseFloat(f.valorFrete || 0); 
        });
        document.getElementById('totalVolume').textContent = tVol;
        document.getElementById('totalPesoLiquido').textContent = formatNumber(tPeso);
        document.getElementById('totalValorNf').textContent = formatCurrency(tNf);
        document.getElementById('totalValorFrete').textContent = formatCurrency(tFrete);
    }

    function renderizarPaginacao() {
        const container = document.getElementById('paginacaoContainer');
        const statusDiv = document.getElementById('statusPaginacao');
        if (!container || !statusDiv) return;

        const totalPaginas = Math.ceil(totalRegistros / registrosPorPagina);
        if (totalRegistros === 0 || totalPaginas <= 1) {
            container.innerHTML = '';
            statusDiv.textContent = totalRegistros > 0 ? `Mostrando ${totalRegistros} registro(s).` : '';
            return;
        }

        let html = '<ul class="pagination justify-content-center">';
        html += `<li class="page-item ${paginaAtual === 1 ? 'disabled' : ''}"><a class="page-link" href="#" data-page="1">««</a></li>`;
        for (let i = 1; i <= totalPaginas; i++) {
            if (i >= paginaAtual - 2 && i <= paginaAtual + 2) {
                html += `<li class="page-item ${i === paginaAtual ? 'active' : ''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
            }
        }
        html += `<li class="page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}"><a class="page-link" href="#" data-page="${totalPaginas}">»»</a></li></ul>`;
        container.innerHTML = html;

        container.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                const p = parseInt(e.target.dataset.page);
                if (p !== paginaAtual) buscarFretes(tipoBuscaAtual, termoBuscaAtual, p);
            });
        });
    }

    // =========================================================
    // 3. PERSISTÊNCIA, BUSCA E ORDENAÇÃO
    // =========================================================

	function buscarFretes(tipo, termo, pagina) {
	        tipoBuscaAtual = tipo; 
	        termoBuscaAtual = termo; 
	        paginaAtual = pagina;

	        const params = new URLSearchParams({ 
	            action: tipo, 
	            termo: termo || '', 
	            pagina: pagina, 
	            registrosPorPagina: registrosPorPagina,
	            orderBy: colunaOrdenacao, 
	            orderDir: direcaoOrdenacao  
	        });

	        // Certifique-se de que usa LANCAR_FRETE_URL ou a rota exata do servlet
	        fetch(LANCAR_FRETE_URL + '?' + params.toString(), { method: 'POST' })
	        .then(r => r.json())
	        .then(data => {
	            fretesLancados = data.fretes || [];
	            totalRegistros = data.totalRegistros || 0;
	            renderizarTabela();
	        }).catch(err => console.error('Erro:', err));
	    }

    function configurarOrdenacaoCabecalho() {
        document.querySelectorAll('#tabelaFretesLancados thead th[data-coluna]').forEach(th => {
            th.addEventListener('click', () => {
                const coluna = th.getAttribute('data-coluna');
                if (colunaOrdenacao === coluna) {
                    direcaoOrdenacao = (direcaoOrdenacao === 'ASC') ? 'DESC' : 'ASC';
                } else {
                    colunaOrdenacao = coluna;
                    direcaoOrdenacao = 'ASC';
                }
                buscarFretes(tipoBuscaAtual, termoBuscaAtual, 1);
            });
        });
    }

    function atualizarIconesOrdenacao() {
        document.querySelectorAll('#tabelaFretesLancados thead th i.sort-icon').forEach(icon => {
            icon.className = 'fas fa-sort sort-icon'; 
            icon.style.color = '#ccc';
        });
        const thAtivo = document.querySelector(`#tabelaFretesLancados thead th[data-coluna="${colunaOrdenacao}"]`);
        if (thAtivo) {
            const icon = thAtivo.querySelector('i.sort-icon');
            if (icon) {
                icon.className = direcaoOrdenacao === 'ASC' ? 'fas fa-sort-up sort-icon' : 'fas fa-sort-down sort-icon';
                icon.style.color = '#000';
            }
        }
    }

	function handleFormSubmit(e) {
	    e.preventDefault();
		
	// --- NOVA VALIDAÇÃO DE VALORES MAIORES QUE ZERO ---
	    const pesoVal = parseFloat(formatToPlain(document.getElementById('pesoLiquido').value)) || 0;
	    const valorNfVal = parseFloat(formatToPlain(document.getElementById('valorNf').value)) || 0;
	    const porcentagemVal = parseFloat(formatToPlain(document.getElementById('porcentagemFrete').value)) || 0;

	    if (pesoVal <= 0) {
	        showAlertModal('Campo Obrigatório', 'O Peso Líquido deve ser maior que 0,00', 'warning');
	        document.getElementById('pesoLiquido').focus();
	        return;
	    }
	    if (valorNfVal <= 0) {
	        showAlertModal('Campo Obrigatório', 'O Valor da NF deve ser maior que 0,00', 'warning');
	        document.getElementById('valorNf').focus();
	        return;
	    }
	    if (porcentagemVal <= 0) {
	        showAlertModal('Campo Obrigatório', 'A Porcentagem do Frete deve ser maior que 0,00', 'warning');
	        document.getElementById('porcentagemFrete').focus();
	        return;
	    }
	    // --------------------------------------------------

	    const btnSalvar = document.querySelector('button[type="submit"]');
	    const btnTexto = document.getElementById('btnText');

	    if (btnSalvar.disabled) return;

	    // 1. Antes de tudo, força o cálculo do frete com os valores atuais da tela
	    calcularValorFrete();

	    const freteCalculado = parseFloat(document.getElementById('valorFreteHidden').value);
	    
	    // Validação de segurança
	    if (isNaN(freteCalculado) || freteCalculado < 0) {
	        showAlertModal('Erro', 'O valor do frete calculado é inválido.', 'danger');
	        return;
	    }

	    btnSalvar.disabled = true;
	    const originalText = btnTexto.textContent;
	    btnTexto.textContent = 'Salvando...';

	    // 2. LIMPEZA TOTAL: Usamos formatToPlain em todos os campos numéricos
	    const payload = {
	        id: MODO_EDICAO ? parseInt(document.getElementById('idFrete').value) : null,
	        idTransportadora: parseInt(document.getElementById('transportadoraId').value),
	        notaFiscal: document.getElementById('notaFiscal').value.trim(),
	        volume: parseInt(document.getElementById('volume').value) || 0,
	        nomeEmpresa: document.getElementById('nomeEmpresa').value.trim().toUpperCase(),
	        nomeCliente: document.getElementById('nomeCliente').value.trim().toUpperCase(),
	        // Aqui limpamos qualquer máscara de R$, pontos ou vírgulas:
	        pesoLiquido: parseFloat(formatToPlain(document.getElementById('pesoLiquido').value)),
	        valorNf: parseFloat(formatToPlain(document.getElementById('valorNf').value)),
	        valorFrete: freteCalculado, 
			// --- ADICIONE ESTA LINHA ABAIXO ---
		    porcentagemFrete: parseFloat(formatToPlain(document.getElementById('porcentagemFrete').value)) || 0,
	        observacao: document.getElementById('observacao').value.trim().toUpperCase()
	    };

	    fetch(LANCAR_FRETE_URL, {
	        method: MODO_EDICAO ? 'PUT' : 'POST',
	        headers: { 'Content-Type': 'application/json' },
	        body: JSON.stringify(payload)
	    })
	    .then(r => r.json())
	    .then(data => {
	        if(data.success) {
	            showAlertModal('Sucesso', 'Operação realizada!', 'success');
	            resetarFormulario();
	            buscarFretes(tipoBuscaAtual, termoBuscaAtual, MODO_EDICAO ? paginaAtual : 1);
	        } else {
	            showAlertModal('Erro', data.message || 'Erro ao salvar frete.', 'danger');
	            btnSalvar.disabled = false;
	            btnTexto.textContent = originalText;
	        }
	    })
	    .catch(err => {
	        console.error('Erro:', err);
	        showAlertModal('Erro Crítico', 'Erro de comunicação com o servidor (500). Verifique o console do Java.', 'danger');
	        btnSalvar.disabled = false;
	        btnTexto.textContent = originalText;
	    });
	}

	function resetarFormulario() {
	    MODO_EDICAO = false;
	    document.getElementById('formFrete').reset();
	    document.getElementById('idFrete').value = '';
	    
	    const unidadeId = document.getElementById('usuarioUnidadeId')?.value;
	    const campoPct = document.getElementById('porcentagemFrete');

	    // Se for Unidade 8, volta para 2% ao limpar, senão limpa o campo
	    if (unidadeId === "8") {
	        campoPct.value = formatNumber(2.00);
	        campoPct.readOnly = true;
	        const chk = document.getElementById('chkAlterarPorcentagem');
	        if(chk) chk.checked = false;
	        const icone = document.getElementById('iconeCadeado');
	        if(icone) icone.className = "fas fa-lock text-muted";
	    } else {
	        campoPct.value = "";
	        campoPct.readOnly = false;
	    }

	    document.querySelectorAll('.money-mask').forEach(i => {
	        if(i.id !== 'porcentagemFrete') i.value = formatNumber(0);
	    });

	    document.getElementById('valorFreteHidden').value = '0.00';
	    document.getElementById('transportadoraId').value = '';
	    document.getElementById('transportadoraBusca').value = '';
	    document.getElementById('btnText').textContent = 'Inserir na Lista e Salvar';
	    
	    const btnSalvar = document.querySelector('button[type="submit"]');
	    if(btnSalvar) btnSalvar.disabled = false;
	}

    function handleDeleteFrete(id) {
        fetch(`${LANCAR_FRETE_URL}?id=${id}`, { method: 'DELETE' })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                showAlertModal('Sucesso', 'Excluído!', 'success');
                buscarFretes(tipoBuscaAtual, termoBuscaAtual, paginaAtual);
            }
        });
    }

	function carregarFreteParaEdicao(id) {
	    const f = fretesLancados.find(x => (x.idFrete || x.id) == id); 
	    if (!f) return;

	    MODO_EDICAO = true;
	    
	    // Preenchimento dos campos básicos
	    document.getElementById('idFrete').value = f.id || f.idFrete;
	    document.getElementById('transportadoraId').value = f.idTransportadora; 
	    document.getElementById('transportadoraBusca').value = f.nomeTransportadora || '';
	    document.getElementById('notaFiscal').value = f.notaFiscal;
	    document.getElementById('volume').value = f.volume;
	    document.getElementById('nomeEmpresa').value = (f.nomeEmpresa || '').toUpperCase();
	    document.getElementById('nomeCliente').value = (f.nomeCliente || '').toUpperCase();
	    document.getElementById('pesoLiquido').value = formatNumber(f.pesoLiquido);
	    document.getElementById('valorNf').value = formatNumber(f.valorNf);
	    document.getElementById('valorFreteHidden').value = (f.valorFrete || 0).toFixed(2); 
	    document.getElementById('observacao').value = (f.observacao || '').toUpperCase(); 
	    
	    document.getElementById('btnText').textContent = 'Atualizar Frete';
	    document.getElementById('formFrete').scrollIntoView({ behavior: 'smooth' });

	    // --- LÓGICA DA PORCENTAGEM NA EDIÇÃO ---
	    const campoPct = document.getElementById('porcentagemFrete');
	    const unidadeId = document.getElementById('usuarioUnidadeId')?.value;

	    if (f.porcentagemFrete !== undefined && f.porcentagemFrete !== null) {
	        // Se o valor já existe no objeto (veio do banco), usa ele direto
	        campoPct.value = formatNumber(f.porcentagemFrete);
	    } else if (unidadeId !== "8" && f.valorNf > 0) {
	        // Backup: se não houver a porcentagem gravada, faz o cálculo reverso
	        const percRecuperada = (f.valorFrete / f.valorNf) * 100;
	        campoPct.value = formatNumber(percRecuperada);
	    } else if (unidadeId === "8") {
	        // Se for Matriz e estiver nulo, volta para o padrão de 2%
	        campoPct.value = formatNumber(2.00);
	    }
	}

    // =========================================================
    // 4. AUTOCOMPLETE DE TRANSPORTADORA
    // =========================================================
    function initAutocomplete() {
        const inputBusca = document.getElementById('transportadoraBusca');
        const inputId = document.getElementById('transportadoraId');
        const lista = document.getElementById('listaSugestoes');

        if (!inputBusca || !lista) return;

        let selecaoFeita = false;

        inputBusca.addEventListener('input', debounce(function(e) {
            const termo = e.target.value.trim();
            selecaoFeita = false; 

            if (termo.length < 2) { 
                lista.style.display = 'none'; 
                inputId.value = '';
                return; 
            }

            fetch(`${LISTAR_TRANSP_URL}?termo=${encodeURIComponent(termo)}`)
                .then(r => r.json())
                .then(data => {
                    lista.innerHTML = '';
                    if (data && data.length > 0) {
                        data.forEach(item => {
                            const btn = document.createElement('button');
                            btn.type = 'button';
                            btn.className = 'list-group-item list-group-item-action text-start py-2';
                            btn.innerHTML = `<strong>${item.nome || item.razaoSocial}</strong> <small class="text-muted d-block">${item.cidade || ''}</small>`;
                            
                            btn.onmousedown = () => {
                                inputBusca.value = item.nome || item.razaoSocial;
                                inputId.value = item.id;
                                selecaoFeita = true;
                                lista.style.display = 'none';
                            };
                            lista.appendChild(btn);
                        });
                        lista.style.display = 'block';
                    } else { 
                        lista.style.display = 'none'; 
                        inputId.value = '';
                    }
                });
        }, 300));

        inputBusca.addEventListener('blur', function() {
            setTimeout(() => {
                if (!selecaoFeita || inputId.value === '') {
                    const valorDigitado = inputBusca.value.trim();
                    if (valorDigitado !== "") {
                        showAlertModal('Atenção', 'Você deve selecionar uma transportadora da lista sugerida.', 'warning');
                    }
                    inputBusca.value = '';
                    inputId.value = '';
                    lista.style.display = 'none';
                }
            }, 200);
        });

        document.addEventListener('click', (e) => {
            if (!inputBusca.contains(e.target) && !lista.contains(e.target)) lista.style.display = 'none';
        });
    }
	
	// =========================================================
    // NOVO: INTEGRAÇÃO COM I.A. (28.04.2026)
    // =========================================================
    function initIAFoto() {
        const inputFoto = document.getElementById('fotoDanfe');
        const statusIA = document.getElementById('statusIA');

        if (inputFoto) {
            inputFoto.addEventListener('change', function(e) {
                const arquivo = e.target.files[0];
                if (!arquivo) return;

                statusIA.style.display = 'block';
                statusIA.className = "form-text mt-2 text-primary animate-pulse";
                statusIA.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Analisando nota na CBA DIESEL...';

                const formData = new FormData();
                formData.append("imagem", arquivo);

                fetch(`${contextPath}/AnalisarNotaServlet`, {
                    method: 'POST',
                    body: formData
                })
                .then(res => res.json())
                .then(dados => {
                    if (dados.sucesso) {
                        // Preenche os campos usando suas funções de formatação
                        document.getElementById('notaFiscal').value = dados.notaFiscal || '';
                        document.getElementById('nomeCliente').value = (dados.cliente || '').toUpperCase();
                        document.getElementById('pesoLiquido').value = formatNumber(dados.peso || 0);
                        document.getElementById('valorNf').value = formatNumber(dados.valorNf || 0);
                        
                        if (dados.idTransportadora) {
                            document.getElementById('transportadoraId').value = dados.idTransportadora;
                            document.getElementById('transportadoraBusca').value = (dados.nomeTransportadora || '').toUpperCase();
                        }

                        // Dispara o seu cálculo automático de 2% ou filial
                        calcularValorFrete();

                        statusIA.className = "form-text mt-2 text-success fw-bold";
                        statusIA.innerHTML = '<i class="fas fa-check-circle"></i> Nota processada com sucesso!';
                    } else {
                        statusIA.className = "form-text mt-2 text-danger";
                        statusIA.innerHTML = '<i class="fas fa-times-circle"></i> Erro: ' + (dados.mensagem || 'Não foi possível ler.');
                    }
                })
                .catch(err => {
                    statusIA.className = "form-text mt-2 text-danger";
                    statusIA.innerHTML = 'Falha na conexão com o servidor.';
                });
            });
        }
    }

	// =========================================================
	    // 5. INICIALIZAÇÃO E LISTENERS (ATUALIZADO)
	    // =========================================================
	    function debounce(f, d) { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => f.apply(this, a), d); }; }

	    document.addEventListener('DOMContentLoaded', function() {
	        initInputMasks();
	        configurarOrdenacaoCabecalho(); 
	        initAutocomplete(); 
	        
	        document.getElementById('formFrete')?.addEventListener('submit', handleFormSubmit);
	        document.getElementById('btnLimparCancelar')?.addEventListener('click', resetarFormulario);
	        
	        fretesLancados = [];
	        totalRegistros = 0;
	        renderizarTabela(); 
		
			// --- BLOCO DE CONTROLE DE UNIDADE (MATRIZ ID 7 vs FILIAIS) ---
			// --- BLOCO DE CONTROLE DE UNIDADE (MATRIZ ID 7 vs FILIAIS) ---
					    const unidadeId = document.getElementById('usuarioUnidadeId')?.value?.trim();
					    const campoPct = document.getElementById('porcentagemFrete');
					    const containerPerc = document.getElementById('containerPorcentagem');
					    const chkAlterar = document.getElementById('chkAlterarPorcentagem');
					    const icone = document.getElementById('iconeCadeado');
					    const labelTrava = document.getElementById('labelTravaMatriz');

					    // Sempre exibe o container do frete
					    if (containerPerc) containerPerc.style.display = 'block';

					    // Se for a Matriz (ID 7 exato): Aplica a regra restrita de 2% com cadeado
					    if (unidadeId === "7") {
					        if (labelTrava) labelTrava.style.display = 'inline';
					        
					        campoPct.value = formatNumber(2.00);
					        campoPct.readOnly = true;
					        if (icone) icone.className = "fas fa-lock text-muted";
					        if (chkAlterar) chkAlterar.checked = false;

					        chkAlterar.replaceWith(chkAlterar.cloneNode(true));
					        const novoChkAlterar = document.getElementById('chkAlterarPorcentagem');
					        const novoIcone = document.getElementById('iconeCadeado');

					        novoChkAlterar?.addEventListener('change', function() {
					            if (this.checked) {
					                campoPct.readOnly = false;
					                if (novoIcone) novoIcone.className = "fas fa-lock-open text-success";
					            } else {
					                campoPct.readOnly = true;
					                campoPct.value = formatNumber(2.00);
					                if (novoIcone) novoIcone.className = "fas fa-lock text-muted";
					                calcularValorFrete(); 
					            }
					        });
					    } else {
					        // Para qualquer Filial (ou se o ID da sessão não for 7): Esconde o checkbox/cadeado, limpa o campo e deixa totalmente livre
					        if (labelTrava) labelTrava.style.display = 'none';
					        
					        if (campoPct) {
					            campoPct.value = ""; 
					            campoPct.readOnly = false;
					        }
					    }

	        const tbody = document.getElementById('tbodyFretesSessao');
	        tbody?.addEventListener('click', function(e) {
	            const btnDel = e.target.closest('.btn-excluir-frete');
	            const btnEdit = e.target.closest('.btn-editar-frete');
	            if (btnDel) {
	                if (MODO_EDICAO) {
	                    showAlertModal('Ação Não Permitida', 'Salve ou limpe a edição atual antes de excluir.', 'warning');
	                    return;
	                }
	                const id = btnDel.dataset.id;
	                showConfirmModal('Excluir', 'Confirmar exclusão?').then(ok => { if(ok) handleDeleteFrete(id); });
	            }
	            if (btnEdit) carregarFreteParaEdicao(btnEdit.dataset.id);
	        });

			btnLimparPesquisa?.addEventListener('click', () => {
		            if (inputPesquisa) inputPesquisa.value = '';
		            buscarFretes('fretesDoDia', '', 1); 
		        });
	
		        // CORREÇÃO: Dispara a busca geral a partir de 2 caracteres e volta para os fretes do dia se limpar
		        inputPesquisa?.addEventListener('input', debounce((e) => {
		            const t = e.target.value.trim();
		            if (t.length === 0) {
		                buscarFretes('fretesDoDia', '', 1);
		            } else if (t.length >= 2) {
		                buscarFretes('buscaGeral', t, 1);
		            }
		        }, 400));
			 });
})	();
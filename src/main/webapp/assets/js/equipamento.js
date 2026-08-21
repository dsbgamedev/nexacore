document.addEventListener("DOMContentLoaded", function() {
    const inputBusca = document.getElementById("input-busca-produto");
    const listaAutocomplete = document.getElementById("lista-autocomplete");
    const inputIdProduto = document.getElementById("input-id-produto");
    
    const infoTipo = document.getElementById("info-tipo");
    const infoMarca = document.getElementById("info-marca");
    const infoModelo = document.getElementById("info-modelo");
    const infoDetalhes = document.getElementById("info-detalhes");
    
    const containerImg = document.getElementById("container-img-produto");
    const imgProduto = document.getElementById("img-produto");
    
    const inputIdSistema = document.getElementById("input-idsistema");
    const selectDepartamento = document.getElementById("input-departamento");
    const selectStatus = document.getElementById("input-status");
    const selectSituacao = document.getElementById("input-situacao");

    const btnSalvar = document.getElementById("btn-salvar");
    const btnVoltar = document.getElementById("btn-voltar");
    const btnAbrirModal = document.getElementById("btn-abrir-modal-produto");
    const btnLimparBusca = document.getElementById("btn-limpar-busca");
    const btnLimparForm = document.getElementById("btn-limpar-form");

    // Elementos do IP e Checkbox
    const checkPossuiIp = document.getElementById("check-possui-ip");
    const inputIp = document.getElementById("input-ip");

    // Elementos do Modal de Catálogo via Lupa
    const modalElement = document.getElementById("modalBuscaProduto");
    const modalInstance = modalElement ? new bootstrap.Modal(modalElement) : null;
    const modalInputFiltro = document.getElementById("modal-input-filtro");
    const modalTabelaProdutos = document.getElementById("modal-tabela-produtos");

    let listaProdutosGlobal = [];

	// Função auxiliar para gerenciar a exibição da situação com base no status e na origem
	function ajustarSituacaoPorStatus(statusId, situacaoAlvoId = null) {
        if (!selectSituacao) return;

        let textoStatus = "";
        if (selectStatus && selectStatus.selectedIndex >= 0) {
            textoStatus = selectStatus.options[selectStatus.selectedIndex].text.toLowerCase();
        }

        const selectOrigem = document.getElementById("input-origem");
        const codigoOrigemAtual = selectOrigem && selectOrigem.value ? parseInt(selectOrigem.value) : null;
        const ehMatriz161 = (codigoOrigemAtual === 161);

        function garantirOpcao(val, texto) {
            let optExistente = Array.from(selectSituacao.options).find(o => o.value == val);
            if (!optExistente) {
                const novaOpt = document.createElement("option");
                novaOpt.value = val;
                novaOpt.text = texto;
                selectSituacao.appendChild(novaOpt);
            }
        }

        // 1. Status: Em Manutenção
        if (statusId === 2 || textoStatus.includes("manuten")) {
            garantirOpcao("6", "Na Assistência");
            Array.from(selectSituacao.options).forEach(opt => {
                if (!opt.value) return;
                opt.style.display = (opt.value == "6") ? "block" : "none";
            });
            selectSituacao.value = situacaoAlvoId ? situacaoAlvoId : "6";
        } 
        // 2. Status: Baixado
        else if (textoStatus.includes("baixado")) {
            garantirOpcao("7", "Baixado");
            Array.from(selectSituacao.options).forEach(opt => {
                if (!opt.value) return;
                opt.style.display = (opt.value == "7") ? "block" : "none";
            });
            selectSituacao.value = situacaoAlvoId ? situacaoAlvoId : "7";
        } 
        // 3. Status: Inativo
        else if (textoStatus.includes("inativo")) {
            garantirOpcao("10", "Inativo");
            Array.from(selectSituacao.options).forEach(opt => {
                if (!opt.value) return;
                opt.style.display = (opt.value == "10") ? "block" : "none";
            });
            selectSituacao.value = situacaoAlvoId ? situacaoAlvoId : "10";
        } 
		// 4. Status: Ativo
		        else if (statusId === 1 || textoStatus.includes("ativo")) {
		            let temOpcaoValida = false;
		            let primeiraOpcaoVisivel = null;

		            Array.from(selectSituacao.options).forEach(opt => {
		                if (!opt.value) return;
		                const texto = opt.text.toLowerCase();
		                
		                let ehPermitido = texto.includes("uso") || texto.includes("reservado");
		                
		                // REGRA DA SITUAÇÃO: "Disponível" só aparece se a origem for 161 (Matriz)
		                if (ehMatriz161) {
		                    ehPermitido = ehPermitido || texto.includes("disponível");
		                }

		                if (ehPermitido) {
		                    opt.style.display = "block";
		                    if (!primeiraOpcaoVisivel) primeiraOpcaoVisivel = opt.value;
		                    if (opt.value == situacaoAlvoId) temOpcaoValida = true;
		                } else {
		                    opt.style.display = "none";
		                }
		            });
		            
		            // Só define o valor se ele for permitido para a filial atual, senão pega o primeiro disponível ou limpa
		            if (situacaoAlvoId && temOpcaoValida) {
		                selectSituacao.value = situacaoAlvoId;
		            } else {
		                selectSituacao.value = primeiraOpcaoVisivel ? primeiraOpcaoVisivel : "";
		            }
		        } 
		        // 5. Outros Status
		        else {
		            let temOpcaoValida = false;
		            let primeiraOpcaoVisivel = null;

		            Array.from(selectSituacao.options).forEach(opt => {
		                if (!opt.value) return;
		                const texto = opt.text.toLowerCase();
		                
		                if (!ehMatriz161 && texto.includes("disponível")) {
		                    opt.style.display = "none";
		                } else {
		                    opt.style.display = "block";
		                    if (!primeiraOpcaoVisivel) primeiraOpcaoVisivel = opt.value;
		                    if (opt.value == situacaoAlvoId) temOpcaoValida = true;
		                }
		            });

		            if (situacaoAlvoId && temOpcaoValida) {
		                selectSituacao.value = situacaoAlvoId;
		            } else {
		                selectSituacao.value = primeiraOpcaoVisivel ? primeiraOpcaoVisivel : "";
		            }
		        }
		}
    // Listener para quando o usuário alterar o status manualmente na tela
    if (selectStatus) {
        selectStatus.addEventListener("change", function() {
            const statusId = parseInt(this.value);
            ajustarSituacaoPorStatus(statusId);
        });
    }

    // 1. Carrega o próximo ID Sistema automático ao abrir a tela
    async function carregarProximoId() {
        try {
            const response = await fetch('/nexacore/api/equipamentos?acao=proximo-id');
            const data = await response.json();
            if (data.proximoId) {
                inputIdSistema.value = data.proximoId;
            }
        } catch (e) {
            console.error("Erro ao gerar ID automático:", e);
            inputIdSistema.value = "EQ0000000001";
        }
    }
    
    // Carrega as filiais cadastradas para o select de origem
    async function carregarFiliais() {
        try {
            const response = await fetch('/nexacore/api/empresas/');
            if (response.ok) {
                const filiais = await response.json();
                const selectOrigem = document.getElementById("input-origem");
                if (selectOrigem) {
                    selectOrigem.innerHTML = '<option value="">Selecione a origem...</option>';
                    filiais.forEach(f => {
                        const option = document.createElement("option");
                        option.value = f.origemCodigo;
                        option.textContent = `${f.origemCodigo} - ${f.sufixo || f.nomeEmpresa || ''}`;
                        selectOrigem.appendChild(option);
                    });
                }
            }
        } catch (error) {
            console.error("Erro ao carregar filiais:", error);
        }
    }
        
	// 2. Função para carregar os dados do equipamento caso venha um ID na URL (Modo Edição)
    async function carregarEquipamentoParaEdicao() {
        const urlParams = new URLSearchParams(window.location.search);
        const idEquipamento = urlParams.get('id');

        if (!idEquipamento) return;

        try {
            const response = await fetch(`/nexacore/api/equipamentos?id=${idEquipamento}`);
            if (response.ok) {
                const eq = await response.json();
                
                document.getElementById("input-id").value = eq.idEquipamento || eq.id || '';
                
                inputIdSistema.value = eq.idSistema || '';
                document.getElementById("input-patrimonio").value = eq.patrimonio || '';
                document.getElementById("input-numeroserie").value = eq.numeroSerie || '';
                document.getElementById("input-nomeidentificador").value = eq.nomeIdentificador || '';
                
                const selectOrigem = document.getElementById("input-origem");
                if (selectOrigem) {
                    // 1. Define a origem do equipamento carregado imediatamente
                    selectOrigem.value = eq.origemCodigo || '';

                    const codigoOrigemAtual = eq.origemCodigo ? parseInt(eq.origemCodigo) : null;
                    const ehMatriz161 = (codigoOrigemAtual === 161);

                    if (!ehMatriz161 && (eq.idEquipamento == 1 || eq.id == 1 || eq.bloquearOrigem === true || eq.statusMovimentacao === 'EM_DESTINO_EXTERNO' || eq.origemBloqueada === true)) {
                        selectOrigem.disabled = true;
                        selectOrigem.classList.add("bg-light");

                        let avisoOrigem = document.getElementById('avisoBloqueioOrigem');
                        if (!avisoOrigem) {
                            avisoOrigem = document.createElement('div');
                            avisoOrigem.id = 'avisoBloqueioOrigem';
                            avisoOrigem.className = 'form-text text-danger mt-1';
                            avisoOrigem.style.fontSize = '0.75rem';
                            avisoOrigem.innerHTML = '<i class="fa fa-lock me-1"></i> A origem está bloqueada porque o equipamento foi recebido em outra filial. Só será liberada após o retorno oficial (devolução) para a origem original.';
                            selectOrigem.parentNode.appendChild(avisoOrigem);
                        }
                    } else {
                        selectOrigem.disabled = false;
                        selectOrigem.classList.remove("bg-light");
                        const aviso = document.getElementById('avisoBloqueioOrigem');
                        if (aviso) aviso.remove();
                    }
                }
                
                // 3. Define o status correto com base nos dados recebidos
                if (selectStatus) {
                    selectStatus.value = eq.statusId || '';
                    window.statusOriginalEquipamentoId = eq.statusId;
                }
                
                // 4. REGRA: SE JÁ FOI ATIVADO ANTERIORMENTE, REMOVE O STATUS "BAIXADO" DA OPÇÃO
                const STATUS_BAIXADO_ID = 4; // Ajuste para o ID numérico correto do "Baixado" no seu banco
                if (window.statusOriginalEquipamentoId && Number(window.statusOriginalEquipamentoId) !== STATUS_BAIXADO_ID) {
                    if (selectStatus) {
                        Array.from(selectStatus.options).forEach(opt => {
                            if (opt.value == STATUS_BAIXADO_ID) {
                                opt.remove(); // Remove o "Baixado" para que não apareça na lista de seleção
                            }
                        });
                    }
                }
                
                ajustarSituacaoPorStatus(eq.statusId, eq.situacaoId);

                const codigoOrigemAtual = eq.origemCodigo ? parseInt(eq.origemCodigo) : null;
                const ehMatriz161 = (codigoOrigemAtual === 161);

                if (!ehMatriz161 && (eq.idEquipamento == 1 || eq.id == 1 || eq.bloquearOrigem === true || eq.origemBloqueada === true || eq.permiteDisponivel === false)) {
                    if (selectSituacao) {
                        Array.from(selectSituacao.options).forEach(opt => {
                            if (!opt.value) return; 
                            
                            const textoOpt = opt.text.toLowerCase();
                            const ehPermitido = textoOpt.includes("uso") || textoOpt.includes("reservado");
                            
                            if (!ehPermitido) {
                                opt.style.display = "none"; 
                            } else {
                                opt.style.display = "block"; 
                            }
                        });
                    }
                }

                document.getElementById("input-usuario").value = eq.usuarioAtual || '';
                document.getElementById("input-observacoes").value = eq.observacoes || '';

                if (eq.ipAtual) {
                    checkPossuiIp.checked = true;
                    inputIp.disabled = false;
                    inputIp.value = eq.ipAtual;
                    inputIp.classList.remove("bg-light");
                } else {
                    checkPossuiIp.checked = false;
                    inputIp.disabled = true;
                    inputIp.value = "";
                    inputIp.classList.add("bg-light");
                }

                if (selectDepartamento && eq.departamentoId) {
                    selectDepartamento.value = eq.departamentoId;
                }

                if (eq.idProduto && listaProdutosGlobal.length > 0) {
                    const produtoEncontrado = listaProdutosGlobal.find(p => p.id === eq.idProduto);
                    if (produtoEncontrado) {
                        selecionarProduto(produtoEncontrado);
                    }
                }
            } else {
                console.error("Não foi possível carregar os dados do equipamento para edição.");
            }
        } catch (error) {
            console.error("Erro ao buscar equipamento por ID:", error);
        }
    }

    // 3. Carrega todos os produtos para a busca rápida local e para o modal
    async function carregarProdutosCatalogo() {
        try {
            const response = await fetch('/nexacore/api/produtos/consultar');
            if (response.ok) {
                listaProdutosGlobal = await response.json();
            }
        } catch (error) {
            console.error("Erro ao carregar catálogo de produtos:", error);
        }
    }

    // 4. Carrega os departamentos cadastrados na tabela do banco para preencher o <select>
    async function carregarDepartamentos() {
        try {
            const response = await fetch('/nexacore/api/departamentos');
            if (response.ok) {
                const departamentos = await response.json();
                if (selectDepartamento) {
                    selectDepartamento.innerHTML = '<option value="">Selecione o setor...</option>';
                    departamentos.forEach(d => {
                        const option = document.createElement("option");
                        option.value = d.idDepartamento || d.id;
                        option.textContent = d.nomeDepartamento || d.nome;
                        selectDepartamento.appendChild(option);
                    });
                }
            }
        } catch (error) {
            console.error("Erro ao carregar departamentos:", error);
        }
    }
    
    // Carrega as opções para o select de Status do Equipamento
	async function carregarStatusEquipamento() {
        try {
            const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 1));
            const response = await fetch(`${contextPath}/api/status-equipamento`);
            if (response.ok) {
                const listaStatus = await response.json();
                if (selectStatus) {
                    const valorAtual = selectStatus.value;
                    selectStatus.innerHTML = '<option value="">Selecione o status...</option>';
                    
                    const selectOrigem = document.getElementById("input-origem");
                    const origemValor = selectOrigem && selectOrigem.value ? parseInt(selectOrigem.value) : null;
                    const eMatriz161 = (origemValor === 161);

                    if (Array.isArray(listaStatus)) {
                        listaStatus.forEach(s => {
                            if (s.id !== undefined && s.nome) {
                                const nomeStatus = s.nome.toLowerCase();
                                
                                // REGRA DO STATUS: Se NÃO for a matriz (161) e o status for "Inativo", oculta/pula. 
                                // Se for 161, ele passa e aparece normalmente.
                                if (!eMatriz161 && nomeStatus.includes('inativo')) {
                                    return;
                                }

                                const option = document.createElement("option");
                                option.value = s.id;
                                option.textContent = s.nome;
                                selectStatus.appendChild(option);
                            }
                        });
                    }
                    if (valorAtual) {
                        selectStatus.value = valorAtual;
                    }
                }
            }
        } catch (error) {
            console.error("Erro ao carregar status do equipamento:", error);
        }
    }
	
	// Atualiza o select de status caso a origem seja alterada na tela
	    const selectOrigemEl = document.getElementById("input-origem");
	    if (selectOrigemEl) {
	        selectOrigemEl.addEventListener("change", function() {
	            carregarStatusEquipamento();
	        });
	    }

    // Carrega as opções para o select de Situação do Equipamento
    async function carregarSituacaoEquipamento() {
        try {
            const response = await fetch('/nexacore/api/equipamentos/?acaoSituacoes=edicao-direta');
            if (response.ok) {
                const listaSituacao = await response.json();
                if (selectSituacao) {
                    selectSituacao.innerHTML = '<option value="">Selecione a situação...</option>';
                    if (Array.isArray(listaSituacao)) {
                        listaSituacao.forEach(sit => {
                            if (sit.id !== undefined && sit.nome) {
                                const option = document.createElement("option");
                                option.value = sit.id;
                                option.textContent = sit.nome;
                                selectSituacao.appendChild(option);
                            }
                        });
                    }
                }
            }
        } catch (error) {
            console.error("Erro ao carregar situação do equipamento:", error);
        }
    }

    // Inicialização das requisições assíncronas da página
    carregarProximoId();
    carregarDepartamentos();
    carregarFiliais();
    
    // Carrega status/situações e em seguida busca os dados do equipamento caso seja edição
    Promise.all([
        carregarStatusEquipamento(),
        carregarSituacaoEquipamento(),
        carregarProdutosCatalogo()
    ]).then(() => {
        carregarEquipamentoParaEdicao();
    });

    // 5. Comportamento da Barra de Pesquisa (Autocomplete)
    inputBusca.addEventListener("input", function() {
        const termo = this.value.toLowerCase().trim();
        listaAutocomplete.innerHTML = "";
        
        if (termo.length === 0) {
            listaAutocomplete.style.display = "none";
            return;
        }

        const filtrados = listaProdutosGlobal.filter(p => {
            const sku = (p.sku || p.codigoCatalogo || "").toLowerCase();
            const modelo = (p.modelo || "").toLowerCase();
            const marca = (p.marcaNome || p.marca || "").toLowerCase();
            return sku.includes(termo) || modelo.includes(termo) || marca.includes(termo);
        });

        if (filtrados.length > 0) {
            listaAutocomplete.style.display = "block";
            filtrados.forEach(p => {
                const item = document.createElement("a");
                item.href = "#";
                item.className = "list-group-item list-group-item-action small";
                const skuExibicao = p.sku || p.codigoCatalogo || `Produto #${p.id}`;
                const marcaExibicao = p.marcaNome || p.marca || '';
                const modeloExibicao = p.modelo || '';
                item.textContent = `${skuExibicao} - ${marcaExibicao} ${modeloExibicao}`;
                
                item.addEventListener("click", function(e) {
                    e.preventDefault();
                    selecionarProduto(p);
                });
                
                listaAutocomplete.appendChild(item);
            });
        } else {
            listaAutocomplete.style.display = "none";
        }
    });

    document.addEventListener("click", function(e) {
        if (!inputBusca.contains(e.target) && !listaAutocomplete.contains(e.target)) {
            listaAutocomplete.style.display = "none";
        }
    });

    if (btnLimparBusca) {
        btnLimparBusca.addEventListener("click", function() {
            inputBusca.value = "";
            inputIdProduto.value = "";
            listaAutocomplete.style.display = "none";
            
            infoTipo.textContent = "-";
            infoMarca.textContent = "-";
            infoModelo.textContent = "-";
            infoDetalhes.textContent = "-";
            
            containerImg.style.display = "none";
            imgProduto.src = "";
        });
    }

    if (checkPossuiIp && inputIp) {
        checkPossuiIp.addEventListener("change", function() {
            if (this.checked) {
                inputIp.disabled = false;
                inputIp.classList.remove("bg-light");
            } else {
                inputIp.disabled = true;
                inputIp.value = "";
                inputIp.classList.add("bg-light");
            }
        });
    }

    if (btnAbrirModal && modalInstance) {
        btnAbrirModal.addEventListener("click", function() {
            renderizarTabelaModal(listaProdutosGlobal);
            if (modalInputFiltro) modalInputFiltro.value = "";
            modalInstance.show();
        });
    }

    if (modalInputFiltro) {
        modalInputFiltro.addEventListener("input", function() {
            const termo = this.value.toLowerCase().trim();
            const filtrados = listaProdutosGlobal.filter(p => {
                const sku = (p.sku || p.codigoCatalogo || "").toLowerCase();
                const modelo = (p.modelo || "").toLowerCase();
                const marca = (p.marcaNome || p.marca || "").toLowerCase();
                const tipo = (p.tipoNome || p.tipo || "").toLowerCase();
                return sku.includes(termo) || modelo.includes(termo) || marca.includes(termo) || tipo.includes(termo);
            });
            renderizarTabelaModal(filtrados);
        });
    }

    function renderizarTabelaModal(produtos) {
        modalTabelaProdutos.innerHTML = "";
        if (produtos.length === 0) {
            modalTabelaProdutos.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">Nenhum produto encontrado.</td></tr>`;
            return;
        }

        produtos.forEach(p => {
            const tr = document.createElement("tr");
            const sku = p.sku || p.codigoCatalogo || `#${p.id}`;
            
            const tipo = p.nomeTipo || p.tipoNome || p.tipo || '-';
            const marca = p.nomeMarca || p.marcaNome || p.marca || '-';
            const modelo = p.modelo || '-';

            tr.innerHTML = `
                <td><strong>${sku}</strong></td>
                <td>${tipo}</td>
                <td>${marca}</td>
                <td>${modelo}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-primary px-2 py-1 select-prod-btn" type="button">
                        <i class="fa fa-check me-1"></i> Selecionar
                    </button>
                </td>
            `;

            tr.querySelector(".select-prod-btn").addEventListener("click", function() {
                selecionarProduto(p);
                if (modalInstance) modalInstance.hide();
            });

            modalTabelaProdutos.appendChild(tr);
        });
    }

    function selecionarProduto(prod) {
        inputIdProduto.value = prod.id;
        inputBusca.value = prod.sku || prod.codigoCatalogo || `Produto #${prod.id}`;
        listaAutocomplete.style.display = "none";

        infoTipo.textContent = prod.nomeTipo || prod.tipoNome || prod.tipo || '-';
        infoMarca.textContent = prod.nomeMarca || prod.marcaNome || prod.marca || '-';
        infoModelo.textContent = prod.modelo || '-';
        
        let detalhesTexto = prod.descricaoDetalhada || prod.descricaoResumida;
        
        if (!detalhesTexto) {
            let partes = [];
            const marca = prod.nomeMarca || prod.marcaNome || prod.marca;
            const modelo = prod.modelo;
            
            if (marca) partes.push(`Marca: ${marca}`);
            if (modelo) partes.push(`Modelo: ${modelo}`);
            
            for (let key in prod) {
                if (['processador', 'memoria', 'armazenamento', 'disco', 'tamanho', 'potencia', 'resolucao'].includes(key.toLowerCase()) && prod[key]) {
                    let nomeAtributo = key.charAt(0).toUpperCase() + key.slice(1);
                    partes.push(`${nomeAtributo}: ${prod[key]}`);
                }
            }
            
            detalhesTexto = partes.length > 0 ? partes.join(', ') : (prod.detalhes || prod.observacoes || 'N/A');
        }

        infoDetalhes.textContent = detalhesTexto;

        const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 1));
        let imagemAtualIndex = 0;
        let caminhosImagens = [];

        if (prod.caminhosImagens && prod.caminhosImagens.length > 0) {
            caminhosImagens = prod.caminhosImagens.map(img => {
                let nomeArquivo = img;
                if (img.includes('\\')) {
                    nomeArquivo = img.substring(img.lastIndexOf('\\') + 1);
                } else if (img.includes('/')) {
                    nomeArquivo = img.substring(img.lastIndexOf('/') + 1);
                }
                return contextPath + '/api/imagens/' + nomeArquivo;
            });
        } else if (prod.imagemUrl || prod.foto || prod.caminhoImagem) {
            let imgBruta = prod.imagemUrl || prod.foto || prod.caminhoImagem;
            let nomeArquivo = imgBruta;
            if (imgBruta.includes('\\')) {
                nomeArquivo = imgBruta.substring(imgBruta.lastIndexOf('\\') + 1);
            } else if (imgBruta.includes('/')) {
                nomeArquivo = imgBruta.substring(imgBruta.lastIndexOf('/') + 1);
            }
            caminhosImagens = [contextPath + '/api/imagens/' + nomeArquivo];
        }

        const btnPrev = document.getElementById("btn-prev-img");
        const btnNext = document.getElementById("btn-next-img");
        const indicadorImagens = document.getElementById("indicador-imagens");

        function atualizarExibicaoImagem() {
            if (caminhosImagens.length > 0) {
                let imgSrc = caminhosImagens[imagemAtualIndex];
                
                imgProduto.src = '';
                setTimeout(() => {
                    imgProduto.src = imgSrc;
                    containerImg.style.display = "block"; 
                }, 10);

                if (caminhosImagens.length > 1) {
                    if (btnPrev) btnPrev.style.display = "block";
                    if (btnNext) btnNext.style.display = "block";
                    if (indicadorImagens) {
                        indicadorImagens.style.display = "inline-block";
                        indicadorImagens.textContent = `${imagemAtualIndex + 1} / ${caminhosImagens.length}`;
                    }
                } else {
                    if (btnPrev) btnPrev.style.display = "none";
                    if (btnNext) btnNext.style.display = "none";
                    if (indicadorImagens) indicadorImagens.style.display = "none";
                }
            } else {
                containerImg.style.display = "none";
            }
        }

        if (btnPrev) {
            btnPrev.onclick = function() {
                imagemAtualIndex = (imagemAtualIndex > 0) ? imagemAtualIndex - 1 : caminhosImagens.length - 1;
                atualizarExibicaoImagem();
            };
        }

        if (btnNext) {
            btnNext.onclick = function() {
                imagemAtualIndex = (imagemAtualIndex < caminhosImagens.length - 1) ? imagemAtualIndex + 1 : 0;
                atualizarExibicaoImagem();
            };
        }

        imgProduto.onclick = function() {
            if (caminhosImagens.length > 0) {
                const modalZoomEl = document.getElementById("modalZoomImagem");
                const imgZoomModal = document.getElementById("img-zoom-modal");
                if (modalZoomEl && imgZoomModal) {
                    imgZoomModal.src = caminhosImagens[imagemAtualIndex];
                    const modalZoomInstance = new bootstrap.Modal(modalZoomEl);
                    modalZoomInstance.show();
                }
            }
        };

        atualizarExibicaoImagem();
    }

    if (btnVoltar) {
        btnVoltar.addEventListener("click", (e) => {
            e.preventDefault();
            history.back();
        });
    }

    if (btnLimparForm) {
        btnLimparForm.addEventListener("click", function() {
            document.getElementById("input-id").value = "";
            document.getElementById("input-patrimonio").value = "";
            document.getElementById("input-numeroserie").value = "";
            document.getElementById("input-nomeidentificador").value = "";
            document.getElementById("input-origem").value = "";
            document.getElementById("input-ip").value = "";
            document.getElementById("input-status").value = "";
            document.getElementById("input-situacao").value = "";
            document.getElementById("input-usuario").value = "";
            if (selectDepartamento) selectDepartamento.value = "";
            document.getElementById("input-observacoes").value = "";
            
            if (checkPossuiIp && inputIp) {
                checkPossuiIp.checked = true;
                inputIp.disabled = false;
                inputIp.classList.remove("bg-light");
            }
            
            carregarProximoId();
        });
    }

    if (btnSalvar) {
        btnSalvar.addEventListener("click", async function() {
            const payload = {
                idEquipamento: document.getElementById("input-id").value ? parseInt(document.getElementById("input-id").value) : 0,
                idProduto: parseInt(inputIdProduto.value),
                idSistema: inputIdSistema.value.trim(),
                patrimonio: document.getElementById("input-patrimonio").value.trim(),
                numeroSerie: document.getElementById("input-numeroserie").value.trim(),
                nomeIdentificador: document.getElementById("input-nomeidentificador").value.trim(),
                origemCodigo: (function() {
                    const elOrigem = document.getElementById("input-origem");
                    if (!elOrigem) return null;
                    return elOrigem.value ? parseInt(elOrigem.value) : null;
                })(),
                ipAtual: checkPossuiIp && checkPossuiIp.checked ? document.getElementById("input-ip").value.trim() : "",
                
                statusId: selectStatus && selectStatus.value ? parseInt(selectStatus.value) : null,
                situacaoId: selectSituacao && selectSituacao.value ? parseInt(selectSituacao.value) : null,

                usuarioAtual: document.getElementById("input-usuario").value.trim(),
                departamentoId: selectDepartamento && selectDepartamento.value ? parseInt(selectDepartamento.value) : null,
                observacoes: document.getElementById("input-observacoes").value.trim()
            };

            if (!payload.idProduto || !payload.idSistema || !payload.patrimonio || !payload.nomeIdentificador || !payload.origemCodigo || !payload.statusId || !payload.situacaoId) {
                if (typeof ModalService !== 'undefined') {
                    await ModalService.error("Campos Obrigatórios", "Por favor, selecione um produto, preencha os campos obrigatórios (*) e informe o status e a situação.");
                } else {
                    alert("Por favor, selecione um produto, preencha os campos obrigatórios (*) e informe o status e a situação.");
                }
                return;
            }
			
			// NOVA REGRA: Trava para itens já baixados
            /*const statusOriginalId = window.statusOriginalEquipamentoId || null;
            const STATUS_BAIXADO_ID = 4; // Ajuste o número 4 caso o ID do status "Baixado" no seu banco seja diferente
            
            if (statusOriginalId && Number(statusOriginalId) === STATUS_BAIXADO_ID && payload.statusId !== STATUS_BAIXADO_ID) {
                if (typeof ModalService !== 'undefined') {
                    await ModalService.warning("Atenção", "Selecione outro status, pois o item já foi baixado.");
                } else {
                    alert("Selecione outro status, pois o item já foi baixado.");
                }
                return;
            }*/

            try {
                const response = await fetch('/nexacore/api/equipamentos', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const result = await response.json();

                if (response.ok && result.sucesso) {
                    if (typeof ModalService !== 'undefined') {
                        await ModalService.success("Sucesso", result.mensagem);
                    } else {
                        alert(result.mensagem);
                    }
                    window.location.href = '/nexacore/jsp/consulta-equipamento.jsp';
                } else {
                    if (typeof ModalService !== 'undefined') {
                        await ModalService.error("Erro", result.erro || "Não foi possível salvar o equipamento.");
                    } else {
                        alert(result.erro || "Erro ao salvar.");
                    }
                }
            } catch (error) {
                console.error("Erro técnico:", error);
                if (typeof ModalService !== 'undefined') {
                    await ModalService.error("Erro Técnico", "Falha de comunicação com o servidor.");
                } else {
                    alert("Erro técnico de comunicação.");
                }
            }
        });
    }
});
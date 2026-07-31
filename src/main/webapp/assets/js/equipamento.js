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
    const selectDepartamento = document.getElementById("input-departamento"); // Elemento Select atualizado
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
	                        // O value recebe o código numérico (ex: 161)
	                        option.value = f.origemCodigo;
	                        // O texto exibe no formato solicitado: Código - Sufixo (ex: 161 - ssa)
	                        option.textContent = `${f.origemCodigo} - ${f.sufixo}`;
	                        selectOrigem.appendChild(option);
	                    });
	                }
	            }
	        } catch (error) {
	            console.error("Erro ao carregar filiais:", error);
	        }
	    }
		
	//2. Função para carregar os dados do equipamento caso venha um ID na URL (Modo Edição)
    async function carregarEquipamentoParaEdicao() {
        const urlParams = new URLSearchParams(window.location.search);
        const idEquipamento = urlParams.get('id');

        if (!idEquipamento) return; // Se não tem ID, é um cadastro novo (mantém fluxo normal)

        try {
            const response = await fetch(`/nexacore/api/equipamentos?id=${idEquipamento}`);
            if (response.ok) {
                const eq = await response.json();
                
                // Preenche o ID oculto para indicar edição
                document.getElementById("input-id").value = eq.idEquipamento || eq.id || '';
                
                // Preenche os campos de texto comuns
                inputIdSistema.value = eq.idSistema || '';
                document.getElementById("input-patrimonio").value = eq.patrimonio || '';
                document.getElementById("input-numeroserie").value = eq.numeroSerie || '';
                document.getElementById("input-nomeidentificador").value = eq.nomeIdentificador || '';
                document.getElementById("input-origem").value = eq.origemCodigo || '';
                document.getElementById("input-status").value = eq.statusAtual || 'Ativo';
                document.getElementById("input-usuario").value = eq.usuarioAtual || '';
                document.getElementById("input-observacoes").value = eq.observacoes || '';

                // IP e Checkbox
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

                // Departamento
                if (selectDepartamento && eq.departamentoId) {
                    selectDepartamento.value = eq.departamentoId;
                }

                // Se o equipamento já estiver vinculado a um produto do catálogo, seleciona-o automaticamente
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
                    // Mantém apenas a primeira option padrão ("Selecione o setor...")
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

    carregarProximoId();
    carregarProdutosCatalogo();
    carregarDepartamentos(); // Executa o carregamento dos setores ao iniciar
	carregarFiliais(); // <-- Adicionado aqui
	// Carrega o catálogo e logo em seguida verifica se é edição
    carregarProdutosCatalogo().then(() => {
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

    // Fecha a listagem se clicar fora
    document.addEventListener("click", function(e) {
        if (!inputBusca.contains(e.target) && !listaAutocomplete.contains(e.target)) {
            listaAutocomplete.style.display = "none";
        }
    });

    // Botão de Limpar / Refresh da Busca de Produto (Coluna Esquerda)
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

    // Controle do Checkbox de IP Atual (Ativa/Desativa o campo)
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

    // 6. Abertura do Modal de Pesquisa via Lupa
    if (btnAbrirModal && modalInstance) {
        btnAbrirModal.addEventListener("click", function() {
            renderizarTabelaModal(listaProdutosGlobal);
            if (modalInputFiltro) modalInputFiltro.value = "";
            modalInstance.show();
        });
    }

    // Filtro dinâmico dentro do Modal
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

    // 7. Selecionar o produto e preencher os campos
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

    // Botão Voltar
    if (btnVoltar) {
        btnVoltar.addEventListener("click", (e) => {
            e.preventDefault();
            history.back();
        });
    }

    // Botão de Limpar Formulário (Coluna Direita)
    if (btnLimparForm) {
        btnLimparForm.addEventListener("click", function() {
            document.getElementById("input-patrimonio").value = "";
            document.getElementById("input-numeroserie").value = "";
            document.getElementById("input-nomeidentificador").value = "";
            document.getElementById("input-origem").value = "";
            document.getElementById("input-ip").value = "";
            document.getElementById("input-status").value = "Ativo";
            document.getElementById("input-usuario").value = "";
            if (selectDepartamento) selectDepartamento.value = ""; // Reseta o select
            document.getElementById("input-observacoes").value = "";
            
            // Restaura o checkbox e o campo de IP habilitado por padrão
            if (checkPossuiIp && inputIp) {
                checkPossuiIp.checked = true;
                inputIp.disabled = false;
                inputIp.classList.remove("bg-light");
            }
            
            carregarProximoId();
        });
    }

    // 8. Salvar Equipamento
    if (btnSalvar) {
		btnSalvar.addEventListener("click", async function() {
            const payload = {
                // Captura o ID oculto (se for 0 ou vazio, o Java interpreta como INSERT, se tiver valor, faz UPDATE)
                idEquipamento: document.getElementById("input-id").value ? parseInt(document.getElementById("input-id").value) : 0,
                
                idProduto: parseInt(inputIdProduto.value),
                idSistema: inputIdSistema.value.trim(),
                patrimonio: document.getElementById("input-patrimonio").value.trim(),
                numeroSerie: document.getElementById("input-numeroserie").value.trim(),
                nomeIdentificador: document.getElementById("input-nomeidentificador").value.trim(),
                origemCodigo: document.getElementById("input-origem").value ? parseInt(document.getElementById("input-origem").value) : null,
                ipAtual: checkPossuiIp && checkPossuiIp.checked ? document.getElementById("input-ip").value.trim() : "",
                statusAtual: document.getElementById("input-status").value,
                usuarioAtual: document.getElementById("input-usuario").value.trim(),
                departamentoId: selectDepartamento && selectDepartamento.value ? parseInt(selectDepartamento.value) : null,
                observacoes: document.getElementById("input-observacoes").value.trim()
            };
            if (!payload.idProduto || !payload.idSistema || !payload.patrimonio || !payload.nomeIdentificador || !payload.origemCodigo) {
                if (typeof ModalService !== 'undefined') {
                    await ModalService.error("Campos Obrigatórios", "Por favor, selecione um produto e preencha os campos obrigatórios (*).");
                } else {
                    alert("Por favor, selecione um produto e preencha os campos obrigatórios (*).");
                }
                return;
            }

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
					// REDIRECIONA PARA A TELA DE CONSULTA APÓS SALVAR:
					   window.location.href = '/nexacore/jsp/consulta-equipamento.jsp'; // (Ajuste o caminho exato da sua página de consulta se necessário)
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
/**
 * Produto.js - Módulo de Cadastro e Edição de Produto
 * Gerencia a carga dinâmica de campos e suporte a edição via ID na URL.
 */

document.addEventListener("DOMContentLoaded", function() {
    const selectTipo = document.getElementById("select-tipo-produto");
    const selectMarca = document.getElementById("marcaId");
    const divCamposDinamicos = document.getElementById("div-campos-dinamicos");
    const containerAtributos = document.getElementById("container-atributos-tecnicos");
    const containerLista = document.getElementById("container-lista-campos");
    const descInput = document.getElementById('desc-resumida');
    const skuInput = document.getElementById('sku');

    const urlParams = new URLSearchParams(window.location.search);
    const produtoIdEmEdicao = urlParams.get('id');

    const txtObservacoes = document.getElementById('txt-observacoes');
    const charCount = document.getElementById('char-count');
    
    if (txtObservacoes) {
        txtObservacoes.addEventListener('input', function() {
            charCount.textContent = this.value.length;
        });
    }

    const galleryContainer = document.getElementById('gallery-container');
    const imgDestaque = document.getElementById('img-destaque-preview');
    const fileUpload = document.getElementById('file-upload');
    let listaImagens = [];

    const btnSalvar = document.querySelector('.btn-primary');

    if (produtoIdEmEdicao) {
        const tituloPagina = document.querySelector("h4.page-title, h3.page-title, .breadcrumb-header h4");
        if (tituloPagina) tituloPagina.innerText = "EDITAR PRODUTO";
        if (btnSalvar) btnSalvar.innerHTML = '<i class="fas fa-save me-1"></i> Atualizar Produto';
    }

    // Inicialização assíncrona sequencial correta (Garante marcas carregadas antes da edição)
    async function inicializarPagina() {
        await carregarTipos();
        await carregarMarcas();

        if (produtoIdEmEdicao) {
            await carregarProdutoParaEdicao(produtoIdEmEdicao);
        }
    }

    inicializarPagina();

    selectTipo.addEventListener("change", async function() {
        verificarEstadoFormulario(); 
        if (this.value) {
            await carregarCampos(this.value);
        }
    });

    if (selectMarca) {
        selectMarca.addEventListener("change", function() {
            gerarSKU();
            atualizarDescricao();
        });
    }

    containerAtributos.addEventListener('input', function(event) {
        if (event.target.classList.contains('dynamic-attribute')) {
            atualizarDescricao();
            gerarSKU();
        }
    });

    if (btnSalvar) {
        btnSalvar.addEventListener('click', function(event) {
            event.preventDefault(); 
            if (produtoIdEmEdicao) {
                atualizarProduto(produtoIdEmEdicao);
            } else {
                salvarProduto();
            }
        });
    }
    
    const btnCancelar = document.getElementById('btn-cancelar');
    if (btnCancelar) {
        btnCancelar.addEventListener('click', function(event) {
            event.preventDefault();
            if (produtoIdEmEdicao) {
                window.location.href = 'consulta-produto.jsp'; 
            } else {
                limparFormulario();
            }
        });
    }
    
    const btnAlterarDestaque = document.getElementById('btn-alterar-destaque');
    if (btnAlterarDestaque) {
        btnAlterarDestaque.addEventListener('click', () => fileUpload.click());
    }
    
	const btnRemoverDestaque = document.getElementById('btn-remover-destaque');
	    if (btnRemoverDestaque) {
	        btnRemoverDestaque.addEventListener('click', function() {
	            if (listaImagens.length > 0) {
	                // Remove a primeira imagem da lista (que é a imagem destaque/principal atual)
	                listaImagens.shift();
	                
	                // Se houver miniaturas na galeria, remove visualmente a primeira miniatura da tela
	                if (galleryContainer) {
	                    const primeiraThumb = galleryContainer.querySelector('.gallery-item');
	                    if (primeiraThumb) {
	                        primeiraThumb.remove();
	                    }
	                }
	                
	                // Atualiza o preview do destaque (se restaram imagens, exibe a próxima; se não, limpa)
	                atualizarDestaque();
	            }
	        });
	    }
	
	function atualizarDestaque() {
        if (!imgDestaque) return;

        if (listaImagens.length > 0) {
            let imgSrc = listaImagens[0];
            if (!imgSrc.startsWith('data:image') && !imgSrc.startsWith('http') && !imgSrc.startsWith('/nexacore/api/imagens/')) {
                imgSrc = `/nexacore/api/imagens/${imgSrc}`;
            }
            
            // Limpa o src temporariamente para evitar conflito de buffer e o "glitch" colorido
            imgDestaque.src = '';
            
            // Força o navegador a carregar a nova imagem de forma limpa
            setTimeout(() => {
                imgDestaque.src = imgSrc;
                imgDestaque.style.display = 'block';
            }, 10);
        } else {
            imgDestaque.src = '';
            imgDestaque.style.display = 'none';
        }
        
        renderizarGaleria();
    }
	
	function renderizarGaleria() {
        if (!galleryContainer) return;

        // Mantém apenas o bloco base de adicionar
        galleryContainer.innerHTML = `
            <div id="btn-trigger-upload" class="border border-dashed rounded p-3 text-center d-flex flex-column align-items-center justify-content-center" 
                 style="width: 100px; height: 100px; cursor: pointer;">
                <i class="fa fa-plus text-muted"></i>
                <small class="text-muted">Adicionar</small>
            </div>
        `;

        // Associa o evento de clique de forma segura pelo JS
        const btnTriggerUpload = document.getElementById('btn-trigger-upload');
        if (btnTriggerUpload) {
            btnTriggerUpload.onclick = function() {
                if (fileUpload) fileUpload.click();
            };
        }

        // Adiciona cada imagem da lista na galeria
        listaImagens.forEach((imgSrc, index) => {
            let urlFinal = imgSrc;
            if (!urlFinal.startsWith('data:image') && !urlFinal.startsWith('http') && !urlFinal.startsWith('/nexacore/api/imagens/')) {
                urlFinal = `/nexacore/api/imagens/${urlFinal}`;
            }

            const div = document.createElement('div');
            div.className = 'gallery-item';
            div.style.position = 'relative';
            div.style.width = '100px';
            div.style.height = '100px';
            
            const isPrincipal = (index === 0);

            div.innerHTML = `
                <img src="${urlFinal}" draggable="false" style="width:100px; height:100px; object-fit:cover; border-radius: 4px; ${isPrincipal ? 'border: 2px solid #0d6efd;' : ''}">
                <button type="button" class="btn btn-sm btn-danger btn-remove-thumb" style="position:absolute; top:2px; right:2px; padding: 0px 5px; font-size: 12px;" title="Remover">×</button>
                ${!isPrincipal ? `<button type="button" class="btn btn-sm btn-light btn-make-primary" style="position:absolute; bottom:2px; left:2px; padding: 0px 4px; font-size: 10px;" title="Clique para tornar esta imagem a principal"><i class="fa fa-star text-warning"></i></button>` : `<span class="badge bg-primary" style="position:absolute; bottom:2px; left:2px; font-size: 9px;">Principal</span>`}
            `;

            // Ação de Remover
            div.querySelector('.btn-remove-thumb').onclick = function(e) {
                e.stopPropagation();
                listaImagens.splice(index, 1);
                atualizarDestaque();
            };

            // Ação de Tornar Principal
            if (!isPrincipal) {
                div.querySelector('.btn-make-primary').onclick = function(e) {
                    e.stopPropagation();
                    const itemMovido = listaImagens.splice(index, 1)[0];
                    listaImagens.unshift(itemMovido);
                    atualizarDestaque();
                };
            }

            galleryContainer.appendChild(div);
        });
    }

    function verificarEstadoFormulario() {
        const tipoValido = selectTipo && selectTipo.value !== "";
        if (tipoValido) {
            divCamposDinamicos.style.display = "flex";
        } else {
            divCamposDinamicos.style.display = "none";
        }
        if (skuInput && !produtoIdEmEdicao) {
            skuInput.value = "";
        }
    }        

	function processarArquivo(file) {
	        if (file.size > 5 * 1024 * 1024) {
	            ModalService.error("Erro", `A imagem ${file.name} é muito grande!`);
	            return;
	        }

	        const reader = new FileReader();
	        reader.onload = function(event) {
	            const base64 = event.target.result;
	            
	            // Se preferir que a imagem recém-adicionada vire a principal imediatamente, use unshift():
	            // listaImagens.unshift(base64);
	            
	            // Se preferir que ela vá para o final da galeria, use push():
	            listaImagens.push(base64);

	            // Atualiza o destaque e redesenha toda a galeria de forma organizada
	            atualizarDestaque();
	        };
	        reader.readAsDataURL(file);
	    }

	    if (fileUpload) {
	        fileUpload.addEventListener('change', function(e) {
	            Array.from(e.target.files).forEach(processarArquivo);
	            e.target.value = ''; 
	        });
	    }

    async function carregarTipos() {
        try {
            const response = await fetch('/nexacore/api/produtos/listar-tipos');
            const tipos = await response.json();
            
            selectTipo.innerHTML = '<option value="">-- Selecione --</option>';
            tipos.forEach(t => {
                selectTipo.innerHTML += `<option value="${t.id}">${t.nome}</option>`;
            });
        } catch (error) {
            console.error("Erro ao carregar tipos:", error);
        }
    }

	async function carregarMarcas() {
	        try {
	            const response = await fetch('/nexacore/api/produtos/listar-marcas');
	            const marcas = await response.json();
	            
	            if (selectMarca) {
	                selectMarca.innerHTML = '<option value="">-- Selecione --</option>';
	                if (marcas && Array.isArray(marcas)) {
	                    marcas.forEach(m => {
	                        const id = m.marcaId || m.idMarca || m.id;
	                        const nome = m.nomeMarca || m.nome || m.descricao;
	                        
	                        if (id && nome) {
	                            selectMarca.innerHTML += `<option value="${id}">${nome}</option>`;
	                        }
	                    });
	                }
	            }
	        } catch (error) {
	            console.error("Erro ao carregar marcas:", error);
	        }
	    }

    async function carregarCampos(tipoId) {
        try {
            const response = await fetch(`/nexacore/api/produtos/buscar-campos?tipoId=${tipoId}`);
            const campos = await response.json();

            containerAtributos.innerHTML = ""; 
            containerLista.innerHTML = ""; 

            if (!campos || campos.length === 0) {
                divCamposDinamicos.style.display = "none";
                return;
            }

            divCamposDinamicos.style.display = "flex";

            campos.forEach(campo => {
                const divCol = document.createElement("div");
                divCol.className = "col-md-6";
                
                const tooltipIcon = campo.tooltip ? `<i class="fa fa-info-circle ms-1" title="${campo.tooltip}"></i>` : "";
                const nomeNormalizado = campo.nomeAtributo.toLowerCase().trim();
                
                let inputId = (nomeNormalizado === "modelo") ? "input-modelo" : `input-attr-${campo.id || campo.nomeAtributo.replace(/\s+/g, '-').toLowerCase()}`;

                divCol.innerHTML = `
                    <label class="form-label">${campo.nomeAtributo} ${campo.obrigatorio ? '*' : ''} ${tooltipIcon}</label>
                    <input type="${campo.tipoDado === 'number' ? 'number' : 'text'}" 
                           id="${inputId}"
                           data-id-atributo="${campo.id || ''}"
                           data-nome-atributo="${campo.nomeAtributo}"
                           class="form-control form-control-sm dynamic-attribute" 
                           placeholder="${campo.placeholder || ''}"
                           maxlength="${campo.tamanho || ''}"
                           ${campo.obrigatorio ? 'required' : ''}
                           value="${campo.valorPadrao || ''}">
                `;
                containerAtributos.appendChild(divCol);
            });

            containerLista.innerHTML = campos.map(c => 
                `<div>
                    <span><i class="fa-solid fa-circle-check text-success me-2"></i>${c.nomeAtributo}</span>
                    <span class="valor-atributo-lateral" id="valor-lateral-${c.id}"></span>
                </div>`
            ).join("");
            
            atualizarDescricao();
        } catch (error) {
            console.error("Erro na requisição de campos:", error);
        }
    }

    document.getElementById('div-campos-dinamicos').addEventListener('input', function(e) {
        if (e.target.classList.contains('dynamic-attribute')) {
            const id = e.target.getAttribute('data-id-atributo');
            const spanLateral = document.getElementById(`valor-lateral-${id}`);
            if (spanLateral) spanLateral.innerText = e.target.value;
        }
    });

    function gerarSKU() {
        if (produtoIdEmEdicao) return; 

        const nomeTipo = selectTipo && selectTipo.selectedIndex > 0 ? selectTipo.options[selectTipo.selectedIndex].text : "PROD";
        const siglaTipo = nomeTipo.substring(0, 4).toUpperCase();

        let nomeMarca = "GENERICA";
        if (selectMarca && selectMarca.selectedIndex > 0) {
            let textoMarca = selectMarca.options[selectMarca.selectedIndex].text;
            nomeMarca = textoMarca.split(' ')[0].toUpperCase();
        }

        const inputModelo = document.getElementById('input-modelo');
        let textoModelo = inputModelo ? inputModelo.value.trim().toUpperCase().replace(/\s+/g, '') : "";

        let skuBase = `${siglaTipo}-${nomeMarca}-${textoModelo}`;
        if (skuInput) skuInput.value = skuBase;
    }

    function atualizarDescricao() {
        const inputsDinamicos = containerAtributos ? containerAtributos.querySelectorAll('.dynamic-attribute') : [];
        const descDetalhada = document.getElementById('desc-detalhada');
        let novasPartes = [];

        const nomeMarca = (selectMarca && selectMarca.selectedIndex > 0) ? selectMarca.options[selectMarca.selectedIndex].text.trim() : "";
        if (nomeMarca !== "") novasPartes.push(`Marca: ${nomeMarca}`);

        const modeloFieldDynamic = document.getElementById('input-modelo');
        if (modeloFieldDynamic && modeloFieldDynamic.value.trim() !== "") novasPartes.push(`Modelo: ${modeloFieldDynamic.value.trim()}`);

        inputsDinamicos.forEach(input => {
            const nomeAttr = input.getAttribute('data-nome-atributo');
            const valor = input.value.trim();
            if (valor !== "" && nomeAttr !== "Modelo") {
                novasPartes.push(`${nomeAttr}: ${valor}`);
            }
        });

        if (descInput) {
            descInput.value = `${nomeMarca} ${modeloFieldDynamic?.value || ""}`.trim();
        }
        if (descDetalhada) {
            descDetalhada.value = novasPartes.join(", ");
        }
    }   

    async function carregarProdutoParaEdicao(id) {
        try {
            const response = await fetch(`/nexacore/api/produtos/buscar?id=${id}`);
            if (!response.ok) throw new Error("Não foi possível carregar os dados do produto.");
            
            const produto = await response.json();

            if (selectTipo) {
                selectTipo.value = produto.tipoId;
                await carregarCampos(produto.tipoId);
            }

            if (selectMarca) {
                selectMarca.value = produto.marcaId;
            }

            if (skuInput) skuInput.value = produto.sku;
            if (descInput) descInput.value = produto.descricaoResumida || "";

            const descDetalhada = document.getElementById('desc-detalhada');
            if (descDetalhada) descDetalhada.value = produto.descricaoDetalhada || "";

            const selectAtivo = document.getElementById('ativo');
            if (selectAtivo) selectAtivo.value = produto.ativo.toString();

            if (txtObservacoes) {
                txtObservacoes.value = produto.observacoes || "";
                if (charCount) charCount.textContent = txtObservacoes.value.length;
            }

            verificarEstadoFormulario();

            setTimeout(() => {
                const inputModeloDinamico = document.getElementById('input-modelo');
                if (inputModeloDinamico) inputModeloDinamico.value = produto.modelo || "";

                if (produto.atributos && Array.isArray(produto.atributos)) {
                    produto.atributos.forEach(attr => {
                        let inputAttr = document.querySelector(`[data-id-atributo="${attr.idAtributo}"]`);
                        if (inputAttr) {
                            inputAttr.value = attr.valor || "";
                            const spanLateral = document.getElementById(`valor-lateral-${attr.idAtributo}`);
                            if (spanLateral) spanLateral.innerText = attr.valor || "";
                        }
                    });
                }
                atualizarDescricao();
            }, 300); 

            listaImagens = [];
            if (galleryContainer) {
                galleryContainer.querySelectorAll('.gallery-item').forEach(el => el.remove());
            }

			// Use produto.caminhosImagens em vez de produto.imagens
			if (produto.caminhosImagens && Array.isArray(produto.caminhosImagens)) {
			    produto.caminhosImagens.forEach(caminhoImg => {
			        listaImagens.push(caminhoImg);
			        const div = document.createElement('div');
			        div.className = 'gallery-item';
			        div.style.position = 'relative';
			        
			        // Aponta para o novo servlet de imagens criado
			        const urlImagem = `/nexacore/api/imagens/${caminhoImg}`;

			        div.innerHTML = `
			            <img src="${urlImagem}" draggable="false" style="width:100px; height:100px; object-fit:cover; pointer-events:none;">
			            <button type="button" class="btn btn-sm btn-danger btn-remove-thumb" style="position:absolute; top:0; right:0;">×</button>
			        `;
			        div.querySelector('.btn-remove-thumb').onclick = function() {
			            const index = listaImagens.indexOf(caminhoImg);
			            if (index > -1) listaImagens.splice(index, 1);
			            div.remove();
			            atualizarDestaque();
			        };
			        if (galleryContainer) galleryContainer.appendChild(div);
			    });
			    atualizarDestaque();
			}
        } catch (error) {
            console.error("Erro ao carregar produto para edição:", error);
            ModalService.error("Erro", "Falha ao recuperar informações do produto.");
        }
    }

    function limparFormulario() {
        if (produtoIdEmEdicao) {
            window.location.href = 'consulta-produto.jsp';
            return;
        }

        if (selectTipo) selectTipo.value = "";
        if (selectMarca) selectMarca.value = ""; 
        const selectAtivo = document.getElementById("ativo");
        if (selectAtivo) selectAtivo.value = "true"; 

        divCamposDinamicos.style.display = "none";
        containerAtributos.innerHTML = "";
        containerLista.innerHTML = "";
        
        listaImagens = []; 
        if (galleryContainer) galleryContainer.querySelectorAll('.gallery-item').forEach(el => el.remove());
        if (imgDestaque) imgDestaque.src = '';

        if (skuInput) skuInput.value = "";
        if (descInput) descInput.value = "";
        const descDetalhada = document.getElementById('desc-detalhada');
        if (descDetalhada) descDetalhada.value = "";
        
        if (txtObservacoes) {
            txtObservacoes.value = "";
            charCount.textContent = "0";
        }
    }

    async function salvarProduto() {
        const tipoId = selectTipo.value;
        if (!tipoId) {
            await ModalService.error("Atenção", "Por favor, selecione um Tipo de Produto!");
            return;
        }

        const marcaId = selectMarca ? selectMarca.value : "";
        const inputModeloDinamico = document.getElementById('input-modelo');

        if (!marcaId || !inputModeloDinamico || !inputModeloDinamico.value.trim()) {
            await ModalService.error("Campo Obrigatório", "Marca e Modelo são obrigatórios.");
            return;
        }

        const selectAtivo = document.getElementById('ativo');
        const valorAtivo = (selectAtivo.value === 'true');
    
        gerarSKU();
        const atributos = [];
        containerAtributos.querySelectorAll('.dynamic-attribute').forEach(input => {
            atributos.push({
                idAtributo: input.getAttribute('data-id-atributo'),
                nomeAtributo: input.getAttribute('data-nome-atributo'),
                valor: input.value.trim()
            });
        });

        const payload = {
            tipoId: parseInt(tipoId),
            sku: skuInput.value,
            marcaId: parseInt(marcaId),
            modelo: inputModeloDinamico.value.trim(),
            descricaoResumida: descInput.value.trim(),
            descricaoDetalhada: document.getElementById('desc-detalhada')?.value.trim() || "",
            ativo: valorAtivo,
            atributos: atributos,
            imagens: listaImagens,
            observacoes: txtObservacoes ? txtObservacoes.value.trim() : ""
        };

        try {
            const response = await fetch('/nexacore/api/produtos/salvar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const result = await response.json();

            if (response.ok) {
                await ModalService.success("Sucesso", result.mensagem);
                limparFormulario();
            } else {
                await ModalService.error("Erro", result.erro);
            }
        } catch (error) {
            console.error("Erro na requisição:", error);
            ModalService.error("Erro Técnico", "Não foi possível conectar ao servidor.");
        }
    }

    async function atualizarProduto(id) {
        const tipoId = selectTipo.value;
        const marcaId = selectMarca ? selectMarca.value : "";
        const inputModeloDinamico = document.getElementById('input-modelo');
    
        if (!marcaId || !inputModeloDinamico || !inputModeloDinamico?.value.trim()) {
            await ModalService.error("Campo Obrigatório", "Marca e Modelo são obrigatórios.");
            return;
        }
        
        const selectAtivo = document.getElementById('ativo');
        const valorAtivo = (selectAtivo.value === 'true');
    
        const atributos = [];
        containerAtributos.querySelectorAll('.dynamic-attribute').forEach(input => {
            atributos.push({
                idAtributo: input.getAttribute('data-id-atributo'),
                nomeAtributo: input.getAttribute('data-nome-atributo'),
                valor: input.value.trim()
            });
        });
    
        const payload = {
            id: parseInt(id),
            tipoId: parseInt(tipoId),
            sku: skuInput.value,
            marcaId: parseInt(marcaId),
            modelo: inputModeloDinamico.value.trim(),
            descricaoResumida: descInput.value.trim(),
            descricaoDetalhada: document.getElementById('desc-detalhada')?.value.trim() || "",
            ativo: valorAtivo,
            atributos: atributos,
            imagens: listaImagens,
            observacoes: txtObservacoes ? txtObservacoes.value.trim() : ""
        };
    
        try {
            const response = await fetch('/nexacore/api/produtos/atualizar', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const result = await response.json();

            if (response.ok) {
                await ModalService.success("Sucesso", "Produto atualizado com sucesso!");
                window.location.href = 'consulta-produto.jsp';
            } else {
                ModalService.error("Erro", result.erro || "Erro ao atualizar produto.");
            }
        } catch (error) {
            console.error("Erro na requisição de atualização:", error);
            ModalService.error("Erro Técnico", "Não foi possível conectar ao servidor.");
        }
    }
});
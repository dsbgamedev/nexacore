
document.addEventListener("DOMContentLoaded", function () {
    console.log("NexaCore: Inicializando script de cadastro de usuário...");

	// 1. Tenta carregar as unidades dinamicamente caso venham via JSON do Servlet
	    carregarUnidadesDinamicas();

	    // 1.1 Preenche os dados caso seja edição
	    preencherDadosEdicao();

	    // 2. Inicializa contadores e eventos
	    atualizarContadorUnidades();

    const checkboxes = document.querySelectorAll('.unidade-checkbox');
    checkboxes.forEach(cb => {
        cb.addEventListener('change', atualizarContadorUnidades);
    });

	// ==========================================
    // 3. INTERCEPTAÇÃO DO SUBMIT DO FORMULÁRIO
    // ==========================================
    const form = document.querySelector("form"); // Ajuste o seletor se necessário (ex: #formCadastroUsuario)
    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault(); // Impede o envio tradicional (recarregar a página)

			// Captura os dados básicos dos inputs do formulário
            const idInput = form.querySelector("input[name='id']") || document.getElementById("usuarioId");
            const id = idInput ? idInput.value : "";
            
            // Se houver um ID válido (diferente de vazio e de 0), a ação DEVE ser atualizar
            const action = (id && id !== '' && id !== '0') ? 'editar' : 'cadastrar';

            const username = form.querySelector("input[name='usuario']") ? form.querySelector("input[name='usuario']").value : "";
            const nomeCompleto = form.querySelector("input[name='nome']") ? form.querySelector("input[name='nome']").value : "";
            const email = form.querySelector("input[name='email']") ? form.querySelector("input[name='email']").value : "";
            const senha = form.querySelector("input[name='senha']") ? form.querySelector("input[name='senha']").value : "";
            
            const selectPerfil = form.querySelector("select[name='perfil']");
            const perfil = selectPerfil ? selectPerfil.value : "";
            
            const selectFilial = form.querySelector("select[name='filial']");
            const unidadePadrao = selectFilial ? selectFilial.value : "";
            
            const ativoCheckbox = form.querySelector("input[name='ativo']");
            const ativo = ativoCheckbox ? ativoCheckbox.checked : false;
			// Captura a matriz granular de módulos da tela
            const modulosPermitidos = [];
            const linhasModulos = form.querySelectorAll(".modulo-row");
            
            linhasModulos.forEach(linha => {
                const moduloIdInput = linha.querySelector(".modulo-id");
                if (moduloIdInput) {
                    modulosPermitidos.push({
                        id: parseInt(moduloIdInput.value),
                        consultar: linha.querySelector(".check-consultar").checked,
                        inserir: linha.querySelector(".check-inserir").checked,
                        editar: linha.querySelector(".check-editar").checked,
                        excluir: linha.querySelector(".check-excluir").checked,
                        cancelar: linha.querySelector(".check-cancelar").checked
                    });
                }
            });
            // Captura as Unidades Permitidas
            const unidadesPermitidas = [];
            form.querySelectorAll("input[name='unidadesPermitidas']:checked").forEach(cb => {
                unidadesPermitidas.push(cb.value);
            });

            // Monta o objeto JSON exatamente como o Java espera
            const dadosEnvio = {
                action: action,
                id: id ? parseInt(id) : 0,
                username: username,
				nomeCompleto: nomeCompleto, // <-- ADICIONADO AQUI
                email: email,
                senha: senha,
                perfil: perfil,
                ativo: ativo,
                unidadePadrao: unidadePadrao,
                modulosPermitidos: modulosPermitidos,
                unidadesPermitidas: unidadesPermitidas
            };

            //console.log("Enviando dados via JSON para o Servlet:", dadosEnvio);

            // Realiza o envio via Fetch para o Servlet
            fetch(form.action, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=UTF-8"
                },
                body: JSON.stringify(dadosEnvio)
            })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(async resultado => {
                if (resultado.body.success) {
                    // Dispara o Modal de Sucesso customizado e aguarda o usuário clicar em OK
                    await ModalService.success("Sucesso", resultado.body.message);
                    
                    // Redireciona para a tela correta após o fechamento do modal
                    // (Se o seu GerenciarUsuariosServlet chamar o jsp gerenciar-usuarios.jsp, pode manter o servlet ou apontar para o .jsp)
                    window.location.href = contextPath + "/GerenciarUsuariosServlet"; 
                } else {
                    // Dispara o Modal de Erro/Aviso customizado
                    await ModalService.error("Atenção", resultado.body.message);
                }
            })
            .catch(async erro => {
                console.error("Erro na requisição AJAX:", erro);
                await ModalService.error("Erro no Servidor", "Ocorreu um erro inesperado ao comunicar com o servidor.");
            });
        });
    }
 });
 
 function carregarUnidadesDinamicas() {
     const container = document.getElementById('listaUnidades');
     const selectFilial = document.getElementById('unidadePadrao');

     if (typeof unidadesGlobais !== 'undefined' && unidadesGlobais.length > 0) {
         
         // 1. Só recria via JS se o container estiver vazio (evita apagar o HTML estático do JSP)
         if (container && container.querySelectorAll('.unidade-item').length === 0) {
             container.innerHTML = ''; 
             unidadesGlobais.forEach(unidade => {
                 const idFilial = unidade[0];
                 const origemCodigo = unidade[1];
                 const sufixo = unidade[2];

                 const isMatriz = (String(origemCodigo) === '161');
                 const badgeClass = isMatriz ? 'bg-success text-white' : 'bg-light text-dark';
                 const tipoTexto = isMatriz ? 'Matriz' : 'Filial';

                 const htmlItem = `
                     <div class="col-md-4 unidade-item">
                         <label class="unidade-card" style="display: flex; align-items: center; gap: 10px; cursor: pointer; border: 1px solid #dee2e6; padding: 10px; border-radius: 6px;">
                             <input type="checkbox" name="unidadesPermitidas" value="${idFilial}" class="form-check-input unidade-checkbox" style="margin-top: 0;">
                             <div class="unidade-info">
                                 <div class="unidade-header">
                                     <span class="unidade-codigo fw-bold text-primary">${origemCodigo} ${sufixo}</span>
                                     <span class="badge ${badgeClass}">${tipoTexto}</span>
                                 </div>
                                 <div class="unidade-nome text-secondary">Unidade Operacional</div>
                             </div>
                         </label>
                     </div>
                 `;
                 container.innerHTML += htmlItem;
             });
         }

         // Garante os listeners nos checkboxes (seja do JSP ou gerados via JS)
         document.querySelectorAll('.unidade-checkbox').forEach(cb => {
             // Remove listener antigo para evitar duplicação e adiciona o novo
             cb.removeEventListener('change', atualizarContadorUnidades);
             cb.addEventListener('change', atualizarContadorUnidades);
         });

         // 2. Preenche o Select de Filial Principal dinamicamente
         if (selectFilial) {
             // Preserva a opção padrão e limpa as demais para recriar
             selectFilial.innerHTML = '<option value="">Selecione a filial...</option>';
             unidadesGlobais.forEach(unidade => {
                 const idFilial = unidade[0];
                 const origemCodigo = unidade[1];
                 const sufixo = unidade[2];

                 const isMatriz = (String(origemCodigo) === '161');
                 const tipoTexto = isMatriz ? 'Matriz' : 'Filial';

                 const option = document.createElement('option');
                 option.value = String(idFilial);
                 option.textContent = `${origemCodigo} ${sufixo} (${tipoTexto})`;
                 selectFilial.appendChild(option);
             });

             // Se estiver no modo de edição, define a unidade ativa/padrão convertendo para string
             if (typeof usuarioEdicao !== 'undefined' && usuarioEdicao !== null && usuarioEdicao.unidadeAtivaId) {
                 selectFilial.value = String(usuarioEdicao.unidadeAtivaId);
             }
         }

         //console.log("Unidades e Filial Principal processadas com sucesso.");
     }
 }

// Adicione esta função no seu cadastro-usuario.js
function preencherDadosEdicao() {
    if (typeof usuarioEdicao !== 'undefined' && usuarioEdicao !== null) {
        //console.log("Modo de Edição detectado. Preenchendo dados:", usuarioEdicao);

        // 1. Preenche dados textuais básicos
        const usernameInput = document.getElementById("username");
        if (usernameInput) usernameInput.value = usuarioEdicao.username || '';

        const nomeCompletoInput = document.getElementById("nomeCompleto");
        if (nomeCompletoInput) nomeCompletoInput.value = usuarioEdicao.nomeCompleto || '';

        const emailInput = document.getElementById("email");
        if (emailInput) emailInput.value = usuarioEdicao.email || '';

        const perfilSelect = document.getElementById("perfil");
        if (perfilSelect && usuarioEdicao.perfil) {
            perfilSelect.value = usuarioEdicao.perfil.toUpperCase();
        }

        const ativoCheckbox = document.getElementById("ativo");
        if (ativoCheckbox) {
            ativoCheckbox.checked = usuarioEdicao.ativo;
        }

        // 2. Preenche a Filial Principal (Unidade Ativa/Padrão)
        const selectFilial = document.getElementById("unidadePadrao");
        if (selectFilial && usuarioEdicao.unidadeAtivaId) {
            selectFilial.value = String(usuarioEdicao.unidadeAtivaId);
        }

        // 3. Marca os Checkboxes de Unidades Permitidas (Forçando conversão rigorosa para String)
        if (typeof unidadesPermitidasUsuario !== 'undefined' && Array.isArray(unidadesPermitidasUsuario)) {
            setTimeout(() => {
                unidadesPermitidasUsuario.forEach(idUnidade => {
                    const checkboxes = document.querySelectorAll('.unidade-checkbox');
                    checkboxes.forEach(cb => {
                        if (String(cb.value).trim() === String(idUnidade).trim()) {
                            cb.checked = true;
                        }
                    });
                });
                atualizarContadorUnidades();
            }, 200); // Timeout ligeiramente maior para garantir estabilidade do DOM
        }

		// 4. Preenche a Matriz Granular de Módulos (Baseado no mapa de permissões do banco)
        if (usuarioEdicao.permissoesModulos) {
            setTimeout(() => {
                const linhas = document.querySelectorAll(".modulo-row");
                
                linhas.forEach(linha => {
                    // Pega o identificador da linha (geralmente o nome do módulo ou o valor do input hidden/texto)
                    const inputId = linha.querySelector(".modulo-id") || linha.querySelector("input[type='hidden']");
                    const nomeModuloLinha = inputId ? inputId.value.trim().toLowerCase() : '';
                    
                    // Tenta achar a permissão correspondente no objeto map do Java
                    let permissaoObj = null;
                    for (let chave in usuarioEdicao.permissoesModulos) {
                        if (chave.toLowerCase() === nomeModuloLinha || 
                            linha.textContent.toLowerCase().includes(chave.toLowerCase())) {
                            permissaoObj = usuarioEdicao.permissoesModulos[chave];
                            break;
                        }
                    }

                    if (permissaoObj) {
                        const chkConsultar = linha.querySelector("input[name*='consultar'], .check-consultar");
                        const chkInserir = linha.querySelector("input[name*='inserir'], .check-inserir");
                        const chkEditar = linha.querySelector("input[name*='editar'], .check-editar");
                        const chkExcluir = linha.querySelector("input[name*='excluir'], .check-excluir");
                        const chkCancelar = linha.querySelector("input[name*='cancelar'], .check-cancelar");

                        if (chkConsultar) chkConsultar.checked = !!(permissaoObj.podeConsultar || permissaoObj.consultar);
                        if (chkInserir) chkInserir.checked = !!(permissaoObj.podeInserir || permissaoObj.inserir);
                        if (chkEditar) chkEditar.checked = !!(permissaoObj.podeEditar || permissaoObj.editar);
                        if (chkExcluir) chkExcluir.checked = !!(permissaoObj.podeExcluir || permissaoObj.excluir);
                        if (chkCancelar) chkCancelar.checked = !!(permissaoObj.podeCancelar || permissaoObj.cancelar);
                    }
                });
            }, 300);
                
        }
    }
}

function atualizarContadorUnidades() {
    const checkboxes = document.querySelectorAll('.unidade-checkbox:checked');
    const contador = document.getElementById('contadorUnidades');
    if (contador) {
        contador.textContent = checkboxes.length + ' selecionada(s)';
    }
}

function selecionarTodasUnidades() {
    document.querySelectorAll('.unidade-item').forEach(item => {
        if (item.style.display !== 'none') {
            const cb = item.querySelector('.unidade-checkbox');
            if (cb) cb.checked = true;
        }
    });
    atualizarContadorUnidades();
}

function limparUnidades() {
    document.querySelectorAll('.unidade-checkbox').forEach(cb => {
        cb.checked = false;
    });
    atualizarContadorUnidades();
}

function filtrarUnidades() {
    const inputPesquisa = document.getElementById('pesquisaUnidade');
    if (!inputPesquisa) return;

    const termo = inputPesquisa.value.toLowerCase();
    document.querySelectorAll('.unidade-item').forEach(item => {
        const texto = item.textContent.toLowerCase();
        if (texto.includes(termo)) {
            item.style.display = '';
        } else {
            item.style.display = 'none';
        }
    });
}

/**
 * Marca ou desmarca absolutamente todas as caixas de permissão da tabela.
 */
function toggleTodosModulos(masterCheckbox) {
    const checkboxes = document.querySelectorAll('.modulo-row input[type="checkbox"]');
    checkboxes.forEach(cb => {
        cb.checked = masterCheckbox.checked;
    });
}

/**
 * Marca ou desmarca todos os checkboxes de uma coluna específica (ex: todas as caixas de 'Inserir').
 */
function toggleColunaPermissao(classeColuna, masterCheckbox) {
    const checkboxes = document.querySelectorAll(`.${classeColuna}`);
    checkboxes.forEach(cb => {
        cb.checked = masterCheckbox.checked;
    });
}
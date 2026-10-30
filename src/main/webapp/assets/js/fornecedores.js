(() => {
  const ctx = window.NEXACORE_CONTEXT || '';
  const form = document.getElementById('formFornecedor');
  const alertBox = document.getElementById('formAlert');
  const enderecosContainer = document.getElementById('enderecosContainer');
  const enderecos = Array.isArray(window.FORNECEDOR_ENDERECOS) ? window.FORNECEDOR_ENDERECOS : [];
  const filiaisAtuais = (Array.isArray(window.FORNECEDOR_FILIAIS) ? window.FORNECEDOR_FILIAIS : []).map(Number);
  
  // Recupera as observações por filial vindas do JSP (caso seja edição)
  const filiaisObsAtuais = Array.isArray(window.FORNECEDOR_FILIAIS_OBS) ? window.FORNECEDOR_FILIAIS_OBS : [];

  // --- CONTROLE DE ESTADO DAS OBSERVAÇÕES POR FILIAL (ABA ÚNICA) ---
  let filialAtivaId = null;
  const mapaObservacoesFiliais = {}; // { idFilial: textoObservacao }

  // Carrega as observações iniciais no mapa caso seja edição
  if (filiaisObsAtuais.length > 0) {
      filiaisObsAtuais.forEach(item => {
          if (item.idFilial && item.observacao) {
              mapaObservacoesFiliais[item.idFilial] = item.observacao;
          }
      });
  }

  function showAlert(message, success) {
    if (!alertBox) return;
    alertBox.className = 'alert ' + (success ? 'alert-success' : 'alert-danger');
    alertBox.textContent = message;
    alertBox.classList.remove('d-none');
    alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  function esc(v) { 
    return String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); 
  }

  // --- BUSCA CEP VIA API VIACEP ---
  async function buscarCep(inputCep, cardEl) {
    const cepLimpo = inputCep.value.replace(/\D/g, '');
    if (cepLimpo.length !== 8) return;

    try {
      const response = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
      const data = await response.json();

      if (data.erro) {
        alert('CEP não encontrado.');
        return;
      }

      cardEl.querySelector('.a-logradouro').value = data.logradouro || '';
      cardEl.querySelector('.a-bairro').value = data.bairro || '';
      cardEl.querySelector('.a-cidade').value = data.localidade || '';
      cardEl.querySelector('.a-uf').value = (data.uf || '').toUpperCase();
      
      cardEl.querySelector('.a-numero')?.focus();
    } catch (e) {
      console.error('Erro ao consultar o CEP:', e);
    }
  }
  
  // --- LIMPAR FILTROS DE CONSULTA ---
    window.limparFiltrosFornecedores = () => {
      const form = document.getElementById('formFiltros');
      if (!form) return;
      
      form.querySelectorAll('input').forEach(input => input.value = '');
      form.querySelectorAll('select').forEach(select => select.selectedIndex = 0);
      
      window.location.href = ctx + '/ConsultaFornecedorServlet';
    };
	
  // --- GESTÃO DE ENDEREÇOS DINÂMICOS ---
  // --- GESTÃO DE ENDEREÇOS DINÂMICOS E BUSCA CEP CORRIGIDA ---
  // --- GESTÃO DE ENDEREÇOS DINÂMICOS E NAVEGAÇÃO POR ENTER ---
    function addressCard(e = {}, idx = Date.now()) {
      const el = document.createElement('div'); 
      el.className = 'address-card'; 
      el.dataset.address = '1';
      
      el.innerHTML = `
        <div class="address-card-head">
          <div>
            <span class="address-index">ENDEREÇO</span>
            <strong class="address-label">${esc(e.tipoEndereco || 'COMERCIAL')}</strong>
          </div>
          <button type="button" class="btn-icon danger remove-address" title="Remover endereço">
            <i class="fa-regular fa-trash-can"></i>
          </button>
        </div>
        <div class="form-grid">
          <div class="field">
            <label>Tipo</label>
            <select class="form-select a-tipo">
              <option value="COMERCIAL">Comercial</option>
              <option value="COBRANCA">Cobrança</option>
              <option value="ENTREGA">Entrega</option>
              <option value="OPERACIONAL">Operacional</option>
              <option value="OUTRO">Outro</option>
            </select>
          </div>
          <div class="field">
            <label>CEP</label>
            <input class="form-control a-cep" maxlength="9" value="${esc(e.cep)}" placeholder="00000-000">
          </div>
          <div class="field span-2">
            <label>Logradouro</label>
            <input class="form-control a-logradouro" value="${esc(e.logradouro)}" maxlength="200">
          </div>
          <div class="field">
            <label>Número</label>
            <input class="form-control a-numero" value="${esc(e.numero)}" maxlength="20">
          </div>
          <div class="field">
            <label>Complemento</label>
            <input class="form-control a-complemento" value="${esc(e.complemento)}" maxlength="100">
          </div>
          <div class="field">
            <label>Bairro</label>
            <input class="form-control a-bairro" value="${esc(e.bairro)}" maxlength="100">
          </div>
          <div class="field">
            <label>Cidade</label>
            <input class="form-control a-cidade" value="${esc(e.cidade)}" maxlength="100">
          </div>
          <div class="field">
            <label>UF</label>
            <input class="form-control a-uf" value="${esc(e.uf)}" maxlength="2">
          </div>
          <div class="field">
            <label>País</label>
            <input class="form-control a-pais" value="${esc(e.pais || 'Brasil')}" maxlength="60">
          </div>
          <div class="field span-2">
            <label>Referência</label>
            <input class="form-control a-referencia" value="${esc(e.referencia)}" maxlength="200">
          </div>
        </div>
        <label class="main-address">
          <input class="form-check-input a-principal" type="radio" name="enderecoPrincipal" ${e.principal ? 'checked' : ''}> 
          Definir como endereço principal
        </label>
      `;
      
      el.querySelector('.a-tipo').value = e.tipoEndereco || 'COMERCIAL';
      el.querySelector('.remove-address').addEventListener('click', () => { el.remove(); refreshAddresses(); });
      el.querySelector('.a-principal').addEventListener('change', () => { 
        document.querySelectorAll('.a-principal').forEach(r => { if (r !== el.querySelector('.a-principal')) r.checked = false; }); 
      });
      el.querySelector('.a-tipo').addEventListener('change', () => { 
        el.querySelector('.address-label').textContent = el.querySelector('.a-tipo').selectedOptions[0].textContent.toUpperCase(); 
      });
      
      // Função interna isolada para buscar o CEP e preencher os campos deste card específico
      const cepInput = el.querySelector('.a-cep');
      const executarBuscaCep = async () => {
        const cepLimpo = cepInput.value.replace(/\D/g, '');
        if (cepLimpo.length !== 8) return;

        try {
          const response = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
          const data = await response.json();

          if (data.erro) {
            alert('CEP não encontrado.');
            return;
          }

          el.querySelector('.a-logradouro').value = data.logradouro || '';
          el.querySelector('.a-bairro').value = data.bairro || '';
          el.querySelector('.a-cidade').value = data.localidade || '';
          el.querySelector('.a-uf').value = (data.uf || '').toUpperCase();
          
          el.querySelector('.a-numero')?.focus();
        } catch (err) {
          console.error('Erro ao consultar o CEP:', err);
        }
      };

      cepInput.addEventListener('input', ev => { 
        let n = ev.target.value.replace(/\D/g,'').slice(0,8); 
        ev.target.value = n.length > 5 ? n.slice(0,5) + '-' + n.slice(5) : n;
        if (n.length === 8) {
          executarBuscaCep();
        }
      });
      cepInput.addEventListener('blur', executarBuscaCep);

      // 👉 NAVEGAÇÃO POR ENTER DENTRO DO CARD DE ENDEREÇO
      el.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
          e.preventDefault();
          const focusableElements = Array.from(el.querySelectorAll('input, select, button')).filter(item => !item.disabled && item.offsetParent !== null);
          const index = focusableElements.indexOf(e.target);
          if (index > -1 && focusableElements[index + 1]) {
            focusableElements[index + 1].focus();
          }
        }
      });

      enderecosContainer.appendChild(el); 
      refreshAddresses();
    }
  // --- GESTÃO DA ABA ÚNICA DE OBSERVAÇÕES POR FILIAL (DEFINITIVO) ---
    function atualizarAbasFiliais() {
      const containerAbas = document.getElementById('listaAbasFiliais');
      const painelUnico = document.getElementById('painelObsUnico');
      const tituloAtiva = document.getElementById('tituloFilialAtiva');
      const textoObs = document.getElementById('textoObsAtiva');
      
      if (!containerAbas) return;

      const checkboxesSelecionados = document.querySelectorAll('.filial-check:checked');

      if (checkboxesSelecionados.length === 0) {
          containerAbas.innerHTML = '<span class="text-muted small">Nenhuma filial selecionada.</span>';
          if (painelUnico) painelUnico.classList.add('d-none');
          filialAtivaId = null;
          if (textoObs) textoObs.value = '';
          return;
      }

      let idsMarcados = Array.from(checkboxesSelecionados).map(cb => cb.value);

      // Se a filial ativa atual não está mais marcada (ou é nula), pega a primeira disponível
      if (!idsMarcados.includes(String(filialAtivaId))) {
          filialAtivaId = idsMarcados[0];
      }

      // Renderiza as abas apenas uma vez de forma limpa
      let htmlAbas = '';
      checkboxesSelecionados.forEach(cb => {
          const id = cb.value;
          const labelParent = cb.closest('label');
          const sufixo = labelParent.querySelector('.badge').innerText;
          const isAtiva = String(id) === String(filialAtivaId) ? 'btn-primary' : 'btn-outline-secondary';

          htmlAbas += `<button type="button" class="btn btn-sm ${isAtiva} filial-aba" data-filial-id="${id}">${sufixo}</button>`;
      });

      containerAbas.innerHTML = htmlAbas;

      // Exibe o painel único de texto
      if (painelUnico) painelUnico.classList.remove('d-none');

      // Atualiza o título e o conteúdo do textarea com base estritamente na filial ativa
      if (filialAtivaId) {
          const cbOriginal = document.querySelector(`.filial-check[value="${filialAtivaId}"]`);
          if (cbOriginal) {
              const labelParent = cbOriginal.closest('label');
              const nomeEmpresa = labelParent.querySelector('strong').innerText;
              const sufixo = labelParent.querySelector('.badge').innerText;
              
              if (tituloAtiva) {
                  tituloAtiva.innerHTML = `<i class="fa-solid fa-building me-1"></i> ${sufixo} - ${nomeEmpresa}`;
              }
          }
          // Carrega rigorosamente o valor salvo para esta filial ou deixa em branco se não existir
          if (textoObs) {
              textoObs.value = mapaObservacoesFiliais[filialAtivaId] || '';
          }
      }

      // Associa o evento de clique fixo em cada aba gerada
      containerAbas.querySelectorAll('.filial-aba').forEach(btn => {
          btn.onclick = () => {
              // 1. Salva o texto atual do textarea na filial que estava ativa ANTES de mudar
              if (filialAtivaId && textoObs) {
                  mapaObservacoesFiliais[filialAtivaId] = textoObs.value;
              }
              // 2. Altera para o novo ID da aba clicada
              filialAtivaId = btn.dataset.filialId;
              
              // 3. Atualiza a interface
              atualizarAbasFiliais();
          };
      });
    }

    // Garante que qualquer alteração direta no textarea também atualize imediatamente a memória da filial ativa
    document.getElementById('textoObsAtiva')?.addEventListener('input', e => {
        if (filialAtivaId) {
            mapaObservacoesFiliais[filialAtivaId] = e.target.value;
        }
    });

  // --- MÁSCARAS DE INPUT ---
  function maskDoc() {
    const input = document.getElementById('documento'); 
    const tipoPessoaEl = document.getElementById('tipoPessoa');
    if (!input) return;
    const pf = tipoPessoaEl ? tipoPessoaEl.value === 'PF' : false;
    let n = input.value.replace(/\D/g,'').slice(0, pf ? 11 : 14);
    if (pf) { 
      n = n.replace(/(\d{3})(\d)/,'$1.$2').replace(/(\d{3})(\d)/,'$1.$2').replace(/(\d{3})(\d{1,2})$/,'$1-$2'); 
    } else { 
      n = n.replace(/^(\d{2})(\d)/,'$1.$2').replace(/^(\d{2})\.(\d{3})(\d)/,'$1.$2.$3').replace(/\.(\d{3})(\d)/,'.' + '$1/$2').replace(/(\d{4})(\d)/,'$1-$2'); 
    }
    input.value = n;
  }

  // --- MÁSCARA DE TELEFONE ATUALIZADA (SUPORTE A FIXO E 0800) ---
    function maskTelefone(e) {
      let v = e.target.value.replace(/\D/g, '');
      
      // Se começar com 0800 (ou 0300, etc.), formata como 0800-000-0000 (11 dígitos)
      if (v.startsWith('0')) {
        v = v.slice(0, 11);
        if (v.length > 7) {
          v = v.replace(/^(\d{4})(\d{3})(\d{0,4})/, '$1-$2-$3');
        } else if (v.length > 4) {
          v = v.replace(/^(\d{4})(\d{0,3})/, '$1-$2');
        }
      } else {
        // Telefone fixo tradicional (10 dígitos: (00) 0000-0000)
        v = v.slice(0, 10);
        if (v.length > 6) {
          v = v.replace(/^(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
        } else if (v.length > 2) {
          v = v.replace(/^(\d{2})(\d{0,4})/, '($1) $2');
        } else if (v.length > 0) {
          v = v.replace(/^(\d{0,2})/, '($1');
        }
      }
      
      e.target.value = v;
    }

  function maskCelular(e) {
    let v = e.target.value.replace(/\D/g, '').slice(0, 11);
    if (v.length > 7) {
      v = v.replace(/^(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    } else if (v.length > 2) {
      v = v.replace(/^(\d{2})(\d{0,4})/, '($1) $2');
    } else if (v.length > 0) {
      v = v.replace(/^(\d{0,2})/, '($1');
    }
    e.target.value = v;
  }

  // --- EVENT LISTENERS GLOBAIS DO FORMULÁRIO ---
  document.getElementById('addEndereco')?.addEventListener('click', () => {
    addressCard({ tipoEndereco: 'COMERCIAL', pais: 'Brasil', principal: !enderecosContainer.querySelector('.address-card') });
  });
  
  document.getElementById('marcarTodas')?.addEventListener('click', () => {
      document.querySelectorAll('.filial-check').forEach(c => c.checked = true);
      atualizarAbasFiliais();
  });
  
  document.getElementById('limparFiliais')?.addEventListener('click', () => {
      document.querySelectorAll('.filial-check').forEach(c => c.checked = false);
      atualizarAbasFiliais();
  });

  document.querySelectorAll('.filial-check').forEach(checkbox => {
      checkbox.addEventListener('change', atualizarAbasFiliais);
  });
  
  document.getElementById('tipoPessoa')?.addEventListener('change', () => { 
    const d = document.getElementById('documento'); 
    if(d) { d.value = ''; maskDoc(); } 
    d?.focus(); 
  });
  
  document.getElementById('documento')?.addEventListener('input', maskDoc);
  document.getElementById('telefone')?.addEventListener('input', maskTelefone);
  document.getElementById('celular')?.addEventListener('input', maskCelular);

  document.getElementById('inscricaoEstadual')?.addEventListener('input', e => {
    e.target.value = e.target.value.replace(/\D/g, '').slice(0, 14);
  });
  
  document.getElementById('inscricaoMunicipal')?.addEventListener('input', e => {
    e.target.value = e.target.value.replace(/\D/g, '').slice(0, 11);
  });

  // Inicializa marcações de filiais e endereços caso seja edição
  if (form) {
    document.querySelectorAll('.filial-check').forEach(c => {
        if (filiaisAtuais.includes(Number(c.value))) {
            c.checked = true;
        }
    });
    
    // Dispara a renderização inicial das abas de filiais
    atualizarAbasFiliais();
    enderecos.forEach(e => addressCard(e));
    
    // --- SUBMISSÃO DO FORMULÁRIO ---
    form.addEventListener('submit', async ev => {
      ev.preventDefault();
      if (!form.reportValidity()) return;

      const doc = document.getElementById('documento').value.replace(/\D/g,'');
      const pf = document.getElementById('tipoPessoa')?.value === 'PF';
      
      if (pf && doc.length !== 11) { 
        showAlert('Informe um CPF válido com exatamente 11 dígitos.', false); 
        return; 
      }
      if (!pf && doc.length !== 14) { 
        showAlert('Informe um CNPJ válido com exatamente 14 dígitos.', false); 
        return; 
      }

	  // Validação robusta para aceitar tanto a classe quanto o name dos checkboxes de filiais
	  // Salva o texto da última aba ativa antes de gerar o JSON final
        const textoObsAtiva = document.getElementById('textoObsAtiva');
        if (filialAtivaId && textoObsAtiva) {
            mapaObservacoesFiliais[filialAtivaId] = textoObsAtiva.value;
        }

        // 👉 Coleta unificada e robusta das filiais e observações
        const observacoesPorFilial = [];
        const idsFiliaisSelecionadas = [];

        document.querySelectorAll('.filial-check:checked, input[name="filiaisIds"]:checked').forEach(cb => {
            const id = cb.value;
            const idNum = parseInt(id);
            
            if (!idsFiliaisSelecionadas.includes(idNum)) {
                idsFiliaisSelecionadas.push(idNum);
            }

            const obs = mapaObservacoesFiliais[id];
            if (obs && obs.trim() !== '') {
                observacoesPorFilial.push({
                    idFilial: idNum,
                    observacao: obs.trim()
                });
            }
        });

        // Validação no front para garantir que marcou pelo menos uma filial
        if (idsFiliaisSelecionadas.length === 0) {
          showAlert('Selecione pelo menos uma filial ou matriz autorizada para este fornecedor.', false);
          return;
        }

        // Atribui o JSON de observações específicas
        document.getElementById('filiaisObservacoesJson').value = JSON.stringify(observacoesPorFilial);
        
        // Injeta dinamicamente os IDs simples que o Java valida obrigatoriamente
        form.querySelectorAll('input.hidden-filial-id-dinamico').forEach(el => el.remove());
        idsFiliaisSelecionadas.forEach(idFilial => {
            const hid = document.createElement('input');
            hid.type = 'hidden';
            hid.name = 'filiaisIds';
            hid.value = idFilial;
            hid.className = 'hidden-filial-id-dinamico';
            form.appendChild(hid);
        });

        const addrs = collectAddresses();
        if (addrs.filter(a => a.principal).length > 1) { 
          showAlert('Defina apenas um endereço principal.', false); 
          return; 
        }
        
        // Recolhe os dados dos endereços
        document.getElementById('enderecosJson').value = JSON.stringify(addrs);

        const data = new FormData(form);
        
        const btns = form.querySelectorAll('button[type="submit"]'); 
        btns.forEach(b => { 
          b.disabled = true; 
          b.dataset.oldText = b.innerHTML; 
          b.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Salvando...'; 
        });

        try {
          const r = await fetch(ctx + '/FornecedorServlet', { method: 'POST', body: data, headers: { 'X-Requested-With': 'XMLHttpRequest' } });
          const result = await r.json();
          if (!r.ok || !result.sucesso) throw new Error(result.mensagem || 'Não foi possível salvar o fornecedor.');
          showAlert(result.mensagem, true); 
          setTimeout(() => window.location.href = ctx + '/ConsultaFornecedorServlet', 700);
        } catch(err) { 
          showAlert(err.message || 'Falha de comunicação com o servidor.', false); 
        } finally { 
          btns.forEach(b => { 
            b.disabled = false; 
            if(b.dataset.oldText) b.innerHTML = b.dataset.oldText; 
          }); 
        }
    });
  }

  // Torna a função globalmente acessível para exclusão
  window.excluirFornecedor = async (id, context) => {
    if (typeof ModalService !== 'undefined' && typeof ModalService.confirm === 'function') {
      const confirmado = await ModalService.confirm(
        'Confirmar Exclusão',
        'Deseja realmente excluir este fornecedor? Os endereços e vínculos com filiais também serão removidos.',
        'warning'
      );
      
      if (confirmado) {
        await executarExclusao(id, context);
      }
    } else {
      if (!confirm('Deseja realmente excluir este fornecedor? Os endereços e vínculos com filiais também serão removidos.')) return;
      await executarExclusao(id, context);
    }
  };

  async function executarExclusao(id, context) {
      const data = new URLSearchParams({ acao: 'excluir', id: String(id) });
      try { 
        const r = await fetch(context + '/FornecedorServlet', { 
          method: 'POST', 
          headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8', 'X-Requested-With': 'XMLHttpRequest' }, 
          body: data 
        }); 

        const responseText = await r.text();
        let result;
        try {
          result = JSON.parse(responseText);
        } catch (err) {
          throw new Error('Usuário não tem permissão para realizar esta operação.');
        }

        if (!r.ok || !result.sucesso) {
          throw new Error(result.mensagem || 'Não foi possível excluir.'); 
        }
        
        if (typeof ModalService !== 'undefined' && typeof ModalService.success === 'function') {
          await ModalService.success('Sucesso', result.mensagem || 'Fornecedor excluído com sucesso.');
          location.reload();
        } else {
          location.reload();
        }
      } catch(e) { 
        if (typeof ModalService !== 'undefined' && typeof ModalService.error === 'function') {
          await ModalService.error('Atenção', e.message || 'Erro ao excluir fornecedor.');
        } else {
          alert(e.message || 'Erro ao excluir fornecedor.');
        }
      }
  }

  // --- VISUALIZAR FORNECEDOR VIA MODAL ---
  window.visualizarFornecedor = async (id, context) => {
      const modalEl = document.getElementById('viewModal');
      const bodyEl = document.getElementById('viewModalBody');
      
      if (!modalEl || !bodyEl) return;

      bodyEl.innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="text-muted mt-2 mb-0">Carregando informações...</p>
        </div>
      `;
      
      const modal = new bootstrap.Modal(modalEl);
      modal.show();

      try {
        const response = await fetch(`${context}/FornecedorServlet?acao=detalhesJson&id=${id}`, {
          method: 'GET',
          headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        const responseText = await response.text();
        let result;
        try {
          result = JSON.parse(responseText);
        } catch (err) {
          throw new Error('Sessão expirada ou usuário sem permissão.');
        }

        if (!response.ok || !result.sucesso) {
          throw new Error(result.mensagem || 'Não foi possível carregar os dados do fornecedor.');
        }

        const f = result.fornecedor;
        
        bodyEl.innerHTML = `
          <div class="row g-3">
            <div class="col-md-6">
              <label class="text-muted small d-block">Razão Social</label>
              <strong>${esc(f.razaoSocial)}</strong>
            </div>
            <div class="col-md-6">
              <label class="text-muted small d-block">Nome Fantasia</label>
              <strong>${esc(f.nomeFantasia || '-')}</strong>
            </div>
            <div class="col-md-4">
              <label class="text-muted small d-block">Tipo / Documento</label>
              <span>${esc(f.documento)} (${esc(f.tipoPessoa)})</span>
            </div>
            <div class="col-md-4">
              <label class="text-muted small d-block">Inscrição Estadual</label>
              <span>${esc(f.inscricaoEstadual || '-')}</span>
            </div>
            <div class="col-md-4">
              <label class="text-muted small d-block">Inscrição Municipal</label>
              <span>${esc(f.inscricaoMunicipal || '-')}</span>
            </div>
            <div class="col-md-4">
              <label class="text-muted small d-block">Contato Principal</label>
              <span>${esc(f.contatoPrincipal || '-')}</span>
            </div>
            <div class="col-md-4">
              <label class="text-muted small d-block">Telefone / Celular</label>
              <span>${esc(f.telefone || '-')} / ${esc(f.celular || '-')}</span>
            </div>
            <div class="col-md-4">
              <label class="text-muted small d-block">E-mail</label>
              <span>${esc(f.email || '-')}</span>
            </div>
			<div class="col-md-4">
              <label class="text-muted small d-block">Site</label>
              <span>
                ${f.site ? `<a href="${esc(f.site)}" target="_blank" rel="noopener noreferrer" class="text-decoration-none"><i class="fa-solid fa-globe me-1"></i>${esc(f.site)}</a>` : '-'}
              </span>
            </div>
            <div class="col-12">
              <hr class="my-2">
              <h6 class="fw-bold mb-2"><i class="fa-solid fa-map-location-dot me-2"></i>Endereços (${f.enderecos ? f.enderecos.length : 0})</h6>
              <div class="table-responsive" style="max-height: 150px; overflow-y: auto;">
                <table class="table table-sm table-bordered mb-0">
                  <thead>
                    <tr class="table-light">
                      <th>Tipo</th>
                      <th>Logradouro, Nº</th>
                      <th>Bairro / Cidade - UF</th>
                      <th>CEP</th>
                    </tr>
                  </thead>
				  <tbody>
                    ${f.enderecos && f.enderecos.length > 0 ? f.enderecos.map(a => {
                      const enderecoCompleto = `${esc(a.logradouro)}, ${esc(a.numero)}${a.complemento ? ' (' + esc(a.complemento) + ')' : ''}`;
                      const regiaoCompleta = `${esc(a.bairro)} / ${esc(a.cidade)} - ${esc(a.uf)}`;
                      return `
                        <tr>
                          <td><span class="badge bg-secondary">${esc(a.tipoEndereco)}</span> ${a.principal ? '<span class="badge bg-success">Principal</span>' : ''}</td>
                          <td title="${enderecoCompleto}">${enderecoCompleto}</td>
                          <td title="${regiaoCompleta}">${regiaoCompleta}</td>
                          <td title="${esc(a.cep)}">${esc(a.cep)}</td>
                        </tr>
                      `;
                    }).join('') : '<tr><td colspan="4" class="text-center text-muted">Nenhum endereço cadastrado.</td></tr>'}
                  </tbody>
                </table>
              </div>
            </div>
            
            <!-- Filiais e Observações Específicas -->
            <div class="col-12">
              <hr class="my-2">
              <label class="fw-bold text-secondary mb-2">
                  <i class="fa-solid fa-code-branch me-1 text-primary"></i> Observações Específicas por Filial
              </label>
			  <div class="list-group">
			    ${f.filiaisComObservacoes && f.filiaisComObservacoes.length > 0 ? f.filiaisComObservacoes.map(item => `
			      <div class="list-group-item bg-light mb-2 rounded border d-flex flex-column align-items-start text-start">
			        <strong><i class="fa-solid fa-building me-1"></i>${esc(item.nomeFilial)}</strong>
			        <p class="text-muted small mb-0 mt-1 text-start" style="white-space: pre-wrap; word-break: break-word;">${esc((item.observacao || 'Nenhuma observação específica cadastrada para esta unidade.').trim())}</p>
			      </div>
			    `).join('') : '<span class="text-muted small text-start d-block">Nenhuma filial com observação específica.</span>'}
			  </div>
            </div>


          <!-- Observações Gerais -->
		  <div class="col-12">
            <hr class="my-2">
            <label class="fw-bold text-secondary mb-1">
                <i class="fa-solid fa-note-sticky me-1 text-primary"></i> Observações Gerais
            </label>
            <div class="p-3 bg-light border rounded text-muted text-start" style="white-space: pre-wrap; word-break: break-word; max-height: 150px; overflow-y: auto;">
                ${esc((f.observacao || 'Nenhuma observação geral cadastrada.').split('\n').map(l => l.trim()).join('\n'))}
            </div>
          </div>
          </div>
        `;

      } catch (e) {
        bodyEl.innerHTML = `
          <div class="alert alert-danger mb-0">
            <i class="fa-solid fa-circle-exclamation me-2"></i>${esc(e.message)}
          </div>
        `;
      }
  };
})();
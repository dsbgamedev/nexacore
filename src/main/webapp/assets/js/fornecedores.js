(() => {
  const ctx = window.NEXACORE_CONTEXT || '';
  const form = document.getElementById('formFornecedor');
  const alertBox = document.getElementById('formAlert');
  const enderecosContainer = document.getElementById('enderecosContainer');
  let enderecosLista = Array.isArray(window.FORNECEDOR_ENDERECOS) ? [...window.FORNECEDOR_ENDERECOS] : [];
  const filiaisAtuais = (Array.isArray(window.FORNECEDOR_FILIAIS) ? window.FORNECEDOR_FILIAIS : []).map(Number);
  
  const filiaisObsAtuais = Array.isArray(window.FORNECEDOR_FILIAIS_OBS) ? window.FORNECEDOR_FILIAIS_OBS : [];

  let filialAtivaId = null;
  const mapaObservacoesFiliais = {}; 

  if (filiaisObsAtuais.length > 0) {
      filiaisObsAtuais.forEach(item => {
          if (item.idFilial && item.observacao) {
              mapaObservacoesFiliais[item.idFilial] = item.observacao;
          }
      });
  }

  // --- FUNÇÃO PADRÃO DE MENSAGENS COM MODAL SERVICE ---
  async function mostrarAlerta(titulo, mensagem, tipo = 'error') {
    try {
      if (typeof ModalService !== 'undefined') {
        if (tipo === 'success' && typeof ModalService.success === 'function') {
          await ModalService.success(titulo, mensagem);
        } else if (tipo === 'warning' && typeof ModalService.warning === 'function') {
          await ModalService.warning(titulo, mensagem);
        } else if (typeof ModalService.error === 'function') {
          await ModalService.error(titulo, mensagem);
        } else if (typeof ModalService.alert === 'function') {
          await ModalService.alert(titulo, mensagem, tipo);
        } else {
          alert(mensagem);
        }
      } else {
        console.warn('ModalService não definido globalmente.');
        alert(mensagem);
      }
    } catch (e) {
      console.error('Erro ao acionar ModalService:', e);
      alert(mensagem);
    }
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

  window.limparFiltrosFornecedores = () => {
    const formFiltro = document.getElementById('formFiltros');
    if (!formFiltro) return;
    formFiltro.querySelectorAll('input').forEach(input => input.value = '');
    formFiltro.querySelectorAll('select').forEach(select => select.selectedIndex = 0);
    window.location.href = ctx + '/ConsultaFornecedorServlet';
  };

  // --- GESTÃO DE ENDEREÇOS POR SISTEMA DE ABAS (COM ENTER E MODAL SERVICE) ---
  let enderecoAtivoIndex = 0;

  if (enderecosLista.length === 0) {
    enderecosLista.push({ tipoEndereco: 'COMERCIAL', pais: 'Brasil', principal: true });
  }

  function renderizarGerenciadorEnderecos() {
    if (!enderecosContainer) return;

    if (!enderecosLista.some(a => a.principal) && enderecosLista.length > 0) {
      enderecosLista[0].principal = true;
    }

    if (enderecoAtivoIndex >= enderecosLista.length) {
      enderecoAtivoIndex = enderecosLista.length - 1;
    }
    if (enderecoAtivoIndex < 0) enderecoAtivoIndex = 0;

    const e = enderecosLista[enderecoAtivoIndex] || {};

    let htmlAbas = '';
    enderecosLista.forEach((item, idx) => {
      const ativa = idx === enderecoAtivoIndex ? 'btn-primary' : 'btn-outline-secondary';
      const label = item.tipoEndereco || `ENDEREÇO ${idx + 1}`;
      htmlAbas += `
        <button type="button" class="btn btn-sm ${ativa} me-1 mb-2 aba-endereco" data-index="${idx}">
          <i class="fa-solid fa-map-pin me-1"></i>${label} ${item.principal ? '⭐' : ''}
        </button>
      `;
    });

    enderecosContainer.innerHTML = `
      <div class="card border shadow-sm p-3 mb-3" id="painelCardEndereco">
        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
          <div class="d-flex flex-wrap align-items-center" id="containerAbasEnderecos">
            ${htmlAbas}
          </div>
          <div class="d-flex gap-2">
            <button type="button" class="btn btn-outline-primary btn-sm" id="btnAddNovoEndereco">
              <i class="fa-solid fa-plus me-1"></i> Adicionar Endereço
            </button>
            ${enderecosLista.length > 1 ? `
              <button type="button" class="btn btn-outline-danger btn-sm" id="btnRemoverEnderecoAtivo">
                <i class="fa-solid fa-trash-can me-1"></i> Remover Este
              </button>` : ''}
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label>Tipo</label>
            <select class="form-select a-tipo" id="abaTipo">
              <option value="COMERCIAL">Comercial</option>
              <option value="COBRANCA">Cobrança</option>
              <option value="ENTREGA">Entrega</option>
              <option value="OPERACIONAL">Operacional</option>
              <option value="OUTRO">Outro</option>
            </select>
          </div>
          <div class="field">
            <label>CEP</label>
            <input class="form-control a-cep" id="abaCep" maxlength="9" value="${esc(e.cep)}" placeholder="00000-000">
          </div>
          <div class="field span-2">
            <label>Logradouro</label>
            <input class="form-control a-logradouro" id="abaLogradouro" value="${esc(e.logradouro)}" maxlength="200">
          </div>
          <div class="field">
            <label>Número</label>
            <input class="form-control a-numero" id="abaNumero" value="${esc(e.numero)}" maxlength="20">
          </div>
          <div class="field">
            <label>Complemento</label>
            <input class="form-control a-complemento" id="abaComplemento" value="${esc(e.complemento)}" maxlength="100">
          </div>
          <div class="field">
            <label>Bairro</label>
            <input class="form-control a-bairro" id="abaBairro" value="${esc(e.bairro)}" maxlength="100">
          </div>
          <div class="field">
            <label>Cidade</label>
            <input class="form-control a-cidade" id="abaCidade" value="${esc(e.cidade)}" maxlength="100">
          </div>
          <div class="field">
            <label>UF</label>
            <input class="form-control a-uf" id="abaUf" value="${esc(e.uf)}" maxlength="2">
          </div>
          <div class="field">
            <label>País</label>
            <input class="form-control a-pais" id="abaPais" value="${esc(e.pais || 'Brasil')}" maxlength="60">
          </div>
          <div class="field span-2">
            <label>Referência</label>
            <input class="form-control a-referencia" id="abaReferencia" value="${esc(e.referencia)}" maxlength="200">
          </div>
        </div>
        <div class="mt-3">
          <label class="main-address">
            <input class="form-check-input a-principal" id="abaPrincipal" type="radio" name="enderecoPrincipal" ${e.principal ? 'checked' : ''}> 
            Definir como endereço principal
          </label>
        </div>
      </div>
    `;

    document.getElementById('abaTipo').value = e.tipoEndereco || 'COMERCIAL';

    const inputsCard = enderecosContainer.querySelectorAll('input, select');
    inputsCard.forEach(inp => {
      inp.addEventListener('input', () => sincronizarCamposParaMemoria());
      inp.addEventListener('change', () => sincronizarCamposParaMemoria());
    });

    const painelCard = document.getElementById('painelCardEndereco');
    if (painelCard) {
      painelCard.addEventListener('keydown', ev => {
        if (ev.key === 'Enter') {
          ev.preventDefault();
          const focusableElements = Array.from(painelCard.querySelectorAll('input, select, button')).filter(item => !item.disabled && item.offsetParent !== null);
          const index = focusableElements.indexOf(ev.target);
          if (index > -1 && focusableElements[index + 1]) {
            focusableElements[index + 1].focus();
          }
        }
      });
    }

    enderecosContainer.querySelectorAll('.aba-endereco').forEach(btn => {
      btn.addEventListener('click', () => {
        sincronizarCamposParaMemoria();
        enderecoAtivoIndex = parseInt(btn.dataset.index);
        renderizarGerenciadorEnderecos();
      });
    });

    document.getElementById('btnAddNovoEndereco')?.addEventListener('click', () => {
      sincronizarCamposParaMemoria();
      enderecosLista.push({ tipoEndereco: 'OUTRO', pais: 'Brasil', principal: false });
      enderecoAtivoIndex = enderecosLista.length - 1;
      renderizarGerenciadorEnderecos();
    });

    document.getElementById('btnRemoverEnderecoAtivo')?.addEventListener('click', async () => {
      if (enderecosLista.length <= 1) {
        await mostrarAlerta('Atenção', 'O fornecedor deve possuir pelo menos um endereço.', 'warning');
        return;
      }
      enderecosLista.splice(enderecoAtivoIndex, 1);
      enderecoAtivoIndex = 0;
      renderizarGerenciadorEnderecos();
    });

    const cepInput = document.getElementById('abaCep');
    if (cepInput) {
      const executarBuscaCep = async () => {
        const cepLimpo = cepInput.value.replace(/\D/g, '');
        if (cepLimpo.length !== 8) return;
        try {
          const response = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
          const data = await response.json();
          if (data.erro) {
            await mostrarAlerta('Atenção', 'CEP não encontrado.', 'warning');
            return;
          }
          document.getElementById('abaLogradouro').value = data.logradouro || '';
          document.getElementById('abaBairro').value = data.bairro || '';
          document.getElementById('abaCidade').value = data.localidade || '';
          document.getElementById('abaUf').value = (data.uf || '').toUpperCase();
          sincronizarCamposParaMemoria();
          document.getElementById('abaNumero')?.focus();
        } catch (err) {
          console.error('Erro ao consultar CEP:', err);
        }
      };

      cepInput.addEventListener('input', ev => {
        let n = ev.target.value.replace(/\D/g,'').slice(0,8);
        ev.target.value = n.length > 5 ? n.slice(0,5) + '-' + n.slice(5) : n;
        if (n.length === 8) executarBuscaCep();
      });
      cepInput.addEventListener('blur', executarBuscaCep);
    }
  }

  function sincronizarCamposParaMemoria() {
    if (!enderecosContainer) return;
    const item = enderecosLista[enderecoAtivoIndex];
    if (!item) return;

    item.tipoEndereco = document.getElementById('abaTipo')?.value || 'COMERCIAL';
    item.cep = document.getElementById('abaCep')?.value || '';
    item.logradouro = document.getElementById('abaLogradouro')?.value || '';
    item.numero = document.getElementById('abaNumero')?.value || '';
    item.complemento = document.getElementById('abaComplemento')?.value || '';
    item.bairro = document.getElementById('abaBairro')?.value || '';
    item.cidade = document.getElementById('abaCidade')?.value || '';
    item.uf = (document.getElementById('abaUf')?.value || '').toUpperCase();
    item.pais = document.getElementById('abaPais')?.value || 'Brasil';
    item.referencia = document.getElementById('abaReferencia')?.value || '';
    item.principal = document.getElementById('abaPrincipal')?.checked || false;

    if (item.principal) {
      enderecosLista.forEach((adr, idx) => {
        if (idx !== enderecoAtivoIndex) adr.principal = false;
      });
    }
  }

  function collectAddresses() {
    sincronizarCamposParaMemoria();
    return enderecosLista;
  }

  // --- CONTROLE DE ABAS DE FILIAIS ---
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

    if (!idsMarcados.includes(String(filialAtivaId))) {
        filialAtivaId = idsMarcados[0];
    }

    let htmlAbas = '';
    checkboxesSelecionados.forEach(cb => {
        const id = cb.value;
        const labelParent = cb.closest('label');
        const sufixo = labelParent.querySelector('.badge').innerText;
        const isAtiva = String(id) === String(filialAtivaId) ? 'btn-primary' : 'btn-outline-secondary';

        htmlAbas += `<button type="button" class="btn btn-sm ${isAtiva} filial-aba" data-filial-id="${id}">${sufixo}</button>`;
    });

    containerAbas.innerHTML = htmlAbas;

    if (painelUnico) painelUnico.classList.remove('d-none');

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
        if (textoObs) {
            textoObs.value = mapaObservacoesFiliais[filialAtivaId] || '';
        }
    }

    containerAbas.querySelectorAll('.filial-aba').forEach(btn => {
        btn.onclick = () => {
            if (filialAtivaId && textoObs) {
                mapaObservacoesFiliais[filialAtivaId] = textoObs.value;
            }
            filialAtivaId = btn.dataset.filialId;
            atualizarAbasFiliais();
        };
    });
  }

  document.getElementById('textoObsAtiva')?.addEventListener('input', e => {
      if (filialAtivaId) {
          mapaObservacoesFiliais[filialAtivaId] = e.target.value;
      }
  });

  // --- MÁSCARAS ---
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

  function maskTelefone(e) {
    let v = e.target.value.replace(/\D/g, '');
    if (v.startsWith('0')) {
      v = v.slice(0, 11);
      if (v.length > 7) {
        v = v.replace(/^(\d{4})(\d{3})(\d{0,4})/, '$1-$2-$3');
      } else if (v.length > 4) {
        v = v.replace(/^(\d{4})(\d{0,3})/, '$1-$2');
      }
    } else {
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

  // Inicialização no carregamento
  if (form) {
    document.querySelectorAll('.filial-check').forEach(c => {
        if (filiaisAtuais.includes(Number(c.value))) {
            c.checked = true;
        }
    });
    
    atualizarAbasFiliais();
    renderizarGerenciadorEnderecos();
    
    // Submissão do formulário com validações via ModalService
    form.addEventListener('submit', async ev => {
      ev.preventDefault();
      if (!form.reportValidity()) return;

      const doc = document.getElementById('documento').value.replace(/\D/g,'');
      const pf = document.getElementById('tipoPessoa')?.value === 'PF';
      
      if (pf && doc.length !== 11) { 
        await mostrarAlerta('Atenção', 'Informe um CPF válido com exatamente 11 dígitos.', 'warning'); 
        return; 
      }
      if (!pf && doc.length !== 14) { 
        await mostrarAlerta('Atenção', 'Informe um CNPJ válido com exatamente 14 dígitos.', 'warning'); 
        return; 
      }

      // 👉 Validação da Inscrição Estadual (Permite de 8 a 14 dígitos)
      const ie = document.getElementById('inscricaoEstadual').value.replace(/\D/g, '');
      if (ie.length < 8 || ie.length > 14) {
        await mostrarAlerta('Atenção', 'Inscrição Estadual inválida. Deve conter entre 8 e 14 dígitos numéricos.', 'warning');
        return;
      }

      const textoObsAtiva = document.getElementById('textoObsAtiva');
      if (filialAtivaId && textoObsAtiva) {
          mapaObservacoesFiliais[filialAtivaId] = textoObsAtiva.value;
      }

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

      if (idsFiliaisSelecionadas.length === 0) {
        await mostrarAlerta('Atenção', 'Selecione pelo menos uma filial ou matriz autorizada para este fornecedor.', 'warning');
        return;
      }

      document.getElementById('filiaisObservacoesJson').value = JSON.stringify(observacoesPorFilial);
      
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
        await mostrarAlerta('Atenção', 'Defina apenas um endereço principal.', 'warning'); 
        return; 
      }
      
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
        
        await mostrarAlerta('Sucesso', result.mensagem, 'success');
        setTimeout(() => window.location.href = ctx + '/ConsultaFornecedorServlet', 500);
      } catch(err) { 
        await mostrarAlerta('Erro', err.message || 'Falha de comunicação com o servidor.', 'error'); 
      } finally { 
        btns.forEach(b => { 
          b.disabled = false; 
          if(b.dataset.oldText) b.innerHTML = b.dataset.oldText; 
        }); 
      }
    });
  }

  window.excluirFornecedor = async (id, context) => {
    if (typeof ModalService !== 'undefined' && typeof ModalService.confirm === 'function') {
      const confirmado = await ModalService.confirm(
        'Confirmar Exclusão',
        'Deseja realmente excluir este fornecedor? Os endereços e vínculos com filiais também serão removidos.',
        'warning'
      );
      if (confirmado) await executarExclusao(id, context);
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
        let result = JSON.parse(responseText);

        if (!r.ok || !result.sucesso) throw new Error(result.mensagem || 'Não foi possível excluir.'); 
        
        if (typeof ModalService !== 'undefined' && typeof ModalService.success === 'function') {
          await ModalService.success('Sucesso', result.mensagem || 'Fornecedor excluído com sucesso.');
          location.reload();
        } else {
          location.reload();
        }
      } catch(e) { 
        await mostrarAlerta('Atenção', e.message || 'Erro ao excluir fornecedor.', 'error');
      }
  }

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
        const result = await response.json();

        if (!response.ok || !result.sucesso) throw new Error(result.mensagem || 'Não foi possível carregar os dados.');

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
             <label class="text-muted small d-block">Site / Links</label>
              <span>
                ${f.site ? f.site.split(/[\s,]+/).filter(link => link.trim() !== '').map(link => {
                  let urlFormatada = link.trim();
                  if (!urlFormatada.startsWith('http://') && !urlFormatada.startsWith('https://')) {
                    urlFormatada = 'https://' + urlFormatada;
                  }
                  return `<a href="${esc(urlFormatada)}" target="_blank" rel="noopener noreferrer" class="text-decoration-none d-block text-truncate"><i class="fa-solid fa-globe me-1"></i>${esc(link.trim())}</a>`;
                }).join('') : '-'}
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
            
            <div class="col-12">
              <hr class="my-2">
              <label class="fw-bold text-secondary mb-2">
                  <i class="fa-solid fa-code-branch me-1 text-primary"></i> Observações Específicas por Filial
              </label>
			  <div class="list-group">
			    ${f.filiaisComObservacoes && f.filiaisComObservacoes.length > 0 ? f.filiaisComObservacoes.map(item => `
			      <div class="list-group-item bg-light mb-2 rounded border d-flex flex-column align-items-start text-start">
			        <strong><i class="fa-solid fa-building me-1"></i>${esc(item.nomeFilial)}</strong>
			        <p class="text-muted small mb-0 mt-1 text-start" style="white-space: pre-wrap; word-break: break-word;">${esc((item.observacao || 'Nenhuma observação específica cadastrada.').trim())}</p>
			      </div>
			    `).join('') : '<span class="text-muted small text-start d-block">Nenhuma filial com observação específica.</span>'}
			  </div>
            </div>

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
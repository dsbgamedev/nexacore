(() => {
  const ctx = window.NEXACORE_CONTEXT || '';
  const form = document.getElementById('formFornecedor');
  const alertBox = document.getElementById('formAlert');
  const enderecosContainer = document.getElementById('enderecosContainer');
  const enderecos = Array.isArray(window.FORNECEDOR_ENDERECOS) ? window.FORNECEDOR_ENDERECOS : [];
  const filiaisAtuais = (Array.isArray(window.FORNECEDOR_FILIAIS) ? window.FORNECEDOR_FILIAIS : []).map(Number);

  function showAlert(message, success) {
    if (!alertBox) return;
    alertBox.className = 'alert ' + (success ? 'alert-success' : 'alert-danger');
    alertBox.textContent = message;
    alertBox.classList.remove('d-none');
    alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
  function esc(v) { return String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
  
  // Função para buscar o endereço automaticamente via ViaCEP
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

  function addressCard(e = {}, idx = Date.now()) {
    const el = document.createElement('div'); el.className = 'address-card'; el.dataset.address = '1';
    el.innerHTML = `<div class="address-card-head"><div><span class="address-index">ENDEREÇO</span><strong class="address-label">${esc(e.tipoEndereco || 'COMERCIAL')}</strong></div><button type="button" class="btn-icon danger remove-address" title="Remover endereço"><i class="fa-regular fa-trash-can"></i></button></div>
      <div class="form-grid"><div class="field"><label>Tipo</label><select class="form-select a-tipo"><option value="COMERCIAL">Comercial</option><option value="COBRANCA">Cobrança</option><option value="ENTREGA">Entrega</option><option value="OPERACIONAL">Operacional</option><option value="OUTRO">Outro</option></select></div><div class="field"><label>CEP</label><input class="form-control a-cep" maxlength="9" value="${esc(e.cep)}" placeholder="00000-000"></div><div class="field span-2"><label>Logradouro</label><input class="form-control a-logradouro" value="${esc(e.logradouro)}" maxlength="200"></div><div class="field"><label>Número</label><input class="form-control a-numero" value="${esc(e.numero)}" maxlength="20"></div><div class="field"><label>Complemento</label><input class="form-control a-complemento" value="${esc(e.complemento)}" maxlength="100"></div><div class="field"><label>Bairro</label><input class="form-control a-bairro" value="${esc(e.bairro)}" maxlength="100"></div><div class="field"><label>Cidade</label><input class="form-control a-cidade" value="${esc(e.cidade)}" maxlength="100"></div><div class="field"><label>UF</label><input class="form-control a-uf" value="${esc(e.uf)}" maxlength="2"></div><div class="field"><label>País</label><input class="form-control a-pais" value="${esc(e.pais || 'Brasil')}" maxlength="60"></div><div class="field span-2"><label>Referência</label><input class="form-control a-referencia" value="${esc(e.referencia)}" maxlength="200"></div></div>
      <label class="main-address"><input class="form-check-input a-principal" type="radio" name="enderecoPrincipal" ${e.principal ? 'checked' : ''}> Definir como endereço principal</label>`;
    
    el.querySelector('.a-tipo').value = e.tipoEndereco || 'COMERCIAL';
    el.querySelector('.remove-address').addEventListener('click', () => { el.remove(); refreshAddresses(); });
    el.querySelector('.a-principal').addEventListener('change', () => { document.querySelectorAll('.a-principal').forEach(r => { if (r !== el.querySelector('.a-principal')) r.checked = false; }); });
    el.querySelector('.a-tipo').addEventListener('change', () => { el.querySelector('.address-label').textContent = el.querySelector('.a-tipo').selectedOptions[0].textContent.toUpperCase(); });
    
    const cepInput = el.querySelector('.a-cep');
    cepInput.addEventListener('input', ev => { 
      let n = ev.target.value.replace(/\D/g,'').slice(0,8); 
      ev.target.value = n.length > 5 ? n.slice(0,5) + '-' + n.slice(5) : n;
      if (n.length === 8) {
        buscarCep(cepInput, el);
      }
    });
    cepInput.addEventListener('blur', () => buscarCep(cepInput, el));

    enderecosContainer.appendChild(el); 
    refreshAddresses();
  }

  function refreshAddresses() {
    const cards = [...(enderecosContainer?.querySelectorAll('.address-card') || [])];
    const empty = document.getElementById('semEndereco'); if (empty) empty.classList.toggle('d-none', cards.length > 0);
    cards.forEach((c,i) => { c.querySelector('.address-index').textContent = `ENDEREÇO ${String(i+1).padStart(2,'0')}`; });
    if (cards.length && !cards.some(c => c.querySelector('.a-principal').checked)) cards[0].querySelector('.a-principal').checked = true;
  }

  function collectAddresses() {
    return [...(enderecosContainer?.querySelectorAll('.address-card') || [])].map(c => ({
      tipoEndereco:c.querySelector('.a-tipo').value, cep:c.querySelector('.a-cep').value,
      logradouro:c.querySelector('.a-logradouro').value, numero:c.querySelector('.a-numero').value,
      complemento:c.querySelector('.a-complemento').value, bairro:c.querySelector('.a-bairro').value,
      cidade:c.querySelector('.a-cidade').value, uf:c.querySelector('.a-uf').value.toUpperCase(),
      pais:c.querySelector('.a-pais').value, referencia:c.querySelector('.a-referencia').value,
      principal:c.querySelector('.a-principal').checked
    }));
  }

  function maskDoc() {
    const input = document.getElementById('documento'); if (!input) return;
    const pf = document.getElementById('tipoPessoa')?.value === 'PF';
    let n = input.value.replace(/\D/g,'').slice(0, pf ? 11 : 14);
    if (pf) { 
      n = n.replace(/(\d{3})(\d)/,'$1.$2').replace(/(\d{3})(\d)/,'$1.$2').replace(/(\d{3})(\d{1,2})$/,'$1-$2'); 
    } else { 
      n = n.replace(/^(\d{2})(\d)/,'$1.$2').replace(/^(\d{2})\.(\d{3})(\d)/,'$1.$2.$3').replace(/\.(\d{3})(\d)/,'.$1/$2').replace(/(\d{4})(\d)/,'$1-$2'); 
    }
    input.value = n;
  }

  document.getElementById('addEndereco')?.addEventListener('click', () => addressCard({tipoEndereco:'COMERCIAL',pais:'Brasil',principal:!enderecosContainer.querySelector('.address-card')}));
  document.getElementById('marcarTodas')?.addEventListener('click', () => document.querySelectorAll('.filial-check').forEach(c => c.checked=true));
  document.getElementById('limparFiliais')?.addEventListener('click', () => document.querySelectorAll('.filial-check').forEach(c => c.checked=false));
  
  document.getElementById('tipoPessoa')?.addEventListener('change', () => { 
    const d = document.getElementById('documento'); 
    if(d) { d.value = ''; maskDoc(); } 
    d?.focus(); 
  });
  
  document.getElementById('documento')?.addEventListener('input', maskDoc);

  if (form) {
    document.querySelectorAll('.filial-check').forEach(c => c.checked = filiaisAtuais.includes(Number(c.value)));
    enderecos.forEach(e => addressCard(e));
    
    form.addEventListener('submit', async ev => {
      ev.preventDefault();
      if (!form.reportValidity()) return;

      const doc = document.getElementById('documento').value.replace(/\D/g,'');
      const pf = document.getElementById('tipoPessoa').value === 'PF';
      
      if (pf && doc.length !== 11) { 
        showAlert('Informe um CPF válido com exatamente 11 dígitos.', false); 
        return; 
      }
      if (!pf && doc.length !== 14) { 
        showAlert('Informe um CNPJ válido com exatamente 14 dígitos.', false); 
        return; 
      }

      // Validação de Filiais Obrigatórias
      const filiaisMarcadas = document.querySelectorAll('.filial-check:checked');
      if (filiaisMarcadas.length === 0) {
        showAlert('Selecione pelo menos uma filial ou matriz autorizada.', false);
        return;
      }

      const addrs = collectAddresses();
      if (addrs.filter(a => a.principal).length > 1) { 
        showAlert('Defina apenas um endereço principal.', false); 
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
        const r = await fetch(ctx + '/FornecedorServlet', {method:'POST', body:data, headers:{'X-Requested-With':'XMLHttpRequest'}});
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

  window.excluirFornecedor = async (id, context) => {
    if (!confirm('Deseja realmente excluir este fornecedor? Os endereços e vínculos com filiais também serão removidos.')) return;
    const data = new URLSearchParams({acao:'excluir', id:String(id)});
    try { 
      const r = await fetch(context + '/FornecedorServlet', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:data}); 
      const result = await r.json(); 
      if (!r.ok || !result.sucesso) throw new Error(result.mensagem || 'Não foi possível excluir.'); 
      location.reload(); 
    } catch(e) { 
      alert(e.message || 'Erro ao excluir fornecedor.'); 
    }
  };
})();
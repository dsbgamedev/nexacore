document.addEventListener("DOMContentLoaded", function() {
    const inputData = document.getElementById('dataAbertura');
    if (inputData) {
        inputData.value = new Date().toISOString().split('T')[0];
    }

	carregarSelectEquipamentos();
    carregarSelectFiliais();
    carregarSelectDepartamentos();

    const selectEquip = document.getElementById('selectEquipamento');
    if (selectEquip) {
        selectEquip.addEventListener('change', function() {
            const idEq = this.value;
            if (idEq) {
                buscarDetalhesEquipamento(idEq);
                buscarHistoricoCompletoEquipamento(idEq);
            } else {
                limparCardEquipamento();
            }
        });
    }

    const formChamado = document.getElementById('formChamado');
    if (formChamado) {
        formChamado.addEventListener('submit', function(e) {
            e.preventDefault();
            
            ModalService.confirm("Confirmação", "Deseja realmente abrir este chamado de manutenção?").then(confirmado => {
                if (confirmado) {
                    enviarChamadoManutencao();
                }
            });
        });
    }
});

function carregarSelectEquipamentos() {
    // Alinhado com o banco de dados: busca equipamentos com situacaoId = 6 (Na Assistência)
    fetch(contextPath + '/api/equipamentos?situacao=6')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('selectEquipamento');
            if (!select) return;

            select.innerHTML = '<option value="">Digite ou selecione o equipamento...</option>';
            const lista = Array.isArray(data) ? data : [data];
            
            lista.forEach(eq => {
                // Dupla checagem de segurança no front para garantir que apenas situação 6 seja exibida
                let situacaoId = eq.situacaoId || eq.idSituacao;
                if (situacaoId && Number(situacaoId) !== 6) return; 

                let nomeCpu = eq.nomeIdentificador || eq.nomeCpu || '';
                let patrimonio = eq.patrimonio || 'S/P';
                let texto = `${eq.idSistema || 'ID'} - ${patrimonio} ${nomeCpu ? '(' + nomeCpu + ')' : ''}`;
                
                let option = new Option(texto, eq.idEquipamento || eq.id);
                select.add(option);
            });
        })
        .catch(err => console.error("Erro ao carregar equipamentos:", err));
}

function carregarSelectFiliais() {
    fetch(contextPath + '/api/empresas')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('selectFilial');
            if (!select) return;
            select.innerHTML = '<option value="">Selecione a filial...</option>';
            
            const lista = Array.isArray(data) ? data : [data];
            lista.forEach(f => {
                let id = f.idFilial || f.id;
                let nome = f.nomeEmpresa || f.nome || f.sufixo;
                let origem = f.origemCodigo || f.origem; // Captura o código de origem (ex: 161)
                
                if (id && nome) {
                    let option = new Option(nome, id);
                    if (origem) {
                        option.setAttribute('data-origem', origem); // Atribui o data-origem na option
                    }
                    select.add(option);
                }
            });
        })
        .catch(err => {
            console.error("Erro ao carregar filiais:", err);
            const select = document.getElementById('selectFilial');
            if (select) select.innerHTML = '<option value="">Erro ao carregar filiais</option>';
        });
}

function buscarDetalhesEquipamento(idEquipamento) {
    fetch(`${contextPath}/api/equipamentos?id=${idEquipamento}`)
        .then(res => res.json())
        .then(eq => {
            if (!eq) return;

            document.getElementById('patrimonio').value = eq.patrimonio || '---';
            document.getElementById('idSistema').value = eq.idSistema || '---';
            
            const lblNome = document.getElementById('lblNomeEquipamento');
            if (lblNome) lblNome.innerText = eq.nomeIdentificador || eq.nomeCpu || '---';

            const lblSerie = document.getElementById('lblSerie');
            if (lblSerie) lblSerie.innerText = eq.numeroSerie || '---';

            const lblUsuario = document.getElementById('lblUsuario');
            if (lblUsuario) lblUsuario.innerText = eq.usuarioAtual || eq.responsavel || '---';

            // 1. TRATAMENTO CORRETO PARA A FILIAL / LOCAL
            const selectFilial = document.getElementById('selectFilial');
            const lblLocal = document.getElementById('lblLocal');
            
            // Pega o código de origem do equipamento (ex: 161)
            let origemCodigoEquip = eq.origemCodigo || eq.origem;
            let filialNomeEncontrada = '---';

            if (selectFilial && origemCodigoEquip) {
                for (let i = 0; i < selectFilial.options.length; i++) {
                    let opt = selectFilial.options[i];
                    let optText = opt.text; // Ex: "CBA DIESEL SP ATACADISTA DE PECAS LTDA - SSA" ou similar
                    
                    // Como o option do select geralmente exibe o sufixo ou nome, ou podemos verificar se o texto contém o código
                    // Vamos testar se o texto da opção inclui o código de origem (ex: "161") ou se guardamos o atributo data-origem
                    if (optText.includes(String(origemCodigoEquip)) || opt.getAttribute('data-origem') == origemCodigoEquip) {
                        selectFilial.value = opt.value; // Seleciona o id_filial correto (ex: 7)
                        filialNomeEncontrada = optText;
                        break;
                    }
                }
            }

            if (lblLocal) lblLocal.innerText = filialNomeEncontrada;
            if (selectFilial) selectFilial.disabled = true; // Trava o campo

            // 2. TRATAMENTO PARA O DEPARTAMENTO (Já funciona, mantido igual)
            const selectDepto = document.getElementById('selectDepartamento');
            const lblDepto = document.getElementById('lblDepto');
            let deptoId = eq.idDepartamento || eq.departamentoId;

            if (selectDepto && deptoId) {
                selectDepto.value = deptoId;
                if (selectDepto.selectedIndex !== -1 && lblDepto) {
                    lblDepto.innerText = selectDepto.options[selectDepto.selectedIndex].text;
                }
            } else if (lblDepto) {
                lblDepto.innerText = eq.departamentoNome || '---';
            }
            if (selectDepto) selectDepto.disabled = true;

            const badgeSituacao = document.getElementById('lblSituacao');
            const situacaoNome = eq.situacaoAtual || eq.situacaoNome || 'Disponível';
            if (badgeSituacao) {
                badgeSituacao.innerText = situacaoNome;
                badgeSituacao.className = "badge " + (situacaoNome.toLowerCase().includes('manutenção') ? 'bg-warning text-dark' : 'bg-success');
            }

            const badgeStatus = document.getElementById('lblStatus');
            const statusNome = eq.statusNome || 'Em Manutenção';
            if (badgeStatus) {
                badgeStatus.innerText = statusNome;
                badgeStatus.className = "badge bg-warning text-dark";
            }

            const btnHistTopo = document.getElementById('btnHistoricoEquipamento');
            if (btnHistTopo) btnHistTopo.disabled = false;
        })
        .catch(err => console.error("Erro ao buscar detalhes do equipamento:", err));
}
function limparCardEquipamento() {
    document.getElementById('patrimonio').value = '';
    document.getElementById('idSistema').value = '';
    
    if (document.getElementById('lblNomeEquipamento')) document.getElementById('lblNomeEquipamento').innerText = 'Selecione um equipamento';
    if (document.getElementById('lblSerie')) document.getElementById('lblSerie').innerText = '---';
    if (document.getElementById('lblLocal')) document.getElementById('lblLocal').innerText = '---';
    if (document.getElementById('lblUsuario')) document.getElementById('lblUsuario').innerText = '---';
    if (document.getElementById('lblDepto')) document.getElementById('lblDepto').innerText = '---';
    
    // Reseta e bloqueia novamente os selects da seção 2 quando desmarcar o equipamento
    const selectFilial = document.getElementById('selectFilial');
    if (selectFilial) {
        selectFilial.value = "";
        selectFilial.disabled = true;
    }

    const selectDepto = document.getElementById('selectDepartamento');
    if (selectDepto) {
        selectDepto.value = "";
        selectDepto.disabled = true;
    }
    
    const badgeSituacao = document.getElementById('lblSituacao');
    if (badgeSituacao) {
        badgeSituacao.innerText = '---';
        badgeSituacao.className = "badge bg-secondary";
    }
    const badgeStatus = document.getElementById('lblStatus');
    if (badgeStatus) {
        badgeStatus.innerText = '---';
        badgeStatus.className = "badge bg-secondary";
    }

    document.getElementById('tabelaHistoricoManutencoes').innerHTML = '<tr><td colspan="7" class="text-center text-muted">Selecione um equipamento para carregar o histórico.</td></tr>';
    document.getElementById('tabelaHistoricoMovimentacoes').innerHTML = '<tr><td colspan="6" class="text-center text-muted">Selecione um equipamento para carregar as movimentações.</td></tr>';
    
    document.getElementById('resumoTotalManutencoes').innerText = '0';
    document.getElementById('resumoUltimaManutencao').innerText = '---';
    document.getElementById('resumoTempoMedio').innerText = '---';
    document.getElementById('resumoTaxaConclusao').innerText = '---';

    const linkHist = document.getElementById('linkHistoricoCompleto');
    if (linkHist) {
        linkHist.style.pointerEvents = 'none';
        linkHist.style.opacity = '0.5';
    }

    const btnHistTopo = document.getElementById('btnHistoricoEquipamento');
    if (btnHistTopo) btnHistTopo.disabled = true;
}

function buscarHistoricoCompletoEquipamento(idEquipamento) {
    fetch(`${contextPath}/api/manutencoes?idEquipamento=${idEquipamento}`)
        .then(res => res.json())
        .then(lista => {
            const tbody = document.getElementById('tabelaHistoricoManutencoes');
            tbody.innerHTML = '';

            if (!Array.isArray(lista) || lista.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Nenhum chamado anterior registrado.</td></tr>';
                document.getElementById('resumoTotalManutencoes').innerText = '0';
                document.getElementById('resumoUltimaManutencao').innerText = '---';
                return;
            }

            document.getElementById('resumoTotalManutencoes').innerText = lista.length;
            document.getElementById('resumoUltimaManutencao').innerText = lista[0].dataAbertura || '---';

            const linkHist = document.getElementById('linkHistoricoCompleto');
            if (linkHist) {
                linkHist.style.pointerEvents = 'auto';
                linkHist.style.opacity = '1';
                linkHist.href = `${contextPath}/equipamentos/detalhes.jsp?id=${idEquipamento}`;
            }

            const ultimos5 = lista.slice(0, 5);

            ultimos5.forEach(man => {
                let idFormatado = 'MAN-' + String(man.idChamado || 0).padStart(6, '0');
                let tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${idFormatado}</td>
                    <td>${man.dataAbertura || ''}</td>
                    <td>${man.tipoProblema || 'Corretiva'}</td>
                    <td>${man.descricaoProblema || ''}</td>
                    <td>${man.responsavelTecnico || 'Não atribuído'}</td>
                    <td><span class="badge bg-success">${man.statusChamado || 'Concluída'}</span></td>
                    <td class="text-center">
                        <button type="button" class="btn btn-sm btn-light py-0 px-1" title="Visualizar Chamado" onclick="visualizarChamado(${man.idChamado})">
                            <i class="fa fa-eye text-secondary"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });

            const btnVerMais = document.getElementById('btnVerMaisManutencoes');
            if (btnVerMais) {
                btnVerMais.style.display = lista.length > 5 ? 'inline-block' : 'none';
            }
        })
        .catch(() => {
            document.getElementById('tabelaHistoricoManutencoes').innerHTML = '<tr><td colspan="7" class="text-center text-muted">Erro ao carregar histórico.</td></tr>';
        });

    fetch(`${contextPath}/api/movimentacoes?idEquipamento=${idEquipamento}`)
        .then(res => res.json())
        .then(movs => {
            const tbodyMov = document.getElementById('tabelaHistoricoMovimentacoes');
            if (!tbodyMov) return;
            tbodyMov.innerHTML = '';

            if (!Array.isArray(movs) || movs.length === 0) {
                tbodyMov.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Nenhuma movimentação registrada.</td></tr>';
                return;
            }

            movs.slice(0, 5).forEach(mov => {
                let tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${mov.data || ''}</td>
                    <td>${mov.tipo || ''}</td>
                    <td>${mov.origem || '---'}</td>
                    <td>${mov.destino || '---'}</td>
                    <td>${mov.situacao || ''}</td>
                    <td>${mov.responsavel || ''}</td>
                `;
                tbodyMov.appendChild(tr);
            });
        })
        .catch(() => {
            const tbodyMov = document.getElementById('tabelaHistoricoMovimentacoes');
            if (tbodyMov) {
                tbodyMov.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Nenhuma movimentação registrada.</td></tr>';
            }
        });
}

function visualizarChamado(id) {
    ModalService.info("Detalhes do Chamado", "Visualizar chamado completo ID: " + id);
}

function enviarChamadoManutencao() {
    const form = document.getElementById('formChamado');
    
    // Habilita temporariamente os selects travados para que seus valores entrem no FormData
    const selectFilial = document.getElementById('selectFilial');
    const selectDepto = document.getElementById('selectDepartamento');
    
    const filialWasDisabled = selectFilial ? selectFilial.disabled : false;
    const deptoWasDisabled = selectDepto ? selectDepto.disabled : false;
    
    if (selectFilial) selectFilial.disabled = false;
    if (selectDepto) selectDepto.disabled = false;

    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());

    // Restaura o estado travado dos selects para o usuário logo em seguida
    if (selectFilial) selectFilial.disabled = filialWasDisabled;
    if (selectDepto) selectDepto.disabled = deptoWasDisabled;

    fetch(contextPath + '/api/manutencoes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json;charset=UTF-8' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(resposta => {
        if (resposta.sucesso) {
            ModalService.success("Sucesso", resposta.mensagem).then(() => {
                window.location.reload();
            });
        } else {
            ModalService.error("Atenção", resposta.mensagem);
        }
    })
    .catch(() => {
        ModalService.error("Erro", "Erro de comunicação ao registrar o chamado.");
    });
}

function carregarSelectDepartamentos() {
    fetch(contextPath + '/api/departamentos')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('selectDepartamento');
            if (!select) return;
            
            select.innerHTML = '<option value="">Selecione o departamento...</option>';
            
            const lista = Array.isArray(data) ? data : [data];
            lista.forEach(d => {
                // Utiliza as propriedades mapeadas no seu Departamento.java
                let id = d.idDepartamento;
                let nome = d.nomeDepartamento;
                
                if (id && nome) {
                    select.add(new Option(nome, id));
                }
            });
        })
        .catch(err => {
            console.error("Erro ao carregar departamentos:", err);
            const select = document.getElementById('selectDepartamento');
            if (select) {
                select.innerHTML = '<option value="">Erro ao carregar departamentos</option>';
            }
        });
}
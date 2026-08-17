<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Nexacore - Abertura de Chamado</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body { background-color: #f8f9fa; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        .main-container { max-width: 1200px; margin: 30px auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.05); }
        .section-title { font-size: 1.1rem; font-weight: 600; color: #1e293b; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; margin-bottom: 20px; }
        .card-preview { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 20px; }
    </style>
</head>
<body>

    <div class="main-container">
        <h2 class="mb-4 text-primary"><i class="fa-solid fa-screwdriver-wrench"></i> Abertura de Chamado de Manutenção</h2>
        <p class="text-muted">Registre um chamado para manutenção de equipamento e acompanhe o histórico.</p>

        <form id="formChamado">
            
            <!-- SEÇÃO 1: EQUIPAMENTO -->
            <div class="section-title"><i class="fa-solid fa-laptop"></i> 1. Equipamento</div>
            <div class="row mb-4">
                <div class="col-md-6 mb-3">
                    <label for="selectEquipamento" class="form-label font-weight-bold">Selecionar Equipamento *</label>
                    <select class="form-select" id="selectEquipamento" name="idEquipamento" required>
                        <option value="">Digite ou selecione o equipamento...</option>
                        <!-- Preenchido via JS ou DAO -->
                    </select>
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">Número de Patrimônio / Etiqueta</label>
                    <input type="text" class="form-control" id="patrimonio" readonly placeholder="---">
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">ID Sistema</label>
                    <input type="text" class="form-control" id="idSistema" readonly placeholder="---">
                </div>

                <!-- Card de Pré-visualização Detalhada -->
                <div class="col-12">
                    <div class="card-preview">
                        <div class="row">
                            <div class="col-md-3">
                                <span class="text-muted d-block small">Nº de Série</span>
                                <strong id="lblSerie">---</strong>
                            </div>
                            <div class="col-md-3">
                                <span class="text-muted d-block small">Usuário Atual</span>
                                <strong id="lblUsuario">---</strong>
                            </div>
                            <div class="col-md-3">
                                <span class="text-muted d-block small">Local Atual</span>
                                <strong id="lblLocal">---</strong>
                            </div>
                            <div class="col-md-3">
                                <span class="text-muted d-block small">Situação Atual</span>
                                <span id="lblSituacao" class="badge bg-secondary">---</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- SEÇÃO 2: DADOS DA MANUTENÇÃO -->
            <div class="section-title"><i class="fa-solid fa-file-lines"></i> 2. Dados da Manutenção</div>
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <label class="form-label">Tipo de Manutenção *</label>
                    <select class="form-select" name="tipoManutencao" required>
                        <option value="Corretiva">Corretiva</option>
                        <option value="Preventiva">Preventiva</option>
                    </select>
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">Prioridade *</label>
                    <select class="form-select" name="prioridade" required>
                        <option value="Baixa">Baixa</option>
                        <option value="Média" selected>Média</option>
                        <option value="Alta">Alta</option>
                        <option value="Urgente">Urgente</option>
                    </select>
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">Data da Abertura *</label>
                    <input type="date" class="form-control" id="dataAbertura" name="dataAbertura" required>
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">Previsão de Atendimento</label>
                    <input type="date" class="form-control" name="previsaoAtendimento">
                </div>

                <div class="col-md-4 mb-3">
                    <label class="form-label">Responsável pela Abertura *</label>
                    <input type="text" class="form-control" name="solicitante" required placeholder="Ex: Maria Souza">
                </div>
                <div class="col-md-4 mb-3">
                    <label class="form-label">Departamento Solicitante *</label>
                    <input type="text" class="form-control" name="departamento" required placeholder="Ex: Fiscal">
                </div>
                <div class="col-md-4 mb-3">
                    <label class="form-label">Local para Atendimento *</label>
                    <input type="text" class="form-control" name="localAtendimento" required placeholder="Ex: Filial SP">
                </div>

                <div class="col-md-6 mb-3">
                    <label class="form-label">Descrição do Problema *</label>
                    <textarea class="form-control" name="descricaoProblema" rows="3" required placeholder="Descreva a falha apresentada..."></textarea>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Observações Adicionais</label>
                    <textarea class="form-control" name="observacoes" rows="3" placeholder="Informações extras..."></textarea>
                </div>
            </div>

            <!-- SEÇÃO 3: HISTÓRICO DE MANUTENÇÕES DO EQUIPAMENTO (ABAS) -->
            <div class="section-title"><i class="fa-solid fa-clock-rotate-left"></i> 3. Histórico do Equipamento</div>
            <ul class="nav nav-tabs mb-3" id="historicoTabs" role="tablist">
                <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#tabManutencoes" type="button">Manutenções</button></li>
                <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tabMovimentacoes" type="button">Movimentações</button></li>
            </ul>
            <div class="tab-content mb-4" id="historicoContent">
                <div class="tab-pane fade show active" id="tabManutencoes">
                    <table class="table table-sm table-bordered">
                        <thead class="table-light">
                            <tr>
                                <th>#ID</th>
                                <th>Data Abertura</th>
                                <th>Problema</th>
                                <th>Responsável Técnico</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody id="tabelaHistoricoManutencoes">
                            <tr><td colspan="5" class="text-center text-muted">Selecione um equipamento para carregar o histórico.</td></tr>
                        </tbody>
                    </table>
                </div>
                <div class="tab-pane fade" id="tabMovimentacoes">
                    <p class="text-muted text-center py-3">Nenhuma movimentação registrada recente.</p>
                </div>
            </div>

            <!-- BOTÕES DE AÇÃO -->
            <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-secondary" onclick="window.history.back();">Cancelar</button>
                <button type="submit" class="btn btn-success">Abrir Chamado</button>
            </div>

        </form>
    </div>

    <!-- Scripts -->
    <script>
        const contextPath = "${pageContext.request.contextPath}";
        document.addEventListener("DOMContentLoaded", function() {
            // Define a data de hoje automaticamente
            document.getElementById('dataAbertura').value = new Date().toISOString().split('T')[0];
            
            // Aqui você poderá chamar a função para carregar a lista de equipamentos no select
        });
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
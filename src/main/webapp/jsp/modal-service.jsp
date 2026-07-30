<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- ==========================================================
     NEXACORE - MODAL SERVICE
     Modais de utilidade (Alerta/Confirmação) injetados 
     dinamicamente via ModalService.js
========================================================== -->

<!-- Modal de Alerta (Estilo Limpo) -->
<div class="modal fade" id="alertModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <!-- Seu design customizado -->
        <div class="modal-content-custom" id="alertBox">
            <h3 id="alertTitle"></h3>
            <p id="alertMessage"></p>
            <div class="modal-buttons">
                <button type="button" class="btn-confirmar" id="alertOkBtn">OK</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal de Confirmação (Estilo Limpo) -->
<div class="modal fade" id="confirmModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <!-- Mantemos a classe 'modal-content' aqui para o Bootstrap controlar os cliques -->
        <div class="modal-content" style="background: transparent; border: none; box-shadow: none;">
            
            <!-- Seu design customizado fica aqui dentro -->
            <div class="modal-content-custom" id="confirmBox">
                <h3 id="confirmTitle"></h3>
                <p id="confirmMessage"></p>
                <div class="modal-buttons">
                    <button type="button" class="btn-cancelar" id="confirmCancelBtn">Cancelar</button>
                    <button type="button" class="btn-confirmar" id="confirmOkBtn">Confirmar</button>
                </div>
            </div>
            
        </div>
    </div>
</div>
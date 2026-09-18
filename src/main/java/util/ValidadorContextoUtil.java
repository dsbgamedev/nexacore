package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.Usuario;

public class ValidadorContextoUtil {
	
	/**
     * Valida se a filial ativa na sessão do utilizador corresponde à filial onde a operação está a ser feita.
     * Lança uma excepção de segurança caso haja divergência e o utilizador não seja Administrador Global.
     */
    /**
     * Valida se a filial ativa na sessão do usuário corresponde à filial onde a operação está sendo feita.
     * @param request Requisição HTTP atual
     * @param filialOperacaoCodigo O código da filial/origem do registro (ex: 151)
     */
    public static void validarFilialAtiva(HttpServletRequest request, int filialOperacaoCodigo) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("Sessão expirada.");
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            throw new SecurityException("Usuário não autenticado.");
        }

        // Se for Super Administrador ou Administrador global, pode operar em qualquer filial
        String perfil = usuario.getPerfil();
        boolean isAdmin = perfil != null && (
            "SUPER_ADMINISTRADOR".equalsIgnoreCase(perfil) || 
            "ADMINISTRADOR".equalsIgnoreCase(perfil) ||
            perfil.toUpperCase().contains("SUPER")
        );
        
        if (isAdmin) {
            return; // Administradores passam livremente
        }

        // Pega a filial atualmente ativa selecionada no menu superior (ex: 121)
        Integer filialAtivaId = usuario.getUnidadeAtivaId();
        
        if (filialAtivaId == null) {
            throw new SecurityException("Nenhuma filial ativa selecionada no menu superior.");
        }

        // REGRA DE OURO: A filial ativa no menu tem de ser exatamente igual à filial do equipamento/operação
        if (filialAtivaId != filialOperacaoCodigo) {
            throw new SecurityException(
                "Ação bloqueada por segurança! Sua filial ativa no menu superior é a [" + filialAtivaId + 
                "], mas você tentou movimentar/devolver um item pertencente à filial [" + filialOperacaoCodigo + 
                "]. Por favor, altere a filial no menu superior para [" + filialOperacaoCodigo + "] antes de prosseguir."
            );
        }
    }
}
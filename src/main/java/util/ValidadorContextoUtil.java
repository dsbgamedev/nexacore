package util;

import dao.FilialDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.Filial;
import model.Usuario;

public class ValidadorContextoUtil {
    
    /**
     * Valida se a filial ativa na sessão do usuário corresponde à filial onde a operação está sendo feita.
     * @param request Requisição HTTP atual
     * @param filialOperacaoCodigo O código de origem ou ID da filial da operação
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

        // Pega o ID da filial ativa selecionada no menu superior (ex: 3)
        Integer filialAtivaId = usuario.getUnidadeAtivaId();
        
        if (filialAtivaId == null) {
            throw new SecurityException("Nenhuma filial ativa selecionada no menu superior.");
        }

        FilialDAO filialDAO = new FilialDAO();
        int origemCodigoAtiva = -1;
        int origemCodigoOperacaoReal = filialOperacaoCodigo;
        String nomeFilialAtivaStr = String.valueOf(filialAtivaId);
        String nomeFilialOperacaoStr = String.valueOf(filialOperacaoCodigo);

        try {
            // 1. Descobre o código de origem da filial ativa na sessão
            Integer codigoOrigemAtivo = filialDAO.buscarOrigemCodigoPorId(filialAtivaId);
            if (codigoOrigemAtivo != null) {
                origemCodigoAtiva = codigoOrigemAtivo;
            } else {
                origemCodigoAtiva = filialAtivaId; // Fallback caso não ache
            }

            // 2. Varre as filiais para formatar os nomes bonitinhos e converter caso tenham passado o ID no lugar do código
            for (Filial f : filialDAO.listar()) {
                if (f.getIdFilial() == filialAtivaId) {
                    nomeFilialAtivaStr = f.getOrigemCodigo() + " - " + f.getSufixo();
                }
                
                // Trata se o valor recebido for o ID da filial ou o Código de Origem
                if (f.getIdFilial() == filialOperacaoCodigo || f.getOrigemCodigo() == filialOperacaoCodigo) {
                    origemCodigoOperacaoReal = f.getOrigemCodigo();
                    nomeFilialOperacaoStr = f.getOrigemCodigo() + " - " + f.getSufixo();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // REGRA DE OURO: Compara o código de origem real da filial ativa com o código de origem da operação
        if (origemCodigoAtiva != origemCodigoOperacaoReal) {
            throw new SecurityException(
                "Ação bloqueada por segurança! Sua filial ativa no menu superior é a [" + nomeFilialAtivaStr + 
                "], mas você tentou movimentar/devolver um item pertencente à filial [" + nomeFilialOperacaoStr + 
                "]. Por favor, altere a filial no menu superior para [" + nomeFilialOperacaoStr + "] antes de prosseguir."
            );
        }
    }
}
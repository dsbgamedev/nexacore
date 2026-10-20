package util;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import model.Usuario;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebListener
public class SessaoListener implements HttpSessionListener {

    // Mapa para armazenar as sessões ativas por ID de sessão
    private static final Map<String, HttpSession> sessoesAtivas = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Armazena a sessão assim que é criada
        sessoesAtivas.put(se.getSession().getId(), se.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Remove a sessão quando ela expira ou é encerrada
        sessoesAtivas.remove(se.getSession().getId());
    }

    // Método para listar todas as sessões ativas no sistema
    public static Map<String, HttpSession> getSessoesAtivas() {
        return sessoesAtivas;
    }

    // Método para derrubar a sessão de um usuário específico pelo ID da sessão
    public static boolean invalidarSessaoPorId(String sessionId) {
        HttpSession session = sessoesAtivas.get(sessionId);
        if (session != null) {
            try {
                session.invalidate(); // Mata a sessão do usuário
                sessoesAtivas.remove(sessionId);
                return true;
            } catch (IllegalStateException e) {
                // Sessão já podia estar invalidada
                sessoesAtivas.remove(sessionId);
            }
        }
        return false;
    }
}

package controller;

import dao.UsuarioDAO;
import model.Usuario;
import model.enums.PerfilUsuario;
import dto.ModuloDTO; 
import conexao.Conexao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet("/CadastrarUsuarioServlet")
public class CadastrarUsuarioServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Gson gson = new GsonBuilder()
		    .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(src.toString()))
		    .create();
	
	private boolean validarPermissao(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        boolean isAdmin = usuario != null && ("SUPER_ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()) || "ADMINISTRADOR".equalsIgnoreCase(usuario.getPerfil()));
        boolean temPermissaoModulo = usuario != null && usuario.getModulosPermitidos() != null && usuario.getModulosPermitidos().contains("usuarios"); 

        if (usuario == null || (!isAdmin && !temPermissaoModulo)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acesso negado. Você não possui permissão para executar esta ação.\"}");
            return false;
        }
        return true;
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("DEBUG SERVLET: CadastrarUsuarioServlet - Método doGet chamado.");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
     
		// Adicione esta validação no início para bloquear acessos negados via GET
	    if (!validarPermissao(request, response)) {
	        return;
	    }
		
		HttpSession session = request.getSession(false);
		Usuario usuarioLogado = null;
		if (session != null) {
			usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
		}

		// 1. Verificação de Autenticação
		if (usuarioLogado == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp?message=session_expired");
			return;
		}

		// Variáveis para o JSP
		Usuario usuarioParaForm = new Usuario(); 
		boolean isEditing = false;
		boolean disablePerfilField = false;
		boolean disableModuleCheckboxes = false;
		List<ModuloDTO> todosModulosDisponiveis = new ArrayList<>();
		List<String[]> todasUnidadesDisponiveis = new ArrayList<>(); 
		

		UsuarioDAO usuarioDAO = new UsuarioDAO();

		try {
		    todosModulosDisponiveis = usuarioDAO.listarModulosComId();
		    if (todosModulosDisponiveis == null) {
		        todosModulosDisponiveis = new ArrayList<>();
		    }
		} catch (SQLException e) {
		    System.err.println("Erro SQL ao listar módulos com ID no CadastrarUsuarioServlet (GET): " + e.getMessage());
		}
	
		try {
		    todasUnidadesDisponiveis = usuarioDAO.listarUnidadesDisponiveis();
		    System.out.println("DEBUG SERVLET: Quantidade de unidades encontradas no banco: " + (todasUnidadesDisponiveis != null ? todasUnidadesDisponiveis.size() : "null"));
		} catch (SQLException e) {
		    System.err.println("Erro ao listar unidades: " + e.getMessage());
		}

		String idParam = request.getParameter("id");
		if (idParam != null && !idParam.isEmpty()) { // Modo de Edição
			isEditing = true;
			try {
				int idUsuarioSendoEditado = Integer.parseInt(idParam);
				Usuario usuarioEncontrado = usuarioDAO.buscarUsuarioPorId(idUsuarioSendoEditado);

				if (usuarioEncontrado != null) {
					usuarioParaForm = usuarioEncontrado;
					request.setAttribute("title", "Editar Usuário");
					
					// -------------------------------------------------------------
				    // ADICIONE ESTE BLOCO AQUI PARA CARREGAR AS PERMISSÕES DO USUÁRIO
				    // -------------------------------------------------------------
				    try (Connection conn = Conexao.conectar()) {
				        // Carrega as unidades que este usuário já possui salvas no banco
				        List<String> unidadesDoUsuario = usuarioDAO.carregarUnidadesDoUsuario(conn, idUsuarioSendoEditado);
				        usuarioParaForm.setUnidadesPermitidas(unidadesDoUsuario);

				        // Carrega os módulos granulares que este usuário já possui salvos no banco
				        List<String> modulosDoUsuario = usuarioDAO.carregarModulosDoUsuario(conn, idUsuarioSendoEditado);
				        usuarioParaForm.setModulosPermitidos(modulosDoUsuario);
				    } catch (SQLException ex) {
				        System.err.println("Erro ao carregar unidades/módulos do usuário para edição: " + ex.getMessage());
				    }

					boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioLogado.getPerfil());
					boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioLogado.getPerfil());

					boolean isUsuarioSendoEditadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioParaForm.getPerfil());
					boolean isUsuarioSendoEditadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioParaForm.getPerfil());

					if (isUsuarioLogadoSuperAdmin) {
						if (usuarioParaForm.getId() == usuarioLogado.getId()) {
							disablePerfilField = true;
							disableModuleCheckboxes = true;
						} else {
							disablePerfilField = false;
							disableModuleCheckboxes = false;
						}
					}
					else if (isUsuarioLogadoAdmin) {
						if (isUsuarioSendoEditadoSuperAdmin || isUsuarioSendoEditadoAdmin) {
							disablePerfilField = true;
							disableModuleCheckboxes = true;
						} else { 
							if (usuarioParaForm.getId() == usuarioLogado.getId()) {
								disablePerfilField = true;
								disableModuleCheckboxes = true;
							} else {
								disablePerfilField = false;
								disableModuleCheckboxes = false;
							}
						}
					}
					else {
						if (usuarioParaForm.getId() == usuarioLogado.getId()) {
							disablePerfilField = true;
							disableModuleCheckboxes = true;
						} else {
							String encodedMessage = URLEncoder.encode(
									"Você não tem permissão para editar este usuário.",
									StandardCharsets.UTF_8.toString());
							response.sendRedirect(request.getContextPath()
									+ "/GerenciarUsuariosServlet?message=error&custom_message=" + encodedMessage);
							return;
						}
					}

				} else {
					String encodedMessage = URLEncoder.encode("Usuário não encontrado para edição.",
							StandardCharsets.UTF_8.toString());
					response.sendRedirect(request.getContextPath()
							+ "/GerenciarUsuariosServlet?message=error&custom_message=" + encodedMessage);
					return;
				}
			} catch (NumberFormatException e) {
				System.err.println("ID inválido na URL para Usuário: " + idParam + ". Detalhes: " + e.getMessage());
				String encodedMessage = URLEncoder.encode("ID de usuário inválido.", StandardCharsets.UTF_8.toString());
				response.sendRedirect(request.getContextPath()
						+ "/GerenciarUsuariosServlet?message=error&custom_message=" + encodedMessage);
				return;
			} catch (SQLException e) {
				System.err.println(
						"Erro de BD ao buscar Usuário por ID no CadastrarUsuarioServlet (GET): " + e.getMessage());
				e.printStackTrace();
				String encodedMessage = URLEncoder.encode("Erro no banco de dados ao carregar usuário.",
						StandardCharsets.UTF_8.toString());
				response.sendRedirect(request.getContextPath()
						+ "/GerenciarUsuariosServlet?message=error&custom_message=" + encodedMessage);
				return;
			}
		} else { // Modo de Cadastro
			request.setAttribute("title", "Cadastro de Usuário");
			boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
					.equalsIgnoreCase(usuarioLogado.getPerfil());
			boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
					.equalsIgnoreCase(usuarioLogado.getPerfil());

			if (isUsuarioLogadoSuperAdmin || isUsuarioLogadoAdmin) {
				disablePerfilField = false;
				disableModuleCheckboxes = false;
			} else {
				disablePerfilField = true;
				disableModuleCheckboxes = true;
			}
		}

		List<String> perfisDisponiveisParaSelecao = new ArrayList<>();
		PerfilUsuario perfilLogadoEnum = PerfilUsuario.fromString(usuarioLogado.getPerfil());

		for (PerfilUsuario perfilOption : PerfilUsuario.values()) {
			if (perfilLogadoEnum == PerfilUsuario.SUPER_ADMINISTRADOR) {
				perfisDisponiveisParaSelecao.add(perfilOption.name().toLowerCase());
			}
			else if (perfilLogadoEnum == PerfilUsuario.ADMINISTRADOR) {
				if (perfilOption != PerfilUsuario.SUPER_ADMINISTRADOR
						&& perfilOption.getHierarquia() >= perfilLogadoEnum.getHierarquia()) {
					perfisDisponiveisParaSelecao.add(perfilOption.name().toLowerCase());
				}
			}
			else {
				if (perfilOption == perfilLogadoEnum) {
					perfisDisponiveisParaSelecao.add(perfilOption.name().toLowerCase());
				}
			}
		}
		
		perfisDisponiveisParaSelecao = perfisDisponiveisParaSelecao.stream().distinct().sorted(
				(p1, p2) -> PerfilUsuario.fromString(p1).getHierarquia() - PerfilUsuario.fromString(p2).getHierarquia())
				.collect(Collectors.toList());

		request.setAttribute("usuario", usuarioParaForm);
		request.setAttribute("isEditing", isEditing);
		request.setAttribute("disablePerfilField", disablePerfilField);
		request.setAttribute("disableModuleCheckboxes", disableModuleCheckboxes);
		request.setAttribute("todosModulosDisponiveis", todosModulosDisponiveis);
		request.setAttribute("usuarioLogadoId", usuarioLogado.getId());
		request.setAttribute("perfilUsuarioLogado", usuarioLogado.getPerfil());
		request.setAttribute("perfisDisponiveisParaSelecao", perfisDisponiveisParaSelecao);

		request.setAttribute("todosModulosJson", gson.toJson(todosModulosDisponiveis));
		request.setAttribute("usuarioModulosJson", gson.toJson(usuarioParaForm.getModulosPermitidos()));
		request.setAttribute("todasUnidadesJson", gson.toJson(todasUnidadesDisponiveis));
		request.setAttribute("usuarioUnidadesJson", gson.toJson(usuarioParaForm.getUnidadesPermitidas()));
		
		// ADICIONE ESTA LINHA PARA ENVIAR O OBJETO COMPLETO DO USUÁRIO EM JSON PARA O JS:
		request.setAttribute("usuarioJson", gson.toJson(usuarioParaForm));

		request.getRequestDispatcher("/WEB-INF/jsp/cadastro-usuario.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Adicione esta validação no início para bloquear acessos negados via GET
	    if (!validarPermissao(request, response)) {
	        return;
	    }

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		JsonObject jsonResponse = new JsonObject();
		

		HttpSession session = request.getSession(false);
		Usuario usuarioLogado = null;
		if (session != null) {
			usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
		}

		if (usuarioLogado == null) {
			jsonResponse.addProperty("success", false);
			jsonResponse.addProperty("message", "Sessão expirada. Faça login novamente.");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			out.write(gson.toJson(jsonResponse));
			out.close();
			return;
		}

		boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
				.equalsIgnoreCase(usuarioLogado.getPerfil());
		boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name().equalsIgnoreCase(usuarioLogado.getPerfil());

		try {
			BufferedReader reader = request.getReader();
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			String requestBody = sb.toString();

			if (requestBody == null || requestBody.trim().isEmpty()) {
				throw new JsonSyntaxException("Corpo da requisição JSON vazio ou nulo.");
			}

			JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

			String action = jsonRequest.has("action") ? jsonRequest.get("action").getAsString() : "";
			boolean isEditing = "editar".equals(action);

			int id = jsonRequest.has("id") && !jsonRequest.get("id").getAsString().isEmpty()
					? jsonRequest.get("id").getAsInt()
					: 0;
			String username = jsonRequest.has("username") ? jsonRequest.get("username").getAsString() : "";
			String nomeCompleto = jsonRequest.has("nomeCompleto") ? jsonRequest.get("nomeCompleto").getAsString() : "";
			String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : "";
			String senha = jsonRequest.has("senha") ? jsonRequest.get("senha").getAsString() : "";
			String perfil = jsonRequest.has("perfil") ? jsonRequest.get("perfil").getAsString() : "";
			boolean ativo = jsonRequest.has("ativo") ? jsonRequest.get("ativo").getAsBoolean() : false;

			List<String> modulosPermitidos = new ArrayList<>();
			if (jsonRequest.has("modulosPermitidos")) {
				JsonArray modulosArray = jsonRequest.getAsJsonArray("modulosPermitidos");
				for (int i = 0; i < modulosArray.size(); i++) {
					if (modulosArray.get(i).isJsonObject()) {
						JsonObject modObj = modulosArray.get(i).getAsJsonObject();
						if (modObj.has("id")) {
							modulosPermitidos.add(String.valueOf(modObj.get("id").getAsInt()));
						}
					} else {
						modulosPermitidos.add(modulosArray.get(i).getAsString());
					}
				}
			}

			String unidadePadrao = jsonRequest.has("unidadePadrao") ? jsonRequest.get("unidadePadrao").getAsString() : "";
			List<String> unidadesPermitidas = new ArrayList<>();
			
			if (jsonRequest.has("unidadesPermitidas")) {
			    JsonArray unidadesArray = jsonRequest.getAsJsonArray("unidadesPermitidas");
			    
			    if (!unidadePadrao.isEmpty()) {
			        unidadesPermitidas.add(unidadePadrao);
			    }

			    for (int i = 0; i < unidadesArray.size(); i++) {
			        String idUnidade = unidadesArray.get(i).getAsString();
			        if (!idUnidade.equals(unidadePadrao)) {
			            unidadesPermitidas.add(idUnidade);
			        }
			    }
			}

			UsuarioDAO usuarioDAO = new UsuarioDAO();
			Usuario usuarioOperacao = new Usuario();
			usuarioOperacao.setUsername(username);
			usuarioOperacao.setNomeCompleto(nomeCompleto); // <-- ADICIONE ESTA LINHA AQUI
			usuarioOperacao.setEmail(email);
			usuarioOperacao.setSenha(senha);
			usuarioOperacao.setPerfil(perfil);
			usuarioOperacao.setAtivo(ativo);
			usuarioOperacao.setModulosPermitidos(modulosPermitidos);
			usuarioOperacao.setUnidadesPermitidas(unidadesPermitidas);
			
			// CORREÇÃO AQUI: Convertendo a String do JSON para Integer compatível com o Model
			if (unidadePadrao != null && !unidadePadrao.trim().isEmpty()) {
			    try {
			        usuarioOperacao.setUnidadeAtivaId(Integer.parseInt(unidadePadrao));
			    } catch (NumberFormatException e) {
			        usuarioOperacao.setUnidadeAtivaId(null);
			    }
			} else {
			    usuarioOperacao.setUnidadeAtivaId(null);
			}
			
			usuarioOperacao.setId(id);

			if (username.trim().isEmpty() || email.trim().isEmpty() || perfil.trim().isEmpty()) {
				jsonResponse.addProperty("success", false);
				jsonResponse.addProperty("message",
						"Por favor, preencha todos os campos obrigatórios (Usuário, Email, Perfil).");
				return;
			}
			
			if (unidadePadrao == null || unidadePadrao.trim().isEmpty()) {
			    jsonResponse.addProperty("success", false);
			    jsonResponse.addProperty("message", "Por favor, selecione a Unidade Principal do usuário.");
			    return;
			}

			if ("cadastrar".equals(action)) {

				if (username.equalsIgnoreCase(senha)) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "A senha não pode ser idêntica ao nome de usuário (login).");
					return;
				}

				if (senha.isEmpty()) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "A senha é obrigatória para novos cadastros.");
					return;
				}
				if (senha.length() < 6) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "A senha deve ter pelo menos 6 caracteres.");
					return;
				}

				if (!isUsuarioLogadoSuperAdmin && !isUsuarioLogadoAdmin) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Você não tem permissão para cadastrar novos usuários.");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				if (!isUsuarioLogadoSuperAdmin) {
					if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil)) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Você não tem permissão para criar usuários com perfil de Super Administrador.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				if (usuarioDAO.buscarUsuarioPorUsername(username) != null) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Nome de usuário já existe.");
					return;
				}
				if (usuarioDAO.buscarUsuarioPorEmail(email) != null) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "E-mail já cadastrado.");
					return;
				}
				// action == "cadastrar"
				if (usuarioDAO.cadastrarUsuario(usuarioOperacao)) {
				    int idNovoUsuario = usuarioOperacao.getId(); 
				    
				    if (idNovoUsuario > 0) {
				        try (Connection conn = Conexao.conectar()) {
				            if (jsonRequest.has("modulosPermitidos")) {
				                JsonArray modulosArray = jsonRequest.getAsJsonArray("modulosPermitidos");
				                // CORREÇÃO AQUI: Chamar o método correto para JsonArray
				                usuarioDAO.salvarModulosGranularesDoUsuario(conn, idNovoUsuario, modulosArray);
				            }
				            usuarioDAO.salvarUnidadesDoUsuario(conn, idNovoUsuario, unidadesPermitidas);
				        } catch (SQLException ex) {
				            System.err.println("Erro ao gravar permissões granulares no pós-cadastro: " + ex.getMessage());
				        }
				    }

				    jsonResponse.addProperty("success", true);
				    jsonResponse.addProperty("message", "Usuário cadastrado com sucesso!");
				} else {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Falha ao cadastrar usuário. Verifique os dados.");
				}

			} else if (isEditing) {

				if (!senha.isEmpty() && username.equalsIgnoreCase(senha)) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message",
							"A nova senha não pode ser idêntica ao nome de usuário (login).");
					return;
				}

				if (!senha.isEmpty() && senha.length() < 6) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "A nova senha deve ter pelo menos 6 caracteres.");
					return;
				}
				if (senha.isEmpty()) {
					usuarioOperacao.setSenha(null); 
				}

				Usuario usuarioOriginal = usuarioDAO.buscarUsuarioPorId(id);
				if (usuarioOriginal == null) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Usuário a ser editado não encontrado.");
					return;
				}

				boolean isEditingSelf = (usuarioOriginal.getId() == usuarioLogado.getId());
				boolean isUsuarioSendoEditadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
						.equalsIgnoreCase(usuarioOriginal.getPerfil());
				boolean isUsuarioSendoEditadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
						.equalsIgnoreCase(usuarioOriginal.getPerfil());

				boolean canEdit = false;
				if (isUsuarioLogadoSuperAdmin) {
					canEdit = true; 
				} else if (isUsuarioLogadoAdmin) {
					if (isUsuarioSendoEditadoSuperAdmin) {
						canEdit = false; 
					} else {
						canEdit = true; 
					}
				} else { 
					if (isEditingSelf) {
						canEdit = true; 
					} else {
						canEdit = false; 
					}
				}

				if (!canEdit) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Você não tem permissão para editar este usuário.");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				if (!isUsuarioLogadoSuperAdmin) {
					if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil)
							&& !isUsuarioSendoEditadoSuperAdmin) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Você não tem permissão para definir o perfil como Super Administrador.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					if (isUsuarioLogadoAdmin && isUsuarioSendoEditadoSuperAdmin
							&& !PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil)) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Administradores não podem alterar o perfil de um Super Administrador.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				if (isUsuarioLogadoAdmin && (isUsuarioSendoEditadoSuperAdmin || isUsuarioSendoEditadoAdmin)) {
					if (!perfil.equals(usuarioOriginal.getPerfil())
							|| !modulosPermitidos.equals(usuarioOriginal.getModulosPermitidos())) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Administradores não podem alterar o perfil ou módulos de outros Administradores ou Super Administradores.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				if ((!isUsuarioLogadoAdmin && !isUsuarioLogadoSuperAdmin && isEditingSelf)
						|| (isUsuarioLogadoAdmin && isEditingSelf)) {
					if (!perfil.equals(usuarioOriginal.getPerfil())) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message", "Você não tem permissão para alterar seu próprio perfil.");
						return;
					}
					if (!modulosPermitidos.equals(usuarioOriginal.getModulosPermitidos())) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Você não tem permissão para alterar seus próprios módulos.");
						return;
					}
				}

				Usuario existingUsernameUser = usuarioDAO.buscarUsuarioPorUsername(username);
				if (existingUsernameUser != null && existingUsernameUser.getId() != id) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Nome de usuário já existe para outro usuário.");
					return;
				}
				Usuario existingEmailUser = usuarioDAO.buscarUsuarioPorEmail(email);
				if (existingEmailUser != null && existingEmailUser.getId() != id) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "E-mail já cadastrado para outro usuário.");
					return;
				}

				if (usuarioDAO.atualizarUsuario(usuarioOperacao)) {
				    try (Connection conn = Conexao.conectar()) {
				        if (jsonRequest.has("modulosPermitidos")) {
				            JsonArray modulosArray = jsonRequest.getAsJsonArray("modulosPermitidos");
				            // CORREÇÃO AQUI: Chamar o método correto para JsonArray
				            usuarioDAO.salvarModulosGranularesDoUsuario(conn, id, modulosArray);
				        }
				        usuarioDAO.salvarUnidadesDoUsuario(conn, id, unidadesPermitidas);
				    } catch (SQLException ex) {
				        System.err.println("Erro ao atualizar permissões granulares na edição: " + ex.getMessage());
				    }

				    jsonResponse.addProperty("success", true);
				    jsonResponse.addProperty("message", "Usuário atualizado com sucesso!");
				    
					if (isEditingSelf) {
						Usuario usuarioAtualizadoNaSessao = usuarioDAO.buscarUsuarioPorId(usuarioOperacao.getId());
						session.setAttribute("usuarioLogado", usuarioAtualizadoNaSessao);
						session.setAttribute("perfilUsuarioLogado", usuarioAtualizadoNaSessao.getPerfil()); 
					}
				} else {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message",
							"Falha ao atualizar usuário. Nenhuma alteração detectada ou erro no banco de dados.");
				}
			} else {
				jsonResponse.addProperty("success", false);
				jsonResponse.addProperty("message", "Ação inválida.");
			}

		} catch (JsonSyntaxException e) {
			System.err.println("Erro de sintaxe JSON no CadastrarUsuarioServlet (POST): " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			jsonResponse.addProperty("success", false);
			jsonResponse.addProperty("message",
					"Erro de formato de dados: JSON inválido. Verifique o console do navegador para detalhes.");
		} catch (SQLException e) {
			System.err.println("Erro SQL no CadastrarUsuarioServlet (POST): " + e.getMessage());
			e.printStackTrace();
			jsonResponse.addProperty("success", false);
			jsonResponse.addProperty("message", "Erro no banco de dados ao processar usuário.");
		} catch (Exception e) {
			System.err.println("Erro inesperado no CadastrarUsuarioServlet (POST): " + e.getMessage());
			e.printStackTrace();
			jsonResponse.addProperty("success", false);
			jsonResponse.addProperty("message", "Ocorreu um erro inesperado ao processar usuário.");
		} finally {
			out.write(gson.toJson(jsonResponse));
			out.close();
		}
	}
}
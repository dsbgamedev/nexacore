package controller;

import dao.UsuarioDAO;
import model.Usuario;
import model.enums.PerfilUsuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet("/CadastrarUsuarioServlet")
public class CadastrarUsuarioServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Gson gson = new Gson();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("DEBUG SERVLET: CadastrarUsuarioServlet - Método doGet chamado.");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

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
		Usuario usuarioParaForm = new Usuario(); // Objeto padrão para novo cadastro
		boolean isEditing = false;
		boolean disablePerfilField = false;
		boolean disableModuleCheckboxes = false;
		List<String> todosModulosDisponiveis = new ArrayList<>(); // Inicializa vazia
		List<String[]> todasUnidadesDisponiveis = new ArrayList<>(); // Usaremos String[] para guardar [id, nome]
		

		UsuarioDAO usuarioDAO = new UsuarioDAO();

		try {
			// Carrega todos os módulos disponíveis do DAO
			todosModulosDisponiveis = usuarioDAO.listarNomesModulos();
			if (todosModulosDisponiveis == null) {
				todosModulosDisponiveis = new ArrayList<>();
			}
		} catch (SQLException e) {
			System.err
					.println("Erro SQL ao listar nomes de módulos no CadastrarUsuarioServlet (GET): " + e.getMessage());
		}
	
		
		try {
		    // Você precisará criar este método no UsuarioDAO ou usar um UnidadeDAO
		    // Por enquanto, vamos assumir que o UsuarioDAO pode listar as unidades
		    todasUnidadesDisponiveis = usuarioDAO.listarUnidadesDisponiveis();
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

					// Lógica de permissão para edição de campos
					boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioLogado.getPerfil());
					boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioLogado.getPerfil());

					boolean isUsuarioSendoEditadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioParaForm.getPerfil());
					boolean isUsuarioSendoEditadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
							.equalsIgnoreCase(usuarioParaForm.getPerfil());

					// Se o usuário logado é Super Admin
					if (isUsuarioLogadoSuperAdmin) {
						// Super Admin editando a si mesmo: não pode mudar perfil ou módulos
						if (usuarioParaForm.getId() == usuarioLogado.getId()) {
							disablePerfilField = true;
							disableModuleCheckboxes = true;
						} else {
							// Super Admin editando outro: pode mudar tudo
							disablePerfilField = false;
							disableModuleCheckboxes = false;
						}
					}
					// Se o usuário logado é Administrador
					else if (isUsuarioLogadoAdmin) {
						// Admin não pode editar perfil ou módulos de Super Admin ou outro Admin
						if (isUsuarioSendoEditadoSuperAdmin || isUsuarioSendoEditadoAdmin) {
							disablePerfilField = true;
							disableModuleCheckboxes = true;
						} else { // Admin editando Gerente, Técnico, Usuário
							// Admin editando a si mesmo: não pode mudar o próprio perfil ou módulos
							if (usuarioParaForm.getId() == usuarioLogado.getId()) {
								disablePerfilField = true;
								disableModuleCheckboxes = true;
							} else {
								// Admin pode editar perfil e módulos de outros (Gerente, Técnico, Usuário)
								disablePerfilField = false;
								disableModuleCheckboxes = false;
							}
						}
					}
					// Se o usuário logado é Gerente, Técnico ou Usuário
					else {
						// Só pode editar a si mesmo, e não pode mudar perfil ou módulos
						if (usuarioParaForm.getId() == usuarioLogado.getId()) {
							disablePerfilField = true;
							disableModuleCheckboxes = true;
						} else {
							// Se não for editando a si mesmo, não tem permissão para editar este usuário
							String encodedMessage = URLEncoder.encode(
									"Você não tem permissão para editar este usuário.",
									StandardCharsets.UTF_8.toString());
							response.sendRedirect(request.getContextPath()
									+ "/GerenciarUsuariosServlet?message=error&custom_message=" + encodedMessage);
							return;
						}
					}

				} else {
					// Usuário não encontrado para edição
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
		} else { // Modo de Cadastro (não edição)
			request.setAttribute("title", "Cadastro de Usuário");
			// Lógica para desabilitar campos no modo de cadastro
			boolean isUsuarioLogadoSuperAdmin = PerfilUsuario.SUPER_ADMINISTRADOR.name()
					.equalsIgnoreCase(usuarioLogado.getPerfil());
			boolean isUsuarioLogadoAdmin = PerfilUsuario.ADMINISTRADOR.name()
					.equalsIgnoreCase(usuarioLogado.getPerfil());

			if (isUsuarioLogadoSuperAdmin || isUsuarioLogadoAdmin) {
				disablePerfilField = false;
				disableModuleCheckboxes = false;
			} else {
				// Gerente, Técnico, Usuário não podem definir perfil ou módulos no cadastro
				disablePerfilField = true;
				disableModuleCheckboxes = true;
			}
		}

		// Filtrar perfis disponíveis para o select com base no usuário logado
		List<String> perfisDisponiveisParaSelecao = new ArrayList<>();
		PerfilUsuario perfilLogadoEnum = PerfilUsuario.fromString(usuarioLogado.getPerfil());

		for (PerfilUsuario perfilOption : PerfilUsuario.values()) {
			// Super Admin pode selecionar qualquer perfil
			if (perfilLogadoEnum == PerfilUsuario.SUPER_ADMINISTRADOR) {
				perfisDisponiveisParaSelecao.add(perfilOption.name().toLowerCase());
			}
			// Admin pode selecionar perfis do mesmo nível ou inferiores, mas não Super
			// Admin
			else if (perfilLogadoEnum == PerfilUsuario.ADMINISTRADOR) {
				if (perfilOption != PerfilUsuario.SUPER_ADMINISTRADOR
						&& perfilOption.getHierarquia() >= perfilLogadoEnum.getHierarquia()) {
					perfisDisponiveisParaSelecao.add(perfilOption.name().toLowerCase());
				}
			}
			// Outros perfis (Gerente, Técnico, Usuário) só podem selecionar o próprio
			// perfil
			else {
				if (perfilOption == perfilLogadoEnum) {
					perfisDisponiveisParaSelecao.add(perfilOption.name().toLowerCase());
				}
			}
		}
		// Garante que a lista de perfis seja única e ordenada pela hierarquia
		perfisDisponiveisParaSelecao = perfisDisponiveisParaSelecao.stream().distinct().sorted(
				(p1, p2) -> PerfilUsuario.fromString(p1).getHierarquia() - PerfilUsuario.fromString(p2).getHierarquia())
				.collect(Collectors.toList());

		// Passar todos os dados e flags para o JSP
		request.setAttribute("usuario", usuarioParaForm);
		request.setAttribute("isEditing", isEditing);
		request.setAttribute("disablePerfilField", disablePerfilField);
		request.setAttribute("disableModuleCheckboxes", disableModuleCheckboxes);
		request.setAttribute("todosModulosDisponiveis", todosModulosDisponiveis); // Lista de Strings
		request.setAttribute("usuarioLogadoId", usuarioLogado.getId()); // Passa para o data-attribute do JS
		request.setAttribute("perfilUsuarioLogado", usuarioLogado.getPerfil()); // Passa para o data-attribute do JS
		request.setAttribute("perfisDisponiveisParaSelecao", perfisDisponiveisParaSelecao);

		// Serializa as listas de módulos para JSON para serem lidas pelo JavaScript via
		// data-attributes
		request.setAttribute("todosModulosJson", gson.toJson(todosModulosDisponiveis));
		request.setAttribute("usuarioModulosJson", gson.toJson(usuarioParaForm.getModulosPermitidos()));
		
		// AQUI ESTAVA O ERRO: Use os nomes que o JSP espera no bloco <script>
		request.setAttribute("todasUnidadesJson", gson.toJson(todasUnidadesDisponiveis)); // Agora em JSON
		request.setAttribute("usuarioUnidadesJson", gson.toJson(usuarioParaForm.getUnidadesPermitidas()));// NOVO 04.03.2026

		//request.setAttribute("perfisDisponiveisParaSelecao", perfisDisponiveisParaSelecao); // Passa a lista de perfis

		System.out.println("DEBUG SERVLET: Módulos disponíveis para o JSP (Lista Java): " + todosModulosDisponiveis);
		System.out.println("DEBUG SERVLET: Módulos do usuário (Lista Java): " + usuarioParaForm.getModulosPermitidos());

		// CORREÇÃO AQUI: Altera o nome do JSP para o nome correto do seu arquivo
		request.getRequestDispatcher("/cadastroUsuario.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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
			// Não precisa de out.close() aqui, pois o fluxo vai para o finally de qualquer
			// forma se não for um return limpo
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
			// System.out.println("DEBUG SERVLET: JSON recebido no doPost: " + requestBody);
			// // Comentado para não poluir o output

			if (requestBody == null || requestBody.trim().isEmpty()) {
				throw new JsonSyntaxException("Corpo da requisição JSON vazio ou nulo.");
			}

			JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

			String action = jsonRequest.get("action").getAsString();
			boolean isEditing = "editar".equals(action);

			int id = jsonRequest.has("id") && !jsonRequest.get("id").getAsString().isEmpty()
					? jsonRequest.get("id").getAsInt()
					: 0;
			String username = jsonRequest.get("username").getAsString();
			String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : "";
			String senha = jsonRequest.has("senha") ? jsonRequest.get("senha").getAsString() : "";
			String perfil = jsonRequest.has("perfil") ? jsonRequest.get("perfil").getAsString() : "";
			boolean ativo = jsonRequest.has("ativo") ? jsonRequest.get("ativo").getAsBoolean() : false;

			List<String> modulosPermitidos = new ArrayList<>();
			if (jsonRequest.has("modulosPermitidos")) {
				JsonArray modulosArray = jsonRequest.getAsJsonArray("modulosPermitidos");
				for (int i = 0; i < modulosArray.size(); i++) {
					modulosPermitidos.add(modulosArray.get(i).getAsString());
				}
			}
			// NOVO 04.03.2026
			// --- NOVO: Captura da Unidade Principal e Unidades Permitidas ---
			String unidadePadrao = jsonRequest.has("unidadePadrao") ? jsonRequest.get("unidadePadrao").getAsString() : "";
			List<String> unidadesPermitidas = new ArrayList<>();
			
			if (jsonRequest.has("unidadesPermitidas")) {
			    JsonArray unidadesArray = jsonRequest.getAsJsonArray("unidadesPermitidas");
			    
			    // Primeiro, adicionamos a Unidade Principal no topo da lista (Index 0)
			    if (!unidadePadrao.isEmpty()) {
			        unidadesPermitidas.add(unidadePadrao);
			    }

			    // Depois, adicionamos as outras, garantindo que não duplicamos a principal
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
			usuarioOperacao.setEmail(email);
			usuarioOperacao.setSenha(senha);
			usuarioOperacao.setPerfil(perfil);
			usuarioOperacao.setAtivo(ativo);
			usuarioOperacao.setModulosPermitidos(modulosPermitidos); // Define os módulos
			usuarioOperacao.setUnidadesPermitidas(unidadesPermitidas); // Define as unidades
			usuarioOperacao.setId(id);

			if (username.trim().isEmpty() || email.trim().isEmpty() || perfil.trim().isEmpty()) {
				jsonResponse.addProperty("success", false);
				jsonResponse.addProperty("message",
						"Por favor, preencha todos os campos obrigatórios (Usuário, Email, Perfil).");
				// Note: O out.write final no bloco finally cuidará disso, se este não fosse um
				// retorno imediato.
				// Como é um retorno imediato, é melhor deixar a escrita para o finally.
				// Removido out.write() daqui.
				return;
			}
			
			// <<< ADICIONE AQUI >>>
			if (unidadePadrao == null || unidadePadrao.trim().isEmpty()) {
			    jsonResponse.addProperty("success", false);
			    jsonResponse.addProperty("message", "Por favor, selecione a Unidade Principal do usuário.");
			    return;
			}

			if ("cadastrar".equals(action)) {

				// --- REGRA DE SEGURANÇA (BACKEND - Cadastro) ---
				if (username.equalsIgnoreCase(senha)) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "A senha não pode ser idêntica ao nome de usuário (login).");
					return;
				}
				// --- FIM REGRA DE SEGURANÇA ---

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

				// Permissao para cadastrar
				if (!isUsuarioLogadoSuperAdmin && !isUsuarioLogadoAdmin) { // Apenas Super Admin ou Admin podem
																			// cadastrar
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Você não tem permissão para cadastrar novos usuários.");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				if (!isUsuarioLogadoSuperAdmin) { // Apenas Super Admin pode cadastrar Super Administrador
					if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil)) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Você não tem permissão para criar usuários com perfil de Super Administrador.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				// --- VALIDAÇÃO DE UNICIDADE (Cadastro) ---
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
				// --- FIM VALIDAÇÃO DE UNICIDADE ---

				if (usuarioDAO.cadastrarUsuario(usuarioOperacao)) {
					jsonResponse.addProperty("success", true);
					jsonResponse.addProperty("message", "Usuário cadastrado com sucesso!");
				} else {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Falha ao cadastrar usuário. Verifique os dados.");
				}

			} else if (isEditing) {

				// --- REGRA DE SEGURANÇA (BACKEND - Edição) ---
				if (!senha.isEmpty() && username.equalsIgnoreCase(senha)) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message",
							"A nova senha não pode ser idêntica ao nome de usuário (login).");
					return;
				}
				// --- FIM REGRA DE SEGURANÇA ---

				if (!senha.isEmpty() && senha.length() < 6) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "A nova senha deve ter pelo menos 6 caracteres.");
					return;
				}
				if (senha.isEmpty()) {
					usuarioOperacao.setSenha(null); // Não altera a senha se o campo estiver vazio
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
					canEdit = true; // Super Admin pode editar qualquer um
				} else if (isUsuarioLogadoAdmin) {
					if (isUsuarioSendoEditadoSuperAdmin) {
						canEdit = false; // Admin não pode editar Super Admin
					} else {
						canEdit = true; // Admin pode editar outros Admins, Gerentes, Tecnicos, Usuarios
					}
				} else { // Gerente, Técnico, Usuário
					if (isEditingSelf) {
						canEdit = true; // Só pode editar a si mesmo
					} else {
						canEdit = false; // Não pode editar outros
					}
				}

				if (!canEdit) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Você não tem permissão para editar este usuário.");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				// Validações de perfil e módulos ao editar
				if (!isUsuarioLogadoSuperAdmin) {
					// Ninguém além do Super Admin pode alterar o perfil para Super Admin
					if (PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil)
							&& !isUsuarioSendoEditadoSuperAdmin) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Você não tem permissão para definir o perfil como Super Administrador.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					// Impedir que Admin altere o perfil de um Super Admin
					if (isUsuarioLogadoAdmin && isUsuarioSendoEditadoSuperAdmin
							&& !PerfilUsuario.SUPER_ADMINISTRADOR.name().equalsIgnoreCase(perfil)) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Administradores não podem alterar o perfil de um Super Administrador.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				// Regra para Administrador editando outro Administrador (ou Super Admin)
				// Se o usuário logado é um Admin e está editando um Super Admin ou outro Admin,
				// ele não pode alterar o perfil nem os módulos.
				if (isUsuarioLogadoAdmin && (isUsuarioSendoEditadoSuperAdmin || isUsuarioSendoEditadoAdmin)) {
					// Se o perfil enviado for diferente do perfil original, ou se os módulos forem
					// diferentes
					// e o usuário logado não for Super Admin, então nega a operação.
					if (!perfil.equals(usuarioOriginal.getPerfil())
							|| !modulosPermitidos.equals(usuarioOriginal.getModulosPermitidos())) {
						jsonResponse.addProperty("success", false);
						jsonResponse.addProperty("message",
								"Administradores não podem alterar o perfil ou módulos de outros Administradores ou Super Administradores.");
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				// Usuário comum (não Super Admin/Admin) não pode alterar o próprio perfil ou
				// módulos
				// Admin também não pode alterar o próprio perfil ou módulos (se editando a si
				// mesmo)
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

				// --- VALIDAÇÃO DE UNICIDADE (Edição) ---
				// Verificação de unicidade de username e email ao editar (excluindo o próprio
				// usuário)
				Usuario existingUsernameUser = usuarioDAO.buscarUsuarioPorUsername(username);
				if (existingUsernameUser != null && existingUsernameUser.getId() != id) {
					jsonResponse.addProperty("success", false);
					jsonResponse.addProperty("message", "Nome de usuário já existe para outro usuário.");
					return;
				}
				Usuario existingEmailUser = usuarioDAO.buscarUsuarioPorEmail(email);
				if (existingEmailUser != null && existingEmailUser.getId() != id) {
					jsonResponse.addProperty("success", false);
					// MENSAGEM CLARA DE EMAIL DUPLICADO:
					jsonResponse.addProperty("message", "E-mail já cadastrado para outro usuário.");
					return;
				}
				// --- FIM VALIDAÇÃO DE UNICIDADE ---

				if (usuarioDAO.atualizarUsuario(usuarioOperacao)) {
					jsonResponse.addProperty("success", true);
					jsonResponse.addProperty("message", "Usuário atualizado com sucesso!");
					// Se o próprio usuário logado foi editado, atualiza o objeto na sessão
					if (isEditingSelf) {
						Usuario usuarioAtualizadoNaSessao = usuarioDAO.buscarUsuarioPorId(usuarioOperacao.getId());
						session.setAttribute("usuarioLogado", usuarioAtualizadoNaSessao);
						session.setAttribute("perfilUsuarioLogado", usuarioAtualizadoNaSessao.getPerfil()); // Atualiza
																											// o perfil
																											// na sessão
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
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
			// CORREÇÃO CRÍTICA: Garantir que apenas o JSON seja escrito e o stream fechado.
			out.write(gson.toJson(jsonResponse));
			out.close(); // Fecha o PrintWriter para evitar poluição do stream e resolver o SyntaxError
							// no JS.
		}
	}
}
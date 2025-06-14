package br.com.odontoprime.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.exception.ConstraintViolationException;

import br.com.odontoprime.dao.UsuarioDAO;
import br.com.odontoprime.entidade.Usuario;
import br.com.odontoprime.util.MensagemUtil;

@SuppressWarnings("serial")
public class UsuarioService implements Serializable {
	@Inject
	private UsuarioDAO usuarioDAO;
	private HttpSession sessao;

	public void criarSessao() {
		sessao = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	}

	public Usuario autenticar(Usuario usuario) {
		try {

			usuario = usuarioDAO.buscarUsuarioBD(usuario);

			if (usuario == null) {
				usuario = new Usuario();
				MensagemUtil.enviarMensagem("Login ou senha inválidos!", FacesMessage.SEVERITY_ERROR);
				return usuario;
			}

			criarSessao();
			sessao.setAttribute("usuario", usuario);

			// atribui a data de ultimo acesso capturada do banco de dados
			usuario.setExibirDataUltimoLogin(usuario.getDataUltimoLogin());
			// pega a data atual do sistema para registrar o possível novo
			// ultimo acesso
			usuario.setDataUltimoLogin(Calendar.getInstance().getTime());

			return usuario;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}

	public String registrarSaida(Usuario usuario) {
		// atualiza a data de ultimo acesso
		usuarioDAO.atualizar(usuario);

		sessao.invalidate();
		return "/Login.xhtml?faces-redirect=true";
	}

	public void salvar(Usuario usuario) {
		try {
			// verifica se o usuário possui id para atualiza
			boolean editavel = isUsuarioEditavel(usuario);
			
			validarSenha(usuario);
			usuarioDAO.salvar(usuario);

			// diferenciar mensagem
			if (editavel)
				MensagemUtil.enviarMensagem("Usuário atualizado com sucesso!", FacesMessage.SEVERITY_INFO);
			else
				MensagemUtil.enviarMensagem("Usuário cadastrado com sucesso!", FacesMessage.SEVERITY_INFO);

		} catch (Exception ex) {

			Throwable t = ex.getCause();
			if (t instanceof ConstraintViolationException) {
				String msgErro = t.getCause().getMessage();
				if (msgErro.contains(usuario.getLogin())) {
					MensagemUtil.enviarMensagem("Login já cadastrado. Informe outro.", FacesMessage.SEVERITY_ERROR);
				}
			}
			MensagemUtil.enviarMensagem(ex.getMessage(),
					FacesMessage.SEVERITY_ERROR);
			ex.printStackTrace();
		}
	}

	public List<Usuario> buscarTodos() {
		return usuarioDAO.buscarTodos(Usuario.class);
	}

	public boolean isUsuarioCadastravel(Usuario usuario) {
		if (isNovoUsuario(usuario) && usuarioDAO.buscarUsuarioPorLogin(usuario.getLogin())) {
			MensagemUtil.enviarMensagem("Login já cadastrado. Informe outro.", FacesMessage.SEVERITY_ERROR);
			return false;
		}
		return true;
	}

	public boolean isUsuarioEditavel(Usuario usuario) {
		return isUsuarioNotNull(usuario) && usuario.getId() != null && usuario.getId() > 0;
	}

	public boolean isNovoUsuario(Usuario usuario) {
		return isUsuarioNotNull(usuario) && (usuario.getId() == null || usuario.getId() == 0);
	}

	public Usuario recuperarSenha(Usuario usuario) {

		usuario = usuarioDAO.recuperarSenha(usuario);

		if (usuario == null) {
			MensagemUtil.enviarMensagem("Dados inválidos.", FacesMessage.SEVERITY_ERROR);
		}
		return usuario;
	}

	public boolean isUsuarioNotNull(Usuario usuario) {
		return usuario != null;
	}

	public void validarSenha(Usuario usuario) throws Exception {
		if (!usuario.getSenha().equalsIgnoreCase(usuario.getConfirmaSenha())) {
			 throw new Exception("Senha incorreta! Informe a senha corretamente.");
		}
	}

	public void excluir(Usuario usuario) {
		try {
			this.usuarioDAO.remover(usuario);
			MensagemUtil.enviarMensagem("Usuario removido com sucesso!", FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			MensagemUtil.enviarMensagem("Erro ao remover usuário!", FacesMessage.SEVERITY_ERROR);
			e.printStackTrace();
		}
	}
}

package br.com.odontoprime.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.CroppedImage;

import br.com.odontoprime.entidade.Usuario;
import br.com.odontoprime.service.UserImagemService;
import br.com.odontoprime.service.UsuarioService;
import br.com.odontoprime.util.MensagemUtil;

@Named
@SessionScoped
public class UsuarioMB implements Serializable {

	private static final long serialVersionUID = 8797883040852517512L;

	@Inject
	private UsuarioService usuarioService;
	private Usuario usuario;
	private CroppedImage croppedImage;
	@Inject
	private UserImagemService imagemService;
	private boolean exibirImagemCropper = Boolean.FALSE;
	private boolean exibirImagemRecortada = Boolean.FALSE;;
	private boolean exibirImagemPerfil = Boolean.FALSE;
	private String senhaVerificacao;
	private boolean exibirWebCam;

	@PostConstruct
	public void initi() {
		usuario = new Usuario();
		exibirImagemCropper = Boolean.FALSE;

	}

	public boolean isExibirWebCam() {
		return exibirWebCam;
	}

	public void setExibirWebCam(boolean exibirWebCam) {
		this.exibirWebCam = exibirWebCam;
	}

	public String getSenhaVerificacao() {
		return senhaVerificacao;
	}

	public void setSenhaVerificacao(String senhaVerificacao) {
		this.senhaVerificacao = senhaVerificacao;
	}

	public boolean isExibirImagemPerfil() {
		return exibirImagemPerfil;
	}

	public boolean isExibirImagemCropper() {
		return exibirImagemCropper;
	}

	public boolean isExibirImagemRecortada() {
		return exibirImagemRecortada;
	}

	public CroppedImage getCroppedImage() {
		if (croppedImage == null) {
			croppedImage = new CroppedImage();
		}
		return croppedImage;
	}

	public void setCroppedImage(CroppedImage croppedImage) {
		this.croppedImage = croppedImage;
	}

	public Usuario getUsuario() {
		if (usuario == null) {
			usuario = new Usuario();
		}
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String autenticar() {
		usuario = usuarioService.autenticar(usuario);
		if (usuarioEditavel())
			return "/sistema/Inicio?faces-redirect=true";
		else {
			return null;
		}
	}

	public void salvar() {
		usuarioService.salvar(usuario);
	}

	public String registrarSaida() {
		return usuarioService.registrarSaida(usuario);
	}

//	public StreamedContent getStreamedContent() {
//		FacesContext context = FacesContext.getCurrentInstance();
//
//		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
//			// So, we're rendering the view. Return a stub StreamedContent so
//			// that it will generate right URL.
//			return new DefaultStreamedContent();
//		} else {
//			// So, browser is requesting the image. Get ID value from actual
//			// request param.
//			return new DefaultStreamedContent(new ByteArrayInputStream(usuario.getByteFoto()));
//		}
//	}

	public void limpar() {

	}

	public void salvarImagem(FileUploadEvent event) {
		exibirImagemCropper = imagemService.subirImagem(usuario, event.getFile().getContent());
	}

	public void esconderImagens() {
		exibirImagemCropper = Boolean.FALSE;
		exibirImagemRecortada = Boolean.FALSE;
	}

	public void recortarImagem() {
		exibirImagemRecortada = imagemService.recortarImagem(usuario, croppedImage);
	}

	public void salvarImagemRecortada() {
		exibirImagemPerfil = imagemService.salvarImagemRecortada(croppedImage, usuario);
	}

	public void resetarValores() {
		usuario = new Usuario();
	}

	public void recuperarSenha() {
		usuario = usuarioService.recuperarSenha(usuario);
		if (isUsuarioEditavel()) {
			PrimeFaces.current().executeScript("PF('dlgRecuperarSenha').hide();");
			PrimeFaces.current().executeScript("PF('dlgResetarSenha').show();");

		}
	}

	public boolean isUsuarioEditavel() {
		return this.usuario != null && this.usuario.getId() != null && this.usuario.getId() > 0;
	}

	public boolean verificarSenha() {
		if (!usuario.getSenha().equals(senhaVerificacao)) {
			MensagemUtil.enviarMensagem("Senha diferentes! Confirme novamente.", FacesMessage.SEVERITY_ERROR);
			setSenhaVerificacao("");
			usuario.setSenha("");
			return Boolean.FALSE;

		}

		return Boolean.TRUE;
	}

	public void limparImagemCropper() {
		exibirImagemCropper = Boolean.FALSE;
		exibirImagemRecortada = Boolean.FALSE;
	}

	public void mudarSenha() {
		if (verificarSenha()) {
			salvar();
		}
	}

	public boolean usuarioIsNull() {
		return this.usuario == null;
	}

	public boolean usuarioNotNull() {
		return !usuarioIsNull();
	}

	public boolean usuarioEditavel() {
		return usuarioNotNull() && usuario.getId() != null && usuario.getId() > 0;
	}

	public void tirarFoto(CaptureEvent captureEvent) {
		exibirImagemCropper = imagemService.tirarFotoWebCam(captureEvent.getData(), usuario);
	}

}

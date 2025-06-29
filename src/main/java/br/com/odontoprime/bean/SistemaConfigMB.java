package br.com.odontoprime.bean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.odontoprime.dao.SistemaConfigDAO;
import br.com.odontoprime.entidade.SistemaConfig;
import br.com.odontoprime.util.MensagemUtil;

@Named
@ApplicationScoped
public class SistemaConfigMB {
	@Inject
	private SistemaConfigDAO sistemaConfigDAO;
	private SistemaConfig sistemaConfig;
	private String corSelecionada;
	private String corSelecionadaBody;
	private String corTitulo;

	public String getCorTitulo() {
		return corTitulo;
	}

	public void setCorTitulo(String corTitulo) {
		this.corTitulo = corTitulo;
	}

	public String getCorSelecionadaBody() {
		return corSelecionadaBody;
	}

	public void setCorSelecionadaBody(String corSelecionadaBody) {
		this.corSelecionadaBody = corSelecionadaBody;
	}

	public String getCorSelecionada() {
		return corSelecionada;
	}

	public void setCorSelecionada(String corSelecionada) {
		this.corSelecionada = corSelecionada;
	}

	public SistemaConfig getSistemaConfig() {
		return sistemaConfig;
	}

	@PostConstruct
	public void init() {
		this.sistemaConfig = this.load();
		this.corSelecionada = "";
	}

	public void setSistemaConfig(SistemaConfig sistemaConfig) {
		this.sistemaConfig = sistemaConfig;
	}

	public void salvarCorMenu() {
		SistemaConfig config = sistemaConfigDAO.buscarPorId(1L, SistemaConfig.class);
		if (config == null) {
			config = new SistemaConfig();
			config.setId(1L); // opcional, se estiver usando ID manual
		}
		config.setCorMenu(sistemaConfig.getCorMenu());
		sistemaConfigDAO.salvar(config);
		System.out.println("salvando cor do menu...");
	}

	public SistemaConfig load() {
		sistemaConfig = sistemaConfigDAO.buscarPorId(1L, SistemaConfig.class);
		if (sistemaConfig == null) {
			sistemaConfig = new SistemaConfig();
		}
		return sistemaConfig;
	}

	public void aplicarCorSelecionada() {
		sistemaConfig.setCorMenu(corSelecionada);
		salvarCorMenu(); // chama seu método existente de persistência
		MensagemUtil.enviarMensagem("Cor definida com sucesso!", FacesMessage.SEVERITY_INFO);
	}

	public void salvarCorBody() {
		SistemaConfig config = sistemaConfigDAO.buscarPorId(1L, SistemaConfig.class);
		if (config == null) {
			config = new SistemaConfig();
			config.setId(1L); // opcional, se estiver usando ID manual
		}
		config.setCorBody(sistemaConfig.getCorBody());
		config.setCorTitulo(sistemaConfig.getCorTitulo());
		sistemaConfigDAO.salvar(config);
		System.out.println("salvando cor do menu...");
	}

	public void aplicarCorSelecionadaBody() {
		System.out.println(sistemaConfig.getCorTitulo());
		sistemaConfig.setCorBody(corSelecionadaBody);
		salvarCorBody(); // chama seu método existente de persistência
		MensagemUtil.enviarMensagem("Cor definida com sucesso!", FacesMessage.SEVERITY_INFO);
	}
}

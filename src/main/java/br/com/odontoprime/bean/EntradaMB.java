package br.com.odontoprime.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import br.com.odontoprime.entidade.Consulta;
import br.com.odontoprime.entidade.Entrada;
import br.com.odontoprime.entidade.EstadoPagamento;
import br.com.odontoprime.entidade.Parcela;
import br.com.odontoprime.service.ConsultaService;
import br.com.odontoprime.service.EntradaService;
import br.com.odontoprime.service.ParcelaService;

@Named
@ViewScoped
public class EntradaMB implements Serializable {
	// Testando o git no eclipse
	private static final long serialVersionUID = 6383242975512655099L;
	@Inject
	private EntradaService entradaService;
	private Date dataPesquisa;
	private List<Entrada> movimentacoes;
	private List<Consulta> consultas;
	private Entrada entrada;
	private EstadoPagamento estadoPagamento;
	private Parcela parcela;
	@Inject
	private ParcelaService parcelaService;
	private boolean selecionarTodos;
	private boolean pagamentoAtivado;
	@Inject
	private ConsultaService consultaService;
	private Consulta consulta;
	private List<Parcela> parcelasSelecionadas;
	private List<Consulta> consultasFiltradas;
	
	
	
	public List<Consulta> getConsultasFiltradas() {
		return consultasFiltradas;
	}

	public void setConsultasFiltradas(List<Consulta> consultasFiltradas) {
		this.consultasFiltradas = consultasFiltradas;
	}

	public List<Parcela> getParcelasSelecionadas() {
		return parcelasSelecionadas;
	}

	public void setParcelasSelecionadas(List<Parcela> parcelasSelecionadas) {
		this.parcelasSelecionadas = parcelasSelecionadas;
	}

	public boolean isSelecionarTodos() {
		return selecionarTodos;
	}

	public void setSelecionarTodos(boolean selecionarTodos) {
		this.selecionarTodos = selecionarTodos;
	}

	public boolean isPagamentoAtivado() {
		return pagamentoAtivado;
	}

	public void setPagamentoAtivado(boolean pagamentoAtivado) {
		this.pagamentoAtivado = pagamentoAtivado;
	}

	public Parcela getParcela() {
		return parcela;
	}

	public void setParcela(Parcela parcela) {
		this.parcela = parcela;
	}

	public EstadoPagamento getEstadoPagamento() {
		estadoPagamento = EstadoPagamento.PAGO;
		return estadoPagamento;
	}

	public Entrada getEntrada() {
		return entrada;
	}

	public void setEntrada(Entrada entrada) {
		this.entrada = entrada;
	}

	@PostConstruct
	public void initi() {
		consultas = consultaService.buscarTodos();
		consulta = new Consulta();
		parcela = new Parcela();
//		 recuperarDadosPagamento();
	}

	public Consulta getConsulta() {
		return consulta;
	}

	public void setConsulta(Consulta consulta) {
		this.consulta = consulta;
	}

	public List<Consulta> getConsultas() {
		return consultas;
	}

	public List<Entrada> getMovimentacoes() {
		return movimentacoes;
	}

	public void setMovimentacoes(List<Entrada> movimentacoes) {
		this.movimentacoes = movimentacoes;
	}

	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
	}

	public void buscarEntradaPorData() {
		movimentacoes = entradaService.buscarEntradaPorData(dataPesquisa);
	}


	@Deprecated
	public String selecionarDados(Entrada entrada) {
		entrada = entradaService.buscarEntradaComParcelas(entrada.getId());

		FacesContext.getCurrentInstance().getExternalContext().getFlash().put("entrada", entrada);
		FacesContext.getCurrentInstance().getExternalContext().getFlash().put("consulta", consulta);

		return "PagamentoParcela?faces-redirect=true";
	}

	@Deprecated
	public void recuperarDadosPagamento() {
		entrada = (Entrada) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("entrada");
		consulta = (Consulta) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("consulta");

		if (entrada == null && consulta == null) {
			entrada = new Entrada();
			consulta = new Consulta();
		} else {
			entrada = entradaService.buscarEntradaComParcelas(entrada.getId());
		}

	}

	@Deprecated
	public void efetuarPagamentoParcela() {
		parcelaService.efetuarPagamentoParcela(parcela, entrada);
	}

	@Deprecated
	public void cancelarPagamentoParcela() {
		parcelaService.cancelarPagamento(parcela);
	}
	@Deprecated
	public void limparDataPagamento() {
		parcelaService.limparDataPagamentoParcela(parcela);
	}

	public void pagarParcelas() {
		parcelaService.pagarParcelas(entrada, parcelasSelecionadas);
		atualizarConsulta();
	}

	public void cancelarPagamentosParcelas() {
		parcelaService.cancelarPagamentosParcelas(entrada, parcelasSelecionadas);
		atualizarConsulta();
	}

	public void buscarConsultaComParcela(ComponentSystemEvent event) {
		if (!FacesContext.getCurrentInstance().isPostback()) {
			if (this.consulta != null && this.consulta.getId() != null) {
				this.consulta = consultaService.buscarConsultaComParcela(this.consulta.getEntrada().getId());
				this.entrada = consulta.getEntrada();
			}
		}
	}

	public void atualizarConsulta() {

		try {
			this.consulta = consultaService.buscarPorId(consulta.getId());
			this.entrada = entradaService.buscarEntradaComParcelas(this.consulta.getEntrada().getId());
			consultas = consultaService.buscarTodos();
			System.out.println("consulta: " + this.consulta);
			System.out.println("entrada: " + this.entrada);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void desmarcarConsulta() {
		try {
			this.consulta = new Consulta();
			FacesContext.getCurrentInstance().getExternalContext().redirect("PagamentoParcela.xhtml");
			FacesContext.getCurrentInstance().responseComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

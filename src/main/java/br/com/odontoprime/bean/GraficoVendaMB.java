package br.com.odontoprime.bean;

import java.io.InputStream;
import java.io.Serializable;
import java.time.Year;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.chart.BarChartModel;

import br.com.odontoprime.entidade.TipoConsulta;
import br.com.odontoprime.service.ConsultaService;
import br.com.odontoprime.service.GraficoVendaService;
import br.com.odontoprime.util.FiltroNovoGrafico;

@Named
@ViewScoped
public class GraficoVendaMB implements Serializable {

	private static final long serialVersionUID = 4740910704355883080L;
	private int ano;
	private Double totalVenda;
	@Inject
	private ConsultaService consultaService;
	@Inject
	private GraficoVendaService graficoVendaService;
	private boolean exibirBotao = false;
	private Date primeiraData;
	private Date segundaData;
	private List<TipoConsulta> tiposConsulta;
	private BarChartModel barChartModel;
	private FiltroNovoGrafico filtroNovoGrafico;
	private BarChartModel novoBarChartModel;
	private List<Integer> anos;

	@PostConstruct
	public void init() {
		barChartModel = new BarChartModel();
		tiposConsulta = Arrays.asList(TipoConsulta.values());
		novoBarChartModel = new BarChartModel();
		filtroNovoGrafico = new FiltroNovoGrafico();
		int anoAtual = Year.now().getValue(); // Obtém o ano atual
		anos = IntStream.rangeClosed(anoAtual - 30, anoAtual + 30).boxed().toList();
		filtroNovoGrafico.setAno(anoAtual);
		filtroNovoGrafico.setAnoAte(anoAtual);
	}

	public List<Integer> getAnos() {
		return anos;
	}

	public void setAnos(List<Integer> anos) {
		this.anos = anos;
	}

	public BarChartModel getNovoBarChartModel() {
		return novoBarChartModel;
	}

	public void setNovoBarChartModel(BarChartModel novoBarChartModel) {
		this.novoBarChartModel = novoBarChartModel;
	}

	public FiltroNovoGrafico getFiltroNovoGrafico() {
		return filtroNovoGrafico;
	}

	public void setFiltroNovoGrafico(FiltroNovoGrafico filtroNovoGrafico) {
		this.filtroNovoGrafico = filtroNovoGrafico;
	}

	public List<TipoConsulta> getTiposConsulta() {
		return tiposConsulta;
	}

	public void setTiposConsulta(List<TipoConsulta> tiposConsulta) {
		this.tiposConsulta = tiposConsulta;
	}

	public BarChartModel getBarChartModel() {
		return barChartModel;
	}

	public void setBarChartModel(BarChartModel barChartModel) {
		this.barChartModel = barChartModel;
	}

	public Date getPrimeiraData() {
		return primeiraData;
	}

	public void setPrimeiraData(Date primeiraData) {
		this.primeiraData = primeiraData;
	}

	public Date getSegundaData() {
		return segundaData;
	}

	public void setSegundaData(Date segundaData) {
		this.segundaData = segundaData;
	}

	public boolean isExibirBotao() {
		return exibirBotao;
	}

	public void setExibirBotao(boolean exibirBotao) {
		this.exibirBotao = exibirBotao;
	}

	public Double getTotalVenda() {
		return totalVenda;
	}

	public void setTotalVenda(Double totalVenda) {
		this.totalVenda = totalVenda;
	}

	public int getAno() {
		return ano;
	}

	public void setAno(int ano) {
		this.ano = ano;
	}

	public Double totalVendaPorAno() {
		totalVenda = graficoVendaService.totalVendaPorAno(ano);
		return totalVenda;
	}

	public void gerarGrafico() {
		this.barChartModel = graficoVendaService.preencherBarChartModel(primeiraData, segundaData);
	}

	public boolean graficoVazio() {
		return this.novoBarChartModel != null & !novoBarChartModel.getSeries().isEmpty();
	}

	public void gerarNovoGrafico() {
		this.novoBarChartModel = graficoVendaService.novoBarChartModel(filtroNovoGrafico);
	}
}

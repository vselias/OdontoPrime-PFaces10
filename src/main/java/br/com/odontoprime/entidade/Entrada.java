package br.com.odontoprime.entidade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Entrada implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8248762477115345323L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private EstadoPagamento estadoPagamento;

	@Temporal(TemporalType.DATE)
	private Date dataPagamento;

	@Temporal(TemporalType.DATE)
	private Date dataVencimento;

	private String parcela;

	private Double valor;

	@Temporal(TemporalType.DATE)
	private Date dataLancamento;

	private int desconto;

	private Double valorDesconto;

	private Boolean parcelado;

	@Enumerated(EnumType.STRING)
	private FormaPagamento formaPagamento;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "entrada_id")
	private List<Parcela> parcelas;

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public List<Parcela> getParcelas() {
		return parcelas;
	}

	public void setParcelas(List<Parcela> parcelas) {
		this.parcelas = parcelas;
	}

	public Date getDataVencimento() {
		return dataVencimento;
	}

	public Entrada() {
		estadoPagamento = EstadoPagamento.PENDENTE;
		parcelas = new ArrayList<>();
	}

	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public Boolean getParcelado() {
		return parcelado;
	}

	public void setParcelado(Boolean parcelado) {
		this.parcelado = parcelado;
	}

	public EstadoPagamento getEstadoPagamento() {
		return estadoPagamento;
	}

	public void setEstadoPagamento(EstadoPagamento estadoPagamento) {
		this.estadoPagamento = estadoPagamento;
	}

	public String getParcela() {
		return parcela;
	}

	public void setParcela(String parcela) {
		this.parcela = parcela;
	}

	public Double getValorDesconto() {
		if (valorDesconto == null) {
			valorDesconto = Double.valueOf(0);
		}
		return valorDesconto;
	}

	public void setValorDesconto(Double valorDesconto) {
		this.valorDesconto = valorDesconto;
	}

	public Date getDataLancamento() {
		return dataLancamento;
	}

	public void setDataLancamento(Date dataLancamento) {
		this.dataLancamento = dataLancamento;
	}

	public int getDesconto() {
		return desconto;
	}

	public void setDesconto(int desconto) {
		this.desconto = desconto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	@Override
	public String toString() {
		return "Entrada [\nid=" + id + ", estadoPagamento=" + estadoPagamento + ", dataPagamento=" + dataPagamento
				+ ", dataVencimento=" + dataVencimento + ", parcela=" + parcela + ", valor=" + valor
				+ ", dataLancamento=" + dataLancamento + ", desconto=" + desconto + ", valorDesconto=" + valorDesconto
				+ ", parcelado=" + parcelado + ", formaPagamento=" + formaPagamento + ",\nparcelas=\n" + parcelas +"\n]";
	}

}

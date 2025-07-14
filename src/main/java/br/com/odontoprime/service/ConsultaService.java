package br.com.odontoprime.service;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.print.attribute.standard.DateTimeAtCompleted;

import org.hibernate.exception.ConstraintViolationException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.draw.LineSeparator;

import br.com.odontoprime.dao.ConsultaDAO;
import br.com.odontoprime.dao.EntradaDAO;
import br.com.odontoprime.dao.MovimentacaoCaixaDAO;
import br.com.odontoprime.dao.PacienteDAO;
import br.com.odontoprime.entidade.Consulta;
import br.com.odontoprime.entidade.Entrada;
import br.com.odontoprime.entidade.EstadoConsulta;
import br.com.odontoprime.entidade.EstadoPagamento;
import br.com.odontoprime.entidade.Paciente;
import br.com.odontoprime.entidade.Parcela;
import br.com.odontoprime.entidade.StatusCadastro;
import br.com.odontoprime.entidade.Usuario;
import br.com.odontoprime.util.FacesUtil;
import br.com.odontoprime.util.MensagemUtil;

public class ConsultaService implements Serializable {

	private static final long serialVersionUID = -4575922553148091096L;

	@Inject
	private ConsultaDAO consultaDAO;
	@Inject
	private PacienteDAO pacienteDAO;
	@Inject
	private MovimentacaoCaixaDAO movimentacaoCaixaDAO;
	@Inject
	private EntradaDAO entradaDAO;
	@Inject
	private ParcelaDAO parcelaDAO;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	@Inject
	private ParcelaService parcelaService;

	public Consulta buscarPorId(Long id) {
		return consultaDAO.buscarPorId(id, Consulta.class);
	}

	public List<Consulta> vendasPorDia() {
		return consultaDAO.buscarVendasPorDia();
	}

	public void atualizar(Consulta consulta) {
		try {
			consultaDAO.atualizar(consulta);
			MensagemUtil.enviarMensagem("Consulta atualizada com sucesso!", FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			MensagemUtil.enviarMensagem(
					"Erro ao atualizar consulta. Tente novamente mais tarde ou contate o administrador do sistema!",
					FacesMessage.SEVERITY_INFO);
		}
	}

	public void remover(Consulta consulta) {
		try {

			if (!movimentacaoCaixaDAO.existeFechamentoPorData(consulta.getEntrada().getDataLancamento())) {
				consultaDAO.remover(consultaDAO.getReference(Consulta.class, consulta.getId()));
				MensagemUtil.enviarMensagem("Consulta removida com sucesso!", FacesMessage.SEVERITY_INFO);
				return;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String data = sdf.format(consulta.getEntrada().getDataLancamento().getTime());
			MensagemUtil.enviarMensagem("Não é possível remover consulta com caixa fechado para data: " + data,
					FacesMessage.SEVERITY_WARN);
		} catch (Exception e) {
			e.printStackTrace();
			MensagemUtil.enviarMensagem(
					"Erro ao remover consulta.Contate o administrador ou tente novamente mais tarde!",
					FacesMessage.SEVERITY_ERROR);
		}
	}

	public List<Consulta> buscarTodos() {
		return consultaDAO.buscarTodos(Consulta.class);
	}

	public Double buscarVendasPorData(Date dataInicio, Date dataFinal) {

		String inicio = simpleDateFormat.format(dataInicio.getTime());
		String dtFinal = simpleDateFormat.format(dataFinal.getTime());
		System.out.println("Inicio : " + inicio + " Final: " + dtFinal);
		return consultaDAO.valorTotalVendaPorData(dataInicio, dataFinal);
	}

	public List<Consulta> buscarConsultasPorPaciente() {
		return consultaDAO.buscarConsultasPorPaciente();
	}

	public List<Object[]> vendasPorAnoGrafico(int ano) {
		return consultaDAO.vendasPorAno(ano);
	}

	public boolean efetuarConsulta(Consulta consulta) {
		try {

			/*
			 * buscando instancia atualizada com as consultas referente ao paciente
			 * resolvendo problema do lazyLoading
			 */
			Paciente paciente = pacienteDAO.buscarPacienteComConsultas(consulta.getPaciente().getId());

			if (!fluxoCaixaFechado(consulta.getEntrada().getDataLancamento()) && consultaPossuiPaciente(consulta)
					&& !existeHorarioConsulta(consulta) && validarEstadoNovaConsulta(consulta)
					&& validarCalculoDesconto(consulta) && validarDataPagamento(consulta)) {

				// registrar estatus do cadastro
				registrarEstatusCadastroConsulta(consulta);

				// remover referencia antiga caso atualizar
				// consulta

				// metodo para parcelar divida
				if (!existeConsultaPaga(consulta)) {
					removerReferenciaParcela(consulta);
					parcelarConsulta(consulta);
				}

				registrarUsuarioLogado(consulta);

				paciente.getConsultas().add(consulta);
				consulta.setPaciente(paciente);

				pacienteDAO.salvar(paciente);

				enviarMensagemCadastroConsulta(consulta, true);

				return true;
			}

		} catch (Exception e) {
			MensagemUtil.enviarMensagem(
					"Erro ao salvar consulta! tente novamente mais tarde ou contate o admnistrador.",
					FacesMessage.SEVERITY_ERROR);
			e.printStackTrace();
		}
		return false;

	}

	private boolean fluxoCaixaFechado(Date dataLancamento) {

		if (dataLancamento == null)
			dataLancamento = new Date();

		if (movimentacaoCaixaDAO.existeFechamentoPorData(dataLancamento)) {

			MensagemUtil.enviarMensagem("Não pode cadastrar ou atualizar consultas com caixa fechado na data: "
					+ simpleDateFormat.format(dataLancamento.getTime()), FacesMessage.SEVERITY_WARN);

			return true;
		}

		return false;
	}

	public boolean isConsultaAusente(Consulta consulta) {
		return consulta.getEstadoConsulta().equals(EstadoConsulta.AUSENTE);
	}

	public boolean isConsultaCancelada(Consulta consulta) {
		return consulta.getEstadoConsulta().equals(EstadoConsulta.CANCELADO);
	}

	public void enviarMensagemCadastroConsulta(Consulta consulta, boolean consultaEfetuada) {

		if (consultaEfetuada && consulta.getStatusCadastro().equals(StatusCadastro.CADASTRO)) {
			MensagemUtil.enviarMensagem("Consulta cadastrada com sucesso!", FacesMessage.SEVERITY_INFO);
		} else if (consultaEfetuada && consulta.getStatusCadastro().equals(StatusCadastro.ATUALIZACAO)) {
			MensagemUtil.enviarMensagem("Consulta atualizada com sucesso!", FacesMessage.SEVERITY_INFO);
		}
	}

	public boolean existeHorarioConsulta(Consulta consulta) throws Exception {
		List<Consulta> consultas = null;

		if (novaConsulta(consulta) && consultaPossuiData(consulta)) {

			if (consultaEditavel(consulta))
				consultas = consultaDAO.buscarConsultasPorData(consulta.getDataConsulta(), consulta.getId());
			else {
				consultas = consultaDAO.buscarConsultasPorData(consulta.getDataConsulta());
			}

			if (consultas != null && !consultas.isEmpty()) {

				if (consultas.size() > 0) {
					MensagemUtil.enviarMensagem("Esse horário já foi definido! Escolha um novo horário.",
							FacesMessage.SEVERITY_WARN);
					return true;
				}
			}
		}
		return false;
	}

	public Date limparHoraData(Date dataParametro) {
		Calendar formatarData = new GregorianCalendar();
		formatarData.setTime(dataParametro);
		formatarData.set(Calendar.HOUR, 0);
		formatarData.set(Calendar.MINUTE, 0);
		formatarData.set(Calendar.SECOND, 0);
		formatarData.set(Calendar.MILLISECOND, 0);
		dataParametro = formatarData.getTime();
		return dataParametro;
	}

	public boolean validarDataPagamento(Consulta consulta) {
		LocalDate vencimento = Instant.ofEpochMilli(consulta.getEntrada().getDataVencimento().getTime())
				.atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate hoje = LocalDate.now();

		if (existeConsultaPaga(consulta) && vencimento.isBefore(hoje)) {
			return true;
		}

		if (vencimento.compareTo(hoje) == -1) {
			MensagemUtil.enviarMensagem("Data de vencimento não pode ser anterior atual", FacesMessage.SEVERITY_WARN);
			return false;
		}
		return true;
	}

	public void parcelarConsulta(Consulta consulta) {
		// capturamos dados da tela
		Entrada dadosEntrada = consulta.getEntrada();
		// se a consulta for parcela dividir as entradas para
		// adicionar
		// na lista
		int numParcela = 0;

		double valorParcela = 0;

		Entrada entradaVO = null;

		if (consulta.getEntrada().getParcelado()) {
			String[] dadosParcela = consulta.getEntrada().getParcela().split("X");
			dadosParcela[1] = dadosParcela[1].trim().replace("R$", " ");
			System.out.println(dadosParcela[1]);
			// capturar quantidade de parcela e alterar o formato dos valores.
			numParcela = Integer.parseInt(dadosParcela[0].trim());
			if (!dadosParcela[1].contains(".")) {
				valorParcela = Double.parseDouble(dadosParcela[1].trim().replace(",", "."));
			}
			valorParcela = Double.parseDouble(dadosParcela[1].trim().replace(".", "").replace(",", "."));
		} else {
			DecimalFormat decimalFormat = new DecimalFormat("R$ #0.00");
			numParcela = 1;
			valorParcela = dadosEntrada.getValorDesconto();
			consulta.getEntrada().setParcela(numParcela + " X " + decimalFormat.format(valorParcela));
		}

		if (entradaEditavel(dadosEntrada))
			entradaVO = entradaDAO.buscarEntradaComParcelas(consulta.getEntrada().getId());

		if (entradaVO != null)
			consulta.getEntrada().setParcelas(entradaVO.getParcelas());

		if (consulta.getEntrada().getParcelas() == null) {

			consulta.getEntrada().setParcelas(new ArrayList<>());
		}

		for (int i = 0; i < numParcela; i++) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			// objeto para manipular as parcelas
			Parcela parcela = new Parcela();

			Calendar dataVencimento = new GregorianCalendar();
			dataVencimento.setTime(dadosEntrada.getDataVencimento());

			/*
			 * i começa do zero logo primeiro mês é a propria data de vencimento sem
			 * alteração
			 */
			dataVencimento.add(Calendar.MONTH, +i);

			parcela.setEstadoPagamento(EstadoPagamento.PENDENTE);
			parcela.setDataVencimento(dataVencimento.getTime());
			parcela.setNumParcela((i + 1));
			parcela.setValor(valorParcela);
			parcela.setDataRegistro(new Date());

			// logica pra modificar a data do lancamento
			if (consulta.getEntrada().getDataLancamento() == null) {
				consulta.getEntrada().setDataLancamento(new Date());
			}

			consulta.getEntrada().getParcelas().add(parcela);

		}
	}

	public boolean existeConsultaPaga(Consulta consulta) {
		/*
		 * Método para verificar se existe alguma parcela da consulta paga. Caso exista,
		 * é retornado true para não permitir a alteração da consulta.
		 */

		Entrada dadosEntrada = consulta.getEntrada();
		Entrada entradaVO = null;
		try {
			if (consultaEditavel(consulta)) {

				// buscando entrada pra evitar lazyLoading
				if (entradaEditavel(dadosEntrada))
					entradaVO = entradaDAO.buscarEntradaComParcelas(dadosEntrada.getId());

				if (entradaVO != null) {
					if (entradaVO.getParcelas() != null)
						if (parcelaService.existeParcelaPaga(entradaVO.getParcelas())) {
							return true;
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public List<Consulta> buscarConsultasFuturas() {
		return consultaDAO.buscarConsultasFuturas();
	}

	public void gerarRelatorioCosultaPDF(Object documento) {

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			Document pdf = (Document) documento;
			pdf.addCreationDate();
			pdf.setPageSize(PageSize.A4);
			pdf.open();
			String dataAtual = format.format(new Date().getTime());
			Paragraph paragrafo = new Paragraph();
			Image image = Image.getInstance(this.getClass().getResource("/img/logo-login.png"));
			image.scaleToFit(80, 40);
			image.setAlignment(Element.ALIGN_LEFT);
			paragrafo.add(image);
			paragrafo.setAlignment(Element.ALIGN_RIGHT);
			paragrafo.setSpacingAfter(10);
			paragrafo.add(dataAtual);
			pdf.add(paragrafo);
			LineSeparator lineSeparator = new LineSeparator();
			pdf.add(lineSeparator);
			Paragraph paragraph = new Paragraph("Relatório de Consultas");
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.setSpacingAfter(10);
			pdf.add(paragraph);
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao gerar documento paciente. " + e);
		}

	}

	public void registrarEstatusCadastroConsulta(Consulta consulta) {
		if (consultaEditavel(consulta)) {

			// remover entrada no caixa caso a consulta for
			// cancelada
			if (isConsultaCancelada(consulta)) {
				consulta.setEntrada(null);
			}

			// setando o estatus de atualização para consulta
			consulta.setStatusCadastro(StatusCadastro.ATUALIZACAO);
		} else {
			/*
			 * caso for uma nova consulta é definido como cadastro o estatus
			 */
			consulta.setStatusCadastro(StatusCadastro.CADASTRO);

		}
	}

	public boolean validarEstadoNovaConsulta(Consulta consulta) {
		// condicao que verifica se a consulta é nova
		if (consultaNaoEditavel(consulta)) {

			// verificar o estado de ausente e cancelamento da
			// consulta
			// caso de edição
			if (isConsultaCancelada(consulta)) {
				MensagemUtil.enviarMensagem("Estado CANCELADO deve ser uma consulta existente!",
						FacesMessage.SEVERITY_ERROR);
				return Boolean.FALSE;
			} else if (isConsultaAusente(consulta)) {

				MensagemUtil.enviarMensagem("Estado AUSENTE deve ser uma consulta existente!",
						FacesMessage.SEVERITY_ERROR);
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	public boolean calcularDesconto(Entrada entrada) {
		try {
			DecimalFormat format = new DecimalFormat("R$ #,##0.00");
			// recuperando o valor antigo {TOTAL}
			double valorTotal = entrada.getValorDesconto();

			// SETANDO O VALOR TOTAL
			entrada.setValor(valorTotal);

			// calculando novo valor com desconto
			double valorComDesconto = entrada.getValor() - ((entrada.getDesconto() * entrada.getValor()) / 100);

			// SETANDO O VALOR COM DESCONTO
			entrada.setValorDesconto(valorComDesconto);

			String mensagem = "VALOR TOTAL: " + format.format(valorTotal) + " DESCONTO: " + entrada.getDesconto() + "%"
					+ " VALOR COM DESCONTO: " + format.format(entrada.getValorDesconto());
			MensagemUtil.enviarMensagem(mensagem, FacesMessage.SEVERITY_INFO);
			return true;
		} catch (Exception e) {
			MensagemUtil.enviarMensagem("Erro ao calcular desconto!", FacesMessage.SEVERITY_FATAL);
		}

		return false;

	}

	public boolean consultaPossuiEstado(Consulta consulta) {
		return novaConsulta(consulta) && consulta.getEstadoConsulta() != null;
	}

	public boolean consultaEditavel(Consulta consulta) {
		return consulta != null && consulta.getId() != null && consulta.getId() > 0;
	}

	public boolean consultaNaoNula(Consulta consulta) {
		return consulta != null;
	}

	public boolean consultaNaoEditavel(Consulta consulta) {
		return !consultaEditavel(consulta);
	}

	public boolean novaConsulta(Consulta consulta) {
		return consulta != null && (consulta.getId() == null || consulta.getId() == 0);
	}

	public boolean consultaPossuiData(Consulta consulta) {
		return novaConsulta(consulta) && consulta.getDataConsulta() != null;
	}

	public boolean consultaPossuiEntrada(Consulta consulta) {
		return novaConsulta(consulta) && consulta.getEntrada() != null;
	}

	public boolean entradaEditavel(Entrada entrada) {
		return entrada != null && entrada.getId() != null && entrada.getId() > 0;
	}

	public boolean consultaPossuiPaciente(Consulta consulta) {
		if (consultaNaoNula(consulta) && consulta.getPaciente() != null) {
			return Boolean.TRUE;
		} else {
			MensagemUtil.enviarMensagem("Paciente deve ser selecionado!", FacesMessage.SEVERITY_ERROR);
			return Boolean.FALSE;

		}
	}

	public boolean consultaNaoPossuiEntrada(Consulta consulta) {
		return !consultaPossuiEntrada(consulta);
	}

	public boolean usuarioExiste(Usuario usuarioLogado) {
		return usuarioLogado != null && usuarioLogado.getId() != null && usuarioLogado.getId() > 0;
	}

	public boolean validarCalculoDesconto(Consulta consulta) {
		if (!isConsultaCancelada(consulta)) {

			// vinculando objetos da entrada na consulta
			if (consultaNaoEditavel(consulta))
				consulta.getEntrada().setDataLancamento(new Date());

			if (consulta.getEntrada().getValor() == null) {

				consulta.getEntrada().setValor(consulta.getEntrada().getValorDesconto());
			}

			/*
			 * verificar calculo desconto para valor não ser burlado pelo front-end
			 */
			if (consulta.getEntrada().getDesconto() > 0) {

				double valorComDesconto = consulta.getEntrada().getValor()
						- ((consulta.getEntrada().getDesconto() * consulta.getEntrada().getValor()) / 100);
				System.out.println("Calc Valor: " + valorComDesconto + "Desconto Entrada: "
						+ consulta.getEntrada().getValorDesconto());
				if (valorComDesconto == consulta.getEntrada().getValorDesconto()) {
					MensagemUtil.enviarMensagem("Valor da consulta não compatível com desconto.",
							FacesMessage.SEVERITY_WARN);
					return false;
				}
			}

			/*
			 * caso não aplique o desconto o valor total é recebido no valor com desconto
			 */
			if (consulta.getEntrada().getDesconto() == 0) {
				consulta.getEntrada().setValor(consulta.getEntrada().getValorDesconto());
			}
		}
		return true;
	}

	public List<SelectItem> gerarQuantidadePagamento(Double valor) {
		List<SelectItem> quantidadePagamentos = new ArrayList<>();

		DecimalFormat format = new DecimalFormat("R$#,###.00");

		if (valor == null) {
			valor = new Double(0);
		}
		SelectItemGroup dividido = new SelectItemGroup("Prestação");
		SelectItem[] itens = new SelectItem[10];

		if (valor > 0) {
			for (int i = 0; i < itens.length; i++) {
				itens[i] = new SelectItem((i + 1) + " X " + format.format((valor / (i + 1))));
			}
			dividido.setSelectItems(itens);
			quantidadePagamentos.add(dividido);
			return quantidadePagamentos;
		}

		dividido.setSelectItems(new SelectItem[] { new SelectItem("Informe um valor.") });
		quantidadePagamentos.add(dividido);
		return quantidadePagamentos;

	}

	public List<Consulta> buscarConsultasFechamento(Date dataMovimentacao) {
		return consultaDAO.buscarConsultasFechamento(dataMovimentacao);
	}

	public void removerReferenciaParcela(Consulta consulta) throws Exception {
		if (consultaEditavel(consulta)) {
			parcelaDAO.removerParcelasPorId(consulta.getEntrada().getId());
		}
	}

	public void registrarUsuarioLogado(Consulta consulta) {
		// buscar usuário logado no sistema pra cadastrar na
		// consulta
		Usuario usuarioLogado = (Usuario) FacesUtil.getSessionAttribute("usuario");
		if (usuarioExiste(usuarioLogado)) {

			consulta.setUsuarioCadastro(usuarioLogado.getLogin());
			consulta.setDataCadastro(new Date());
		}
	}

	public Consulta buscarConsultaComParcela(Long idEntrada) {
		return consultaDAO.buscarConsultaComParcela(idEntrada);
	}
}

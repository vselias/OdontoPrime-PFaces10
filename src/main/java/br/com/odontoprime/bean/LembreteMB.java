package br.com.odontoprime.bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import br.com.odontoprime.entidade.Lembrete;
import br.com.odontoprime.service.LembreteService;

@Named
@ViewScoped
public class LembreteMB implements Serializable {

	private static final long serialVersionUID = 1L;
	private ScheduleModel scheduleModel;
	@Inject
	private LembreteService lembreteService;
	private Lembrete lembrete;

	public Lembrete getLembrete() {
		return lembrete;
	}

	public void setLembrete(Lembrete lembrete) {
		this.lembrete = lembrete;
	}

	public ScheduleModel getScheduleModel() {
		return scheduleModel;
	}

	public void setScheduleModel(ScheduleModel scheduleModel) {
		this.scheduleModel = scheduleModel;
	}

	@PostConstruct
	public void init() {
		this.scheduleModel = lembreteService.listarLembretes();
		this.lembrete = new Lembrete();
	}

	public void addLembrete() {

		lembreteService.salvar(this.lembrete);
		carregarLembretes();
	}

	public void selecionarData(SelectEvent event) {
		lembrete = new Lembrete();
		lembrete.setDataInicio((LocalDateTime) event.getObject());
		lembrete.setDataFinal((LocalDateTime)  event.getObject());

	}

	public void exibirLembrete(SelectEvent selectEvent) {
		Long id = Long.parseLong(((ScheduleEvent) selectEvent.getObject()).getData().toString());
		lembrete = lembreteService.buscarPorId(id);
	}

	public void carregarLembretes() {
		scheduleModel = lembreteService.listarLembretes();
		this.lembrete = new Lembrete();
	}

	public void excluir() {
		System.out.println("lembrete id: " + this.lembrete.getId());
		lembreteService.excluir(this.lembrete);
		carregarLembretes();

	}

	public void moverLembrete(ScheduleEntryMoveEvent event) {

		Long id = Long.parseLong(event.getScheduleEvent().getData().toString());

		List<Lembrete> lembretes = lembreteService.buscarTodos();

		for (Lembrete lemb : lembretes) {
			if (lemb.getId().equals(id)) {
				lembrete = lemb;
				break;
			}
		}
		lembrete.setDataInicio(event.getScheduleEvent().getStartDate());
		lembrete.setDataFinal(event.getScheduleEvent().getEndDate());
		lembreteService.salvar(lembrete);
		carregarLembretes();
	}

	public void redimensionarLembrete(ScheduleEntryResizeEvent event) {
		Long id = Long.parseLong(event.getScheduleEvent().getData().toString());
		this.lembrete = lembreteService.buscarPorId(id);
//		List<Lembrete> lembretes = lembreteService.buscarTodos();
//
//		for (Lembrete lemb : lembretes) {
//			if (lemb.getId().equals(id)) {
//				lembrete = lemb;
//				break;
//			}
//		}
		this.lembrete.setDataInicio(event.getScheduleEvent().getStartDate());
		this.lembrete.setDataFinal(event.getScheduleEvent().getEndDate());
		this.lembreteService.salvar(this.lembrete);
		// carregarLembretes();
	}
}

package br.com.odontoprime.service;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import br.com.odontoprime.dao.LembreteDAO;
import br.com.odontoprime.entidade.Lembrete;
import br.com.odontoprime.util.MensagemUtil;

public class LembreteService implements Serializable {

	private static final long serialVersionUID = 781313023961065563L;
	@Inject
	private LembreteDAO lembreteDAO;

	public void salvar(Lembrete lembrete) {

		if (lembrete.getId() == null) {
			lembreteDAO.salvar(lembrete);
			MensagemUtil.enviarMensagem("Lembrete adicionado com sucesso!", FacesMessage.SEVERITY_INFO);
		} else {
			lembreteDAO.atualizar(lembrete);
			MensagemUtil.enviarMensagem("Lembrete atualizado com sucesso!", FacesMessage.SEVERITY_INFO);
		}
	}

	public ScheduleModel listarLembretes() {

		List<Lembrete> lembretes = lembreteDAO.buscarTodos(Lembrete.class);
		ScheduleModel scheduleModel = new DefaultScheduleModel();
		scheduleModel.clear();
		lembretes.forEach(l -> {

			DefaultScheduleEvent event = new DefaultScheduleEvent();
			event.setData(l.getId());
			event.setTitle(l.getTitulo());
			event.setStartDate(l.getDataInicio());
			event.setEndDate(l.getDataFinal());
			event.setAllDay(l.isDiaTodo());
			event.setEditable(true); // Permite edição

			if (l.isFinalizado()) {
				event.setStyleClass("finalizado");
			}
			scheduleModel.addEvent(event);
		});

		return scheduleModel;
	}

	public Lembrete buscarPorId(Long id) {
		return lembreteDAO.buscarPorId(id, Lembrete.class);
	}

	public List<Lembrete> buscarTodos() {
		return lembreteDAO.buscarTodos(Lembrete.class);
	}

	public void excluir(Lembrete lembrete) {
		lembreteDAO.remover(lembrete);
		MensagemUtil.enviarMensagem("Lembrete removido!", FacesMessage.SEVERITY_INFO);

	}
}

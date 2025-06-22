package br.odontoprime.converter;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.odontoprime.dao.PacienteDAO;
import br.com.odontoprime.entidade.Paciente;

@FacesConverter(value = "pacienteConverter", forClass = Paciente.class)
public class PacienteConverter implements Converter {

	@Inject
	private PacienteDAO pacienteDAO;

	public PacienteConverter() {
		this.pacienteDAO = CDILocator.getBean(PacienteDAO.class);
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value != null && !"".equals(value)) {
			return pacienteDAO.buscarPorId(Long.parseLong(value), Paciente.class);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {

			Long codigo = ((Paciente) value).getId();
			return codigo == null ? "" : codigo.toString();
		}
		return null;
	}

}

package br.com.odontoprime.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.FileUploadEvent;

import br.com.odontoprime.entidade.Foto;
import br.com.odontoprime.entidade.Paciente;
import br.com.odontoprime.service.GaleriaService;
import br.com.odontoprime.service.PacienteService;

@Named(value = "galeriaMB")
@ViewScoped
public class GaleriaMB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Paciente paciente;
	private Foto foto;
	private List<Paciente> pacientes;
	@Inject
	private PacienteService pacienteService;
	@Inject
	private GaleriaService galeriaService;
	private List<Paciente> listaPesquisa;
	private String fotoId;
	@Inject
	private FotoDAO fotoDAO;

	public List<Paciente> getListaPesquisa() {
		return listaPesquisa;
	}

	public Foto getFoto() {
		return foto;
	}

	public void setFoto(Foto foto) {
		this.foto = foto;
	}

	public Paciente getPaciente() {
		if (paciente == null)
			paciente = new Paciente();
		return paciente;
	}

	public void setPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public List<Paciente> getPacientes() {
		return pacientes;
	}

	@PostConstruct
	public void init() {
		paciente = new Paciente();
		pacientes = pacienteService.buscarTodos();
		foto = new Foto();
		listaPesquisa = new ArrayList<>();
	}

	public void inserirFotoGaleria(FileUploadEvent fileUploadEvent) {
		galeriaService.inserirFotoGaleria(foto, fileUploadEvent);
	}

	public void salvarGaleria() {
		System.out.println("chamou o metodo galeriaMB salvar");
		galeriaService.salvarGaleriaPaciente(paciente, foto);
		atualizarPacienteEFoto();
	}

	public void atualizarPacienteEFoto() {
		pacientes = pacienteService.buscarTodos();
		foto = new Foto();
	}

	public void selecionarPaciente(Paciente paciente) {
		this.paciente = galeriaService.buscarPacienteComFotos(paciente.getId());
	}

	public void enviarPaciente(Paciente p) {
		this.paciente = p;
	}

	public String getFotoId() {
		return fotoId;
	}

	public void setFotoId(String fotoId) {
		this.fotoId = fotoId;
	}

	public void selecionarFoto() {
		String id = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("fotoId");
		System.out.println("Foto selecionada = " + id);
		this.foto = fotoDAO.buscarPorId(Long.parseLong(id), Foto.class);
		this.fotoId = id;
		System.out.println("Objeto foto com id :" + this.foto.getId());

	}

	public void excluirFoto() {

		pacienteService.excluirFoto(fotoId);
		atualizarPacienteEFoto();
		Optional<Paciente> pacienteFilter = pacientes.stream().filter(p -> p.getId() == this.paciente.getId())
				.findFirst();
		this.paciente = pacienteFilter.get();
	}
}

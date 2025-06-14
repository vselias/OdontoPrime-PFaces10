package br.com.odontoprime.entidade;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.com.odontoprime.dao.GenericDAO;
import br.com.odontoprime.util.Transactional;

public class FotoDAO extends GenericDAO<Foto, Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Inject
	EntityManager entityManager;

	@Transactional
	public void removerDependenciaFoto(Long id) {
		entityManager.createNativeQuery("Delete from paciente_foto where fotos_id = :id").setParameter("id", id)
				.executeUpdate();
	}

}

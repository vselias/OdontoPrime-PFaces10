package br.com.odontoprime.entidade;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SistemaConfig {
	@Id
	private Long id;
	private String corMenu = "#1a73e8";
	private String corBody =  "#1a73e8";
	private String corTitulo = "#1a73e8";

	public String getCorTitulo() {
		return corTitulo;
	}

	public void setCorTitulo(String corTitulo) {
		this.corTitulo = corTitulo;
	}

	public String getCorBody() {
		if (corBody != null && !corBody.startsWith("#")) {
			return "#" + corBody;
		}
		return corBody;
	}

	public void setCorBody(String corBody) {
		this.corBody = corBody;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCorMenu() {
		if (corMenu != null && !corMenu.startsWith("#")) {
			return "#" + corMenu;
		}
		return corMenu;

	}

	public void setCorMenu(String corMenu) {
		this.corMenu = corMenu;
	}

}

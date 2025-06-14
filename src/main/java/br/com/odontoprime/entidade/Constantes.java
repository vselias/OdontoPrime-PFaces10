package br.com.odontoprime.entidade;

import java.io.File;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

public class Constantes {
	public static final String USER_HOME = System.getProperty("user.home");
	public static final String PASTA_IMAGEM = "/OP/imagens";
	public static final String CAMINHO_IMAGEM = USER_HOME + PASTA_IMAGEM;

	public static String getPathServidor() {
	    FacesContext facesContext = FacesContext.getCurrentInstance();
	    if (facesContext == null) {
	        return null; // Retorna null ou um valor padrão para evitar erro
	    }
	    ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
	    return servletContext.getRealPath("") + File.separator + "temp" + File.separator + "imagens";
	}

}

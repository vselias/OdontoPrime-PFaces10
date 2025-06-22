package br.com.odontoprime.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

import br.com.odontoprime.dao.ProdutorEntityManager;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Named("relatorioMB")
@ViewScoped
public class RelatorioMB implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void gerarRelatorio() {
		try {
			URL arquivoURL = getClass().getClassLoader().getResource("relatorio/paciente.jrxml");
			String caminhoAbsoluto = Paths.get(arquivoURL.toURI()).toString();
			System.out.println("Caminho correto: " + caminhoAbsoluto);
			JasperCompileManager.compileReportToFile(caminhoAbsoluto, caminhoAbsoluto.replace(".jrxml", ".jasper"));

			InputStream jasperInput = getClass().getClassLoader().getResourceAsStream("relatorio/paciente.jasper");
			JasperReport report = (JasperReport) JRLoader.loadObject(jasperInput);

			Map<String, Object> parametros = new HashMap<>();
			EntityManager em = ProdutorEntityManager.getFactory().createEntityManager();
			Session session = em.unwrap(Session.class);

			Connection conn = session.doReturningWork(new ReturningWork<Connection>() {
				@Override
				public Connection execute(Connection connection) throws SQLException {
					return connection;
				}
			});
			parametros.put("pacienteId", 1L);

			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parametros, conn);

			byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

			// Configurar tipo de conteúdo
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "inline; filename=relatorio.pdf");

			try (OutputStream os = response.getOutputStream()) { // Aqui está a correção
				os.write(pdf);
				os.flush();
			}
			facesContext.responseComplete();

			// Definir um caminho absoluto na pasta do usuário
			String caminhoDiretorio = System.getProperty("user.home") + "/relatorios";
			File diretorio = new File(caminhoDiretorio);
			if (!diretorio.exists()) {
				diretorio.mkdirs(); // Criar a pasta se não existir
			}

			// Criar o arquivo PDF dentro do diretório correto
			File arquivoPDF = new File(diretorio, "relatorio.pdf");

			// Exibir o caminho do arquivo antes da exportação
			System.out.println("PDF será salvo em: " + arquivoPDF.getAbsolutePath());

			if (jasperPrint == null) {
				System.out.println("JasperPrint está nulo! Verifique os parâmetros e conexão.");
			} else {
				System.out.println("JasperPrint gerado com sucesso.");
			}

			JasperExportManager.exportReportToPdfFile(jasperPrint, arquivoPDF.getAbsolutePath());

			// Verificar se o arquivo realmente foi criado
			if (arquivoPDF.exists()) {
				System.out.println("Arquivo encontrado: " + arquivoPDF.getAbsolutePath());
			} else {
				System.out.println("Nenhum arquivo gerado. Verifique permissões.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

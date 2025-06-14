package br.com.odontoprime.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.odontoprime.entidade.Constantes;

@WebServlet("/imagens/*")
public class ExibirImagensServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String nomeImagem = request.getPathInfo(); // Ex: /123456.png
		if (nomeImagem == null || nomeImagem.equals("/")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome da imagem inválido.");
			return;
		}

		File imagem = new File(Constantes.CAMINHO_IMAGEM, nomeImagem);
		if (!imagem.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Imagem não encontrada.");
			return;
		}

		// Define o tipo de conteúdo (isso é essencial pra navegador exibir
		// corretamente)
		String contentType = getServletContext().getMimeType(imagem.getName());
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		response.setContentType(contentType);
		response.setContentLength((int) imagem.length());

		// Copia a imagem para a saída da resposta
		try (FileInputStream in = new FileInputStream(imagem); ServletOutputStream out = response.getOutputStream()) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush(); // 👈 Isso garante que a imagem seja renderizada
		}
	}
}

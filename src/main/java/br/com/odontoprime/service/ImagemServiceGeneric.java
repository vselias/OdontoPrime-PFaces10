package br.com.odontoprime.service;

import java.io.File;

import javax.faces.application.FacesMessage;
import javax.imageio.stream.FileImageOutputStream;

import br.com.odontoprime.entidade.Constantes;
import br.com.odontoprime.util.MensagemUtil;

public class ImagemServiceGeneric {
	public String gerarNomeImagemAleatoria() {
		return System.currentTimeMillis() + ".png";
	}
	private String nomeImagem;
	public boolean criarArquivo(String caminho, byte[] dados, String nomeImagem) {

		FileImageOutputStream fileImageOutputStream;
		if (dados != null) {

			try {
				File diretorio = new File(caminho);
				if (!diretorio.exists()) {
					diretorio.mkdirs();
				}
				fileImageOutputStream = new FileImageOutputStream(new File(caminho, nomeImagem));
				fileImageOutputStream.write(dados, 0, dados.length);
				fileImageOutputStream.close();
				System.out.println("[criarArquivo] - Arquivo criado com sucesso.");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[criarArquivo] - Erro ao criar arquivo.");
			}
		}
		return false;
	}

	public void salvarImagem(byte[] byteImg, String nomeImagem) {
		boolean imagemSalva = false;
		try {

			imagemSalva = criarArquivo(Constantes.CAMINHO_IMAGEM, byteImg, nomeImagem);
			if (imagemSalva) {
				MensagemUtil.enviarMensagem("Imagem salva com sucesso!", FacesMessage.SEVERITY_INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[salvarImagem] erro ao salvar imagem.");
		}
	}

	public String salvarImagemRecortada(byte[] byteImg) {
		boolean imagemSalva = false;
	
		try {
			if (byteImg == null) {
				throw new Exception("Imagem não informada! bytes nulos");
			}
			nomeImagem = gerarNomeImagemAleatoria();
			imagemSalva = criarArquivo(Constantes.CAMINHO_IMAGEM, byteImg, nomeImagem);

			if (imagemSalva) {
				System.out.println("[salvarImagemRecotada] imagem recortada salva com sucesso.");
				MensagemUtil.enviarMensagem("Imagem salva com sucesso!", FacesMessage.SEVERITY_INFO);
				return nomeImagem;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[salvarImagemRecotada] erro ao salvar imagem recortada."+ e.getMessage());
		}
		return "";
	}

}

package br.com.odontoprime.service;

import java.io.File;
import java.io.FileNotFoundException;

import javax.faces.application.FacesMessage;
import javax.imageio.stream.FileImageOutputStream;

import org.primefaces.model.CroppedImage;

import br.com.odontoprime.entidade.Paciente;
import br.com.odontoprime.util.MensagemUtil;

public interface ImagemServiceInt<T> {

	default boolean criarArquivo(String caminho, byte[] dados, String nomeImagem) {

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

	@Deprecated
	public void salvarImagem(T classe);

	public boolean recortarImagem(T classe, CroppedImage croppedImage);

	public boolean salvarImagemRecortada(CroppedImage croppedImage, T classe);

	public boolean subirImagem(T classe, byte[] dados);

	@Deprecated
	public boolean tirarFotoWebCam(byte[] dados, T classe);
}

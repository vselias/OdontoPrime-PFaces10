package br.com.odontoprime.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.primefaces.model.CroppedImage;

import br.com.odontoprime.dao.UsuarioDAO;
import br.com.odontoprime.entidade.Constantes;
import br.com.odontoprime.entidade.Usuario;
import br.com.odontoprime.util.MensagemUtil;

@SuppressWarnings("serial")
public class ImagemService implements Serializable, ImagemServiceInt<Usuario> {
	@Inject
	private UsuarioDAO usuarioDAO;
	private String nomeImagem;


	public String gerarNomeImagemAleatoria() {
		return System.currentTimeMillis() + ".png";
	}

	public boolean recortarImagem(Usuario usuario, CroppedImage croppedImage) {
		boolean fotoRecortada = false;
		usuario.setNomeImagemRecortada(gerarNomeImagemAleatoria());

		if (croppedImage == null) {
			return false;
		}

		try {
			fotoRecortada = criarArquivo(Constantes.getPathServidor(), croppedImage.getBytes(), usuario.getNomeImagemRecortada());
			if (fotoRecortada) {
				MensagemUtil.enviarMensagem("Imagem recortada com sucesso.", FacesMessage.SEVERITY_INFO);
				System.out.println("[ImagemService - recortarImagem] imagem recortada com sucesso.");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[ImagemService - recortarImagem] erro ao recortar imagem.");
		}

		return false;
	}

	public boolean salvarImagemRecortada(CroppedImage croppedImage, Usuario usuario) {
		boolean imagemSalva = false;
		try {

			if (croppedImage == null || croppedImage.getBytes() == null) {
				return false;
			}
			usuario.setNomeImagem(gerarNomeImagemAleatoria());
			imagemSalva = criarArquivo(Constantes.CAMINHO_IMAGEM, croppedImage.getBytes(), usuario.getNomeImagem());

			if (imagemSalva) {
				System.out.println("[salvarImagemRecotada] imagem recortada salva com sucesso.");
				MensagemUtil.enviarMensagem("Imagem salva com sucesso!", FacesMessage.SEVERITY_INFO);
				usuarioDAO.salvar(usuario);
				return imagemSalva;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[salvarImagemRecotada] erro ao salvar imagem recortada.");
		}

		return imagemSalva;

	}

	public void salvarImagem(Usuario usuario) {
		boolean imagemSalva = false;
		try {
			usuario.setNomeImagem(gerarNomeImagemAleatoria());
			imagemSalva = criarArquivo(Constantes.CAMINHO_IMAGEM, usuario.getByteFoto(), usuario.getNomeImagem());

			if (imagemSalva) {
				MensagemUtil.enviarMensagem("Imagem salva com sucesso!", FacesMessage.SEVERITY_INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[salvarImagem] erro ao salvar imagem.");
		}
	}

	public boolean reduzirSalvarImagemUser(String caminho, byte[] dados, String nomeImagem) {
		try {
			if (dados == null || dados.length == 0) {
				System.out.println("[criarArquivo] - Dados inválidos!");
				return false;
			}

			BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(dados));
			if (originalImage == null) {
				System.out.println("[criarArquivo] - Falha ao carregar imagem.");
				return false;
			}
			File pastaImg = new File(caminho);
			if (!pastaImg.exists())
				pastaImg.mkdirs();

			int larguraOriginal = originalImage.getWidth();
			int alturaOriginal = originalImage.getHeight();

			// Calculando a proporção
			double proporcaoLargura = (double) 600 / larguraOriginal;
			double proporcaoAltura = (double) 350 / alturaOriginal;

			// Escolhendo a menor proporção para evitar distorção
			double proporcaoFinal = Math.min(proporcaoLargura, proporcaoAltura);

			int novaLargura = (int) (larguraOriginal * proporcaoFinal);
			int novaAltura = (int) (alturaOriginal * proporcaoFinal);

			// Definir tipo de imagem considerando transparência
			int tipoImagem = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
			BufferedImage resizedImage = new BufferedImage(novaLargura, novaAltura, tipoImagem);

			// Redimensionando a imagem
			Graphics2D g2d = resizedImage.createGraphics();
			g2d.drawImage(originalImage.getScaledInstance(novaLargura, novaAltura, Image.SCALE_SMOOTH), 0, 0, null);
			g2d.dispose();

			// Obtendo formato do arquivo
			String formato = nomeImagem.substring(nomeImagem.lastIndexOf('.') + 1).toLowerCase();
			File outputFile = new File(caminho, nomeImagem);

			ImageIO.write(resizedImage, formato, outputFile);
			System.out.println("[criarArquivo] - Arquivo criado com sucesso.");
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[criarArquivo] - Erro ao criar arquivo.");
		}

		return false;
	}

	public boolean tirarFoto(byte[] dados, Usuario usuario) {
		boolean imagemSalva = false;
		try {
			usuario.setNomeImagemCropper(gerarNomeImagemAleatoria());
			imagemSalva = criarArquivo(Constantes.getPathServidor(), dados, usuario.getNomeImagemCropper());

			if (imagemSalva) {
				MensagemUtil.enviarMensagem("Foto capturada com sucesso!", FacesMessage.SEVERITY_INFO);
				return imagemSalva;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[tirarFoto] erro ao capturar imagem.");
		}
		return imagemSalva;
	}

	public boolean subirImagem(Usuario usuario, byte[] dados) {

		boolean fotoTirada = false;
		try {

			nomeImagem = gerarNomeImagemAleatoria();
			fotoTirada = reduzirSalvarImagemUser(Constantes.getPathServidor(), dados, nomeImagem);
			if (fotoTirada) {
				if (usuario == null) {
					usuario = new Usuario();
				}
				usuario.setNomeImagemCropper(nomeImagem);
				MensagemUtil.enviarMensagem("Foto enviada com sucesso!", FacesMessage.SEVERITY_INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[enviarFotoServidor] erro ao enviar imagem.");
		}
		return fotoTirada;
	}

	@Override
	public boolean tirarFotoWebCam(byte[] dados, Usuario classe) {
		// TODO Auto-generated method stub
		return false;
	}
}

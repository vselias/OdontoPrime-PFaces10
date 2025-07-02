package br.com.odontoprime.entidade;

public enum FormaPagamento {
	VISA("Visa", "visa.png"), MASTERCARD("MasterCard", "mastercard.png"), ELO("Elo", "elo.png"),
	AMERICAN_EXPRESS("American Express", "american.png"), HIPERCARD("HiperCard", "hipercard.png"),
	NOTA_PROMISSORIA("Nota Promissória", "boleto.png");

	private String descricao;
	private String img;

	public String getDescricao() {
		return descricao;
	}

	public String getImg() {
		return img;
	}

	private FormaPagamento(String descricao, String img) {
		this.descricao = descricao;
		this.img = img;
	}

}

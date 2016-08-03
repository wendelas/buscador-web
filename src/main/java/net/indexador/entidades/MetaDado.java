package net.indexador.entidades;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "campo", "fontedados_id" }) })
public class MetaDado {
	private Integer id;
	private FonteDados fonteDados;
	private String campo;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "fontedados_id")
	public FonteDados getFonteDados() {
		return fonteDados;
	}

	public void setFonteDados(FonteDados fonteDados) {
		this.fonteDados = fonteDados;
	}

	public String getCampo() {
		return campo;
	}

	public void setCampo(String campo) {
		this.campo = campo;
	}
}

package net.indexador.entidades;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class AnexoFonteDados {
    private Integer id;
    private FonteDados fonteDados;
    private String nomeArquivo;
    private byte[] anexo;
    private Timestamp dataEnvio;
    private long tamanho;
    private String separador;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
	return id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    public FonteDados getFonteDados() {
	return fonteDados;
    }

    public void setFonteDados(FonteDados fonteDados) {
	this.fonteDados = fonteDados;
    }

    @Lob
    public byte[] getAnexo() {
	return anexo;
    }

    public void setAnexo(byte[] anexo) {
	this.anexo = anexo;
    }

    public void setNomeArquivo(String nomeArquivo) {
	this.nomeArquivo = nomeArquivo;
    }

    public String getNomeArquivo() {
	return nomeArquivo;
    }

    public void setTamanho(long size) {
	this.tamanho = size;
    }

    public long getTamanho() {
	return tamanho;
    }

    public void setDataEnvio(Timestamp dataEnvio) {
	this.dataEnvio = dataEnvio;
    }

    public Timestamp getDataEnvio() {
	return dataEnvio;
    }

    public void setSeparador(String separador) {
	this.separador = separador;
    }

    public String getSeparador() {
	return separador;
    }
}

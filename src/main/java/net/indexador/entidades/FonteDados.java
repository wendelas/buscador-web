package net.indexador.entidades;

import java.util.*;

import javax.persistence.*;

@Entity
public class FonteDados {
    private Integer id;
    private String query;
    private String dataSource;
    private String url;
    private String usuario;
    private String password;
    private String diretorio;
    private String nome;
    private String nomeDriver;
    private List<MetaDado> metadados;
    private List<AnexoFonteDados> anexos;
    private byte[] dicionario;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
	return id;
    }

    public String getQuery() {
	return query;
    }

    public void setQuery(String query) {
	this.query = query;
    }

    public String getDataSource() {
	return dataSource;
    }

    public void setDataSource(String dataSource) {
	this.dataSource = dataSource;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public void setNome(String nome) {
	this.nome = nome;
    }

    @Column(unique = true, nullable = false)
    public String getNome() {
	return nome;
    }

    public void setDiretorio(String diretorio) {
	this.diretorio = diretorio;
    }

    public String getDiretorio() {
	return diretorio;
    }

    public void setNomeDriver(String nomeDriver) {
	this.nomeDriver = nomeDriver;
    }

    public String getNomeDriver() {
	return nomeDriver;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    @Transient
    public String getDiretorioIndice() {
	return System.getProperty("user.home") + "/dados/indices/" + getNome();
    }

    public void setMetadados(List<MetaDado> metadados) {
	this.metadados = metadados;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "fonteDados", cascade = CascadeType.ALL)
    public List<MetaDado> getMetadados() {
	return metadados;
    }

    public void setAnexos(List<AnexoFonteDados> anexos) {
	this.anexos = anexos;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fonteDados", cascade = CascadeType.ALL)
    public List<AnexoFonteDados> getAnexos() {
	return anexos;
    }

    @Lob
    public void setDicionario(byte[] dicionario) {
	this.dicionario = dicionario;
    }

    public byte[] getDicionario() {
	return dicionario;
    }

}

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

	@Column(unique = true)
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
		return System.getProperty("user.home") + "/indices/" + getNome();
	}

	public void setMetadados(List<MetaDado> metadados) {
		this.metadados = metadados;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fonte")
	public List<MetaDado> getMetadados() {
		return metadados;
	}

}

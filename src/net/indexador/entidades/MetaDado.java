package net.indexador.entidades;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"campo", "id"})})
public class MetaDado {
  private Integer id;
  private FonteDados fonte;
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
  public FonteDados getFonte() {
    return fonte;
  }

  public void setFonte(FonteDados fonte) {
    this.fonte = fonte;
  }

  public String getCampo() {
    return campo;
  }

  public void setCampo(String campo) {
    this.campo = campo;
  }
}

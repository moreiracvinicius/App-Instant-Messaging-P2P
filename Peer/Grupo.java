import java.util.ArrayList;
import java.util.List;

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: Grupo
* Funcao...........: Modela os grupos
************************************************************************************************ */
public class Grupo {
  private String nome;
  private List<String> mensagens;

  public Grupo(String nome) {
    this.nome = nome;
    mensagens = new ArrayList<>();
  } // fim construtor

  public String getNome() {
    return nome;
  } // fim getNome

  public List<String> getMensagens() {
    return mensagens;
  } // fim getMensagens
} // fim class

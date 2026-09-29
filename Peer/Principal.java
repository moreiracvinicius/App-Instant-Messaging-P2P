import javafx.application.Application;
import javafx.stage.Stage;

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 01/07 comeco
* Nome.............: Principal
* Funcao...........: Chamar a tela inicial do programa
************************************************************************************************ */
public class Principal extends Application{
  public static Stage stage;
  public static void main(String[] args) throws Exception {
    launch(args);
  } // fim main

  @Override
  public void start(Stage stage) throws Exception {
    Principal.stage = stage;
    new TelaLogin(stage);
  } // fim start
}// fim class
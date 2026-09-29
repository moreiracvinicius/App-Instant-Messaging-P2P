import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: TelaInicial
* Funcao...........: Gerencia a GUI da tela inicial
************************************************************************************************ */
public class TelaInicial {
  private static Stage stage;
  private Scene scene;
  private AnchorPane pane;
  private ScrollPane paneGrupos;
  private ScrollPane paneChat;
  private AnchorPane paneNavGrupos;
  private AnchorPane paneNavChat;
  private AnchorPane paneBottomChat;
  private VBox vBoxGrupos;
  private VBox vBoxChat;
  private HBox hBoxPane;
  private HBox hBoxBottomChat;
  private VBox vBoxMensagens;
  private Button novoGrupo;
  private Button sair;
  private Button enviar;
  private List<Button> buttonGrupos;
  private TextField novaMensagem;
  private Label grupos;
  private Label nomeGrupo;
  private VBox vBoxGruposAdicionados;
  private final int ESPACAMENTO = 10;
  
  public TelaInicial(Stage stage) {
    TelaInicial.stage = stage;
    Controller.ouvinteMensagem(this);
    setScene();
  } // fim construtor

  private void setScene() {
    iniciarComponentes();
    iniciarOuvintes();
    stage.setScene(scene);
    stage.setResizable(false);
    stage.setTitle("Tela Inicial");
    stage.show();
    iniciarLayout();
  } // fim setScene

  private void iniciarComponentes() {
    pane = new AnchorPane();
    pane.setPrefSize(600, 500);
    paneGrupos = new ScrollPane();
    paneGrupos.setPrefSize(200, 450);
    paneGrupos.setHbarPolicy(ScrollBarPolicy.NEVER);
    paneChat = new ScrollPane();
    paneChat.setPrefSize(200, 400);
    paneChat.setHbarPolicy(ScrollBarPolicy.NEVER);
    paneNavGrupos = new AnchorPane();
    paneNavGrupos.setPrefSize(200, 50);
    paneNavChat = new AnchorPane();
    paneNavChat.setPrefSize(400, 50);
    paneBottomChat = new AnchorPane();
    paneBottomChat.setPrefSize(400, 50);
    novoGrupo = new Button("Novo grupo");
    sair = new Button("Sair do grupo");
    enviar = new Button("Enviar");
    enviar.setDisable(true);
    buttonGrupos = new ArrayList<>();
    novaMensagem = new TextField();
    novaMensagem.setPromptText("Nova mensagem");
    grupos = new Label("Grupos");
    nomeGrupo = new Label();
    paneNavGrupos.getChildren().addAll(grupos, novoGrupo);
    paneNavChat.getChildren().addAll(nomeGrupo, sair);
    vBoxGruposAdicionados = new VBox();
    paneGrupos.setContent(vBoxGruposAdicionados);
    vBoxGrupos = new VBox();
    vBoxGrupos.getChildren().addAll(paneNavGrupos, paneGrupos);
    vBoxChat = new VBox();
    vBoxMensagens = new VBox();
    hBoxBottomChat = new HBox();
    hBoxBottomChat.getChildren().addAll(novaMensagem, enviar);
    paneBottomChat.getChildren().addAll(hBoxBottomChat);
    paneChat.setContent(vBoxMensagens);
    vBoxChat.getChildren().addAll(paneNavChat, paneChat, paneBottomChat);
    hBoxPane = new HBox();
    hBoxPane.getChildren().addAll(vBoxGrupos, vBoxChat);
    pane.getChildren().add(hBoxPane);
    scene = new Scene(pane);
  } // fim iniciarComponentes
  
  private void iniciarOuvintes() {
    // botao para criar grupo
    novoGrupo.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        novoGrupo();
      }
    });
    // botao para sair de grupo
    sair.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        sairDoGrupo();
      }
    });
    // botao para enviar mensagem
    enviar.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        enviarMensagem();
      }
    });
    stage.setOnCloseRequest(e -> fecharPrograma());
  }// fim iniciarOuvintes

  /************************************************************************************************
  * Metodo: novoGrupo
  * Funcao: Adiciona novo grupo na GUI
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  private void novoGrupo() {
    String nomeNovoGrupo = JOptionPane.showInputDialog("Nome do grupo:");
    if (nomeNovoGrupo.isEmpty()) { // verifica se o usuario deixou o campo vazio
      JOptionPane.showMessageDialog(null, "O grupo precisa de um nome!");
    } else {
      if (Controller.join(nomeNovoGrupo)) { // atualiza grupos se este for criado
        setGrupos();
      } else { // caso o usuario ja tenha um grupo com esse nome
        JOptionPane.showMessageDialog(null, "O nome deve ser único!");
      }
    }
  } // fim novoGrupo

  /************************************************************************************************
  * Metodo: setGrupos
  * Funcao: atualiza grupos na GUI
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  private void setGrupos() {
    vBoxGruposAdicionados.getChildren().clear();
    List<Grupo> auxGrupos = Controller.getGrupos();
    for (Grupo gp : auxGrupos) {
      Button button = new Button(gp.getNome());
      HBox hBox = new HBox();
      hBox.setPrefSize(200, 50);
      button.setMinSize(hBox.getPrefWidth(), hBox.getPrefHeight());
      hBox.getChildren().add(button);
      vBoxGruposAdicionados.getChildren().add(hBox);
      buttonGrupos.add(button);
      iniciarOuvintes(button);
    } // fim for
  } // fim setGrupos
  
  private void iniciarOuvintes(Button button) {
    // botoes que representam grupos
    button.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selecionaGrupo(button.getText());
      }
    });
  }// fim iniciarOuvintes

  /************************************************************************************************
  * Metodo: sairDoGrupo
  * Funcao: Sai e apaga grupo na GUI
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  private void sairDoGrupo() {
    Controller.leave();
    nomeGrupo.setText("");
    vBoxMensagens.getChildren().clear();
    paneChat.setContent(vBoxMensagens);
    setGrupos();
    enviar.setDisable(true);
  } // fim sairDoGrupo

  /************************************************************************************************
  * Metodo: enviarMensagem
  * Funcao: Envia mensagem do usuario e adiciona na GUI
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  private void enviarMensagem() {
    Controller.send(novaMensagem.getText());
    // adiciona propria mensagem na GUI
    Label label = new Label("(Você): " + novaMensagem.getText());
    vBoxMensagens.getChildren().add(label);
    novaMensagem.clear();
    setMensagens();
  } // fim enviarMensagem

  /************************************************************************************************
  * Metodo: setMensagens
  * Funcao: Atualiza mensagens na GUI
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  private void setMensagens() {
    String grupoAtivo = Controller.getGrupoAtivo();
    List<String> mensagens = Controller.buscarGrupo(grupoAtivo).getMensagens();
    vBoxMensagens.getChildren().clear();
    for (String msg : mensagens) {
      Label label = new Label("    " + msg);
      vBoxMensagens.getChildren().add(label);
    }
    paneChat.setContent(vBoxMensagens);
  } // fim setMensagens

  private void fecharPrograma() {
    Controller.leaveAll();
    stage.close();
    System.exit(0);
  }// fim fecharPrograma

  /************************************************************************************************
  * Metodo: selecionaGrupo
  * Funcao: marca selecao de grupo na GUI
  * Parametros: nomeGp = nome do grupo
  * Retorno: void
  ********************************************************************************************** */
  private void selecionaGrupo(String nomeGp) {
    Controller.grupoAtivo(nomeGp);
    nomeGrupo.setText(nomeGp);
    setMensagens();
    enviar.setDisable(false);
  } // fim selecionaGrupo
  
  private void iniciarLayout() {
    grupos.setLayoutX(ESPACAMENTO);
    grupos.setLayoutY((paneNavGrupos.getPrefHeight() - grupos.getHeight()) / 2);
    novoGrupo.setLayoutX((paneNavGrupos.getPrefWidth() - novoGrupo.getWidth()) - ESPACAMENTO);
    novoGrupo.setLayoutY((paneNavGrupos.getPrefHeight() - grupos.getHeight()) / 2);
    nomeGrupo.setLayoutX(ESPACAMENTO);
    nomeGrupo.setLayoutY((paneNavChat.getPrefHeight() - nomeGrupo.getHeight()) / 2);
    sair.setLayoutX((paneNavChat.getPrefWidth() - sair.getWidth()) - ESPACAMENTO);
    sair.setLayoutY((paneNavChat.getPrefHeight() - sair.getHeight()) / 2);
    enviar.setLayoutX((paneBottomChat.getPrefWidth() - enviar.getWidth()) - ESPACAMENTO);
    novaMensagem.setLayoutX(ESPACAMENTO);
    novaMensagem.setMinWidth(paneBottomChat.getPrefWidth() - enviar.getWidth() - 2*ESPACAMENTO);
    pane.requestFocus();
  } // fim iniciarLayout

  /************************************************************************************************
  * Metodo: mensagemRecebida
  * Funcao: Atualiza quando chega uma mensagem nova
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public void mensagemRecebida() {
    setMensagens();
  } // fim mensagemRecebida
} // fim class
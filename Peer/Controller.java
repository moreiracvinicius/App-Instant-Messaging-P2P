import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: Controller
* Funcao...........: Faz o login gerencia a GUI e APDUs
************************************************************************************************ */
public class Controller {
  private static String nomeDeUsuario;
  private static String IPServidor;
  private static String IP;
  private static List<Grupo> grupos;
  private static List<List<String>> ED; // ED que armazena nome dos grupos e seus clientes
  private static DateTimeFormatter horarioFormato;
  private static String grupoAtivo = "";
  private static TelaInicial telaInicial;
  private static volatile List<String> peers; // IPs do peers ativos

  /************************************************************************************************
  * Metodo: login
  * Funcao: Realiza o login na aplicacao
  * Parametros: nome = nome de usuario; IP = ip do servidor
  * Retorno: void
  ********************************************************************************************** */
  public static void login(String nome) {
    IP = "null";
    peers = new ArrayList<>();
    ED = new ArrayList<>();
    nomeDeUsuario = nome;
    grupos = new ArrayList<>();
    Peer.servidorUDP();
    Peer.servidorTCP();
    IPServidor = "255.255.255.255";
    try {
      Peer.clienteUDP("ME", IPServidor); // encontrar IP
      Thread.sleep(1000);
    } catch (Exception e) {
      e.getMessage();
    }
    //System.out.println(IP);
    hello();
    System.out.println(nomeDeUsuario + " fez o login");
    //new TelaTeste2();
  }// fim login

  /************************************************************************************************
  * Metodo: hello
  * Funcao: Envia broadcast em busca de servidor
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public static void hello() {
    String apdu = "HELLO";
    try {
      Peer.clienteUDP(apdu, IPServidor);
      Thread.sleep(3000);
    } catch (Exception e) {
      e.getMessage();
    }
  }// fim hello

  /************************************************************************************************
  * Metodo: join
  * Funcao: Envia as APDUs tipo JOIN
  * Parametros: nomeGrupo = grupo para qual sera enviado
  * Retorno: boolean
  ********************************************************************************************** */
  public static boolean join(String nomeGrupo) {
    if (buscarGrupo(nomeGrupo) != null) {
      return false;
    }
    String apdu = "JOIN&" + nomeDeUsuario + "&" + nomeGrupo;
    for (String p : peers) { // envia join para todos os peers
      try {
        Peer.clienteTCP(apdu, p);
      } catch (Exception e) {
        e.getMessage();
      }
    }
    tratamentoJoin(apdu + "&" + IP);
    grupos.add(new Grupo(nomeGrupo));
    return true;
  }// fim join

  /************************************************************************************************
  * Metodo: send
  * Funcao: Envia as APDUs tipo SEND
  * Parametros: mensagem = texto a ser enviado
  * Retorno: void
  ********************************************************************************************** */
  public static void send(String mensagem) {
    // [SEND]&[nomeUser]&[nomeGrupo]&[msg]
    Grupo grupo = buscarGrupo(grupoAtivo);
    String apdu = "SEND&" + nomeDeUsuario + "&" + grupo.getNome() + "&" + mensagem;
    int indexGrupo = buscarIDGrupoED(grupo.getNome()); // recebe o index do grupo da APDU 
    // encaminha para membros do mesmo grupo
    for (String clientes : ED.get(indexGrupo)) {
      if (!clientes.equals(grupo.getNome())) {
        // adiciona o IP ah lista se for diferente do nome do grupo
        String[] cliente = clientes.split("@");
        //IPClientes.add(cliente[1]);
        try {
          Peer.clienteUDP(apdu, cliente[1].trim());
        } catch (Exception e) {
          e.getMessage();
        }
      } // fim if
    } // fim for
    //grupo.getMensagens().add("(Você): " + mensagem);
  } // fim send

  /************************************************************************************************
  * Metodo: leave
  * Funcao: Envia as APDUs tipo LEAVE e sai do grupo no cliente
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public static void leave() {
    Grupo grupo = buscarGrupo(grupoAtivo);
    grupoAtivo = "";
    String apdu = "LEAVE&" + nomeDeUsuario + "&" + grupo.getNome();
    for (String p : peers) { // envia join para todos os peers
      try {
        //String apduGeral = converteApduTcp(apdu);
        Peer.clienteTCP(apdu, p);
      } catch (Exception e) {
        e.getMessage();
      }
    }
    tratamentoLeave(apdu + "&" + IP);
    grupos.remove(grupo);    
  } // fim leave

  /************************************************************************************************
  * Metodo: leaveAll
  * Funcao: Envia as APDUs tipo LEAVE para todos os grupos do cliente
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public static void leaveAll() {
    for (Grupo grupo : grupos) {
      String apdu = "LEAVE&" + nomeDeUsuario + "&" + grupo.getNome();
      for (String p : peers) { // envia join para todos os peers
        try {
          //String apduGeral = converteApduTcp(apdu);
          Peer.clienteTCP(apdu, p);
        } catch (Exception e) {
          e.getMessage();
        }
      }
      tratamentoLeave(apdu + "&" + IP);
    }
  } // fim leave

  /************************************************************************************************
  * Metodo: buscarGrupo
  * Funcao: Pesquisa grupo pelo nome na lista
  * Parametros: nome = nome do grupo
  * Retorno: Grupo
  ********************************************************************************************** */
  public static Grupo buscarGrupo(String nome) {
    int indexGrupo = 0;
    for (Grupo grupo : grupos) {
      if (grupo.getNome().equals(nome)) {
        break;
      }
      indexGrupo++;
    }
    if (indexGrupo == grupos.size()) { // grupo nao esta lista
      return null;
    }
    return grupos.get(indexGrupo);
  } // fim buscarGrupo

  /************************************************************************************************
  * Metodo: tratarMensagemUDP
  * Funcao: Trata as mensagens enviadas pelo servidor
  * Parametros: mensagem = APDU SEND
  * Retorno: void
  ********************************************************************************************** */
  public static void tratarMensagemUDP(String mensagem) {
    //APDU&...
    String[] apdu = mensagem.split("&");
    //System.out.println(apdu[0]);
    if (apdu[0].equals("SEND")) {
      tratarSend(mensagem);
    } else if (apdu[0].trim().equals("HELLO")) {
      tratarHello(mensagem);
    } else if (apdu[0].trim().equals("ME") && IP.equals("null")) {
      //System.out.println("Para tratar");
      tratarMe(mensagem);
    }
    System.out.println(ED.toString());
  } // fim tratarMensagemUDP

  /************************************************************************************************
  * Metodo: tratarHello
  * Funcao: Trata as mensagens enviadas pelo servidor
  * Parametros: mensagem = APDU SEND
  * Retorno: void
  ********************************************************************************************** */
  public static void tratarHello(String mensagem) {
    //HELLO&IP ou  HELLO&ANS&IP
    String[] apdu = mensagem.split("&");
    if (!apdu[1].trim().equals("ANS") && !apdu[1].trim().equals(IP)) {
      try { // retorna o hello
        Peer.clienteUDP("HELLO&ANS", apdu[1].trim());
        Thread.sleep(3000);
      } catch (Exception e) {
        e.getMessage();
      }
      peers.add(apdu[1].trim());
      System.out.println("Peers:");
      System.out.println(peers.toString());
    } else if (apdu[1].trim().equals("ANS")) {
      peers.add(apdu[2].trim());
      System.out.println("Peers:");
      System.out.println(peers.toString());
    }
  } // fim tratarMensagemUDP

  /************************************************************************************************
  * Metodo: tratarSend
  * Funcao: Trata as mensagens do tipo SEND
  * Parametros: mensagem = APDU SEND
  * Retorno: void
  ********************************************************************************************** */
  public static void tratarSend(String mensagem) {
    String[] apdu = mensagem.split("&");
    //[SEND][nomeUser][nomeGrupo][msg][IP]
    int indexGrupo = buscarIDGrupo(apdu[2]);
    grupos.get(indexGrupo).getMensagens().add("(" + apdu[1] + "): " + apdu[3]);
    Platform.runLater(() -> {
      telaInicial.mensagemRecebida();
    });
  } // fim tratarSend

  /************************************************************************************************
  * Metodo: tratarMe
  * Funcao: Trata as mensagens que chegam da APDU ME
  * Parametros: mensagem = APDU ME
  * Retorno: void
  ********************************************************************************************** */
  public static void tratarMe(String mensagem) {
    // [ME]&[IP]
    //System.out.println(mensagem);
    String[] apdu = mensagem.split("&"); // divide os campos da APDU pela flag
    // log de encontro de proprio IP
    IP = apdu[1].trim();
  } // fim tratarMe

  /************************************************************************************************
  * Metodo: tratarMensagemTCP
  * Funcao: Encaminha a msg para o tratamento de JOIN ou LEAVE
  * Parametros: mensagem = APDU JOIN ou LEAVE
  * Retorno: void
  ********************************************************************************************** */
  public static void tratarMensagemTCP(String mensagem) {
    // JOIN/LEAVE&nomeUser&nomeGrupo
    // 0 - JOIN/LEAVE
    // 1 - nomeUser
    // 2 - nomeGrupo
    String[] apdu = mensagem.split("&"); // divide a APDU
    if (apdu[0].equals("JOIN")) { // se for JOIN
      tratamentoJoin(mensagem);
    } else if (apdu[0].equals("LEAVE")) { // se eh do tipo LEAVE
      tratamentoLeave(mensagem);
    } // fim if else
    System.out.println(ED.toString());
  } // fim tratarMensagemTCP

  /************************************************************************************************
  * Metodo: tratamentoJoin
  * Funcao: Trata mensagens do tipo JOIN
  * Parametros: mensagem = APDU JOIN 
  * Retorno: void
  ********************************************************************************************** */
  public static void tratamentoJoin(String mensagem) {
    //System.out.println(mensagem);
    String[] apdu = mensagem.split("&"); // divide a APDU
    // [JOIN][nome][grupo][IP]
    int indexGrupo = 0;
      if (!ED.isEmpty()) { // se a lista nao estiver vazia
        for (List<String> grupo : ED) {
          if (grupo.get(0).equals(apdu[2])) {
            break;
          } // fim if
          indexGrupo++; // caso o grupo nao esteja na lista o contador ficara maior que o tamanho
        } // fim for
      } // fim if
      if (indexGrupo == ED.size()) { // se o grupo estah na lista
        List<String> novoGrupo = new ArrayList<>(); // cria nova lista ou seja grupo
        novoGrupo.add(apdu[2]); // o nome do grupo eh adicionado como primeiro elemento da lista
        ED.add(novoGrupo); // o grupo eh adicionado a lista de grupos
        // log criacao de grupo
      } 
      ED.get(indexGrupo).add(apdu[1] + "@" + apdu[3]); // adiciona o cliente ao grupo
  } // fim tratamentoJoin

  /************************************************************************************************
  * Metodo: tratamentoLeave
  * Funcao: Trata mensagens do tipo LEAVE
  * Parametros: mensagem = APDU LEAVE
  * Retorno: void
  ********************************************************************************************** */
  public static void tratamentoLeave(String mensagem) {
    String[] apdu = mensagem.split("&"); // divide a APDU
    // [LEAVE][nome][grupo][IP]
    int indexGrupo = buscarIDGrupoED(apdu[2]); // busca o index do grupo
    ED.get(indexGrupo).remove(apdu[1] + "@" + apdu[3]); // remove o cliente do grupo
    if (ED.get(indexGrupo).size() == 1) { // verifica se nao a mais clientes no grupo
      ED.remove(indexGrupo); // remove o grupo da lista
    } // fim if
  } // fim tratamentoLeave

  /************************************************************************************************
  * Metodo: buscaIDGrupo
  * Funcao: Pesquisa grupo pelo nome na lista
  * Parametros: nome = nome do grupo
  * Retorno: int
  ********************************************************************************************** */
  public static int buscarIDGrupo(String nome) {
    int indexGrupo = 0;
    for (Grupo grupo : grupos) {
      if (grupo.getNome().equals(nome)) {
        break;
      }
      indexGrupo++;
    }
    return indexGrupo;
  } // fim buscarGrupo

  /************************************************************************************************
  * Metodo: buscarIDGrupoED
  * Funcao: retorna o ID do grupo buscando se pelo nome
  * Parametros: nome = nome do grupo pesquisado
  * Retorno: int
  ********************************************************************************************** */
  public static int buscarIDGrupoED(String nome) {
    int indexGrupo = 0;
    for (List<String> grupo : ED) {
      if (grupo.get(0).equals(nome)) {
        break; // para quando encontra o grupo buscado
      }
      indexGrupo++;
    }
    return indexGrupo;
  } // fim buscarGrupoED

  /************************************************************************************************
  * Metodo: ouvinteMensagem
  * Funcao: recebe referencia da tela
  * Parametros: tela = referencia da tela
  * Retorno: void
  ********************************************************************************************** */
  public static void ouvinteMensagem(TelaInicial tela) {
    Controller.telaInicial = tela;
  } // fim ouvinteMensagem

  /************************************************************************************************
  * Metodo: grupoAtivo
  * Funcao: Verifica grupo selecionado pelo usuario
  * Parametros: gpAtivo = nome do grupo
  * Retorno: void
  ********************************************************************************************** */
  public static void grupoAtivo(String gpAtivo) {
    grupoAtivo = gpAtivo; 
  } // fim grupoAtivo

  public static List<Grupo> getGrupos() {
    return grupos;
  } // fim getGrupos

  public static String getGrupoAtivo() {
    return grupoAtivo;
  } // fim getGrupoAtivo
}// fim class

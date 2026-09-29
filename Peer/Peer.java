import java.io.*;
import java.net.*;

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: Peer
* Funcao...........: Envia e recebe e trata as APDUs
************************************************************************************************ */
public class Peer {
  private static int portaRemota = 6789;
  
  /************************************************************************************************
  * Metodo: clienteTCP
  * Funcao: Envia as APDUs do tipo JOIN e LEAVE
  * Parametros: mensagem = APDU; IP = ip para ser enviado
  * Retorno: void
  ********************************************************************************************** */
  public static void clienteTCP(String mensagem, String IP) throws Exception {
    InetAddress ipServidor = InetAddress.getByName(IP);
    Socket cliente = new Socket(ipServidor, portaRemota);
    BufferedWriter saida = new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream()));
    saida.write(mensagem);
    saida.newLine();
    saida.flush();
    cliente.close();
  } // fim clienteTCP

  /************************************************************************************************
  * Metodo: servidorUDP
  * Funcao: Inicia a thread de servidor UDP
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public static void servidorUDP() {
    ServidorUDP servidorUDP = new ServidorUDP();
    servidorUDP.start();
  }// fim servidorUDP

  /************************************************************************************************
  * Metodo: servidorTCP
  * Funcao: inicia a thread do servidor TCP
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public static void servidorTCP() {
    ServidorTCP servidorTCP = new ServidorTCP();
    servidorTCP.start();
  }// fim servidorTCP

  /************************************************************************************************
  * Metodo: clienteUDP
  * Funcao: Envia as APDUs via datagrama
  * Parametros: 
  * Retorno: void
  ********************************************************************************************** */
  public static void clienteUDP(String mensagem, String IP) throws Exception {
    InetAddress ipServidor = InetAddress.getByName(IP);
	  DatagramSocket clienteSocket = new DatagramSocket();
    if (IP.equals("255.255.255.255")) {
      clienteSocket.setBroadcast(true);
    }
    byte[] dadosEnviados = new byte[1024];
    dadosEnviados = mensagem.getBytes();
    DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados,
                                                          dadosEnviados.length,
                                                          ipServidor,
                                                          portaRemota);
    clienteSocket.send(datagramaEnviado);
    clienteSocket.close();
  } // fim clienteUDP
} // fim class

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: Servidor UDP
* Funcao...........: Mantem um loop que recebe as mensagens do servidor
************************************************************************************************ */
class ServidorUDP extends Thread {
  public void run() {
    try {
      int portaLocal = 6789;
      DatagramSocket servidor;
      while(true){
        servidor = new DatagramSocket(portaLocal);
        byte[] dadosRecebidos = new byte[1024];
        DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos,
		                                                        dadosRecebidos.length);
        servidor.receive(pacoteRecebido);
        String mensagem = new String(pacoteRecebido.getData());
        mensagem += "&" + pacoteRecebido.getAddress().getHostAddress(); // Ip do transmissor
        System.out.println(mensagem); // send recebido pelo cliente
        TratamentoUDP tratamento = new TratamentoUDP(mensagem);
        tratamento.start();
        servidor.close();
	    } // fim while
    } catch (Exception e) {
      e.getMessage();
    } // fim try catch
  } // fim run
}// fim class

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: TratamentoUDP
* Funcao...........: Trata as mensagens do UDP em outra thread
************************************************************************************************ */
class TratamentoUDP extends Thread {
  private String mensagem;
  public TratamentoUDP(String mensagem) {
    this.mensagem = mensagem;
  } // fim construtor

  public void run() {
    //String apdu = converteApduUdp(mensagem);
    Controller.tratarMensagemUDP(mensagem);
  } // fim run
} // fim class

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: ServidorTCP
* Funcao...........: Mantem o loop do servidor TCP
************************************************************************************************ */
class ServidorTCP extends Thread {
  public void run() {
    try {
      int portaLocal = 6789;
      ServerSocket servidor;
      servidor = new ServerSocket(portaLocal);
      while (true) {
        Socket conexao;
        conexao = servidor.accept();
        BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
        String mensagem = entrada.readLine();
        mensagem += "&" + conexao.getInetAddress().getHostAddress();
        //System.out.println(mensagem); apdu sem tratamento
        TratamentoTCP tratamento = new TratamentoTCP(mensagem);
        tratamento.start();
        //servidor.close(); 
      }// fim while
    } catch (Exception e) {
      e.getMessage();
    } // fim try catch
  } // fim run
}// fim class

/* ************************************************************************************************
* Autor............: Vinicius Castro Moreira
* Matricula........: 202310357
* Inicio...........: 01/07
* Ultima alteracao.: 05/07 
* Nome.............: TratamentoTCP
* Funcao...........: Trata as mensagens enviadas pelo TCP em outra thread
************************************************************************************************ */
class TratamentoTCP extends Thread {
  private String mensagem;
  public TratamentoTCP(String mensagem) {
    this.mensagem = mensagem;
  } // fim construtor

  public void run() {
    Controller.tratarMensagemTCP(mensagem);
  } // fim run
} // fim class
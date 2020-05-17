

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @file TCP_Server.java
 * @date Feb 17, 2020 , 12:28:55
 * @author Muhammet Alkan
 */
public class TCP_Server {

    private ServerSocket serverSocket;
    private javax.swing.JTextPane historyJTextPane;
    private Thread serverThread;
    private ArrayList<ListenThread> lThreads = new ArrayList();
    private HashSet<ObjectOutputStream> allClients = new HashSet<>();
    private HashSet<ObjectOutputStream> kolayClients = new HashSet<>();
    private HashSet<ObjectOutputStream> ortaClients = new HashSet<>();
    private HashSet<ObjectOutputStream> zorClients = new HashSet<>();
    

    protected void start(int port, javax.swing.JTextPane jTextPaneHistory) throws IOException {
        // server soketi oluşturma (sadece port numarası)
        serverSocket = new ServerSocket(port);
        System.out.println("Server başlatıldı ..");

        // server arayüzündeki history alanı, bütün olaylar buraya yazılacak
        this.historyJTextPane = jTextPaneHistory;

        // arayüzü kitlememek için, server yeni client bağlantılarını ayrı Thread'de beklemeli
        serverThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    // blocking call, yeni bir client bağlantısı bekler
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Yeni bir client bağlandı : " + clientSocket);

                    // bağlanan her client için bir thread oluşturup dinlemeyi başlat
                    ListenThread a = new ListenThread(clientSocket);
                    lThreads.add(a);
                    a.start();
                } catch (IOException ex) {
                    System.out.println("Hata - new Thread() : " + ex);
                    break;
                }
            }
        });
        serverThread.start();
    }

    protected void sendBroadcast(String message) throws IOException {
        // bütün bağlı client'lara mesaj gönder
        for (ObjectOutputStream output : allClients) {
            output.writeObject("Server : " + message);
        }
    }

    protected void writeToHistory(String message) {
        // server arayüzündeki history alanına mesajı yaz
        historyJTextPane.setText(historyJTextPane.getText() + "\n" + message);
    }

    protected void stop() throws IOException {
        // bütün streamleri ve soketleri kapat
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    class ListenThread extends Thread {

        // dinleyeceğimiz client'ın soket nesnesi, input ve output stream'leri
        private final Socket clientSocket;
        private ObjectInputStream clientInput;
        private ObjectOutputStream clientOutput;
        private QuestionThread qt;
        
        private ListenThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            writeToHistory("Bağlanan client için thread oluşturuldu : " + this.getName());

            try {
                // input  : client'dan gelen mesajları okumak için
                // output : server'a bağlı olan client'a mesaj göndermek için
                clientInput = new ObjectInputStream(clientSocket.getInputStream());
                clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());


                // broadcast için, yeni gelen client'ın output stream'ını listeye ekler
                allClients.add(clientOutput);

                // client ismini mesaj olarak gönder
                clientOutput.writeObject("@id-" + this.getName());

                Object mesaj;
                // client mesaj gönderdiği sürece mesajı al
                while ((mesaj = clientInput.readObject()) != null) {
                    // client'in gönderdiği mesajı server ekranına yaz
                    writeToHistory(this.getName() + " : " + mesaj);
                    System.out.println(mesaj);
                    if(mesaj instanceof String && ((String)mesaj).contains("@zorluk-")){
                    switch (Integer.parseInt(((String)mesaj).charAt(8)+"")){
                        case 0:
                            kolayClients.add(clientOutput);
                            for (ObjectOutputStream out : kolayClients) {
                            out.writeObject(this.getName() +" odaya katıldı." );
                            }
                            break;
                        case 1:
                            ortaClients.add(clientOutput);
                            for (ObjectOutputStream out : ortaClients) {
                            out.writeObject(this.getName() +" odaya katıldı." );
                            }
                            break;
                        case 2:
                            zorClients.add(clientOutput);
                            for (ObjectOutputStream out : zorClients) {
                            out.writeObject(this.getName() +" odaya katıldı." );
                            }
                            break;
                    
                    }
                        writeToHistory(this.getName() + " adlı Oyuncunun ataması başarıyla gerçekleştirildi.");
                    }else if(mesaj instanceof String && ((String)mesaj).contains("@start-")){
                        qt = new QuestionThread(0, clientInput, clientOutput);
                        qt.start();
                    }else if(mesaj instanceof String && ((String)mesaj).contains("@cevap-")){
                        qt.setCevap(Integer.parseInt(((String)mesaj).substring(7)));
                        qt.cevapVerdiMi(true);
                        writeToHistory(this.getName() + "adlı oyuncunun mesajı set edildi.");
                    }           
                    else{
                    // bütün client'lara gelen bu mesajı gönder
                    for (ObjectOutputStream out : allClients) {
                        out.writeObject(this.getName() + ": " + mesaj);
                    }
                    }
                    
                    

                    // "son" mesajı iletişimi sonlandırır
                    if (mesaj.equals("son")) {
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Hata - ListenThread : " + ex);
            } finally {
                try {
                    
                    if (kolayClients.contains(clientOutput)) {
                        allClients.remove(clientOutput);
                        kolayClients.remove(clientOutput);
                        for (ObjectOutputStream out : kolayClients) {
                        out.writeObject(this.getName() + " server'dan ayrıldı.");
                    }
                    }else if (ortaClients.contains(clientOutput)) {
                        allClients.remove(clientOutput);
                        ortaClients.remove(clientOutput);
                        for (ObjectOutputStream out : ortaClients) {
                        out.writeObject(this.getName() + " server'dan ayrıldı.");
                    }
                    }else if (zorClients.contains(clientOutput)) {
                        allClients.remove(clientOutput);
                        zorClients.remove(clientOutput);
                        for (ObjectOutputStream out : zorClients) {
                        out.writeObject(this.getName() + " server'dan ayrıldı.");
                    }
                    }
                    
                    
                    

                    // bütün streamleri ve soketleri kapat
                    if (clientInput != null) {
                        clientInput.close();
                    }
                    if (clientOutput != null) {
                        clientOutput.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                    writeToHistory("Soket kapatıldı : " + clientSocket);
                } catch (IOException ex) {
                    System.out.println("Hata - Soket kapatılamadı : " + ex);
                }
            }
        }
    }

        class QuestionThread extends Thread {

        // dinleyeceğimiz client'ın soket nesnesi, input ve output stream'leri
        private int Zorluk;
        private ObjectInputStream clientInput;
        private ObjectOutputStream clientOutput;
        private Quizz exam ;
        private int Cevap;
        private boolean setCevap = false;
        private QuestionThread(int Zorluk,ObjectInputStream clientInput, ObjectOutputStream clientOutput) {
            this.Zorluk = Zorluk;
            this.clientOutput = clientOutput;
            this.clientInput = clientInput;
            this.exam = new Quizz();
        }

        private void setCevap(int cevap){
        this.Cevap = cevap;
        }
        
        private void cevapVerdiMi(boolean durum){
            
         this.setCevap = durum;   
        }
        
        @Override
        public void run() {
            
               System.out.println("Question Thread Start");
                ArrayList<Question> questions = exam.getQuizz(Zorluk);
                
                int counter = 0 ;
                while (!questions.isEmpty()){
                
                
                    try {
                        clientOutput.writeObject(questions.get(counter).Soru);
                        Thread.sleep(10000);
                        
                        if(Cevap == questions.get(counter).cevap && setCevap == true){
                        questions.remove(questions.get(counter));
                        clientOutput.writeObject("SERVER : Tebrikler Dogru Cevap Verdin");
                        
                        }else{
                        Random rand = new Random();
                        clientOutput.writeObject("SERVER : Malesef Yanlıs Cevap verdin");
                        questions.get(counter).increaseWrongCount();
                        if (questions.get(counter).getWrongCount() == 2){
                        clientOutput.writeObject(questions.get(counter).Soru + " sorusunun cevabı = " + questions.get(counter).cevap);
                        questions.remove(questions.get(counter));
                        }
                        
                        
                        for (int i = 0; i < questions.size(); i++) {
			int randomIndexToSwap = rand.nextInt(questions.size());
			Question temp = questions.get(randomIndexToSwap);
			questions.set(randomIndexToSwap, questions.get(i));
			questions.set(i, temp);
		}
                        }
                        setCevap = false;
                        
                    } catch (IOException ex) {
                        Logger.getLogger(TCP_Server.class.getName()).log(Level.SEVERE, null, ex);
                    

                }  catch (InterruptedException ex) {
                       Logger.getLogger(TCP_Server.class.getName()).log(Level.SEVERE, null, ex);
                   }
               

            
           
                
            } 
                
            try {
                clientOutput.writeObject("@finishGame-");
            } catch (IOException ex) {
                Logger.getLogger(TCP_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}   
    
    
    


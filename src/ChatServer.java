
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ChatServer implements Runnable {

    PrintWriter out;
    BufferedReader in;
    Socket s;
    Scanner keyboard;
    int index;
    String input;
    boolean doRun = true;
    static SecretKey key;

    public ChatServer(Socket a, int u) {
        s = a;
        keyboard = new Scanner(System.in);
        index = u;
    }

    public void run() {
        File f = new File("key.txt");
        FileReader fr;
        try {
            com.sun.org.apache.xml.internal.security.Init.init();
            Cipher cipher = Cipher.getInstance("AES");
            fr = new FileReader(f.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);
            String k2r = br.readLine();
            byte[] decodedKey = Base64.decode(k2r);
            key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            br.close();
            fr.close();

            try {
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new PrintWriter(s.getOutputStream());

                System.out.println("connexion de " + s.getInetAddress().toString() + " sur le port " + s.getPort());
                String talk = in.readLine();

                while (doRun) {
                    while (talk == null) {
                        talk = in.readLine();
                    }
                    System.out.println("encodé : "+talk);
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    byte[] res2 = cipher.doFinal(Base64.decode(talk));
                    //byte[] res2 = cipher.doFinal(res_str.getBytes("utf-8"));
                    talk = new String(res2);
                    System.out.println("décodé : "+ talk);
                    if (talk.compareToIgnoreCase("bye") == 0) {
                        System.out.println("shutting down following remote request");
                        doRun = false;
                    } else {
                        System.out.print("to client#" + index + "> ");
                        input = keyboard.nextLine();
                        cipher.init(Cipher.ENCRYPT_MODE, key);
                        byte[] res = cipher.doFinal(input.getBytes("UTF-8"));
                        String res_str = Base64.encode(res);//new String(res);

                        out.println(res_str);
                        out.flush();
                        if (input.compareToIgnoreCase("bye") == 0) {
                            System.out.println("server shutting down");
                            doRun = false;
                        } else {
                            talk = in.readLine();
                        }
                    }
                }
                s.close();
            } catch (Exception e) {
                System.out.println("raaah! what did u forget this time?");
                e.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Base64DecodingException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

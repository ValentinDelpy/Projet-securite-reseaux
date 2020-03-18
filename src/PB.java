
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PB {

    static SecretKey key;

    public static void main(String[] args) throws FileNotFoundException, IOException, Base64DecodingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        InetAddress addr;
        Socket client;
        PrintWriter out;
        BufferedReader in;
        String input;
        String userInput;
        boolean doRun = true;
        File f = new File("key.txt");
        FileReader fr = new FileReader(f.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        String k2r = br.readLine();
        byte[] decodedKey = Base64.decode(k2r);;
        key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        br.close();
        fr.close();
        Scanner k = new Scanner(System.in);
        try {
            Cipher cipher = Cipher.getInstance("AES");
            com.sun.org.apache.xml.internal.security.Init.init();
            client = new Socket("localhost", 4444);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            System.out.print("enter msg> ");
            userInput = k.nextLine();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] res = cipher.doFinal(userInput.getBytes("UTF-8"));
            String res_str = Base64.encode(res);
            out.println(res_str);
            out.flush();
            System.out.println("done");

            if (userInput.compareToIgnoreCase("bye") == 0) {
                System.out.println("shutting down");
                doRun = false;
            } else {
                while (doRun) {
                    input = in.readLine();
	            cipher.init(Cipher.DECRYPT_MODE, key);
                    while (input == null) {
                        input = in.readLine();
                    }
                    //Dans un but de démonstration, on montre le message encodé puis décodé.
                    //Dans une utilisation classique, on ne montrera que le message décodé.
                    System.out.println("encodé : "+input);
                    byte[] res2 = cipher.doFinal(Base64.decode(input));
                    input = new String(res2);
                    System.out.println("décodé : "+ input);
                    if (input.compareToIgnoreCase("bye") == 0) {
                        System.out.println("client shutting down from server request");
                        doRun = false;
                    } else {
                        System.out.print("enter msg> ");
                        userInput = k.nextLine();
                        //cipher.init(Cipher.ENCRYPT_MODE, key);
                        //byte[] res_tobytes = cipher.doFinal(userInput.getBytes());
                        //String res_tostr = Base64.encode(res_tobytes);;//new String(res);
                        out.println("message encodé :"+userInput);
                        out.flush();
                        if (userInput.compareToIgnoreCase("bye") == 0) {
                            System.out.println("shutting down");
                            doRun = false;
                        }

                    }
                }
            }
            client.close();
            k.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}

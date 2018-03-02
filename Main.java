package alda.huffman;

public class Main {


    public static void main(String[] args){
        Encoder graph = new Encoder();

        // === Call encryptMessage() on a text to convert it to huffman binary text.
        String file =  System.getProperty("user.dir") + "/test_Decrypted.txt";
        graph.encryptText(file);



        // === Call decryptMessage() on a binary text to decode it.
        String file2 =  System.getProperty("user.dir")+"/test_Encrypted.txt";
        graph.decryptMessage(file2);

    }
}

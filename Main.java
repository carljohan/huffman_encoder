package alda.huffman;

public class Main {


    public static void main(String[] args){
        Encoder graph = new Encoder();



        // === Call encryptMessage() on a text to convert it to huffman binary text.
        String file = "/Users/juan/Downloads/test.txt";
        graph.encryptText(file);
//

//
//        // === Call decryptMessage() on a binary text to decrypt it.
//        String file2 = "/Users/juan/Downloads/test_Encrypted.txt";
//        graph.decryptMessage(file2);
    }
}

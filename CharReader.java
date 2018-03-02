package alda.huffman;

import java.io.*;

class CharReader {
    public static final char NULL = (char)0;
    public static final char EOF = (char)-1; // End of file.

    private FileInputStream stream = null;
    private InputStreamReader reader = null;
    private char current = NULL;

    public CharReader(String fileName){
        try {
            stream = new FileInputStream(fileName);
            reader = new InputStreamReader(stream);
        }catch(FileNotFoundException fe){
            System.err.println("FileNotFound: " + fe+". Please make sure it exist." );
            System.exit(0);
        }
    }

    public char current() {
        return current;
    }

    public void moveNext(){
        try {
            if (reader == null) throw new IOException("No open file.");
            if (current != EOF) current = (char) reader.read();
            else {
                close();
            }
        }
        catch (IOException e) {
            System.err.println("IOException: " + e+". Error when reading file." );
            System.exit(0);
        }
    }

    public String getCharacters(int amount){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i<amount; i++){
            moveNext();
            str.append(current);
        }
        return str.toString();
    }

    public void close() throws IOException {
        if (reader != null)
            reader.close();
        if (stream != null)
            stream.close();
    }
}
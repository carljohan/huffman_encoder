package alda.huffman;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;


public class Encoder {
    private FileOutputStream stream = null;
    private OutputStreamWriter writer = null;

    private HashMap<Integer, Integer> charMap;
    private HashMap<Integer, String> binaryMap = new HashMap<>();

    private HNode root = null;


    //===========DEBUG MODE=====================
    private boolean debug = false;          //==
    //==========================================



    //Adds charMap to queue, , the queue will sort the chars
    private void buildGraph() {
        PriorityQueue<HNode> queue = new PriorityQueue<>(charMap.size(), new NodeComparator());


        for (Map.Entry<Integer, Integer> entry : charMap.entrySet()) {
            int i = entry.getValue();
            int c = entry.getKey();
            queue.add(new HNode(i, (char) c));

        }

        if(debug) {
            System.out.println("charMap size: " + charMap.size());
            for (HNode n : queue) {
                System.out.println(n);
            }

        }

        //adds the chars to correct node
        while (queue.size() > 1) {
            HNode x = queue.poll();
            HNode y = queue.poll();
            HNode newNode = new HNode(y.getValue() + x.getValue());
            newNode.setLeft(x);
            newNode.setRight(y);
            root = newNode;
            queue.add(newNode);
        }
    }





    public void encryptText(String file) {
        String inputFileName = file;
        charMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        char current = CharReader.NULL;

        CharReader charReader = new CharReader(file);
        while (current != CharReader.EOF) {  //Read the file until EOF
            charReader.moveNext();
            current = charReader.current();
            builder.append(current);

            if (current == CharReader.EOF) { // if current is EOF, abort
                break;
            }

            if (charMap.get((int) current) == null) { // Does value exist in charmap?
                charMap.put((int) current, 1); // No, add 1 instance of it.
            }
            else {
                int charCount = charMap.get((int) current) + 1; // yes, just add one more.
                charMap.put((int) current, charCount);
            }
        }

        if(debug) {
            Iterator it = charMap.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                int key = (int) pair.getKey();
                int value = (int) pair.getValue();
                System.out.println("" + key + " : " + value);
            }
        }


        buildGraph();
        buildBinaryText(inputFileName);
    }

    private void buildBinaryText(String file) {
        CharReader charReader = new CharReader(file);
        int current = CharReader.NULL;

        getCode(root, "");
        StringBuilder binaryValue = new StringBuilder();


        //Calculates binary for the graph and adds it first in the binaries.
        binaryValue.append(saveGraph(binaryMap));

        //then adds the encoded the message.
        while (current != CharReader.EOF) {
            charReader.moveNext();
            current = charReader.current();

            if (current == CharReader.EOF) {
                break;
            }

            binaryValue.append(binaryMap.get(current));
        }

        writeFile(binaryValue, "Encrypted");
    }

    public void getCode(HNode root, String s){
        if(root.getLeft() == null && root.getRight() == null){


            if(debug) {
                System.out.println(s + " : " + root.getCharacter());
            }
            binaryMap.put((int)root.getCharacter(), s);
            return;
        }

        getCode(root.getLeft(), s+0);
        getCode(root.getRight(), s+1);
    }


    public boolean isLetter(char c) {
        if (c == 0) {
            return false;
        }
        return true;
    }









    public String saveGraph(HashMap graph){
        StringBuilder graphSave = new StringBuilder();
        Iterator it = graph.entrySet().iterator();
        int graphSize = graph.size();

        if(graphSize > 1000){
            throw new IndexOutOfBoundsException("Too big message to encrypt");
        }

        if(graphSize>=100) {
            String graphStringSize = graphSize + "";

            graphSave.append(getBinaryForAscii(graphStringSize.charAt(0)));
            graphSave.append(getBinaryForAscii(graphStringSize.charAt(1)));
            graphSave.append(getBinaryForAscii(graphStringSize.charAt(2)));

        }else if(graphSize>=10) {
            String graphStringSize = graphSize + "";

            graphSave.append(getBinaryForAscii(  intToAscii(  0  )  ));
            graphSave.append(getBinaryForAscii(graphStringSize.charAt(0)));
            graphSave.append(getBinaryForAscii(graphStringSize.charAt(1)));
        }else{

            graphSave.append(getBinaryForAscii(  intToAscii(  0  )  ));
            graphSave.append(getBinaryForAscii(  intToAscii(  0  )  ));
            graphSave.append(getBinaryForAscii(  intToAscii(  graph.size()  )  ));

        }

        int count = 0;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            int key = (int)pair.getKey();

            graphSave.append(getBinaryForAscii(key));


            //Adds charMap Value. must be 8bit x 2 since the value could be bigger than 9. Error if bigger than 99
            int charMapValue = charMap.get(key);
            if(charMapValue > 1000){
                throw new IndexOutOfBoundsException("Too big message to encrypt");
            }
            if(charMapValue >= 100){
                String charMapValueString = charMapValue + "";
                graphSave.append(getBinaryForAscii(charMapValueString.charAt(0)));
                graphSave.append(getBinaryForAscii(charMapValueString.charAt(1)));
                graphSave.append(getBinaryForAscii(charMapValueString.charAt(2)));
            }
            else if(charMapValue >= 10){
                String charMapValueString = charMapValue + "";
                graphSave.append(getBinaryForAscii(  intToAscii(  0  )  ));
                graphSave.append(getBinaryForAscii(charMapValueString.charAt(0)));
                graphSave.append(getBinaryForAscii(charMapValueString.charAt(1)));
            }
            else{
                graphSave.append(getBinaryForAscii(  intToAscii(  0  )  ));
                graphSave.append(getBinaryForAscii(  intToAscii(  0  )  ));
                graphSave.append(  getBinaryForAscii(  intToAscii(  charMap.get(key))   ) );
            }

            //Calculate how long the huffman binary is.
            int length = (char)((String)pair.getValue()).length();
            String asciiLength = getBinaryForAscii(intToAscii(length));
            graphSave.append(asciiLength);
            graphSave.append(pair.getValue());



            if(debug) {
                System.out.println("Key: (" + key + ") " + getBinaryForAscii(key));
                System.out.println("Huffman value Length in Ascii binary: (" + length + ") + " + asciiLength);
                System.out.println("Huffman Length: " + pair.getValue());
                System.out.println("------" + ++count + "---------");

            }
        }
        return graphSave.toString();
    }


    public void decryptMessage(String file) {
        StringBuilder builder = new StringBuilder();
        int current = CharReader.NULL;
        CharReader charReader = new CharReader(file);
        generateGraphFromBinaries(charReader);
        buildGraph();
        HNode node = root;

        while (current != CharReader.EOF) {
            charReader.moveNext();
            current = charReader.current();

            if (current == CharReader.EOF) {
                break;
            }

            if(current == 48){
                node = node.getLeft();
            }else if(current == 49){
                node = node.getRight();
            }

            if(node.getLeft() == null && node.getRight() == null && isLetter(node.getCharacter())){
                builder.append(node.getCharacter());
                node = root;
            }
        }
        if(debug) {
            System.out.println("Messages: "+ builder.toString());
        }else {
            writeFile(builder, "Decrypted");
        }
    }



    private void generateGraphFromBinaries(CharReader charReader){
        charMap = new HashMap<>();
        int graphCounter = 0;

    //-------------------------Reads how big the Graph is(size). Between 1 - 512 key so (3 x 8 bit)-------------------------
        //---first 8 bit ---(hundreds)
        String amount = charReader.getCharacters(8);
        int graphCount = convertBinaryToAsciiValue(amount);
        int firstInt = charIntToValue(graphCount);

        //---middle 8 bit ---(tens)
        amount = charReader.getCharacters(8);
        graphCount = convertBinaryToAsciiValue(amount);
        int secondInt = charIntToValue(graphCount);

        //---last 8 bit ---(singulars)
        amount = charReader.getCharacters(8);
        graphCount = convertBinaryToAsciiValue(amount);
        int thirdInt = charIntToValue(graphCount);
        graphCount = Integer.parseInt(""+ firstInt + secondInt + thirdInt); //implicit cast from int to string then parse back to int(but added together






        while (graphCounter < graphCount) {
            //-------------------------binaryMap key + charMap key  (1x 8 bit)-------------------------
            amount = charReader.getCharacters(8);
            int key = convertBinaryToAsciiValue(amount);
            int charMapKey = key;

            //------------------------- charMap value (3 x 8 bit) -------------------------
            //---first 8 bit --- (hundreds)
            amount = charReader.getCharacters(8);
            int charMapValue = convertBinaryToAsciiValue(amount);
            int firstCharMapValue = charIntToValue(charMapValue);

            //--- second 8 bit---(tens)
            amount = charReader.getCharacters(8);
            charMapValue = convertBinaryToAsciiValue(amount);
            int secondCharMapValue = charIntToValue(charMapValue);

            //--- third 8 bit---(singulars)
            amount = charReader.getCharacters(8);
            charMapValue = convertBinaryToAsciiValue(amount);
            int thirdCharMapValue = charIntToValue(charMapValue);


            //--finally put both together att insert into charMap--
            charMapValue = Integer.parseInt(firstCharMapValue + "" +secondCharMapValue + thirdCharMapValue); //implicit typecast
            charMap.put(charMapKey, charMapValue);


            //------------------------- huffman bit custom size (2-8bit) -------------------------
            amount = charReader.getCharacters(8);
            int stepCount = convertBinaryToAsciiValue(amount); //reads how long the huffman bit will be

            stepCount = charIntToValue(stepCount); // converts it to from ascii-value to int.

            String value = charReader.getCharacters(stepCount); // read huffman-bit from file.
            binaryMap.put(key, value); //add key and huffman value.
            graphCounter++; // keep track of how many Mapvalues should be inserted(first value of binary text)

            if(debug) {
                System.out.println("key: " + charMapKey + ". value: " + charMapValue);
                System.out.println("key: " + key + ". Value: " + value);
                System.out.println("--------" + graphCounter + "------");
            }
        }
    }


    private int charIntToValue(int i){
        char convert = (char)i;
        return Integer.parseInt( convert+"");
    }

    private int intToAscii(int i){
        int ascii = (int)Character.forDigit(i, 10)  ;
        return ascii;
    }

    private String getBinaryForAscii(int info){
        String asciiValue =  String.format("%8s", Integer.toBinaryString(info)).replace(' ', '0');
        return asciiValue;
    }

    private int convertBinaryToAsciiValue(String info){
        int charCode = Integer.parseInt(info, 2);
        return charCode;
    }


    private void writeFile(StringBuilder builder, String name){

        System.out.println(name+" message: \n" + builder.toString());
        try{
            try{
                stream = new FileOutputStream("/Users/juan/Downloads/test_"+name+".txt");
                writer = new OutputStreamWriter(stream);
                writer.write(builder.toString());
            }
            catch (Exception exception) {
                System.out.println("EXCEPTION: " + exception);
            }
            finally {
                if (writer != null)
                    writer.close();
                if (stream != null)
                    stream.close();
            }
        }
        catch (Exception exception) {
            System.out.println("EXCEPTION: " + exception);
        }
    }
}




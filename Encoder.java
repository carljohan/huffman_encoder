package alda.huffman;

import java.util.TreeMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;


public class Encoder {
    private FileOutputStream stream = null;
    private OutputStreamWriter writer = null;

    private TreeMap<Integer, Integer> charMap;
    private TreeMap<Integer, String> binaryMap = new TreeMap<>();

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
           HNode hn = new HNode(i, (char) c);

           queue.add(hn);
        }

        if(debug) {
            System.out.println("charMap size: " + charMap.size());
            for (HNode n : queue) {
                System.out.print(n.getCharacter() + ""+n.getValue()+"");
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
        getCode(root, "");
    }



    public void encryptText(String file) {
        String inputFileName = file;
        charMap = new TreeMap<>();
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

            if((int) current > 256){
                throw new NumberFormatException(" Only ASCII-values below 256 supported. Your text has--> "+
                        current + "<-- which has value: "+(int) current);
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
        StringBuilder binaryValue = new StringBuilder();
        CharReader charReader = new CharReader(file);
        int current = CharReader.NULL;

        getCode(root, "");

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

        if(debug) {
            System.out.println(binaryValue.toString());
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

            if(node.getLeft() == null && node.getRight() == null){
                builder.append(node.getCharacter());
                node = root;
            }
        }


        if(debug) {
            System.out.println("Message: "+ builder.toString());
        }else {
            writeFile(builder, "Decrypted");
        }
    }


    private void addAtChar(StringBuilder builder, String string, int charAtValue, int padding){
        int amountOfPadding = padding-charAtValue;
        for(int j = 0; j<=amountOfPadding; j++){
            if(amountOfPadding == 0){
                break;
            }
                builder.append(getBinaryForAscii(  intToAscii(  0  )  ));
                amountOfPadding--;
        }
        for(int i= 0; i < charAtValue; i++){
            builder.append(getBinaryForAscii(string.charAt(i)));
        }
    }

    public String saveGraph(TreeMap graph){
        StringBuilder graphSave = new StringBuilder();
        Iterator it = graph.entrySet().iterator();
        int graphSize = graph.size();


        //------------ GraphMapSize----------//
        if(graphSize > 1000){
            throw new IndexOutOfBoundsException("Too big message to encrypt");
        }
        if(graphSize>=100) {
            addAtChar(graphSave, (""+graphSize), 3,3 );
        }else if(graphSize>=10) {
            addAtChar(graphSave, (""+graphSize), 2,3 );
        }else{
            addAtChar(graphSave, (""+graphSize), 1,3 );
        }


        int count = 0;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            int key = (int)pair.getKey();
            int charMapValue = charMap.get(key);

            //-------------Key--------//
            graphSave.append(getBinaryForAscii(key));


            //-------------CharMap Value--------//
            //Adds charMap Value. must be 8bit x 2 since the value could be bigger than 9. Error if bigger than 99

            if(charMapValue >= 1000){
                throw new IndexOutOfBoundsException("Too big message to encrypt");
            }
            if(charMapValue >= 100){
                addAtChar(graphSave, (""+charMapValue), 3, 3 );
            }
            else if(charMapValue >= 10){
                addAtChar(graphSave, (""+charMapValue), 2, 3 );
            }else{
                addAtChar(graphSave, (""+charMapValue), 1, 3 );
            }

            //-------------huffman bit length Value up to 16-bit--------//

            //Calculate how long the huffman binary is.
            int length = (char)((String)pair.getValue()).length();

            if(length >= 100){
                throw new IndexOutOfBoundsException("Too big message to encrypt");
            }
            if(length >= 10){
                addAtChar(graphSave, (""+length), 2, 2);
            }else{
                addAtChar(graphSave, (""+length), 1, 2);
            }



            //-------------huffman actual Value--------//
            graphSave.append(pair.getValue());


            if(debug) {
                System.out.println("Key: (" + key + ") " + getBinaryForAscii(key));
                System.out.println("Huffman value Length in Ascii binary: (" + length + ") + " + getBinaryForAscii((char) length));
                System.out.println("Huffman Length: " + pair.getValue());
                System.out.println("------" + ++count + "---------");
            }
        }
        return graphSave.toString();
    }


    private void generateGraphFromBinaries(CharReader charReader){
        charMap = new TreeMap<>();
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


        String value ="";

        while (graphCounter < graphCount) {
            //-------------------------binaryMap key + charMap key  (1x 8 bit)-------------------------

            amount = charReader.getCharacters(8);
            int key = convertBinaryToAsciiValue(amount);
            int charMapKey = key;
//            System.out.print("| charMapkey "+ amount);


            //------------------------- charMap value (3 x 8 bit) -------------------------
            //---first 8 bit --- (hundreds)
            amount = charReader.getCharacters(8);
            int charMapValue = convertBinaryToAsciiValue(amount);
            int firstCharMapValue = charIntToValue(charMapValue);

//            System.out.print("| firstCharmap "+ amount);
//
            //--- second 8 bit---(tens)
            amount = charReader.getCharacters(8);
            charMapValue = convertBinaryToAsciiValue(amount);
            int secondCharMapValue = charIntToValue(charMapValue);
//            System.out.print("| SecondCharMap "+ amount);

            //--- third 8 bit---(singulars)
            amount = charReader.getCharacters(8);
            charMapValue = convertBinaryToAsciiValue(amount);
            int thirdCharMapValue = charIntToValue(charMapValue);
//            System.out.print("| ThirdCharMap "+ amount);


            //--finally put both together att insert into charMap--
            charMapValue = Integer.parseInt(firstCharMapValue + "" +secondCharMapValue + thirdCharMapValue); //implicit typecast
            charMap.put(charMapKey, charMapValue);


            //------------------------- huffman bit custom size (2-16bit) -------------------------
            //------first 8 bit of length---(tens)
            amount = charReader.getCharacters(8);
//            System.out.print("aammount "+ amount);
            int stepCount = convertBinaryToAsciiValue(amount); //reads how long the huffman bit will be
//            System.out.println("Steocount "+ stepCount);
            int stepCount1 = charIntToValue(stepCount);

            //------last 8 bit of length---(singulars)
            amount = charReader.getCharacters(8);
            stepCount = convertBinaryToAsciiValue(amount); //reads how long the huffman bit will be
            int stepCount2  = charIntToValue(stepCount);

            stepCount = Integer.parseInt("" + stepCount1 + stepCount2);

//            System.out.println("Stepcount"+ stepCount);
//            stepCount = charIntToValue(stepCount); // converts it to from ascii-value to int.

             value = charReader.getCharacters(stepCount); // read huffman-bit from file based on calculated length


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




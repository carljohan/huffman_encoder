package alda.huffman;

import java.util.TreeMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;


class Encoder {
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
                System.out.print(n.getCharacter() + ""+n.getValue()+"s");
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

        if(root == null){
            throw new IndexOutOfBoundsException("Text must be atleast 2 characters.");
        }
        getCode(root, "");
    }



    void encryptText(String file) {
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
        else
            writeFile(binaryValue, "Encrypted");
    }




    private void getCode(HNode root, String s){
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


    protected void decryptMessage(String file) {
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

    private String saveGraph(TreeMap graph){
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

            if(debug) {
                System.out.println("Key: (" + key + ") " + getBinaryForAscii(key));
                System.out.println("Huffman Length: " + pair.getValue());
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

            graphCounter++; // keep track of how many Mapvalues should be inserted(first value of binary text)


            if(debug) {
                System.out.println("key: " + charMapKey + ". value: " + charMapValue);
                System.out.println("--------" + graphCounter + "------");
            }
        }
    }

    private int charIntToValue(int i){
        char convert = (char)i;
        return Integer.parseInt( convert+"");
    }

    private int intToAscii(int i){
        return (int)Character.forDigit(i, 10)  ;
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
                stream = new FileOutputStream(System.getProperty("user.dir") + "/test_"+name+".txt");
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




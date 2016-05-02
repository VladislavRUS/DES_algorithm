import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String LEFT = "l";
    private static final String RIGHT = "r";

    private static String getEightBitRepresentation(String str){
        String result = new BigInteger(str, 16).toString(2);
        while (result.length() % 8 != 0){
            result = "0" + result;
        }
        return result;
    }

    private static String swapBits(String bytesToSwap, int[] array){
        char[] swapped = new char[array.length];
        for(int i = 0; i < array.length; i++){
            swapped[i] = bytesToSwap.charAt(array[i]-1);
        }
        return String.valueOf(swapped);
    }

    private static String getHalf(String source, String which){
        switch (which){
            case LEFT:{
                return source.substring(0, source.length()/2);
            }
            case RIGHT:{
                return source.substring(source.length()/2, source.length());
            }
            default:{
                throw new RuntimeException("You can choose only left or right!");
            }
        }
    }

    private static String[] computeSubKeys(String key){
        key = getEightBitRepresentation(key);
        if(key.length() != 64){
            throw new RuntimeException("Key must be 64 bits long!");
        }
        key = swapBits(key, Tables.PC_1);
        String C_0 = getHalf(key, LEFT);
        String D_0 = getHalf(key, RIGHT);

        Pair[] pairs = new Pair[16];

        Pair initial = new Pair();
        initial.setFirstValue(C_0);
        initial.setSecondValue(D_0);

        Pair previous = initial;
        for(int i = 0; i < 16; i++){
            Pair pair = new Pair();
            pair.setFirstValue(shiftLeft(previous.getFirstValue(), Tables.numberOfLeftShifts[i]));
            pair.setSecondValue(shiftLeft(previous.getSecondValue(), Tables.numberOfLeftShifts[i]));
            pairs[i] = pair;
            previous = pair;
        }

        String[] subKeys = new String[16];
        for(int i = 0; i < 16; i++){
            String currentKey = pairs[i].getFirstValue() + pairs[i].getSecondValue();
            subKeys[i] = swapBits(currentKey, Tables.PC_2);
        }
        return subKeys;
    }

    private static String shiftLeft(String bytesToShift, int numberOfShifts){
        char[] shifted = new char[bytesToShift.length()];
        for(int i = 0; i < shifted.length; i++) {
            int position = i + numberOfShifts;
            if(position >= bytesToShift.length()){
                position -= bytesToShift.length();
            }
            shifted[i] = bytesToShift.charAt(position);
        }
        return String.valueOf(shifted);
    }

    private static String xor(String first, String second){
        if(first.length() != second.length()){
            throw new RuntimeException("Lengths are not equal!");
        }
        char[] xorArray = new char[first.length()];
        for(int i = 0; i < first.length(); i++){
            int firstStringValue = Integer.parseInt(String.valueOf(first.charAt(i)));
            int secondStringValue = Integer.parseInt(String.valueOf(second.charAt(i)));
            int result = firstStringValue ^ secondStringValue;
            xorArray[i] = String.valueOf(result).charAt(0);
        }
        return String.valueOf(xorArray);
    }

    private static String f(String secondValue, String key) {
        String R = expand(secondValue);
        String sum = xor(R, key);
        return processBlocks(sum);
    }

    private static String processBlocks(String blocks){
        String[] blocksArray = new String[8];
        int cnt = 0;
        for(int i = 0; i < blocks.length(); i += 6){
            blocksArray[cnt++] = blocks.substring(i, i+6);
        }
        StringBuilder result = new StringBuilder("");
        for(int i = 0; i < blocksArray.length; i++) {
            String block = blocksArray[i];
            int row = getRow(block);
            int column = getColumn(block);
            int value = Tables.S_TABLES[i][row][column];
            String binary = Integer.toBinaryString(value);
            result.append(makeFourBits(binary));
        }
        return swapBits(result.toString(), Tables.P);
    }

    private static String makeFourBits(String binary){
        while (binary.length() < 4){
            binary = "0" + binary;
        }
        return binary;
    }
    private static int getRow(String block){
        String rowNumber = String.valueOf(block.charAt(0)) + String.valueOf(block.charAt(block.length() - 1));
        return Integer.parseInt(rowNumber, 2);
    }

    private static int getColumn(String block){
        String columnNumber = block.substring(1, block.length()-1);
        return Integer.parseInt(columnNumber, 2);
    }

    private static String expand(String R){
        return swapBits(R, Tables.E_BIT_SELECTION_TABLE);
    }

    private static String des(String message, String key, boolean shouldBeEncrypted){
        String[] subKeys = computeSubKeys(key);
        if(shouldBeEncrypted){
            message = String.valueOf(Hex.encodeHex(message.getBytes()));
        }
        List<String> blocks = getMessageBlocks(message);
        List<String> processedBlocks = new ArrayList<>(blocks.size());
        blocks.forEach(block -> processedBlocks.add(processBlock(block, subKeys, shouldBeEncrypted)));
        StringBuilder result = new StringBuilder();
        processedBlocks.forEach(processedBlock -> result.append(PrintUtils.toHexadecimalChars(processedBlock)));
        return result.toString().toUpperCase();
    }

    private static List<String> getMessageBlocks(String message){
        StringBuilder messageBuilder = new StringBuilder(message);
        while(messageBuilder.length() % 16 != 0){
            messageBuilder.append("0");
        }
        List<String> messageBlocks = new ArrayList<>();
        String binaryMessage = getEightBitRepresentation(messageBuilder.toString());
        for(int i = 0; i < binaryMessage.length(); i += 64){
            messageBlocks.add(binaryMessage.substring(i, i+64));
        }
        return messageBlocks;
    }

    private static String processBlock(String block, String[] keys, boolean shouldBeEncrypted){
        String IP = swapBits(block, Tables.IP_BEGIN);
        String L_0 = getHalf(IP, LEFT);
        String R_0 = getHalf(IP, RIGHT);

        Pair initial = new Pair();
        initial.setFirstValue(L_0);
        initial.setSecondValue(R_0);

        Pair[] pairs;
        if(shouldBeEncrypted){
            pairs = encodeBlock(initial, keys);
        }
        else{
            pairs = decodeBlock(initial, keys);
        }
        String reversed = pairs[15].getSecondValue() + pairs[15].getFirstValue();
        return swapBits(reversed, Tables.IP_END);
    }

    private static Pair[] encodeBlock(Pair previous, String[] keys){
        Pair[] pairs = new Pair[16];
        for(int i = 0; i < 16; i++){
            Pair currentPair = new Pair();
            currentPair.setFirstValue(previous.getSecondValue());
            currentPair.setSecondValue(xor(previous.getFirstValue(), f(previous.getSecondValue(), keys[i])));
            pairs[i] = currentPair;
            previous = currentPair;
        }
        return pairs;
    }

    private static Pair[] decodeBlock(Pair previous, String[] keys){
        Pair[] pairs = new Pair[16];
        for(int i = 0, j = 15; j >= 0; i++, j--){
            Pair currentPair = new Pair();
            currentPair.setFirstValue(previous.getSecondValue());
            currentPair.setSecondValue(xor(previous.getFirstValue(), f(previous.getSecondValue(), keys[j])));
            pairs[i] = currentPair;
            previous = currentPair;
        }
        return pairs;
    }

    public static void main(String[] args) {
        String key = "133457799BBCDFF1";

        String msg = "des implementation by ssau student of 6th faculty Kurochkin Vladislav";
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + msg);

        String encrypted = des(msg, key, true);
        System.out.println("\nENCRYPTED: " + encrypted.toUpperCase());

        String decrypted = des(encrypted, key, false);
        System.out.println("DECRYPTED: " + decrypted.toUpperCase());

        System.out.println("\nDECRYPTED MESSAGE: " + getDecodedHexString(decrypted));
    }

    private static String getDecodedHexString(String hex){
        StringBuilder value = new StringBuilder();
        try {
            byte[] bytes = Hex.decodeHex(hex.toCharArray());
            for(byte b: bytes){
                value.append((char)b);
            }
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return value.toString();
    }
}

import java.math.BigInteger;

public class PrintUtils {
    public static void printByFour(String binary){
        for(int i = 0; i < binary.length(); i += 4){
            System.out.print(binary.substring(i, i + 4) + " ");
        }
        System.out.println();
    }

    public static void printBySix(String binary){
        for(int i = 0; i < binary.length(); i += 6){
            System.out.print(binary.substring(i, i + 6) + " ");
        }
        System.out.println();
    }

    public static void printBySeven(String binary){
        for(int i = 0; i < binary.length(); i += 7){
            System.out.print(binary.substring(i, i + 7) + " ");
        }
        System.out.println();
    }

    public static void printByEight(String binary){
        for(int i = 0; i < binary.length(); i += 8){
            System.out.print(binary.substring(i, i + 8) + " ");
        }
        System.out.println();
    }

    public static void printFromBinaryToHexadecimalChars(String binary){
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < binary.length(); i += 8){
            String block = binary.substring(i, i + 8);
            result.append(new BigInteger(String.valueOf(Integer.parseInt(block, 2))).toString(16));
        }
        System.out.println(result.toString());
    }

    public static String toHexadecimalChars(String binary){
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < binary.length(); i += 8){
            String block = binary.substring(i, i + 8);
            String hex = new BigInteger(String.valueOf(Integer.parseInt(block, 2))).toString(16);
            result.append(hex.length() == 1 ? "0" + hex : hex);
        }
        return result.toString();
    }
}

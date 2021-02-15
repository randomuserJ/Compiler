package classes;

import java.io.*;

public class Main {
    private static final String PATH = "src\\";

    public static void main(String[] args) {
        String content = "";
        if (args.length == 0)
            content = loadFile("test.txt");
        else {
            for (String arg : args) {
                content = loadFile(arg);
            }
        }

        Compiler C = new Compiler();
        C.runProgram(content);
    }

    private static String loadFile(String filename) {
        StringBuilder sb = new StringBuilder();

        try {
            InputStream is = new FileInputStream(PATH + filename);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private static void readFile(String program) {
        String[] lines = program.split("\n");
        for (String line : lines) {
            String[] words = line.split(" ");
            for (String word : words)
                System.out.println(word);
        }
    }
}

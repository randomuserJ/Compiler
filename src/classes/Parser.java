package classes;

import classes.Grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Parser {

    public Parser() {
    }

    public static List<List<Symbol>> parseProgram(String program, boolean translate) {
        int lineNumber = 1;
        String[] lines = program.split("\n");
        List<List<Symbol>> translatedProgram = new ArrayList<>();

        for (String line : lines) {
            List<Symbol> translatedLine = new ArrayList<>();
            String[] words = line.split(" ");

            for (String word : words) {
                try {
                    //  System.out.print((translate ? classifyWord(word) : word) + " ");
                    translatedLine.add(classifyWord(word));
                } catch (Exception e) {
                    System.err.println(" Parser error on the line " + lineNumber + ". " + e.getMessage());
                    return null;
                }
            }

            translatedProgram.add(translatedLine);
            lineNumber++;
        }
        System.out.println("--- parsed successfully ---");
        return translatedProgram;
    }

    private static Symbol classifyWord(String word) throws Exception {
        switch (word) {
            case "variable":
                return Symbol.TYPE;
            case "read":
            case "write":
                return Symbol.EXPRESSION;
            case "cycle":
                return Symbol.CYCLE_START;
            case "end":
                return Symbol.CYCLE_END;
            case "=":
                return Symbol.ASSIGN;
            case "+":
            case "-":
            case "*":
                return Symbol.OPERATOR;
            case "<":
            case ">":
            case ">=":
            case "<=":
            case "!=":
            case "==":
                return Symbol.COMPARE;
        }
        if (Pattern.compile("^[a-zA-Z_$][a-zA-Z_$0-9]*$").matcher(word).matches()) {

            return Symbol.VARIABLE;
        }

        if (Pattern.compile("^[-+]?\\d*$").matcher(word).matches())
            return Symbol.VALUE;

        throw new Exception("Cannot resolve symbol " + word + ".");
    }
}

package classes;

import java.util.Arrays;
import java.util.List;

public class Grammar {

    enum Symbol {
        TYPE,
        EXPRESSION,
        CYCLE_START,
        CYCLE_END,
        VARIABLE,
        VALUE,
        ASSIGN,
        COMPARE,
        OPERATOR
    }

    private static final List<List<Symbol>> TYPE_RULES = Arrays.asList(
            Arrays.asList(Symbol.TYPE, Symbol.VARIABLE)
    );

    private static final List<List<Symbol>> EXPR_RULES = Arrays.asList(
            Arrays.asList(Symbol.EXPRESSION, Symbol.VARIABLE)
    );

    private static final List<List<Symbol>> CYCLE_RULES = Arrays.asList(
            Arrays.asList(Symbol.CYCLE_START, Symbol.VARIABLE, Symbol.COMPARE, Symbol.VARIABLE),
            Arrays.asList(Symbol.CYCLE_START, Symbol.VARIABLE, Symbol.COMPARE, Symbol.VALUE),
            Arrays.asList(Symbol.CYCLE_START, Symbol.VALUE, Symbol.COMPARE, Symbol.VARIABLE),
            Arrays.asList(Symbol.CYCLE_START, Symbol.VALUE, Symbol.COMPARE, Symbol.VALUE),
            Arrays.asList(Symbol.CYCLE_END)
    );


    public static boolean checkSyntax(List<Symbol> line) throws Exception {

        Symbol startingSymbol = line.get(0);
        switch (startingSymbol) {
            case TYPE:
                return checkAgainstStaticRules(line, TYPE_RULES);
            case EXPRESSION:
                return checkAgainstStaticRules(line, EXPR_RULES);
            case CYCLE_START:
            case CYCLE_END:
                return checkAgainstStaticRules(line, CYCLE_RULES);
            case VARIABLE:
                return checkAgainstDynamicRules(line);
            default:
                throw new Exception("Broken syntax!");
        }
    }

    private static boolean checkAgainstStaticRules(List<Symbol> line, List<List<Symbol>> rules) throws Exception {
        for (List<Symbol> rule : rules)
            if (line.equals(rule)) return true;

        throw new Exception("Broken syntax!");
    }

    private static boolean checkAgainstDynamicRules(List<Symbol> line) throws Exception {
        if (line.size() <= 2)
            throw new Exception("Broken syntax - line starting with variable is too short!");

        if (!(line.get(line.size() - 1) == Symbol.VARIABLE || line.get(line.size() - 1) == Symbol.VALUE))
            throw new Exception("Broken syntax! - line must end with variable or value");

        if (line.get(0) == Symbol.VARIABLE &&
                line.get(1) == Symbol.ASSIGN &&
                (line.get(2) == Symbol.VARIABLE || line.get(2) == Symbol.VALUE)
        ) {
            for (int i = 2; i < line.size() - 1; i++) {
                if (line.get(i) == Symbol.VARIABLE || line.get(i) == Symbol.VALUE) {
                    if (line.get(i + 1) != Symbol.OPERATOR)
                        throw new Exception("Broken syntax - operator expected!");
                } else if (line.get(i) == Symbol.OPERATOR) {
                    if (line.get(i + 1) != Symbol.VARIABLE && line.get(i + 1) != Symbol.VALUE)
                        throw new Exception("Broken syntax - variable or value expected!");
                } else
                    throw new Exception("Broken syntax!");
            }
            return true;
        }
        throw new Exception("Broken syntax!");
    }
}

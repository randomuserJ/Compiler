package classes;

import classes.Grammar.Symbol;

import java.util.*;

public class Compiler {
    private Map<String, Integer> variables = new HashMap<>();
    private List<List<Symbol>> parsedProgram;

    Compiler() {
    }

    public void runProgram(String program) {
        try {
            if (!compile(program))
                return;
            run(program);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private boolean compile(String program) {

        parsedProgram = classes.Parser.parseProgram(program, true);
        if (parsedProgram == null)
            return false;

        String[] lines = program.split("\n");
        String[] words;

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            words = lines[lineNumber].split(" ");

            try {
                classes.Grammar.checkSyntax(parsedProgram.get(lineNumber));
                checkVariableLogic(parsedProgram.get(lineNumber), words);
            } catch (Exception e) {
                System.err.println("Compilation error on the line " + (lineNumber + 1) + ". " + e.getMessage());
                return false;
            }
        }
        try {
            checkBeginEndCycleCount();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }

        System.out.println("--- compiled successfully ---");
        return true;
    }

    private void run(String program) throws Exception {
        List<Integer> returnAddresses = new ArrayList<>();
        String[] lines = program.split("\n");
        String[] words;


        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            words = lines[lineNumber].split(" ");
            Symbol firstSymbol = parsedProgram.get(lineNumber).get(0);

            switch (firstSymbol) {
                case EXPRESSION: {
                    String variableName = words[1];
                    if (words[0].equals("read")) {
                        System.out.print("Enter a value for variable " + variableName + ": ");
                        Scanner sc = new Scanner(System.in);
                        try {
                            Integer input = sc.nextInt();
                            variables.put(variableName, input);
                        } catch (InputMismatchException e) {
                            System.err.println("Cannot convert your input to integer.");
                            return;
                        }
                    } else
                        System.out.println(variableName + " = " + variables.get(variableName));
                    break;
                }
                case VARIABLE: {
                    if (parsedProgram.get(lineNumber).get(1) == Symbol.ASSIGN) {
                        variables.put(words[0], evaluateLine(words));
                    } else throw new Exception("Non-specific assignment exception (should never happen)");
                    break;
                }
                case CYCLE_START: {
                    if (evaluateCondition(words))
                        returnAddresses.add(lineNumber);
                    else
                        lineNumber = endCycle(lineNumber);
                    break;
                }
                case CYCLE_END: {
                    Integer startCyclelineNumber = returnAddresses.get(returnAddresses.size() - 1);
                    words = lines[startCyclelineNumber].split(" ");
                    if (evaluateCondition(words))
                        lineNumber = startCyclelineNumber;
                    else
                        returnAddresses.remove(startCyclelineNumber);
                    break;
                }
                default:
                    break;
            }
        }
    }

    private Integer endCycle(Integer lineNumber) throws Exception {
        List<Integer> cycles = new ArrayList<>();
        lineNumber++;
        while (true) {
            try {
                if (parsedProgram.get(lineNumber).get(0) == Symbol.CYCLE_START)
                    cycles.add(lineNumber);
                if (parsedProgram.get(lineNumber).get(0) == Symbol.CYCLE_END) {
                    if (cycles.isEmpty())
                        return lineNumber;
                    cycles.remove(cycles.size() - 1);
                }
                lineNumber++;
            } catch (IndexOutOfBoundsException e) {
                throw new Exception("Non-specific cycle-end exception (should never happen)");
            }
        }
    }

    private boolean evaluateCondition(String[] line) throws Exception {
        if (line.length != 4) {
            throw new Exception("Non-specific condition exception (should never happen)");
        }
        String leftSide = line[1];
        String rightSide = line[3];
        switch (line[2]) {
            case "==":
                return getValue(leftSide).equals(getValue(rightSide));
            case "!=":
                return !getValue(leftSide).equals(getValue(rightSide));
            case ">=":
                return getValue(leftSide) >= (getValue(rightSide));
            case "<=":
                return getValue(leftSide) <= (getValue(rightSide));
            case ">":
                return getValue(leftSide) > (getValue(rightSide));
            case "<":
                return getValue(leftSide) < (getValue(rightSide));
        }
        throw new Exception("Non-specific condition exception (should never happen)");
    }

    private Integer evaluateLine(String[] line) throws Exception {
        return evaluateAddSub(line, 2);
    }

    private Integer evaluateAddSub(String[] line, int pos) throws Exception {
        if (pos + 1 >= line.length)
            return getValue(line[pos]);
        if (line[pos + 1].equals("+"))
            return getValue(line[pos]) + evaluateAddSub(line, pos + 2);
        else if (line[pos + 1].equals("-"))
            return getValue(line[pos]) - evaluateAddSub(line, pos + 2);
        else if (line[pos + 1].equals("*"))
            return evaluateMultiplication(line, pos);
        else throw new Exception("Non-specific equation format exception (should never happen)");
    }

    private Integer evaluateMultiplication(String[] line, int pos) throws Exception {
        if (pos + 1 >= line.length || !line[pos + 1].equals("*"))
            return getValue(line[pos]);
        if (line[pos + 1].equals("*"))
            return getValue(line[pos]) * evaluateMultiplication(line, pos + 2);
        throw new Exception("Non-specific equation format exception (should never happen)");
    }

    private Integer getValue(String valueORvariable) {
        try {
            return Integer.parseInt(valueORvariable);
        } catch (NumberFormatException e) {
            return variables.get(valueORvariable);
        }
    }

    private void checkVariableLogic(List<Symbol> translatedLine, String[] line) throws Exception {
        if (translatedLine.get(0) == Symbol.TYPE) {
            if (allocateVariable(line[1]) != null)
                throw new Exception("Variable '" + line[1] + "' has already been initialized!");
        }

        String variable;
        for (int i = 0; i < translatedLine.size(); i++) {
            if (translatedLine.get(i) == Symbol.VARIABLE) {
                variable = line[i];
                if (!isAllocated(variable))
                    throw new Exception("Variable '" + variable + "' has not been initialized!");
            }
        }
    }

    private void checkBeginEndCycleCount() throws Exception {

        List<Integer> cycles = new ArrayList<>();
        for (int i = 0; i < parsedProgram.size(); i++) {
            Symbol firstSymbol = parsedProgram.get(i).get(0);
            if (firstSymbol == Symbol.CYCLE_START)
                cycles.add(0);
            if (firstSymbol == Symbol.CYCLE_END) {
                if (cycles.isEmpty())
                    throw new Exception("Cycle scheme violation - Missing start statement.");
                cycles.remove(cycles.size() - 1);
            }
        }
        if (!cycles.isEmpty())
            throw new Exception("Cycle scheme violation - Missing end statement.");
    }

    private Integer allocateVariable(String var) {
        return variables.put(var, 0);
    }

    private boolean isAllocated(String var) {
        return variables.containsKey(var);
    }
}

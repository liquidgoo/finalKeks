import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chepin {
    private String prog;
    private HashSet<String> vars;
    public HashSet<String> input, modify, control, parasite;
    public Chepin chepinIO;
    private HashSet<String> ioVars = new HashSet<>();

    public Chepin(String prog, Set<String> vars, HashSet<String> ioVars) {
        this.prog = prog;
        this.vars = new HashSet<>(vars);
        divideByGroups();
        for (String var : vars) {
            if (!ioVars.contains(var)) {
                input.remove(var);
                modify.remove(var);
                control.remove(var);
                parasite.remove(var);
            }
        }
    }

    public Chepin(String prog, Set<String> vars) {
        this.prog = prog;
        this.vars = new HashSet<>(vars);
        divideByGroups();
        chepinIO = new Chepin(prog, vars, ioVars);

    }

    public double calculateMetric() {
        return input.size() + 2 * modify.size() + 3 * control.size() + 0.5 * parasite.size();
    }

    private void divideByGroups() {
        input = new HashSet<>();
        modify = new HashSet<>();
        control = new HashSet<>();
        parasite = new HashSet<>();

        String controlKeyWords = "(for\\()|(while\\()|(if\\()(switch\\()";    // начало поиска управляющих

        Matcher m = Pattern.compile(controlKeyWords).matcher(prog);
        while (m.find()) {
            int start = m.end();
            int end = endOfParentheses(start);
            String controlString = prog.substring(start, end);

            control.addAll(findVarsInString(controlString));
        }

        m = Pattern.compile("case ").matcher(prog);
        while (m.find()) {
            int start = m.end();
            int end = prog.indexOf(':', start);

            String caseString = prog.substring(start, end);

            control.addAll(findVarsInString(caseString));
        }                                                           //конец
        //_________________________________________________________________________________________

        HashSet<String> inputMethods = new HashSet<>();
        try {
            Files.lines(Paths.get("inputMethods.txt")).forEach(line -> inputMethods.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String modifier = "((?<!=)=(?!=))|(\\+\\+)|(--)";             //начало поиска модифицируемых и вводимых
        m = Pattern.compile(modifier).matcher(prog);
        while (m.find()) {
            int end = m.start();
            for (String var : vars) {
                int endOfModifier = endOfModifier(end);
                String line = prog.substring(end, endOfModifier);
                boolean isInput = false;
                for (String inputMethod : inputMethods) {       //вводим ли эту переменную
                    if (line.contains(inputMethod)) {
                        isInput = true;
                        break;
                    }
                }
                if (!control.contains(var) && prog.startsWith(var, end - var.length())) {
                    if (isInput) {      //если вводим, то в вводимые, если нет, то в модифицируемые
                        input.add(var);
                        ioVars.add(var);
                    } else {
                        input.remove(var);
                        modify.add(var);
                    }
                }
            }
        }
        for (String var : vars) {
            if (!(input.contains(var) || modify.contains(var) || control.contains(var))) {
                modify.add(var);      //добавляем оставшие переменные в модифицируемые, т.к. создаются в программе
            }
        }
        //_________________________________________________________________________________________
        LinkedList<String> useful = new LinkedList<>(control);
        HashSet<String> outputMethods = new HashSet<>();
        try {
            Files.lines(Paths.get("outputMethods.txt")).forEach(line -> outputMethods.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String outputMethod : outputMethods) {             //Вывоимые переменные
            int start = prog.indexOf(outputMethod);
            while (start != -1) {
                int previousOperatorEnd = start;
                while (previousOperatorEnd > -1 && !(prog.charAt(previousOperatorEnd) == ';' || prog.charAt(previousOperatorEnd) == '\n')) {
                    previousOperatorEnd--;
                }
                previousOperatorEnd++;
                String substr = prog.substring(previousOperatorEnd, start);
                HashSet<String> auxiliaryOutput = findVarsInString(substr);
                useful.addAll(auxiliaryOutput);

                start += outputMethod.length();
                int end = endOfParentheses(start);
                substr = prog.substring(start, end);

                HashSet<String> outputs = findVarsInString(substr);
                useful.addAll(outputs);
                ioVars.addAll(outputs);

                start = prog.indexOf(outputMethod, start);
            }
        }
        for (int i = 0; i < useful.size(); i++) {       //Переменные для вычисления выводимых
            m = Pattern.compile(modifier).matcher(prog);
            while (m.find()) {
                int start = m.start();
                String var = useful.get(i);
                if (prog.startsWith(var, start - var.length())) {
                    int end = endOfModifier(start);
                    String substr = prog.substring(start, end);
                    HashSet<String> founds = findVarsInString(substr);

                    for (String found : founds) {
                        if (!useful.contains(found)) useful.add(found);
                    }
                }
            }
        }

        for (String var : vars) {                   //Определение паразитных
            if (!useful.contains(var)) {
                parasite.add(var);
            }
        }
    }


    private int endOfModifier(int start) {
        int endOfLine = prog.indexOf('\n', start);
        int endOfOperator = prog.indexOf(';', start);
        if (endOfOperator == -1 || (endOfLine != -1 && endOfLine < endOfOperator)) endOfOperator = endOfLine;

        int endOfParentheses = endOfParentheses(start);
        if (endOfOperator == -1 || (endOfParentheses != -1 && endOfParentheses < endOfOperator))
            endOfOperator = endOfParentheses;

        return endOfOperator;
    }

    private HashSet<String> findVarsInString(String string) {
        HashSet<String> found = new HashSet<>();
        for (String var : vars) {
            if (string.contains(var)) {
                found.add(var);
            }
        }
        return found;
    }

    private int endOfParentheses(int start) {
        int parentheses = 1;
        int end = start;
        while (parentheses > 0 && end < prog.length()) {
            if (prog.charAt(end) == '(') parentheses++;
            if (prog.charAt(end) == ')') parentheses--;
            end++;
        }
        end--;
        return end < prog.length() ? end : -1;
    }
}

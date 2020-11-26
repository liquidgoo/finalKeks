import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spen {
    private HashMap<String, Integer> vars = new HashMap<>();
    private int sum = 0;

    public Spen(String prog) {
        HashSet<String> keyWords = new HashSet<>();
        try {
            Files.lines(Paths.get("keyWords.txt")).forEach(line -> keyWords.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }

        prog = prog.replace("(", "( ").replace(")", " )");
        String[] lines = prog.split("\\n");
        String id = "[A-z$_][A-z$_0-9]*";
        for (String line : lines) {
            String[] words = line.split(" +");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].matches(id) && words[i + 1].matches(id) && !(keyWords.contains(words[i]) || keyWords.contains(words[i + 1]))) {
                    prog = prog.replace(words[i] + " " + words[i + 1], words[i + 1]);
                }
            }
        }


        StringBuilder sb = new StringBuilder(prog);
        Pattern p = Pattern.compile(id + "\\s*" + "[(]");
        Matcher m = p.matcher(sb);
        while (m.find()) {
            sb.delete(m.start(), m.end());
            m = p.matcher(sb);
        }

        HashSet<String> operatorsWithBraces = new HashSet<>();
        try {
            Files.lines(Paths.get("keyWordsBraces.txt")).forEach(line -> operatorsWithBraces.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String op : operatorsWithBraces) {
            p = Pattern.compile(op + "\\s*[{]");
            m = p.matcher(sb);
            while (m.find()) {
                sb.delete(m.start(), m.end() - 1);
                m = p.matcher(sb);
            }
        }

        prog = sb.toString();
        //Удаление ключ слов(почти)
        for (String keyWord : keyWords) {
            prog = prog.replaceAll("[\\s\n]+" + keyWord + "\\s+", " ");
        }

        sb = new StringBuilder(prog);


        HashSet<String> operators = new HashSet<>();
        try {
            Files.lines(Paths.get("operators.txt")).forEach(line -> operators.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
//Подсчет и удаление оставшихся операторов
        for (String op : operators) {
            int start = 0;
            while (start != -1) {
                start = sb.indexOf(op, start);
                if (start != -1) {
                    sb.replace(start, start + op.length(), " ");
                    start += 1;
                }
            }
        }

        m = Pattern.compile(id).matcher(sb);
        while (m.find()) {
            String var = m.group();
            vars.put(var, vars.containsKey(var) ? vars.get(var) + 1 : 0);
        }

        for (int spen : vars.values()) {
            sum += spen;
        }
    }

    public Set<String> getVars() {
        return vars.keySet();
    }

    public HashMap<String, Integer> getSpens(){
        return vars;
    }

    public int getSum() {
        return sum;
    }
}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.regex.Pattern;

public class TrashRemover {
    String prog;
    final String id = "[A-z$_][A-z$_0-9]*";

    public TrashRemover(String prog) {
        prog = prog.replaceAll("(?s:/\\*.*?\\*/)|//.*", ""); //комменты
            prog = prog.replaceAll("\"(?:\\\\\"|[^\"])*?\"", ""); //литералы


        HashSet<String> keyWords = new HashSet<>();
        try {
            Files.lines(Paths.get("keyWords.txt")).forEach(line -> keyWords.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        prog = prog.replace("(", "( ").replace(")", " )");
        prog = " " + prog;
        for (String keyWord : keyWords) {
            prog = prog.replaceAll("(?<=\\s)+" + keyWord + " +", " ");
        }
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
        prog = prog.replaceAll(" ", "");
        prog = prog.replaceAll(";\\n", "\n");
        prog = prog.replaceAll("\\n+", "\n"); //пустые строки
        if (prog.charAt(prog.length() - 1) == '\n') {  //пустая строка на конце
            prog = prog.substring(0, prog.length() - 1);
        }



//        int i = prog.indexOf(" ");              //все пробелы кроме тех, что между типом и переменной
//        while (i != -1) {
//            if (!(prog.substring(0, i).matches("(?s).*" + id) && prog.substring(i + 1).matches(id + "(?s).*"))) {
//                prog = prog.substring(0, i) + prog.substring(i + 1);
//            } else i++;
//            i = prog.indexOf(" ", i);
//        }

        this.prog = prog;
    }

    public String getProg() {
        return prog;
    }
}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

public class Yacc {

    public static ArrayList<String[]> in = new ArrayList<String[]>();
    public static ArrayList<String[]> first = new ArrayList<String[]>();
    public static ArrayList<String[]> follow = new ArrayList<String[]>();
    public static ArrayList<String[]> track = new ArrayList<String[]>();
    public static ArrayList<String> Nonterminal = new ArrayList<String>();
    public static ArrayList<String> terminal = new ArrayList<String>();
    public static ArrayList<String[]> first2 = new ArrayList<String[]>();
    public static String[][] table2;
    public static int[][] table;

    public static void main(String[] args) {
        String source = args[0];
        ArrayList<String> filePath = new ArrayList<String>();
        File f = null;
        f = new File(args[0]);
        File[] files = f.listFiles();   
        List<File> list = new ArrayList<File>();
        for (File file : files) {
            list.add(file);
        }
        for (File file : files) {
            String path = file.getAbsolutePath();
            if (path.charAt(path.length() - 1) == 'f'||path.charAt(path.length() - 1) == 'F') {
                source=path;
            } else {
                filePath.add(path);
            }
        }
        File file = new File(source);
        BufferedReader reader = null;
        String sline;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                StringBuffer buffer = new StringBuffer(tempString);
                int l = buffer.indexOf(" ");
                while (l > 0) {
                    buffer.delete(l, l + 1);
                    l = buffer.indexOf(" ");
                }
                sline = buffer.toString();
                String s[] = sline.split("::=");
                if (s.length == 1) {
                    System.out.println("文法有误！");
                    System.exit(0);
                }
                StringTokenizer fx = new StringTokenizer(s[1], "|");
                while (fx.hasMoreTokens()) {
                    String[] one = new String[2];
                    one[0] = s[0];
                    one[1] = fx.nextToken();
                    in.add(one);
                }
                Nonterminal.add(s[0]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        if (in.size() == 0) {
            System.out.println("没有输入文法！");
            return;
        }
        isTerminal();
        process("First");
        firstAction();
        track.clear();
        process("Follow");
        followAction();
        followFinal();
        getFirst2();
        boolean isSure = getTable();
        if (!isSure) {
            System.out.println("非LL(1)文法！");
            System.exit(0);
        }
        for (int q = 0; q < filePath.size(); q++) {
            String path = filePath.get(q);
            file = new File(path);
            reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempString = "";
                String temp = null;
                boolean is = true;
                while ((temp = reader.readLine()) != null) {
                    is = false;
                    StringBuffer buffer = new StringBuffer(temp);
                    int l = buffer.indexOf(" ");
                    while (l > 0) {
                        buffer.delete(l, l + 1);
                        l = buffer.indexOf(" ");
                    }
                    tempString = tempString + buffer.toString();
                }
                if (is) {
                    System.out.println("没有输入！");
                    System.exit(0);
                }
                isSure = isSure(tempString);
                if (isSure) {
                    System.out.println("yes");
                } else {
                    System.out.println("no");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
    }

    public static void isTerminal() {
        for (int i = 0; i < in.size(); i++) {
            String[] temp = in.get(i);
            int x = 0;
            while (x + 1 < temp[1].length()) {
                int index = getNext(temp[1], x) + 1;
                String t = temp[1].substring(x, index);
                if (!isNonterminal(t)) {
                    if (!terminal.contains(t)) {
                        terminal.add(t);
                    }
                }
                x = index;
            }
        }
    }

    public static int addAnother(String s, String t) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result2 = new ArrayList<String>();
        boolean is = true;
        int x = 0, y = 0;
        for (int i = 0; i < first.size(); i++) {
            String[] temp = first.get(i);
            if (temp[0].equals(s)) {
                x = i;
                for (int j = 1; j < temp.length; j++) {
                    result.add(temp[j]);
                }
            }
            if (temp[0].equals(t)) {
                y = i;
                for (int j = 1; j < temp.length; j++) {
                    result2.add(temp[j]);
                }
            }
        }
        if (!isNonterminal(t)) {
            if (result.contains(t)) {
                return 0;
            }
            result.add(t);
            String[] sf = new String[result.size() + 1];
            sf[0] = first.get(x)[0];
            for (int w = 1; w <= result.size(); w++) {
                sf[w] = result.get(w - 1);
            }
            first.set(x, sf);
            return 0;
        }
        for (int i = 0; i < result2.size(); i++) {
            String temp = result2.get(i);
            if (!temp.equals("\"\"")) {
                if (!result.contains(temp)) {
                    result.add(temp);
                    is = false;
                }
            }
        }
        if (is) {
            return 0;
        }
        String[] sf = new String[result.size() + 1];
        sf[0] = first.get(x)[0];
        for (int w = 1; w <= result.size(); w++) {
            sf[w] = result.get(w - 1);
        }
        first.set(x, sf);
        return 1;
    }

    public static void firstAction() {
        ArrayList<String> number = new ArrayList<String>();
        for (int i = 0; i < first.size(); i++) {
            ArrayList<String> result = new ArrayList<String>();
            String[] result1 = first.get(i);
            for (int j = 0; j < result1.length; j++) {
                result.add(result1[j]);
            }
            if (result.contains("\"\"")) {
                number.add(String.valueOf(i));
            }
        }
        for (int i = 0; i < in.size(); i++) {
            String[] result = in.get(i);
            int x = 0, y = 0;
            for (int j = 0; j < number.size(); j++) {
                int b = Integer.parseInt(number.get(j));
                String N = Nonterminal.get(b);
                if (result[1].indexOf(N) == x) {
                    if (result[1].endsWith(N)) {
                        ArrayList<String> result3 = new ArrayList<String>();
                        int z = 0;
                        for (int k = 0; k < first.size(); k++) {
                            String[] temp = first.get(k);
                            if (temp[0].equals(result[0])) {
                                z = k;
                                for (int l = 1; l < temp.length; l++) {
                                    result3.add(temp[l]);
                                }
                            }
                        }
                        if (!result3.contains("\"\"")) {
                            result3.add("\"\"");
                            String[] sf = new String[result3.size() + 1];
                            sf[0] = first.get(z)[0];
                            for (int w = 1; w <= result3.size(); w++) {
                                sf[w] = result3.get(w - 1);
                            }
                            first.set(z, sf);
                        }
                    } else {
                        int index = getNext(result[1], N.length());
                        String A = result[1].substring(N.length(), index + 1);
                        int fina = addAnother(result[0], A);
                        if (fina == 0) {
                            j = number.size();
                        } else {
                            j = 0;
                            y = x;
                        }
                    }
                }
            }
        }
    }

    public static void printFirst() {
        System.out.println("\nFirst集：");
        for (int i = 0; i < first.size(); i++) {
            String[] r = first.get(i);
            System.out.print(r[0]);
            System.out.print(":");
            for (int j = 1; j < r.length; j++) {
                System.out.print(r[j]);
                if (j < r.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public static void printFollow() {
        System.out.println("\nFollow集：");
        for (int i = 0; i < follow.size(); i++) {
            String[] r = follow.get(i);
            System.out.print(r[0]);
            System.out.print(":");
            for (int j = 1; j < r.length; j++) {
                System.out.print(r[j]);
                if (j < r.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public static void followFinal() {
        for (int i = 0; i < follow.size(); i++) {
            ArrayList<String> result = new ArrayList<String>();
            String[] result1 = follow.get(i);
            for (int j = 0; j < result1.length; j++) {
                result.add(result1[j]);
            }
            if (result.contains("#")) {
                String[] sf = new String[result.size() - 1];
                for (int w = 0, k = 0; w < result.size(); w++) {
                    if (!result.get(w).equals("#")) {
                        sf[k] = result.get(w);
                        k++;
                    }
                }
                follow.set(i, sf);
            }
        }
    }

    public static void followAction() {
        ArrayList<String> number = new ArrayList<String>();
        for (int i = 0; i < follow.size(); i++) {
            ArrayList<String> result = new ArrayList<String>();
            String[] result1 = follow.get(i);
            for (int j = 0; j < result1.length; j++) {
                result.add(result1[j]);
            }
            if (result.contains("#")) {
                number.add(String.valueOf(i));
            }
        }
        boolean p = false;
        for (int i = 0; i < in.size(); i++) {
            String[] temp = in.get(i);
            for (int j = 0; j < number.size(); j++) {
                int b = Integer.parseInt(number.get(j));
                String N = Nonterminal.get(b);
                if (temp[1].endsWith(N)) {
                    if (!temp[0].equals(N)) {
                        boolean h = setAnother(temp[0], N);
                        if (h) {
                            p = true;
                        }
                        int x = temp[1].length() - N.length();
                        while (hasNull(N) && x > 0) {
                            int index = getEnd(temp[1], x - 1);
                            if (index < 0) {
                                break;
                            }
                            N = temp[1].substring(index, x);
                            if (isNonterminal(N)) {
                                boolean n = setAnother(temp[0], N);
                                if (n) {
                                    p = true;
                                }
                            }
                            x = index;
                        }
                    }
                }
            }
            if (p && (i + 1) == in.size()) {
                i = 0;
                p = false;
            }
        }
    }

    public static boolean hasNull(String s) {
        for (int i = 0; i < first.size(); i++) {
            ArrayList<String> result = new ArrayList<String>();
            String[] result1 = first.get(i);
            if (result1[0].equals(s)) {
                for (int j = 0; j < result1.length; j++) {
                    result.add(result1[j]);
                }
                if (result.contains("\"\"")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean setAnother(String s, String e) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result2 = new ArrayList<String>();
        int x = 0, y = 0;
        for (int i = 0; i < follow.size(); i++) {
            String[] temp = follow.get(i);
            if (temp[0].equals(s)) {
                for (int j = 1; j < temp.length; j++) {
                    result.add(temp[j]);
                }
                y = i;
            }
            if (temp[0].equals(e)) {
                for (int j = 1; j < temp.length; j++) {
                    result2.add(temp[j]);
                }
                x = i;
            }
        }
        boolean p = false;
        for (int i = 0; i < result.size(); i++) {
            String te = result.get(i);
            if (!result2.contains(te)) {
                result2.add(te);
                p = true;
            }
        }
        if (p) {
            String[] sf = new String[result2.size() + 1];
            sf[0] = follow.get(x)[0];
            for (int w = 1; w < result2.size() + 1; w++) {
                sf[w] = result2.get(w - 1);
            }
            follow.set(x, sf);
            return true;
        }
        return false;
    }

    public static void getFirst2() {
        for (int i = 0; i < in.size(); i++) {
            String[] temp = in.get(i);
            ArrayList<String> a = null;
            a = getAfirst(temp[0], temp[1]);
            String[] sf = new String[a.size() + 1];
            sf[0] = temp[0];
            for (int j = 0, u = 1; j < a.size(); j++, u++) {
                sf[u] = a.get(j);
            }
            first2.add(sf);
        }
    }

    public static boolean getTable() {
        int h = Nonterminal.size();
        int w = terminal.size();
        table = new int[h][w];
        table2 = new String[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                table[i][j] = -1;
                table2[i][j] = "error";
            }
        }
        int x, y;
        for (int i = 0; i < in.size(); i++) {
            String[] temp2 = first2.get(i);
            x = getIndexOfN(temp2[0]);
            for (int j = 1; j < temp2.length; j++) {
                y = getIndexOfT(temp2[j]);
                if (temp2[j].equals("\"\"")) {
                    int dex = getIndexOfN(temp2[0]);
                    String[] fo = follow.get(dex);
                    for (int k = 1; k < fo.length; k++) {
                        if (fo[k].endsWith("$")) {
                            y = getIndexOfT("\"\"");
                            if (table[x][y] == -1 || table[x][y] == i) {
                                table[x][y] = i;
                                String[] t = in.get(i);
                                table2[x][y] = t[0] + "::=" + t[1];
                            } else {
                                return false;
                            }
                        } else {
                            y = getIndexOfT(fo[k]);
                            if (table[x][y] == -1 || table[x][y] == i) {
                                table[x][y] = i;
                                String[] t = in.get(i);
                                table2[x][y] = t[0] + "::=" + t[1];
                            } else {
                                return false;
                            }
                        }
                    }
                } else {
                    if (table[x][y] == -1 || table[x][y] == i) {
                        table[x][y] = i;
                        String[] t = in.get(i);
                        table2[x][y] = t[0] + "::=" + t[1];
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void printTable() {
        for (int i = 0; i < Nonterminal.size(); i++) {
            for (int j = 0; j < terminal.size(); j++) {
                System.out.print(table[i][j] + "    ");
            }
            System.out.println();
        }
    }

    public static void printTable2() {
        for (int i = 0; i < Nonterminal.size(); i++) {
            for (int j = 0; j < terminal.size(); j++) {
                System.out.print(table2[i][j] + "             ");
            }
            System.out.println();
        }
    }

    public static int getIndexOfN(String s) {
        for (int i = 0; i < Nonterminal.size(); i++) {
            if (s.equals(Nonterminal.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static int getIndexOfT(String s) {
        for (int i = 0; i < terminal.size(); i++) {
            if (s.equals(terminal.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static void printAfirst() {
        System.out.println("\nFirst2集：");
        for (int i = 0; i < first2.size(); i++) {
            String[] r = first2.get(i);
            System.out.print(r[0]);
            System.out.print(":");
            for (int j = 1; j < r.length; j++) {
                System.out.print(r[j]);
                if (j < r.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public static ArrayList<String> getAfirst(String s, String e) {
        ArrayList<String> result = new ArrayList<String>();
        int x = 0;
        int index = getNext(e, x) + 1;
        String f = e.substring(x, index);
        if (!isNonterminal(f)) {
            result.add(f);
            return result;
        } else {
            for (int i = 0; i < first.size(); i++) {
                String[] temp = first.get(i);
                if (temp[0].equals(f)) {
                    for (int j = 1; j < temp.length; j++) {
                        if (!temp[j].equals("\"\"")) {
                            result.add(temp[j]);
                        } else {
                            x = index;
                            if (x < e.length()) {
                                index = getNext(e, x) + 1;
                                f = e.substring(x, index);
                                i = 0;
                            }
                            if (x == e.length()) {
                                result.add("\"\"");
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean isSure(String s) {
        Stack<String> stack = new Stack<String>();
        stack.push(in.get(0)[0]);
        int x, y, u, d = 0, index;
        String e, f;
        while (d < s.length()) {
            index = getNext(s, d) + 1;
            e = s.substring(d, index);
            if (stack.isEmpty() && d < s.length()) {
                return false;
            }
            while (!stack.isEmpty()) {
                f = stack.pop().toString();
                if (isNonterminal(f)) {
                    x = getIndexOfN(f);
                    y = getIndexOfT(e);
                    u = table[x][y];
                    if (u == -1) {
                        return false;
                    } else {
                        String temp = in.get(u)[1];
                        if (!temp.equals("\"\"")) {
                            int d2 = temp.length(), index2;
                            while (d2 > 0) {
                                index2 = getEnd(temp, d2 - 1);
                                stack.push(temp.substring(index2, d2));
                                d2 = index2;
                            }
                        }
                    }
                } else {
                    d = index;
                    break;
                }
            }
        }
        while (!stack.isEmpty()) {
            f = stack.pop().toString();
            if (!terminal.contains("\"\"")) {
                return false;
            }
            if (!isNonterminal(f)) {
                return false;
            }
            x = getIndexOfN(f);
            y = getIndexOfT("\"\"");
            u = table[x][y];
            if (u == -1) {
                return false;
            } else {
                String temp = in.get(u)[1];
                if (!temp.equals("\"\"")) {
                    int d2 = temp.length(), index2;
                    while (d2 > 0) {
                        index2 = getEnd(temp, d2 - 1);
                        stack.push(temp.substring(index2, d2));
                        d2 = index2;
                    }
                }
            }
        }
        if (!stack.isEmpty()) {
            return false;
        }
        return true;
    }

    public static int getNext(String s, int index) {
        int i = index + 1;
        switch (s.charAt(index)) {
            case '<':
                while (s.charAt(i) != '>') {
                    i++;
                }
                break;
            case '"':
                while (s.charAt(i) != '"') {
                    i++;
                }
                break;
        }
        return i;
    }

    public static int getEnd(String s, int index) {
        int i = index - 1;
        switch (s.charAt(index)) {
            case '>':
                while (s.charAt(i) != '<') {
                    i--;
                }
                break;
            case '"':
                while (s.charAt(i) != '"') {
                    i--;
                }
                break;
        }
        return i;
    }

    public static boolean isNonterminal(String s) {
        for (int i = 0; i < Nonterminal.size(); i++) {
            if (s.equals(Nonterminal.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static int isMore(String s) {
        int l = 0, r = 0;
        switch (s.charAt(0)) {
            case '<':
                l = s.indexOf("><", 1);
                r = s.indexOf(">\"", 1);
                if (l == -1) {
                    return r;
                } else if (r == -1) {
                    return l;
                } else {
                    if (l < r) {
                        return l;
                    } else {
                        return r;
                    }
                }
            case '"':
                l = s.indexOf("\"<", 1);
                r = s.indexOf("\"\"", 1);
                if (l == -1) {
                    return r;
                } else if (r == -1) {
                    return l;
                } else {
                    if (l < r) {
                        return l;
                    } else {
                        return r;
                    }
                }
        }
        return -1;
    }

    public static void process(String firstORfollow) {
        for (int i = 0; i < in.size(); i++) {
            boolean bool = true;
            for (int j = 0; j < i; j++) {
                if (in.get(j)[0].equals(in.get(i)[0])) {
                    bool = false;
                }
            }
            if (bool) {
                ArrayList<String> a = null;
                if (firstORfollow.equals("First")) {
                    a = getFirst(in.get(i)[0], "First(" + in.get(i)[0] + ")/");
                } else if (firstORfollow.equals("Follow")) {
                    a = getFollow(in.get(i)[0], in.get(i)[0], "");
                }
                String[] sf = new String[a.size() / 2 + 1];
                String[] st = new String[a.size() / 2];
                if (firstORfollow.equals("First")) {
                    sf[0] = in.get(i)[0];
                    for (int j = 0; j < a.size(); j++) {
                        if (j % 2 == 0) {
                            sf[j / 2 + 1] = a.get(j);
                        } else {
                            st[j / 2] = a.get(j);
                        }
                    }
                    first.add(sf);
                    track.add(st);
                } else if (firstORfollow.equals("Follow")) {
                    if (!a.contains("\"\"")) {
                        String[] ss = new String[a.size() / 2 + 1];
                        String[] sd = new String[a.size() / 2];
                        ss[0] = in.get(i)[0];
                        for (int j = 0; j < a.size(); j++) {
                            if (j % 2 == 0) {
                                ss[j / 2 + 1] = a.get(j);
                            } else {
                                sd[j / 2] = a.get(j);
                            }
                        }
                        follow.add(ss);
                        track.add(sd);
                    } else {
                        String[] ss = new String[a.size() / 2];
                        String[] sd = new String[a.size() / 2 - 1];
                        ss[0] = in.get(i)[0];
                        for (int j = 0, w = 1; j < a.size(); j++) {
                            if (a.get(j).equals("\"\"")) {
                                j++;
                            } else {
                                if (j % 2 == 0) {
                                    ss[w] = a.get(j);
                                } else {
                                    sd[w - 1] = a.get(j);
                                    w++;
                                }
                            }
                        }
                        follow.add(ss);
                        track.add(sd);
                    }
                }
            }
        }
    }

    public static ArrayList<String> getFirst(String s, String track1) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result1 = new ArrayList<String>();
        String t = "First(" + s + ")/";
        int l = t.length();
        if (isNonterminal(s)) {
            for (int i = 0; i < in.size(); i++) {
                String[] one = in.get(i);
                if (s.equals(one[0])) {
                    if (track1.substring(0, track1.length() - l).indexOf("First(" + s + ")") >= 0)
                            ; else if (isMore(one[1]) == -1) {
                        result1 = getFirst(one[1], track1 + "First(" + one[1] + ")/");
                    } else if (isMore(one[1]) > 0) {
                        result1 = getFirst(one[1].substring(0, isMore(one[1]) + 1), track1 + "First(" + one[1].substring(0, isMore(one[1]) + 1) + ")/");
                    }
                    result = addArrayString(result, result1);
                    result1.clear();
                }
            }
        } else {
            if (s.equals("\"\"")) {
                result1.add("\"\"");
            } else {
                result1.add(s);
            }
            result1.add(track1);
            result = result1;
        }
        return result;
    }

    public static ArrayList<String> getFollow(String s, String element, String track1) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result1 = new ArrayList<String>();
        if (isNonterminal(s)) {
            for (int i = 0; i < in.size(); i++) {
                String[] one = in.get(i);
                int slen = s.length();
                int olen = one[1].length();
                if (element.equals(in.get(0)[0])) {
                    result1.add("$");
                    result1.add(in.get(0)[0] + "→" + in.get(0)[0] + "\t");
                }
                result = addArrayString(result, result1);
                result1.clear();
                if (one[1].indexOf(s) >= 0 && track1.indexOf((char) ('a' + i) + "") >= 0)
                        ; else if (one[1].indexOf(s) >= 0) {
                    int index = -1;
                    index = one[1].indexOf(s, 0);
                    while (index >= 0) {
                        if (olen - slen == index) {
                            result1 = getFollow(one[0], element, track1 + (char) ('a' + i));
                            result = addArrayString(result, result1);
                            result1.clear();
                        } else {
                            int t = index + slen;
                            result1 = returnFirstofFollow(s, element, track1, one[0], one[1], index, t);
                            result = addArrayString(result, result1);
                            result1.clear();
                        }
                        index = one[1].indexOf(s, index + slen);
                    }
                }
                if (one[1].endsWith(element)) {
                    result1.add("#");
                    result1.add(in.get(0)[0] + "→" + one[1] + "\t");
                    result = addArrayString(result, result1);
                    result1.clear();
                }
            }
        }
        return result;
    }

    public static ArrayList<String> returnFirstofFollow(String s, String element, String track1, String one0, String one1, int index, int t) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> result1 = new ArrayList<String>();
        ArrayList<String> beckFirst;
        String lsh;
        int nextIndex = getNext(one1, t);
        lsh = one1.substring(t, nextIndex + 1);
        String[] ls = null;
        int beflen = 2;
        if (track1.length() > 0) {
            ls = in.get(track1.charAt(track1.length() - 1) - 'a');
            int endIndex = getEnd(ls[1], ls[1].length() - 1);
            if (isNonterminal(ls[1].substring(endIndex, ls[1].length() - 1))) {
                beflen = 1;
            }
        }
        beckFirst = getFirst(lsh, "First(" + lsh + ")/");
        for (int j = 0; j < beckFirst.size() / 2; j++) {
            String lh = "";
            if (beckFirst.get(j * 2).equals("$")) {
                result1.add(beckFirst.get(j * 2));
                if (ls == null) {
                    lh = in.get(0)[0] + "→" + one1 + "→" + one1.substring(0, index) + element + "ε" + one1.substring(t + lsh.length(), one1.length());
                } else {
                    lh = in.get(0)[0] + "→" + one1 + "→" + one1.substring(0, index) + ls[1] + one1.substring(index + s.length(), one1.length())
                            + "→." + element + "ε" + one1.substring(t + lsh.length(), one1.length());
                }
                result1.add(lh);
                result = addArrayString(result, result1);
                result1.clear();
                if (1 + index + lsh.length() < one1.length()) {
                    result1 = returnFirstofFollow(s, element, track1, one0, one1, index, t + lsh.length());
                } else {
                    result1 = getFollow(one0, element, track1);
                }
            } else {
                if (isNonterminal(one1.substring(t, nextIndex))) {
                    if (ls == null) {
                        lh = in.get(0)[0] + "→" + one1 + "→" + one1.substring(0, index) + element + beckFirst.get(j * 2) + one1.substring(t + lsh.length(), one1.length());
                    } else {
                        lh = in.get(0)[0] + "→" + one1 + "→" + one1.substring(0, index) + ls[1] + one1.substring(index + s.length(), one1.length())
                                + "→." + element + beckFirst.get(j * 2) + one1.substring(t + lsh.length(), one1.length());
                    }
                } else {
                    if (ls == null) {
                        if (element == in.get(0)[0] || s.equals(element)) {
                            lh = in.get(0)[0] + "→" + one1.substring(0, index) + element + one1.substring(t, one1.length()) + "\t";
                        } else {
                            lh = in.get(0)[0] + "→" + one1 + "" + one1.substring(0, index) + element + one1.substring(t, one1.length()) + "\t";
                        }
                    } else {
                        if (ls[1].length() == 1 || ls[1].length() == 2 && !ls[1].endsWith("’") && !ls[1].endsWith("\'")) {
                            lh = in.get(0)[0]
                                    + "→" + one1 + "" + one1.substring(0, index) + element + one1.substring(t, one1.length());
                        } else {
                            lh = in.get(0)[0] + "→" + one1 + "" + one1.substring(0, index) + ls[1] + one1.substring(index + s.length(), one1.length())
                                    + "→→→." + element + one1.substring(t, one1.length()) + "!";
                        }
                    }
                }
                result1.add(beckFirst.get(j * 2));
                result1.add(lh);
            }
        }
        result = addArrayString(result, result1);
        result1.clear();
        return result;
    }

    public static ArrayList<String> addArrayString(ArrayList<String> a, ArrayList<String> b) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < a.size(); i += 2) {
            String s = a.get(i);
            if (result.contains(s) || s.equals("")) {
                int index = result.indexOf(s);
                if (result.get(index + 1).length() > a.get(i + 1).length()) {
                    result.set(index, s);
                    result.set(index + 1, a.get(i + 1));
                }
                continue;
            }
            result.add(s);
            result.add(a.get(i + 1));
        }
        for (int i = 0; i < b.size(); i += 2) {
            String s = b.get(i);
            if (result.contains(s) || s.equals("")) {
                int index = result.indexOf(s);
                if (result.get(index + 1).length() > b.get(i + 1).length()) {
                    result.set(index, s);
                    result.set(index + 1, b.get(i + 1));
                }
                continue;
            }
            result.add(s);
            result.add(b.get(i + 1));
        }
        return result;
    }
}

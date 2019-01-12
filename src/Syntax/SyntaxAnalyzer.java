package Syntax;

import Lexical.LexicalAnalyzer;
import Lexical.RE.Type;
import Lexical.Token;
import Syntax.entity.I;
import Syntax.entity.Sentence;

import java.io.*;
import java.util.*;

public class SyntaxAnalyzer {

    private static String path = "/Users/panxy/IdeaProjects/SyntaxAnalyzer/src/";

    ArrayList<I> iStates;
    ArrayList<Sentence> commands;
    ArrayList<Sentence> sequence;
    HashMap<Integer, I> iTable;
    HashMap<String, Boolean> isFollowed;
    Stack<Token> tokenStack;
    Stack<I> stateStack;
    int id;

    public SyntaxAnalyzer() {
        id = 0;
        iStates = new ArrayList<>();
        sequence = new ArrayList<>();
        commands = new ArrayList<>();
        iTable = new HashMap<>();
        tokenStack = new Stack<>();
        stateStack = new Stack<>();
        isFollowed = new HashMap<>();
    }

    /**
     * 语法测定
     *
     * @param tokens
     */
    public void parse(ArrayList<Token> tokens) {
        tokens.add(new Token(Type.Vt, "$"));
        tokenStack.push(new Token(Type.Vn, "$"));
        int current = 0;
        int index = 0;
        boolean isReduct = false;
        while (true) {
            I i = iTable.get(current);
            if (!isReduct)
                stateStack.push(i);
            Token token = tokens.get(index);
            if (i.getNextState().get(token.getToken()) == null || token.getToken() == "$") {
                int item = i.getNextState().get("$");
                Sentence sentence = commands.get(item);
                sequence.add(sentence);
                ArrayList<Token> tokenPop = sentence.tokens;
                for (int j = tokenPop.size() - 1; j >= 0; j--) {
                    if (tokenPop.get(j).getToken().equals("ε"))
                        break;
                    if (!tokenPop.get(j).getToken().equals(tokenStack.pop().getToken()))
                        throw new RuntimeException("Not match");
                    stateStack.pop();
                }
                String start = sentence.start;
                tokenStack.push(new Token(Type.Vn, sentence.start));
                int lastId = stateStack.lastElement().getId();
                if (iTable.get(lastId).getNextState().get(start) == null)
                    break;
                int nextId = iTable.get(lastId).getNextState().get(start);
                stateStack.push(iTable.get(nextId));
                current = nextId;
                isReduct = true;
                continue;
            }
            isReduct = false;
            tokenStack.push(token);
            current = i.getNextState().get(tokenStack.lastElement().getToken());
            index++;
        }
    }

    /**
     * 构造LR(1)分析表
     *
     * @param cfg
     */
    public void constructLRTable(ArrayList<String> cfg) {
        String start = "S'->S";
        commands.add(new Sentence(start, 0));
        for (String s : cfg)
            commands.add(new Sentence(s, 0));
        Sentence firstSentence = new Sentence(commands.get(0).value, commands.get(0).dot);
        firstSentence.addReduction(new Token(Type.Vt, "$"));
        I i0 = new I(id++, commands);
        i0.addOne(firstSentence);
        i0.setInTable(true);
        iStates.add(i0);
        addI(i0);
        for (I i : iStates) {
            iTable.put(i.getId(), i);
        }
    }

    public ArrayList<Token> transfer(ArrayList<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == Type.Id)
                token.setToken("id");
            else if (token.getType() == Type.INTEG)
                token.setToken("num");
        }
        return tokens;
    }

    /**
     * 输出LR(1)分析表
     */
    public void printourLRTable() {
        for (int i = 0; i < commands.size(); i++)
            System.out.println("(" + i + ") " + commands.get(i).value);
        System.out.println();
        for (int index : iTable.keySet()) {
            I i = iTable.get(index);
            System.out.println("I" + i.getId() + ": ");
            for (Sentence sentence : i.getSentences()) {
                System.out.print(sentence.value + " " + sentence.dot + " ,");
                for (Token token : sentence.getReductionSet()) {
                    System.out.print(token.getToken() + "|");
                }
                System.out.println();
            }
            System.out.println("Next state:");
            for (String key : i.getNextState().keySet()) {
                System.out.println("    " + key + "->I" + i.getNextState().get(key));
            }
            System.out.println("Reduction:");
            System.out.print("    ");
            for (int r : i.getReduction())
                System.out.print("r" + r + " ");
            System.out.println();
            System.out.println();
        }
    }

    /**
     * 画状态
     *
     * @param lastI 上一个状态
     */
    public void addI(I lastI) {
        for (Sentence sentence : lastI.getSentences()) {
            if (sentence.isProcess()) {
                I i = new I(id++, commands);
                Sentence newISentence = new Sentence(sentence.value, sentence.dot);
                newISentence.dot++;
                newISentence.addAllReduction(sentence.getReductionSet());
                i.addOne(newISentence);
                boolean isHas = false;
                for (I hasI : iStates) {
                    if (i.isEqual(hasI)) {
                        id--;
                        i = hasI;
                        isHas = true;
                        break;
                    }
                }
                lastI.addState(sentence.getCurrent().getToken(), i.getId());
                if (!isHas)
                    iStates.add(i);
            }
        }

        for (String key : lastI.getNextState().keySet()) {
            I nextI = iStates.get(lastI.getNextState().get(key));
            if (!nextI.isAdded()) {
                nextI.setAdded(true);
                addI(nextI);
            }
        }
    }

    /**
     * 求first集
     *
     * @param token
     * @return
     */
    public Set<Token> first(Token token) {
        Set<Token> tmp = new HashSet<>();
        for (Sentence sentence : commands) {
            if (sentence.start.equals(token.getToken())) {
                Token tmpToken = sentence.tokens.get(0);
                if (tmpToken.getType() != Type.Vn)
                    tmp.add(tmpToken);
                else
                    tmp.addAll(first(tmpToken));
            }
        }
        return tmp;
    }

    /**
     * 求所有的follow集
     *
     * @param token
     * @return
     */
    public Set<Token> follow(Token token) {
        isFollowed.put(token.getToken(), true);
        Set<Token> tmp = new HashSet<>();
        for (Sentence sentence : commands) {
            for (int i = 0, len = sentence.tokens.size(); i < len; i++)
                if (sentence.tokens.get(i).getToken().equals(token.getToken())) {
                    if (i + 1 == len) {
                        if (isFollowed.get(sentence.start) == null || !isFollowed.get(sentence.start)) {
                            tmp.addAll(follow(new Token(Type.Vn, sentence.start)));
                        }
                    } else if (sentence.tokens.get(i + 1).getType() != Type.Vn)
                        tmp.add(sentence.tokens.get(i + 1));
                    else {
                        Set<Token> firstSet = first(sentence.tokens.get(i + 1));
                        for (Token firstToken : firstSet)
                            if (!firstToken.getToken().equals("ε"))
                                tmp.add(firstToken);
                    }
                }
        }
        boolean isDot = false;
        for (Token dot : tmp) {
            if (dot.getToken().equals("$")) {
                isDot = true;
                break;
            }
        }
        if (!isDot)
            tmp.add(new Token(Type.Vt, "$"));
        return tmp;
    }

    /**
     * 获取非终结符
     *
     * @return
     */
    public Set<String> getVts() {
        ArrayList<String> cfg = getCFG();
        Set<String> vts = new HashSet<>();
        for (String sentence : cfg) {
            String[] param = sentence.split("-");
            vts.add(param[0]);
        }
        return vts;
    }

    /**
     * 获取CFG
     *
     * @return
     */
    public ArrayList<String> getCFG() {
        ArrayList<String> cfg = new ArrayList<>();
        String inputFile = path + "CFG.txt";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
            String line = "";
            while ((line = br.readLine()) != null) {
                cfg.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    /**
     * 获取输入流
     *
     * @return
     */
    public String getInput() {
        String stream = "";
        String inputFile = path + "input.txt";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
            String line = "";
            char[] temp = null;
            while ((line = br.readLine()) != null) {
                stream += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    /**
     * 输出
     */
    public void setOutput() {
        String outputFile = path + "output.txt";
        String stream = "";
        for (Sentence sentence : sequence) {
            stream += sentence.value;
            stream += "\n";
        }
        try {
            File file = new File(outputFile);
            FileWriter writer = new FileWriter(file, false);
            PrintWriter printWriter = new PrintWriter(writer);
            printWriter.write(stream);
            printWriter.println();
            writer.close();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SyntaxAnalyzer analyzer = new SyntaxAnalyzer();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        ArrayList<Token> tokens = lexicalAnalyzer.getTokens(analyzer.getInput());
        analyzer.constructLRTable(analyzer.getCFG());
        analyzer.printourLRTable();
//        analyzer.parse(analyzer.transfer(tokens));
//        analyzer.setOutput();
//        for (Sentence sentence : analyzer.sequence)
//            System.out.println(sentence.value);
    }

}

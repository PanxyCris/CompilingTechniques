package Lexical;

import Lexical.RE.Type;

import java.io.*;
import java.util.ArrayList;

public class LexicalAnalyzer {

    private static String path = "/Users/panxy/IdeaProjects/SyntaxAnalyzer/src/";

    private static String[] keywords = {"public", "protected", "private", "class", "static", "void", "main", "String", "int", "double", "float", "char", "if", "else", "else if", "do", "while", "try", "catch", "finally", "case", "switch", "case", "break", "for"};

    private static String[] operation = {"++", "--", "<<", ">>", "<>", ">", "==", "!=", "&&", "||", "+=", "-=", "<=", ">=", "[", "]", "(", ")", "!", "~", "*", "/", "%", "+", "-", "<", "=", "|", "&", "^"};

    private static char[] partition = {',', '{', '}', ';', '.'};

    private static String reId = "a(a|b)*";

    private static String reInteger = "bb*";

    private static String reString = "(a|b)*";

    /**
     * 获取最后的token
     * @return
     */
    public ArrayList<Token> getTokens(String stream){
        ArrayList<Token> preProcessTokens = preProcess(stream);
        for (int i = 0, len = preProcessTokens.size(); i < len; i++) {
            Token token = preProcessTokens.get(i);
            if (token.getType() == Type.UNKNOWN)
                preProcessTokens.set(i, reProcess(token));
        }
        return preProcessTokens;
    }

    /**
     * 预处理
     *
     * @param stream
     */
    public ArrayList<Token> preProcess(String stream) {
        ArrayList<Token> tokens = new ArrayList<>();
        String buffer = "";
        boolean flag = false;
        boolean isPartion = false;
        for (int i = 0; i < stream.length(); i++) {
            char ch = stream.charAt(i);
            /**
             * 是否为空格
             */
            if (ch == ' ' || ch == '\n' || ch == '\t') {
                flag = true;
                continue;
            }
            /**
             * 是否为普通字符
             */
            if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                if (flag) {
                    if (buffer.length() != 0)
                        tokens.add(new Token(Type.UNKNOWN, buffer));
                    buffer = "";
                    flag = false;
                }
                buffer += ch;
                continue;
            }
            /**
             * 是否为分隔符
             */
            for (int j = 0; j < partition.length; j++)
                if (ch == partition[j]) {
                    if (buffer.length() != 0)
                        tokens.add(new Token(Type.UNKNOWN, buffer));
                    buffer = "";
                    tokens.add(new Token(Type.Partion, String.valueOf(ch)));
                    isPartion = true;
                    flag = false;
                    break;
                }
            if (isPartion) {
                isPartion = false;
                continue;
            }
            /**
             * 是否为操作符
             */
            for (int j = 0; j < operation.length; j++) {
                if (operation[j].length() == 1) {
                    if (String.valueOf(ch).equals(operation[j])) {
                        if (buffer.length() != 0)
                            tokens.add(new Token(Type.UNKNOWN, buffer));
                        buffer = "";
                        tokens.add(new Token(Type.Operation, String.valueOf(ch)));
                        flag = false;
                        break;
                    }
                } else {
                    if (j == operation.length - 1)
                        break;
                    else {
                        String tmp = "";
                        tmp += ch;
                        tmp += stream.charAt(i + 1);
                        if (tmp.equals(operation[j])) {
                            if (buffer.length() != 0)
                                tokens.add(new Token(Type.UNKNOWN, buffer));
                            buffer = "";
                            i++;
                            tokens.add(new Token(Type.Operation, tmp));
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }
        return tokens;
    }

    /**
     * 二次处理
     *
     * @param token
     * @return
     */
    public Token reProcess(Token token) {
        for (int i = 0; i < keywords.length; i++)
            if (token.getToken().equals(keywords[i])) {
                token.setType(Type.Keywords);
                return token;
            }
        if (token.require(reId)) {
            token.setType(Type.Id);
            return token;
        }
        if (token.require(reInteger)) {
            token.setType(Type.INTEG);
            return token;
        }
        if (token.require(reString)) {
            token.setType(Type.STR);
            return token;
        }
        token.setType(Type.Error);
        return token;
    }


}
package Syntax.entity;

import Lexical.RE.Type;
import Lexical.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Sentence {

    private static String[] VnList = {"T'", "C'", "E'", "S", "E", "T", "F", "C", "D"}; //非终结符
    private static String[] keyword = {"while", "if", "else"};
    private static String[] operation = {"++", "--", "<<", ">>", "<>", ">", "==", "!=", "&&", "||", "+=", "-=", "<=", ">=", "[", "]", "(", ")", "!", "~", "*", "/", "%", "+", "-", "<", "=", "|", "&", "^"};
    private static String[] partition = {",", "{", "}", ";"};

    public String start;
    public String value;
    public ArrayList<Token> tokens;
    public int dot;
    private Set<Token> reductionSet;

    /**
     * 当前指针下token
     *
     * @return
     */
    public Token getCurrent() {
        if (isProcess())
            return tokens.get(dot);
        else
            return null;
    }

    /**
     * 是否还在移位
     *
     * @return
     */
    public boolean isProcess() {
        return dot < tokens.size();
    }

    public void addReduction(Token reduction) {
        reductionSet.add(reduction);
    }

    public void addAllReduction(Set<Token> reductions) {
        reductionSet.addAll(reductions);
    }

    public Set<Token> getReductionSet() {
        return reductionSet;
    }

    public Token follow() {
        if (getCurrent().getType() == Type.Vn && dot + 1 < tokens.size())
            if (tokens.get(dot + 1).getType() != Type.Vn)
                return tokens.get(dot + 1);
        return null;
    }


    public Sentence(String value, int dot) {
        tokens = new ArrayList<>();
        reductionSet = new HashSet<>();
        this.value = value;
        this.dot = dot;
        String re = "";
        for (int i = 0, len = value.length(); i < len; i++) {
            if (value.charAt(i) == '>') {
                start = value.substring(0, i - 1);
                re = value.substring(i + 1);
                break;
            }
        }
        String buffer = "";
        for (int i = 0, len = re.length(); i < len; i++) {
            String current = re.substring(i, i + 1);
            /**
             * 终结符
             */
            if (current.equals("ε")) {
                tokens.add(new Token(Type.Vt, current));
                this.dot = 1;
                return;
            }
            /**
             * 非终结符
             */
            boolean isVn = false;

            for (int j = 0; j < VnList.length; j++)
                if (VnList[j].length() == 2) {
                    if (i + 1 < len) {
                        String tmp = re.substring(i, i + 2);
                        if (tmp.equals(VnList[j])) {
                            if (buffer.length() != 0) {
                                tokens.add(new Token(Type.UNKNOWN, buffer));
                                buffer = "";
                            }
                            tokens.add(new Token(Type.Vn, tmp));
                            i++;
                            isVn = true;
                            break;
                        }
                    }
                } else if (current.equals(VnList[j])) {
                    if (buffer.length() != 0) {
                        tokens.add(new Token(Type.UNKNOWN, buffer));
                        buffer = "";
                    }
                    tokens.add(new Token(Type.Vn, VnList[j]));
                    isVn = true;
                    break;
                }
            if (isVn)
                continue;
            /**
             * 分隔符
             */
            boolean isPartion = false;
            for (int j = 0; j < partition.length; j++)
                if (current.equals(partition[j])) {
                    if (buffer.length() != 0) {
                        tokens.add(new Token(Type.UNKNOWN, buffer));
                        buffer = "";
                    }
                    tokens.add(new Token(Type.Partion, current));
                    isPartion = true;
                    break;
                }
            if (isPartion)
                continue;
            /**
             * 操作符
             */
            boolean isOperation = false;
            for (int j = 0; j < operation.length; j++)
                if (operation[j].length() == 2) {
                    if (i + 1 < len) {
                        String tmp = re.substring(i, i + 2);
                        if (tmp.equals(operation[j])) {
                            if (buffer.length() != 0) {
                                tokens.add(new Token(Type.UNKNOWN, buffer));
                                buffer = "";
                            }
                            tokens.add(new Token(Type.Operation, tmp));
                            i++;
                            isOperation = true;
                            break;
                        }
                    }
                } else if (current.equals(operation[j])) {
                    if (buffer.length() != 0) {
                        tokens.add(new Token(Type.UNKNOWN, buffer));
                        buffer = "";
                    }
                    tokens.add(new Token(Type.Operation, current));
                    isOperation = true;
                    break;
                }
            if (isOperation)
                continue;
            buffer += current;
        }
        if (buffer.length() != 0)
            tokens.add(new Token(Type.UNKNOWN, buffer));
        for (Token token : tokens)
            if (token.getType() == Type.UNKNOWN) {
                int index = tokens.indexOf(token);
                boolean isKeyword = false;
                for (int i = 0; i < keyword.length; i++)
                    if (token.getToken().equals(keyword[i])) {
                        isKeyword = true;
                        token.setType(Type.Keywords);
                        tokens.set(index, token);
                        break;
                    }
                if (isKeyword)
                    continue;
                if (token.getToken().equals("id")) {
                    token.setType(Type.Id);
                    tokens.set(index, token);
                    continue;
                }
                if (token.getToken().equals("num")) {
                    token.setType(Type.INTEG);
                    tokens.set(index, token);
                }

            }
    }
}

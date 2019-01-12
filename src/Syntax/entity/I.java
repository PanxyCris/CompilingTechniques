package Syntax.entity;

import Lexical.RE.Type;
import Lexical.Token;

import java.util.*;

public class I {
    int id;
    ArrayList<Sentence> commands;
    HashSet<Sentence> sentences;
    Map<String, Integer> nextState;
    ArrayList<Integer> reduction;
    boolean isInTable;
    boolean isAdded;

    public I(int id, ArrayList<Sentence> commands) {
        this.id = id;
        this.commands = commands;
        isInTable = false;
        isAdded = false;
        sentences = new HashSet<>();
        nextState = new HashMap<>();
        reduction = new ArrayList<>();
    }

    public void addReduction(int item) {
        reduction.add(item);
    }


    public void addOne(Sentence sentence) {

        Set<Token> newReductions = new HashSet<>();
        newReductions.addAll(sentence.getReductionSet());
        sentences.add(sentence);
        if (!sentence.isProcess())
            return;
        Token currentToken = sentence.getCurrent();
//        if (sentence.follow() != null) {
//            boolean isExist = false;
//            for (Token token : newReductions) {
//                if (token.getToken().equals(sentence.follow().getToken())) {
//                    isExist = true;
//                    break;
//                }
//            }
//            if (!isExist)
//                newReductions.add(sentence.follow());
//        }
        if (currentToken.getType() == Type.Vn) {
            for (Sentence s : commands)
                if (currentToken.getToken().equals(s.start)) {
                    addOne(s, newReductions);
                }
        }

    }

    /**
     * 添加CFG
     *
     * @param sentence
     */
    private void addOne(Sentence sentence, Set<Token> reductions) {
        if (!hasSentence(sentence)) {
            sentence.addAllReduction(reductions);
            Set<Token> newReductions = new HashSet<>();
            newReductions.addAll(reductions);
            sentences.add(sentence);
            if (!sentence.isProcess())
                return;
            Token currentToken = sentence.getCurrent();
            if (sentence.follow() != null) {
                boolean isExist = false;
                for (Token token : newReductions) {
                    if (token.getToken().equals(sentence.follow().getToken())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist)
                    newReductions.add(sentence.follow());
            }
            if (currentToken.getType() == Type.Vn) {
                for (Sentence s : commands)
                    if (currentToken.getToken().equals(s.start)) {
                        addOne(s, newReductions);
                    }
            }
        }
    }

    private boolean hasSentence(Sentence sentence) {
        for (Sentence s : sentences) {
            if (s.value.equals(sentence.value))
                return true;
        }
        return false;
    }

    public boolean isEqual(I another) {
        ArrayList<Sentence> list = new ArrayList<>();
        list.addAll(sentences);
        list.addAll(another.sentences);
        HashMap<String, Integer> map = new HashMap<>();
        HashMap<String, Integer> dotMap = new HashMap<>();
        for (Sentence sentence : list) {
            if (map.get(sentence.value) == null) {
                map.put(sentence.value, 1);
                dotMap.put(sentence.value, sentence.dot);
            } else {
                if (dotMap.get(sentence.value) != sentence.dot)
                    return false;
                map.put(sentence.value, map.get(sentence.value) + 1);
            }
        }
        for (String key : map.keySet()) {
            if (map.get(key) % 2 == 1)
                return false;
        }
        return true;
    }

    public void addState(String pointer, int nextId) {
        nextState.put(pointer, nextId);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public HashSet<Sentence> getSentences() {
        return sentences;
    }

    public Map<String, Integer> getNextState() {
        return nextState;
    }

    public boolean isInTable() {
        return isInTable;
    }

    public void setInTable(boolean inTable) {
        isInTable = inTable;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public ArrayList<Integer> getReduction() {
        return reduction;
    }
}

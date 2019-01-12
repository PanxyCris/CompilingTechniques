package Lexical.DFA;

import Lexical. NFA.State;

import java.util.*;

public class I {

    private int id;
    private Map<Character,I> next;
    private Set<State> elements;
    public ArrayList<I> friends;

    public I(int id){
        this.id = id;
        next = new HashMap<>();
        elements = new HashSet<>();
        friends = new ArrayList<>();
    }

    public void addEdge(char s,I newI){
        next.put(s,newI);
    }

    public void addOne(State e){
        elements.add(e);
    }

    public void addAll(Set<State> es){
        elements.addAll(es);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<Character, I> getNext() {
        return next;
    }

    public Set<State> getElements() {
        return elements;
    }

}

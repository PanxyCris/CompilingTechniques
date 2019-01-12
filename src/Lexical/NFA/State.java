package Lexical.NFA;

import java.util.ArrayList;

public class State {

    private int id;//状态的编号
    private ArrayList<Pointer> next; //'o'表示epsilo
    private boolean isVisited;
    private boolean isAdded;

    public State() {
        next = new ArrayList<>();
        isVisited = false;
        isAdded = false;
    }

    public State(int id) {
        this.id = id;
        next = new ArrayList<>();
        isVisited = false;
        isAdded = false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void addEdge(char s, State nextState) {
        next.add(new Pointer(s, nextState));
    }

    public ArrayList<Pointer> getNext() {
        return next;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }
}

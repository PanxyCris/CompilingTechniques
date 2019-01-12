package Lexical.NFA;

public class Pointer {

    public char p;
    public State state;

    public Pointer(char p, State state){
        this.p = p;
        this.state = state;
    }

}

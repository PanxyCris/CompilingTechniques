package Lexical.NFA;

import java.util.HashSet;

public class NFA {

    private static char epsilo = 'o';
    public HashSet<State> states;
    public State start;
    public State end;

    public NFA() {
        states = new HashSet<>();
    }

    /**
     * 打印nfa对象
     */
    public void printObject() {
        printout(start);
        clearVisit();
    }

    /**
     * 深度优先遍历，将状态加入状态集
     * @param state
     */
    public void dfs(State state) {
        states.add(state);
        if (!state.isVisited()) {
            state.setVisited(true);
            for (Pointer pointer : state.getNext()) {
                dfs(pointer.state);
            }
        }
    }

    /**
     * 递归输出
     * @param state
     */
    public void printout(State state) {
        states.add(state);
        if (!state.isVisited()) {
            state.setVisited(true);
            for (Pointer pointer : state.getNext()) {
                System.out.println(state.getId() + " " + pointer.p + " " + pointer.state.getId());
                printout(pointer.state);
            }
        }
    }

    /**
     * 链接 ·
     *
     * @param nfa
     */
    public void doCat(NFA nfa) {
        end.addEdge(epsilo, nfa.start);
        end = nfa.end;
        nfa.start = null;
        nfa.end = null;
    }

    /**
     * 或 |
     *
     * @param nfa
     * @param id1
     * @param id2
     */
    public void doUnion(NFA nfa, int id1, int id2) {
        State newStart = new State(id1);
        State newEnd = new State(id2);
        newStart.addEdge(epsilo, start);
        newStart.addEdge(epsilo, nfa.start);
        end.addEdge(epsilo, newEnd);
        nfa.end.addEdge(epsilo, newEnd);
        start = newStart;
        end = newEnd;
        nfa.start = null;
        nfa.end = null;
    }

    /**
     * 星号 *
     *
     * @param id1
     * @param id2
     */
    public void doStar(int id1, int id2) {
        State newStart = new State(id1);
        State newEnd = new State(id2);
        newStart.addEdge(epsilo, newEnd);
        newStart.addEdge(epsilo, start);
        end.addEdge(epsilo, start);
        end.addEdge(epsilo, newEnd);
        end = newEnd;
        start = newStart;
    }

    /**
     * 清空加入dfa状态表
     */
    public void clear() {
        for (State state : states)
            state.setAdded(false);
    }

    /**
     * 清空加入nfa状态表
     */
    public void clearVisit() {
        for (State state : states)
            state.setVisited(false);
    }


}

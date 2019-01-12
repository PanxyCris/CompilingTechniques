package Lexical.DFA;

import Lexical.NFA.NFA;
import Lexical.NFA.State;
import Lexical.NFA.Pointer;

import java.util.*;

public class DFA {

    private static char epsilo = 'o';
    public I start;
    public I end;
    private State lastState;
    private HashMap<Integer, I> parseTable;
    private int id;
    private NFA nfa;
    private Map<Integer, Boolean> isInTable;

    public DFA() {
    }

    public DFA(NFA nfa) {
        id = 0;
        nfa.dfs(nfa.start);
        this.nfa = nfa;
        parseTable = new HashMap<>();
        isInTable = new HashMap<>();
        State nfaStart = nfa.start;
        lastState = nfa.end;
        generateI(nfaStart);
        start = parseTable.get(0);
        end = parseTable.get(parseTable.size() - 1);
    }

    /**
     * 输出DFA优化前对象
     */
    public void printObject() {
        for (int index : parseTable.keySet()) {
            I i = parseTable.get(index);
            System.out.print("I" + i.getId() + ":");
            for (State state : i.getElements())
                System.out.print(state.getId() + " ");
            System.out.print("  ");
            for (char key : i.getNext().keySet()) {
                if (i.getNext().get(key) != null) {
                    System.out.print(key + ":I" + i.getNext().get(key).getId() + "    ");
                }
            }
            System.out.println();
        }
    }

    /**
     * 输出优化结果
     * @param result
     */
    public void printOptimization(ArrayList<I> result) {
        System.out.println("phase:");
        for (I i : result) {
            System.out.print("I" + i.getId());
//            System.out.print("I" + i.getId() + ":");
//            for (State state : i.getElements())
//                System.out.print(state.getId() + " ");
            System.out.print("  ");
            for (char key : i.getNext().keySet()) {
                if (i.getNext().get(key) != null) {
                    System.out.print(key + ":I" + i.getNext().get(key).getId() + "    ");
                }
            }
            System.out.println();
        }
    }

    /**
     * 优化，将DFA->DFA度
     *
     * @return
     */
    public DFA optimize() {
        ArrayList<I> tmp = new ArrayList<>();
        for (int key : parseTable.keySet())
            tmp.add(parseTable.get(key));
        DFATree dfaTree = new DFATree(tmp);
        //分离是否含有最后一个元素
        ArrayList<I> left = new ArrayList<>();
        ArrayList<I> right = new ArrayList<>();
        for (I i : tmp) {
            boolean isLast = false;
            for (State state : i.getElements()) {
                if (state.getId() == lastState.getId()) {
                    right.add(i);
                    isLast = true;
                    break;
                }
            }
            if (!isLast)
                left.add(i);
        }
        dfaTree.left = new DFATree(left);
        dfaTree.right = new DFATree(right);
        dfaTree.left.divide(right);
        dfaTree.right.divide(left);
        ArrayList<I> result = dfaTree.merge();
//        printOptimization(result);
        DFA newDFa = new DFA();
        newDFa.start = result.get(0);
        newDFa.end = result.get(result.size() - 1);
        return newDFa;
    }


    /**
     * 生成新I
     *
     * @param state
     * @return
     */
    public I generateI(State state) {
        I newI = new I(id++);
        newI.addOne(state);
        for (Pointer pointer : state.getNext()) { //加入epsilo
            State nextState = pointer.state;
            if (pointer.p == epsilo) {
                newI.addAll(epsiloUnion(nextState));
            }
        }
        for (State addState : newI.getElements())
            addState.setAdded(false);
        addPointer(newI);
        return newI;
    }

    /**
     * 加指针I
     *
     * @param newI
     */
    public void addPointer(I newI) {
        for (State element : newI.getElements()) //生成下一指针
            for (Pointer pointer : element.getNext()) {
                if (pointer.p != epsilo) {
                    State nextState = pointer.state;
                    nfa.clear();
                    if (newI.getNext().get(pointer.p) == null) { //第一次结合
                        I nextI = new I(id++);
                        nextI.addAll(epsiloUnion(nextState));//加入结合状态连接的epsilo
                        boolean repeat = false;
                        for (int i : parseTable.keySet()) { //遍历parse表寻找是否有相同元素
                            if (isEqual(nextI.getElements(), parseTable.get(i).getElements())) {
                                id--;
                                repeat = true;
                                newI.addEdge(pointer.p, parseTable.get(i));
                                parseTable.put(newI.getId(), newI);
                                break;
                            }
                        }
                        if (!repeat) {
                            newI.addEdge(pointer.p, nextI);
                            parseTable.put(nextI.getId(), nextI);
                        }

                    } else { //如果已经有键值，需要加元素
                        I hasI = newI.getNext().get(pointer.p);
                        hasI.addAll(epsiloUnion(nextState));
                        boolean repeat = false;
                        for (int i : parseTable.keySet()) {
                            if (isEqual(hasI.getElements(), parseTable.get(i).getElements())) {
                                id--;
                                repeat = true;
                                break;
                            }
                        }
                        if (!repeat) {
                            newI.addEdge(pointer.p, hasI);
                            parseTable.put(hasI.getId(), hasI);
                        }
                    }
                }
            }
        isInTable.put(newI.getId(), true);
        Map<Character, I> nextPointer = newI.getNext();
        for (char key : nextPointer.keySet()) {
            I postNewI = nextPointer.get(key);
            if (isInTable.get(postNewI.getId()) == null)
                addPointer(postNewI);
        }
        parseTable.put(newI.getId(), newI);
    }


    /**
     * 结合epsilo
     *
     * @param state
     * @return
     */
    public Set<State> epsiloUnion(State state) {
        Set<State> integerSet = new HashSet<>();
        if (!state.isAdded()) {
            state.setAdded(true);
            integerSet.add(state);
            for (Pointer pointer : state.getNext()) {
                if (pointer.p == epsilo) {
                    State nextState = pointer.state;
                    integerSet.addAll(epsiloUnion(nextState));
                }
            }
        }
        return integerSet;
    }

    public boolean isEqual(Set<State> set1, Set<State> set2) {
        if (set1.size() != set2.size())
            return false;
        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        for (State state : set1)
            list1.add(state.getId());
        for (State state : set2)
            list2.add(state.getId());
        Collections.sort(list1);
        Collections.sort(list2);
        for (int i = 0; i < list1.size(); i++)
            if (list1.get(i) != list2.get(i))
                return false;
        return true;
    }

}

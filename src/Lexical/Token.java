package Lexical;

import Lexical.DFA.DFA;
import Lexical.DFA.I;
import Lexical.NFA.NFA;
import Lexical.RE.Type;
import Lexical.NFA.State;
import Lexical.NFA.Paren;

import java.util.Stack;

public class Token {

    private Type type;
    private String token;

    public Token(Type type, String token) {
        this.type = type;
        this.token = token;
    }

    /**
     * 操作
     *
     * @param re
     */
    public boolean require(String re) {
        String reStr = "";
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (ch >= '0' && ch <= '9')
                reStr += 'b';
            else
                reStr += 'a';
        }
        String postRE = reToPostRE(re);
        NFA nfa = postRETONFA(postRE);
        DFA dfa = nfaTODFA(nfa);
        DFA dfao = dfa.optimize();
        I pointer = dfao.start;
        for (int i = 0; i < reStr.length(); i++) {
            char ch = reStr.charAt(i);
            boolean isMatch = false;
            for (char key : pointer.getNext().keySet()) {
                if (ch == key) {
                    pointer = pointer.getNext().get(key);
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch)
                return false;
        }
        if (pointer.getId() != dfao.end.getId())
            return false;
        else
            return true;
    }

    /**
     * re转后缀
     *
     * @param re
     * @return
     */
    public String reToPostRE(String re) {
        Paren paren = new Paren();
        Stack<Paren> charStack = new Stack<>(); //预存栈
        int nalt = 0;
        int natom = 0;
        String postExpr = "";
        for (int i = 0; i < re.length(); i++) {
            char ch = re.charAt(i);
            switch (ch) {
                case ' ':
                    continue;
                case '(':
                    if (natom > 1) {
                        postExpr = postExpr + '.';
                    }
                    paren.natom = natom;
                    paren.nalt = nalt;
                    charStack.push(paren);
                    nalt = 0;
                    natom = 0;
                    break;
                case '*':
                    if (natom == 0)
                        throw new RuntimeException("提前出现'*'");
                    postExpr = postExpr + ch;
                    break;
                case '|':
                    if (natom == 0)
                        throw new RuntimeException("提前出现'|'");
                    while (--natom > 0) {
                        postExpr = postExpr + '.';
                    }
                    nalt++;
                    break;
                case ')':
                    if (natom == 0 || charStack.empty())
                        throw new RuntimeException("括号不匹配");
                    while (--natom > 0) {//比如((a|b)(c|d))模式，当上一次匹配完倒数第二个右括号后，natom为2，需要添加'.'
                        postExpr = postExpr + '.';
                    }
                    while (nalt-- > 0) {
                        postExpr = postExpr + '|';
                    }
                    paren = charStack.pop();
                    natom = paren.natom;
                    nalt = paren.nalt;
                    natom++;
                    break;
                default:
                    if (natom > 1) {
                        natom--;
                        postExpr = postExpr + '.';
                    }
                    natom++;
                    postExpr = postExpr + ch;
                    break;
            }
        }
        if (!charStack.empty())
            throw new RuntimeException("括号不匹配");
        while (--natom > 0) {
            postExpr = postExpr + '.';
        }
        while (nalt-- > 0) {
            postExpr = postExpr + '|';
        }
        return postExpr;
    }

    /**
     * re后缀转NFA
     *
     * @param postRE
     * @return
     */
    public NFA postRETONFA(String postRE) {
        int id = 0;
        Stack<NFA> nfaStk = new Stack<>();
        NFA e1, e2, e;
        int i, len = postRE.length();
        for (i = 0; i < len; i++) {
            char ch = postRE.charAt(i);
            switch (ch) {
                case '.':
                    if (nfaStk.size() > 1) {
                        e2 = nfaStk.pop();
                        e1 = nfaStk.pop();
                        e1.doCat(e2);
                        nfaStk.push(e1);
                    }
                    break;
                case '|':
                    e2 = nfaStk.pop();
                    e1 = nfaStk.pop();
                    e1.doUnion(e2, id++, id++);
                    nfaStk.push(e1);
                    break;
                case '*':
                    e = nfaStk.pop();
                    e.doStar(id++, id++);
                    nfaStk.push(e);
                    break;
                default:
                    NFA alpha = new NFA();
                    alpha.start = new State(id++);
                    alpha.end = new State(id++);
                    alpha.start.addEdge(ch, alpha.end);
                    nfaStk.push(alpha);
            }
        }
        e = nfaStk.pop();
        if (!nfaStk.empty())
            throw new RuntimeException("未知错误");
        return e;
    }

    /**
     * nfa转dfa
     *
     * @param nfa
     * @return
     */
    public DFA nfaTODFA(NFA nfa) {
        DFA dfa = new DFA(nfa);
        return dfa;
    }

    public String toString() {
        return "<" + type.getValue() + "," + token + ">";
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

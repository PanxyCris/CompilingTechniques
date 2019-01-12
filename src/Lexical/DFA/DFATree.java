package Lexical.DFA;

import java.util.ArrayList;

public class DFATree {

    public ArrayList<I> elements;
    ArrayList<I> result;
    public DFATree left;
    public DFATree right;

    public DFATree() {
        elements = new ArrayList<>();
        result = new ArrayList<>();
    }

    public DFATree(ArrayList<I> elements) {
        this.elements = elements;
        result = new ArrayList<>();
    }

    /**
     * 划分
     *
     * @param others
     */
    public void divide(ArrayList<I> others) {
        ArrayList<I> left = new ArrayList<>();
        ArrayList<I> right = new ArrayList<>();
        int len = elements.size();
        int otherLen = others.size();
        for (int i = 0; i < len; i++) {
            boolean isLeft = true;
            for (char key : elements.get(i).getNext().keySet()) {
                int mainId = elements.get(i).getNext().get(key).getId();
                boolean isEqual = true;
                for (int j = 0; j < otherLen; j++) {
                    if (mainId == others.get(j).getId()) {
                        isEqual = false;
                        break;
                    }
                }
                if (!isEqual) {
                    isLeft = false;
                    break;
                }
            }
            if (isLeft)
                left.add(elements.get(i));
            else
                right.add(elements.get(i));
        }
        if (left.size() != 0 && right.size() != 0) {
            this.left = new DFATree(left);
            this.right = new DFATree(right);
            this.left.divide(right);
            this.right.divide(left);
        }
    }

    /**
     * 得到DFA度
     *
     * @return
     */
    public ArrayList<I> getDFAo() {
        /**
         * 获取
         */
        if (elements.size() != 0)
            if (((left == null) || (right == null))) {
                I represent = elements.get(0);
                for (int i = 1; i < elements.size(); i++)
                    represent.friends.add(elements.get(i));
                result.add(represent);
            } else {
                result.addAll(left.getDFAo());
                result.addAll(right.getDFAo());
            }
        return result;
    }

    public ArrayList<I> merge() {
        ArrayList<I> result = getDFAo();
        /**
         * 合并
         */
        for (I i : result) {
            if (i.friends.size() > 0) {
                for (I friend : i.friends)
                    for (I j : result) {
                        for (char key : j.getNext().keySet()) {
                            if (j.getNext().get(key).equals(friend))
                                j.addEdge(key, i);
                        }
                    }
            }
        }
        for (int i = 0; i < result.size(); i++)
            for (int j = i + 1; j < result.size(); j++)
                if (result.get(i).getId() > result.get(j).getId()) {
                    I tmp = result.get(i);
                    result.set(i, result.get(j));
                    result.set(j, tmp);
                }
        return result;
    }

}

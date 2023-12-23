package access_structure.tree;

import access_structure.AccessStructure;
import access_structure.tree.node.*;
import access_structure.tree.node.*;

import java.util.*;

public class AccessTree implements AccessStructure {
    private Map<Integer, String> rho;
    private List<List<Integer>> A;
    private TreeNode policyTree;
    private int partsIndex;
    private List<PolicyAttribute> policyAttributeList;


    private AccessTree() {
        policyAttributeList = new ArrayList<PolicyAttribute>();
        A = new ArrayList<>();
        rho = new HashMap<>();
    }


    /**
     * @param policy 策略的字符串
     * @return 访问结构
     */
    public static AccessTree buildFromPolicy(String policy) {
        AccessTree aRho = new AccessTree();
        aRho.generateTree(policy);
        aRho.generateMatrix();
        aRho.computeAttributeList();
        return aRho;
    }

    public Map<Integer, String> getRho() {
        return rho;
    }

    public List<List<Integer>> getA() {
        return A;
    }

    public List<Integer> getRow(int row) {
        return A.get(row);
    }

    public int getL() {
        return A.get(0).size();
    }

    //获取行数
    public int getN() {
        return A.size();
    }

    public String rho(int i) {
        return rho.get(i);
    }

    public List<PolicyAttribute> getAttributeList() {
        return policyAttributeList;
    }

    /**
     *
     * @param node 策略树根节点
     * @return 是否满足生成策略树的条件
     */
    private boolean findIfSAT(TreeNode node) {
        if (node instanceof PolicyAttribute)
            return 1 == node.getSat();
        else {
            boolean b;
            if (node instanceof AndGate) {
                b = findIfSAT(((AndGate) node).getLeft());
                b &= findIfSAT(((AndGate) node).getRight());
            } else if (node instanceof OrGate) {
                b = findIfSAT(((OrGate) node).getLeft());
                b |= findIfSAT(((OrGate) node).getRight());
            } else
                throw new IllegalArgumentException("Unknown node type");
            node.setSat(b ? 1 : -1);
            return b;
        }
    }


    private void computeAttributeList(){
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(policyTree);

        while (!queue.isEmpty()) {
            TreeNode t = queue.poll();

            if (t instanceof PolicyAttribute) {
                policyAttributeList.add((PolicyAttribute) t);
            } else if (t instanceof InternalNode) {
                queue.add(((InternalNode) t).getLeft());
                queue.add(((InternalNode) t).getRight());
            }
        }
    }


    public List<Integer> getIndexesList(Collection<String> pKeys) {
        // initialize
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(policyTree);

        while (!queue.isEmpty()) {
            TreeNode t = queue.poll();
            if (t instanceof PolicyAttribute) {
                t.setSat(pKeys.contains(((PolicyAttribute) t).getName()) ? 1 : -1);
            } else if (t instanceof InternalNode) {
                t.setSat(0);
                queue.add(((InternalNode) t).getLeft());
                queue.add(((InternalNode) t).getRight());
            }
        }

        // find if satisfiable
        //是否有不能识别的节点。
        if (!findIfSAT(policyTree))
            return null;

        // populate the list
        List<Integer> list = new LinkedList<Integer>();
        queue.add(policyTree);
        while (!queue.isEmpty()) {
            TreeNode t = queue.poll();
            if (1 == t.getSat()) {
                if (t instanceof AndGate) {
                    queue.add(((AndGate) t).getLeft());
                    queue.add(((AndGate) t).getRight());
                } else if (t instanceof OrGate) {
                    if (1 == ((OrGate) t).getLeft().getSat()) {
                        queue.add(((OrGate) t).getLeft());
                    } else if (1 == ((OrGate) t).getRight().getSat()) {
                        queue.add(((OrGate) t).getRight());
                    }
                } else if (t instanceof PolicyAttribute) {
                    list.add(((PolicyAttribute) t).getX());
                }
            }
        }

        //此处list返回的就是需要扫描的叶节点在A中的位置。
        return list;
    }

    //生成矩阵，矩阵的行对应一个属性节点，记录属性节点的标签，如果当前标签长度小于c（最长标签长度）就在后面一直加0
    private void generateMatrix() {
        int c = computeLabels(policyTree);

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(policyTree);

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (node instanceof InternalNode) {
                queue.add(((InternalNode) node).getLeft());
                queue.add(((InternalNode) node).getRight());
            } else {
                //键值对：属性在A的位置，属性的指纹
                rho.put(A.size(), node.getName());
                //设置属性节点对应A的位置
                ((PolicyAttribute) node).setX(A.size());
                //生成一个List用于存储对应的属性的各种值，并且放到A中，也就是A在x的List
                List<Integer> Ax = new ArrayList<>(c);

                //标签的值也就是0，1，*构成，转化成枚举类对象
                for (int i = 0; i < node.getLabel().length(); i++) {
                    switch (node.getLabel().charAt(i)) {
                        case '0':
                            Ax.add(0);
                            break;
                        case '1':
                            Ax.add(1);
                            break;
                        case '*':
                            Ax.add(-1);
                            break;
                    }
                }
                //如果Ax没有达到最大的长度，则一直加0
                while (c > Ax.size())
                    Ax.add(0);
                A.add(Ax);
            }
        }
    }


    //给每个节点计算标签
    private int computeLabels(TreeNode root) {
        Queue<TreeNode> queue = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        //反应了AND节点的数目，
        int c = 1;

        //初始标签为1
        root.setLabel("1");
        queue.add(root);

        //递归把标签传递给子节点
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            //属性节点就不管
            if (node instanceof PolicyAttribute)
                continue;

            //OR节点，把OR节点的标签复制到左右孩子
            if (node instanceof OrGate) {
                ((OrGate) node).getLeft().setLabel(node.getLabel());
                queue.add(((OrGate) node).getLeft());
                ((OrGate) node).getRight().setLabel(node.getLabel());
                queue.add(((OrGate) node).getRight());
            }
            //AND节点，把节点标签赋值给孩子之后，没到当前最大标签的时候一直加0，到了最大长度则根据左右孩子分别加1和*
            else if (node instanceof AndGate) {
                sb.delete(0, sb.length());

                sb.append(node.getLabel());

                while (c > sb.length())
                    sb.append('0');
                sb.append('1');

                ((AndGate) node).getLeft().setLabel(sb.toString());
                queue.add(((AndGate) node).getLeft());

                sb.delete(0, sb.length());
                //
                while (c > sb.length())
                    sb.append('0');
                sb.append('*');

                ((AndGate) node).getRight().setLabel(sb.toString());
                queue.add(((AndGate) node).getRight());

                c++;
            }
        }

        return c;
    }


    // 根据策略：and Teacher CS 这样的前缀表达的字符串数组生成策略树
    // 而且生成的也只是策略树的子树，之后还要插入策略树，生成完整策略树的代码在后面
    private TreeNode generateTree(String[] policyParts) {
        partsIndex++;

        String policyAtIndex = policyParts[partsIndex];
        TreeNode node = generateNodeAtIndex(policyAtIndex);

        if (node instanceof InternalNode) {
            ((InternalNode) node).setLeft(generateTree(policyParts));
            ((InternalNode) node).setRight(generateTree(policyParts));
        }
        return node;
    }

    private TreeNode generateNodeAtIndex(String policyAtIndex) {
        switch (policyAtIndex) {
            case "and":
                return new AndGate();
            case "or":
                return new OrGate();
            default:
                return new PolicyAttribute(policyAtIndex);
        }
    }


    //生成完整策略树的代码
    private void generateTree(String policy) {
        String[] policyParts;
        partsIndex = -1;

        policyParts = infixNotationToPolishNotation(policy.split("\\s+"));

        policyTree = generateTree(policyParts);
    }


    // 中缀变成前缀表达式，并且没有括号的那种
    private String[] infixNotationToPolishNotation(String[] policy) {
        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("and", 2);
        precedence.put("or", 1);
        precedence.put("(", 0);

        Stack<String> rpn = new Stack<String>(); //rpn stands for Reverse Polish Notation
        Stack<String> operators = new Stack<String>();

        //此处生成逆前缀表达式
        for (String token : policy) {
            if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.peek().equals("(")) {
                    rpn.add(operators.pop());
                }
                operators.pop();
            } else if (precedence.containsKey(token)) {
                while (!operators.empty() && precedence.get(token) <= precedence.get(operators.peek())) {
                    rpn.add(operators.pop());
                }
                operators.push(token);
            } else {
                rpn.add(token);
            }
        }
        while (!operators.isEmpty()) {
            rpn.add(operators.pop());
        }

        // 倒转生成前缀表达式
        // reversing the result to obtain Normal Polish Notation
        List<String> polishNotation = new ArrayList<String>(rpn);
        Collections.reverse(polishNotation);
        return polishNotation.toArray(new String[] {});
    }

    public void printMatrix() {
        for (int x = 0; x < A.size(); x++) {
            List<Integer> Ax = A.get(x);
            System.out.printf("%s: [", rho.get(x));
            for (Integer aAx : Ax) {
                System.out.print("  "+aAx);
            }
            System.out.print("]");
            System.out.println();
        }
    }

    private void toString(StringBuilder builder, TreeNode node) {
        if (builder.length() != 0) builder.append(" ");

        if (node instanceof InternalNode) {
            builder.append(node.getName());
            toString(builder, ((InternalNode) node).getLeft());
            toString(builder, ((InternalNode) node).getRight());
        } else {
            builder.append(node.getName());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, policyTree);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessTree that = (AccessTree) o;
        return partsIndex == that.partsIndex &&
                Objects.equals(rho, that.rho) &&
                Objects.equals(A, that.A) &&
                Objects.equals(policyTree, that.policyTree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rho, A, policyTree, partsIndex);
    }
}

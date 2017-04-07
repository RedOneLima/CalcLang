import java.util.*;

public class Node {
    private static int count = 0;  // maintain unique id for each node
    private int id; //id is count
    private String kind;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as the actual identifier
    private Node first, second, third; //children nodes
    private static HashMap<String, Double> map = new HashMap<>();
    Scanner scn = new Scanner(System.in);

    public Node( String k, Node one, Node two, Node three ) {
        kind = k;  info = "";
        first = one;  second = two;  third = three;
        id = count;
        count++;
        //System.out.println( this );
    }

    public Node( Token token ) {
        kind = token.getKind();  info = token.getDetails();
        first = null;  second = null;  third = null;
        id = count;
        count++;
        //System.out.println( this );
    }

    public String toString() {
        return "#" + id + "[" + kind + "," + info + "]";
    }

    public void execute(){

//=================================== Statements ===============================================================

        if(kind.equals("statements")){
            first.execute();
            if(second != null){
                second.execute();
            }

//=================================== Statement ==========================================================

        }else if(kind.equals("statement")) {

//=================================== <var> = <exp> =========================================================

            if (first.kind.equals("var")) {
                if (second.info.equals("=")) {
                    double lit = third.evaluate();
                    map.put(first.info, lit);
                }

//==================================== Keywords ==============================================================

            }else if(first.kind.equals("show")){
                System.out.print(second.evaluate());
            }else if(first.kind.equals("msg")){
                System.out.print(second.info);
            }else if(first.kind.equals("newline")){
                System.out.print("\n");
            }else if(first.kind.equals("input")){
                System.out.print(second.info);
                map.put(third.info, scn.nextDouble());
            }
        }
    }

    public double evaluate(){

//================================ Expression =================================================================

        if(kind.equals("expression")){

//================================ <term> + or - <exp> =========================================================

            if(second != null){
                if(second.info.equals("+")){
                    return first.evaluate() + third.evaluate();
                }else if(second.info.equals("-")){
                    return first.evaluate() - third.evaluate();
                }

//=========================================== <term> =========================================================

            }else{
                return first.evaluate();
            }

//===================================== <fac> * or / <term> ==================================================

        }else if (kind.equals("term")){
            if(second != null){
                if(second.info.equals("*")){
                    return first.evaluate() * third.evaluate();
                }else if(second.info.equals("/")){
                    return first.evaluate() / third.evaluate();
                }

//=========================================== <fac> =========================================================

            }else{
                return first.evaluate();
            }

//========================================== Factor =========================================================

        }else if (kind.equals("factor")) {
            if (second != null) {

//================================ Negative Factor =============================================================

                if(first.info.equals("-")){
                    return (second.evaluate()*-1);

//=============================== Parentheses ==================================================================

                }else if(first.info.equals("(")){
                    return second.evaluate();

//============================= Built in Functions ============================================================

                }else if(first.kind.equals("bif")){

                    if (first.info.equals("sqrt")){
                        return Math.sqrt(second.evaluate());

                    }else if(first.info.equals("sin")){
                        return Math.sin(second.evaluate());

                    }else if(first.info.equals("cos")){
                        return Math.cos(second.evaluate());
                    }
                }

//================================== Terminals ===============================================================

            } else if (first.kind.equals("var")) {
                return Double.valueOf(map.get(first.info));

            } else if (first.kind.equals("num")) {
                return Double.parseDouble(first.info);
            }
        }
        return 0;
    }
}// Node
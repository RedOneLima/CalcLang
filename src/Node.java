/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {
    private static int count = 0;  // maintain unique id for each node
    private int id; //id is count
    private String kind;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as the actual identifier
    private Node first, second, third; //children nodes
    private static HashMap<String, Double> map = new HashMap<>();
    Scanner scn = new Scanner(System.in);

    public Node( String k, Node one, Node two, Node three ) {
    // construct a common node with no info specified
        kind = k;  info = "";
        first = one;  second = two;  third = three;
        id = count;
        count++;
        System.out.println( this );
    }

    public Node( String k, String inf, Node one, Node two, Node three ) {
    // construct a node with specified info
        kind = k;  info = inf;
        first = one;  second = two;  third = three;
        id = count;
        count++;
        System.out.println( this );
    }

    public Node( Token token ) {
    // construct a node that is essentially a token
        kind = token.getKind();  info = token.getDetails();
        first = null;  second = null;  third = null;
        id = count;
        count++;
        System.out.println( this );
    }

    public String toString() {
        return "#" + id + "[" + kind + "," + info + "]";
    }

    public void execute(){
        if(kind.equals("statements")){
            first.execute();
            if(second != null){
                second.execute();
            }
        }else if(kind.equals("statement")) {
            if (first.kind.equals("var")) {
                if (second.info.equals("=")) {
                    //TODO
                    third.evaluate();
                }
            }else if(first.kind.equals("show")){
                System.out.println(first.evaluate());
            }else if(first.kind.equals("msg")){
                System.out.println(first.info);
            }else if(first.kind.equals("newline")){
                System.out.println();
            }else if(first.kind.equals("input")){
                System.out.println(second.info);
                map.put(third.info, scn.nextDouble());
            }
        }
    }

    public double evaluate(){
        if(kind.equals("expression")){
            if(second != null){
                //TODO exp has + or -
            }else{
                first.evaluate();
            }
        }else if (kind.equals("term")){
            if(second != null){
                //TODO term has * or /
            }else{
                first.evaluate();
            }
        }else if (kind.equals("factor")) {
            if (second != null) {
                if(first.info.equals("-")){
                    return (second.evaluate()*-1);
                }else if(first.info.equals("(")){
                    return second.evaluate();
                }else if(first.kind.equals("bif")){
                    if (first.info.equals("sqrt")){
                        return Math.sqrt(second.evaluate());
                    }else if(first.info.equals("sin")){
                        return Math.sin(second.evaluate());
                    }else if(first.info.equals("cos")){
                        return Math.cos(second.evaluate());
                    }
                }else if(first.info.equals("(")){
                    return second.evaluate();
                }
            } else if (first.kind.equals("var")) {
                return map.get(first.info);
            } else if (first.kind.equals("num")) {
                return Double.parseDouble(first.info);
            }
        }



        return 0;}



//===================================================================================================================
//--------------------------------------GUI STUFF--------------------------------------------------------------------
//===================================================================================================================

    //******************************************************
    // graphical display of this node and its subtree
    // in given camera, with specified location (x,y) of this
    // node, and specified distances horizontally and vertically
    // to children
    public void draw( Camera cam, double x, double y, double h, double v ) {

        System.out.println("draw node " + id );

        // set drawing color
        cam.setColor( Color.black );

        String text = kind;
        if( ! info.equals("") ) text += "(" + info + ")";
        cam.drawHorizCenteredText( text, x, y );

        // positioning of children depends on how many
        // in a nice, uniform manner
        Node[] children = getChildren();
        int number = children.length;
        System.out.println("has " + number + " children");

        double top = y - 0.75*v;

        if( number == 0 ) {
            return;
        }
        else if( number == 1 ) {
            children[0].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
        }
        else if( number == 2 ) {
            children[0].draw( cam, x-h/2, y-v, h/2, v );     cam.drawLine( x, y, x-h/2, top );
            children[1].draw( cam, x+h/2, y-v, h/2, v );     cam.drawLine( x, y, x+h/2, top );
        }
        else if( number == 3 ) {
            children[0].draw( cam, x-h, y-v, h/2, v );     cam.drawLine( x, y, x-h, top );
            children[1].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
            children[2].draw( cam, x+h, y-v, h/2, v );     cam.drawLine( x, y, x+h, top );
        }
        else {
            System.out.println("no Node kind has more than 3 children???");
            System.exit(1);
        }

    }// draw
    private Node[] getChildren() {
        //produce array with the non-null children in order
        int count = 0;
        if( first != null ) count++;
        if( second != null ) count++;
        if( third != null ) count++;
        Node[] children = new Node[count];
        int k=0;
        if( first != null ) {  children[k] = first; k++; }
        if( second != null ) {  children[k] = second; k++; }
        if( third != null ) {  children[k] = third; k++; }

        return children;
    }


}// Node
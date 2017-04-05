/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {
    public static int count = 0;  // maintain unique id for each node

    private int id;

    private String kind;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as
    // the actual identifier for an I

    // references to children in the parse tree
    private Node first, second, third;

    // construct a common node with no info specified
    public Node( String k, Node one, Node two, Node three ) {
        kind = k;  info = "";
        first = one;  second = two;  third = three;
        id = count;
        count++;
        System.out.println( this );
    }

    // construct a node with specified info
    public Node( String k, String inf, Node one, Node two, Node three ) {
        kind = k;  info = inf;
        first = one;  second = two;  third = three;
        id = count;
        count++;
        System.out.println( this );
    }

    // construct a node that is essentially a token
    public Node( Token token ) {
        kind = token.getKind();  info = token.getDetails();
        first = null;  second = null;  third = null;
        id = count;
        count++;
        System.out.println( this );
    }

    public String toString() {
        return "#" + id + "[" + kind + "," + info + "]";
    }

    // produce array with the non-null children
    // in order
    private Node[] getChildren() {
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

    //=========================================================================
    //   translate Joint code to VPL code
    //=========================================================================

    // ancillary data needed or convenient for translation to VPL
    private static PrintWriter out;
    private static NameIntTable globals;
    private static NameIntTable functions;
    private static NameIntTable locals;
    private static int lastAux;  // last auxiliary local cell used
    private static int lastLabel;  // last label used (start at 1000 for readability)

    // for convenience use these arrays for the uniform ops
    private static String[] bifs = { "add", "sub", "mult", "div", "rem", "eq", "neq", "lt", "lteq",
            "and", "or", "new" };
    private static int[] bifOps = { 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 31 };

    public static int nextAux() {
        int temp = lastAux;
        lastAux++;
        return temp;
    }

    private static int nextLabel() {
        int temp = lastLabel;
        lastLabel++;
        return temp;
    }

    // generate code and store here for later sending to
    // vpl file after figure out how many locals, create
    // literals
    private static ArrayList<String> code;

    // open output file with given name and initialize all
    // ancillary data structures
    public static void prepare( String vplFileName ) {
        try {
            out = new PrintWriter( new File( vplFileName ) );
        }
        catch(Exception e) {
            System.out.println("could not open output file");
            System.exit(1);
        }

        lastLabel = 100;

        globals = new NameIntTable();
        functions = new NameIntTable();
        functions.add( "ignite", nextLabel() );

    }

    public static void error( String message ) {
        System.out.println( message );
        out.close();  // so can see partial results
        System.exit(1);
    }

    // a node can translate the tree rooted at it to VPL code
    // Behavior depends on kind of node, and sometimes info
    public void toVPL() {

        if( kind.equals("program") ) {

            if( first != null ) {// has globalDecs

                // traverse tree and collect the global variables
                Node temp = first;
                while( temp != null ) {
                    globals.add( temp.first.info, globals.size() );
                    temp = temp.second;
                }
                System.out.println("after collecting globals have table:\n" + globals );

                // create VPL code to set up globals
                out.println("32 " + globals.size() );

            }// has globalDecs

            // always do call to ignite (first label) and halt
            out.println("2 " + functions.getNumber( "ignite" ) );
            out.println("26");
            out.println();

            // create code for all functions
            second.toVPL();

            System.out.println("functions table:\n" + functions );

            // finish
            out.close();

        }// program

        else if( kind.equals("funcDefs") ) {
            first.toVPL();
            if( second != null )
                second.toVPL();
        }// funcDefs

        else if( kind.equals("funcDef") ) {
            String funcName = first.info;

            // check whether is error due to repeat
            if( funcName.equals( "ignite" ) ) {
                // don't check for repeat (slightly lazy)
                // and already registered in functions
            }
            else {// name other than "ignite" is checked for repeat here and registered
                int where = functions.getNumber( funcName );
                if( where >= 0 )
                    error( "detected repeated function name " + funcName );
                else
                    functions.add( funcName, nextLabel() );
            }

            out.print("0 " + funcName + "(" );

            // locals will hold info for all local cells, start by
            // filling in params
            locals = new NameIntTable();

            // process params
            if( second != null ) {// have params, get them
                // traverse tree and collect the params variables
                Node temp = second;
                while( temp != null ) {
                    locals.add( temp.first.info, locals.size() );
                    temp = temp.second;
                }
                System.out.println("after collecting params for " + funcName + " have locals table:\n" + locals );

                for( int k=0; k<locals.size(); k++ ) {
                    out.print( locals.getName(k) );
                    if( k < locals.size()-1 ) out.print( "," );
                }
            }// have params

            out.println(") ---------------------------------");

            out.println("1 " + functions.getNumber( funcName ) + "\n" );

            // generate code for function body while building info
            code = new ArrayList<String>();

            // restart aux counting for each function def
            lastAux = 0;

            if( third != null ) {// have statements in body, do them
                third.toVPL();  // from here downward in parse tree, send to code, rather than out
            }

            // use info to generate up-front VPL stuff for this function,
            // namely 4 total locals needed, bunch of 22's for intlits,
            // code using 31, 24, 25, to make strings

            System.out.println( funcName + " ends up with locals\n" + locals );

            out.println("4 " + locals.size() );

            // go through locals and use names to know whether
            // to build intlit or string, or skip
            for( int k=0; k<locals.size(); k++ ) {
                NameIntPair pair = locals.get(k);
                if( pair.name.charAt(0) == '#' ) {
                    System.out.println("--------------> " + pair.name );
                    out.println( "22 " + pair.number + " " + pair.name.substring(1,pair.name.length()) );
                }
            }
            out.println();

            // now finish the vpl code for this funcDef by spewing the stored code:
            for( String s : code )
                out.println( s );

            // in case still here---never returned somewhere else---
            // return 0---assuming have at least one local
            out.println();
            out.println("22 0 0");
            out.println("5 0\n");

        }// funcDef

        // note:  from here down, send lines of code to code, rather than out
        // ***********************************************************

        else if( kind.equals("statements") ) {
            first.toVPL();
            if( second != null )
                second.toVPL();
        }// statements

        else if( kind.equals("statement") ) {
            if( info.equals("funcCall") ) {
                first.toVPL();
            }// call
            else if( info.equals("return") ) {

                int aux = locals.getAux();
                first.toVPL( aux );
                // now return whatever is in cell aux
                // (the expression value)
                code.add( "5 " + aux );

                // return aux to pool
                locals.releaseAux( aux );

            }// return
            else if( info.equals("store") ) {

                // generate code to evaluate the right-hand side into aux:
                int aux = locals.getAux();
                second.toVPL( aux );

                // determine actual vpl cell to store into:
                String name = first.info;
                boolean local = false;
                int cell = globals.getNumber( name );   // is target global?
                if( cell == -1 ) {// local
                    local = true;
                    cell = locals.getNumber( name );  // look for in locals
                    if( cell == -1 ) {// first use in function, add to locals
                        locals.add( name, locals.size() );
                        cell = locals.size()-1;
                    }
                }

                // generate code to do the storing:
                if( local ) {
                    code.add( "23 " + cell + " " + aux );
                }
                else {// global
                    code.add( "33 " + cell + " " + aux );
                }

                // return aux to pool
                locals.releaseAux( aux );

            }// store

            else if( info.equals("arrayStore") ) {
            }// arrayStore
            else if( info.equals("loop") ) {
            }// loop
            else if( info.equals("branch") ) {
            }// branch
            else {
                error("unknown statement info: " + info );
            }
        }// statement

        else {// non-existent kind
            error("non-existent kind of Node to do toVPL: " + kind );
        }

    }// toVPL

    // depending on kind of Node, generate VPL code to ArrayList code,
    // that will evaluate the expression,
    // using cell aux as temporary place to put the resulting
    // value
    public void toVPL( int aux ) {

        if( kind.equals("expression") ) {

            if( first.kind.equals( "num" ) ) {// numeric literal
                String numeral = first.info;
                // see if numeral already in locals
                int cell = locals.getNumber( numeral );
                if( cell == -1 ) {// first encounter with this numeral, add it
                    locals.add( "#" + numeral, locals.size() );
                    cell = locals.size()-1;
                }

                code.add( "23 " + aux + " " + cell );

            }

            else if( first.kind.equals( "id" ) ) {// identifier
                String name = first.info;
                // see if name in locals
                int cell = locals.getNumber( name );
                if( cell == -1 ) {// no such local variable
                    cell = globals.getNumber( name );
                    if( cell == -1 ) {// no such variable
                        error("no such variable as " + name );
                    }
                    else {// give global variable value
                        code.add( "34 " + aux + " " + cell );
                    }
                }
                else {// give local variable value
                    code.add( "23 " + aux + " " + cell );
                }
            }

            // do array retrieve and string later

            else if( first.kind.equals( "funcCall" ) ) {// function call
                first.toVPL( aux );
            }

            else {
                error("unknown type of expression node" );
            }

        }// expression

        else if( kind.equals("funcCall") ) {

            String name = first.info;  // function name
            System.out.println("function name is " + name );

            // handle bifs first (they actually use more aux's than user-defined ones)
            int which = -1;
            for( int k=0; k<bifs.length && which==-1; k++ ) {
                if( bifs[k].equals( name ) )
                    which = k;
            }

            if( which >= 0 ) {// is a nice (two args, use VPL op directly), handle all similarly
                int aux1 = locals.getAux(), aux2 = locals.getAux();

                // generate code to evaluate the two arguments
                System.out.println("bif, arg kinds are " + second.first.kind + " and " + second.second.first.kind );
                second.first.toVPL( aux1 );
                System.out.println("finished first arg");
                second.second.first.toVPL( aux2 );
                System.out.println("finished second arg");

                code.add( bifOps[ which ] + " " + aux + " " + aux1 + " " + aux2 );

                locals.releaseAux( aux1 );
                locals.releaseAux( aux2 );

            }
            else if( name.equals("not") ) {
                int aux2 = locals.getAux();

                // generate code to evaluate the argument
                second.first.toVPL( aux2 );

                code.add( "20 " + aux + " " + aux2 );

                locals.releaseAux( aux2 );

            }
            else if( name.equals("opp") ) {
                int aux2 = locals.getAux();

                // generate code to evaluate the argument
                second.first.toVPL( aux2 );

                code.add( "21 " + aux + " " + aux2 );

                locals.releaseAux( aux2 );

            }
            else if( name.equals("input") ) {// a little weird---has no args, has side effect
                code.add( "27 " + aux );
            }
            else if( name.equals("show") ) {// a little weird---no value returned
                int aux2 = locals.getAux();

                // generate code to evaluate the argument
                second.first.toVPL( aux2 );

                code.add( "28 " + " " + aux2 );

                locals.releaseAux( aux2 );
            }
            else if( name.equals("newline") ) {// a little weird---no value returned, no args
                code.add( "29" );
            }

            else {// must be a user-defined function

                // evaluate the args and pass across one at a time
                int aux2 = locals.getAux();
                Node temp = second;
                System.out.println(temp + " before loop");
                while( temp != null ) {
                    temp.first.toVPL( aux2 );  // generate code to evaluate the expression
                    code.add( "3 " + aux2 );   // pass the value across
                    temp = temp.second;
                    System.out.println(temp + " after update at bottom of loop");
                }

                int start = functions.getNumber( name );
                code.add( "2 " + start );  // call the function

                locals.releaseAux( aux2 );
            }

        }// funcCall

        else {
            error("unknown kind of Node " + kind + " for translation to VPL with single aux cell");
        }

    }// toVPL( aux )


}// Node
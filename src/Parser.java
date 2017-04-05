/*
    This class provides a recursive descent parser of Joint
    (a joint project of both sections, very similar to both
     Otter and Blunt),
    creating a parse tree which is then translated to
    VPL code
*/

import java.util.*;
import java.io.*;

public class Parser {

    private Lexer lex;

    public Parser( Lexer lexer ) {
        lex = lexer;
    }

    public static void main(String[] args) throws Exception {
        System.out.print("Enter file name: ");
        Scanner keys = new Scanner( System.in );
        String name = keys.nextLine();
        Lexer lex = new Lexer( name );
        Parser parser = new Parser( lex );

        Node root = parser.parseStatement();  // parser.parseProgram();

        TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root );

    }

    private Node parseStatements() {
        System.out.println("-----> parsing statements:");
        Node first = parseStatement();
        Token token = lex.getToken();
        if (!token.isKind("eof")) {
            lex.putBack(token);
            Node second = parseStatements();
            return new Node("statements", first, second, null);
        } else {
            lex.putBack(token);
            return new Node("statements", first, null, null);
        }
    }

    private Node parseStatement() {
        System.out.println("-----> parsing statement:");

        Token token = lex.getToken();
        Node first = null, second = null, third = null;
        String info = "";   // specify info for <statement> node

        if( token.isKind( "var" ) ) {
            Token token2 = lex.getToken();

            if (token2.matches("op", "=")) {// variable assignment
               first = parseExpression();
               return new Node ("statement", first, null, null);
                //info = "call";
                //first = null;
            }
        }else if (token.isKind("show")){
            //TODO keyword
            lex.putBack(token);
            first = parseExpression();
            return new Node ("statement", first, null, null);
        }else if (token.isKind("msg")){
            //TODO keyword
        }else if (token.isKind("newline")){
            //TODO keyword
        }else if (token.isKind("input")){
            //TODO keyword
        }
        else {// error
            System.out.println("Error:  illegal first token " + token + " to start <statement>");
            System.exit(1);
        }

        return new Node( "statement", info, first, second, third );
    }


    private Node parseExpression() {
        System.out.println("-----> parsing expression:");
        Token token1 = lex.getToken();
        if( token1.isKind("num") ) {
            Node first = parseTerm();
            return new Node("expression", first, null, null );
        }
        else {// token1 must be identifier (noticing <funcCall> starts with ID)
            errorCheck( token1, "id" );
            Node first = new Node( token1 );

            Token token2 = lex.getToken();  // look ahead

            if( token2.matches( "single", "[" ) ) {// array retrieve
                Node second = parseExpression();
                Token token = lex.getToken();
                errorCheck( token, "single", "]" );
                return new Node("expression", first, second, null );
            }
            else if( token2.matches( "single", "(" ) ) {// funcCall
                lex.putBack( token2 );  // put back ( and identifier that turns out to be func name
                lex.putBack( token1 );
                Node second = null;//TODO parseFuncCall();
                return new Node("expression", second, null, null );
            }
            else {// must be just an identifier
                lex.putBack( token2 );
                return new Node("expression", first, null, null );
            }
        }

    }

    private Node parseTerm(){return new Node(null);}//TODO

    private Node parseFactor(){return new Node(null);}//TODO

    // check whether token is correct kind
    private void errorCheck( Token token, String kind ) {
        if( ! token.isKind( kind ) ) {
            System.out.println("Error:  expected " + token + " to be of kind " + kind );
            System.exit(1);
        }
    }

    // check whether token is correct kind and details
    private void errorCheck( Token token, String kind, String details ) {
        if( ! token.isKind( kind ) || ! token.getDetails().equals( details ) ) {
            System.out.println("Error:  expected " + token + " to be kind=" + kind + " and details=" + details );
            System.exit(1);
        }
    }

}
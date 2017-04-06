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

        Node root = parser.parseStatements();  // parser.parseProgram();
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

            if (token2.matches("opp", "=")) {// variable assignment
               first = parseExpression();
               return new Node ("statement", new Node(token), new Node(token2), first);
            }else{
                System.out.println("Missing assignment statement");
                System.exit(1);
            }
        }else if (token.isKind("show")){
            //TODO keyword
            first = parseExpression();
            return new Node ("statement", first, null, null);
        }else if (token.isKind("msg")){
            //TODO keyword
            Token tok2 = lex.getToken();
            if(tok2.isKind("string")){
                return new Node("statement", first, new Node(tok2), null);
            }else{
                System.out.println("msg expects a string");
                System.exit(1);
            }
        }else if (token.isKind("newline")){
            //TODO keyword
            return new Node(token);
        }else if (token.isKind("input")){
            //TODO keyword
            Token tok2 = lex.getToken();
            if (tok2.isKind("string")){
                Token tok3 = lex.getToken();
                if (tok3.isKind("var")){
                    return new Node("statement", new Node(token), new Node(tok2), new Node(tok3));
                }else{
                    System.out.println("Expected variable");
                    System.exit(1);
                }
            }else{
                System.out.println("String expected");
                System.exit(1);
            }
        }
        else {// error
            System.out.println("Error:  illegal first token " + token + " to start <statement>");
            System.exit(1);
        }

        return new Node( "statement", info, first, second, third );
    }

    private Node parseExpression() {
        System.out.println("-----> parsing expression:");
        Node first = parseTerm();
        Token token1 = lex.getToken();
        //TODO --> WHAT DO I DO WITH THE TOKEN??
        if( token1.matches("opp", "+") || token1.matches("opp","-") ) {
            Node second = parseExpression();
            return new Node("expression", first, new Node(token1) , second );
        }
        lex.putBack(token1);
        return new Node ("expression", first, null, null);
    }

    private Node parseTerm(){
        System.out.println("-----> parsing term");
        Node first = parseFactor();
        Token tok = lex.getToken();
        //TODO --> WHAT DO I DO WITH THE TOKEN??
        if( tok.matches("opp","*") || tok.matches("opp","/")){
            Node second = parseTerm();
            return new Node("term", first, new Node(tok), second);
        }
        lex.putBack(tok);
        return new Node("term", first, null, null);

    }

    private Node parseFactor() {
        System.out.println("-----> parsing factor");
        Token tok = lex.getToken();
//Terminals
        if (tok.isKind("num") || tok.isKind("var")) {
            return new Node("factor", new Node(tok), null, null);
//BIFN
        } else if (tok.isKind("bif")) {
            Token tok2 = lex.getToken();
            if (tok2.matches("opp", "(")) {
                Node first = parseExpression();
                Token tok3 = lex.getToken();
                if (tok3.matches("opp", ")")) {
                    return new Node("factor", new Node(tok), first, null);
                } else {
                    System.out.println("-> ' ) ' expected");
                    System.exit(1);
                }
            } else {
                System.out.println("-> ' ( ' expected");
                System.exit(1);
            }
//Negitive
        } else if (tok.matches("opp", "-")) {
            Node first = parseFactor();
            return new Node("factor", new Node(tok), first, null);
        }
// ( )
        else if (tok.matches("opp", "(")) {
            Node first = parseExpression();
            Token tok2 = lex.getToken();
            if (tok.matches("opp", ")")) {
                return new Node("factor", new Node(tok), first, new Node(tok2));
            } else {
                System.out.println("-> ' ) ' expected");
                System.exit(1);
            }
        }else{
            System.out.println("Token "+tok +" outside of grammer for factor");
            System.exit(1);
        }
        return null;
    }

    private void errorCheck( Token token, String kind ) {
    // check whether token is correct kind
        if( ! token.isKind( kind ) ) {
            System.out.println("Error:  expected " + token + " to be of kind " + kind );
            System.exit(1);
        }
    }

    private void errorCheck( Token token, String kind, String details ) {
    // check whether token is correct kind and details
        if( ! token.isKind( kind ) || ! token.getDetails().equals( details ) ) {
            System.out.println("Error:  expected " + token + " to be kind=" + kind + " and details=" + details );
            System.exit(1);
        }
    }

}
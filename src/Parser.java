public class Parser {

    private Lexer lex;

    public Parser( Lexer lexer ) { lex = lexer; }

    public Node parseStatements() {

        Node first = parseStatement();
        Token token = lex.getToken();

//=============================== <stmt> <stmts> ==============================================================

        if (!token.isKind("eof")) {
            lex.putBack(token);
            Node second = parseStatements();
            return new Node("statements", first, second, null);

//================================ <stmt> ====================================================================

        } else {
            return new Node("statements", first, null, null);
        }
    }

    private Node parseStatement() {

        Token token = lex.getToken();
        Node first = null, second = null, third = null;

//=================================== <var> = <exp> ============================================================

        if( token.isKind( "var" ) ) {
            Token token2 = lex.getToken();

            if (token2.matches("opp", "=")) {// variable assignment
               first = parseExpression();
               return new Node ("statement", new Node(token), new Node(token2), first);
            }else{
                System.out.println("Missing assignment statement");
                System.exit(1);
            }

//=================================== Keywords ===============================================================

        }else if (token.isKind("show")){

            first = parseExpression();
            return new Node ("statement", new Node(token), first, null);

        }else if (token.isKind("msg")){

            Token tok2 = lex.getToken();
            if(tok2.isKind("string")){
                return new Node("statement", new Node(token), new Node(tok2), null);
            }else{
                System.out.println("msg expects a string");
                System.exit(1);
            }

        }else if (token.isKind("newline")){

            return new Node("statement", new Node(token), null, null);

        }else if (token.isKind("input")){

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

//=============================================== Error ========================================================

        else {// error
            System.out.println("Error:  illegal first token " + token + " to start <statement>");
            System.exit(1);
        }

        return new Node( "statement", first, second, third );
    }

    private Node parseExpression() {

        Node first = parseTerm();
        Token token1 = lex.getToken();

//==================================<term> + or - <exp>==========================================================

        if( token1.matches("opp", "+") || token1.matches("opp","-") ) {
            Node second = parseExpression();
            return new Node("expression", first, new Node(token1) , second );
        }
//==================================<exp>=======================================================================

        lex.putBack(token1);
        return new Node ("expression", first, null, null);
    }

    private Node parseTerm(){

        Node first = parseFactor();
        Token tok = lex.getToken();

//========================================== <fac> * or / <term>=============================================

        if( tok.matches("opp","*") || tok.matches("opp","/")){
            Node second = parseTerm();
            return new Node("term", first, new Node(tok), second);
        }
//==========================================<term>===========================================================
        lex.putBack(tok);
        return new Node("term", first, null, null);

    }

    private Node parseFactor() {

        Token tok = lex.getToken();

//================================== Terminals ===============================================================

        if (tok.isKind("num") || tok.isKind("var")) {
            return new Node("factor", new Node(tok), null, null);

//============================= Built in Functions ============================================================

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

//================================ Negative Factor =============================================================

        } else if (tok.matches("opp", "-")) {

            Node first = parseFactor();
            return new Node("factor", new Node(tok), first, null);
        }

//=============================== Parentheses ==================================================================

        else if (tok.matches("opp", "(")) {
            Node first = parseExpression();
            Token tok2 = lex.getToken();
            return new Node("factor",  new Node(tok), first, new Node(tok2));

//=============================== Error =======================================================================

        }else{
            System.out.println("Token "+tok +" outside of grammer for factor");
            System.exit(1);
        }
        return null;
    }
}
import java.util.*;
import java.io.*;

public class Lexer {

    private static String[] keywords = { "show", "msg",
            "newline", "input"};
    private static String[] builtInFunctions = {
            "sqrt", "sin", "cos"};
    private Stack<Token> stack;
    private BufferedReader input;
    private int lookahead;

    public Lexer( String fileName )
    {
        try{
            input = new BufferedReader( new FileReader( fileName ) );
        }
        catch(Exception e) {
            System.out.println("Problem opening file named [" + fileName + "]" );
        }

        stack = new Stack<Token>();
        lookahead = 0;  // indicates no lookahead symbol present
    }// constructor

    // produce the next token
    public Token getToken()
    {
        if( ! stack.empty() ) {
            //  produce the most recently putback token
            Token token = stack.pop();
            return token;
        }
        else {
            // produce a token from the input source
            int state = 1;  // state of DFA
            String data = "";  // specific info for the token
            boolean done = false;
            int sym;  // holds current symbol

//=============================================================================================

            do {
                sym = getNextSymbol();
                switch (state) {
/*State 1*/         case 1:

                        if (sym == 10 || sym == 13 || sym == 32) {
                            // state stays at 1
                        } else if (isLetter(sym)) {
                            data += (char) sym;
                            state = 2;
                        } else if (isOperator(sym)) {
                            data += (char) sym;
                            state = 3;
                        } else if (isDigit(sym)) {
                            state = 4;
                            data += (char) sym;
                        } else if (sym == '\"') {
                            state = 6;
                        } else if (sym == -1) {
                            state = 8;
                        }
                        break;


/*State 2*/          case 2:

                        if (isLetter(sym)) {
                            data += (char) sym;
                            // state stays at  2;
                        } else if (isDigit(sym)) {
                            data += (char) sym;
                            // stay in state 2
                        } else {
                            done = true;
                            putBackSymbol(sym);
                        }
                        break;

/*State 3*/         case 3:

                        done = true;
                        putBackSymbol(sym);
                        break;

/*State 4*/         case 4:

                        if (isDigit(sym)) {
                            // stay in state 4
                            data += (char) sym;
                        } else if (sym == '.') {
                            data += (char) sym;
                            state = 5;
                        } else {
                            done = true;
                            putBackSymbol(sym);
                        }
                        break;

/*State 5*/         case 5:

                        if (isDigit(sym)) {
                            data += (char) sym;
                        } else {
                            done = true;
                            putBackSymbol(sym);
                        }
                        break;

/*State 6*/         case 6:

                        if (isDigit(sym) || (isPrintable(sym) && sym != '\"') || isLetter(sym)) {
                            // stay in state 6
                            data += (char) sym;
                        } else if (sym == '\"') {
                            state = 7;
                        }
                        break;

/*State 7*/         case 7:

                        done = true;
                        putBackSymbol(sym);
                        break;

/*State 8*/         case 8:
                        done = true;
                        break;

                    default:
                        System.out.println("Unknown state " + state + " in Lexer");
                }
            } while (!done);

//========================================================================================================

            // generate token depending on stopping state
            Token token;
            switch (state) {
                case 2: // reserved word, bif, or user-defined id

                    for (int k = 0; k < keywords.length; k++)
                        if (keywords[k].equals(data)) {
                            token = new Token(data, "");
                            return token;
                        }

                    for (int k = 0; k < builtInFunctions.length; k++)
                        if (builtInFunctions[k].equals(data)) {
                            token = new Token("bif", data);
                            return token;
                        }

                    token = new Token("var", data);

                    return token;

                case 3: // operator symbol
                    token = new Token("opp", data);
                    return token;

                case 4: // numeric token
                case 5:
                    token = new Token("num", data);
                    return token;

                case 7:// string literal
                    token = new Token("string", data);
                    return token;

                case 8: // eof

                    token = new Token("eof", "");
                    return token;

                default:    // Lexer error
                    System.out.println("somehow Lexer FA halted in inappropriate state " + state);
                    System.exit(1);
                    return null;

            }
        }
    }// getToken

    public void putBack( Token token )
    {
        //System.out.println( margin + "put back token " + token.toString() );
        stack.push( token );
    }

    private int getNextSymbol() {
        int result = -1;

        if( lookahead == 0 ) {// is no lookahead, use input
            try{  result = input.read();  }
            catch(Exception e){}
        }
        else {// use the lookahead and consume it
            result = lookahead;
            lookahead = 0;
        }
        return result;
    }

    private void putBackSymbol( int sym ) {
        if( lookahead == 0 ) {// sensible to put one back
            lookahead = sym;
        }
        else {
            System.out.println("Oops, already have a lookahead " + lookahead +
                    " when trying to put back symbol " + sym );
            System.exit(1);
        }
    }// putBackSymbol

    private boolean isLetter(int code ) {
        if(code >= 97 && code <= 122) return true;
        else if(code >= 65 && code <= 90) return true;
        else return false;
    }

    private boolean isDigit(int code ) {
        return '0'<=code && code<='9';
    }

    private boolean isPrintable(int code ) {
        return ' '<=code && code<='~';
    }

    private boolean isOperator(int code){
        return code == '=' || code == '+' || code == '-' || code == '*' ||
                code == '/' || code == '(' || code == ')';
    }

}
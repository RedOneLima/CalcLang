/*  an instance of this class provides methods that produce a
    sequence of tokens following some Finite State Automata,
    with capability to put back tokens
*/
import java.util.*;
import java.io.*;

public class Lexer
{
    public static String margin = "";

    private static String[] keywords = { "show", "msg",
            "newline", "input"};

    private static String[] builtInFunctions = {
            "sqrt", "sin", "cos"};

    // holds any number of tokens that have been put back
    private Stack<Token> stack;
    // the source of physical symbols
    private BufferedReader input;
    // one lookahead physical symbol
    private int lookahead;

    // construct a Lexer ready to produce tokens from a file
    public Lexer( String fileName )
    {
        try{
            input = new BufferedReader( new FileReader( fileName ) );
        }
        catch(Exception e) {
            error("Problem opening file named [" + fileName + "]" );
        }

        stack = new Stack<Token>();
        lookahead = 0;  // indicates no lookahead symbol present
    }// constructor

    // produce the next token
    public Token getNext()
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

            do{
                sym = getNextSymbol();
//State 1
                if( state == 1 ) {
                    if( sym==10 || sym==13 || sym==32 ) {
                        // state stays at 1
                    }
                    else if( isLetter(sym) ) {
                        data += (char)sym;
                        state = 2;
                    }
                    else if( isOperator(sym) ) {
                        data += (char)sym;
                        state = 3;
                    }
                    else if( isDigit(sym) ) {
                        state = 4;
                        data += (char) sym;
                    }
                    else if( sym == '\"' ) {
                        state = 6;
                        data += (char)sym;
                    }
                    else if (sym == -1){
                        state = 8;
                    }
//State 2
                }else if(state == 2) {
                    if(isLetter(sym)) {
                        data += (char)sym;
                        // state stays at  2;
                    }
                    else if(isDigit(sym)) {
                        data += (char)sym;
                        // stay in state 2
                    }else {
                        done = true;
                        putBackSymbol(sym);
                    }
//State 3
                }else if( state == 3 ) {
                    done = true;
                    putBackSymbol(sym);
//State 4
                }else if( state == 4 ) {
                    if(isDigit(sym)) {
                        // stay in state 4
                        data += (char) sym;
                    }else if(sym == '.') {
                        data += (char)sym;
                        state = 5;
                    }else {
                        done = true;
                        putBackSymbol( sym );
                    }
                }
//State 5
                else if( state == 5 )
                {
                    if( isDigit(sym) ) {
                        data += (char) sym;
                    }else {
                        done = true;
                        putBackSymbol(sym);
                    }
                }
//State 6
                else if( state == 6 ) {
                    if(isDigit(sym) || (isPrintable(sym) && sym != '\"') || isLetter(sym)){
                        // stay in state 6
                        data += (char) sym;
                    }
                    else if(sym == '\"'){
                        data += (char)sym;
                        state = 7;
                    }
                }
//State 7
                else if( state == 7 ) {
                    done = true;
                    putBackSymbol(sym);
                }
//State 8
                else if(state == 8){
                    done = true;
                }
                else {
                    error("Unknown state " + state + " in Lexer");
                }

            }while( !done );

//========================================================================================================

            // generate token depending on stopping state
            Token token;

            if( state == 2 ) {// reserved word, bif, or user-defined id
                for( int k=0; k<keywords.length; k++ )
                    if( keywords[k].equals(data) )
                    {
                        token = new Token( data, "" );
                        return token;
                    }
                for( int k=0; k<builtInFunctions.length; k++ )
                    if( builtInFunctions[k].equals(data) )
                    {
                        token = new Token( "bif", data );
                        return token;
                    }
                token = new Token( "var", data );
                return token;
            }
            else if( state == 4 || state == 5 ) {// numeric token
                token = new Token( "num", data );
                return token;
            }
            else if( state == 7 ) {// string literal
                token = new Token( "string", data );
                return token;
            }
            else if( state == 3 ) {// operator symbol
                token = new Token( "opp", data );
                return token;
            }
            else if( state == 8 ) {// eof
                token = new Token( "eof", "" );
                return token;
            }
            else {// Lexer error
                error("somehow Lexer FA halted in inappropriate state " + state );
                return null;
            }
        }

    }// getNext

    public Token getToken() {
        Token token = getNext();
        System.out.println("Got Token: " + token );
        return token;
    }

    public void putBack( Token token )
    {
        System.out.println( margin + "put back token " + token.toString() );
        stack.push( token );
    }

    // next physical symbol is the lookahead symbol if there is one,
    // otherwise is next symbol from file
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

    private static void error( String message ) {
        System.out.println( message );
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        System.out.print("Enter file name: ");
        Scanner keys = new Scanner( System.in );
        String name = keys.nextLine();

        Lexer lex = new Lexer( name );
        Token token;

        do{
            token = lex.getNext();
            System.out.println( token.toString() );
        }while( ! token.getKind().equals( "eof" )  );

    }

}
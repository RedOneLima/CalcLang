import java.util.Scanner;

public class CalcLang {
    public static void main(String[] args) throws Exception {
        Scanner keys = new Scanner( System.in );
        System.out.print("Enter CalcLang file name: ");
        String name = keys.nextLine();
        Lexer lex = new Lexer( name );
        Parser parser = new Parser( lex );
        Node root = parser.parseStatements();
        root.execute();
    }

}

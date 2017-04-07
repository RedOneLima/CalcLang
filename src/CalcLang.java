import java.util.Scanner;

/**
 * Created by Kyle on 4/7/2017.
 */
public class CalcLang {
    public static void main(String[] args) throws Exception {
        Scanner keys = new Scanner( System.in );
        System.out.print("Enter CalcLang file name: ");
        String name = keys.nextLine();
        Lexer lex = new Lexer( name );
        Parser parser = new Parser( lex );
        Node root = parser.parseStatements();

//        TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root );
        root.execute();
    }

}

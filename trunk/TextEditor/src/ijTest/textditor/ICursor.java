package ijTest.textditor;

/**
 * Created by IntelliJ IDEA.
 * User: kvv
 * Date: 15.06.2010
 * Time: 14:50:08
 * To change this template use File | Settings | File Templates.
 */
public interface ICursor {
    void dispose();

    int compare(ICursor c);

    char getChar();
    String getLine();

    boolean isEOL();
    boolean isBOF();
    boolean isEOF();

    void prevChar();
    void nextChar();
    void prevLine();
    void nextLine();

    int getLineNo();
    int getColNo();

    void set(int line, int col);
    void set(ICursor c);

    void toLine(int line);

    void toBOL();
    void toEOL();
    void toBOF();
    void toEOF();
}

package ijTest.textditor;

import ijTest.abstracteditor.IDocument;

/**
 * Created by IntelliJ IDEA.
 * User: kvv
 * Date: 15.06.2010
 * Time: 14:50:25
 * To change this template use File | Settings | File Templates.
 */
public interface ITextDocument extends IDocument {
    int getLineCount();
    String getText(ICursor selStart, ICursor selEnd);
    String getLine(int line);
    void replace(ICursor selStart, ICursor selEnd, String text);
    ICursor createCursor();
    ICursor createCursor(int line, int col);
    ICursor createCursor(ICursor c);
}

package ijTest.textditor;

import ijTest.abstracteditor.IView;

import java.awt.Point;

/**
 * Created by IntelliJ IDEA.
 * User: kvv
 * Date: 15.06.2010
 * Time: 14:43:53
 * To change this template use File | Settings | File Templates.
 */
public interface ITextView extends IView {
    void left(boolean select);
    void right(boolean select);
    void up(boolean select);
    void down(boolean select);
    void homeFile(boolean select);
    void homeLine(boolean select);
    void endFile(boolean select);
    void endLine(boolean select);
    void pageUp(boolean select);
    void pageDown(boolean select);

    ICursor getSelStart();
    ICursor getSelEnd();

    void moveCaret(Point point);
    void dragCaret(Point point);

    void ensureVisible();
}

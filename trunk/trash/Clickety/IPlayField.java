import java.util.*;

interface IPlayField
{
    final static int PF_W = 10;
    final static int PF_H = 16;
    final static int NO_COLOR = -1;

    static final int CMD_CLOSE = 0;
    static final int CMD_INIT = 1;
    static final int CMD_GET = 2;
    static final int CMD_CLICK = 3;
    static final int CMD_CHECK_EOG = 4;
    static final int CMD_GET_CNT = 5;
    static final int CMD_STOP = 6;
    static final int CMD_ADD_OBSERVER = 7;

//    static final int NOTIF_CLEAR = 0;
//    static final int NOTIF_LEFT = 1;
//    static final int NOTIF_DOWN = 2;

    void Init(int ncolors);
    int GetAt(int x, int y);
    boolean Click(int x, int y);
    boolean CheckEndOfGame();
    int GetBlocksCount();

    void stopServer();
    void addObserver(Observer o);
    void deleteObserver(Observer o);
}


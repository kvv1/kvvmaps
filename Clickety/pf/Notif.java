package pf;
import java.util.*;

class NotifElem
{
    int x;
    int y;
    int color;

    NotifElem(int x, int y, int color) { this.x = x; this.y = y; this.color = color; }
}

class Notif
{
    final static int CLEAR = 1;
    final static int DOWN = 2;
    final static int LEFT = 3;

    int code;

    Vector elems = new Vector();

//    int x;
//    int y;
//    int[] yFrom;

//    Notif(int code, int x, int y, int[] yFrom) { this.code = code; this.x = x; this.y = y; this.yFrom = yFrom; }
    Notif(int code) { this.code = code; }
}


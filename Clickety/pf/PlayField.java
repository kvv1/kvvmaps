package pf;
import java.util.*;
import java.net.*;
import java.io.*;

class CField
{
    final static int MARK_MOVE = 1;
    int m_color;
    int m_flags;
    CField(int color) { m_color = color; m_flags = 0; }
};

public class PlayField extends Observable implements IPlayField
{
    final static int PF_W = 10;
    final static int PF_H = 16;
    final static int NO_COLOR = -1;

    private CField[][] m_pf = new CField[PF_W][PF_H];

    Server server = new Server(this);

    void ASSERT(boolean f) { if(!f) throw new Error(); }
    void ASSERT(int n) { ASSERT(n != 0); }
    void ASSERT(Object o) { ASSERT(o != null); }

    PlayField(int ncolors)
    {
        Init(ncolors);
        server.start();
    }

    public void deleteObserver(Observer o)
    {
        super.deleteObserver(o);
    }

    public void addObserver(Observer o)
    {
        super.addObserver(o);
    }

    synchronized public int GetAt(int x, int y) { return m_pf[x][y].m_color; }

    synchronized public void Init(int ncolors)
    {
        Notif notif = new Notif(Notif.CLEAR);

	    for(int x = 0; x < PF_W; x++)
	    for(int y = 0; y < PF_H; y++)
        {
            m_pf[x][y] = new CField((int)Math.round(Math.floor(Math.random() * ncolors)));
            notif.elems.add(new NotifElem(x, y, m_pf[x][y].m_color));
        }

        setChanged();
        notifyObservers(notif);
    }

    synchronized public boolean Click(int x, int y)
    {
        ASSERT(x >= 0 && x < PF_W && y >= 0 && y < PF_H);

        int color = m_pf[x][y].m_color;
        if(color == NO_COLOR) return false;

        if(!IsSingle(x, y))
        {
            SetBlack(x, y, color);
            Compact();
            return true;
        }
        return false;
    }

    synchronized public boolean CheckEndOfGame()
    {
	    for(int x = 0; x < PF_W; x++)
	    for(int y = 0; y < PF_H; y++)
        {
            if(m_pf[x][y].m_color == NO_COLOR) continue;
            if(!IsSingle(x, y)) return false;
        }

        return true;
    }

    synchronized public int GetBlocksCount()
    {
        int n = 0;

	    for(int x = 0; x < PF_W; x++)
	    for(int y = 0; y < PF_H; y++)
            if(m_pf[x][y].m_color != NO_COLOR) n++;

        return n;
    }

    private void SetBlack(int x, int y, int color)
    {
        ASSERT(x >= 0 && x < PF_W && y >= 0 && y < PF_H);
        ASSERT(m_pf[x][y].m_color == color);

        m_pf[x][y].m_color = NO_COLOR;

        Notif notif = new Notif(Notif.CLEAR);
        notif.elems.add(new NotifElem(x, y, NO_COLOR));
        setChanged();
        notifyObservers(notif);

        if(x > 0 && m_pf[x - 1][y].m_color == color) SetBlack(x - 1, y, color);
        if(x < PF_W - 1 && m_pf[x + 1][y].m_color == color) SetBlack(x + 1, y, color);

        if(y > 0 && m_pf[x][y - 1].m_color == color) SetBlack(x, y - 1, color);
        if(y < PF_H - 1 && m_pf[x][y + 1].m_color == color) SetBlack(x, y + 1, color);
    }

    private void Compact()
    {
        boolean moved = true;

        while(moved)
        {
            moved = false;

            Notif notif = new Notif(Notif.DOWN);

	        for(int x = 0; x < PF_W; x++)
            {
                int y;
	            for(y = 0; y < PF_H; y++)
                    if(m_pf[x][y].m_color == NO_COLOR) break;

                if(y == PF_H) continue;

	            for(; y < PF_H - 1; y++)
                {
                    m_pf[x][y] = m_pf[x][y + 1];
                    notif.elems.add(new NotifElem(x, y, m_pf[x][y].m_color));
                    if(m_pf[x][y].m_color != NO_COLOR) moved = true;
                }
                m_pf[x][y] = new CField(NO_COLOR);
                notif.elems.add(new NotifElem(x, y, NO_COLOR));
            }

            if(moved)
            {
                setChanged();
                notifyObservers(notif);
            }
        }

        moved = true;

        while(moved)
        {
            moved = false;

            int x;

    	    for(x = 0; x < PF_W; x++)
            {
                if(m_pf[x][0].m_color == NO_COLOR) break;
            }

            int xFrom = x;

            if(xFrom == PF_W) continue;

            Notif notif = new Notif(Notif.LEFT);

	        for(int y = 0; y < PF_H; y++)
            {
        	    for(x = xFrom; x < PF_W - 1; x++)
                {
                    m_pf[x][y] = m_pf[x + 1][y];
                    notif.elems.add(new NotifElem(x, y, m_pf[x][y].m_color));
                    if(m_pf[x][y].m_color != NO_COLOR) moved = true;
                }

                m_pf[x][y] = new CField(NO_COLOR);
                notif.elems.add(new NotifElem(x, y, NO_COLOR));
            }

            if(moved)
            {
                setChanged();
                notifyObservers(notif);
            }
        }

    }

    private void UnmarkMoves()
    {
	    for(int x = 0; x < PF_W; x++)
        for(int y = 0; y < PF_H; y++)
            m_pf[x][y].m_flags &= ~CField.MARK_MOVE;
    }

    private boolean IsMarkedMove(int x, int y) 
    { 
        return (m_pf[x][y].m_flags & CField.MARK_MOVE) != 0; 
    }

    private void MarkMove(int x, int y)
    {
        if((m_pf[x][y].m_flags & CField.MARK_MOVE) != 0) return;
        m_pf[x][y].m_flags |= CField.MARK_MOVE;

        int color = m_pf[x][y].m_color;

        if(x > 0 && m_pf[x - 1][y].m_color == color) MarkMove(x - 1, y);
        if(x < PF_W - 1 && m_pf[x + 1][y].m_color == color) MarkMove(x + 1, y);

        if(y > 0 && m_pf[x][y - 1].m_color == color) MarkMove(x, y - 1);
        if(y < PF_H - 1 && m_pf[x][y + 1].m_color == color) MarkMove(x, y + 1);
    }

    //////////////////////////////////////////////////////////////////////////

    private boolean IsSingle(int x, int y)
    {
        int color = m_pf[x][y].m_color;
        if(color == NO_COLOR) return false;

        if(x > 0 && color == m_pf[x - 1][y].m_color) return false;
        if(y > 0 && color == m_pf[x][y - 1].m_color) return false;
        if(x < PF_W - 1 && color == m_pf[x + 1][y].m_color) return false;
        if(y < PF_H - 1 && color == m_pf[x][y + 1].m_color) return false;
        return true;
    }

    private boolean IsValidMove(int x, int y)
    { 
        return m_pf[x][y].m_color != NO_COLOR && !IsSingle(x, y); 
    }

    public void stopServer()
    {
        server.stopServer();
    }

    public static void main(String[] args)
    {
    	int ncolors = 5;
    	if(args.length > 0)
    		ncolors = Integer.parseInt(args[0]);
    	
        new PlayField(ncolors);
    }
};

class Server extends Thread
{
    IPlayField pf;
    Server(PlayField pf)
    {
        this.pf = pf;
    }

    private ServerSocket ssock = null;

    public void run()
    {
        try
        {
            ssock = new ServerSocket(701);
            for(;;)
            {
                System.out.println("<accept>");
                Socket sock = ssock.accept();
                new Service(pf, sock).start();
            }
        }
        catch(Exception e)
        {
            System.out.println("Exception " + e.getMessage());
        }

    }

    public void stopServer()
    {
        try 
        {
            System.out.println("<close>");
            if(ssock != null) ssock.close();
        }
        catch(Exception e)
        {
            System.out.println("Exception " + e.getMessage());
        }
    }
}

class Service extends Thread implements Observer
{
    IPlayField pf;
    Socket sock;
    InputStream is;
    OutputStream os;

    Service(IPlayField pf, Socket sock)
    {
        this.pf = pf;
        this.sock = sock;
    }

    public void run()
    {
        try
        {
            is = sock.getInputStream();
            os = sock.getOutputStream();

            for(;;)
            {

                int cmd = is.read();

                if(cmd == IPlayField.CMD_CLOSE) break;

                switch(cmd)
                {
                    case IPlayField.CMD_INIT:
                        pf.Init(is.read());
                        break;
                    case IPlayField.CMD_STOP:
                        pf.stopServer();
                        break;
                    case IPlayField.CMD_GET:
                        {
                        int x = is.read();
                        int y = is.read();
                        os.write(pf.GetAt(x, y));
                        }
                        break;
                    case IPlayField.CMD_CLICK:
                        {
                        int x = is.read();
                        int y = is.read();
                        pf.Click(x, y);
                        }
                        break;
                    case IPlayField.CMD_CHECK_EOG:
                        os.write(pf.CheckEndOfGame() ? 1 : 0);
                        break;
                    case IPlayField.CMD_GET_CNT:
                        os.write(pf.GetBlocksCount());
                        break;
                    case IPlayField.CMD_ADD_OBSERVER:
                        pf.addObserver(this);
                        break;
                }
            }

            // Close everything.
            is.close();
            os.close();
            sock.close();
        }
        catch(Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            pf.deleteObserver(this);
        }
    }

    public void update(Observable o, Object arg)
    {
        Notif notif = (Notif) arg;
        System.out.println("Send Notif code = " + notif.code + " size = " + notif.elems.size());
        if(notif.elems.size() == 0) return;

        try
        {
            os.write(notif.code);
            os.write(notif.elems.size());
            for(int i = 0; i < notif.elems.size(); i++)
            {
                NotifElem elem = (NotifElem) notif.elems.elementAt(i);
                os.write(elem.x);
                os.write(elem.y);
                os.write(elem.color);
            }
            os.flush();
        }
        catch(Exception e)
        {
        }
    }
}


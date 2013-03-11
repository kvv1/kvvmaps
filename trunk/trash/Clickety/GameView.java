import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

class PlayFieldProxy extends Observable implements IPlayField, Runnable
{
    Socket socket;
    InputStream is;
    OutputStream os;

    PlayFieldProxy() 
    { 
        try{
        socket = new Socket("localhost", 701);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        } catch(Exception e) {}
    }

    public void addObserver(Observer o) 
    { 
        super.addObserver(o);
        new Thread(this).start();
    }

    synchronized public void Init(int ncolors)
    {
        try{
        os.write(IPlayField.CMD_INIT);
        os.write(Game.NCOLORS);
        } catch(Exception e) {}
    }
    synchronized public int GetAt(int x, int y)
    {
        try{
        os.write(IPlayField.CMD_GET);
        os.write(x);
        os.write(y);
        int color = is.read();
        if((color & 0x80) != 0) color |= 0xFFFFFFF0;
        return color;
        } catch(Exception e) {}
        return IPlayField.NO_COLOR;
    }
    synchronized public boolean Click(int x, int y)
    {
        try{
        os.write(IPlayField.CMD_CLICK);
        os.write(x);
        os.write(y);
        return true;
        } catch(Exception e) {}
        return false;
    }
    synchronized public boolean CheckEndOfGame()
    {
        try{
        os.write(IPlayField.CMD_CHECK_EOG);
        return is.read() != 0;
        } catch(Exception e) {}
        return true;
    }
    synchronized public int GetBlocksCount()
    {
        try{
        os.write(IPlayField.CMD_GET_CNT);
        return is.read();
        } catch(Exception e) {}
        return 0;
    }

    synchronized public void stopServer()
    {
        try{
        os.write(IPlayField.CMD_STOP);
        } catch(Exception e) {}
    }

    public void run()
    {
        try{
        Socket socket = new Socket("localhost", 701);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        os.write(IPlayField.CMD_ADD_OBSERVER);

        for(;;)
        {
            int code = is.read();
            Notif notif = new Notif(code);
            int n = is.read();
            System.out.println("Receive Notif code = " + code + " size = " + n);
            while(n --> 0)
            {
                int x = is.read();
                int y = is.read();
                int color = is.read();
                if((color & 0x80) != 0) color |= 0xFFFFFFF0;
                notif.elems.add(new NotifElem(x, y, color));
            }
            setChanged();
            notifyObservers(notif);
        }

        } catch(Exception e) {}
    }
}

public class GameView extends JPanel implements MouseListener, ActionListener, Observer
{
//    IPlayField m_pf = new PlayField();


    IPlayField m_pf;

    static Color[] ColorTab;

    {
        ColorTab = new Color[6];
        int i = 0;
        ColorTab[i++] = new Color(255, 128, 128);
        ColorTab[i++] = new Color(128, 255, 128);
        ColorTab[i++] = new Color(128, 128, 255);
        ColorTab[i++] = new Color(255, 255, 128);
        ColorTab[i++] = new Color(128, 255, 255);
        ColorTab[i++] = new Color(255, 128, 255);
    }

    static Color RGB_EMPTY = new Color(64, 64, 64);

    static final int CELL_SIZE = 28;
    static final int X_OFFSET = 10;
    static final int Y_OFFSET = 10;

    public GameView()
    {
//        new PlayField();
        m_pf = new PlayFieldProxy();
        m_pf.addObserver(this);
        addMouseListener(this);
    }

    public void actionPerformed(ActionEvent e) 
    {
        JMenuItem target = (JMenuItem)e.getSource();

        String cmd = target.getActionCommand();

        if(cmd.equals("restart"))
        {
            m_pf.Init(Game.NCOLORS);
        }
    }

    boolean OnClick(int x, int y)
    {
        System.out.println("x = " + x + " y = " + y);

        boolean res = m_pf.Click(x, y);

        if(res && m_pf.CheckEndOfGame()) 
        {
            int cnt = m_pf.GetBlocksCount();
            JOptionPane.showMessageDialog(this, "Game over. " + cnt + " blocks left", "Game over", 
                JOptionPane.INFORMATION_MESSAGE, null);
            m_pf.Init(Game.NCOLORS);
        }

        return res;
    }

    public void mousePressed(MouseEvent e) 
    {
        Point point = e.getPoint();

        if(point.x >= X_OFFSET + 1 && point.y >= Y_OFFSET + 1)
        {
            int x = (point.x - X_OFFSET - 1) / CELL_SIZE;
            int y = IPlayField.PF_H - (point.y - Y_OFFSET - 1) / CELL_SIZE - 1;

            if(x >= 0 && x < IPlayField.PF_W && y >= 0 && y < IPlayField.PF_H)
            {
                OnClick(x, y);
            }
        }
    }


    synchronized public void update(Observable o, Object arg)
    {
        Notif notif = (Notif) arg;
        switch(notif.code)
        {
            case Notif.CLEAR:
                _OnClear(notif);
                break;
            case Notif.LEFT:
                _OnMoveLeft(notif);
                break;
            case Notif.DOWN:
                _OnMoveDown(notif);
                break;
        }
    }

    public void _OnMoveDown(Notif notif)
    {
        Graphics g = getGraphics();

        for(int i = CELL_SIZE - 4; i >= 0; i -= 4)
        {
            for(int j = 0; j < notif.elems.size(); j++)
            {
                NotifElem elem = (NotifElem) notif.elems.elementAt(j);
                DrawField(g, elem.x, elem.y, elem.color, 0, i);
            }

            try { Thread.currentThread().sleep(10); } catch(Exception e) {}
        }
    }

    public void _OnMoveLeft(Notif notif)
    {
        Graphics g = getGraphics();

        for(int i = CELL_SIZE - 4; i >= 0; i -= 4)
        {
            for(int j = 0; j < notif.elems.size(); j++)
            {
                NotifElem elem = (NotifElem) notif.elems.elementAt(j);
                DrawField(g, elem.x, elem.y, elem.color, i, 0);
            }
            try { Thread.currentThread().sleep(10); } catch(Exception e) {}
        }
    }

    public void _OnClear(Notif notif)
    {
        for(int i = 0; i < notif.elems.size(); i++)
        {
            NotifElem elem = (NotifElem) notif.elems.elementAt(i);
            DrawField(getGraphics(), elem.x, elem.y, elem.color, 0, 0);
        }
    }

    public void mouseReleased(MouseEvent e) 
    {
    }

    public void mouseEntered(MouseEvent e) 
    {
    }

    public void mouseExited(MouseEvent e) 
    {
    }

    public void mouseClicked(MouseEvent e) 
    {
    }


    void DrawField(Graphics g, int x, int y, int colorIdx, int xoffset, int yoffset)
    {
        Color color = RGB_EMPTY;
        if(colorIdx != IPlayField.NO_COLOR) color = ColorTab[colorIdx];

        Color oldColor = g.getColor();
        g.setColor(color);

        int X = x * CELL_SIZE + 1 + X_OFFSET + xoffset;
        int Y = (IPlayField.PF_H - y - 1) * CELL_SIZE + 1 + Y_OFFSET - yoffset;

        int XSIZE = CELL_SIZE;
        int YSIZE = CELL_SIZE;

        if(x == IPlayField.PF_W - 1) { XSIZE -= xoffset; }
        if(y == IPlayField.PF_H - 1) { YSIZE -= yoffset; Y += yoffset; }

        g.fillRect(X, Y, XSIZE, YSIZE);

        g.setColor(oldColor);
    }
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        for(int x = 0; x < IPlayField.PF_W; x++)
        for(int y = 0; y < IPlayField.PF_H; y++)
        {
            DrawField(g, x, y, m_pf.GetAt(x, y), 0, 0);
        }
    }

    void onClosed()
    {
        //m_pf.stopServer();
    }
}


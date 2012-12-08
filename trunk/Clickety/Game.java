import java.util.*;
import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import java.awt.Toolkit;

public class Game extends JFrame
{
    GameView view;
    
    public static int NCOLORS = 5;

    protected void processWindowEvent(WindowEvent e)
    {
        if(e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            view.onClosed();
        }

        super.processWindowEvent(e);
    }

    Game()
    {
        setTitle("Untitled - game");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        view = new GameView();

        getContentPane().add(view);

        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.add("restart").addActionListener(view);
        menuBar.add(menu);

        setJMenuBar(menuBar);

        addMouseListener(view);

        setSize(310, 28 * 18 + 20);
        setVisible(true);
    }


    static public void main(String[] args)
    {
    	if(args.length > 0)
    		NCOLORS = Integer.parseInt(args[0]);
    		
    	pf.PlayField.main(new String[] {Integer.toString(NCOLORS)});
        Game fr = new Game();
    }
}


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import javax.imageio.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

//do filter of image in serial
class DisplayImage{
    public static void main(String[] args) throws InterruptedException, IOException {
    	
        int w = 500; // window width
        int h=500; //window height
    	
		//deal with command line arguments if provided
		if (args.length==2)  {
				w=Integer.parseInt(args[0]); // width provided
				h=Integer.parseInt(args[1]); // width provided			
		}
		System.out.println("Using image  of size "+w + "x"+h );
        BufferedImage dish =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ImageIcon icon=new ImageIcon(dish);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(w,h);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int noThreads=6;
        Thread []cols= new Thread[noThreads];
  
        CountDownLatch allDone = new CountDownLatch(noThreads);
        CyclicBarrier middle_barrier= new CyclicBarrier(noThreads);
        
       // for(int i=0,y=0;i<noThreads;i++,y+=20) {
        cols[0] =new Thread( new HThread(10,490,100,150,dish,
        		new Color(255,0,0),allDone, middle_barrier));
        cols[1] =new Thread( new HThread(10,490,200,250,dish,
        		new Color(255,0,0),allDone, middle_barrier));
        cols[2] =new Thread( new VThread(200,250,10,490,dish,
        		new Color(0,255,0),allDone, middle_barrier));
        cols[3] =new Thread( new VThread(100,150,10,490,dish,
        		new Color(0,255,0),allDone, middle_barrier));
        cols[4] =new Thread( new HThread(10,490,300,350,dish,
        		new Color(255,0,0),allDone, middle_barrier));
        cols[5] =new Thread( new VThread(300,350,10,490,dish,
        		new Color(0,255,0),allDone, middle_barrier));

        	
       // }
        for(int i=0;i<noThreads;i++) {cols[i].start();}
        Thread frame_painter = new Thread () { public void run() { while (true) frame.repaint(); }};
        frame_painter.start();
        allDone.await(); //instead of lots of joins
        File dstFile = new File("output.png");
        ImageIO.write(dish, "png", dstFile);
        
        
    }
}
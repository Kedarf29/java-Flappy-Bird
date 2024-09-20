import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;
import java.awt.geom.AffineTransform;

public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    private double angle = 0; // Initial angle for rotation
    private Timer timer; // Timer for animation

    //images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //bird class
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;
    int birdSpeed = 30;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

     //pipe class
     int pipeX = boardWidth;
     int pipeY = 0;
     int pipeWidth = 64;  //scaled by 1/6
     int pipeHeight = 512;
     
     class Pipe {
         int x = pipeX;
         int y = pipeY;
         int width = pipeWidth;
         int height = pipeHeight;
         Image img;
         boolean passed = false;
 
         Pipe(Image img) {
             this.img = img;
         }
     }
 

    //game logic
    Bird bird;
    int velocityX = -4;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    //game timer
    Timer gameLoop;
    boolean gameOver = false;
    Timer placePipeTimer;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth,boardHeight));

        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Code to be executed
              placePipes();
            }
        });
        placePipeTimer.start();
        //game timer
		gameLoop = new Timer(1000/60, this); //how long it takes to start timer, milliseconds gone between frames 
        gameLoop.start();

        // Set up key listener
        setFocusable(true);
        addKeyListener(this);
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
    
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}
    public void draw(Graphics g) {
        //System.out.println("draww");
        //background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(200, 400); // Move to the center of the sun
        g2d.rotate(angle); // Rotate around the center

        // Draw the sun (a yellow circle)
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(-50, -50, 100, 100); // Sun's body

        // Draw the sun rays
        g2d.setColor(Color.ORANGE);
        for (int i = 0; i < 12; i++) {
            g2d.rotate(Math.toRadians(30)); // Rotate for each ray
            g2d.drawLine(0, -50, 0, -80); // Draw ray
        }

        // Restore original transform
        g2d.setTransform(oldTransform);
        
        //bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

          //pipes
          for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //score
        g.setColor(Color.white);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }
    public void move() {
        angle += Math.toRadians(5); // Rotate 5 degrees
        if (angle >= Math.toRadians(360)) {
            angle = 0; // Reset angle
        }

          //pipes
          for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; //0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }


        }
    
        boolean collision(Bird a, Pipe b) {
            return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
                   a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
                   a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
                   a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
        }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
       if (keyCode == KeyEvent.VK_UP) {
        bird.y -= birdSpeed; // Move bird up
    } else if (keyCode == KeyEvent.VK_DOWN) {
        bird.y += birdSpeed; // Move bird down
    }else if (keyCode == KeyEvent.VK_RIGHT) {
        bird.x += birdSpeed; // Move bird down
    }else if (keyCode == KeyEvent.VK_LEFT) {
        bird.x -= birdSpeed; // Move bird down
    }
    // Ensure the bird doesn't move out of bounds
    if (bird.y < 0) bird.y = 0;
    if (bird.y > boardHeight - bird.height) bird.y = boardHeight - bird.height;
    if (bird.x < 0) bird.x = 0;
    if (bird.x > boardWidth - bird.width) bird.x = boardWidth - bird.width;
    repaint(); // Trigger a repaint to update the position
    }
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
         repaint();
         if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }
}

    

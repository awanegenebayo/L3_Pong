package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import network.Client;
import network.CustomProtocol;
import network.Server;

/**
 * An Pong is a Java graphical container that extends the JPanel class in
 * order to display graphical elements.
 */
public class Pong extends JPanel implements KeyListener {

	private static final long serialVersionUID = 1L;

	/**
	 * Constant (c.f. final) common to all Pong instances (c.f. static)
	 * defining the background color of the Pong
	 */
	private static final Color backgroundColor = new Color(0x22, 0x40, 0);
	
	/**
	 * defining the color of the score
	 */
	private static final Color score = new Color(0, 0, 0);

	/**
	 * Width of pong area
	 */
	private static final int SIZE_PONG_X = 800;
	/**
	 * Height of pong area
	 */
	private static final int SIZE_PONG_Y = 600;
	/**
	 * Time step of the simulation (in ms)
	 */
	public static final int timestep = 10;
	
	
	public static final Point POINT_DEFAULT = new Point(800,800);

	/**
	 * Pixel data buffer for the Pong rendering
	 */
	private Image buffer = null;
	/**
	 * Graphic component context derived from buffer Image
	 */
	public Graphics graphicContext = null;

	/**
	 * One Ball to be displayed
	 */
	private BallType ball;
	/**
	 * New Balls to be displayed with bonuses
	 */
	private BallType ball2;

	private RacketType racketPlayer;
	private RacketType racketOpponent;

	private Client client;
	private Server server;
	
	private Score pongScore;
	private int playTime = 0;
	private Bonus bonus;
	private Bonus bonus1;
	private boolean bonusIsCreated = false;
	
	private long time;
	
	
	//Si on est un serveur
	public Pong(Server s) {
		this.server = s;
		this.client = null;
		this.pongScore = new Score();
		this.time = System.currentTimeMillis();
		construct(false);
	}
	
	//Si on est un client
	public Pong(Client c){
		this.client = c;
		this.server = null;
		this.pongScore = new Score();
		this.time = System.currentTimeMillis();
		construct(true);
	}

	
	//Par soucis de factorisation
	public void construct(boolean client){
		this.ball = new Ball(client);
		//seconde balle utilisée quand l'un des joueurs atteind 10 points
		//this.ball2 = new Ball(client);
		this.racketPlayer = new Racket(true);
		
		this.racketOpponent = new Racket(false);
		
		this.setPreferredSize(new Dimension(SIZE_PONG_X, SIZE_PONG_Y));
		this.addKeyListener(this);
	}

	/**
         * Proceeds to the movement of the ball and updates the screen
	 */
	public void animate() {
		/* Update ball position */
		ball.moveBall(SIZE_PONG_X, SIZE_PONG_Y, racketPlayer, racketOpponent, pongScore);
		playTime++;
		//Permet le lancement des bonus a un moment precis
		if(playTime == 300 || playTime == 301)
			bonusManagement();
		// Si le round est en cour
		if(!pongScore.getFinRound()){
			//Permet le lancement des bonus a un moment precis
			/* Update racket position */
			racketPlayer.moveRacket(SIZE_PONG_Y, ball);
			/* Update racket position */
			racketOpponent.moveRacket(SIZE_PONG_Y, ball);
			/* And update output */
			updateScreen();
		}else{
		// Sinon on reinitialise le round
			pongScore.setFinRound(false);
			boolean client = (this.server == null);
			ball.restartBall(client);
			racketPlayer.restartRacket(true);
			racketOpponent.restartRacket(false);
			/* And update output */
			updateScreen();
			// On attend deux sec avant le debut du prochain round
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				racketPlayer.setSpeed(racketPlayer.getSpeed()-racketPlayer.getBaseSpeed());
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				racketPlayer.setSpeed(racketPlayer.getBaseSpeed());
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
				racketPlayer.releaseTheBall(ball, this);
				break;
			default:
				System.out.println("got press "+e);
		}
	}
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				racketPlayer.setSpeed(0);
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				racketPlayer.setSpeed(0);
				break;
			default:
				System.out.println("got release "+e);
		}
	}
	public void keyTyped(KeyEvent e) { }

	/*
	 * (non-Javadoc) This method is called by the AWT Engine to paint what
	 * appears in the screen. The AWT engine calls the paint method every time
	 * the operative system reports that the canvas has to be painted. When the
	 * window is created for the first time paint is called. The paint method is
	 * also called if we minimize and after we maximize the window and if we
	 * change the size of the window with the mouse.
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		g.drawImage(buffer, 0, 0, this);
	}

	/**
	 * Draw each Pong item based on new positions
	 */
	public void updateScreen() {
		
		sendReceiveData();
		
		if (buffer == null) {
			/* First time we get called with all windows initialized */
			buffer = createImage(SIZE_PONG_X, SIZE_PONG_Y);
			if (buffer == null)
				throw new RuntimeException("Could not instanciate graphics");
			else
				graphicContext = buffer.getGraphics();
		}
		/* Fill the area with green */
		graphicContext.setColor(backgroundColor);
		graphicContext.fillRect(0, 0, SIZE_PONG_X, SIZE_PONG_Y);

		/* Draw items */
		graphicContext.drawImage(ball.getImage(), ball.getPosition().x, ball.getPosition().y, ball.getWidth(), ball.getHeight(), null);
		graphicContext.drawImage(racketPlayer.getImage(), racketPlayer.getPosition().x, racketPlayer.getPosition().y, racketPlayer.getWidth(), racketPlayer.getHeight(), null);
		graphicContext.drawImage(racketOpponent.getImage(), racketOpponent.getPosition().x, racketOpponent.getPosition().y, racketOpponent.getWidth(), racketOpponent.getHeight(), null);
		graphicContext.setColor(score);
		graphicContext.drawString("score Player:"+Integer.toString(pongScore.getScorePlayer()), 400, 50);
		graphicContext.drawString("score Opponent:"+Integer.toString(pongScore.getScoreOpponent()), 400, 70);
//		bonusManagement();

		this.repaint();
	}
	
	public void sendReceiveData(){
		boolean cheater = false;
		if(this.client == null){//Je suis serveur
			server.setData(new CustomProtocol((int)racketPlayer.getPosition().getY(), ball.getPosition(), ball.getHasLift(), ball.getLiftSpeed(), ball.getSpeed().y));
			CustomProtocol p = server.getData();
			racketOpponent.setY(p.getRacketY());
//			System.out.println("ball pos X = "+(SIZE_PONG_X - ball.getPosition().x - ball.getWidth())+" Y = "+ball.getPosition().y+" lift? "+ball.getHasLift()+" Opp = "+p.getHasLift()+" P = "+ball.getLiftSpeed()+" O = "+p.getLiftSpeed()+" speed X = "+ball.getSpeed().x+" Y = "+ball.getSpeed().y);
			if (ball.getPosition().getX() > ((SIZE_PONG_X - ball.getWidth())/2) ){//C'est de l'autre côté, on prend les données de l'autre
				if ((System.currentTimeMillis() - time) >= 10){
					if ((SIZE_PONG_X - ball.getPosition().getX() - ball.getWidth())!= p.getBallPosition().getX() ||
								ball.getPosition().getY() != p.getBallPosition().getY() ||
								ball.getHasLift() != p.getHasLift() ||
								ball.getLiftSpeed() != p.getLiftSpeed() ||
								ball.getSpeed().getY() != p.getBallSpeedY()){
						cheater = true;
//						System.out.println("ball pos X = "+(SIZE_PONG_X - ball.getPosition().x - ball.getWidth())+" Y = "+ball.getPosition().y+" lift? "+ball.getHasLift()+" Opp = "+p.getHasLift()+" P = "+ball.getLiftSpeed()+" O = "+p.getLiftSpeed()+" speed X = "+ball.getSpeed().x+" Y = "+ball.getSpeed().y);
//						System.out.println("ball pos X = "+ball.getPosition().getX()+" OppX = "+(RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth()));
					}
					time = System.currentTimeMillis();
				}
				ball.setPosition(p.getBallPosition());
				ball.inverserPosition(SIZE_PONG_X);
//				System.out.println("ball pos X = "+ball.getPosition().getX()+" OppX = "+(RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth()));
				if (ball.getPosition().getX() == RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth()|| 
						ball.getPosition().getX() == RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth() - Math.abs(ball.getSpeed().getX())){
					ball.setHasLift(p.getHasLift());
					ball.setLiftSpeed(p.getLiftSpeed());
					ball.setSpeedY(p.getBallSpeedY());
					cheater = false;
				}
				if (cheater)
					System.out.println("CHEATER !!!!!"); 
			}
		}
		else{//Je suis client
			client.setData(new CustomProtocol((int)racketPlayer.getPosition().getY(), ball.getPosition(), ball.getHasLift(), ball.getLiftSpeed(), ball.getSpeed().y));
			CustomProtocol p = client.getData();
			racketOpponent.setY(p.getRacketY());
//			System.out.println("ball pos X = "+ball.getPosition().x+" Y = "+ball.getPosition().y+" lift? "+ball.getHasLift()+" Opp = "+p.getHasLift()+" P = "+ball.getLiftSpeed()+" O = "+p.getLiftSpeed()+" speed X = "+ball.getSpeed().x+" Y = "+ball.getSpeed().y); 
			if (ball.getPosition().getX() > ((SIZE_PONG_X - ball.getWidth())/2) ){//C'est de l'autre côté, on prend les données de l'autre
				if ((System.currentTimeMillis() - time) >= 10){
					if ((SIZE_PONG_X - ball.getPosition().getX()- ball.getWidth())!= p.getBallPosition().getX() ||
								ball.getPosition().getY() != p.getBallPosition().getY() ||
										ball.getHasLift() != p.getHasLift() ||
										ball.getLiftSpeed() != p.getLiftSpeed() ||
										ball.getSpeed().getY() != p.getBallSpeedY()){
						cheater = true;
//						System.out.println("ball pos X = "+ball.getPosition().x+" Y = "+ball.getPosition().y+" lift? "+ball.getHasLift()+" Opp = "+p.getHasLift()+" P = "+ball.getLiftSpeed()+" O = "+p.getLiftSpeed()+" speed X = "+ball.getSpeed().x+" Y = "+ball.getSpeed().y); 
//						System.out.println("ball pos X = "+ball.getPosition().getX()+" OppX = "+(RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth()));
					}
					time = System.currentTimeMillis();
				}
				ball.setPosition(p.getBallPosition());
				ball.inverserPosition(SIZE_PONG_X);
//				System.out.println("ball pos X = "+ball.getPosition().getX()+" OppX = "+(RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth()));
				if (ball.getPosition().getX() == RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth()|| 
						ball.getPosition().getX() == RacketType.RACKET_OPPONENT_BASE_POSITION_X - ball.getWidth() - Math.abs(ball.getSpeed().getX())){
					ball.setHasLift(p.getHasLift());
					ball.setLiftSpeed(p.getLiftSpeed());
					ball.setSpeedY(p.getBallSpeedY());
					cheater = false;
				}
				if (cheater)
					System.out.println("CHEATER !!!!!"); 
			}
		}
	}
	/**
	 * Gestion des bonuses
	 */
	private void bonusManagement(){
		if(!bonusIsCreated){
			this.bonus = new Bonus(1,4,ball,racketPlayer,racketOpponent,true);
			this.bonus1 = new Bonus(1,4,ball,racketPlayer,racketOpponent,false);
			System.out.print("Arrivée des bonus\n");
			bonusIsCreated = true;
			bonus.updateScreenBonus(this);
		}
		if(bonus.getInUse() == true){
			bonus.moveBonus(SIZE_PONG_X, SIZE_PONG_Y ,racketPlayer, racketOpponent, ball);
			bonus.updateScreenBonus(this);
		}
		if(bonus1.getInUse() == true){
			bonus1.moveBonus(SIZE_PONG_X, SIZE_PONG_Y ,racketPlayer, racketOpponent, ball);
			bonus1.updateScreenBonus(this);
		}
		if(bonus.getInUse() == false && bonus1.getInUse() == false){
			playTime = 302;
			bonusIsCreated = false;
			System.out.print("Fin des bonus\n");
		}
		else
			playTime = 300;
	}
}

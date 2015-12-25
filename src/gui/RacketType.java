package gui;

import java.awt.Image;
import java.awt.Point;
import javax.swing.ImageIcon;

public abstract class RacketType extends PongItem{

	/**
	 * Speed of racket, in pixels per timestamp
	 */
	protected int baseSpeed;
	/**
	 * Speed of racket, in pixels per timestamp
	 */
	protected int speed;
	/**
	 * Speed of racket (in pixels per second)
	 */
	public static final int RACKET_BASE_SPEED = 4;
	
	public RacketType(Image image, boolean player){
		super(image);
		speed = 0;
		baseSpeed = RACKET_BASE_SPEED;
		if(player)
			this.position.setLocation(28, 250);
		else
			this.position.setLocation(750, 250);
	}

	/**
	 * Get / Set accessors object
	 */
	public int getBaseSpeed() {
		return baseSpeed;
	}
	
	public void setBaseSpeed(int baseSpeed) {
		this.baseSpeed = baseSpeed;
	}
	
	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	abstract void moveBallOnRacketCote(int size_pong_x, int size_pong_y,RacketType racketPlayer, RacketType racketOpponent, BallType ball);
	abstract void moveBallOnRacketOther(int size_pong_x, int size_pong_y,RacketType racketPlayer, RacketType racketOpponent, BallType ball);
	
	public void moveRacket(int size_pong_y, BallType ball) {
		int posY=position.y;
		boolean prob=false;
		for(int i=Math.abs(speed); i>0; i--){
			if (itemOnRacketHaut(ball))
				prob=true;
			position.y += (speed/Math.abs(speed));
		}
		if (itemOnRacketHaut(ball))
			prob=true;
		if (prob)
			position.y = posY;
		if (position.y < 0)
			position.y = 0;
		if (position.y > size_pong_y - height)
			position.y = size_pong_y - height;
	}
	
	public void setY(int y){
		this.position.setLocation(this.position.getX(), y);
	}

	/**
     * Test if the ball is on the racket
	 */
	public boolean itemOnRacketCote(PongItem item) {
		return ((((
			// Si l'item touche sur les cot�s de la racket
			item.getPosition().y > getPosition().y &&
			item.getPosition().y < getPosition().y + getHeight()) || (
			item.getPosition().y + item.getHeight() > getPosition().y &&
			item.getPosition().y + item.getHeight() < getPosition().y + getHeight())) && ((
			item.getPosition().x == getPosition().x + getWidth()) || (
			item.getPosition().x + item.getWidth() == getPosition().x))));
	}
	
	public boolean itemOnRacketHaut(PongItem item) {
		return ((((
			// Si l'item touche le dessous de la racket
			item.getPosition().x > getPosition().x &&
			item.getPosition().x < getPosition().x + getWidth()) || (
			item.getPosition().x + item.getWidth() > getPosition().x &&
			item.getPosition().x + item.getWidth() < getPosition().x + getWidth())) && ((
			item.getPosition().y + item.getHeight() == getPosition().y ) || (
			item.getPosition().y == getPosition().y + getHeight()))));
	}
	
	public boolean itemOnRacketCorner(PongItem item) {
		return ((
			// Si l'item touche sur les coins de la racket
			item.getPosition().y == getPosition().y + getHeight() || 
			item.getPosition().y + item.getHeight() == getPosition().y) && (
			item.getPosition().x == getPosition().x + getWidth() || 
			item.getPosition().x + item.getWidth() == getPosition().x));
	}
}

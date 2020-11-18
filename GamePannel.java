package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.net.SocketTimeoutException;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JPanel;

import game.net.Conexion;
import game.net.ConexionCliente;
import game.net.ConexionServer;

@SuppressWarnings("serial")
public class GamePannel extends JPanel implements Runnable, KeyListener {

	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;

	// Render
	private Graphics2D g2d;
	private BufferedImage image;
	// Game Loop
	private Thread thread;
	private boolean running;
	private long TargetTime;

	// Game Stuff
	private final int SIZE = 10;
	private Entity head, apple, head2, apple2;
	private ArrayList<Entity> snake, snake2;
	private int score, score2;
	private int level, level2;
	private boolean start2;
	private boolean gameover, win, empate;
	private boolean ready;

	private float tiempoParaMoverse = 0.25f;

	// Movement
	private int dy, dx;
	private boolean recibir;

	// key input
	private boolean up, down, left, right, start;

	private Conexion Conexion;
	private Object obj = new Object();
	private Thread commThread;

	public GamePannel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
		addKeyListener(this);

		Scanner input = new Scanner(System.in);

		System.out.println("Ingrese 1 para hostear, 2 para conectarse: ");

		int elegido = Integer.valueOf(input.nextLine());
		if (elegido == 2) {
			System.out.println("Ingrese la direccion IP a la que desea conectarse");
			String ip = input.nextLine();
			Conexion = new ConexionCliente(ip);
		} else if (elegido == 1) {
			Conexion = new ConexionServer();
		}
		input.close();

		recibir = Conexion instanceof ConexionServer;

		Conexion.conectar();

		commThread = new Thread(() -> {
			while (true) {
				if (Conexion instanceof ConexionServer) {
					System.out.println("Recibiendo data");
					String data = Conexion.recibir();
					System.out.println("Data recibida");
					synchronized (obj) {
						Reconvertir(data);
					}
					System.out.println("Enviando data");
					synchronized (obj) {
						Conexion.Enviar(ConvertToString());
					}
					System.out.println("Data enviada");
				} else {
					System.out.println("Enviando data");
					synchronized (obj) {
						Conexion.Enviar(ConvertToString());
					}
					System.out.println("Data enviada");
					System.out.println("Recibiendo data");
					String data = Conexion.recibir();
					synchronized (obj) {
						Reconvertir(data);
					}
					System.out.println("Data recibida");
				}
			}
		});
	}

	public void addNotify() {
		super.addNotify();
		thread = new Thread(this);
		thread.start();
	}

	private void setFPS(int fps) {
		TargetTime = 10000 / fps;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();

		if (k == KeyEvent.VK_UP)
			up = true;
		if (k == KeyEvent.VK_DOWN)
			down = true;
		if (k == KeyEvent.VK_LEFT)
			left = true;
		if (k == KeyEvent.VK_RIGHT)
			right = true;
		if (k == KeyEvent.VK_ENTER)
			start = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// se reemplaza seteando todas las variables de keys a false en el
		// update
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// No le damos uso a este evento pero es necesario por la interfaz
		// keylistener
	}

	@Override
	public void run() {
		if (running)
			return;
		init();
		long startTime;
		long elapsed;
			while (running) {
				startTime = System.nanoTime();
				elapsed = System.nanoTime() - startTime;
	
				update(elapsed / 100000f);
				requestRender();
				try {
					Thread.sleep((long) (tiempoParaMoverse * 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	}

	private void init() {
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();
		running = true;
		setUpLevel();
		commThread.start();

		// No empezamos el juego hasta que el otro jugador haya enviado algun dato
		while (true) {
			synchronized (obj) {
				if (snake2 != null && head2 != null && apple2 != null)
					break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void setUpLevel() {
		snake = new ArrayList<Entity>();
		head = new Entity(SIZE);

		if (Conexion instanceof ConexionServer)
			head.setPosition(WIDTH / 2, HEIGHT / 2 - SIZE);
		else
			head.setPosition(WIDTH / 2, HEIGHT / 2 + SIZE);

		snake.add(head);

		for (int i = 1; i < 3; i++) {
			Entity e = new Entity(SIZE);
			e.setPosition(head.getX() + (i * SIZE), head.getY());
			snake.add(e);
		}
		apple = new Entity(SIZE);
		setApple();
		score = score2 = 0;
		gameover = win = empate = false;
		level = 1;
		dx = dy = 0;
		setFPS(level * 10);

		ready = true;
	}

	public void setApple() {
		int x = (int) (Math.random() * (WIDTH - SIZE));
		int y = (int) (Math.random() * (HEIGHT - SIZE));
		x = x - (x % SIZE);
		y = y - (y % SIZE);
		apple.setPosition(x, y);
	}

	private void requestRender() {
		synchronized (obj) {
			render(g2d);
			Graphics g = getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
		}
	}

	private void update(float elapsed) {
		synchronized (obj) {
				if (up && snake.get(1).getY() >= head.getY()) {
					dy = -SIZE;
					dx = 0;
				}
	
				if (down && snake.get(1).getY() <= head.getY()) {
					dy = SIZE;
					dx = 0;
				}
	
				if (left && snake.get(1).getX() >= head.getX()) {
					dx = -SIZE;
					dy = 0;
				}
	
				if (right && snake.get(1).getX() <= head.getX()) {
					dx = SIZE;
					dy = 0;
				}

			if ((dx != 0 || dy != 0) && start2) {
				for (int i = snake.size() - 1; i > 0; i--) {
					snake.get(i).setPosition(snake.get(i - 1).getX(), snake.get(i - 1).getY());
				}
				head.move(dx, dy);
			}

			if (head.getX() < 0)// hace que la serpiente de la vuelta al panel
				head.setX(WIDTH);
			if (head.getY() < 0)
				head.setY(HEIGHT);
			if (head.getX() > WIDTH)
				head.setX(0);
			if (head.getY() > HEIGHT)
				head.setY(0);

			if (head2.getX() < 0)
				head2.setX(WIDTH);
			if (head2.getY() < 0)
				head2.setY(HEIGHT);
			if (head2.getX() > WIDTH)
				head2.setX(0);
			if (head2.getY() > HEIGHT)
				head2.setY(0);

			if ((WIDTH / SIZE) * (HEIGHT / SIZE) == snake.size()) {
				win = true;
				dx = dy = 0;
			}
			if (gameover || win || empate) {
				if (start && start2) {
					setUpLevel();
					ready = false;
				}
				return;
			}

			gameover = win = empate = false;

			for (Entity e : snake) {
				if (e != snake.get(0)) {
					if (e.IsCollsion(head)) {
						gameover = true;
						break;
					}
					if (e.IsCollsion(head2)) {
						win = true;
						break;
					}
				}
			}

			for (Entity e : snake2) {
				if (e != snake2.get(0)) {
					if (e.IsCollsion(head2)) {
						win = true;
						break;
					}
					if (e.IsCollsion(head)) {
						gameover = true;
						break;
					}
				}
			}

			if (head.IsCollsion(head2)) {
				empate = true;
			}

			if (apple.IsCollsion(head) || apple2.IsCollsion(head)) {
				score++;

				Entity e = new Entity(SIZE);
				e.setPosition(-100, -100);
				snake.add(e);

				if (apple.IsCollsion(head)) {
					setApple();
				}

				if (score % 10 == 0) {
					level++;
					if (level > 10)
						level = 10;
					setFPS(level * 10);
				}
				if (tiempoParaMoverse > 0) {
					tiempoParaMoverse = tiempoParaMoverse - 0.01f;
				}
			}

			if (apple.IsCollsion(head2) || apple2.IsCollsion(head2)) {
				score2++;
				if (apple.IsCollsion(head2))
					setApple();

				if (score2 % 10 == 0) {
					level2++;
					if (level2 > 10)
						level2 = 10;
					setFPS(level2 * 10);
				}
			}

			if(win == true || gameover == true || empate == true) running = false;
			left = right = up = down = start = start2 = false;
		}
	}

	private void render(Graphics2D g2d) {
		g2d.clearRect(0, 0, WIDTH, HEIGHT);

		g2d.setColor(Color.GREEN); // dibujar serpiente 1
		for (Entity e : snake) {
			e.render(g2d);
		}

		g2d.setColor(Color.BLUE); // dibujar serpiente 2
		for (Entity e : snake2) {
			e.render(g2d);
		}

		g2d.setColor(Color.RED);
		apple.render(g2d);

		g2d.setColor(Color.ORANGE);
		apple2.render(g2d);

		if (gameover) {
			g2d.scale(2, 2);
			g2d.setColor(Color.RED);
			g2d.drawString("PERDISTE!", WIDTH / 6, HEIGHT / 6);
			g2d.scale(0.5, 0.5);
		}

		if (win) {
			g2d.scale(2, 2);
			g2d.setColor(Color.GREEN);
			g2d.drawString("HAS GANADO!", WIDTH / 6, HEIGHT / 6);
			g2d.scale(0.5, 0.5);

		}

		if (empate) {
			g2d.scale(2, 2);
			g2d.setColor(Color.RED);
			g2d.drawString("AMBOS JUGADORES HAN PERDIDO!", WIDTH / 6, HEIGHT / 6);
			g2d.scale(0.5, 0.5);
		}

		g2d.setColor(Color.WHITE);
		g2d.drawString("PUNTAJE JUGADOR 1: " + score, 10, 10);
		g2d.drawString("PUNTAJE JUGADOR 2: " + score2, 10, 30);
		g2d.drawString("NIVEL DEL JUGADOR 1: " + level, 10, 60);
		g2d.drawString("NIVEL DEL JUGADOR 2: " + level2, 10, 80);

		if (dx == 0 && dy == 0 && (ready)) {
			g2d.drawString("READY!", WIDTH / 2, HEIGHT / 2);
		}

	}

	public String ConvertToString() {
		String serX = "", serY = "";
		String appleX = "", appleY = "";
		String puntaje = "", nivel = "";
		int empezar = 0;

		for (int i = 0; i < (snake.size()); i++) {
			if (i > 0) {
				serX = serX + " " + String.valueOf(snake.get(i).getX());
				serY = serY + " " + String.valueOf(snake.get(i).getY());
			} else {
				serX = String.valueOf(snake.get(i).getX());
				serY = String.valueOf(snake.get(i).getY());
			}
		}

		appleX = String.valueOf(apple.getX());
		appleY = String.valueOf(apple.getY());

		// la cabeza no se pasa porque head2 = snake2[0]

		puntaje = String.valueOf(score);
		nivel = String.valueOf(level);
		if (dx != 0 || dy != 0) empezar = 1;

		return serX + "/" + serY + "/" + appleX + "/" + appleY + "/" + puntaje + "/" + nivel + "/" + empezar;
	}

	public void Reconvertir(String Entrada) {
		snake2 = new ArrayList<Entity>();
		apple2 = new Entity(SIZE);
		String[] datos = Entrada.split("/");
		String serX = datos[0];
		String serY = datos[1];
		String appleX = datos[2];
		String appleY = datos[3];
		String puntaje = datos[4];
		String nivel = datos[5];
		String empezar = datos[6];

		serX.trim();
		serY.trim();

		String[] serpienteX = serX.split(" ");
		String[] serpienteY = serY.split(" ");

		for (int i = 0; i < serpienteX.length; i++) {
			snake2.add(new Entity(SIZE));
			snake2.get(i).setX(Integer.valueOf(serpienteX[i]));
			snake2.get(i).setY(Integer.valueOf(serpienteY[i]));
		}

		apple2.setX(Integer.valueOf(appleX));
		apple2.setY(Integer.valueOf(appleY));
		head2 = snake2.get(0);
		score2 = Integer.valueOf(puntaje);
		level2 = Integer.valueOf(nivel);
		if (Integer.valueOf(empezar) == 1) start2 = true;
		else start2 = false;
	}
}
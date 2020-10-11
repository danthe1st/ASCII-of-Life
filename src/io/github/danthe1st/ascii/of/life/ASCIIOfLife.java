package io.github.danthe1st.ascii.of.life;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ASCIIOfLife {
	private static final int HEIGHT = 18;
	private static final Graphics2D calcG2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
	private static Font asciiFont = new Font("Arial", Font.PLAIN, HEIGHT);
	private static final FontMetrics fm = calcG2d.getFontMetrics(asciiFont);

	private static final int DEFAULT_WAIT_TIME = 500;

	private static int waitTime = -1;

	public static void main(String[] args) throws IOException, InterruptedException {
		int height = -1;
		int width = -1;
		if (System.console() != null && !System.getProperty("os.name").startsWith("Windows")) {
			String[] cmd = { "/bin/sh", "-c", "stty -icanon </dev/tty" };
			Runtime.getRuntime().exec(cmd).waitFor();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					Runtime.getRuntime().exec("tput cols").getInputStream(), StandardCharsets.UTF_8))) {
				width = Integer.parseInt(br.readLine());
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					Runtime.getRuntime().exec("tput lines").getInputStream(), StandardCharsets.UTF_8))) {
				height = Integer.parseInt(br.readLine());
			}
		} else {
			System.err.println("Run this program using a linux console for the best experience.");
			@SuppressWarnings("resource") //I need stdin after that
			Scanner scan = new Scanner(System.in);
			while (height < 2 || width < 2) {
				try {
					System.err.println(
							"Please enter the number of rows (should not be more then the terminal line count)");//TODO min 2, catch exceptons
					height = scan.nextInt();
					System.err.println();
					System.err.println(
							"Please enter the number of columns (should not be more then the terminal column count)");
					width = scan.nextInt();
					System.err.println();
				} catch (InputMismatchException e) {
					scan.nextLine();
				}
			}
			;
		}

		Simulation sim = new Simulation(width, height);
		Lock genThreadLock = new ReentrantLock();
		Condition genThreadWakeup = genThreadLock.newCondition();

		Thread generationThread = new Thread(() -> genLoop(sim, genThreadLock, genThreadWakeup));
		generationThread.setDaemon(true);
		generationThread.start();
		inputLoop(sim, genThreadLock, genThreadWakeup);
	}

	private static void genLoop(Simulation sim, Lock wakeupLock, Condition wakeupCond) {
		try {
			while (!Thread.currentThread().isInterrupted()) {

				try {
					wakeupLock.lock();
					if (waitTime == -1) {
						wakeupCond.await();
					}
				} finally {
					wakeupLock.unlock();
				}

				generation(sim);
				Thread.sleep(waitTime);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	private static void showHelp() {
		printTextASCIIArt("ASCII of Life");
		System.out.println("You can control this program with the following keys:");
		System.out.println("space\tstart/stop the automatic simulation");
		System.out.println("l\tshow the current game");
		System.out.println("n\tmove one generation forward");
		System.out.println("r\trandomize the game");
		System.out.println("c\tclear the game field");
		System.out.println("q\texit");
		System.out.println("wasd\tmove the cursor");
		System.out.println("u\tchange the state of the cell the cursor is pointing to");
		System.out.println("t\tload a template");
	}

	private static void inputLoop(Simulation sim, Lock genWakeupLock, Condition genWakeupCond) throws IOException {
		Random rand = new Random();
		sim.randomize(rand);
		int cursorX = 0;
		int cursorY = sim.getHeight() - 1;
		showHelp();
		while (!Thread.currentThread().isInterrupted()) {
			int ch = System.in.read();
			switch (ch) {
			case ' ':
				if (waitTime == -1) {//TODO create display
					waitTime = DEFAULT_WAIT_TIME;
					try {
						genWakeupLock.lock();
						genWakeupCond.signal();
					} finally {
						genWakeupLock.unlock();
					}
				} else {
					waitTime = -1;
				}
				break;

			case '?':
				showHelp();
				break;
			case 'l':
				display(sim, cursorX, cursorY);
				break;
			case 'n':
				generation(sim);
				break;
			case 'r':
				sim.randomize(rand);
				break;
			case 'c':
				sim.clear();
				break;
			case 'q':
				Thread.currentThread().interrupt();
				break;
			case 'w':
				cursorY--;
				if (cursorY == -1) {
					cursorY = sim.getHeight() - 1;
				}
				display(sim, cursorX, cursorY);
				break;
			case 's':
				cursorY++;
				if (cursorY == sim.getHeight()) {
					cursorY = 0;
				}
				display(sim, cursorX, cursorY);
				break;
			case 'a':
				cursorX--;
				if (cursorX == -1) {
					cursorX = sim.getWidth() - 1;
				}
				display(sim, cursorX, cursorY);
				break;
			case 'd':
				cursorX++;
				if (cursorX == sim.getWidth()) {
					cursorX = 0;
				}
				display(sim, cursorX, cursorY);
				break;
			case 'u':
				sim.changeCell(cursorX, cursorY);
				display(sim, cursorX, cursorY);
				break;
			case 't':
				loadTemplate(sim, cursorX, cursorY);
				break;
			default:
				if (ch == '\n' || ch == '\r' || ch == '\0') {
					//ignore
				} else {
					System.out.println("Unknown command. type '?' in order to get help." + ch);
				}

			}
		}
	}

	private static void loadTemplate(Simulation sim, int cursorX, int cursorY) throws IOException {
		System.out.println("Select the template to load. Press the associated number in order to load a template");
		System.out.println("Available templates: ");
		System.out.println("1\tblinker");
		System.out.println("2\tr-Pentomino");
		System.out.println("3\tSelf-Destructor");
		System.out.println("4\t42");
		System.out.println("5\tglider");
		System.out.println("6\tGosper glider gun");
		System.out.println("0\texit this menu");
		int ch = -1;
		while ((ch = System.in.read()) < '0' || ch > '6') {
			if (ch != '\n' && ch != '\r' && ch != '\0') {
				System.err.println("\nWrong input - please try again");
			}
		}
		if (ch == '0') {
			return;
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("templates/" + (char) ch),
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				int x = cursorX;
				for (int i = 0; i < line.length(); i++) {
					x = (x + 1) % sim.getWidth();
					switch (line.charAt(i)) {
					case '#':
						sim.setCell(x, cursorY, true);
						break;
					case '-':
						sim.setCell(x, cursorY, false);
						break;
					default:
						//do nothing
					}
				}
				cursorY++;
			}
		}
		display(sim, cursorX, cursorY);
	}

	private static void generation(Simulation sim) {
		sim.nextRound();
		display(sim, -1, -1);
	}

	private static void display(Simulation sim, int cursorX, int cursorY) {
		System.out.println();
		for (int y = 0; y < sim.getHeight(); y++) {
			for (int x = 0; x < sim.getWidth(); x++) {
				if (x == cursorX && y == cursorY) {
					System.out.print(sim.isAlive(x, y) ? '*' : 'o');
				} else {
					System.out.print(sim.isAlive(x, y) ? '#' : ' ');
				}
			}
			System.out.println();
		}
	}

	private static void printTextASCIIArt(String text) {

		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		try {
			g2d.setBackground(Color.WHITE);
			g2d.clearRect(0, 0, width, height);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			g2d.setColor(Color.BLACK);
			g2d.drawString(text, 0, fm.getAscent() - fm.getDescent());
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					System.err.print(img.getRGB(j, i) == Color.WHITE.getRGB() ? " " : "*");
				}
				System.err.println();
			}
		} finally {
			g2d.dispose();
		}
	}
}

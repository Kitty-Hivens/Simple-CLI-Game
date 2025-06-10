import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Main {
    public static char[][] playerArea = new char[20][40]; // уменьшил для удобства

    static List<Enemy> enemies = new ArrayList<>();
    static Random random = new Random();

    static {
        for (char[] chars : playerArea) {
            Arrays.fill(chars, ' ');
        }
    }

    public static int playerLocationX = playerArea[0].length / 2;
    public static int playerLocationY = playerArea.length / 2;

    public static int playerHP = 5;
    public static int turnCount = 0;

    public static void main(String[] args) throws IOException {
        generateEnemies(8);

        Terminal terminal = TerminalBuilder.terminal();
        terminal.enterRawMode();

        updatePlayerPosition();

        gameLoop:
        while (true) {
            render();

            if (playerHP <= 0) {
                showGameOver();
                break;
            }

            int keyCode = terminal.reader().read();
            char key = (char) keyCode;

            int prevX = playerLocationX;
            int prevY = playerLocationY;
            boolean playerMoved = false; // Флаг для отслеживания реального движения

            switch (key) {
                case 'w', 'ц':
                    if (playerLocationY > 0) {
                        playerLocationY--;
                        playerMoved = true;
                    }
                    break;
                case 's', 'ы':
                    if (playerLocationY < playerArea.length - 1) {
                        playerLocationY++;
                        playerMoved = true;
                    }
                    break;
                case 'a', 'ф':
                    if (playerLocationX > 0) {
                        playerLocationX--;
                        playerMoved = true;
                    }
                    break;
                case 'd', 'в':
                    if (playerLocationX < playerArea[0].length - 1) {
                        playerLocationX++;
                        playerMoved = true;
                    }
                    break;
                case 'q', 'й':
                    break gameLoop;
                default:
                    continue;
            }

            // Проверка на столкновение с врагом только если игрок реально двинулся
            if (playerMoved) {
                Enemy collidedEnemy = getEnemyAt(playerLocationX, playerLocationY);
                if (collidedEnemy != null) {
                    playerHP--;
                    enemies.remove(collidedEnemy);
                    playerLocationX = prevX;
                    playerLocationY = prevY;
                } else {
                    updatePlayerPosition();
                }

                // Счётчик ходов увеличивается только при реальном движении
                turnCount++;

                // Враги двигаются раз в два хода
                if (turnCount % 2 == 0) {
                    moveEnemies();
                }

                // Новые враги появляются каждые 5 ходов
                if (turnCount % 5 == 0) {
                    spawnEnemy();
                }
            }
            // Если игрок не двинулся (упёрся в стену), ход не засчитывается
        }
    }

    public static void generateEnemies(int count) {
        enemies.clear();
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = random.nextInt(playerArea[0].length);
                y = random.nextInt(playerArea.length);
            } while ((x == playerLocationX && y == playerLocationY) || isEnemyAt(x, y));
            enemies.add(new Enemy(x, y));
        }
    }

    public static Enemy getEnemyAt(int x, int y) {
        for (Enemy e : enemies) {
            if (e.x == x && e.y == y) {
                return e;
            }
        }
        return null;
    }

    public static boolean isEnemyAt(int x, int y) {
        return getEnemyAt(x, y) != null;
    }

    public static void updatePlayerPosition() {
        // Чистим прошлую позицию игрока
        for (int y = 0; y < playerArea.length; y++) {
            for (int x = 0; x < playerArea[y].length; x++) {
                if (playerArea[y][x] == 'P') {
                    playerArea[y][x] = ' ';
                }
            }
        }
        playerArea[playerLocationY][playerLocationX] = 'P';
    }

    public static void moveEnemies() {
        for (Enemy e : enemies) {
            // Если враг рядом с игроком (Манхэттен ≤ 5), то пытается идти к игроку
            int dist = Math.abs(e.x - playerLocationX) + Math.abs(e.y - playerLocationY);
            if (dist <= 5) {
                // Выбираем направление, максимально приближающее к игроку
                int dx = Integer.compare(playerLocationX, e.x);
                int dy = Integer.compare(playerLocationY, e.y);

                // Пытаемся передвинуться по X, если возможно
                if (dx != 0 && canMoveTo(e.x + dx, e.y)) {
                    e.x += dx;
                }
                // Иначе пытаемся передвинуться по Y
                else if (dy != 0 && canMoveTo(e.x, e.y + dy)) {
                    e.y += dy;
                }
                // Иначе враг стоит на месте
            }
        }

        // После движения врагов проверяем столкновения с игроком
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy e = iterator.next();
            if (e.x == playerLocationX && e.y == playerLocationY) {
                playerHP--;
                iterator.remove();
            }
        }
    }

    // Проверка, можно ли встать на клетку (не занята ли игроком или врагом)
    public static boolean canMoveTo(int x, int y) {
        if (x < 0 || x >= playerArea[0].length || y < 0 || y >= playerArea.length) {
            return false;
        }
        if (playerLocationX == x && playerLocationY == y) {
            return false;
        }
        return !isEnemyAt(x, y);
    }

    public static void render() {
        clearConsole();

        // Отрисовка карты с игроком и врагами
        for (int y = 0; y < playerArea.length; y++) {
            for (int x = 0; x < playerArea[y].length; x++) {
                if (x == playerLocationX && y == playerLocationY) {
                    System.out.print('P' + " ");
                } else {
                    Enemy e = getEnemyAt(x, y);
                    if (e != null) {
                        System.out.print(e.symbol + " ");
                    } else {
                        System.out.print(' ' + " ");
                    }
                }
            }

            // Отрисовка мини-GUI справа
            if (y == 1) {
                System.out.print("   HP: ");
                for (int i = 0; i < playerHP; i++) {
                    System.out.print("♥ ");
                }
                for (int i = playerHP; i < 5; i++) {
                    System.out.print("♡ ");
                }
            }
            if (y == 3) {
                System.out.print("   Turns: " + turnCount);
            }
            System.out.println();
        }
        System.out.println("\nUse WASD (или цфыв) для движения, q (й) для выхода");
    }

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void showGameOver() {
        clearConsole();
        System.out.println("Игра закончена!");
        System.out.println("Ваш результат: " + turnCount + " ходов");
        System.out.println("Нажмите любую клавишу для выхода...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void spawnEnemy() {
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(playerArea[0].length);
            int y = random.nextInt(playerArea.length);

            int dist = Math.abs(x - playerLocationX) + Math.abs(y - playerLocationY);
            if (dist >= 3 && !isEnemyAt(x, y) && (x != playerLocationX || y != playerLocationY)) {
                enemies.add(new Enemy(x, y));
                break;
            }
        }
    }
}

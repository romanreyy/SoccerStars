package main;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

	// Modificar dimensiones
    private final int WIDTH = 1000;          // Aumentamos ancho
    private final int HEIGHT = 600;          // Aumentamos alto
    private final int HEADER_HEIGHT = 80;    // Espacio para marcador y nombres
    private final int FIELD_HEIGHT = HEIGHT - HEADER_HEIGHT; // Altura real del campo
    private boolean dragging = false;
    private Player selectedPlayer;
    private Point dragStart;
    private Point dragCurrent;
    private final int MAX_DRAG_LENGTH = 100;
    private boolean isSpinEnabled = false; // Variable para controlar si el efecto está activado
    private double spinAngle = 0.0;        // Ángulo para el efecto
    
    private Player player1;
    private Player player2;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;
    private Timer timer;
    private boolean isPlayer1Turn = true; // Control de turnos
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean canShoot = true;     

    
    // Constructor modificado
    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(34, 139, 34)); // Verde oscuro para el campo
        
        // Ajustar posiciones iniciales considerando el nuevo espacio
        player1 = new Player(120, HEADER_HEIGHT + (FIELD_HEIGHT / 2));
        player2 = new Player(WIDTH - 160, HEADER_HEIGHT + (FIELD_HEIGHT / 2));
        
        // Ajustar posición de los arcos
        leftGoal = new Goal(0, HEADER_HEIGHT + (FIELD_HEIGHT/2) - 60, true);
        rightGoal = new Goal(WIDTH - 20, HEADER_HEIGHT + (FIELD_HEIGHT/2) - 60, false);
        
        // Ajustar posición inicial de la pelota
        ball = new Ball(WIDTH / 2, HEADER_HEIGHT + (FIELD_HEIGHT / 2));
        
        timer = new Timer(16, this);
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public void startGame() {
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        player1.update();
        player2.update();
        ball.update();

        if (!dragging) {
            player1.move();
            player2.move();
        }

        checkCollisions();
        checkGoals();
        
        // Verificar si todos los objetos están quietos para permitir el siguiente turno
        if (!canShoot && isObjectsStatic()) {
            canShoot = true;
            isPlayer1Turn = !isPlayer1Turn; // Cambiar turno
        }
    }
    
    private boolean isObjectsStatic() {
        return player1.isStatic() && 
               player2.isStatic() && 
               ball.isStatic();
    }
    
    private void checkGoals() {
        if (leftGoal.checkGoal(ball)) {
            player2Score++;
            resetPositions();
        } else if (rightGoal.checkGoal(ball)) {
            player1Score++;
            resetPositions();
        }
    }
    
    // Modificar el método resetPositions
    private void resetPositions() {
        ball.setPosition(WIDTH / 2, HEADER_HEIGHT + (FIELD_HEIGHT / 2));
        ball.setVelocity(0, 0);
        player1.reset(120, HEADER_HEIGHT + (FIELD_HEIGHT / 2));
        player2.reset(WIDTH - 160, HEADER_HEIGHT + (FIELD_HEIGHT / 2));
        canShoot = true;
    }

    // Modificar el método de pintado
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Dibujar header
        drawHeader(g);
        
        // Dibujar línea que separa el header del campo
        g.setColor(Color.WHITE);
        g.drawLine(0, HEADER_HEIGHT, WIDTH, HEADER_HEIGHT);
        
        // Dibujar campo
        drawField(g);
        
        // Dibujar jugadores y pelota
        player1.draw(g);
        player2.draw(g);
        ball.draw(g);
        
        // Dibujar arcos
        leftGoal.draw(g);
        rightGoal.draw(g);
        
        // Dibujar línea de dirección si estamos arrastrando
        if (dragging && selectedPlayer != null && dragStart != null && dragCurrent != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            // Configurar el renderizado para líneas más suaves
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(2));
            
            // Calcular el centro del jugador
            int centerX = selectedPlayer.getX() + selectedPlayer.getDiameter() / 2;
            int centerY = selectedPlayer.getY() + selectedPlayer.getDiameter() / 2;
            
            // Calcular el vector de dirección
            double dx = dragCurrent.x - dragStart.x;
            double dy = dragCurrent.y - dragStart.y;
            
            // Calcular la magnitud
            double magnitude = Math.sqrt(dx * dx + dy * dy);
            
            // Limitar la longitud de la línea
            if (magnitude > MAX_DRAG_LENGTH) {
                dx = (dx / magnitude) * MAX_DRAG_LENGTH;
                dy = (dy / magnitude) * MAX_DRAG_LENGTH;
            }
            
            // Calcular el punto final
            int endX = centerX - (int)dx;
            int endY = centerY - (int)dy;
            
            // Dibujar línea principal
            g2d.setColor(Color.RED);
            g2d.drawLine(centerX, centerY, endX, endY);
           
            
            // Dibujar indicador de fuerza
            drawForceIndicator(g2d, magnitude);
            
            // Dibujar punta de flecha
            drawArrowHead(g2d, centerX, centerY, endX, endY);
        }
    }
    
 // Nuevo método para dibujar el header
    private void drawHeader(Graphics g) {
        // Fondo del header
        g.setColor(new Color(48, 48, 48)); // Gris oscuro
        g.fillRect(0, 0, WIDTH, HEADER_HEIGHT);
        
        // Dibujar nombres de equipo
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Equipo 1", 50, 30);
        g.drawString("Equipo 2", WIDTH - 150, 30);
        
        // Dibujar marcador
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String score = player1Score + " - " + player2Score;
        FontMetrics fm = g.getFontMetrics();
        int scoreWidth = fm.stringWidth(score);
        g.drawString(score, (WIDTH - scoreWidth) / 2, 45);
        
        // Dibujar indicador de turno
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String turnText = "Turno: " + (isPlayer1Turn ? "Equipo 1" : "Equipo 2");
        g.drawString(turnText, (WIDTH - fm.stringWidth(turnText)) / 2, 70);
    }

 // Modificar el método de dibujo del campo
    private void drawField(Graphics g) {
        // Dibujar círculo central
        g.setColor(Color.WHITE);
        int circleDiameter = 120;
        g.drawOval(WIDTH/2 - circleDiameter/2, 
                   HEADER_HEIGHT + (FIELD_HEIGHT/2) - circleDiameter/2, 
                   circleDiameter, 
                   circleDiameter);
        
        // Línea central
        g.drawLine(WIDTH/2, HEADER_HEIGHT, WIDTH/2, HEIGHT);
        
        // Áreas de los arcos (opcional)
        int areaWidth = 150;
        int areaHeight = 300;
        // Área izquierda
        g.drawRect(0, HEADER_HEIGHT + (FIELD_HEIGHT - areaHeight)/2, areaWidth, areaHeight);
        // Área derecha
        g.drawRect(WIDTH - areaWidth, HEADER_HEIGHT + (FIELD_HEIGHT - areaHeight)/2, areaWidth, areaHeight);
    }
    
    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString(player1Score + " - " + player2Score, WIDTH/2 - 40, 30);
    }
    
    private void drawTurnIndicator(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String turnText = "Turno: " + (isPlayer1Turn ? "Jugador 1" : "Jugador 2");
        g.drawString(turnText, WIDTH/2 - 60, 60);
    }
    
    private void drawForceIndicator(Graphics2D g2d, double magnitude) {
        if (selectedPlayer != null) {
            int centerX = selectedPlayer.getX() + selectedPlayer.getDiameter() / 2;
            int barWidth = 50;
            int barHeight = 5;
            int barX = centerX - barWidth / 2;
            int barY = selectedPlayer.getY() - 15;
            
            // Calcular porcentaje de fuerza
            double forcePct = Math.min(magnitude / MAX_DRAG_LENGTH, 1.0);
            
            // Dibujar barra de fuerza
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(barX, barY, barWidth, barHeight);
            
            // Dibujar progreso
            g2d.setColor(new Color(255, (int)(255 * (1 - forcePct)), 0));
            g2d.fillRect(barX, barY, (int)(barWidth * forcePct), barHeight);
        }
    }

    private void drawArrowHead(Graphics2D g2d, int startX, int startY, int endX, int endY) {
        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowSize = 10;
        
        Point2D end = new Point2D.Double(endX, endY);
        Point2D tip1 = new Point2D.Double(
            endX - arrowSize * Math.cos(angle - Math.PI/6),
            endY - arrowSize * Math.sin(angle - Math.PI/6)
        );
        Point2D tip2 = new Point2D.Double(
            endX - arrowSize * Math.cos(angle + Math.PI/6),
            endY - arrowSize * Math.sin(angle + Math.PI/6)
        );
        
        g2d.drawLine((int)end.getX(), (int)end.getY(), (int)tip1.getX(), (int)tip1.getY());
        g2d.drawLine((int)end.getX(), (int)end.getY(), (int)tip2.getX(), (int)tip2.getY());
    }

    private void checkCollisions() {
        // Colisiones entre jugadores y pelota
        if (player1.collidesWith(ball)) {
            player1.handleCollision(ball);
        }
        if (player2.collidesWith(ball)) {
            player2.handleCollision(ball);
        }

        // Colisiones entre jugadores
        if (player1.collidesWithPlayer(player2)) {
            handlePlayerCollision(player1, player2);
        }

        // Colisiones con los bordes para los jugadores y la pelota
        checkWallCollisions();
    }

    // Método para manejar colisiones entre jugadores
    private void handlePlayerCollision(Player p1, Player p2) {
        // Calcular centros
        double p1CenterX = p1.getX() + p1.getDiameter() / 2.0;
        double p1CenterY = p1.getY() + p1.getDiameter() / 2.0;
        double p2CenterX = p2.getX() + p2.getDiameter() / 2.0;
        double p2CenterY = p2.getY() + p2.getDiameter() / 2.0;

        // Calcular vector de colisión
        double dx = p2CenterX - p1CenterX;
        double dy = p2CenterY - p1CenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) return; // Evitar división por cero

        // Normalizar el vector de colisión
        dx /= distance;
        dy /= distance;

        // Calcular velocidades relativas
        double relativeVelX = p2.getVelX() - p1.getVelX();
        double relativeVelY = p2.getVelY() - p1.getVelY();

        // Calcular producto punto
        double dotProduct = relativeVelX * dx + relativeVelY * dy;

        // Factor de elasticidad para el rebote entre jugadores
        double elasticity = 0.8;

        // Calcular impulso
        double impulseMagnitude = -(1 + elasticity) * dotProduct;

        // Aplicar impulso a ambos jugadores
        p1.setVelocity(
            p1.getVelX() - dx * impulseMagnitude,
            p1.getVelY() - dy * impulseMagnitude
        );
        p2.setVelocity(
            p2.getVelX() + dx * impulseMagnitude,
            p2.getVelY() + dy * impulseMagnitude
        );

        // Separar los jugadores para evitar que se peguen
        double overlap = (p1.getDiameter() + p2.getDiameter()) / 2 - distance;
        if (overlap > 0) {
            p1.setPosition(
                p1.getX() - dx * overlap / 2,
                p1.getY() - dy * overlap / 2
            );
            p2.setPosition(
                p2.getX() + dx * overlap / 2,
                p2.getY() + dy * overlap / 2
            );
        }
    }

    // Método actualizado para las colisiones con paredes
    private void checkWallCollisions() {
        // Colisiones para la pelota
        if (ball.getY() < HEADER_HEIGHT) {
            ball.setPosition(ball.getX(), HEADER_HEIGHT);
            ball.setVelocity(ball.getVelX(), -ball.getVelY());
        }
        if (ball.getY() + ball.getDiameter() > HEIGHT) {
            ball.setPosition(ball.getX(), HEIGHT - ball.getDiameter());
            ball.setVelocity(ball.getVelX(), -ball.getVelY());
        }
        if (ball.getX() < 0) {
            ball.setPosition(0, ball.getY());
            ball.setVelocity(-ball.getVelX(), ball.getVelY());
        }
        if (ball.getX() + ball.getDiameter() > WIDTH) {
            ball.setPosition(WIDTH - ball.getDiameter(), ball.getY());
            ball.setVelocity(-ball.getVelX(), ball.getVelY());
        }

        // Colisiones para player1
        if (player1.getY() < HEADER_HEIGHT) {
            player1.setPosition(player1.getX(), HEADER_HEIGHT);
            player1.setVelocity(player1.getVelX(), -player1.getVelY() * 0.7);
        }
        if (player1.getY() + player1.getDiameter() > HEIGHT) {
            player1.setPosition(player1.getX(), HEIGHT - player1.getDiameter());
            player1.setVelocity(player1.getVelX(), -player1.getVelY() * 0.7);
        }
        if (player1.getX() < 0) {
            player1.setPosition(0, player1.getY());
            player1.setVelocity(-player1.getVelX() * 0.7, player1.getVelY());
        }
        if (player1.getX() + player1.getDiameter() > WIDTH) {
            player1.setPosition(WIDTH - player1.getDiameter(), player1.getY());
            player1.setVelocity(-player1.getVelX() * 0.7, player1.getVelY());
        }

        // Colisiones para player2 (mismo patrón que player1)
        if (player2.getY() < HEADER_HEIGHT) {
            player2.setPosition(player2.getX(), HEADER_HEIGHT);
            player2.setVelocity(player2.getVelX(), -player2.getVelY() * 0.7);
        }
        if (player2.getY() + player2.getDiameter() > HEIGHT) {
            player2.setPosition(player2.getX(), HEIGHT - player2.getDiameter());
            player2.setVelocity(player2.getVelX(), -player2.getVelY() * 0.7);
        }
        if (player2.getX() < 0) {
            player2.setPosition(0, player2.getY());
            player2.setVelocity(-player2.getVelX() * 0.7, player2.getVelY());
        }
        if (player2.getX() + player2.getDiameter() > WIDTH) {
            player2.setPosition(WIDTH - player2.getDiameter(), player2.getY());
            player2.setVelocity(-player2.getVelX() * 0.7, player2.getVelY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!canShoot) return; // Evitar disparos mientras los objetos están en movimiento
        
        Point clickPoint = e.getPoint();
        if (isPlayer1Turn && player1.contains(clickPoint)) {
            selectedPlayer = player1;
            dragStart = clickPoint;
            dragCurrent = clickPoint;
            dragging = true;
        } else if (!isPlayer1Turn && player2.contains(clickPoint)) {
            selectedPlayer = player2;
            dragStart = clickPoint;
            dragCurrent = clickPoint;
            dragging = true;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging && selectedPlayer != null) {
            dragCurrent = e.getPoint();
            
            // Calcular ángulo para el efecto
            if (isSpinEnabled && dragStart != null) {
                double dx = e.getX() - dragStart.x;
                double dy = e.getY() - dragStart.y;
                spinAngle = Math.atan2(dy, dx);
            }
            
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging && selectedPlayer != null && dragStart != null) {
            dragCurrent = e.getPoint();
            
            // Calcular vector de disparo
            double dx = dragCurrent.x - dragStart.x;
            double dy = dragCurrent.y - dragStart.y;
            
            // Aplicar el disparo
            selectedPlayer.shoot(dx, dy);
            
            // Aplicar efecto si está activado
            if (isSpinEnabled) {
                selectedPlayer.addSpinToBall(ball, spinAngle);
            }
            
            canShoot = false; // Deshabilitar disparos hasta que todo esté estático
            selectedPlayer = null;
            dragStart = null;
            dragCurrent = null;
            dragging = false;
            repaint();

        }
    }
    
    

    // Método para activar/desactivar el modo de efecto
    public void toggleSpinMode() {
        isSpinEnabled = !isSpinEnabled;
    }

    // Métodos adicionales del MouseListener y MouseMotionListener
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
}
package org.hack.h1;


import static com.codename1.ui.CN.*;

import com.codename1.charts.util.ColorUtil;
import com.codename1.ui.*;
import com.codename1.ui.geom.Point;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;

import java.io.IOException;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.io.NetworkEvent;
import com.codename1.ui.util.UITimer;

public class AppMain {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        Log.bindCrashProtection(true);

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            if(err.getError() != null) {
                Log.e(err.getError());
            }
            Log.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });        
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        new Game();
    }

    public void stop() {
        current = getCurrentForm();
        if(current instanceof Dialog) {
            ((Dialog)current).dispose();
            current = getCurrentForm();
        }
    }
    
    public void destroy() {
    }

}

class Game extends Form implements Runnable {
    private GameWorld gw;

    // Constructor
    public Game() {
        this.gw = new GameWorld();

        addKeyListener('Q', (evt) -> gw.quit());
        addKeyListener(-93, (evt) -> gw.left());
        addKeyListener(-94, (evt) -> gw.right());
        addKeyListener(-91, (evt) -> gw.up());
        addKeyListener(-92, (evt) -> gw.down());

        UITimer timer = new UITimer(this);
        timer.schedule(100, true, this);

        this.getAllStyles().setBgColor(ColorUtil.BLACK);

        this.show();
    }

    @Override
    public void run() {
        this.gw.tick();
        repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);
        gw.draw(g);
    }
}

class GameWorld {
    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;
    private Player p;

    // Constructor
    public GameWorld() {
        this.DISPLAY_WIDTH = Display.getInstance().getDisplayWidth();
        this.DISPLAY_HEIGHT = Display.getInstance().getDisplayHeight();
        this.p = new Player();
    }

    // Restart of the GameWorld Object
    // new GameWorld() === init()
    public void init() {
        this.p = new Player();
    }

    public void tick() {
        updateDisplay();
    }

    public void left() {
        this.p.updateAngleMultiplier(-1);
    }

    public void right() {
        this.p.updateAngleMultiplier(1);
    }

    public void up() {
        this.p.move(64);
    }

    public void down() {
        this.p.move(-64);
    }

    public void draw(Graphics g) {
        g.clearRect(0, 0, this.DISPLAY_HEIGHT, this.DISPLAY_WIDTH);
        this.p.draw(g);
    }

    private void updateDisplay() {
        this.DISPLAY_WIDTH = Display.getInstance().getDisplayWidth();
        this.DISPLAY_HEIGHT = Display.getInstance().getDisplayHeight();
    }

    public void quit() {
        Display.getInstance().exitApplication();
    }
}

class Player {
    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;
    private Point position;
    private Point center;
    private int playerBoundingWidth;
    private int playerBoundingHeight;
    private int angleMultiplier;
    private double angle;

    // Constructor
    public Player() {
        // Creating our object variables
        this.DISPLAY_WIDTH = Display.getInstance().getDisplayWidth();
        this.DISPLAY_HEIGHT = Display.getInstance().getDisplayHeight();
        this.center = new Point(
                // Center with respect to X
                this.DISPLAY_WIDTH / 2,
                // In Zone 15
                this.DISPLAY_HEIGHT - (this.DISPLAY_HEIGHT / 16)
        );
        this.position = new Point(
                // Center with respect to X
                this.DISPLAY_WIDTH / 2,
                // In Zone 15
                this.DISPLAY_HEIGHT - (this.DISPLAY_HEIGHT / 16)
        );
        this.playerBoundingWidth = 64;
        this.playerBoundingHeight = 64;
        this.position = this.translate(this.position);
        this.angleMultiplier = 18;
        updateAngle();
    }

    public void draw(Graphics g) {
        // update display dimensions
        updateDisplay();

        // Calc values for fill arc function
        int boundingX = this.position.getX();
        int boundingY = this.position.getY();
        int width = this.playerBoundingWidth;
        int height = this.playerBoundingHeight;

        // Calc values for draw line function
        int circleCenterX = this.center.getX();
        int circleCenterY = this.center.getY();
        int radius = this.playerBoundingWidth / 2;
        int lineLength = 2000;
        double circleOuterX =
                circleCenterX + lineLength * (Math.cos(this.angle) / radius);
        double circleOuterY =
                circleCenterY + lineLength * (Math.sin(this.angle) / radius);

        // Set color for current player object
        g.setColor(ColorUtil.YELLOW);

        // Fill Arc Function
        g.fillArc(
                boundingX,
                boundingY,
                width,
                height,
                0,
                360
        );

        // Set color for current player object
        g.setColor(ColorUtil.MAGENTA);

        // Drow line Function
        g.drawLine(
                (int)circleOuterX,
                (int)circleOuterY,
                circleCenterX,
                circleCenterY
        );
    }

    public void updateAngleMultiplier(int value) {
        switch (value) {
            case 1:
                if ((this.angleMultiplier + value) == 25) {
                    this.angleMultiplier = 1;
                } else {
                    this.angleMultiplier++;
                }
                break;
            case -1:
                if ((this.angleMultiplier + value) == 0) {
                    this.angleMultiplier = 24;
                } else {
                    this.angleMultiplier--;
                }
                break;
        }
        updateAngle();
    }

    private void updateAngle() {
        this.angle = Math.toRadians(15 * this.angleMultiplier);
    }

    private void updateDisplay() {
        this.DISPLAY_WIDTH = Display.getInstance().getDisplayWidth();
        this.DISPLAY_HEIGHT = Display.getInstance().getDisplayHeight();
    }

    public void move(int v) {
        this.center.setX(
                (int)(this.center.getX() + (Math.cos(this.angle) * v))
        );
        this.center.setY(
                (int)(this.center.getY() + (Math.sin(this.angle) * v))
        );
        this.position.setX(this.center.getX());
        this.position.setY(this.center.getY());
        this.position = translate(this.position);
    }

    private Point translate(Point p) {
        // x moves half our player size to the Left
        p.setX(p.getX() - (this.playerBoundingWidth / 2));
        // y moves half our player size to the up
        p.setY(p.getY() - (this.playerBoundingHeight / 2));

        return p;
    }
}

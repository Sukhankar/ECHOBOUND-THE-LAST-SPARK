package com.echobound.core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Window {
    public static final int INTERNAL_WIDTH = 640;
    public static final int INTERNAL_HEIGHT = 360;

    private final JFrame frame;
    private final GamePanel panel;

    // Two off-screen buffers ping-ponged between the game thread (which renders into
    // whichever one is currently "back") and the Swing EDT (which only ever paints
    // whichever one was last fully finished — "front"). Previously there was a single
    // shared BufferedImage: the game thread kept mutating it via bufferGraphics while the
    // EDT concurrently read the very same object in paintComponent, with no synchronization
    // at all. That's a data race — the EDT could paint a half-drawn frame (blocks drawn,
    // player/HUD not yet) or torn pixel data, which is exactly what shows up on screen as
    // flickering. frontBuffer is volatile so a completed frame is safely published to the
    // EDT with a single reference write/read, without needing a lock on the hot render path.
    private final BufferedImage bufferA;
    private final BufferedImage bufferB;
    private BufferedImage backBuffer;
    private volatile BufferedImage frontBuffer;
    private Graphics2D bufferGraphics;

    private boolean isFullscreen = false;
    private Rectangle windowedBounds;

    public Window(String title, Input input) {
        bufferA = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        bufferB = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        backBuffer = bufferA;
        frontBuffer = bufferB; // arbitrary until the first present(); both start blank
        bufferGraphics = createGraphicsFor(backBuffer);

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.BLACK);

        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(1280, 720));
        panel.setFocusable(true);
        if (input != null) {
            panel.addKeyListener(input);
        }

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        windowedBounds = frame.getBounds();
    }

    public Window(String title) {
        this(title, null);
    }

    public void addKeyListener(java.awt.event.KeyListener l) {
        panel.addKeyListener(l);
    }

    public void addMouseListener(java.awt.event.MouseListener l) {
        panel.addMouseListener(l);
    }

    public void addMouseMotionListener(java.awt.event.MouseMotionListener l) {
        panel.addMouseMotionListener(l);
    }

    public void addMouseWheelListener(java.awt.event.MouseWheelListener l) {
        panel.addMouseWheelListener(l);
    }

    public JPanel getPanel() {
        return panel;
    }

    public Graphics2D getBufferGraphics() {
        return bufferGraphics;
    }

    private static Graphics2D createGraphicsFor(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    /**
     * Called once per frame after EchoBoundMasterEngine finishes drawing into
     * getBufferGraphics()'s buffer. Publishes that now-complete frame as frontBuffer (what
     * paintComponent will draw) and switches backBuffer to the other image so the very next
     * frame's drawing never touches the buffer the EDT might still be mid-paint on.
     */
    public void present() {
        BufferedImage justDrawn = backBuffer;
        backBuffer = (backBuffer == bufferA) ? bufferB : bufferA;
        bufferGraphics.dispose();
        bufferGraphics = createGraphicsFor(backBuffer);
        frontBuffer = justDrawn;
        panel.repaint();
    }

    public void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        frame.dispose();
        if (isFullscreen) {
            windowedBounds = frame.getBounds();
            frame.setUndecorated(true);
            try {
                if (gd.isFullScreenSupported()) {
                    gd.setFullScreenWindow(frame);
                } else {
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.setVisible(true);
                }
            } catch (Exception e) {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
        } else {
            try {
                if (gd.getFullScreenWindow() == frame) {
                    gd.setFullScreenWindow(null);
                }
            } catch (Exception ignored) {}
            frame.setUndecorated(false);
            frame.setBounds(windowedBounds);
            frame.setVisible(true);
        }
        panel.requestFocusInWindow();
    }

    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int panelW = getWidth();
            int panelH = getHeight();

            if (panelW <= 0 || panelH <= 0) return;

            // Preserve 16:9 aspect ratio with letterboxing
            float targetAspect = (float) INTERNAL_WIDTH / INTERNAL_HEIGHT;
            float currentAspect = (float) panelW / panelH;

            int drawW, drawH, drawX, drawY;

            if (currentAspect > targetAspect) {
                // Window is wider than 16:9 (pillarbox bars on sides)
                drawH = panelH;
                drawW = (int) (panelH * targetAspect);
                drawX = (panelW - drawW) / 2;
                drawY = 0;
            } else {
                // Window is taller than 16:9 (letterbox bars on top/bottom)
                drawW = panelW;
                drawH = (int) (panelW / targetAspect);
                drawX = 0;
                drawY = (panelH - drawH) / 2;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            // Read frontBuffer once into a local: it's volatile and present() can swap it out
            // from the game thread at any time, so re-reading the field mid-method could
            // otherwise mix drawImage's internal calls across two different frames.
            BufferedImage toDraw = frontBuffer;
            g2.drawImage(toDraw, drawX, drawY, drawW, drawH, null);
        }
    }
}

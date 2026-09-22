package com.echobound.core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Window {
    public static final int INTERNAL_WIDTH = 640;
    public static final int INTERNAL_HEIGHT = 360;

    private final JFrame frame;
    private final GamePanel panel;
    private final BufferedImage backBuffer;
    private final Graphics2D bufferGraphics;

    private boolean isFullscreen = false;
    private Rectangle windowedBounds;

    public Window(String title, Input input) {
        backBuffer = new BufferedImage(INTERNAL_WIDTH, INTERNAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = backBuffer.createGraphics();

        // Default high quality render hints for the buffer
        bufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

    public void present() {
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
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(backBuffer, drawX, drawY, drawW, drawH, null);
        }
    }
}

package com.echobound.sandbox;

import com.echobound.core.Window;

import javax.swing.*;

public class SandboxMain {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "true");

        SwingUtilities.invokeLater(() -> {
            Window window = new Window("ECHOBOUND: THE LAST SPARK — PIXEL OPEN-WORLD SANDBOX [PART 1]");
            SandboxGameEngine engine = new SandboxGameEngine(window);

            window.addKeyListener(engine);
            window.addMouseListener(engine);
            window.addMouseMotionListener(engine);
            window.addMouseWheelListener(engine);

            engine.start();
        });
    }
}

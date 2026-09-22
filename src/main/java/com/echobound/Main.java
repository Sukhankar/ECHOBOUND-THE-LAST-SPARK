package com.echobound;

import com.echobound.core.EchoBoundMasterEngine;
import com.echobound.core.Window;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable hardware acceleration hints where available
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "true");

        SwingUtilities.invokeLater(() -> {
            Window window = new Window("ECHOBOUND: THE LAST SPARK — PIXEL OPEN-WORLD ACTION RPG");
            EchoBoundMasterEngine engine = new EchoBoundMasterEngine(window);

            window.addKeyListener(engine);
            window.addMouseListener(engine);
            window.addMouseMotionListener(engine);
            window.addMouseWheelListener(engine);

            engine.start();
        });
    }
}

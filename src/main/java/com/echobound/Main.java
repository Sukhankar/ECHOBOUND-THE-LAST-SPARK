package com.echobound;

import com.echobound.core.GameEngine;
import com.echobound.core.Input;
import com.echobound.core.Window;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable hardware acceleration hints where available
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "true");

        SwingUtilities.invokeLater(() -> {
            Input input = new Input();
            Window window = new Window("ECHOBOUND: THE LAST SPARK — ARCADE EDITION [M1 CORE ENGINE]", input);
            GameEngine engine = new GameEngine(window, input);
            engine.start();
        });
    }
}

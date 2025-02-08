package com.example;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

public class Window {
    static class Options {
        String title = "Untitled Window";
        int width = 640;
        int height = 480;
        boolean closeOnEscape = true;
    }

    private Game game;
    private long windowID;
    private final Options options;

    private long startTime;
    private long lastFrameTime;

    public int width, height;

    public Window(Game game, Options options) {
        this.game = game;
        this.options = options;
    }

    public long getWindowID() {
        return this.windowID;
    }

    /**
     * Create a window and register callbacks.
     */
    public Window create() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        width = options.width;
        height = options.height;
        windowID = createGLFWWindow(width, height, options.title);
        centreWindow();
        setInputCallbacks();
        glfwMakeContextCurrent(windowID);
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(windowID);
        glfwSetFramebufferSizeCallback(windowID, (long win, int w, int h) -> {
            GL11.glViewport(0, 0, w, h);
            game.windowResized(w, h);
            width = w;
            height = h;
        });
        glfwMakeContextCurrent(NULL);
        return this;
    }

    /**
     * Performs the render and update loop.
     */
    public void loop() {
        glfwMakeContextCurrent(windowID);
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        game.init(this);
        startTime = System.currentTimeMillis();
        lastFrameTime = startTime;
        while (!glfwWindowShouldClose(windowID)) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            if (game != null) {
                game.update(currentTime - startTime, deltaTime);
                game.draw();
            }
            glfwSwapBuffers(windowID);
            glfwPollEvents();
        }
    }

    /**
     * Close the window and terminates GLFW
     */
    public void close() {
        game.dispose();
        glfwFreeCallbacks(windowID);
        glfwDestroyWindow(windowID);
        GL.setCapabilities(null);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private long createGLFWWindow(int width, int height, String title) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        long window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        return window;
    }

    private void centreWindow() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(windowID, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    windowID,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2);
        }
    }

    private void setInputCallbacks() {
        glfwSetKeyCallback(windowID, (window, key, scanCode, action, mods) -> {
            handleWindowKeys(key, action);
            if (game != null)
                game.handleKeyPress(key, action);
        });
        glfwSetMouseButtonCallback(windowID, (long win, int button, int action, int mods) -> {
            if (game != null)
                game.handleMouseClick(button, action);
        });
    }

    private void handleWindowKeys(int key, int action) {
        if (options.closeOnEscape)
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(windowID, true);
    }

}
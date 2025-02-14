package com.example;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

public class MyGame extends Game {

    Shader shader = new Shader("vertex.vert", "fragment.frag");

    int buff;
    int textureId;
    float offsetMove;
    Window window;
    float x;
    float y;
    float square2Y;
    float square2X;
    float speed = 1.5f;

    public void init(Window window) {
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f); // Changes background colour. //
        shader.initialize();
        this.window = window;
        square2Y = getRandomFloat(0, window.height - 3.0f);
        square2X = getRandomFloat(0, window.width - 3.0f);

        float[] vertexData = new float[] {
                -1.5f, -1.5f,
                -1.5f, 1.5f,
                1.5f, 1.5f,
                1.5f, -1.5f,
                300, 700, 0.25f, 1, 1,
                100, 400, 1, 1, 0,
                300, 200, 1, 1, 1,
                200, 300, 0.5f, 0.5f, 1,
                600, 200, 0.7f, 1, 0.5f,
                100, 100, 0, 0, 1
        };
        FloatBuffer fb = MemoryUtil.memCallocFloat(38);
        fb.put(0, vertexData); // Clears VertexData //

        buff = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, buff);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        try { // PNGDecoder. Gets the texture and then gets put into a bytebuffer and the data
              // gets reset and then placed into a texture from 0-15. //
            PNGDecoder decoder = new PNGDecoder(MyGame.class.getResourceAsStream("texture.png"));
            ByteBuffer textureDataBuffer = MemoryUtil.memCalloc(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(textureDataBuffer, decoder.getWidth() * 4, Format.RGBA);
            textureDataBuffer.flip();
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, textureDataBuffer);
            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
            MemoryUtil.memFree(textureDataBuffer);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void update(long currentTime, long deltaTime) {
        offsetMove += deltaTime / speed; // float controls the speed of the square. //
        // Converts time to seconds. Doesn't have to be divided by 1000.0 but it'll show
        // a floating point number like 5.0 instead of just 5 if it was 1000. //
        double currentTimeSeconds = currentTime / 1000.0;
        try (MemoryStack stack = stackPush()) {
            DoubleBuffer xbuff = stack.callocDouble(1);
            DoubleBuffer ybuff = stack.callocDouble(1);
            glfwGetCursorPos(window.getWindowID(), xbuff, ybuff);
            x = (float) xbuff.get(0);
            y = (float) -ybuff.get(0) + window.height;

        }

        float square1Size = 10.0f; // Size of the square that follows the cursor. //
        float square2Size = 10.0f; // Size of the square that moves across the screen. //
        float square1X = x;
        float square1Y = y;
        float square2X = offsetMove;
        // checks if the square goes offscreen. //
        if (square2X > window.width) {
            square2X = getRandomFloat(0, window.width - square2Size);
            square2Y = getRandomFloat(0, window.height - square2Size);
            offsetMove = square2X; // Update offsetMove to the new position //
        }
        // This checks if the squares collidded and if so, it simply closes the program
        // and tells you how long you managed to survive in the terminal. //
        if (checkCollision(square1X, square1Y, square1Size, square2X, square2Y, square2Size)) {
            glfwSetWindowShouldClose(window.getWindowID(), true);
            System.out.println("You survived for: " + currentTimeSeconds);

        }
        // checks if the current time is equal to 25.0 and if so, change the background colour while the speed of the moving square increases. the '< 0.1' is there in case of any precision problems when comparing two double values. //
        // I know this is the most unoptimized and worst way to implement something like this. //
        if (Math.abs(currentTimeSeconds - 15.0) < 0.1) {
            glClearColor(0.34f, 0.23f, 0.49f, 1.0f);
            speed = 1.3f;
        }

        if (Math.abs(currentTimeSeconds - 30.0) < 0.1) {
            glClearColor(0.26f, 0.26f, 0.54f, 1.0f);
            speed = 1.1f;
        }

        if (Math.abs(currentTimeSeconds - 45.0) < 0.1) {
            glClearColor(0.55f, 0.25f, 0.32f, 1.0f);
            speed = 1.0f;
        }

        if (Math.abs(currentTimeSeconds - 60.0) < 0.1) {
            glClearColor(0.16f, 0.11f, 0.21f, 1.0f);
            speed = 0.8f;
        }



    }

    public void draw() {
        int program = shader.getProgramID();
        glUseProgram(program);
        glBindBuffer(GL_ARRAY_BUFFER, buff);
        int positionAttribLocation = glGetAttribLocation(program, "position");
        glEnableVertexAttribArray(positionAttribLocation);
        glVertexAttribPointer(positionAttribLocation, 2, GL_FLOAT, false, 5 * 4, 8 * 4);

        int colourAttribLocation = glGetAttribLocation(program, "colour");
        glEnableVertexAttribArray(colourAttribLocation);
        glVertexAttribPointer(colourAttribLocation, 3, GL_FLOAT, false, 5 * 4, 10 * 4);

        int offsetLocation = glGetUniformLocation(program, "offset");
        int scaleLocation = glGetUniformLocation(program, "scale");

        glUniform2f(offsetLocation, 0, 0);
        glUniform1f(scaleLocation, 1);

        glLineWidth(5);
        glVertexAttribPointer(positionAttribLocation, 2, GL_FLOAT, false, 0 * 4, 0 * 4);
        glUniform1f(scaleLocation, 10);
        glUniform2f(offsetLocation, x, y);

        glDrawArrays(GL_QUADS, 0, 4);
        glUniform2f(offsetLocation, offsetMove, square2Y);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(glGetUniformLocation(program, "mainTexture"), 0);

        int positionTexAttribLocation = glGetAttribLocation(program, "position");
        glEnableVertexAttribArray(positionTexAttribLocation);
        int mainTextureAttribLocation = glGetAttribLocation(program, "Tex");
        glEnableVertexAttribArray(mainTextureAttribLocation);
        glVertexAttribPointer(mainTextureAttribLocation, 2, GL_FLOAT, false, 5 * 4, 0);

        glDrawArrays(GL_QUADS, 0, 4);
        glPointSize(100);
        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

    // Checks if the two squares collided/ //
    private boolean checkCollision(float square1X, float square1Y, float square1Size, float square2X, float square2Y,
            float square2Size) {
        return square1X < square2X + square2Size &&
                square1X + square1Size > square2X &&
                square1Y < square2Y + square2Size &&
                square1Y + square1Size > square2Y;
    }

    private final Random random = new Random();

    // Generates random float value within a range and returns it. //
    private float getRandomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public void handleKeyPress(int key, int action) {
    }

    public void handleMouseClick(int key, int action) {
    }

    public void windowResized(int width, int height) {
    }

    public static void rect(float x, float y) {
    }

    public void dispose() {
    }

}

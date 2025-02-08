package com.example;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public class MyGame extends Game {

    Shader shader = new Shader("vertex.vert", "fragment.frag");

    int buff;
    int textureId;
    float offsetMove;
    Window window;
    float x;
    float y;

    public void init(Window window) {
        shader.initialize();
        this.window = window;

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
        fb.put(0, vertexData);

        buff = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, buff);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        try {
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
        offsetMove += deltaTime / 10f;
        try (MemoryStack stack = stackPush()) {
            DoubleBuffer xbuff = stack.callocDouble(1);
            DoubleBuffer ybuff = stack.callocDouble(1);
            glfwGetCursorPos(window.getWindowID(), xbuff, ybuff);
            x = (float) xbuff.get(0);
            y = (float) -ybuff.get(0) + window.height;

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
        // System.out.println(offsetMove);
        glUniform2f(offsetLocation, offsetMove, 4);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(glGetUniformLocation(program, "mainTexture"), 0);

        int positionTexAttribLocation = glGetAttribLocation(program, "position");
        glEnableVertexAttribArray(positionTexAttribLocation);
        int mainTextureAttribLocation = glGetAttribLocation(program, "Tex");
        glEnableVertexAttribArray(mainTextureAttribLocation);
        // glVertexAttribPointer(positionTexAttribLocation, 2, GL_FLOAT, false, 5 * 4,
        // 0);
        glVertexAttribPointer(mainTextureAttribLocation, 2, GL_FLOAT, false, 5 * 4, 0);

        glDrawArrays(GL_QUADS, 0, 4);
        glPointSize(100);
        glUseProgram(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

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

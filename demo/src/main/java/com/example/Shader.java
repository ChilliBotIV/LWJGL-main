package com.example;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderiv;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.system.MemoryStack;

public class Shader {
    private int programID;
    private String vertexShaderFileName;
    private String fragmentShaderFileName;

    public int getProgramID() {
        return programID;
    }

    Shader(String vertexShaderFileName, String fragmentShaderFileName) {
        this.vertexShaderFileName = vertexShaderFileName;
        this.fragmentShaderFileName = fragmentShaderFileName;
    }

    public void initialize() {
        int vs = createShader(GL_VERTEX_SHADER, loadFile(this.vertexShaderFileName));
        int fs = createShader(GL_FRAGMENT_SHADER, loadFile(this.fragmentShaderFileName));
        programID = createProgram(new int[] { vs, fs });
        glDeleteShader(vs);
        glDeleteShader(fs);

    }

    private String _bufferToString(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private String loadFile(String filename) {
        String fileContent = null;
        Path path = Paths.get(System.getProperty("user.dir"), "shaders", filename).toAbsolutePath();
        System.out.println("Loading file from " + path.toString());
        try {
            fileContent = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return fileContent;
    }

    private int createShader(int eShaderType, String shaderFile) {
        int shader = glCreateShader(eShaderType);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glShaderSource(shader, shaderFile);

            glCompileShader(shader);

            IntBuffer status = stack.mallocInt(1);
            glGetShaderiv(shader, GL_COMPILE_STATUS, status);
            if (status.get(0) == GL_FALSE) {
                IntBuffer infoLogLength = stack.mallocInt(1);
                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, infoLogLength);

                ByteBuffer infoLog = stack.malloc(infoLogLength.get(0) + 1);
                glGetShaderInfoLog(shader, infoLogLength, infoLog);

                String strShaderType = "";
                switch (eShaderType) {
                    case GL_VERTEX_SHADER:
                        strShaderType = "vertex";
                        break;
                    case GL_GEOMETRY_SHADER:
                        strShaderType = "geometry";
                        break;
                    case GL_FRAGMENT_SHADER:
                        strShaderType = "fragment";
                        break;
                }

                System.err.format("Compile failure in %s shader:%s", strShaderType, _bufferToString(infoLog));
            }
        }

        return shader;
    }

    private int createProgram(int[] shaderList) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int program = glCreateProgram();

            for (int i = 0; i < shaderList.length; i++)
                glAttachShader(program, shaderList[i]);

            glLinkProgram(program);

            IntBuffer status = stack.mallocInt(1);
            glGetProgramiv(program, GL_LINK_STATUS, status);
            if (status.get(0) == GL_FALSE) {
                IntBuffer infoLogLength = stack.mallocInt(1);
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, infoLogLength);

                ByteBuffer infoLog = stack.malloc(infoLogLength.get(0) + 1);
                glGetProgramInfoLog(program, infoLogLength, infoLog);
                System.err.format("Linker failure: %s", _bufferToString(infoLog));
            }

            for (int i = 0; i < shaderList.length; i++)
                glDetachShader(program, shaderList[i]);

            return program;
        }
    }

}

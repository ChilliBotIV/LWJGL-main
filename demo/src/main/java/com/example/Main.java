package com.example;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

class Args {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "--width", "-w" }, description = "Width of the screen")
    public int width = 640;

    @Parameter(names = { "--height", "-h" }, description = "Height of the screen")
    public int height = 480;

}

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
        Args arguments = new Args();
        JCommander.newBuilder().addObject(arguments).build().parse(args);

        createGame(arguments);
    }

    private static void createGame(Args arguments) {
        Window.Options options = new Window.Options();
        options.title = "Game";
        options.width = arguments.width;
        options.height = arguments.height;
        Window w = new Window(new MyGame(), options).create();
        w.loop();
        w.close();
    }
}

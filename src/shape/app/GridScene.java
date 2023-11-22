package shape.app;

import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import shape.global.AbstractRenderer;
import shape.model.Axis;
import shape.model.Character;
import shape.model.TerrainGrid;
import shape.utils.FpsLimiter;
import transforms.*;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.GL_PATCH_VERTICES;
import static org.lwjgl.opengl.GL40.glPatchParameteri;

enum Mode {
    Fill, Lines;

    public Mode next() {
        Mode[] array = Mode.values();
        int i = Arrays.asList(array).indexOf(this);
        return array[(i + 1) % array.length];
    }
}

public class GridScene extends AbstractRenderer {
    double ox, oy;
    Character character = new Character(new Camera().withPosition(new Vec3D(0, 0, 128.)).withZenith(-0.4), "res/map.bmp");
    Mat4 proj = new Mat4PerspRH(Math.toRadians(100), (double) height / width, 0.01, 500.);
    int shaderProgram;
    int tes = 21;
    private boolean renderDocDebug;
    private boolean changeScene = false;
    private HashMap<String, Integer> gridShaders;
    private FpsLimiter limiter;
    private TerrainGrid grid;
    private boolean manual = false;
    private Axis axis;
    private HashMap<String, ArrayList<String>> info;
    private boolean persp = true;
    private double speed = 2.5;
    private double zoom = 32;
    private boolean mouseButton1 = false;
    private Mode mode = Mode.Fill;
    private OGLTexture2D datamap, colormap;

    public GridScene(int width, int height, boolean debug) {
        super(width, height);
        renderDocDebug = debug;
        callbacks();
        gridShaders = new HashMap<>();

        info = new HashMap<>();
        info.put("gravity", new ArrayList<>(List.of("[G] Gravity:", String.valueOf(character.isGravity()))));
        info.put("maxTess", new ArrayList<>(List.of("[+/-] Max Tesselation:", String.valueOf(tes))));
        info.put("mode", new ArrayList<>(List.of("[M] Render mode:", "")));
        info.put("projection", new ArrayList<>(List.of("[P] Projection:", "")));
        info.put("speed", new ArrayList<>(List.of("[MWH] Zoom:", "32")));

        info.get("mode").set(1, mode.toString());
        info.get("projection").set(1, persp ? "Persp" : "Ortho");
    }

    private void callbacks() {
        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    switch (key) {
                        case GLFW_KEY_M -> {
                            mode = mode.next();
                            info.get("mode").set(1, mode.toString());
                        }
                        case GLFW_KEY_KP_ADD -> {
                            tes++;
                            info.get("maxTess").set(1, String.valueOf(tes));
                        }
                        case GLFW_KEY_KP_SUBTRACT -> {
                            tes--;
                            info.get("maxTess").set(1, String.valueOf(tes));
                        }
                        case GLFW_KEY_O -> {
                            manual = !manual;
                            info.get("manual").set(1, String.valueOf(manual));
                        }
                        case GLFW_KEY_G -> {
                            character.setGravity(!character.isGravity());
                            info.get("gravity").set(1, String.valueOf(character.isGravity()));
                        }
                        case GLFW_KEY_P -> {
                            if (persp) {
                                proj = new Mat4OrthoRH(width / zoom, height / zoom, 0.01, 1000.0);
                                persp = false;
                            } else {
                                proj = new Mat4PerspRH(Math.toRadians(100), (double) height / width, 0.01, 500.);
                                persp = true;
                            }
                            info.get("projection").set(1, persp ? "Persp" : "Ortho");
                        }
                        case GLFW_KEY_SPACE -> character.jump();
                        case GLFW_KEY_W -> character.forward(speed);
                        case GLFW_KEY_D -> character.right(speed);
                        case GLFW_KEY_S -> character.backward(speed);
                        case GLFW_KEY_A -> character.left(speed);
                        case GLFW_KEY_LEFT_CONTROL -> character.down(speed);
                        case GLFW_KEY_LEFT_SHIFT -> character.up(speed);
                    }
                }
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                if (!persp) {
                    zoom += zoom * dy * 0.1;
                    info.get("speed").set(1, String.format("%.0f", zoom));
                    proj = new Mat4OrthoRH(width / zoom, height / zoom, 0.01, 1000.0);
                }
            }
        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
                    character = character.addAzimuth((double) Math.PI * (ox - x) / width).addZenith((double) Math.PI * (oy - y) / width);
                    ox = x;
                    oy = y;
                }
            }
        };

        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    mouseButton1 = true;
                    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuffer, yBuffer);
                    ox = xBuffer.get(0);
                    oy = yBuffer.get(0);
                }

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) {
                    mouseButton1 = false;
                    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuffer, yBuffer);
                    double x = xBuffer.get(0);
                    double y = yBuffer.get(0);
                    character = character.addAzimuth((double) Math.PI * (ox - x) / width).addZenith((double) Math.PI * (oy - y) / width);
                    ox = x;
                    oy = y;
                }
            }

        };
    }

    @Override
    public void init() {
        super.init();
        changeScene = false;
        GL.createCapabilities();
        limiter = new FpsLimiter();
        glClearColor(0.4f, 0.4f, 0.5f, 1.0f);

        shaderProgram = ShaderUtils.loadProgram("/tessel_terrain");

        axis = new Axis();
        try {
            datamap = new OGLTexture2D("map.bmp");
            colormap = new OGLTexture2D("colormap.bmp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        grid = new TerrainGrid(32, 1024F);
        glEnable(GL_DEPTH_TEST);
        glfwWindowHint(GLFW_SAMPLES, 4);
        glEnable(GL_MULTISAMPLE);
        glPatchParameteri(GL_PATCH_VERTICES, 4);
    }

    @Override
    public void display() {
        character.gravity();
        //shared across scenes
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);
        datamap.bind(shaderProgram, "datamap", 0);
        colormap.bind(shaderProgram, "colormap", 1);
        glUniform1i(glGetUniformLocation(shaderProgram, "maxTess"), tes);
        glUniform3fv(glGetUniformLocation(shaderProgram, "cameraPos"), ToFloatArray.convert(character.getPosition()));
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "view"), false, ToFloatArray.convert(character.getViewMatrix()));
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), false, ToFloatArray.convert(proj));

        switch (mode) {
            case Fill -> {
                glPolygonMode(GL_FRONT, GL_FILL);
                glPolygonMode(GL_BACK, GL_FILL);
                grid.draw(shaderProgram);
            }
            case Lines -> {
                glPolygonMode(GL_FRONT, GL_LINE);
                glPolygonMode(GL_BACK, GL_LINE);
                grid.draw(shaderProgram);
            }
        }

        axis.draw(character.getViewMatrix().mul(proj));

        if (!renderDocDebug) {
            text();
            textRenderer.addStr2D(width - 120, height - 3, " (c) Matěj Kolář UHK");
            textRenderer.addStr2D(width - 50, 15, "FPS: " + limiter.getCurrentFps());
        }
        limiter.limit();
    }

    private void text() {
        int y = 15;
        for (ArrayList<String> entry : info.values()) {
            textRenderer.addStr2D(5, y, String.join(" ", entry));
            y += 15;
        }
    }

    public boolean nextScene() {
        return changeScene;
    }

    @Override
    public void dispose() {
        for (int s : gridShaders.values()) {
            glDeleteProgram(s);
        }
        grid.unbind();
    }

}
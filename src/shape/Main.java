package shape;

import shape.app.GridScene;
import shape.global.LwjglWindow;

public class Main {
    public static void main(String[] args) {
        boolean debufg = false;
        int w = 1280;
        int h = 720;
        GridScene grid = new GridScene(w, h, debufg);
        LwjglWindow win = new LwjglWindow(w, h, false);
        win.setRenderer(grid);
        win.exit();
        System.exit(0);
    }

}
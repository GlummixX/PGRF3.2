package shape.model;

import transforms.Camera;
import transforms.Mat4;
import transforms.Vec2D;
import transforms.Vec3D;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Character {
    private Camera cam;
    private BufferedImage map;
    private int width, height;
    private boolean gravity = true;
    private boolean jumped = false;
    private double zdist = 2.;
    private int jumping = 0;

    public Character(Camera cam, String mapFile) {
        this.cam = cam;
        try {
            map = ImageIO.read(new File(mapFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        width = map.getWidth();
        height = map.getHeight();
    }

    private int getZ(Vec2D pos) {
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getX() > width || pos.getY() > height) {
            return 0;
        }
        int val = map.getRGB((int) pos.getX(), (int) pos.getY());
        return (val >> 16) & 0xFF;
    }

    public void gravity() {
        if (gravity) {
            Vec3D pos = cam.getPosition();
            int z = getZ(pos.ignoreZ());
            if (jumping < 20) {
                cam = cam.up(0.4);
                jumping++;
            } else if (pos.getZ() > z + zdist) {
                cam = cam.down(0.2);
            }
        }
    }

    private void terrain() {
        Vec3D reloc = cam.getPosition();
        Vec3D pos = cam.getPosition();
        if (pos.getX() > width) {
            reloc = reloc.withX(width);
        }
        if (pos.getX() < 0) {
            reloc = reloc.withX(0.);
        }
        if (pos.getY() > height) {
            reloc = reloc.withY(height);
        }
        if (pos.getY() < 0) {
            reloc = reloc.withY(0.);
        }
        int z = getZ(pos.ignoreZ());
        if (pos.getZ() < z + zdist - 0.02) {
            reloc = reloc.withZ(z + zdist);
        }
        if (pos.getZ() <= z + zdist - 0.02 && jumped) {
            jumped = false;
        }

        if (reloc != pos) {
            cam = cam.withPosition(reloc);
        }
    }

    public void jump() {
        if (jumped) {
            return;
        }
        jumped = true;
        jumping = 0;
        terrain();
    }

    public void forward(double speed) {
        cam = cam.forward(speed);
        terrain();
    }

    public void right(double speed) {
        cam = cam.right(speed);
        terrain();
    }

    public void backward(double speed) {
        cam = cam.backward(speed);
        terrain();
    }

    public void left(double speed) {
        cam = cam.left(speed);
        terrain();
    }

    public void down(double speed) {
        cam = cam.down(speed);
        terrain();
    }

    public void up(double speed) {
        cam = cam.up(speed);
        terrain();
    }

    public Character addAzimuth(double v) {
        cam = cam.addAzimuth(v);
        return this;
    }

    public Character addZenith(double v) {
        cam = cam.addZenith(v);
        return this;
    }

    public Vec3D getPosition() {
        return cam.getPosition();
    }

    public Mat4 getViewMatrix() {
        return cam.getViewMatrix();
    }

    public boolean isGravity() {
        return gravity;
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }
}

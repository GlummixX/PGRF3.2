package shape.model;

import lwjglutils.OGLBuffers;

import static org.lwjgl.opengl.GL40.GL_PATCHES;

public class TerrainGrid {
    private OGLBuffers buffers;
    private int topology;

    public TerrainGrid(int boxesPerSide, float size) {
        float c = boxesPerSide / 2.f;
        float boxSize = size / boxesPerSide;
        boxesPerSide += 1;
        float[] vertex = new float[boxesPerSide * boxesPerSide * 2];
        for (int y = 0; y < boxesPerSide; y++) {
            for (int x = 0; x < boxesPerSide; x++) {
                int idx = (y * boxesPerSide + x) * 2;
                vertex[idx] = (x * boxSize);
                vertex[idx + 1] = (y * boxSize);
            }
        }

        int[] index = new int[((boxesPerSide - 1) * (boxesPerSide - 1)) * 4];
        for (int y = 0; y < boxesPerSide - 1; y++) {
            for (int x = 0; x < boxesPerSide - 1; x++) {
                int idx = (y * (boxesPerSide - 1) + x) * 4;
                index[idx] = (y * boxesPerSide + x);
                index[idx + 1] = (y * boxesPerSide + x + 1);
                index[idx + 2] = ((y + 1) * boxesPerSide + x + 1);
                index[idx + 3] = ((y + 1) * boxesPerSide + x);
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)};

        buffers = new OGLBuffers(vertex, attributes, index);
        topology = GL_PATCHES;
    }

    public void draw(int program) {
        buffers.draw(topology, program);
    }

    public void draw(int program, int topology) {
        buffers.draw(topology, program);
    }

    public void unbind() {
        buffers.unbind();
    }
}



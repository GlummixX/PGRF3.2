package shape.model;

import lwjglutils.OGLBuffers;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class TerrainGrid {
    private List<SubGrid> subs;

    public TerrainGrid(int centerSide, float centerSideSize){
        float mx = -centerSideSize/2F;
        float my = -centerSideSize/2F;
        subs = new ArrayList<>();
        subs.add(new SubGrid(centerSide, centerSideSize, mx,my));
        for (int x = -1; x < 2 ; x++) {
            for (int y = -1; y < 2 ; y++) {
                if (x == 0 && y == 0)continue;
                subs.add(new SubGrid(centerSide/2, centerSideSize, x*centerSideSize+mx,y*centerSideSize+my));
            }
        }
        centerSideSize = centerSideSize*3;
        mx = -centerSideSize/2F;
        my = -centerSideSize/2F;
        centerSide = centerSide*3;
        for (int x = -1; x < 2 ; x++) {
            for (int y = -1; y < 2 ; y++) {
                if (x == 0 && y == 0)continue;
                subs.add(new SubGrid(centerSide/4, centerSideSize, x*centerSideSize+mx,y*centerSideSize+my));
            }
        }
    }

    public void draw(int program){
        for (SubGrid s:subs) {
            s.draw(program);
        }
    }
    public void draw(int program, int topology){
        for (SubGrid s:subs) {
            s.draw(program, topology);
        }
    }
    public void unbind(){
        for (SubGrid s:subs) {
            s.unbind();
        }
    }


    public class SubGrid {
        private OGLBuffers buffers;
        private int topology;

        public void draw(int program) {
            buffers.draw(topology, program);
        }

        public void draw(int program, int topology) {
            buffers.draw(topology, program);
        }

        public SubGrid(int boxesPerSide, float size, float xoff, float yoff) {
            float c = boxesPerSide / 2.f;
            float boxSize = size / boxesPerSide;
            boxesPerSide += 1;
            float[] vertex = new float[boxesPerSide * boxesPerSide * 2];
            for (int y = 0; y < boxesPerSide; y++) {
                for (int x = 0; x < boxesPerSide; x++) {
                    int idx = (y * boxesPerSide + x) * 2;
                    vertex[idx] = (x  * boxSize)+xoff;
                    vertex[idx + 1] = (y * boxSize)+yoff;
                }
            }

            int[] index = new int[2 * boxesPerSide * (boxesPerSide - 1)];
            int r = 0;
            for (int row = 0; row < boxesPerSide - 1; row++) {
                if (row % 2 == 0) {
                    // Even rows
                    for (int col = 0; col < boxesPerSide; col++) {
                        index[r++] = col + row * boxesPerSide;
                        index[r++] = col + (row + 1) * boxesPerSide;
                    }
                } else {
                    // Odd rows (reverse order)
                    for (int col = boxesPerSide - 1; col >= 0; col--) {
                        index[r++] = col + (row + 1) * boxesPerSide;
                        index[r++] = col + row * boxesPerSide;
                    }
                }
            }

            OGLBuffers.Attrib[] attributes = {
                    new OGLBuffers.Attrib("inPosition", 2)};

            buffers = new OGLBuffers(vertex, attributes, index);
            topology = GL_TRIANGLE_STRIP;
        }

        public void unbind() {
            buffers.unbind();
        }
    }
}



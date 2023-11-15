package shape.utils;

public class FpsLimiter {
    private boolean limit;
    private int loopTimeReq;
    private AveragingCircularBuffer<Float> buffer;

    private long realDelta = 1;
    private long realRef = 1;

    public FpsLimiter(int fps) {
        setFps(fps);
        limit = true;
        buffer = new AveragingCircularBuffer<Float>(6);
    }

    public FpsLimiter() {
        limit = false;
        buffer = new AveragingCircularBuffer<Float>(6);
    }

    public void limit() {
        realDelta = System.currentTimeMillis() - realRef;
        realRef = System.currentTimeMillis();
        buffer.add(1000F / realDelta);
        if (limit) {
            long wait = loopTimeReq - realDelta;
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void setFps(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS limit must be greater than 0");
        }
        this.loopTimeReq = Math.round(1000F / fps);
    }

    public void useLimit(boolean enabled) {
        limit = enabled;
    }

    public long getDelta() {
        return realDelta;
    }

    public int getCurrentFps() {
        return (int) Math.round(buffer.getAverage());
    }
}

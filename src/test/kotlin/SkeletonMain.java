import com.codingame.gameengine.runner.SoloGameRunner;

public class SkeletonMain {
    public static void main(String[] args) {
        SoloGameRunner gameRunner = new SoloGameRunner();
        gameRunner.setAgent(TestPlayer.class);
        gameRunner.setTestCase("test1.json");
        gameRunner.start();
    }
}

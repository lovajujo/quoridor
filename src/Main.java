import game.engine.Engine;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        int rand_int= ThreadLocalRandom.current().nextInt();
        String szam=Integer.toString(rand_int);
        String[] args1={"0", "game.quoridor.QuoridorGame", "1827364532", "1000", "game.quoridor.players.BlockRandomPlayer", "SamplePlayer"};
        try {
            Engine.main(args1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
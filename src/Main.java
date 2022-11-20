import game.engine.Engine;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        String[] args1={"5", "game.quoridor.QuoridorGame", "9517836240",
                "1000", "Agent","game.quoridor.players.BlockRandomPlayer" };
        try {
            Engine.main(args1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
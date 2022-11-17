import game.engine.Engine;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        String[] args1={"1", "game.quoridor.QuoridorGame", "1234567890", "10000000", "Agent","game.quoridor.players.HumanPlayer" };
        try {
            Engine.main(args1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
///lovajujo, lovaszi.zsuzsanna@stud.u-szeged.hu
import java.util.*;

import game.quoridor.MoveAction;
import game.quoridor.QuoridorGame;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.DummyPlayer;
import game.quoridor.utils.PlaceObject;
import game.quoridor.utils.QuoridorAction;
import game.quoridor.utils.WallObject;


public class Agent extends QuoridorPlayer {
    private final List<WallObject> walls = new LinkedList<>();
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    private int numWalls;
    private final Node[][] graph = getGraph();
    private final int whichWay=(color==1 ? 0 : 8);

    public Agent(int i, int j, int color, Random random){
        super(i, j, color, random);
        players[color]=this;
        players[1-color]=new DummyPlayer((1-color) * (QuoridorGame.HEIGHT - 1), j, 1-color, null);
        numWalls = 0;
    }

    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {

        if (prevAction instanceof WallAction) {
            WallAction a = (WallAction) prevAction;
            walls.add(new WallObject(a.i, a.j, a.horizontal));
        } else if (prevAction instanceof MoveAction) {
            MoveAction a = (MoveAction) prevAction;
            players[1 - color].i = a.to_i;
            players[1 - color].j = a.to_j;

            WallObject wall = buildWall(graph[players[1 - color].i][players[1 - color].j]);
            if (wall != null) {
                numWalls++;
                walls.add(wall);
                return wall.toWallAction();
            }
        }
        int[] index = findShortestPath(graph[players[color].i][players[color].j], whichWay, false);
        return new MoveAction(players[color].i, players[color].j, index[0], index[1]);
    }
    public WallObject buildWall(Node start){
        ArrayList<WallObject> wallCandidates=new ArrayList<>();
        int[] index=findShortestPath(start, 8-whichWay, true);
        wallCandidates.add(new WallObject(index[0], index[1],true));
        if(index[1]>0){
            wallCandidates.add(new WallObject(index[0], index[1]-1, true));
        }
        for(WallObject wall:wallCandidates){
            if(QuoridorGame.checkWall(wall, walls, players)){
                return wall;
            }
        }
        return null;
    }
    /*tömböt ad vissz: i, j index (kövi lépés)*/
    public int[] findShortestPath(Node start, int goal, boolean wall){
        setNodeCosts(start, goal);
        int[]index={start.getRow(), start.getColumn()};
        ArrayList<Node> open=new ArrayList<>();
        LinkedList<Node> closed=new LinkedList<>();
        open.add(start);
        while(!open.isEmpty()){
            Node current=findMinimum(open);
            if(current.getRow()==goal){
                Node next=reconstructPath(current, wall);
                index[0]=next.getRow();
                index[1]=next.getColumn();
                resetParents();
                return index;
            }
            open.remove(current);
            ArrayList<Node> children=findChildren(current);
            for (Node child : children) {
                if (!closed.contains(child) && !open.contains(child) &&
                        !QuoridorGame.isWallBetween(walls, new PlaceObject(current.getRow(), current.getColumn()),
                                new PlaceObject(child.getRow(), child.getColumn()))){
                    if(!child.sameNode(start)){
                        child.setParent(current);
                    }
                    open.add(child);
                }
            }
            closed.add(current);
        }
        return index;
    }

    public Node reconstructPath(Node last, boolean wall){
        Node parent=last;
        LinkedList<Node> path=new LinkedList<>();
        while(parent!=null){
            path.add(parent);
            parent=parent.getParent();
        }
        if((wall || path.size()==1) && whichWay==0){
            return path.getLast();
        }
        path.removeLast();
        return path.getLast();

    }

    public Node findMinimum(ArrayList<Node> open){
        int i=0;
        int j=0;
        int f=999;
        int g=999;
        for(Node node:open){
            if(node.getF()<f || (node.getF()==f && node.getG()<g)){
                f=node.getF();
                g= node.getG();
                i=node.getRow();
                j=node.getColumn();
            }
        }
        return graph[i][j];
    }

    public ArrayList<Node> findChildren( Node parent){
        int i=parent.getRow();
        int j=parent.getColumn();
        ArrayList<Node> children=new ArrayList<>();
        if(i>0){
            children.add(graph[i-1][j]);
        }
        if(i<QuoridorGame.WIDTH-1){
            children.add(graph[i+1][j]);
        }
        if(j>0){
            children.add(graph[i][j-1]);
        }
        if(j<QuoridorGame.HEIGHT-1){
            children.add(graph[i][j+1]);
        }

        return  children;
    }

    public int getHCost(int current_i, int destination_i){
        return Math.abs(current_i-destination_i);
    }
    public int getGCost(int start_i, int start_j, int current_i, int current_j){
        int vertical=Math.abs(start_j-current_j);
        int horizontal=Math.abs(start_i-current_i);
        return vertical+horizontal;
    }

    public Node[][] getGraph(){
        Node[][] graph=new Node[QuoridorGame.HEIGHT][QuoridorGame.WIDTH];
        for(int i=0; i<QuoridorGame.HEIGHT;i++){
            for(int j=0; j<QuoridorGame.WIDTH;j++){
                graph[i][j]=new Node(i,j);
            }
        }
        return graph;
    }
    public void setNodeCosts(Node start, int goal){
        for(int i=0; i<QuoridorGame.HEIGHT;i++){
            for(int j=0; j<QuoridorGame.WIDTH;j++){
                graph[i][j].setG(getGCost(start.getRow(), start.getColumn(), i, j));
                graph[i][j].setH(getHCost(i, goal));
                graph[i][j].setF();
            }
        }
    }

    public void resetParents(){
        for(int i=0;i<QuoridorGame.HEIGHT;i++){
            for(int j=0; j<QuoridorGame.WIDTH;j++){
                graph[i][j].setParent(null);
            }
        }
    }

    class Node{
        Node parent=null;
        int column;
        int row;
        int f;
        int g;
        int h;


        public Node(int r, int c) {
            this.column = c;
            this.row = r;

        }

        public Node(Node parent, int column, int row, int f, int g, int h) {
            this.parent = parent;
            this.column = column;
            this.row = row;
            this.f = f;
            this.g = g;
            this.h = h;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public int getF() {
            return f;
        }

        public void setF() {
            this.f = this.g+this.h;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }

        public boolean sameNode(Node other){
            return (getRow()==other.getRow() && getColumn()==other.getColumn());
        }
    }
}

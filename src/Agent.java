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
            WallAction action = (WallAction) prevAction;
            walls.add(new WallObject(action.i, action.j, action.horizontal));
        } else if (prevAction instanceof MoveAction) {
            MoveAction action = (MoveAction) prevAction;
            players[1 - color].i = action.to_i;
            players[1 - color].j = action.to_j;
        }
        LinkedList<Node> thisPath = findShortestPath(graph[i][j], whichWay);
        LinkedList<Node> oppositionPath = findShortestPath(graph[players[1 - color].i][players[1 - color].j], 8 - whichWay);
        if (thisPath.size() > oppositionPath.size()) {
            WallObject wall = getWall(nextPlace(oppositionPath));
            if (wall != null) {
                numWalls++;
                walls.add(wall);
                return wall.toWallAction();
            }
        }
        LinkedList<Integer> place= nextPlace(thisPath);
        return new MoveAction(players[color].i, players[color].j,
                place.getFirst(), place.getLast());
    }
    public WallObject getWall(LinkedList<Integer> next){
        ArrayList<WallObject> wallCandidates=new ArrayList<>();
        if(next.getFirst()==players[1-color].i){
            if(next.getLast()-players[1-color].j==1){
                wallCandidates.add(new WallObject(players[1-color].i, players[1-color].j,false));
                if(players[1-color].i>0){
                    wallCandidates.add(new WallObject(players[1-color].i-1, players[1-color].j,false));
                }
            }else{
                wallCandidates.add(new WallObject(next.getFirst(), next.getLast(),false));
                if(next.getFirst()>0){
                    wallCandidates.add(new WallObject(next.getFirst()-1, next.getLast(),false));
                }
            }

        }
        else if(next.getLast()==players[1-color].j){
            if(whichWay==0){
                wallCandidates.add(new WallObject(players[1-color].i,players[1-color].j, true));
                if(players[1-color].j>0){
                    wallCandidates.add(new WallObject(players[1-color].i,players[1-color].j-1,true));
                }
            }else{
                wallCandidates.add(new WallObject(next.getFirst(), next.getLast(), true));
                if(players[1-color].j>0){
                    wallCandidates.add(new WallObject(next.getFirst(), next.getLast()-1,true));
                }
            }

        }
        for(WallObject wall:wallCandidates){
            if(numWalls < QuoridorGame.MAX_WALLS && QuoridorGame.checkWall(wall, walls, players)){
                return wall;
            }
        }
        return null;
    }

    public LinkedList<Node> findShortestPath(Node start, int goal){
        ArrayList<Node> open=new ArrayList<>();
        LinkedList<Node> closed=new LinkedList<>();
        open.add(start);
        Node current=start;
        while(!open.isEmpty()){
            if(current.getRow()==goal){
                LinkedList<Node> path;
                path=reconstructPath(current);
                resetParents();
                return path;
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
            current=findMinimum(open, goal, reconstructPath(current).size());
        }
        return null;
    }

    public LinkedList<Node> reconstructPath(Node last){
        Node parent=last;
        LinkedList<Node> path=new LinkedList<>();
        path.add(parent);
        while(parent.getParent()!=null){
            path.add(parent.getParent());
            parent=parent.getParent();
        }
        for (Node node:path) {

        }
        return path;
    }

    public LinkedList<Integer> nextPlace(LinkedList<Node> path){
        LinkedList<Integer> place=new LinkedList<>();
        path.removeLast();
        place.add(path.getLast().getRow());
        place.add(path.getLast().getColumn());
        return place;
    }

    public Node findMinimum(ArrayList<Node> open, int goal, int stepsSoFar){
        int i=0;
        int j=0;
        int f=999;
        int g=999;
        for(Node node:open){
            setNodeCost(node, goal, stepsSoFar);
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

    public void setNodeCost(Node node, int goal, int g){
        node.setH(getHCost(node.getRow(), goal));
        node.setG(g);
        node.setF();
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

package utils;

import java.util.ArrayList;
import agents.TAgent;
import repast.simphony.space.grid.GridPoint;

public class Calls<T> {

    public static enum Positions {A_SHORT, A_LONG, A_SITE, MID_TO_B, B_TUNNELS, B_SITE}
    public static enum Callouts {A_SPLIT, A_RUSH, B_SPLIT, B_RUSH, DEFAULT} 

   
    private final Positions[] A_SPLIT = {Positions.A_SHORT, Positions.A_SHORT, Positions.A_LONG, Positions.A_LONG, Positions.A_LONG};
    private final Positions[] A_RUSH = {Positions.A_SITE, Positions.A_SITE, Positions.A_SITE, Positions.A_SITE, Positions.A_SITE};
    private final Positions[] B_SPLIT = {Positions.MID_TO_B, Positions.MID_TO_B, Positions.MID_TO_B, Positions.B_TUNNELS, Positions.B_TUNNELS};
    private final Positions[] B_RUSH = {Positions.B_SITE, Positions.B_SITE, Positions.B_SITE, Positions.B_SITE, Positions.B_SITE};
    private final Positions[] DEFAULT = {Positions.B_SITE, Positions.MID_TO_B, Positions.A_SHORT, Positions.A_LONG, Positions.A_SITE};

    
    public Positions[] getCallouts(Callouts co) {
    	switch(co) {
    	case A_SPLIT:
    		return A_SPLIT;
    	case A_RUSH:
    		return A_RUSH;
    	case B_SPLIT:
    		return B_SPLIT;
    	case B_RUSH:
    		return B_RUSH;
    	case DEFAULT:
    		return DEFAULT;
    	default:
    		return null;
    	}
    }

    public ArrayList<GridPoint> getPosition(Positions position, T type){
        ArrayList<GridPoint> pos = new ArrayList<GridPoint>();
        switch(position){
            case A_SHORT:
                pos.add(new GridPoint(31,30));

                if(type instanceof TAgent)
                    pos.add(new GridPoint(39,42));
                
                return pos;

            case A_LONG:
                pos.add(new GridPoint(42,24));

                if(type instanceof TAgent)
                    pos.add(new GridPoint(39,42));
                return pos;
            
            case A_SITE:
                pos.add(new GridPoint(39,42));
                return pos;

            case MID_TO_B:
                pos.add(new GridPoint(5,26));

                if(type instanceof TAgent)
                    pos.add(new GridPoint(10,43));
                return pos;

            case B_TUNNELS:
                pos.add(new GridPoint(39,44));

                if(type instanceof TAgent)
                    pos.add(new GridPoint(10,43));
                return pos;

            case B_SITE:
                pos.add(new GridPoint(10,43));
                return pos;

            default:
                return null;
        }
    }
}
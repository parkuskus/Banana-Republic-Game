package banana.republic.building;

import banana.republic.player.Player;


public class PlayerSupply {

    private static final int MAX_POS_PANTAU = 5;
    private static final int MAX_LABORATORIUM = 4;
    private static final int MAX_ROADS = 15;

    private final Player owner;
    private int posPantau;
    private int laboratorium;
    private int roads;

    public PlayerSupply(Player owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        this.owner = owner;
        this.posPantau = MAX_POS_PANTAU;
        this.laboratorium = MAX_LABORATORIUM;
        this.roads = MAX_ROADS;
    }

    public int getPosPantauRemaining() {
        return posPantau;
    }

    public int getMaxPosPantau() {
        return MAX_POS_PANTAU;
    }

    public int getPosPantauUsed() {
        return MAX_POS_PANTAU - posPantau;
    }

    public int getLaboratoriumRemaining() {
        return laboratorium;
    }

    public int getMaxLaboratorium() {
        return MAX_LABORATORIUM;
    }

    public int getLaboratoriumUsed() {
        return MAX_LABORATORIUM - laboratorium;
    }

    public int getRoadsRemaining() {
        return roads;
    }

    public int getMaxRoads() {
        return MAX_ROADS;
    }

    public int getRoadsUsed() {
        return MAX_ROADS - roads;
    }


    public PosPantau takePosPantau() {
        if (posPantau <= 0) {
            throw new IllegalStateException("No Pos Pantau remaining for " + owner.getName());
        }
        posPantau--;
        assert posPantau >= 0 : "Pos Pantau supply should never be negative";
        return new PosPantau(owner);
    }

    public Laboratorium takeLaboratorium() {
        if (laboratorium <= 0) {
            throw new IllegalStateException("No Laboratorium remaining for " + owner.getName());
        }
        laboratorium--;
        assert laboratorium >= 0 : "Laboratorium supply should never be negative";
        return new Laboratorium(owner);
    }


    public Road takeRoad() {
        if (roads <= 0) {
            throw new IllegalStateException("No roads remaining for " + owner.getName());
        }
        roads--;
        assert roads >= 0 : "Road supply should never be negative";
        return new Road(owner);
    }


    public void returnPosPantau(PosPantau posPantau) {
        if (posPantau == null) {
            throw new IllegalArgumentException("Pos Pantau cannot be null");
        }
        if (posPantau.getOwner() != owner) {
            throw new IllegalArgumentException("Cannot return a Pos Pantau owned by another player");
        }
        if (this.posPantau >= MAX_POS_PANTAU) {
            throw new IllegalStateException("Pos Pantau supply already full for " + owner.getName());
        }
        this.posPantau++;
    }

    public boolean canBuildPosPantau() {
        return posPantau > 0;
    }

    public boolean canBuildLaboratorium() {
        return laboratorium > 0;
    }

    public boolean canBuildRoad() {
        return roads > 0;
    }

    @Override
    public String toString() {
        return String.format("PlayerSupply[posPantau=%d/%d, laboratorium=%d/%d, roads=%d/%d]",
            posPantau, MAX_POS_PANTAU, laboratorium, MAX_LABORATORIUM, roads, MAX_ROADS);
    }
}

package banana.republic.building;

import banana.republic.player.Player;


public class Laboratorium extends Building {

    private static final int VICTORY_POINTS = 2;
    private static final int PRODUCTION_AMOUNT = 2;

    public Laboratorium(Player owner) {
        super(owner);
    }

    @Override
    public int getVictoryPoints() {
        return VICTORY_POINTS;
    }

    @Override
    public int getProductionAmount() {
        return PRODUCTION_AMOUNT;
    }

    @Override
    public BuildingType getBuildingType() {
        return BuildingType.LABORATORIUM;
    }
}

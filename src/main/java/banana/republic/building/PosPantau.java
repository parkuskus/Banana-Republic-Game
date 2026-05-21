package banana.republic.building;

import banana.republic.player.Player;


public class PosPantau extends Building {

    private static final int VICTORY_POINTS = 1;
    private static final int PRODUCTION_AMOUNT = 1;

    public PosPantau(Player owner) {
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
        return BuildingType.POS_PANTAU;
    }
}

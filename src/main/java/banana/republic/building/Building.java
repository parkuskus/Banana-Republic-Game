package banana.republic.building;

import banana.republic.player.Player;

public abstract class Building {

    private final Player owner;

    public Building(Player owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Building owner cannot be null");
        }
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }


    public abstract int getVictoryPoints();


    public abstract int getProductionAmount();

 
    public abstract BuildingType getBuildingType();

    @Override
    public String toString() {
        return String.format("%s[owner=%s, vp=%d, production=%d]",getClass().getSimpleName(), owner.getName(), getVictoryPoints(), getProductionAmount());
    }
}

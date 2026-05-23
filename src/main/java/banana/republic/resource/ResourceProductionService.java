package banana.republic.resource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.building.Building;
import banana.republic.player.Player;


public class ResourceProductionService {


    public void distribute(HexTile tile, Board board, List<Player> players, Bank bank) {
        if (tile == null || board == null || players == null || bank == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        if (!tile.canProduce()) {
            return;
        }

        ResourceType resourceType = tile.getResourceType();
        if (resourceType == null) {
            return;
        }

        Map<Player, Integer> productionPerPlayer = calculateProduction(tile, board);
        if (productionPerPlayer.isEmpty()) {
            return;
        }

        int totalNeeded = productionPerPlayer.values().stream().mapToInt(Integer::intValue).sum();
        int available = bank.getCount(resourceType);

        if (totalNeeded <= available) {
            for (Map.Entry<Player, Integer> entry : productionPerPlayer.entrySet()) {
                int amount = entry.getValue();
                if (amount > 0) {
                    bank.takeResource(resourceType, amount);
                    entry.getKey().addResource(resourceType, amount);
                }
            }
        } else {
            int affectedPlayerCount = productionPerPlayer.size();
            if (affectedPlayerCount == 1) {
                Map.Entry<Player, Integer> entry = productionPerPlayer.entrySet().iterator().next();
                int amount = Math.min(entry.getValue(), available);
                if (amount > 0) {
                    bank.takeResource(resourceType, amount);
                    entry.getKey().addResource(resourceType, amount);
                }
            } else {
                System.out.println("Resource shortage for " + resourceType + " on tile " + tile.getId() +". Needed: " + totalNeeded + ", Available: " + available +". No resources distributed to any player.");
            }
        }
    }


    public void distributeForRoll(int roll, Board board, List<Player> players, Bank bank) {
        assert roll >= 2 && roll <= 12 : "Dice roll must be between 2 and 12, was: " + roll;
        if (roll == 7) {
            return;
        }
        if (board == null || players == null || bank == null) {
            throw new IllegalArgumentException("Board, players, and bank cannot be null");
        }

        List<HexTile> tiles = board.getTilesWithToken(roll);
        for (HexTile tile : tiles) {
            if (tile.canProduce()) {
                distribute(tile, board, players, bank);
            }
        }
    }


    public boolean canDistribute(HexTile tile, Board board, List<Player> players, Bank bank) {
        if (tile == null || board == null || players == null || bank == null) {
            return false;
        }

        if (!tile.canProduce()) {
            return false;
        }

        ResourceType resourceType = tile.getResourceType();
        if (resourceType == null) {
            return false;
        }

        Map<Player, Integer> productionPerPlayer = calculateProduction(tile, board);
        if (productionPerPlayer.isEmpty()) {
            return false;
        }

        int totalNeeded = productionPerPlayer.values().stream().mapToInt(Integer::intValue).sum();
        int available = bank.getCount(resourceType);

        if (totalNeeded <= available) {
            return true;
        }

        if (productionPerPlayer.size() == 1) {
            return available > 0; // Single player gets partial
        }

        return false; // Multiple players, shortage -> nobody gets anything
    }


    public void distributeInitialResources(Player player, Intersection secondPosPantau,
                                             Bank bank, Board board) {
        if (player == null || secondPosPantau == null || bank == null || board == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        List<HexTile> adjacentHexes = board.getAdjacentHexTiles(secondPosPantau);
        Map<ResourceType, Integer> resourceCounts = new EnumMap<>(ResourceType.class);

        for (HexTile hex : adjacentHexes) {
            if (hex.canProduce()) {
                ResourceType type = hex.getResourceType();
                if (type != null) {
                    resourceCounts.merge(type, 1, Integer::sum);
                }
            }
        }

        for (Map.Entry<ResourceType, Integer> entry : resourceCounts.entrySet()) {
            ResourceType type = entry.getKey();
            int needed = entry.getValue();
            int available = bank.getCount(type);

            if (needed <= available) {
                bank.takeResource(type, needed);
                player.addResource(type, needed);
            } else if (available > 0) {
                // Only partial available - give what we can
                bank.takeResource(type, available);
                player.addResource(type, available);
            }
        }
    }

    private Map<Player, Integer> calculateProduction(HexTile tile, Board board) {
        Map<Player, Integer> productionPerPlayer = new java.util.HashMap<>();
        List<Intersection> adjacentIntersections = board.getAdjacentIntersections(tile);

        for (Intersection intersection : adjacentIntersections) {
            if (intersection.hasBuilding()) {
                Building building = intersection.getBuilding();
                Player owner = building.getOwner();
                int production = building.getProductionAmount();
                if (owner != null && production > 0) {
                    productionPerPlayer.merge(owner, production, Integer::sum);
                }
            }
        }

        return productionPerPlayer;
    }
}

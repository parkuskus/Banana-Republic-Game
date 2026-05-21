package banana.republic.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import banana.republic.card.ExperimentCard;
import banana.republic.core.GameState;
import banana.republic.resource.ResourceType;


public class BotPlayer implements Player {

    private final String name;
    private final PlayerColor color;
    private final Map<ResourceType, Integer> resources;
    private final List<ExperimentCard> handCards;
    private int knightsPlayed;
    private int longestRoadLength;
    private final Map<SpecialCardType, Boolean> specialCards;
    private PlayerStrategy strategy;

    public BotPlayer(String name, PlayerColor color, PlayerStrategy strategy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or blank");
        }
        if (color == null) {
            throw new IllegalArgumentException("Player color cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null for a bot player");
        }
        this.name = name;
        this.color = color;
        this.strategy = strategy;
        this.resources = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            this.resources.put(type, 0);
        }
        this.handCards = new ArrayList<>();
        this.knightsPlayed = 0;
        this.longestRoadLength = 0;
        this.specialCards = new EnumMap<>(SpecialCardType.class);
        for (SpecialCardType type : SpecialCardType.values()) {
            this.specialCards.put(type, false);
        }
    }


    public void executeTurn(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        strategy.takeTurn(state);
    }

    public PlayerStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(PlayerStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerColor getColor() {
        return color;
    }

    @Override
    public int getResourceCount(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    @Override
    public int getTotalResourceCount() {
        return resources.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public void addResource(ResourceType type, int amount) {
        validateAmount(amount);
        if (type == null) {
            throw new IllegalArgumentException("Resource type cannot be null");
        }
        resources.put(type, resources.getOrDefault(type, 0) + amount);
    }

    @Override
    public void removeResource(ResourceType type, int amount) {
        validateAmount(amount);
        if (type == null) {
            throw new IllegalArgumentException("Resource type cannot be null");
        }
        int current = resources.getOrDefault(type, 0);
        if (current < amount) {
            throw new IllegalArgumentException(
                String.format("Cannot remove %d %s. Only have %d", amount, type, current)
            );
        }
        resources.put(type, current - amount);
    }

    @Override
    public boolean hasResource(ResourceType type, int amount) {
        if (amount < 0 || type == null) {
            return false;
        }
        return resources.getOrDefault(type, 0) >= amount;
    }

    @Override
    public List<ExperimentCard> getHandCards() {
        return Collections.unmodifiableList(handCards);
    }

    @Override
    public void addCard(ExperimentCard card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        handCards.add(card);
    }

    @Override
    public void removeCard(ExperimentCard card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        if (!handCards.remove(card)) {
            throw new IllegalArgumentException("Card not found in hand: " + card.getCardName());
        }
    }

    @Override
    public int getKnightsPlayed() {
        return knightsPlayed;
    }

    @Override
    public void incrementKnightsPlayed() {
        this.knightsPlayed++;
    }

    @Override
    public int getLongestRoadLength() {
        return longestRoadLength;
    }

    @Override
    public void setLongestRoadLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Road length cannot be negative");
        }
        this.longestRoadLength = length;
    }

    @Override
    public boolean hasSpecialCard(SpecialCardType type) {
        return specialCards.getOrDefault(type, false);
    }

    @Override
    public void setSpecialCard(SpecialCardType type, boolean owned) {
        if (type == null) {
            throw new IllegalArgumentException("Special card type cannot be null");
        }
        specialCards.put(type, owned);
    }

    @Override
    public boolean isBot() {
        return true;
    }

    private void validateAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
    }

    @Override
    public String toString() {
        return String.format("BotPlayer[name=%s, color=%s, resources=%d, cards=%d]",
            name, color, getTotalResourceCount(), handCards.size());
    }
}

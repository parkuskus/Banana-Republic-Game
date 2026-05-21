package banana.republic.player;

import java.util.List;

import banana.republic.card.ExperimentCard;
import banana.republic.resource.ResourceType;


public interface Player {

    String getName();

    PlayerColor getColor();

    int getResourceCount(ResourceType type);

    int getTotalResourceCount();

    void addResource(ResourceType type, int amount);

    void removeResource(ResourceType type, int amount);

    boolean hasResource(ResourceType type, int amount);

    List<ExperimentCard> getHandCards();

    void addCard(ExperimentCard card);

    void removeCard(ExperimentCard card);

    int getKnightsPlayed();

    void incrementKnightsPlayed();

    int getLongestRoadLength();

    void setLongestRoadLength(int length);

    boolean hasSpecialCard(SpecialCardType type);

    void setSpecialCard(SpecialCardType type, boolean owned);

    boolean isBot();
}

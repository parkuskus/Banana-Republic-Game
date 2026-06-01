package banana.republic.ui;

import banana.republic.core.Game;

/**
 * Marker interface untuk dialog controller yang membutuhkan akses ke Game instance.
 */
public interface GameAwareController {
    void setGame(Game game);
}

package banana.republic.ui.presenter;

import java.util.List;

import banana.republic.core.Game;
import banana.republic.core.LogEntry;
import javafx.scene.control.Label;

public class LogbookPresenter {

    private final List<Label> logLabels;

    public LogbookPresenter(List<Label> logLabels) {
        this.logLabels = logLabels;
    }

    public void render(Game game) {
        if (game == null || logLabels == null) return;
        List<LogEntry> entries = game.getGameLog().getRecentEntries(logLabels.size());
        for (int i = 0; i < logLabels.size(); i++) {
            Label label = logLabels.get(i);
            if (label == null) continue;
            if (i < entries.size()) {
                label.setText("• " + entries.get(entries.size() - 1 - i).getMessage());
            } else {
                label.setText("");
            }
        }
    }
}

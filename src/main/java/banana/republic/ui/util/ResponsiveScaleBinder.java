package banana.republic.ui.util;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class ResponsiveScaleBinder {

    public void bindToParent(Group group) {
        if (group == null) return;
        Node parent = group.getParent();
        if (!(parent instanceof Region container)) return;
        double baseScaleX = group.getScaleX();
        double baseScaleY = group.getScaleY();

        Runnable recompute = () -> {
            double mapW = group.getLayoutBounds().getWidth();
            double mapH = group.getLayoutBounds().getHeight();
            double availW = container.getWidth();
            double availH = container.getHeight();
            if (mapW <= 0 || mapH <= 0 || availW <= 0 || availH <= 0) return;
            double fitScale = Math.min(availW / (mapW * baseScaleX), availH / (mapH * baseScaleY));
            fitScale = Math.min(fitScale, 1.0);
            group.setScaleX(baseScaleX * fitScale);
            group.setScaleY(baseScaleY * fitScale);
        };

        Platform.runLater(recompute);
        container.widthProperty().addListener((obs, oldValue, newValue) -> recompute.run());
        container.heightProperty().addListener((obs, oldValue, newValue) -> recompute.run());
        group.layoutBoundsProperty().addListener((obs, oldValue, newValue) -> recompute.run());
    }
}

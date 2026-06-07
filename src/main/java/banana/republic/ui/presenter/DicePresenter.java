package banana.republic.ui.presenter;

import banana.republic.dice.DiceResult;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DicePresenter {

    private final ImageView diceImage1;
    private final ImageView diceImage2;
    private final Image[] diceImages;

    public DicePresenter(ImageView diceImage1, ImageView diceImage2, Image[] diceImages) {
        this.diceImage1 = diceImage1;
        this.diceImage2 = diceImage2;
        this.diceImages = diceImages;
    }

    public void show(DiceResult result) {
        if (result == null || diceImage1 == null || diceImage2 == null || diceImages == null) return;
        int d1 = result.getDie1();
        int d2 = result.getDie2();
        if (d1 >= 1 && d1 <= 6 && diceImages[d1 - 1] != null) {
            diceImage1.setImage(diceImages[d1 - 1]);
        }
        if (d2 >= 1 && d2 <= 6 && diceImages[d2 - 1] != null) {
            diceImage2.setImage(diceImages[d2 - 1]);
        }
    }
}

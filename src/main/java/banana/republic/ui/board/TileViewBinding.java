package banana.republic.ui.board;

import banana.republic.board.HexTile;
import javafx.scene.layout.StackPane;

public record TileViewBinding(StackPane view, HexTile tile) {
}

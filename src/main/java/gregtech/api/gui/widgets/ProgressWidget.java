package gregtech.api.gui.widgets;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import net.minecraft.network.PacketBuffer;

import java.util.function.DoubleSupplier;

public class ProgressWidget<T extends IUIHolder> extends Widget<T> {

    public enum MoveType {
        VERTICAL,
        HORIZONTAL
    }

    public final DoubleSupplier progressSupplier;
    private final int x, y, width, height;

    private MoveType moveType;
    private TextureArea emptyBarArea;
    private TextureArea filledBarArea;

    private double lastProgressValue;

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(SLOT_DRAW_PRIORITY + 200);
        this.progressSupplier = progressSupplier;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ProgressWidget<T> setProgressBar(TextureArea emptyBarArea, TextureArea filledBarArea, MoveType moveType) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = filledBarArea;
        this.moveType = moveType;
        return this;
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if(emptyBarArea != null) {
            emptyBarArea.draw(x, y, width, height);
        }
        if(filledBarArea != null) {
            if(moveType == MoveType.HORIZONTAL) {
                filledBarArea.drawSubArea(x, y, (int) (width * lastProgressValue), height, lastProgressValue, 1.0);
            } else if(moveType == MoveType.VERTICAL) {
                filledBarArea.drawSubArea(x, y, width, (int) (height * lastProgressValue), 1.0, lastProgressValue);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        double actualValue = progressSupplier.getAsDouble();
        //todo check if given epsilon is enough for long recipes
        if(Math.abs(actualValue - lastProgressValue) < 0.00001) {
            this.lastProgressValue = actualValue;
            writeUpdateInfo(0, buffer -> buffer.writeDouble(actualValue));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if(id == 0) {
            this.lastProgressValue = buffer.readDouble();
        }
    }
}
package guis;

import guis.GuiTexture;

import java.util.List;

/**
 * Created by jamesraynor on 10/31/15.
 */
public interface IButton {
    void onClick();

    void whileHover();

    void startHover();

    void stopHover();

    void checkHover();

    void playHoverAnimation(float scaleFactor);

    void playerClickAnimation(float scaleFactor);

    void hide(List<GuiTexture> guiTextures);

    void show(List<GuiTexture> guiTextures);

    void reopen(List<GuiTexture> guiTextures);
}

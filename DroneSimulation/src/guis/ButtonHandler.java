package guis;

import guis.GuiTexture;
import renderEngine.Loader;

import java.util.HashMap;
import java.util.List;

public class ButtonHandler {
    private Loader loader;
    private List<GuiTexture> guiTextureList;

    public ButtonHandler(Loader loader, List<GuiTexture> guiList) {
        this.loader = loader;
        this.guiTextureList = guiList;
    }


    private HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();

    public void update() {
        for (Button button : buttons.values()) button.checkHover();
    }


    public void registerButton(int id, Button button) {
        if (!buttons.containsKey(button)) {
            buttons.put(id, button);
        }
    }

    public List<GuiTexture> getGuiTextureList() {
        return guiTextureList;
    }

    public HashMap<Integer, Button> getButtons() {
        return buttons;
    }

    public Button getButton(int id) {
        return buttons.get(id);
    }

    public Loader getLoader() {
        return loader;
    }
}

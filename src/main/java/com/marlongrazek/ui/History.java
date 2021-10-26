package com.marlongrazek.ui;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class History {

    private final ArrayList<UI.Page> history = new ArrayList<>();
    private final Player player;

    public History(Player player) {
        this.player = player;
    }

    public void addPage(UI.Page page) {
        history.add(page);
    }

    public void removePage(int index) {
        history.remove(history.size() - index);
    }

    public void clear() {
        history.clear();
    }

    public UI.Page getPage(int index) {
        return history.get(history.size() - (index + 1));
    }

    public ArrayList<UI.Page> list() {
        return history;
    }

    public void openPage(int index) {
        if (getPage(index) != null) {
            UI.Page page = getPage(index);
            for (int i = 0; i < index; i++) history.remove(history.size() - 1);
            page.open(player);
        } else player.closeInventory();
    }
}

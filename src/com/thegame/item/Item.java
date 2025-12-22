package com.thegame.item;
import com.thegame.character.Character;

public interface Item {
    //获取道具名称
    String getName();
    //使用道具
    void use(Character user);
}

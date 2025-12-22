package com.thegame.item;
import com.thegame.character.Character;
import com.thegame.character.Player;

public class ManaPotion implements Item {
    // 固定恢复法力值
    private static final int MP_RECOVER = 10;

    @Override
    public String getName() {
        return "法力药水";
    }

    @Override
    public void use(Character user) {
        // 仅玩家有MP属性，强转校验
        if (user instanceof Player) {
            Player player = (Player) user;
            int newMp = player.getMp() + MP_RECOVER;
            player.setMp(newMp);
            System.out.printf("[道具效果] 使用%s!恢复%d点法力值,当前法力值:%d%n",getName(), MP_RECOVER, player.getMp());
        } else {
            System.out.println("[道具效果] 该道具仅玩家可使用！");
        }
    }
}
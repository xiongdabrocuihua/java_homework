package com.thegame.item;

import com.thegame.character.Character;
import com.thegame.character.Player;

public class DefensePotion implements Item {
    // 临时防御值
    private static final int TEMP_DEFENSE = 3;
    // 持续回合数
    private static final int DURATION_TURNS = 2;

    @Override
    public String getName() {
        return "防御药水";
    }

    @Override
    public void use(Character user) {
        // 仅玩家可使用，强转并设置临时防御
        if (user instanceof Player) {
            Player player = (Player) user;
            player.setTempDefense(TEMP_DEFENSE);
            player.setTempDefenseTurns(DURATION_TURNS);
            System.out.printf("[道具效果] 使用%s！临时提升%d点防御，持续%d回合！%n", 
                    getName(), TEMP_DEFENSE, DURATION_TURNS);
        } else {
            System.out.println("[道具效果] 该道具仅玩家可使用！");
        }
    }
}
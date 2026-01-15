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
            
            // 检查是否满魔法
            if (player.getMp() >= player.getMaxMp()) {
                System.out.printf("[道具效果] 使用%s!但你已经是满魔法状态，没有恢复任何法力值。\n", getName());
                return;
            }
            
            // 计算实际恢复的法力值（不能超过最大值）
            int currentMp = player.getMp();
            int actualRecover = Math.min(MP_RECOVER, player.getMaxMp() - currentMp);
            
            // 恢复法力值
            int newMp = currentMp + actualRecover;
            player.setMp(newMp);
            
            // 显示实际恢复的法力值
            System.out.printf("[道具效果] 使用%s!恢复%d点法力值,当前法力值:%d\n",getName(), actualRecover, player.getMp());
        } else {
            System.out.println("[道具效果] 该道具仅玩家可使用！");
        }
    }
}
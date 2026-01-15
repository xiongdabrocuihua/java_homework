package com.thegame.item;
import com.thegame.character.Character;

public class HealthPotion implements Item{
    // 固定恢复生命值
    private static final int HP_RECOVER = 15;

    @Override
    public String getName(){
        return "生命药水";
    }

    @Override
    public void use(Character user){
        // 检查是否满血
        if (user.getHp() >= user.getMaxHp()) {
            System.out.printf("[道具效果] 使用%s!但你已经是满血状态，没有恢复任何生命值。\n", getName());
            return;
        }
        
        // 计算实际恢复的生命值（不能超过最大值）
        int currentHp = user.getHp();
        int actualRecover = Math.min(HP_RECOVER, user.getMaxHp() - currentHp);
        
        // 恢复生命值
        int newHp = currentHp + actualRecover;
        user.setHp(newHp);
        
        // 显示实际恢复的生命值
        System.out.printf("[道具效果] 使用%s!恢复%d点生命值,当前生命值:%d\n", 
                getName(), actualRecover, user.getHp());
    }
}

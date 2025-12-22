package com.thegame.character;

import java.util.Random;

/**
 * 守护骑士:钟楼废墟小Boss,20%概率盾击（眩晕，玩家本回合无法行动）
 */
public class GuardianKnight extends Enemy {
    private static final Random random = new Random();
    // 标记是否触发盾击（用于战斗引擎判断）
    private boolean shieldStun;

    //hp85,atk8,def5
    public GuardianKnight(String name, int hp, int mp, int attack, int defense) {
        super(name, hp, mp, attack, defense);
        this.shieldStun = false;
    }

    @Override
    public void takeAction(Character target) {
        shieldStun = false;
        int randomNum = random.nextInt(100);
        if (randomNum < 20) {
            // 盾击：眩晕玩家，造成普通伤害
            shieldStun = true;
            System.out.printf("%s发动盾击!你被眩晕,本回合无法行动!%n", getName());
            attack(target);
        } else if(randomNum < 40){
            System.out.println("守护骑士发动重斩！");
            int slashDmg = 15; // 按要求：重斩造成15点伤害
            target.takeDamage(slashDmg);
            System.out.printf("你受到%d点重斩伤害!当前HP:%d%n", slashDmg, target.getHp());
        }
        
        else {
            // 普通攻击（高伤害）
            System.out.printf("%s挥舞长剑猛攻!%n", getName()); 
            attack(target);
        }
    }

    public boolean isShieldStun() { return shieldStun; }
}
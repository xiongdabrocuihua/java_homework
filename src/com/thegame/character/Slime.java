package com.thegame.character;

import java.util.Random;

// 史莱姆类，继承Enemy
public class Slime extends Enemy {
    private static final Random random = new Random();

    public Slime(String name, int hp, int mp, int attack, int defense) {
        super(name, hp, mp, attack, defense);
    }

    @Override
    public void takeAction(Character target) {
        // 史莱姆的行动逻辑（如50%普攻、10%毒击）
        int randomNum = random.nextInt(100);
        if (randomNum < 30) { // 30%概率触发毒击
            System.out.println("史莱姆发动毒击！你中毒了！");
            // 中毒效果：每回合5点伤害，持续3回合
            target.setPoisonDmg(5);
            target.setPoisonTurns(3);
            System.out.printf("%s每回合将受到5点伤害,持续3回合!%n", target.getName());
        } else { // 70%概率普通攻击
            attack(target);
        }
    }
}
package com.thegame.character;



/**
 * 镜影召唤的小镜像：HP30/攻击5/防御1，无特殊技能
 */
public class MirrorImage extends Enemy {
    public MirrorImage() {
        super("镜影分身", 30, 0, 5, 1); // 按要求：HP30，攻击5，防御1
    }

    @Override
    public void takeAction(Character target) {
        // 普通攻击
        System.out.printf("%s挥出暗影爪!%n", getName());
        attack(target);
    }
}
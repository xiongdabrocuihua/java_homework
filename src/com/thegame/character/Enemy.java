package com.thegame.character;

import java.util.Random;

public abstract class Enemy extends Character{

    public Enemy(String name, int hp, int mp, int attack, int defense){
        super(name,hp,mp,attack,defense);
    }
    
    /**
     * 怪物行动逻辑：随机选择普攻/技能
     * @param target 攻击目标（玩家）
     */
    //抽象方法，强制子类实现行动 技能逻辑
    public abstract void takeAction(Character target);

    // 基础方法（确保技能中能正常调用）
    @Override
    public boolean isAlive() {
        return getHp() > 0;
    }

    @Override
    public void takeDamage(int damage) {
        // 防御抵消部分伤害（技能固定伤害除外）
        int finalDmg = Math.max(damage - getDefense(), 1); // 至少1点伤害
        setHp(getHp() - finalDmg);
        System.out.printf("[伤害] %s受到%d点伤害!剩余HP:%d%n", getName(), finalDmg, getHp());
    }

    // 普通攻击（供子类技能外的基础攻击调用）
    public void attack(Character target) {
        int damage = getAttack();
        target.takeDamage(damage);
        System.out.printf("[攻击] %s发起普通攻击!%n", 
                getName());
    }

    //显示敌人状态
    @Override
    public void displayStatus(){
        System.out.printf("=== 敌人状态 ===%n");
        System.out.printf("名称：%s%n",getName());
        System.out.printf("生命值：%d%n",getHp());
        //System.out.printf("法力值：%d%n",getMp());
        System.out.printf("攻击力：%d | 防御力：%d%n", getAttack(), getDefense());
        if (getPoisonTurns() > 0) {
            System.out.printf("中毒状态：每回合%d点伤害,剩余%d回合%n", 
                    getPoisonDmg(), getPoisonTurns());
        }
        System.out.println("====================");
    }
}

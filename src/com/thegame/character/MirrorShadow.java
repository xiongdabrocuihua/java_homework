package com.thegame.character;

import java.util.Random;

import java.util.List;
import java.util.ArrayList;

/**
 * 镜影：镜湖最终Boss，30%概率镜像复制（模仿玩家攻击），20%概率吸血（伤害转化为自身HP）
 */
public class MirrorShadow extends Enemy {
    private static final Random random = new Random();
    private List<Enemy> mirrorImages; // 存储召唤的镜像

    public MirrorShadow(String name, int hp, int mp, int attack, int defense) {
        super(name, hp, mp, attack, defense);
        this.mirrorImages = new ArrayList<>();
    }

    @Override
    public void takeAction(Character target) {
         // 先处理已召唤的镜像攻击
        if (!mirrorImages.isEmpty()) {
            System.out.println("\n[镜影] 我的分身会撕碎你！");
            for (Enemy mirror : mirrorImages) {
                if (mirror.isAlive()) {
                    mirror.takeAction(target);
                }else {
                    mirrorImages.remove(mirror); // 移除死亡镜像
                }
            }
        }
        if (!isAlive()) return;//仅本体存活时触发
        int randomNum = random.nextInt(100);
        if (randomNum < 30) {
            // 暗影冲击（固定20点伤害）
            System.out.println("你无法逃离自己的影子！");
            System.out.println("镜影发动暗影冲击!");
            int shadowDmg = 20; // 按要求：暗影冲击造成20点伤害
            target.takeDamage(shadowDmg);
        } else if (randomNum < 50) {
            // 镜像复制（召唤1个小镜像）
            System.out.println("看看这就是你的懦弱！");
            System.out.println("镜影发动镜像复制！召唤出分身！");
            MirrorImage newMirror = new MirrorImage();
            mirrorImages.add(newMirror);
            System.out.printf("镜像%s出现了!当前HP:%d%n", newMirror.getName(), newMirror.getHp());  
        } else if (randomNum < 70) {
            // 保留镜像复制（模仿玩家攻击）
            System.out.println("这是你曾经的招式！");
            System.out.println("镜影复制了你的攻击！");
            int copyDmg = target.getAttack();
            target.takeDamage(copyDmg);
        } else {
            // 吸血攻击（保留，调整伤害为自身攻击力）
            System.out.println("你的生命将成为我的养分！");
            System.out.println("镜影发动吸血攻击！");
            int damage = getAttack();
            target.takeDamage(damage);
            setHp(getHp() + damage - target.getDefense()); // 吸血：伤害=回血-防御
            System.out.printf("镜影恢复%d点HP,当前HP:%d%n", damage-target.getDefense(), getHp());
        }
    }
    // 获取镜像列表（供战斗引擎判断）
    public List<Enemy> getMirrorImages() {
        return mirrorImages;
    }

    // 清空镜像（战斗结束后）
    public void clearMirrors() {
        mirrorImages.clear();
    }
}
package com.thegame.battle;

import com.thegame.character.Character;
import com.thegame.character.Enemy;

public class PoisonStrikeSkill implements Skill{
    // 持续伤害值
    private static final int POISON_DMG = 5;
    // 持续回合数
    private static final int POISON_TURNS = 2;
    
    @Override
    public String getName() {
        return "毒击";
    }

    @Override
    public boolean apply(Character caster, Character target) {
        // 1. 仅怪物可释放
        if (!(caster instanceof Enemy)) {
            System.out.println("仅怪物可释放毒击！");
            return false;
        }

        // 2. 触发中毒效果
        target.setPoisonDmg(POISON_DMG);
        target.setPoisonTurns(POISON_TURNS);

        // 3. 输出技能日志
        System.out.printf("%s释放%s!%n", caster.getName(), getName());
        System.out.printf("%s中剧毒!每回合受到%d点伤害,持续%d回合%n", 
                target.getName(), POISON_DMG, POISON_TURNS);
        return true;
    }
}

package com.thegame.battle;
//治疗术:消耗5mp，回10点生命值

import com.thegame.character.Character;
import com.thegame.character.Player;

public class HealSkill implements Skill{
    // 技能消耗MP
    private static final int MP_COST = 5;
    // 恢复生命值
    private static final int HP_RECOVER = 10;

    @Override
    public String getName() {
        return "治疗术";
    }

    @Override
    public boolean apply(Character caster, Character target){
        //1.仅玩家可释放，检查mp是否足够
        if(!(caster instanceof Player)){//判断施法者是不是玩家
            System.out.println("只有玩家才能释放治疗术！");
            return false;
        }
        Player player = (Player) caster;
        if(player.getMp() < MP_COST){
            System.out.printf("[技能] 法力值不足！治疗术需要%d点MP,当前仅%d点%n",MP_COST, player.getMp());
            return false;
        }

        //2.消耗mp
        player.setMp(player.getMp() - MP_COST);

        // 3. 恢复生命值（目标为自己）
        int newHp = caster.getHp() + HP_RECOVER;
        caster.setHp(newHp);

        // 4. 输出技能日志
        System.out.printf("%s释放%s!消耗%d点MP%n", caster.getName(), getName(), MP_COST);
        System.out.printf("恢复%d点生命值!当前生命值:%d%n", 
                HP_RECOVER, caster.getHp());
        return true;
    }
}

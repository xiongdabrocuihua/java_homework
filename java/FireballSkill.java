import java.util.Random;

//火球术:消耗8mp,造成11-15随机伤害
public class FireballSkill implements Skill{
    //技能消耗mp
    private static final int MP_COST = 8;
    //随机伤害范围
    private static final int MIN_DMG = 11;
    private static final int MAX_DMG = 15;
    private static final Random random = new Random();

    @Override
    public String getName() {
        return "火球术";
    }

    @Override
    public boolean apply(Character caster, Character target){
        //1.仅玩家可释放，检查mp是否足够
        if(!(caster instanceof Player)){//判断施法者是不是玩家
            System.out.println("只有玩家才能释放火球术！");
            return false;
        }
        Player player = (Player) caster;//将施法者转换为玩家
        if (player.getMp() < MP_COST) {//检查玩家mp是否足够
            System.out.printf("[技能] 法力值不足！火球术需要%d点MP,当前仅%d点%n",MP_COST, player.getMp());
            return false;
        }

        //2.消耗mp
        player.setMp(player.getMp() - MP_COST);

        //3.计算随机伤害（11-15）
        int damage = random.nextInt(MAX_DMG - MIN_DMG + 1) + MIN_DMG;
        //最终伤害 = 技能伤害 - 目标防御（最低1点）
        int actualDmg = Math.max(1, damage - target.getDefense());
        target.setHp(Math.max(0, target.getHp() - actualDmg));

        //4.输出技能日志
        System.out.printf("%s释放%s!消耗%d点MP%n",caster.getName(), getName(), MP_COST);
        System.out.printf("%s受到%d点火焰伤害!剩余生命值:%d%n",target.getName(), actualDmg, target.getHp());
        return true;
    }
}

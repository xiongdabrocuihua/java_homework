import java.util.Random;

public class Enemy extends Character{
    private static final Random random = new Random();
    private Skill poisonStrike;

    public Enemy(String name, int hp, int mp, int attack, int defense){
        super(name,hp,mp,attack,defense);
        this.poisonStrike = new PoisonStrikeSkill();
    }
    
    /**
     * 怪物行动逻辑：随机选择普攻/技能
     * @param target 攻击目标（玩家）
     */
    //默认是史莱姆，每个怪物都会重写攻击行为实现特殊化
    public void takeAction(Character target) {
        // 生成0-99的随机数，判断行动类型
        int randomNum = random.nextInt(100);
        if (randomNum < 90) {
            // 50%概率：普通攻击
            attack(target);
        } else {
            // 10%概率：毒击技能
            poisonStrike.apply(this, target);
        }
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

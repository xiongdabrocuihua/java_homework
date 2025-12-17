import java.util.Random;

/**
 * 哥布林：迷雾森林/低语峡谷怪物，5%概率触发偷袭（双倍伤害）
 */
public class Goblin extends Enemy {
    private static final Random random = new Random();

    public Goblin(String name, int hp, int mp, int attack, int defense) {
        super(name, hp, mp, attack, defense);
    }

    @Override
    public void takeAction(Character target) {
        int randomNum = random.nextInt(100);
        if (randomNum < 25) { // 25%概率偷袭（双倍伤害）
            System.out.println("哥布林发动偷袭！");
            int sneakDmg = getAttack() * 2;
            target.takeDamage(sneakDmg);
            System.out.printf("你受到%d点偷袭伤害!当前HP:%d%n", sneakDmg, target.getHp());
        } else { // 75%概率普通攻击
            attack(target);
        }
    }
}
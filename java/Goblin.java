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
        // 5%概率偷袭（双倍伤害），95%普通攻击
        if (random.nextInt(100) < 5) {
            System.out.printf("%s发动偷袭!%n", getName());
            int doubleDmg = getAttack() * 2;
            target.takeDamage(doubleDmg);
        } else {
            attack(target);
        }
    }
}
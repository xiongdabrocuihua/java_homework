import java.util.Random;

/**
 * 镜影：镜湖最终Boss，30%概率镜像复制（模仿玩家攻击），20%概率吸血（伤害转化为自身HP）
 */
public class MirrorShadow extends Enemy {
    private static final Random random = new Random();

    public MirrorShadow(String name, int hp, int mp, int attack, int defense) {
        super(name, hp, mp, attack, defense);
    }

    @Override
    public void takeAction(Character target) {
        int randomNum = random.nextInt(100);
        if (randomNum < 30) {
            // 镜像复制：造成玩家攻击力的伤害
            System.out.printf("[技能] %s复制了你的攻击！%n", getName());
            int copyDmg = target.getAttack();
            target.takeDamage(copyDmg);
        } else if (randomNum < 50) {
            // 吸血攻击：造成伤害并恢复自身HP
            System.out.printf("[技能] %s发动吸血攻击！%n", getName());
            int damage = getAttack();
            target.takeDamage(damage);
            setHp(getHp() + damage); // 吸血：伤害=回血
            System.out.printf("[吸血] %s恢复%d点HP，当前HP：%d%n", getName(), damage, getHp());
        } else {
            // 普通攻击（超高伤害）
            attack(target);
        }
    }
}
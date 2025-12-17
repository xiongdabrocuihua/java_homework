import java.util.Random;

// 史莱姆类，继承Enemy
public class Slime extends Enemy {
    private static final Random random = new Random();

    public Slime(String name, int hp, int mp, int attack, int defense) {
        super(name, hp, mp, attack, defense);
    }

    @Override
    public void takeAction(Character target) {
        // 史莱姆的行动逻辑（如50%普攻、10%毒击）
        int randomNum = random.nextInt(100);
        if (randomNum < 50) {
            attack(target);
        } else if (randomNum < 60) {
            // 假设已定义PoisonStrikeSkill
            new PoisonStrikeSkill().apply(this, target);
        } else {
            attack(target);
        }
    }
}
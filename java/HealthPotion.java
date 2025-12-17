public class HealthPotion implements Item{
    // 固定恢复生命值
    private static final int HP_RECOVER = 15;

    @Override
    public String getName(){
        return "生命药水";
    }

    @Override
    public void use(Character user){
        // 恢复生命值（保证不低于0）
        int newHp = user.getHp() + HP_RECOVER;
        user.setHp(newHp);
        System.out.printf("[道具效果] 使用%s!恢复%d点生命值,当前生命值:%d%n", 
                getName(), HP_RECOVER, user.getHp());
    }
}

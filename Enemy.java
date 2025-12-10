public class Enemy extends Character{
    public Enemy(String name, int hp, int mp, int attack, int defense){
        super(name,hp,mp,attack,defense);
    }
    
    //显示敌人状态
    @Override
    public void displayStatus(){
        System.out.printf("=== 敌人状态 ===%n");
        System.out.printf("名称：%s%n",getName());
        System.out.printf("生命值：%d%n",getHp());
        //System.out.printf("法力值：%d%n",getMp());
        System.out.printf("攻击力：%d | 防御力：%d%n", getAttack(), getDefense());
    }
}

package com.thegame.character;
public abstract class Character {
    private String name;//名字
    private int hp;//生命值
    private int mp;//法力值
    private int attack;//攻击
    private int defense;//防御
    private int maxHp; // 最大生命值
    private int maxMp; // 最大法力值
    private int poisonDmg;//每回合中毒伤害

    private int poisonTurns;//剩余中毒回合数

    //初始化角色属性
    public Character(String name, int hp, int mp, int attack, int defense){
        this.name = name;
        this.hp = Math.max(0,hp);
        this.mp = Math.max(0,mp);
        this.attack = Math.max(1,attack);
        this.defense = Math.max(0,defense);
        this.maxHp = this.hp;
        this.maxMp = this.mp;
        this.poisonDmg = 0;
        this.poisonTurns = 0;
    }

    //处理每回合中毒伤害
    public void takePoisonDamage(){
        if(poisonTurns>0){
            int newHp = hp - poisonDmg;
            setHp(newHp); // 使用setHp方法，确保不低于0且不高于maxHp
            System.out.printf("%s受到%d点毒伤!剩余生命值:%d%n",name, poisonDmg, hp);
            poisonTurns--;
            // 中毒回合结束，重置中毒状态
            if (poisonTurns == 0) {
                poisonDmg = 0;
                System.out.printf("%s的中毒效果消失!%n", name);
            } else {
                System.out.printf("%s的中毒剩余%d回合%n", name, poisonTurns);
            }
        }
    }


    //判断是否存活
    public boolean isAlive(){
        return this.hp > 0;
    }

    //角色受到伤害
    public void takeDamage(int dmg){
        int actualDmg = Math.max(1,dmg - this.defense);
        this.hp = Math.max(0, this.hp - actualDmg);
        System.out.printf("%s受到了%d点伤害,当前生命值为:%d%n", this.name, actualDmg, this.hp);
    }

    //角色发起攻击
    public void attack(Character target){
        if (this.isAlive() && target.isAlive()){//只有双方存活时才能攻击
            System.out.printf("%s 向 %s 发起攻击！%n",this.name, target.getName());
            target.takeDamage(this.attack);
        } else if (!this.isAlive()){
            System.out.printf("%s 已经死亡，无法攻击！%n",this.name);
        } else if (!target.isAlive()){
            System.out.printf("%s 已经死亡，无需攻击！%n", target.getName());
        }
    }

    //显示角色状态
    public abstract void displayStatus();

    //获取名字
    public String getName(){
        return name;
    }

    //设置名字
    public void setName(String name){
        this.name = name;
    }

    //获取生命值
    public int getHp(){
        return hp;
    }

    //设置生命值
    public void setHp(int hp){
        // 确保生命值不小于0且不大于最大生命值
        this.hp = Math.max(0, Math.min(hp, this.maxHp));
    }

    //获取法力值
    public int getMp(){
        return mp;
    }

    //设置法力值
    public void setMp(int mp){
        // 确保法力值不小于0且不大于最大法力值
        this.mp = Math.max(0, Math.min(mp, this.maxMp));
    }

    //获取攻击值
    public int getAttack(){
        return attack;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    //设置攻击值
    public void setAttack(int attack){
        this.attack = Math.max(1,attack);
    }

    //获取防御值
    public int getDefense(){
        return defense;
    }

    //设置防御值
    public void setDefense(int defense){
        this.defense = Math.max(0,defense);
    }
    
    //获取中毒伤害
    public int getPoisonDmg(){
        return poisonDmg;
    }
    
    //设置中毒伤害
    public void setPoisonDmg(int poisonDmg){
        this.poisonDmg = Math.max(0,poisonDmg);
    }
    
    //获取中毒回合数
    public int getPoisonTurns(){
        return poisonTurns;
    }
    
    //设置中毒回合数
    public void setPoisonTurns(int poisonTurns){
        this.poisonTurns = Math.max(0,poisonTurns);
    }
}
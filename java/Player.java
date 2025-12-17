import java.util.ArrayList;
import java.util.List;


public class Player extends Character{
    //法术消耗mp
    private static final int SPELL_MP_COST = 5;
    // 背包属性
    private Inventory inventory;
    // 临时防御（防御药水效果）
    private int tempDefense;
    // 临时防御剩余回合
    private int tempDefenseTurns;

    private List<Skill> skills;

    // ===== 战斗前快照存储 =====
    private int preBattleHp;       // 战斗前HP
    private int preBattleMp;       // 战斗前MP
    private int preBattlePoisonTurns; // 战斗前中毒回合
    private int preBattlePoisonDmg;   // 战斗前中毒伤害
    private int preBattleTempDefense; // 战斗前临时防御
    private int preBattleTempDefenseTurns; // 战斗前临时防御回合
    private List<Item> preBattleInventory; // 战斗前道具栏（深拷贝）

    //初始化
    public Player(String name, int hp, int mp, int attack, int defense){
        super(name,hp,mp,attack,defense);
        this.inventory = new Inventory(); // 初始化背包
        this.tempDefense = 0;
        this.tempDefenseTurns = 0;
        this.skills = new ArrayList<>();
        skills.add(new FireballSkill());
        skills.add(new HealSkill());
        this.preBattleInventory = new ArrayList<>();
        System.out.printf("%s掌握了「%s」和「%s」!%n", name, skills.get(0).getName(), skills.get(1).getName());
    }

    // ===== 保存战斗前的状态快照 =====
    public void savePreBattleState() {
        // 1. 保存基础属性
        this.preBattleHp = getHp();
        this.preBattleMp = getMp();
        // 2. 保存负面状态
        this.preBattlePoisonTurns = getPoisonTurns();
        this.preBattlePoisonDmg = getPoisonDmg();
        // 3. 保存临时属性
        this.preBattleTempDefense = tempDefense;
        this.preBattleTempDefenseTurns = tempDefenseTurns;
        // 4. 深拷贝道具栏（避免引用共享，确保恢复时是原道具）
        this.preBattleInventory.clear();
        for (Item item : inventory.getItems()) {
            // 假设Item是不可变类，直接复制；若Item有修改逻辑，需实现克隆方法
            this.preBattleInventory.add(item);
        }
        System.out.println("已保存战斗前状态！");
    }

    //展示技能列表
    public void displaySkills() {
        System.out.println("\n===== 你的技能 =====");
        for (int i = 0; i < skills.size(); i++) {
            System.out.printf("编号 %d → %s%n", i, skills.get(i).getName());
        }
        System.out.println("====================");
    }

    // ===== 新增：恢复到战斗前的状态（失败重试专用）=====
    public void restorePreBattleState() {
        // 1. 恢复基础属性
        setHp(preBattleHp);
        setMp(preBattleMp);
        // 2. 恢复负面状态
        setPoisonTurns(preBattlePoisonTurns);
        setPoisonDmg(preBattlePoisonDmg);
        // 3. 恢复临时属性
        this.tempDefense = preBattleTempDefense;
        this.tempDefenseTurns = preBattleTempDefenseTurns;
        // 4. 恢复道具栏
        inventory.clearItems();
        for (Item item : preBattleInventory) {
            inventory.addItem(item);
        }
        System.out.println("[重置] 已恢复到战斗开始前的状态！");
    }
    /**
     * 释放指定编号的技能
     * @param skillIndex 技能编号
     * @param target 技能目标（治疗术目标为自己，火球术目标为敌人）
     * @return true=释放成功，false=编号无效/释放失败
     */
    public boolean castSkill(int skillIndex, Character target) {
        // 1. 校验技能编号
        if (skillIndex < 0 || skillIndex >= skills.size()) {
            System.out.println("无效的技能编号！");
            return false;
        }

        // 2. 释放技能
        Skill skill = skills.get(skillIndex);
        return skill.apply(this, target);
    }


    //重写防御值，考虑临时防御
    @Override
    public int getDefense() {
        return super.getDefense() + tempDefense;
    }

    //显示玩家状态
    @Override
    public void displayStatus(){
        System.out.printf("=== 玩家状态 ===%n");
        System.out.printf("姓名：%s%n",getName());
        System.out.printf("生命值：%d%n",getHp());
        System.out.printf("法力值：%d%n",getMp());
        System.out.printf("攻击力：%d%n", getAttack());
        System.out.printf("基础防御：%d | 临时防御：%d | 总防御：%d%n",super.getDefense(), tempDefense, getDefense());
        // 显示中毒状态
        if (getPoisonTurns() > 0) {
            System.out.printf("中毒状态：每回合%d点伤害,剩余%d回合%n",getPoisonDmg(), getPoisonTurns());
        }
        System.out.println("====================");
    }

    //释放法术
    public boolean castSpell(Character target){
        if(getMp() < SPELL_MP_COST){
            System.out.printf("法力值不足！释放法术需要%d点MP,当前仅%d点%n",SPELL_MP_COST,getMp());
            return false;
        }

        // 消耗MP
        setMp(getMp() - SPELL_MP_COST);
        // 计算法术伤害（攻击力×1.5，最低1点）
        int spellDmg = (int) (getAttack() * 1.5);
        int actualDmg = Math.max(1, spellDmg - target.getDefense());

        // 执行法术攻击
        System.out.printf("%s 释放法术！消耗%d点MP 剩余%d点MP%n", getName(), SPELL_MP_COST, getMp());
        System.out.printf("%s 受到 %d 点法术伤害！剩余生命值：%d%n", target.getName(), actualDmg, Math.max(0, target.getHp() - actualDmg));
        target.setHp(Math.max(0, target.getHp() - actualDmg));
        return true;
    }

    //处理临时防御回合数提示
    public void reduceTempDefenseTurns() {
        if (tempDefenseTurns > 0) {
            tempDefenseTurns--;
            System.out.printf("临时防御剩余回合：%d%n", tempDefenseTurns);
            if (tempDefenseTurns == 0) {
                tempDefense = 0;
                System.out.println("防御药水效果消失，防御力恢复正常！");
            }
        }
    }

    public List<Skill> getSkills() {
        return skills;
    }

    // Getter & Setter
    public Inventory getInventory() {
        return inventory;
    }

    public int getTempDefense() {
        return tempDefense;
    }

    public void setTempDefense(int tempDefense) {
        this.tempDefense = tempDefense;
    }

    public int getTempDefenseTurns() {
        return tempDefenseTurns;
    }

    public void setTempDefenseTurns(int tempDefenseTurns) {
        this.tempDefenseTurns = tempDefenseTurns;
    }
}

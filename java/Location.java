import java.util.List;

/**
 * 场景类：定义每个地图场景的属性（名称、描述、怪物、战利品、概率等）
 */
public class Location {
    private String name;          // 场景名称（如黎明村）
    private String description;   // 场景描述
    private char sceneChar;       // 场景标识字符（如V=黎明村）
    private List<Class<? extends Enemy>> enemyTypes; // 该场景的怪物类型,限制类型为Enemy或其子类
    private List<Item> lootItems; // 场景随机战利品
    private double encounterRate; // 遇怪概率（0-1）
    private double trapRate;      // 陷阱概率（0-1）
    private int trapDamage;       // 陷阱伤害

    // 构造方法（安全区专用：无怪物/陷阱）
    public Location(String name, String description, char sceneChar, List<Item> lootItems) {
        this.name = name;
        this.description = description;
        this.sceneChar = sceneChar;
        this.enemyTypes = List.of(); // 空怪物列表
        this.lootItems = lootItems;
        this.encounterRate = 0.0;
        this.trapRate = 0.0;
        this.trapDamage = 0;
    }

    // 构造方法（战斗场景：有怪物/陷阱）
    public Location(String name, String description, char sceneChar, 
                    List<Class<? extends Enemy>> enemyTypes, List<Item> lootItems,
                    double encounterRate, double trapRate, int trapDamage) {
        this.name = name;
        this.description = description;
        this.sceneChar = sceneChar;
        this.enemyTypes = enemyTypes;
        this.lootItems = lootItems;
        this.encounterRate = encounterRate;
        this.trapRate = trapRate;
        this.trapDamage = trapDamage;
    }

    // Getter方法
    public String getName() { return name; }
    public String getDescription() { return description; }
    public char getSceneChar() { return sceneChar; }
    public List<Class<? extends Enemy>> getEnemyTypes() { return enemyTypes; }
    public List<Item> getLootItems() { return lootItems; }
    public double getEncounterRate() { return encounterRate; }
    public double getTrapRate() { return trapRate; }
    public int getTrapDamage() { return trapDamage; }
}
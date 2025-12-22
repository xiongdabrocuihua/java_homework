# java作业  

## 目录结构  
```
javahomework/
├── .gitignore
├── abc_tool.py
├── README.md
└── src/
    └── com/
        └── thegame/
            ├── battle/
            │   ├── FireballSkill.java
            │   ├── HealSkill.java
            │   ├── PoisonStrikeSkill.java
            │   └── Skill.java
            ├── character/
            │   ├── Character.java
            │   ├── Enemy.java
            │   ├── Goblin.java
            │   ├── GuardianKnight.java
            │   ├── MirrorImage.java
            │   ├── MirrorShadow.java
            │   ├── Player.java
            │   └── Slime.java
            ├── item/
            │   ├── DefensePotion.java
            │   ├── HealthPotion.java
            │   ├── Inventory.java
            │   ├── Item.java
            │   └── ManaPotion.java
            ├── main/
            │   └── NewGame.java
            └── map/
                ├── GameMap.java
                └── Location.java
```

## 包及类用途说明

### com.thegame.character
- **Character**：所有角色的基类，定义角色通用属性（姓名、HP、MP、攻击力、防御力、中毒状态等），并提供初始化方法。
- **Player**：玩家类，继承Character。包含背包系统、临时防御属性及相关方法；掌握技能（火球术、治疗术），提供技能释放、法术攻击等战斗相关方法；支持战斗前状态保存与恢复，用于战斗失败重试。
- **Enemy**：所有敌人的抽象基类，继承Character，定义敌人战斗行动的抽象方法`takeAction()`，强制子类实现具体战斗逻辑。
- **Goblin**：哥布林敌人类，继承Enemy。实现战斗逻辑：25%概率发动双倍伤害的偷袭，75%概率进行普通攻击。
- **GuardianKnight**：守护骑士（小Boss）类，继承Enemy。实现战斗逻辑：20%概率盾击（眩晕玩家）、20%概率重斩（固定15点伤害）、60%概率普通攻击，包含判断是否触发盾击的方法。
- **MirrorShadow**：镜影（最终Boss）类，继承Enemy。实现多阶段战斗逻辑：指挥存活分身攻击、发动暗影冲击（固定20点伤害）、召唤分身、复制玩家攻击、吸血攻击等，支持清空分身。
- **MirrorImage**：镜影分身类，继承Enemy。定义分身基础属性（HP=30、攻击=5、防御=1），仅进行普通攻击。


### com.thegame.battle
- **Skill**：技能接口，定义获取技能名称的`getName()`方法和释放技能的`apply()`方法（规定施法者和目标角色参数）。
- **HealSkill**：治疗术技能实现类，实现Skill接口。消耗5点MP为玩家恢复10点生命值，仅玩家可使用，提供技能释放逻辑及相关提示。
- **FireballSkill**：火球术技能实现类，实现Skill接口。消耗8点MP，造成11-15点随机伤害（最终伤害受目标防御影响，最低1点），仅玩家可使用，提供技能释放逻辑及相关提示。
- **PoisonStrikeSkill**：毒击技能实现类，实现Skill接口，提供技能名称“毒击”（具体技能逻辑未完全展示）。


### com.thegame.item
- **Inventory**：背包系统类，通过ArrayList存储道具。提供添加道具、使用指定索引道具、获取道具列表、清空道具、展示道具（带索引）、获取道具数量等功能。
- **ManaPotion**：法力药水道具类，实现Item接口，提供道具名称“法力药水”（用于恢复玩家MP，具体使用逻辑可推断）。


### com.thegame.map
- **GameMap**：游戏地图类（8×8矩阵）。管理地图矩阵、玩家位置、场景配置（字符与场景对象映射）；提供玩家移动逻辑（校验边界、地形）、获取当前场景、显示地图（带图例）、触发场景事件（遇怪、陷阱、战利品）等功能。
- **Location**：场景类，定义单个场景的属性，包括名称、描述、对应字符、战利品列表、可能出现的敌人类型、遇怪率、陷阱率、陷阱伤害等，作为GameMap的配置单元。


### com.thegame.main
- **NewGame**：游戏主类，包含main方法作为程序入口。负责初始化游戏（玩家、地图、初始道具）、运行游戏主循环（处理玩家移动、查看地图、退出等指令）、触发战斗流程、处理战斗结果（胜利/失败结局）等核心逻辑。

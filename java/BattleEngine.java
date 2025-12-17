import java.util.Random;
import java.util.Scanner;

public class BattleEngine {
    //扫描器:接收控制台输入
    private static final Scanner scanner = new Scanner(System.in);
    private static GameMap gameMap; // 游戏地图
    private static Player player;   // 玩家对象
    public static void main(String[] args){
        // 1. 初始化游戏
        initGame();

        // 2. 游戏主循环（探索+战斗）
        while (player.isAlive()) {
            System.out.println("\n===== 游戏指令 =====");
            System.out.println("w/a/s/d = 上下左右移动 | m = 查看地图 | quit = 退出游戏");
            System.out.print("请输入指令：");
            String command = scanner.nextLine().trim().toLowerCase();

            // 处理核心指令
            switch (command) {
                case "quit":
                    System.out.println("游戏结束！感谢游玩～");
                    scanner.close();
                    return;
                case "m":
                    gameMap.displayMap();
                    break;
                case "w": case "a": case "s": case "d":
                    // 移动+触发场景事件
                    String moveResult = gameMap.movePlayer(command);
                    System.out.println(moveResult);
                    // 触发场景事件（遇怪/陷阱/战利品）
                    Enemy encounterEnemy = gameMap.triggerSceneEvent(player);
                    // 若遇怪，触发战斗
                    if (encounterEnemy != null) {
                        startBattle(encounterEnemy);
                        // 若玩家战败，结束游戏
                        if (!player.isAlive()) {
                            System.out.println("你被击败了，游戏结束！");
                            scanner.close();
                            return;
                        }
                    }
                    break;
                default:
                    System.out.println("[错误] 无效指令!请输入w/a/s/d/m/quit");
            }
        }
    }

    private static void initGame() {
        // 初始化玩家（属性提升，适配Boss战斗）
        player = new Player("勇者", 80, 50, 12, 6);
        // 初始化地图
        gameMap = new GameMap();
        // 黎明村初始道具（安全区自动获取）
        Location village = gameMap.getSceneConfig().get('V');
        for (Item item : village.getLootItems()) {
            player.getInventory().addItem(item);
        }
        System.out.println("===== 欢迎来到黎明之地 =====");
        System.out.println("你醒来在迷雾森林边缘，目标是击败镜湖的镜影，拯救村庄！");
        System.out.println("输入m查看地图,w/a/s/d移动,quit退出游戏。");
    }

    /**
     * 触发战斗循环（玩家vs怪物）
     */
    private static void startBattle(Enemy enemy) {
        System.out.println("\n===== 战斗开始 =====");
        boolean playerStunned = false; // 玩家是否被眩晕（守护骑士盾击）

        while (player.isAlive() && enemy.isAlive()) {
            // 处理玩家中毒
            player.takePoisonDamage();
            if (!player.isAlive()) break;

            // 玩家回合（若未被眩晕）
            if (!playerStunned) {
                playerTurn(player, enemy);
            } else {
                System.out.println("[眩晕] 你被眩晕，无法行动！");
                playerStunned = false; // 眩晕仅持续1回合
            }
            if (!enemy.isAlive()) break;

            // 处理敌人中毒
            enemy.takePoisonDamage();
            if (!enemy.isAlive()) break;

            // 敌人回合
            enemyTurn(player, enemy);
            // 检查守护骑士盾击
            if (enemy instanceof GuardianKnight) {
                playerStunned = ((GuardianKnight) enemy).isShieldStun();
            }

            // 处理玩家临时防御
            player.reduceTempDefenseTurns();
        }

        // 战斗结束：胜利则获得战利品
        if (player.isAlive()) {
            System.out.printf("[胜利] 你击败了%s!%n", enemy.getName());
            // 场景战利品
            Location currentLoc = gameMap.getCurrentLocation();
            if (!currentLoc.getLootItems().isEmpty()) {
                // 随机选一个战利品（或直接取第一个）
                int randomIndex = new Random().nextInt(currentLoc.getLootItems().size());
                Item loot = currentLoc.getLootItems().get(randomIndex);
                player.getInventory().addItem(loot);
                System.out.printf("[战利品] 你获得了%s！%n", loot.getName());
            }
        }
        System.out.println("===== 战斗结束 =====");
    }

    // 以下为原有方法：playerTurn/castPlayerSkill/usePotion/enemyTurn/endBattle（无修改）
    private static void playerTurn(Player player, Enemy enemy) {
        System.out.println("\n===== 你的回合 =====");
        boolean isTurnEnd = false;

        while (!isTurnEnd) {
            System.out.print("请输入战斗指令(attack/skill/spell/status/potion):");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "attack":
                    player.attack(enemy);
                    isTurnEnd = true;
                    break;
                case "skill":
                    isTurnEnd = castPlayerSkill(player, enemy);
                    break;
                case "spell":
                    if (player.castSpell(enemy)) {
                        isTurnEnd = true;
                    }
                    break;
                case "status":
                    player.displayStatus();
                    enemy.displayStatus();
                    break;
                case "potion":
                    usePotion(player);
                    break;
                default:
                    System.out.println("[错误] 无效指令!请输入attack/skill/spell/status/potion");
            }
        }
    }

    /**
     * 玩家释放技能：显示技能列表→输入编号→释放
     * @return true=释放成功（消耗回合），false=释放失败（不消耗回合）
     */
    private static boolean castPlayerSkill(Player player, Enemy enemy) {
        // 1. 显示技能列表
        player.displaySkills();

        // 2. 输入技能编号
        System.out.print("请输入技能编号：");
        String input = scanner.nextLine().trim();
        int skillIndex;
        try {
            skillIndex = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("[技能] 请输入有效的数字编号！");
            return false;
        }

        // 3. 检查技能编号是否有效
        if (skillIndex < 0 || skillIndex >= player.getSkills().size()) {
            System.out.println("[技能] 无效的技能编号！");
            return false;
        }

        // 4. 释放技能（治疗术目标为自己，火球术目标为敌人）
        Skill skill = player.getSkills().get(skillIndex);
        Character target = skill.getName().equals("治疗术") ? player : enemy;
        return player.castSkill(skillIndex, target);
    }
    
    /**
     * 使用道具：显示背包→选择索引→使用道具
     */
    private static void usePotion(Player player) {
        Inventory inventory = player.getInventory();
        // 显示背包道具
        inventory.displayItems();

        // 背包为空则直接返回
        if (inventory.getItemCount() == 0) {
            return;
        }

        // 输入道具索引（容错处理）
        System.out.print("请输入要使用的道具索引：");
        String indexInput = scanner.nextLine().trim();
        int index;
        try {
            index = Integer.parseInt(indexInput);
        } catch (NumberFormatException e) {
            System.out.println("请输入有效的数字索引！");
            return;
        }

        // 使用道具
        inventory.useItem(index, player);
    }

    /**
     * 敌人回合：自动攻击玩家
     */
    private static void enemyTurn(Character player, Character enemy) {
        System.out.println("\n===== 敌人回合 =====");
        enemy.attack(player); // 敌人自动攻击玩家
    }

    /**
     * 战斗结束：输出最终结果
     */
    private static void endBattle(Character player, Character enemy) {
        System.out.println("\n===== 战斗结束 =====");
        if (player.isAlive() && !enemy.isAlive()) {
            System.out.printf("[胜利] 你击败了 %s!%n", enemy.getName());
        } else if (!player.isAlive() && enemy.isAlive()) {
            System.out.printf("[失败] 你被 %s 击败了!%n", enemy.getName());
        } else {
            System.out.println("[平局] 双方同归于尽!");
        }
    }
}

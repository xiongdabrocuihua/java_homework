import java.util.Random;
import java.util.Scanner;
import java.util.List;

public class BattleEngine {
    //扫描器:接收控制台输入
    private static final Scanner scanner = new Scanner(System.in);
    private static GameMap gameMap; // 游戏地图
    private static Player player;   // 玩家对象
    private static boolean gameRunning = true; // 控制游戏主循环
    public static void main(String[] args){
        // 1. 初始化游戏
        initGame();

        // 2. 游戏主循环（探索+战斗）
        while (gameRunning) {
            System.out.println("\n===== 游戏指令 =====");
            System.out.println("w/a/s/d = 上下左右移动 | m = 查看地图 | quit = 退出游戏");
            System.out.print("请输入指令：");
            String command = scanner.nextLine().trim().toLowerCase();

            // 处理核心指令
            switch (command) {
                case "quit":
                    System.out.println("游戏结束！感谢游玩～");
                    gameRunning = false;
                    break;
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
                        // 检查是否击败最终Boss（胜利结局）
                        if (encounterEnemy instanceof MirrorShadow && !encounterEnemy.isAlive()) {
                            victoryEnding(); // 触发胜利结局
                            gameRunning = false; // 结束游戏
                        }
                        // 检查玩家是否战败（失败结局）
                        if (!player.isAlive()) {
                            if (!failEnding(encounterEnemy)) { // 触发失败结局，返回false则退出
                                gameRunning = false;
                            }
                        }
                    }
                    break;
                default:
                    System.out.println("[错误] 无效指令!请输入w/a/s/d/m/quit");
            }
        }
        scanner.close();
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
        // ===== 战斗开始前保存玩家状态快照 =====
        player.savePreBattleState();

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

            // 处理镜影分身（若有）
            if (enemy instanceof MirrorShadow) {
                MirrorShadow mirrorBoss = (MirrorShadow) enemy;
                List<Enemy> mirrors = mirrorBoss.getMirrorImages();
                // 移除死亡的分身
                mirrors.removeIf(m -> !m.isAlive());
                // 检查所有分身是否存活
                boolean anyMirrorAlive = mirrors.stream().anyMatch(Enemy::isAlive);
                if (!enemy.isAlive() && !anyMirrorAlive) {
                    break; // 镜影和分身都死亡，战斗结束
                }
            }

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
            //Location currentLoc = gameMap.getCurrentLocation();
            if (enemy instanceof GuardianKnight || enemy instanceof MirrorShadow) {
                System.out.println("[战利品] Boss掉落了丰厚的奖励!");
                // 必掉：生命药剂×2 + 魔法药剂×1
                player.getInventory().addItem(new HealthPotion());
                player.getInventory().addItem(new HealthPotion());
                player.getInventory().addItem(new ManaPotion());
                System.out.println("你获得了:生命药剂×2 + 魔法药剂×1!");
            } else {
                // 普通怪物随机掉落
                Location currentLocation = gameMap.getCurrentLocation();
                if (!currentLocation.getLootItems().isEmpty()) {
                    int randomIndex = new Random().nextInt(currentLocation.getLootItems().size());
                    Item loot = currentLocation.getLootItems().get(randomIndex);
                    player.getInventory().addItem(loot);
                    System.out.printf("[战利品] 你获得了%s!%n", loot.getName());
                }
            }
            // 清空镜影分身
            if (enemy instanceof MirrorShadow) {
                ((MirrorShadow) enemy).clearMirrors();
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
            System.out.println("请输入有效的数字编号！");
            return false;
        }

        // 3. 检查技能编号是否有效
        if (skillIndex < 0 || skillIndex >= player.getSkills().size()) {
            System.out.println("无效的技能编号！");
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
    private static void enemyTurn(Player player, Enemy enemy) {
        System.out.println("\n===== 敌人回合 =====");
        // 仅当怪物存活时，调用技能/行动逻辑（核心：不能省略这行）
        if (enemy.isAlive()) {
            enemy.takeAction(player); // 直接调用子类的技能逻辑
        } else {
            System.out.printf("%s已死亡,无法行动!%n", enemy.getName());
        }
    }

    // 胜利结局（击败镜影）
    private static void victoryEnding() {
        System.out.println("\n=====================================");
        System.out.println("               胜利结局               ");
        System.out.println("=====================================");
        System.out.println("你拼尽全力击碎了镜影，它在消散前化作了你的模样——那是你曾经逃避的自己。");
        System.out.println("“我不是你的枷锁，只是你未敢面对的过去。”镜影的声音在空气中消散。");
        System.out.println("迷雾缓缓散去，阳光重新洒向黎明村，村民们的欢呼从远方传来。");
        System.out.println("你接纳了自己的过去，也守护了这片土地。");
        System.out.println("=====================================");
        System.out.println("              游戏结束                ");
        System.out.println("=====================================");
    }

    // 失败结局（玩家HP归0）
    private static boolean failEnding(Enemy enemy) {
        System.out.println("\n=====================================");
        System.out.println("               失败结局               ");
        System.out.println("=====================================");
        System.out.println("你倒在了冰冷的地面上，意识逐渐模糊。");
        System.out.println("迷雾重新笼罩了这片土地，黎明村的灯火一点点熄灭，森林陷入了永恒的沉寂。");
        System.out.println("=====================================");
        // 询问是否重试
        while (true) {
            System.out.print("是否重新开始？1.Yes 2.No：");
            String input = scanner.nextLine().trim();
            if (input.equals("1")) {
            // ===== 核心修改：恢复战斗前状态（不重置地图位置）=====
            player.restorePreBattleState();
            // 可选：清空敌人的特殊状态（如镜影的分身）
            if (enemy instanceof MirrorShadow) {
                ((MirrorShadow) enemy).clearMirrors();
            }
            return true; // 回到战斗前，重新开始这场战斗
            } else if (input.equals("2")) {
                System.out.println("感谢你的游玩，再见～");
                return false; // 退出游戏
            } else {
                System.out.println("[错误] 无效输入!请输入1或2");
            }
        }
    }
}

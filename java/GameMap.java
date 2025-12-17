import java.util.Arrays; // 需导入Arrays类用于深拷贝
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 游戏地图类（8×8版本）：管理地图矩阵、玩家位置、场景切换
 */
public class GameMap {
    // 8×8地图矩阵（#=山脉，. =空地，V/F/C/T/M=场景，@=玩家）
    private char[][] mapMatrix;
    // 场景配置：字符 → 场景对象
    private Map<java.lang.Character, Location> sceneConfig;
    // 玩家当前坐标（8×8矩阵索引：0~7）
    private int playerX;
    private int playerY;
    // 随机数（用于遇怪/陷阱/战利品）
    private Random random;

    private char[][] initialMapMatrix; // 初始地图快照

    // 初始化8×8地图
    public GameMap() {
        random = new Random();
        // 1. 初始化8×8地图矩阵（核心场景分布更均匀）
        mapMatrix = new char[][] {
            {'#', '#', '#', 'C', 'T', '#', '#', '#'},
            {'#', 'F', 'F', 'F', 'F', 'F', '#', '#'},
            {'#', 'F', '.', '.', '.', 'F', 'F', '#'},
            {'#', 'F', '.', '.', '.', 'F', 'F', '#'},
            {'#', 'V', '.', '.', '.', '.', 'F', '#'},
            {'#', '#', '#', '.', '.', '.', 'F', '#'},
            {'#', '#', 'T', '.', 'M', '.', '#', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#'}
        };
        // 2. 保存初始地图快照（深拷贝）
        initialMapMatrix = new char[mapMatrix.length][];
        for (int i = 0; i < mapMatrix.length; i++) {
            initialMapMatrix[i] = Arrays.copyOf(mapMatrix[i], mapMatrix[i].length);
        }
        // 3. 初始化玩家初始坐标（8×8矩阵：第3行第3列，索引从0开始）
        playerX = 3;
        playerY = 3;
        mapMatrix[playerX][playerY] = '@'; // 动态标记玩家位置
        // 4. 初始化场景配置（场景逻辑不变，仅地图位置调整）
        initSceneConfig();
    }

    /**
     * 初始化5个核心场景的配置（逻辑与之前一致）
     */
    private void initSceneConfig() {
        sceneConfig = new HashMap<java.lang.Character, Location>();
        // ===== 1. 黎明村（V）：安全区（8×8地图位置：第4行第1列）=====
        List<Item> villageLoot = List.of(new HealthPotion()); // 初始道具：生命药水×1
        Location village = new Location(
            "黎明村",
            "宁静的初始村庄。",
            'V',
            villageLoot
        );
        sceneConfig.put('V', village);

        // ===== 2. 迷雾森林（F）：史莱姆+哥布林，30%遇怪 =====
        List<Class<? extends Enemy>> forestEnemies = List.of(Slime.class, Goblin.class);
        List<Item> forestLoot = List.of(new HealthPotion(), new ManaPotion());
        Location forest = new Location(
            "迷雾森林",
            "树木遮天蔽日，雾气弥漫，能听到奇怪的声响。",
            'F',
            forestEnemies,
            forestLoot,
            0.3,    // 30%遇怪
            0.0,    // 无陷阱
            0
        );
        sceneConfig.put('F', forest);

        // ===== 3. 低语峡谷（C）：哥布林，30%遇怪+10%陷阱（HP-5） =====
        List<Class<? extends Enemy>> canyonEnemies = List.of(Goblin.class);
        List<Item> canyonLoot = List.of(new HealthPotion(), new DefensePotion());
        Location canyon = new Location(
            "低语峡谷",
            "峡谷中传来诡异低语，地面布满松动的碎石。",
            'C',
            canyonEnemies,
            canyonLoot,
            0.3,    // 30%遇怪
            0.1,    // 10%陷阱
            5       // 陷阱伤害5HP
        );
        sceneConfig.put('C', canyon);

        // ===== 4. 钟楼废墟（T）：小Boss守护骑士，必遇怪 =====
        List<Class<? extends Enemy>> towerEnemies = List.of(GuardianKnight.class);
        List<Item> towerLoot = List.of(new HealthPotion(), new DefensePotion(), new ManaPotion());
        Location tower = new Location(
            "钟楼废墟",
            "破败的钟楼里，守护骑士的灵魂在游荡。",
            'T',
            towerEnemies,
            towerLoot,
            1.0,    // 100%遇怪（必战斗）
            0.0,    // 无陷阱
            0
        );
        sceneConfig.put('T', tower);

        // ===== 5. 镜湖（M）：最终Boss镜影，必遇怪（8×8地图位置：第6行第4列）=====
        List<Class<? extends Enemy>> lakeEnemies = List.of(MirrorShadow.class);
        List<Item> lakeLoot = List.of(new HealthPotion(), new ManaPotion(), new DefensePotion());
        Location lake = new Location(
            "镜湖",
            "湖面如镜，倒映出不属于这个世界的影子。",
            'M',
            lakeEnemies,
            lakeLoot,
            1.0,    // 100%遇怪（必战斗）
            0.0,    // 无陷阱
            0
        );
        sceneConfig.put('M', lake);

        // 空地（.）：无场景
        sceneConfig.put('.', new Location("空地", "空旷的平地，无特殊事件。", '.', List.of()));
    }

    /**
     * 玩家移动逻辑（w=上，a=左，s=下，d=右）—— 无需修改，自动适配8×8边界
     * @param direction 移动方向
     * @return 移动结果提示
     */
    public String movePlayer(String direction) {
        // 1. 计算新坐标
        int newX = playerX;
        int newY = playerY;
        switch (direction.toLowerCase()) {
            case "w": newX--; break; // 上：行号-1
            case "a": newY--; break; // 左：列号-1
            case "s": newX++; break; // 下：行号+1
            case "d": newY++; break; // 右：列号+1
            default: return "[地图] 无效方向!请输入w/a/s/d(上下左右)";
        }

        // 2. 校验边界（8×8矩阵：0<=newX<8，0<=newY<8）
        if (newX < 0 || newX >= mapMatrix.length || newY < 0 || newY >= mapMatrix[0].length) {
            return "[地图] 无法移动！已到达地图边界。";
        }

        // 3. 校验是否是山脉（#：不可通行）
        if (mapMatrix[newX][newY] == '#') {
            return "[地图] 无法通过！前方是陡峭的山脉。";
        }

        // 4. 移动成功：更新坐标，刷新地图矩阵
        mapMatrix[playerX][playerY] = initialMapMatrix[playerX][playerY]; // 恢复原场景字符
        playerX = newX;
        playerY = newY;
        mapMatrix[playerX][playerY] = '@'; // 标记玩家新位置

        // 5. 获取当前场景，返回场景描述
        Location currentLoc = getCurrentLocation();
        return String.format("[地图] 你移动到了%s!%n[场景] %s", currentLoc.getName(), currentLoc.getDescription());
    }

    /**
     * 获取玩家当前位置的场景对象
     */
    public Location getCurrentLocation() {
        char currentChar = getOriginalSceneChar(playerX, playerY);
        return sceneConfig.getOrDefault(currentChar, 
            new Location("未知区域", "神秘的未知区域，无特殊事件。", currentChar, List.of()));
    }

    /**
     * 获取指定坐标的原始场景字符（排除玩家@）—— 适配8×8初始位置
     */
    private char getOriginalSceneChar(int x, int y) {
        // 从初始地图快照中获取原始字符（而非当前被修改的mapMatrix）
        return initialMapMatrix[x][y];
    }

    /**
     * 显示当前8×8地图（带图例）—— 无需修改，自动打印8行8列
     */
    public void displayMap() {
        System.out.println("\n===== 8×8 游戏地图 =====");
        // 打印8×8地图矩阵
        for (char[] row : mapMatrix) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
        // 打印图例
        System.out.println("===== 图例 =====");
        System.out.println("@ = 你的位置 | # = 山脉（不可通行） | V = 黎明村（安全区）");
        System.out.println("F = 迷雾森林（遇怪） | C = 低语峡谷（遇怪+陷阱） | T = 钟楼废墟（小Boss）");
        System.out.println("M = 镜湖（最终Boss） | . = 空地");
        System.out.println("===================");
        // 打印当前位置
        Location currentLoc = getCurrentLocation();
        System.out.printf("当前位置：%s（%s）%n", currentLoc.getName(), currentLoc.getDescription());
    }

    /**
     * 场景随机事件：遇怪/陷阱/战利品（逻辑不变）
     * @param player 玩家对象
     * @return 事件结果，若返回Enemy则触发战斗
     */
    public Enemy triggerSceneEvent(Player player) {
        Location currentLoc = getCurrentLocation();
        Enemy encounterEnemy = null;

        // 1. 触发陷阱（仅低语峡谷）
        if (random.nextDouble() < currentLoc.getTrapRate()) {
            int trapDmg = currentLoc.getTrapDamage();
            player.setHp(player.getHp() - trapDmg);
            System.out.printf("[陷阱] 你触发了陷阱！受到%d点伤害，当前HP：%d%n", trapDmg, player.getHp());
        }

        // 2. 触发遇怪
        if (random.nextDouble() < currentLoc.getEncounterRate() && !currentLoc.getEnemyTypes().isEmpty()) {
            // 随机选择怪物类型
            Class<? extends Enemy> enemyClass = currentLoc.getEnemyTypes().get(random.nextInt(currentLoc.getEnemyTypes().size()));
            try {
                // 实例化怪物（根据不同怪物初始化属性）
                if (enemyClass == Slime.class) {
                    encounterEnemy = new Slime("史莱姆", 30, 0, 6, 2);
                } else if (enemyClass == Goblin.class) {
                    encounterEnemy = new Goblin("哥布林", 35, 0, 7, 3);
                } else if (enemyClass == GuardianKnight.class) {
                    encounterEnemy = new GuardianKnight("守护骑士（小Boss）", 80, 0, 10, 6);
                } else if (enemyClass == MirrorShadow.class) {
                    encounterEnemy = new MirrorShadow("镜影（最终Boss）", 150, 0, 15, 8);
                }
                System.out.printf("[遇怪] %s出现在你面前！准备战斗！%n", encounterEnemy.getName());
            } catch (Exception e) {
                System.out.println("[错误] 怪物生成失败：" + e.getMessage());
            }
        }

        // 3. 随机获得战利品（安全区/战斗场景都可能触发）
        if (!currentLoc.getLootItems().isEmpty() && random.nextDouble() < 0.5) { // 50%概率获得战利品
            Item loot = currentLoc.getLootItems().get(random.nextInt(currentLoc.getLootItems().size()));
            player.getInventory().addItem(loot);
            System.out.printf("[战利品] 你在%s找到了%s！%n", currentLoc.getName(), loot.getName());
        }

        return encounterEnemy;
    }

    // 新增：获取场景配置（供BattleEngine初始化黎明村道具）
    public Map<java.lang.Character, Location> getSceneConfig() {
        return this.sceneConfig;
    }

    // Getter：玩家坐标（用于调试）
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
}
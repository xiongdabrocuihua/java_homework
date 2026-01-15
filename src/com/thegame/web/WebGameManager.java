package com.thegame.web;

import com.thegame.character.Enemy;
import com.thegame.character.GuardianKnight;
import com.thegame.character.MirrorShadow;
import com.thegame.character.Player;
import com.thegame.item.HealthPotion;
import com.thegame.item.Item;
import com.thegame.item.ManaPotion;
import com.thegame.map.GameMap;
import com.thegame.map.Location;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class WebGameManager {
    private static WebGameManager instance;
    private Player player;
    private GameMap gameMap;
    private boolean gameRunning;
    private Enemy currentEnemy;
    private boolean playerStunned;

    private WebGameManager() {
        initGame();
    }

    public static synchronized WebGameManager getInstance() {
        if (instance == null) {
            instance = new WebGameManager();
        }
        return instance;
    }

    public void initGame() {
        // 初始化玩家（属性提升，适配Boss战斗）
        player = new Player("勇者", 80, 50, 12, 6);
        // 初始化地图
        gameMap = new GameMap();
        // 黎明村初始道具（安全区自动获取）
        Location village = gameMap.getSceneConfig().get('V');
        for (Item item : village.getLootItems()) {
            player.getInventory().addItem(item);
        }
        gameRunning = true;
        currentEnemy = null;
        playerStunned = false;
    }

    // 捕获 System.out 的输出
    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos, true, "UTF-8"));
            action.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setOut(originalOut);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public String handleMove(String direction) {
        if (currentEnemy != null && currentEnemy.isAlive()) {
            return "{\"success\": false, \"message\": \"战斗中无法移动！\", \"logs\": \"战斗中无法移动！\"}";
        }
        
        String logs = captureOutput(() -> {
            String moveResult = gameMap.movePlayer(direction);
            System.out.println(moveResult);
            Enemy encounterEnemy = gameMap.triggerSceneEvent(player);
            if (encounterEnemy != null) {
                currentEnemy = encounterEnemy;
                player.savePreBattleState();
                System.out.println("\n===== 战斗开始 =====");
                System.out.println("遇到了 " + encounterEnemy.getName() + "!");
            }
        });

        return buildGameStateJson(logs);
    }

    public String handleBattleAction(String action, String param) {
        if (currentEnemy == null || !currentEnemy.isAlive()) {
            return "{\"success\": false, \"message\": \"不在战斗中\", \"logs\": \"不在战斗中\"}";
        }

        String logs = captureOutput(() -> {
            boolean isTurnEnd = false;
            
            // 玩家回合
            if (!playerStunned) {
                switch (action) {
                    case "attack":
                        player.attack(currentEnemy);
                        isTurnEnd = true;
                        break;
                    case "skill":
                        try {
                            int skillIndex = Integer.parseInt(param);
                            // 简单的技能目标判断：治疗对自己，其他对敌人
                            // 这里假设 skillIndex 1 是治疗 (HealSkill)，0 是攻击 (FireballSkill)
                            // 实际应该检查 Skill 类型，但为了简单，这里直接复用 castPlayerSkill 逻辑或者简化
                            // 让我们看看 Player.castSkill 签名: castSkill(int skillIndex, Character target)
                            // 我们需要判断目标。
                            // 查看 Player 代码，skills.get(1) 是 HealSkill。
                            if (skillIndex == 1) { // Heal
                                if(player.castSkill(skillIndex, player)) isTurnEnd = true;
                            } else {
                                if(player.castSkill(skillIndex, currentEnemy)) isTurnEnd = true;
                            }
                        } catch (Exception e) {
                            System.out.println("无效的技能指令");
                        }
                        break;
                    case "potion":
                         // 简化：这里需要 param 知道用哪个药水，或者自动使用
                         // 暂时只支持一种简单的逻辑：param 是药水索引
                         // 或者复用 usePotion 逻辑，但 usePotion 用了 Scanner。
                         // 我们这里简单实现：
                         usePotionByName(param);
                         // 吃药不消耗回合？看原代码 switch case "potion" break，没有 isTurnEnd=true
                         // 原代码 case "potion": usePotion(player); break; 
                         // usePotion 内部如果是有效使用，应该算回合吗？
                         // 查看 NewGame.java:197 case "potion" -> usePotion(player) -> break (not setting isTurnEnd=true)
                         // 似乎吃药不算回合？或者 usePotion 内部没返回 boolean。
                         // 让我们假设吃药不算回合，或者这只是个菜单。
                         // 为了简化，假设吃药不消耗回合。
                        break;
                    default:
                        System.out.println("未知指令");
                }
            } else {
                System.out.println("[眩晕] 你被眩晕，无法行动！");
                playerStunned = false; 
                isTurnEnd = true; // 眩晕跳过回合
            }

            if (isTurnEnd) {
                // 检查敌人是否死亡
                if (!currentEnemy.isAlive()) {
                    handleEnemyDeath();
                } else {
                    // 敌人回合
                    // 处理中毒
                    player.takePoisonDamage();
                    if (player.isAlive()) {
                         enemyTurnLogic();
                    }
                }
            }
        });

        return buildGameStateJson(logs);
    }
    
    private void usePotionByName(String potionName) {
        // 简单的药水使用逻辑
        List<Item> items = player.getInventory().getItems();
        Item toUse = null;
        for (Item item : items) {
            if (item.getName().contains(potionName) || item.getClass().getSimpleName().equalsIgnoreCase(potionName)) {
                toUse = item;
                break;
            }
        }
        
        if (toUse != null) {
            toUse.use(player);
            player.getInventory().getItems().remove(toUse);
            System.out.println("使用了 " + toUse.getName());
        } else {
            System.out.println("没有找到 " + potionName);
        }
    }

    private void enemyTurnLogic() {
        // 敌人中毒
        currentEnemy.takePoisonDamage();
        if (!currentEnemy.isAlive()) {
            handleEnemyDeath();
            return;
        }

        // 镜影分身逻辑
        if (currentEnemy instanceof MirrorShadow) {
            MirrorShadow mirrorBoss = (MirrorShadow) currentEnemy;
            List<Enemy> mirrors = mirrorBoss.getMirrorImages();
            mirrors.removeIf(m -> !m.isAlive());
            boolean anyMirrorAlive = mirrors.stream().anyMatch(Enemy::isAlive);
            if (!currentEnemy.isAlive() && !anyMirrorAlive) {
                handleEnemyDeath(); // 应该不会走到这里，因为前面 currentEnemy.isAlive() 检查过了
                return;
            }
        }

        // 敌人攻击
        // 原代码直接调用 enemyTurn(player, enemy)，但在 NewGame 类中是 private static。
        // 我们需要在 WebGameManager 重新实现或者反射调用。
        // 简单起见，重新实现核心逻辑：
        currentEnemy.attack(player);
        
        // 检查守护骑士盾击
        if (currentEnemy instanceof GuardianKnight) {
            playerStunned = ((GuardianKnight) currentEnemy).isShieldStun();
        }

        // 处理玩家临时防御
        player.reduceTempDefenseTurns();
        
        if (!player.isAlive()) {
             System.out.println("你被打败了...");
             // 简单的失败处理：重置
             // player.restorePreBattleState(); // 可选：自动重置
        }
    }

    private void handleEnemyDeath() {
        System.out.printf("[胜利] 你击败了%s!%n", currentEnemy.getName());
        // 掉落逻辑
        if (currentEnemy instanceof GuardianKnight || currentEnemy instanceof MirrorShadow) {
            System.out.println("[战利品] Boss掉落了丰厚的奖励!");
            player.getInventory().addItem(new HealthPotion());
            player.getInventory().addItem(new HealthPotion());
            player.getInventory().addItem(new ManaPotion());
            System.out.println("你获得了:生命药剂×2 + 魔法药剂×1!");
        } else {
             Location currentLocation = gameMap.getCurrentLocation();
             if (!currentLocation.getLootItems().isEmpty()) {
                int randomIndex = new Random().nextInt(currentLocation.getLootItems().size());
                Item loot = currentLocation.getLootItems().get(randomIndex);
                player.getInventory().addItem(loot);
                System.out.printf("[战利品] 你获得了%s!%n", loot.getName());
             }
        }
        
        if (currentEnemy instanceof MirrorShadow) {
            ((MirrorShadow) currentEnemy).clearMirrors();
            System.out.println("恭喜！你通关了游戏！");
        }
        
        currentEnemy = null;
        System.out.println("===== 战斗结束 =====");
    }

    public String handleUseItem(String indexStr) {
        String logs = captureOutput(() -> {
            try {
                int index = Integer.parseInt(indexStr);
                List<Item> items = player.getInventory().getItems();
                if (index >= 0 && index < items.size()) {
                    Item item = items.get(index);
                    item.use(player);
                    items.subList(index, index + 1).clear(); // 使用后移除
                    System.out.println("使用了 " + item.getName());
                    
                    // 如果是在战斗中，使用药水是否算一回合？
                    // 根据原版逻辑，NewGame.java 中 usePotion 后没有结束回合。
                    // 这里保持一致，不强制结束回合。
                } else {
                    System.out.println("无效的物品索引");
                }
            } catch (NumberFormatException e) {
                System.out.println("无效的物品索引格式");
            }
        });
        return buildGameStateJson(logs);
    }

    public String getStatus() {
        String logs = ""; // 状态查询不产生新日志
        return buildGameStateJson(logs);
    }
    
    // 构建 JSON 响应
    private String buildGameStateJson(String logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"player\": {");
        sb.append("\"name\": \"").append(player.getName()).append("\",");
        sb.append("\"hp\": ").append(player.getHp()).append(",");
        sb.append("\"maxHp\": ").append(player.getMaxHp()).append(","); 
        sb.append("\"mp\": ").append(player.getMp()).append(",");
        sb.append("\"maxMp\": ").append(player.getMaxMp()).append(",");
        sb.append("\"attack\": ").append(player.getAttack()).append(",");
        sb.append("\"defense\": ").append(player.getDefense());
        sb.append("},");
        
        sb.append("\"inventory\": [");
        List<Item> items = player.getInventory().getItems();
        for (int i = 0; i < items.size(); i++) {
            sb.append("\"").append(items.get(i).getName()).append("\"");
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"location\": \"").append(gameMap.getCurrentLocation().getDescription()).append("\",");
        sb.append("\"inBattle\": ").append(currentEnemy != null && currentEnemy.isAlive()).append(",");
        
        if (currentEnemy != null && currentEnemy.isAlive()) {
            sb.append("\"enemy\": {");
            sb.append("\"name\": \"").append(currentEnemy.getName()).append("\",");
            sb.append("\"hp\": ").append(currentEnemy.getHp());
            sb.append("},");
        }
        
        // 处理 logs 中的换行符，避免破坏 JSON
        String safeLogs = logs.replace("\n", "\\n").replace("\r", "").replace("\"", "\\\"");
        sb.append("\"logs\": \"").append(safeLogs).append("\",");

        // Map Data
        sb.append("\"map\": [");
        char[][] matrix = gameMap.getMapMatrix();
        for (int i = 0; i < matrix.length; i++) {
            sb.append("[");
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append("\"").append(matrix[i][j]).append("\"");
                if (j < matrix[i].length - 1) sb.append(",");
            }
            sb.append("]");
            if (i < matrix.length - 1) sb.append(",");
        }
        sb.append("],");

        // Structured Inventory
        sb.append("\"inventoryDetails\": [");
        List<Item> invItems = player.getInventory().getItems();
        for (int i = 0; i < invItems.size(); i++) {
            sb.append("{");
            sb.append("\"index\": ").append(i).append(",");
            sb.append("\"name\": \"").append(invItems.get(i).getName()).append("\"");
            sb.append("}");
            if (i < invItems.size() - 1) sb.append(",");
        }
        sb.append("]");
        
        sb.append("}");
        return sb.toString();
    }
}

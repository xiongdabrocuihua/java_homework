import java.util.Scanner;

public class BattleEngine {
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args){
        Character player = new Player("玩家",40,15,7,3);
        Character enemy = new Enemy("敌人",20,0,4,1);

        //显示双方状态
        player.displayStatus();
        enemy.displayStatus();

        //战斗流程
        System.out.println("===== 战斗开始 =====");
        System.out.println("可用命令:attack(攻击)、status(查看状态)");
        System.out.println("====================");

        //敌人攻击玩家
        while (player.isAlive() && enemy.isAlive()) {
            // ---- 玩家回合 ----
            playerTurn(player, enemy);
            
            // 玩家攻击后，若敌人已死亡，直接结束循环
            if (!enemy.isAlive()) {
                break;
            }

            // ---- 敌人回合 ----
            enemyTurn(player, enemy);
        }

        // 3. 战斗结束：输出结果
        endBattle(player, enemy);
        scanner.close(); // 关闭扫描器
    }
    /**
     * 玩家回合：接收控制台命令，执行攻击/查看状态
     */
    private static void playerTurn(Character player, Character enemy) {
        System.out.println("\n===== 你的回合 =====");
        String command = "";
        // 循环接收命令，直到输入有效指令（attack/status）
        while (true) {
            System.out.print("请输入命令（attack/status）：");
            command = scanner.nextLine().trim().toLowerCase(); // 转小写，忽略大小写
            if (command.equals("attack") || command.equals("status")) {
                break; // 指令有效，退出循环
            } else {
                System.out.println("[错误] 无效命令！请输入 attack 或 status");
            }
        }

        // 执行玩家指令
        switch (command) {
            case "attack":
                player.attack(enemy); // 玩家攻击敌人
                break;
            case "status":
                player.displayStatus(); // 查看玩家状态
                enemy.displayStatus();  // 同时查看敌人状态
                break;
        }
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
            System.out.printf("[胜利] 你击败了 %s！%n", enemy.getName());
        } else if (!player.isAlive() && enemy.isAlive()) {
            System.out.printf("[失败] 你被 %s 击败了！%n", enemy.getName());
        } else {
            System.out.println("[平局] 双方同归于尽！");
        }
    }
}

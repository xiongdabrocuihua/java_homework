import java.util.ArrayList;
import java.util.List;

/**
 * 背包系统：管理道具的添加、使用、展示
 */
public class Inventory {
    // 存储道具的容器（ArrayList支持动态增删）
    private List<Item> items;

    // 初始化背包
    public Inventory() {
        items = new ArrayList<>();
    }

    /**
     * 添加道具到背包
     * @param item 要添加的道具
     */
    public void addItem(Item item) {
        items.add(item);
        System.out.printf("[背包] 获得「%s」!%n", item.getName());
    }

    /**
     * 使用指定索引的道具
     * @param index 道具在背包中的索引（从0开始）
     * @param user 使用道具的角色
     * @return true=使用成功，false=索引无效/使用失败
     */
    public boolean useItem(int index, Character user) {
        // 校验索引有效性
        if (index < 0 || index >= items.size()) {
            System.out.println("[背包] 无效的道具索引！");
            return false;
        }

        // 取出并使用道具
        Item item = items.get(index);
        item.use(user);
        // 使用后从背包移除道具
        items.remove(index);
        return true;
    }

    /**
     * 展示背包内所有道具（带索引，方便选择）
     */
    public void displayItems() {
        System.out.println("\n===== 你的背包 =====");
        if (items.isEmpty()) {
            System.out.println("背包空空如也~");
        } else {
            for (int i = 0; i < items.size(); i++) {
                System.out.printf("索引 %d → %s%n", i, items.get(i).getName());
            }
        }
        System.out.println("====================");
    }

    /**
     * 获取背包道具数量（用于判断是否有道具可使用）
     */
    public int getItemCount() {
        return items.size();
    }
}
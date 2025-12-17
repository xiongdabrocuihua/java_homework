public interface Skill {
    /**
     * 获取技能名称
     * @return 技能名（如“火球术”）
     */
    String getName();

    /**
     * 释放技能（核心逻辑）
     * @param caster 施法者（释放技能的角色）
     * @param target 技能目标（被攻击/治疗的角色）
     * @return true=释放成功，false=MP不足/释放失败
     */
    boolean apply(Character caster, Character target);
}

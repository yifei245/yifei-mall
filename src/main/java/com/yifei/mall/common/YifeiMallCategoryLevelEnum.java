
package com.yifei.mall.common;

/**
 * @author wangfei
 * @description : 分类级别
 * @date : 2022/11/18 15:00
 */
public enum YifeiMallCategoryLevelEnum {

    DEFAULT(0, "ERROR"),
    LEVEL_ONE(1, "一级分类"),
    LEVEL_TWO(2, "二级分类"),
    LEVEL_THREE(3, "三级分类");

    private int level;

    private String name;

    YifeiMallCategoryLevelEnum(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public static YifeiMallCategoryLevelEnum getYifeiMallOrderStatusEnumByLevel(int level) {
        for (YifeiMallCategoryLevelEnum yifeiMallCategoryLevelEnum : YifeiMallCategoryLevelEnum.values()) {
            if (yifeiMallCategoryLevelEnum.getLevel() == level) {
                return yifeiMallCategoryLevelEnum;
            }
        }
        return DEFAULT;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

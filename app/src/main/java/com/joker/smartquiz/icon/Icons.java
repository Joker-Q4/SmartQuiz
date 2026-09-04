package com.joker.smartquiz.icon;

import com.joanzapata.iconify.Icon;

/**
 * @author Joker
 * @since 2019/10/26 0026 16:02
 */
public enum Icons implements Icon {

    fang_da('\ue6d2'),           //放大
    yin_cang('\ue675'),           //隐藏
    da_an('\ue628'),           //看答案
    close('\ue60f');           //关闭

    private final char character;

    Icons(char character) {
        this.character = character;
    }

    @Override
    public String key() {
        return name().replace('_', '-');
    }

    @Override
    public char character() {
        return character;
    }
}

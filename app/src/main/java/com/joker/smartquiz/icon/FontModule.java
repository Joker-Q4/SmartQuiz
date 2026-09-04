package com.joker.smartquiz.icon;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

/**
 * @author Joker
 * @since 2019/10/26 0026 16:03
 */
public class FontModule implements IconFontDescriptor {

    @Override
    public String ttfFileName() {
        return "iconfont.ttf";
    }

    @Override
    public Icon[] characters() {
        return Icons.values();
    }
}

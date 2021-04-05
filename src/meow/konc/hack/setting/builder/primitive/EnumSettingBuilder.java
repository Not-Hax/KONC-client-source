package meow.konc.hack.setting.builder.primitive;

import meow.konc.hack.setting.Setting;
import meow.konc.hack.setting.builder.SettingBuilder;
import meow.konc.hack.setting.impl.EnumSetting;

/**
 * Created by 086 on 14/10/2018.
 */
public class EnumSettingBuilder<T extends Enum> extends SettingBuilder<T> {
    Class<? extends Enum> clazz;

    public EnumSettingBuilder(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Setting<T> build() {
        return new EnumSetting<>(initialValue, predicate(), consumer(), name, visibilityPredicate(), clazz);
    }
}

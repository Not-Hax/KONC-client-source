package meow.konc.hack.setting.builder.primitive;

import meow.konc.hack.setting.builder.SettingBuilder;
import meow.konc.hack.setting.impl.StringSetting;

/**
 * Created by 086 on 13/10/2018.
 */
public class StringSettingBuilder extends SettingBuilder<String> {
    @Override
    public StringSetting build() {
        return new StringSetting(initialValue, predicate(), consumer(), name, visibilityPredicate());
    }
}

package meow.konc.hack.setting.builder.primitive;

import meow.konc.hack.setting.builder.SettingBuilder;
import meow.konc.hack.setting.impl.BooleanSetting;

/**
 * Created by 086 on 13/10/2018.
 */
public class BooleanSettingBuilder extends SettingBuilder<Boolean> {
    @Override
    public BooleanSetting build() {
        return new BooleanSetting(initialValue, predicate(), consumer(), name, visibilityPredicate());
    }

    @Override
    public BooleanSettingBuilder withName(String name) {
        return (BooleanSettingBuilder) super.withName(name);
    }
}

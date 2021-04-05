package meow.konc.hack.event.events.other;

import meow.konc.hack.event.KONCEvent;
import net.minecraft.entity.Entity;

public class EntityUseTotemEvent extends KONCEvent {
    private Entity entity;

    public EntityUseTotemEvent(Entity entity) {
        super();
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
package mage.abilities.common;

import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DamagedBatchAllEvent;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;

import java.util.stream.Stream;

/**
 * This triggers only once for each combat damage step the source creature deals damage.
 * So a creature blocked by two creatures and dealing damage to both blockers in the same
 * combat damage step triggers only once.
 *
 * @author LevelX, xenohedron
 */
public class DealsCombatDamageTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<DamagedEvent> {

    public DealsCombatDamageTriggeredAbility(Effect effect, boolean optional) {
        super(Zone.BATTLEFIELD, effect, optional);
        setTriggerPhrase(getWhen() + "{this} deals combat damage, ");
        this.withRuleTextReplacement(true);
    }

    protected DealsCombatDamageTriggeredAbility(final DealsCombatDamageTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DealsCombatDamageTriggeredAbility copy() {
        return new DealsCombatDamageTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_BATCH_FOR_ALL;
    }

    @Override
    public Stream<DamagedEvent> filterBatchEvent(GameEvent event, Game game) {
        if (!(event instanceof DamagedBatchAllEvent)) {
            return Stream.empty();
        }
        return ((DamagedBatchAllEvent) event)
                .getEvents()
                .stream()
                .filter(DamagedEvent::isCombatDamage)
                .filter(e -> e.getAttackerId().equals(getSourceId()));
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        int amount = filterBatchEvent(event, game)
                .mapToInt(GameEvent::getAmount)
                .sum();
        if (amount < 1) {
            return false;
        }
        this.getEffects().setValue("damage", amount);
        return true;
    }
}

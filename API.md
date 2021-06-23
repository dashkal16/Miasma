# Miasma API
The Miasma API can be found at `name.dashkal.minecraft.miasma.api.MiasmaModAPI`.

Only classes from the api module should be accessed by API clients. This includes avoiding the `*Impl` classes under the
`api` package that are defined in the main module.

Internal classes may be modified in breaking ways even in point releases.

## Miasma Modifiers
Miasma Modifiers, or implementations of `name.dashkal.minecraft.miasma.api.capability.IMiasmaModifier` allow API clients
to interact with infections in a manner than is attached to something, often a piece of equipment.

There is also a more traditional event based API discussed below.

### Contributing a piece of modifying gear
Attach an instance of a `IMiasmaModifier` capability to an item in `Item.initCapabilities`. There are static helper
methods on `IMiasmaModifier` to ease creating a simple capability provider. The provided capability handler does _not_
provide side sensitivity or serialization/deserialization capabilities.

See `name.dashkal.minecraft.miasma.testmod.ProtectionHelmetItem` or
`name.dashkal.minecraft.miasma.testmod.ProtectionRingItem` for examples.

### Contributing a new type of Modifier
`IMiasmaModifier` may also be used outside of a capability context

To do so, first contribute a new `name.dashkal.minecraft.miasma.api.imc.MiasmaModifierLocator` by sending Miasma an IMC
message during `InterModEnqueueEvent`:
```java
public class IMCHandler {
  public static void onInterModEnqueueEvent(InterModEnqueueEvent event) {
    InterModComms.sendTo(
        "miasma",
        ModifierLocator.IMC_METHOD,
        new ModifierLocator(
            new ResourceLocation(
                MyMod.MODID,
                "my_miasma_mods"
            ),
            250,
            entity -> getModifiers(entity)
        )
    );
  }
  
  private List<IMiasmaModifier> getModifiers(LivingEntity entity) {
      // Your logic here
  }
}
```

`ModifierLocator`'s constructor takes three arguments:
* `resourceLocation`
    * A unique identifier for the modifier locator
* `priority`
    * A priority used to sort modifier locators (processing stops if a `IMiasmaModifier` check method returns false, so
      order matters).
        * Lower priority numbers are checked first
        * Held items are checked at priority 0.<br/>
        * Armor pieces are checked at priority 100.<br/>
        * If both Miasma Integrations and Curios are installed, curios are checked at priority 200.
* `locatorFunction`
    * A `java.util.Function<LivingEntity, List<IMiasmaModifier>>` that will be called to obtain modifiers for the given
      entity. The implementation of this function is free to add modifiers or not based on the state of the entity.

See `name.dashkal.minecraft.miasma.integration.MiasmaIntegrationMod` for an example.

## Miasma Events
A more traditional event based API for interacting with miasma events also exists.

### Responding to Miasma Events
Subscribe to any subclass of `name.dashkal.minecraft.miasma.api.event.MiasmaEvent`. All of these events are fired on the
`net.minecraft.forge.MinecraftForge.EVENT_BUS` bus.

Canceling a cancelable event will prevent the action from occurring.
Some events also allow property modifiers to be added, allowing modification of infection variables by event handlers.

See `name.dashkal.minecraft.miasma.testmod.MiasmTestMod` for examples.

## Miasma Property Modifiers
There is a property modifier system inspired by Attributes that allows API clients to request modification to infection
variables.

### Miasma Modifiers
If using `IMiasmaModifier`, implementations may create an instance of `MiasmaPropertyModifiers` via the builder:
```java
public class MyItem {
  public static MiasmaPropertyModifiers getModifiers() {
    MiasmaPropertyModifiers.Builder builder = new MiasmaPropertyModifiers.Builder();
    builder.addModifier(MiasmaPropertyModifierType.CLEANSE_STAGE_TIME, Fraction.ONE_THIRD);
    builder.addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(3, 1));
    builder.addModifier(MiasmaPropertyModifierType.DAMAGE, Fraction.ONE_THIRD);
    return builder.build();
  }
}
```
`org.apache.commons.lang3.math.Fraction` is available from Apache Commons Lang, which Minecraft Forge depends on.

See `name.dashkal.minecraft.miasma.testmod.ProtectionHelmetItem` for an example.

### Miasma Events
If using events, property modifiers may be added with the `addModifier` method available on `InfectionPreApplyEvent` and
`InfectionPrePulseEvent`:
```java
public class MyHandler {
    public static void onInfectionPreApplyEvent(MiasmaEvent.InfectionPreApplyEvent event) {
      event.addModifier(MiasmaPropertyModifierType.INTENSIFY_STAGE_TIME, Fraction.getFraction(1, 10));
    }
}
```

See `name.dashkal.minecraft.miasma.testmod.MiasmTestMod` for an example.

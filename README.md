CallableHorses

## For mod and modpack authors

Which entities can be bound as a personal horse is decided entirely by the entity type tag
`callablehorses:callablehorses`. Nothing else is checked, so any rideable mob can be made
callable from a datapack or from inside another mod's jar — no code, no dependency on this
mod.

Add `data/callablehorses/tags/entity_type/callablehorses.json`:

```json
{
	"replace": false,
	"values": [
		"yourmod:your_mount"
	]
}
```

`"replace": false` merges your entries with everyone else's. Tags from mod jars and datapacks
are combined, so several mods can extend the list independently.

Modpacks can also go the other way and shrink the list with `"replace": true` (or a NeoForge
`"remove"` entry) to, say, allow donkeys but not horses.

Requirements for an entity to work:

- it must be a `Mob` — calling it uses the entity's own pathfinding to walk it over, and the
  stat viewer renders it as a living entity
- the player must be able to ride it, since binding happens while mounted

Vanilla behaviour is unchanged: the shipped tag lists horses, donkeys, mules, llamas, skeleton
horses and zombie horses.

### Known limitations for non-horse mounts

- A called mount is rebuilt from stored NBT with a **new UUID**. Mods that track a pet through
  its entity UUID will lose the link.
- A mount's own internal inventory survives the death-and-respawn path. Only vanilla equipment
  slots, and a horse's saddle/chest inventory, are cleared, so anything a mount keeps elsewhere -
  a pig's saddle, a modded mount's own container - comes back with the respawned copy.
- Because of that, with `deathIsPermanent` off a bound mount that is not horse-family drops
  nothing at all when it dies: no loot, no equipment, no experience. Dropping its gear *and*
  handing back a copy that still holds it would duplicate it. Horse-family mounts are unaffected
  and still drop their armour, saddle and chest contents where they died; only their loot table is
  suppressed, and only while `disableHorseDrops` is on.
- Mounts that depend on state stored outside the entity, such as a linked block entity, may
  not survive being saved and restored.

# Changelog

## [2.0.0] - 2026-08-04

- Rewritten as a multiloader mod: Fabric, Forge and NeoForge are now supported
- Updated to Minecraft 1.21.1
- Any rideable mob can now be a personal horse, not just horses. Eligibility is decided
  entirely by the `callablehorses:callablehorses` entity type tag, so mods and modpacks can
  extend or restrict the list from a datapack. See the README
- **Breaking:** entities from other mods that extend `AbstractHorse` were previously callable
  automatically. They now need to be in the tag, which their author or a one-file datapack can
  add. The vanilla horse types are unchanged


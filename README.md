# Zrub Moda

Client-side appearance override mod for Minecraft Java 1.21.4, Fabric, Java 21.

## Build

Install JDK 21 and run `./gradlew build` (Windows: `gradlew.bat build`). The Gradle wrapper can be generated with `gradle wrapper --gradle-version 8.12` if it is not included.

## Use

Press **N** to open the item picker. Search by translated item name or registry ID, click an item to apply it to the item currently held in the main hand, and use the mode button to switch the saved mode. Config is stored at `.minecraft/config/zrub-moda.json`.

## Important implementation notes

The mod is client-side and does not intentionally mutate inventory stacks or send packets. The current renderer hook replaces the resolved baked model, not the underlying stack. The transform selector is persisted in configuration; full per-context first-/third-person transform control requires additional renderer hooks and should be validated against the exact Yarn mappings before release. No Gradle wrapper or compiled JAR is checked in yet.
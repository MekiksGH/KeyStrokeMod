# KeyStrokeMod

A clean, client-side [Fabric](https://fabricmc.net/) **keystrokes overlay** for **Minecraft 1.21.11**.
It shows your **W A S D**, **space bar** and **LMB / RMB**, each lighting up the instant you press it —
and you can **drag it anywhere** on screen and **resize it** to taste.

## Features

- Live keystrokes display: WASD + space + LMB/RMB, inverting colour while held
- **Move it anywhere** — drag the overlay in the editor
- **Resize it** — drag the corner grip (scale 0.5×–3.0×)
- Reads your actual movement/attack/use keybinds, so it respects rebinds
- Its own **"KeyStrokeMod"** category in *Options → Controls → Key Binds*
- Position and size persist across launches (`config/keystrokemod.properties`)
- Client-side only — no server install, no gameplay changes

## Usage

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) and
   [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11.
2. Drop `keystrokemod-1.0.0.jar` into your `mods` folder.
3. Go to **Options → Controls → Key Binds → KeyStrokeMod** and bind a key
   (default **K**) to *Open Editor*.
4. Press that key in game, then **drag** the overlay to move it and **drag the
   corner grip** to resize. Hit **Done** to save.

## Building from source

Requires **JDK 21**.

```bash
./gradlew build   # -> build/libs/keystrokemod-1.0.0.jar
```

## License

Released under the [MIT License](LICENSE).

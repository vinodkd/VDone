# Icon Generation

Source: `design/ic_launcher.svg`  
Font: DejaVu Sans Bold (must be installed; ships with most Linux distros)

## Regenerate all mipmap PNGs

Run from the project root:

```bash
SVG=design/ic_launcher.svg
RES=app/src/main/res

for size_dir in "48:mipmap-mdpi" "72:mipmap-hdpi" "96:mipmap-xhdpi" "144:mipmap-xxhdpi" "192:mipmap-xxxhdpi"; do
  size="${size_dir%%:*}"
  dir="${size_dir##*:}"
  inkscape --export-type=png --export-width=$size --export-height=$size \
           --export-filename="$RES/$dir/ic_launcher.png" "$SVG"
  cp "$RES/$dir/ic_launcher.png" "$RES/$dir/ic_launcher_round.png"
done
```

## Adaptive icon foreground (transparent background)

The `mipmap-anydpi-v26/` adaptive icon references `@drawable/ic_launcher_foreground`.  
That drawable is the vector XML (`drawable/ic_launcher_foreground.xml`) — regenerate it
by adjusting the path data if the design changes, or replace it with a PNG rendered from
a transparent-background variant of the SVG.

# Third-party notices

## Fritzing-derived controller assets — CC BY-SA 3.0

These distributed circuit assets use graphics from the [official Fritzing parts repository](https://github.com/fritzing/fritzing-parts):

- `hardware/ZenPongController.fzz`, including its embedded custom parts and graphics;
- `hardware/ZenPongController.svg` and `hardware/ZenPongController_schematic.svg`.

They are licensed under [Creative Commons Attribution-ShareAlike 3.0 Unported](https://creativecommons.org/licenses/by-sa/3.0/), consistent with [Fritzing's official license notice](https://github.com/fritzing/fritzing-parts/blob/master/LICENSE.txt). The [full license](https://creativecommons.org/licenses/by-sa/3.0/legalcode.en) supplies the applicable terms and warranty disclaimer. Retain attribution, identify modifications, and share adaptations under the same license.

**Attribution:** This image was created with Fritzing. Upstream part credits, where supplied:

| Source part | Upstream credit |
| --- | --- |
| [RSR 03MB102 Breadboard](https://github.com/fritzing/fritzing-parts/blob/master/core/breadboard2.fzp) | GorillaCoder |
| [Arduino Uno (Rev3) - ICSP](https://github.com/fritzing/fritzing-parts/blob/master/core/arduino_uno%28rev3%29-icsp.fzp) | althaus |
| [Pushbutton](https://github.com/fritzing/fritzing-parts/blob/master/core/pushbutton_4_horizontal.fzp) | Fritzing project; no individual author specified |
| [Resistor](https://github.com/fritzing/fritzing-parts/blob/master/core/resistor_5band.fzp) | Brendan Howell |
| [Piezo Speaker](https://github.com/fritzing/fritzing-parts/blob/master/core/piezo%20sensor.fzp) | Lionel Michel |
| [Rotary Potentiometer (Large)](https://github.com/fritzing/fritzing-parts/blob/master/core/potentiometer_rotary_16mm_5.fzp) | Brendan Howell; modified by A. Faina |

Zen Pong contributors adapted the layout, custom controls, routing, colors and English annotations for this controller. The `.fzz` embeds its custom parts; standard parts are resolved by a normal Fritzing installation. A standalone core part library is not distributed here.

## Original work — MIT

The game, Arduino firmware, tests, tools, documentation and game screenshots use the [MIT License](LICENSE). That license does not replace CC BY-SA 3.0 for the controller assets listed above.

## External development tools

Processing, its Serial library, the Java runtime, Arduino CLI, the AVR core and Fritzing are installed separately and are not redistributed by this repository.

The original melody and effects are generated from note frequencies in the source; the game does not include sampled music, external fonts or downloaded artwork.

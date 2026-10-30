# Serial protocol

Use **115200 baud, 8 data bits, no parity, 1 stop bit**, with ASCII messages terminated by LF (`\n`). The firmware ignores CR. Both programs must use protocol version 3.

## Controller → computer

```text
READY,ZENPONG,3
I,x,y,pause,slower,faster,visual
```

Identification is sent at boot and once per second. Input is sent every 20 ms. X and Y are integers in −1000…1000, with a ±65 ADC-count center dead zone applied before normalization. The default orientation expects negative Y for up and positive Y for down; the sketch can invert either axis.

The four button fields are 0 (released) or 1 (pressed), after 25 ms of stable electrical state. The computer treats the first valid input packet as a baseline and acts on later rising edges, preventing a held button from activating on connection. Simultaneous new slower/faster presses cancel each other. Invalid field counts, out-of-range values and incompatible identifiers are ignored.

## Computer → controller

| Message | Meaning |
| --- | --- |
| `H` | Heartbeat, every 200 ms after identification |
| `S,TITLE` | Title state; stop the buzzer |
| `S,PLAY` | Playing state; stop the previous buzzer output |
| `S,PAUSE` | Paused state; stop the buzzer |
| `T,hz,ms` | Play a tone: 0…5000 Hz and 0…2000 ms |

Zero frequency or duration stops the buzzer. Each tone replaces the previous tone; there is no polyphony. The PC schedules melody and effect notes without blocking the rendering loop. PC sound uses the same pitches and durations but has a separate volume control.

## Timeouts

- Input is considered fresh for 400 ms. Stale controller input does not move the paddle or show CONNECTED.
- An unidentified port is closed after 6 seconds. A previously identified port is closed after 1.5 seconds without valid input.
- The Arduino stops the buzzer after 1.5 seconds without a valid host command.
- The firmware bounds command length and serial work per loop. This is a small local-device protocol without authentication.

Example exchange after a port opens:

```text
Arduino: READY,ZENPONG,3
PC:      S,TITLE
Arduino: I,0,0,0,0,0,0
PC:      H
Arduino: I,0,0,1,0,0,0
PC:      S,PLAY
PC:      T,523,65
```

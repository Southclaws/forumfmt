[COLOR=#FF4700][SIZE=7][B]percent[/B][/SIZE][/COLOR]

You should use [FONT=courier new]%e[/FONT] instead of [FONT=courier new]%s[/FONT] in SQL calls.

[CODE]
format(str, [COLOR=Purple]128[/COLOR], [COLOR=Purple]"%s"[/COLOR], s);
[/CODE]

